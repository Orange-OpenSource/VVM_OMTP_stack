/*
 * Copyright (C) 2011 The Android Open Source Project Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package com.orange.labs.uk.omtp.sync;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;

import android.content.Context;
import android.content.Intent;

import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.callbacks.Callbacks;
import com.orange.labs.uk.omtp.fetch.VoicemailFetcherFactory;
import com.orange.labs.uk.omtp.greetings.Greeting;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.voicemail.Voicemail;
import com.orange.labs.uk.omtp.voicemail.VoicemailIntentUtils;

/**
 * VvmStore implementation backed by an OMTP voicemail service.
 */
@ThreadSafe
public class OmtpVvmStore implements VvmStore {

	private static Logger logger = Logger.getLogger(OmtpVvmStore.class);

	/**
	 * The {@code action} sent when we wish to fetch the content of a voicemail message.
	 */
	public static final String FETCH_INTENT = "com.orange.labs.uk.omtp.VOICEMAIL_FETCH";

	/**
	 * The {@code action} sent by the android call log to fetch the content of a voicemail message.
	 */
	public static final String ANDROID_FETCH_INTENT = "android.intent.action.FETCH_VOICEMAIL";

	/**
	 * The {@code action} sent when we wish to fetch the content of a greetings message.
	 */
	public static final String FETCH_GREETING_INTENT = "com.orange.labs.uk.omtp.GREETING_FETCH";

	private final VoicemailFetcherFactory mVoicemailFetcherFactory;
	private final Executor mExecutor;
	private final Context mContext;
	private final MirrorVvmStore mMirrorStore;

	public OmtpVvmStore(VoicemailFetcherFactory voicemailFetcherFactory, Executor executor,
			Context context, MirrorVvmStore store) {
		mMirrorStore = store;
		mVoicemailFetcherFactory = voicemailFetcherFactory;
		mExecutor = executor;
		mContext = context;
	}

	/**
	 * Perform the provided {@link Action} on the store. The provided callback is invoked when an
	 * operation fails or when the whole set of operation has succeeded.
	 */
	@Override
	public void performActions(final List<VvmStore.Action> actions, final Callback<Void> callback) {

		if (actions == null || actions.size() == 0) {
			// if the list is empty, execute the callback directly.
			callback.onSuccess(null);
			return;
		}

		mExecutor.execute(new Runnable() {
			@Override
			public void run() {
				AtomicInteger actionsSize = new AtomicInteger(actions.size());
				AtomicBoolean failureReported = new AtomicBoolean(false);

				for (VvmStore.Action action : actions) {
					ResultCallback resultCallback = new ResultCallback(action, actionsSize,
							callback, failureReported);
					performSingleAction(action, resultCallback);
				}
			}
		});
	}

	/**
	 * Return all messages contain by the store through the provided callback.onSuccess method.
	 */
	@Override
	public void getAllMessages(final Callback<List<Voicemail>> callback) {
		mVoicemailFetcherFactory.createVoicemailFetcher().fetchAllVoicemails(callback);
	}

	/**
	 * The delete all messages operation is not supported on the remote store.
	 */
	@Override
	public void deleteAllMessages(Callback<Void> callback) {
		throw new UnsupportedOperationException("Not implemented for Remote Store");
	}

	/**
	 * Execute a single action on the store. Once the action has been performed, the provided
	 * callback's method is invoked.
	 * 
	 * @param action
	 *            Action to apply on the store.
	 * @param callback
	 *            Callback to be invoked while it's done (successfuly or not).
	 */
	private void performSingleAction(Action action, Callback<Void> callback) {
		Voicemail message = action.getVoicemail();
		Operation operation = action.getOperation();

		switch (operation) {
		case DELETE:
			mVoicemailFetcherFactory.createVoicemailFetcher().markVoicemailsAsDeleted(callback,
					message);
			break;
		case MARK_AS_READ:
			mVoicemailFetcherFactory.createVoicemailFetcher().markVoicemailsAsRead(callback,
					message);
			break;
		case INSERT:
			// Inserting into a remote OMTP store isn't supported.
			throw new UnsupportedOperationException("Cannot insert new message into remote store");
		case FETCH_VOICEMAIL_CONTENT:
			logger.d(String
					.format("Broadcasting a fetch Intent for voicemail message: %s", message));
			mContext.sendBroadcast(VoicemailIntentUtils.createFetchIntent(message));
			callback.onSuccess(null);
			break;
		case FETCH_GREETING_CONTENT: {
			Greeting greeting = action.getGreeting();
			Intent fetchIntent = VoicemailIntentUtils.createFetchIntent(greeting);
			logger.d(String
					.format("Broadcasting a fetch Intent %s, for Greeting message: %s", fetchIntent, 
							greeting));
			mContext.sendBroadcast(fetchIntent);
			callback.onSuccess(null);
		}
			break;
		default:
			break;
		}
	}

	private class ResultCallback implements Callback<Void> {

		/** Action being executed */
		private final Action mAction;
		/** Callback to invoke when all actions have been performed */
		private final Callback<Void> mCallback;
		/** Number of actions that remain */
		private final AtomicInteger mActionsRemaining;
		/** Indicate if a failure has been reported, we don't report twice */
		private final AtomicBoolean mFailureReported;

		public ResultCallback(Action action, AtomicInteger actionsRemaining,
				Callback<Void> callback, AtomicBoolean failureReported) {
			mAction = action;
			mCallback = callback;
			mActionsRemaining = actionsRemaining;
			mFailureReported = failureReported;
		}

		@Override
		public void onSuccess(Void result) {
			logger.i(String.format("Action successful: %s", mAction.toString()));
			if (!mAction.getOperation().equals(VvmStore.Operation.FETCH_VOICEMAIL_CONTENT)) {
				mMirrorStore.performActions(mAction, Callbacks.<Void> emptyCallback());
			}
			operationCompleted();
		}

		@Override
		public void onFailure(Exception error) {
			logger.w(String.format("Action failed: %s", mAction.toString()));
			if (!mFailureReported.getAndSet(true)) {
				mCallback.onFailure(error);
			}
			operationCompleted();
		}

		private void operationCompleted() {
			if (mActionsRemaining.decrementAndGet() == 0)
				if (!mFailureReported.get()) {
					mCallback.onSuccess(null);
				}
		}
	}

}
