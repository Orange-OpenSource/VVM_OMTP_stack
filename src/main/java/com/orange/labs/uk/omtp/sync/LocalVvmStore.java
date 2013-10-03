/*
 * Copyright (C) 2011 Google Inc. All Rights Reserved.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.callbacks.Callbacks;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.voicemail.LocalVoicemailProvider;
import com.orange.labs.uk.omtp.voicemail.Voicemail;
import com.orange.labs.uk.omtp.voicemail.VoicemailImpl;
import com.orange.labs.uk.omtp.voicemail.VoicemailIntentUtils;

/**
 * Implementation of the VvmStore backed by the local voicemail content provider.
 */
@ThreadSafe
public class LocalVvmStore implements VvmStore {

	private static final Logger logger = Logger.getLogger(LocalVvmStore.class);

	/** Voicemail Provider to manage Android Voicemail Content Provider */
	private final LocalVoicemailProvider mVoicemailProviderHelper;
	/** Used to execute asynchronous operation */
	private final Executor mExecutor;
	/** Content to broadcast fetching Intent */
	private final Context mContext;
	/** Used to mirror changes, such as insertions */
	private final MirrorVvmStore mMirrorStore;

	public LocalVvmStore(Executor executor, LocalVoicemailProvider voicemailProviderHelper,
			Context context, MirrorVvmStore store) {
		mVoicemailProviderHelper = voicemailProviderHelper;
		mExecutor = executor;
		mContext = context;
		mMirrorStore = store;
	}

	@Override
	public void getAllMessages(final Callback<List<Voicemail>> callback) {
		mExecutor.execute(new Runnable() {
			@Override
			public void run() {
				callback.onSuccess(mVoicemailProviderHelper.getAllVoicemails());
			}
		});
	}

	@Override
	public void deleteAllMessages(final Callback<Void> callback) {
		mExecutor.execute(new Runnable() {

			@Override
			public void run() {
				int deletions = mVoicemailProviderHelper.deleteAll();
				if (deletions > 0) {
					callback.onSuccess(null);
				} else {
					callback.onFailure(new VvmStoreException("Failed to delete all messages"));
				}
			}
		});
	}

	@Override
	public void performActions(final List<VvmStore.Action> actions, final Callback<Void> callback) {
		if (actions == null || actions.size() == 0) {
			callback.onSuccess(null);
			return;
		}

		final Map<VvmStore.Operation, List<Voicemail>> actionsMap = VvmStoreActions
				.buildMap(actions);

		final AtomicBoolean failureReported = new AtomicBoolean(false);
		final AtomicInteger remaining = new AtomicInteger(actionsMap.size());

		mExecutor.execute(new Runnable() {

			@Override
			public void run() {
				for (VvmStore.Operation operation : actionsMap.keySet()) {
					List<Voicemail> messages = actionsMap.get(operation);
					// No need to check if empty, no operation if not one
					// element at least.
					if (messages.size() > 1) {
						ActionsMirrorCallback mirrorCallback = new ActionsMirrorCallback(callback,
								remaining, failureReported);
						performMultipleActions(operation, messages, mirrorCallback);
					} else {
						ActionMirrorCallback mirrorCallback = new ActionMirrorCallback(callback,
								remaining, failureReported);
						performSingleAction(operation, messages.get(0), mirrorCallback);
					}

				}
			}

		});
	}

	private void performMultipleActions(VvmStore.Operation operation, List<Voicemail> messages,
			ActionsMirrorCallback callback) {
		switch (operation) {
		case MARK_AS_READ:
			Map<Uri, Voicemail> messMap = buildMarkAsReadMap(messages);
			if (mVoicemailProviderHelper.update(messMap) == messages.size()) {
				callback.onSuccess(VvmStoreActions.createActions(messages, operation));
				return;
			}
			break;
		case INSERT:
			removeAlreadyInserted(messages); // filter messages to exclude already inserted ones.
			if (messages.size() == 0) {
				logger.w("All messages were already inserted in Local VVM Store.");
				callback.onSuccess(null);
				return;
			}

			List<Uri> uris = mVoicemailProviderHelper.insert(messages);
			if (uris.size() > 0 && uris.size() == messages.size()) {

				// Fetching content for all messages
				for (int i = 0; i < messages.size(); i++) {
					broadcastFetchIntent(messages.get(i), uris.get(i));
				}

				callback.onSuccess(VvmStoreActions.createActions(insertedList(uris, messages),
						operation));
				return;
			}
			break;
		case DELETE:
			int rows = mVoicemailProviderHelper.delete(getMessagesUris(messages));
			if (rows == messages.size()) {
				callback.onSuccess(VvmStoreActions.createActions(messages, operation));
				return;
			}
			break;
		case FETCH_VOICEMAIL_CONTENT:
			throw new UnsupportedOperationException("Local store cannot FETCH_CONTENT or UPLOAD_GREETING");
		case FETCH_GREETING_CONTENT:
			break;
		default:
			break;
		}

		// if we went that far, it failed...
		callback.onFailure(new VvmStoreException(operation, messages));
	}

