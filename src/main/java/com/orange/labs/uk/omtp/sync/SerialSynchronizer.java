/*
 * Copyright (C) 2012 Orange Labs UK. All Rights Reserved.
 * Copyright (C) 2011 The Android Open Source Project
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

import java.util.EnumSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;

import com.android.email.mail.AuthenticationFailedException;
import com.orange.labs.uk.omtp.account.OmtpAccountStoreWrapper;
import com.orange.labs.uk.omtp.config.StackStaticConfiguration;
import com.orange.labs.uk.omtp.dependency.StackDependencyResolverImpl;
import com.orange.labs.uk.omtp.greetings.GreetingUpdateType;
import com.orange.labs.uk.omtp.imap.SynchronizationCallback;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.notification.ChangeTuiLanguageNotification;
import com.orange.labs.uk.omtp.notification.SourceNotifier;

/**
 * This class behaves as a queue for synchronizations. This class exists because of Android often
 * sending two events when a change is made. Doing synchronizations avoid doing twice the same
 * operations.
 */
public class SerialSynchronizer {

	private static Logger logger = Logger.getLogger(SerialSynchronizer.class);

	/** Useful to notify the source */
	private final Context mContext;
	/** Used to notify the sources */
	private final SourceNotifier mNotifier;
	/** Counter for attempts, retry while not 0 */
	private final AtomicInteger mAttempts = new AtomicInteger();
	/** Queue that contains synchronisation to execute serially */
	private final Queue<SyncFlag> mSyncQueue = new LinkedBlockingQueue<SyncFlag>();

	private volatile SyncFlag mActiveFlag;
	private volatile Set<GreetingUpdateType> mGreetingUpdateSet;
	/** Used in SynchronizationCallback to update the local account */
	private final OmtpAccountStoreWrapper mAccountStore;

	private volatile int mNewLanguage;

	public SerialSynchronizer(Context context, SourceNotifier notifier,
			OmtpAccountStoreWrapper accountStore) {
		mContext = context;
		mNotifier = notifier;
		mAccountStore = accountStore;
	}

	public synchronized void execute(SyncFlag flag) {

		mSyncQueue.offer(flag);
		logger.d(String.format("New voicemail synchronisation in queue, total: %d", mSyncQueue.size()));

		if (mActiveFlag == null) {
			scheduleNextSynchronisation();
		}
	}

	public synchronized void executeGreeting(Set<GreetingUpdateType> greetingUpdateTypeSet) {
		
		if (greetingUpdateTypeSet == null) {
			logger.d("Greetings synchronisation called with null type.");
			mGreetingUpdateSet = EnumSet.of(GreetingUpdateType.UNKNOWN);
		} else {
			mGreetingUpdateSet = greetingUpdateTypeSet;
		}

		mSyncQueue.offer(SyncFlag.GREETINGS_SYNCHRONIZATION);
		logger.d(String.format("New greeting synchronisation in queue, total: %d", mSyncQueue.size()));

		if (mActiveFlag == null) {
			scheduleNextSynchronisation();
		}
	}
	
	public synchronized void executeTuiChange(SyncFlag flag, int languageId) {
		
		mNewLanguage = languageId;
		
		mSyncQueue.offer(flag);
		logger.d(String.format("New TUI language change in queue, total: %d", mSyncQueue.size()));

		if (mActiveFlag == null) {
			scheduleNextSynchronisation();
		}
	}
	
	private synchronized void scheduleNextSynchronisation() {
		if ((mActiveFlag = mSyncQueue.poll()) != null) {
			logger.d(String.format("About to schedule synchronisation, remaining: %d",
					mSyncQueue.size()));

			// Reset attempt number and execute the synchronisation.
			mAttempts.set(StackStaticConfiguration.MAX_IMAP_ATTEMPTS);
			executeSynchronization();
		}
	}

