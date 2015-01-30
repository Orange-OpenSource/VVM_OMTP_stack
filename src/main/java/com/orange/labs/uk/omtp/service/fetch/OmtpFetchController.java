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
package com.orange.labs.uk.omtp.service.fetch;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.orange.labs.uk.omtp.account.OmtpAccountStoreWrapper;
import com.orange.labs.uk.omtp.config.StackStaticConfiguration;
import com.orange.labs.uk.omtp.fetch.VoicemailFetcherFactory;
import com.orange.labs.uk.omtp.imap.SynchronizationCallback;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.notification.SourceNotifier;
import com.orange.labs.uk.omtp.voicemail.LocalVoicemailProvider;
import com.orange.labs.uk.omtp.voicemail.Voicemail;
import com.orange.labs.uk.omtp.voicemail.VoicemailIntentUtils;
import com.orange.labs.uk.omtp.voicemail.VoicemailPayload;

/**
 * Contains the logic for handling fetch requests on behalf of the {@link OmtpFetchService}.
 * <p>
 * This class is not thread safe for concurrent access. The calls to the
 * {@link #onHandleFetchIntent(Intent)} method will be made sequentially with a guarantee of memory
 * visibility between calls, see the {@link OmtpFetchService} class documentation for more details.
 * <p>
 * Note also that the fetch operation should complete before {@link #onHandleFetchIntent(Intent)}
 * method returns, i.e. should not be performed by yet another background thread, else our original
 * caller will think that we are done and may begin performing the next fetch immediately.
 */
@NotThreadSafe
public class OmtpFetchController {
	private static final Logger logger = Logger.getLogger(OmtpFetchController.class);

	/** Over a 3G network, fetching one message by IMAP can take > 10s. */
	private static final long TIME_TO_WAIT_FOR_RESULT_MS = 40000;

	private final VoicemailFetcherFactory mVoicemailFetcherFactory;
	private final LocalVoicemailProvider mVoicemailProviderHelper;
	private final Context mContext;
	private final OmtpAccountStoreWrapper mAccountStore;
	private final SourceNotifier mNotifier;
	private final AtomicInteger mAttempts = new AtomicInteger();

	public OmtpFetchController(Context context, OmtpAccountStoreWrapper accountStore,
			VoicemailFetcherFactory voicemailFetcherFactory,
			LocalVoicemailProvider voicemailProviderHelper, SourceNotifier notifier) {
		mContext = context;
		mAccountStore = accountStore;
		mVoicemailFetcherFactory = voicemailFetcherFactory;
		mVoicemailProviderHelper = voicemailProviderHelper;
		mNotifier = notifier;
	}

	public void onHandleFetchIntent(Intent intent) {
		// Work out which Voicemail this intent corresponds to fetching.
		String identifier = VoicemailIntentUtils.extractIdentifierFromIntent(intent);
		// Initialise voicemail object
		Voicemail voicemail = null;

		// Identifier can be null if the original Intent comes from the Android
		// call log, extracting the id from the Voicemail ContenProvider.
		if (identifier == null) {
			logger.d("Voicemail ID is null, tryeing to get voicemail object byUri data");
			// Trying to get the identifier for the local vvm store.
			voicemail = mVoicemailProviderHelper.findVoicemailByUri(intent.getData());
			if (voicemail == null || voicemail.getSourceData().isEmpty()) {
				logger.e("Asked to fetch for intent without identifier: " + intent);
				return;
			}

			identifier = voicemail.getSourceData();
			logger.d(String.format("Identifier: %s", identifier));
		} else {
			logger.d(String.format("Trying to get Voicemail object with ID:%s", identifier));
			voicemail = mVoicemailProviderHelper.findVoicemailBySourceData(identifier);

			// if voicemail is still null, try to get it by Uri from local store
			if (voicemail == null) {
				Uri data = intent.getData();
				logger.d(String.format(
						"Getting Voicemail object by ID has failed, trying by Uri:%s", data));
				voicemail = mVoicemailProviderHelper.findVoicemailByUri(data);
				if (voicemail != null) {
					identifier = voicemail.getSourceData();
				}
			}
		}

		// this should never happened, but better to prevent an exception later
		if (identifier == null) {
			logger.w("Application was unable to determine Voicemal identifier and will not " +
					"fetch the message.");
			return;
		}
		
		logger.d(String.format("Received onHandleFetchIntent(\"%s\" for identifier:%s)",
				intent, identifier));

		VoicemailPayload fetchedPayload = null;

		mAttempts.set(StackStaticConfiguration.MAX_IMAP_ATTEMPTS);
		do {
			FetchAttachmentCallback callback = new FetchAttachmentCallback(mContext, mNotifier,
					mAccountStore, mAttempts);

			// Fire off a fetch request and wait synchronously for the result.
			// Retry up to
			// N times specified in StackConfiguration.MAX_IMAP_ATTEMPTS if the
			// operation fails.
			mVoicemailFetcherFactory.createVoicemailFetcher().fetchVoicemailPayload(identifier,
					callback);
			fetchedPayload = callback.waitForResult();

			// Update the retry indicator in case it failed.
		} while (fetchedPayload == null && mAttempts.get() > 0);

		if (check(fetchedPayload != null, "Missing payload", identifier)) {
			try {
				if (check(voicemail != null, "Inexistent voicemail", identifier)) {
					// put the fetched content in the right place
					logger.d(voicemail.toString());
					mVoicemailProviderHelper.setVoicemailContent(voicemail.getUri(),
							fetchedPayload.getBytes(), fetchedPayload.getMimeType());
				}
			} catch (IOException e) {
				logger.e("Couldn't write payload to content provider", e);
				return;
			}
		}
	}

	private boolean check(boolean check, String message, Object object) {
		if (!check) {
			logger.e(message + ": " + object);
			return false;
		}
		return true;
	}

	/**
	 * Helper class used as a callback that also allows a thread to wait for the result.
	 */
	private class FetchAttachmentCallback extends SynchronizationCallback<VoicemailPayload> {

		public FetchAttachmentCallback(Context context, SourceNotifier notifier,
				OmtpAccountStoreWrapper accountStore, AtomicInteger attempts) {
			super(context, notifier, accountStore, attempts);
		}

		private final CountDownLatch mIsComplete = new CountDownLatch(1);
		private volatile VoicemailPayload mResult;

		@Override
		public void onFailure(Exception error) {
			if (!shouldRetry(error)) {
				super.onFailure(error);
			}

			mIsComplete.countDown();
		}

		@Override
		public void onSuccess(VoicemailPayload result) {
			mAttemptsLeft.decrementAndGet(); // in case result is null.
			mResult = result;
			mIsComplete.countDown();
		}

		/**
		 * Waits for the asynchronous result of the callback to complete.
		 * <p>
		 * Returns the voicemail and the payload that we retrieved. Returns null if the thread was
		 * interrupted, if there was an exception of any sort fetching the data from the server, or
		 * if the timeout expired (i.e. the fetch took too long).
		 */
		@Nullable
		private VoicemailPayload waitForResult() {
			try {
				mIsComplete.await(TIME_TO_WAIT_FOR_RESULT_MS, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// Restore interrupt status and fall through.
				Thread.currentThread().interrupt();
			}
			return mResult;
		}
	}
}