	private void performSingleAction(VvmStore.Operation operation, Voicemail message,
			ActionMirrorCallback callback) {
		switch (operation) {
		case INSERT: {
			if (!isAlreadyInserted(message)) {
				Uri newMsgUri = mVoicemailProviderHelper.insert(message);
				if (newMsgUri != null) {
					// Send intent to fetch the content of the voicemail.
					broadcastFetchIntent(message, newMsgUri);

					// Create a new action containing the inserted URI.
					VvmStore.Action action = VvmStoreActions.insert(VoicemailImpl
							.createCopyBuilder(message).setUri(newMsgUri).build());
					callback.onSuccess(action);
					return;
				}
			} else {
				logger.w(String.format(
						"Message (ID: %s) was already present in the local vvm store",
						message.getSourceData()));
				callback.onSuccess(null); // don't want to insert twice in Mirror.
				return;
			}
			break;
		}
		case DELETE:
			if (mVoicemailProviderHelper.delete(message.getUri()) > 0) {
				callback.onSuccess(VvmStoreActions.delete(message));
				return;
			}
			break;
		case MARK_AS_READ:
			if (mVoicemailProviderHelper.update(message.getUri(), VoicemailImpl
					.createEmptyBuilder().setIsRead(true).build()) > 0) {
				callback.onSuccess(VvmStoreActions.markAsRead(message));
				return;
			}
			break;
		case FETCH_VOICEMAIL_CONTENT:
			throw new UnsupportedOperationException("Local store cannot FETCH_CONTENT or UPLOAD_GREETING");
		default:
			break;
		}

		// if we went that far it failed.
		callback.onFailure(new VvmStoreException(operation, message));
	}

	/**
	 * Check if the provided message is already present in the {@link LocalVvmStore}. If it is, it
	 * also checks if the content is present. If not, it broadcasts a FETCH intent.
	 * 
	 * @param message
	 *            Message to check.
	 * @return boolean indicating if the message has already been inserted before.
	 */
	private boolean isAlreadyInserted(Voicemail message) {
		Voicemail providerMessage = mVoicemailProviderHelper.findVoicemailBySourceData(message
				.getSourceData());

		if (providerMessage != null) {
			logger.d(String.format("[Existing Voicemail] Exists in CP: %s", message.toString()));
			if (!providerMessage.hasContent() && providerMessage.hasUri()) {
				logger.d("[Existing Voicemail] No content. Sending Fetch Intent.");
				broadcastFetchIntent(providerMessage, providerMessage.getUri());
			}
		}

		return (providerMessage != null);
	}

	/**
	 * Remove from the provided {@link List} of {@link Voicemail} the ones that are already present
	 * in the Content Provider using the isAlreadyInserted method.
	 * 
	 * @param messages
	 *            {@link List} of {@link Voicemail} that needs to be checked against the CP.
	 */
	private void removeAlreadyInserted(List<Voicemail> messages) {
		Iterator<Voicemail> it = messages.iterator();
		while (it.hasNext()) {
			Voicemail message = it.next();
			if (isAlreadyInserted(message)) {
				it.remove();
			}
		}
	}

	/**
	 * Extract a {@link List} of {@link Voicemail} {@link Uri}.
	 * 
	 * @param messages
	 *            {@link Voicemail} instances we should get the {@link Uri} from.
	 * @return a {@link List} that contains voicemails {@link Uri}
	 */
	private List<Uri> getMessagesUris(List<Voicemail> messages) {
		List<Uri> uris = new ArrayList<Uri>(messages.size());
		for (Voicemail voicemail : messages) {
			uris.add(voicemail.getUri());
		}
		return uris;
	}