	/**
	 * Execute a synchronization using both the active {@link SyncFlag} and number of left attempts
	 * by creating a new {@link SyncResolver}.
	 */
	private synchronized void executeSynchronization() {
		SyncResolver resolver = StackDependencyResolverImpl.getInstance().createSyncResolver();
		
		if (mActiveFlag == null) {
			logger.d("Nothing to synchronise...");
			return;
		}
		
		switch (mActiveFlag) {
		case FULL_SYNCHRONIZATION:
			logger.d("Executing full synchronization...");
			resolver.syncAllMessages(new SynchronisationCallback(mContext, mNotifier,
					mAccountStore, mAttempts));
			break;
		case LOCAL_SYNCHRONIZATION:
			logger.d("Executing local changes based synchronization...");
			resolver.syncLocalMessages(new SynchronisationCallback(mContext, mNotifier,
					mAccountStore, mAttempts));
			break;
		case GREETINGS_SYNCHRONIZATION:
			logger.d(String.format("Executing greetings synchronization... attempts left :%s",
					mAttempts));
			resolver.syncGreetings(new SynchronisationCallback(mContext, mNotifier, mAccountStore,
					mAttempts), mGreetingUpdateSet);
			break;
		case TUI_LANGUAGE_CHANGE:
			logger.d("Executing TUI language change action...");
			resolver.updateTuiLanguage(new TuiSynchronisationCallback(mContext, mNotifier, mAccountStore,
					mAttempts), mNewLanguage, mContext, mAccountStore);
		default:
			break;
		
		}
	}

	/**
	 * In case of error, this method should be called to drop the current synchronization. The other
	 * synchronizations in the queue are kept, but not executed.
	 */
	public synchronized void dropCurrentSynchronization() {
		logger.d("Dropping current synch.");
		mActiveFlag = null;
	}

	public enum SyncFlag {
		/**
		 * Flag used to execute a full synchronization: download remote message headers, compare to
		 * local stores, update local and remote store with changes.
		 */
		FULL_SYNCHRONIZATION,
		/**
		 * Flag used to execute a local synchronization: compare local/mirror stores, obtain list of
		 * changes, reflect these changes on remote store.
		 */
		LOCAL_SYNCHRONIZATION,
		/**
		 * Flag used to trigger greetings synchronization
		 */
		GREETINGS_SYNCHRONIZATION,
		/**
		 * Flag used  to trigger TUI language change
		 */
		TUI_LANGUAGE_CHANGE,
	}

	/**
	 * Simple callback that schedule the next synchronization when the current one is done.
	 */
	private class SynchronisationCallback extends SynchronizationCallback<Void> {

		public SynchronisationCallback(Context context, SourceNotifier notifier,
				OmtpAccountStoreWrapper accountStore, AtomicInteger attempts) {
			super(context, notifier, accountStore, attempts);
		}

		/**
		 * In case of success, we schedule the next synchronization in queue.
		 */
		@Override
		public void onSuccess(Void result) {
			logger.d(String.format("[Synchronization Success] In Queue %d", mSyncQueue.size()));
			scheduleNextSynchronisation();
		}

		/**
		 * If a synchronization fails, we retry up to
		 * {@link StackStaticConfiguration#MAX_IMAP_ATTEMPTS} times except if there was an
		 * {@link AuthenticationFailedException}.
		 */
		@Override
		public void onFailure(Exception error) {
			if (shouldRetry(error)) {
				logger.d(String.format("[Synchronization Failed] Exception:%s, Retrying...",
						error.getClass()));
				executeSynchronization();
			} else {
				super.onFailure(error);

				// Retry synchronisation only if not authentication error.
				if (authenticationError(error)) {
					dropCurrentSynchronization();
				} else if (vvmFetchingException(error)) {
					logger.d("VvmFetchingException has been receid by SerialSynchronizer");
					shouldNotifyAboutGreetingsUploadFailure((VvmFetchingException)error);
				} else {
					scheduleNextSynchronisation();
				}
			}
		}
	}
	
	/**
	 * Special Synchronisation callback for TUI language selection.
	 */
	private class TuiSynchronisationCallback extends SynchronisationCallback {
		
		public TuiSynchronisationCallback(Context context, SourceNotifier notifier,
				OmtpAccountStoreWrapper accountStore, AtomicInteger attempts) {
			super(context, notifier, accountStore, attempts);
		}
		
		@Override
		public void onFailure(Exception error) {
			super.onFailure(error);
			if (mAttemptsLeft.get() == 0) {
				// send error notification to source application
				mNotifier.sendNotification(ChangeTuiLanguageNotification.reportUpdateError());

			}
		}
		
	}

	
	
}
