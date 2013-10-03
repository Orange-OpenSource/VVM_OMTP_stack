package com.orange.labs.uk.omtp.service.fetch;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import android.content.Context;
import android.content.Intent;

import com.orange.labs.uk.omtp.account.OmtpAccountStoreWrapper;
import com.orange.labs.uk.omtp.config.StackStaticConfiguration;
import com.orange.labs.uk.omtp.fetch.VoicemailFetcherFactory;
import com.orange.labs.uk.omtp.greetings.Greeting;
import com.orange.labs.uk.omtp.greetings.GreetingUpdateType;
import com.orange.labs.uk.omtp.greetings.GreetingsHelper;
import com.orange.labs.uk.omtp.greetings.database.LocalGreetingsProvider;
import com.orange.labs.uk.omtp.imap.SynchronizationCallback;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.notification.SourceNotifier;
import com.orange.labs.uk.omtp.voicemail.VoicemailIntentUtils;
import com.orange.labs.uk.omtp.voicemail.VoicemailPayload;

/**
 * Used to fetch Greetings messages content. 
 */
public class GreetingsFetchController {
	private static final Logger logger = Logger.getLogger(GreetingsFetchController.class);

	public static final long TIME_TO_WAIT_FOR_RESULT_MS = 50000;

	private final Context mAppContext;
	private final OmtpAccountStoreWrapper mAccountStore;
	private final GreetingsHelper mGreetingsHelper;
	private final VoicemailFetcherFactory mVoicemailFetcherFactory;
	private final SourceNotifier mNotifier;
	private final LocalGreetingsProvider mLocalGreetingProvider;
	private final AtomicInteger mAttempts = new AtomicInteger();

	/**
	 *
	 * @param appContext
	 * @param accountStore
	 * @param greetingsHelper
	 * @param voicemailFetcherFactory
	 * @param sourceNotifier
	 */
	public GreetingsFetchController(Context appContext, OmtpAccountStoreWrapper accountStore,
			GreetingsHelper greetingsHelper, VoicemailFetcherFactory voicemailFetcherFactory,
			SourceNotifier sourceNotifier, LocalGreetingsProvider localGreetingProvider) {
		mAppContext = appContext;
		mAccountStore = accountStore;
		mGreetingsHelper = greetingsHelper;
		mVoicemailFetcherFactory = voicemailFetcherFactory;
		mNotifier = sourceNotifier;
		mLocalGreetingProvider = localGreetingProvider;
	}

	public void onHandleFetchIntent(Intent intent) {
		
		if (intent != null) {
			String greetingUid = VoicemailIntentUtils.extractIdentifierFromIntent(intent);
			logger.d(String.format(
					"In onHandleIntent() of GreetingsFetchController with greeting Uid:%s",
					greetingUid));
			
			if (greetingUid != null) {
				
				Greeting greeting = null;
				
				// get Greeting object from DB in a loop with retries.
				// This is required because sometimes the operation of inserting greeting to
				// local db has not been finished before we ask local db to retrieve 
				// this new Greeting object
				// TODO: think about a better idea of doing it
				mAttempts.set(StackStaticConfiguration.MAX_IMAP_ATTEMPTS);
				do {
					greeting = mLocalGreetingProvider.getGreetingWithUid(greetingUid);
				} while (greeting == null && mAttempts.get() > 0);
				
				if (greeting != null) {
					fetchGreetingsVoiceAttachment(greeting);
				} else {
					logger.w(String.format("It has not been possible find the Greeting with the ID=%s in the local " +
							"db. Remote Greeting fetch is not possible.", greetingUid));
				}
			} else {
				logger.w("It has not been possible to Fetch greeting, because it's ID is null!");
			}
			
		}
	}

	/**
	 * Triggers actions related to fetching Greeting voice attachment content.
	 * 
	 * @param greeting
	 *            object used to retrieve Greeting content
	 */
	private void fetchGreetingsVoiceAttachment(Greeting greeting) {
		VoicemailPayload fetchedPayload = null;
		mAttempts.set(StackStaticConfiguration.MAX_IMAP_ATTEMPTS);
		do {
			FetchAttachmentCallback callback = new FetchAttachmentCallback(mAppContext,
					mNotifier, mAccountStore, mAttempts);

			// Fire off a fetch request and wait synchronously for the result.
			// Retry up to N times specified in StackConfiguration.MAX_IMAP_ATTEMPTS if the
			// operation fails.
			mVoicemailFetcherFactory.createVoicemailFetcher().fetchGreetingPayload(
					callback, greeting);
			fetchedPayload = callback.waitForResult();

			// Update the retry indicator in case it failed.
		} while (fetchedPayload == null && mAttempts.get() > 0);

		if (fetchedPayload != null) {
			// Save greeting file
			mGreetingsHelper.updateGreetingsFile(fetchedPayload.getBytes(),
					greeting.getGreetingType());

			// update local db, set current Greeting downloaded state as true
			boolean setDownloadedStateResult = mLocalGreetingProvider
					.setDownloadedStateTrue(greeting);
			logger.d(String.format("State downloaded voice set to true success:%s", 
					setDownloadedStateResult));
			
			// Send Greetings success notification to Source application
			mGreetingsHelper
					.notifySourceAboutGreetingsUpdate(GreetingUpdateType.FETCH_GREETINGS_CONTENT);
		} else {
			logger.w(String.format("Unable to get fetched freetings file bytes %s",
					greeting));
		}
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