	/**
	 * Build a new {@link List} of {@link Voicemail} with a copy of all of them associated with
	 * their URI. The two lists must have the same size.
	 * 
	 * @param insertedUris
	 *            Uris of inserted voicemails, should be in the same order as the provided
	 *            {@link Voicemail} list.
	 * @param messages
	 *            {@link List} of {@link Voicemail}
	 * @return a new {@link List} containing a copy of the original {@link Voicemail} with their
	 *         {@link Uri} parameter set.
	 */
	private List<Voicemail> insertedList(List<Uri> insertedUris, List<Voicemail> messages) {
		List<Voicemail> list = new ArrayList<Voicemail>(insertedUris.size());
		for (int i = 0; i < insertedUris.size(); i++) {
			list.add(VoicemailImpl.createCopyBuilder(messages.get(i)).setUri(insertedUris.get(i))
					.build());
		}

		return list;
	}

	/**
	 * Creates a {@link Map} that associates newly read {@link Voicemail} to a {@link Voicemail}
	 * instance with its read attribute set to true. This map can then be used to update the batch
	 * of voicemails pointed by the {@link Uri} {@link List}.
	 */
	private Map<Uri, Voicemail> buildMarkAsReadMap(List<Voicemail> voicemails) {
		Map<Uri, Voicemail> messages = new HashMap<Uri, Voicemail>(voicemails.size());
		Voicemail readVoicemail = VoicemailImpl.createEmptyBuilder().setIsRead(true).build();
		for (Voicemail voicemail : voicemails) {
			messages.put(voicemail.getUri(), readVoicemail);
		}
		return messages;
	}

	/**
	 * Broadcast an {@link Intent} to the system to ask the {@link Voicemail} to be fetched. A
	 * {@link Uri} of the messages also requires to be provided.
	 */
	private void broadcastFetchIntent(Voicemail message, Uri newMsgUri) {
		Intent fetchIntent = new Intent(OmtpVvmStore.FETCH_INTENT, newMsgUri);
		VoicemailIntentUtils.storeIdentifierInIntent(fetchIntent, message);
		mContext.sendBroadcast(fetchIntent);
	}

	/**
	 * Callback for an unique {@link Action}.
	 */
	private class ActionMirrorCallback extends MirrorCallback<Action> {

		public ActionMirrorCallback(Callback<?> callback, AtomicInteger actionsRemaining,
				AtomicBoolean failureReported) {
			super(callback, actionsRemaining, failureReported);
		}

		@Override
		public void onSuccess(Action result) {
			if (result != null) {
				logger.d("[Action Success] Mirroring...");
				mMirrorStore.performActions(result, Callbacks.<Void> emptyCallback());
			}
			operationCompleted();
		}

	}

	/**
	 * Callback for a {@link List} of {@link Action}.
	 */
	private class ActionsMirrorCallback extends MirrorCallback<List<Action>> {

		public ActionsMirrorCallback(Callback<?> callback, AtomicInteger actionsRemaining,
				AtomicBoolean failureReported) {
			super(callback, actionsRemaining, failureReported);
		}

		@Override
		public void onSuccess(List<Action> result) {
			if (result != null && result.size() > 0) {
				logger.d("[Action Success] Mirroring...");
				mMirrorStore.performActions(result, Callbacks.<Void> emptyCallback());
			}
			operationCompleted();
		}
	}

	/**
	 * Abstract callback dedicated to mirror actions on the {@link MirrorVvmStore}. The
	 * operationCompleted() method should be executed after each onFailure and onSuccess.
	 */
	private abstract class MirrorCallback<T> implements Callback<T> {
		private final Callback<?> mCallback;

		private final AtomicBoolean mFailureReported;
		private final AtomicInteger mActionsRemaining;

		public MirrorCallback(Callback<?> callback, AtomicInteger actionsRemaining,
				AtomicBoolean failureReported) {
			mCallback = callback;
			mActionsRemaining = actionsRemaining;
			mFailureReported = failureReported;
		}

		@Override
		public abstract void onSuccess(T result);

		@Override
		public void onFailure(Exception error) {
			logger.d(String.format("[Action Failed] %s", error.getMessage()));
			if (!mFailureReported.getAndSet(true)) {
				mCallback.onFailure(error);
			}
			operationCompleted();
		}

		protected void operationCompleted() {
			logger.d(String.format("[Operation Completed] Remaining: %d", mActionsRemaining.get()));
			if (mActionsRemaining.decrementAndGet() == 0) {
				if (!mFailureReported.get()) {
					logger.d("LocalVvmStore >> Success!");
					mCallback.onSuccess(null);
				}
			}
		}
	}

}
