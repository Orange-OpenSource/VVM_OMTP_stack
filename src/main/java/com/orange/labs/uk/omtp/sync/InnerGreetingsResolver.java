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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.greetings.Greeting;
import com.orange.labs.uk.omtp.greetings.GreetingType;
import com.orange.labs.uk.omtp.greetings.GreetingUpdateType;
import com.orange.labs.uk.omtp.greetings.GreetingsHelper;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.sync.VvmGreetingsStoreResolver.ResolvePolicy;

/**
 * Class used to resolve differences between the greetings messages stored in the application db
 * and in the IMAP server. 
 */
public final class InnerGreetingsResolver {
	
	private static final Logger logger = Logger.getLogger(InnerGreetingsResolver.class);

	private final VvmGreetingsStore mRemoteStore;
	private final VvmGreetingsStore mLocalStore;
	private final Callback<Void> mCallback;
	private final Set<GreetingUpdateType> mGreetingUpdateSet;
	private GreetingType mGreetingToActivate = GreetingType.UNKNOWN;
	private GreetingUpdateType mGreetingUpdateType = GreetingUpdateType.UNKNOWN;
	private final GreetingsHelper mGreetingsHelper;
	private final ResolvePolicy mResolvePolicy;
	/** Counter for attempts, i.e. retry executed by SerialSynchroniser in
	 * case of errors. If == 0 and there was Greeting fetch/update problem a notification 
	 * should be send to source application (it can send a pending state for retry again) */
	
	/**
	 * Checks that we never call the resolve() method more than once, as per class contract.
	 */
	private final AtomicBoolean mHasResolveBeenCalled;


	public InnerGreetingsResolver(VvmGreetingsStore remoteStore, VvmGreetingsStore localStore,
			Callback<Void> result, Set<GreetingUpdateType> greetingUpdateType,
			GreetingsHelper greetingsHelper, ResolvePolicy policy) {
		mRemoteStore = remoteStore;
		mLocalStore = localStore;
		mCallback = result;
		mResolvePolicy = policy;
		mHasResolveBeenCalled = new AtomicBoolean(false);
		
		if (greetingUpdateType == null) {
			mGreetingUpdateSet = EnumSet.of(GreetingUpdateType.UNKNOWN);
		} else {
			mGreetingUpdateSet = greetingUpdateType;
		}
		mGreetingsHelper = greetingsHelper;
	}

	/**
	 * Performs the resolve between the remote and local greetings messages
	 * 
	 * @throws IllegalStateException
	 *             if you call this method more than once.
	 */
	public void resolve() {
		if (mHasResolveBeenCalled.getAndSet(true)) {
			throw new IllegalStateException("You cannot use this class more than once.");
		}
		
		// Check what to do with the requested fetch type, download greetings|
		// Upload greetings | just get greetings list with headers?
		resolveUpdateType();
		logger.d(String.format("Performing greetings synchronisation type:%s, greeting:%s",
				mGreetingUpdateType, mGreetingToActivate));
		
		switch(mGreetingUpdateType) {
		case FETCH_GREETINGS_CONTENT:
		case UNKNOWN:
		{
			GreetingsManagementCallback<List<Greeting>> localCallback = new GreetingsManagementCallback<List<Greeting>>();
			GreetingsManagementCallback<List<Greeting>> remoteCallback = new GreetingsManagementCallback<List<Greeting>>();

			mLocalStore.getAllGreetingsMessages(localCallback);
			mRemoteStore.getAllGreetingsMessages(remoteCallback);
			
			List<Greeting> localGreetingsList = localCallback.waitForResult();
			List<Greeting> remoteGreetingsList = remoteCallback.waitForResult();

			if (remoteGreetingsList != null && localGreetingsList != null) {
				logger.d(String.format("[Remote Greetings] %s", remoteGreetingsList));
				logger.d(String.format("[Local Greetings] %s", localGreetingsList));
				// set current active greeting (used by Source application to determinate which
				// greeting is currently active)
				checkWhichRemoteGreetingIsActiveAndUpdate(remoteGreetingsList, mGreetingUpdateType);

				// resolve local and remote messages
				performResolve(localGreetingsList, remoteGreetingsList);
				// send notification to the source about a Greetings Update
				mGreetingsHelper.notifySourceAboutGreetingsUpdate(mGreetingUpdateType);
			} else {
				logger.d("There was a problem with local/remote Greetings Retrieval.");

				// call to callback onFailure() method is required in case of GreetingsManagementCallback
				// timer expiration, otherwise it may happened that the task is never removed from
				// SerialSynchronizer queue
				mCallback.onFailure(new VvmFetchingException(
						"Local/Remote Greetings retrieval error", mGreetingToActivate,
						mGreetingUpdateType));
			}
			break;
		}
		case ONLY_CHANGE_REQUIRED:
		case UPLOAD_REQUIRED:
		{
			GreetingsManagementCallback<Greeting> callback = new GreetingsManagementCallback<Greeting>();
			
			// launch upload
			mRemoteStore.uploadGreetings(callback, mGreetingUpdateType, mGreetingToActivate,
					mGreetingsHelper);
			Greeting successfulyUploadedGreeting = callback.waitForResult();
			
			// If it is an upload there should be a Greeting returned, if change only, the 
			// returned Greeting should be null
			if (successfulyUploadedGreeting != null) {
				mGreetingsHelper.setCurrentActiveGreeting(mGreetingToActivate, mGreetingUpdateType);
				
				logger.d("Successfuly uploaded/changed greeting, updating Local Greeting DB");
				List<VvmStore.Action> localActions = new ArrayList<VvmStore.Action>();
				localActions.add(VvmStoreActions.insert(successfulyUploadedGreeting));
				mLocalStore.performActions(localActions, new StoreCallback(new AtomicInteger(1),
						 new AtomicBoolean(false)));
				
			} else {
				logger.d("There was a problem with greetich change/upload, no confirmation has been received.");

				// call to callback onFailure() method is required in case of GreetingsManagementCallback
				// timer expiration, otherwise it may happened that the task is never removed from
				// SerialSynchronizer queue
				mCallback.onFailure(new VvmFetchingException("Greetings change/upload error",
						mGreetingToActivate, mGreetingUpdateType));
			}
			
			break;
		}
		default:
			break;
		}
	}

	private void performResolve(List<Greeting> localGreetingsList, List<Greeting> remoteGreetingsList) {
		List<VvmStore.Action> localActions = new ArrayList<VvmStore.Action>();
		List<VvmStore.Action> remoteActions = new ArrayList<VvmStore.Action>();
		Map<String, Greeting> remoteGreetingsMap = buildMap(remoteGreetingsList);
		// iterate through the local Greetings list
		for (Greeting localMessage : localGreetingsList) {
			if (remoteGreetingsMap.containsKey(localMessage.getVoicemail().getSourceData())) {
				Greeting remoteMessage = remoteGreetingsMap.remove(localMessage.getVoicemail()
						.getSourceData());
				mResolvePolicy.resolveBothLocalAndRemoteMessage(localMessage, remoteMessage,
						localActions, remoteActions);
			} else { 
				// delete it from the local db store
				mResolvePolicy.resolveLocalOnlyMessage(localMessage, localActions, remoteActions);
			}
		}
		
		// Because we did a remove() from remoteGreetingsMap during the loop through the list of local
		// results, we know that remoteGreetingsMap values() contains only messages that are missing
		// locally.
		for (Greeting remoteMessage : remoteGreetingsMap.values()) {
			mResolvePolicy.resolveRemoteOnlyMessage(remoteMessage, localActions, remoteActions);
		}

		logger.d("localGreetingActions: " + localActions);
		logger.d("remoteGreetingActions: " + remoteActions);

		AtomicInteger storesRemaining = new AtomicInteger(2);
		AtomicBoolean failureReported = new AtomicBoolean(false);
		
		mRemoteStore.performActions(remoteActions, new StoreCallback(storesRemaining,
				failureReported));
		mLocalStore.performActions(localActions,
				new StoreCallback(storesRemaining, failureReported));

	}

	/**
	 * Build Greetings map.
	 * 
	 * @param remoteGreetingsList
	 * @return Map of greetings Uids and Greeting objects
	 */
	private Map<String, Greeting> buildMap(List<Greeting> remoteGreetingsList) {
		Map<String, Greeting> map = new HashMap<String, Greeting>();
		for (Greeting message : remoteGreetingsList) {
			map.put(message.getVoicemail().getSourceData(), message);
		}
		return map;
	}

	/**
	 * Checks what and how should be updated. This information is received in an
	 * EnumSet from source application, greetings activity.
	 */
	private void resolveUpdateType() {
		for (GreetingUpdateType greetingUpdateItem : mGreetingUpdateSet) {
			switch (greetingUpdateItem) {
			case NORMAL:
				mGreetingToActivate = GreetingType.NORMAL;
				break;
			case VOICE_SIGNATURE:
				mGreetingToActivate = GreetingType.VOICE_SIGNATURE;
				break;
			case ONLY_CHANGE_REQUIRED:
				mGreetingUpdateType = GreetingUpdateType.ONLY_CHANGE_REQUIRED;
				break;
			case UPLOAD_REQUIRED:
				mGreetingUpdateType = GreetingUpdateType.UPLOAD_REQUIRED;
				break;
			case FETCH_GREETINGS_CONTENT:
				mGreetingUpdateType = GreetingUpdateType.FETCH_GREETINGS_CONTENT;
				break;
			case UNKNOWN:
				logger.d("Greeting update UNKNOWN, it will be interpreted as greetings fetch");
			default:
				mGreetingUpdateType = GreetingUpdateType.UNKNOWN;
				mGreetingToActivate = GreetingType.UNKNOWN;
				break;
			}
		}
	}


	/**
	 * Updates active greeting type information that could be received by the Source application.
	 * @param remoteGreetings received greetings list
	 */
	private void checkWhichRemoteGreetingIsActiveAndUpdate(List<Greeting> remoteGreetings, 
			GreetingUpdateType updateType) {
		if (remoteGreetings != null) {
			for(Greeting greeting : remoteGreetings) {
				if (greeting.isActive()){
					mGreetingsHelper.setCurrentActiveGreeting(greeting.getGreetingType(), updateType);
				}
			}
		}
	}

	/**
	 * Helper class used as a callback that also allows a thread to wait for the result.
	 */
	private class GreetingsManagementCallback<T> implements Callback<T> {
		public static final long TIME_TO_WAIT_FOR_RESULT_MS = 40000;
		private final CountDownLatch mIsComplete = new CountDownLatch(1);
		private volatile T mResult;

		@Override
		public void onSuccess(T result) {
			logger.d("GreetingsManagementCallback onSuccess() called");
			mResult = result;
			mIsComplete.countDown();
		}

		@Override
		public void onFailure(Exception error) {
			logger.w("GreetingsManagementCallback onFailure() called");
			mCallback.onFailure(error);
		}
		
		/**
		 * Waits for the asynchronous result of the callback to complete.
		 * <p>
		 * Returns the greetings structure that we retrieved. Returns null if the thread was
		 * interrupted, if there was an exception of any sort fetching the data from the server, or
		 * if the timeout expired (i.e. the fetch took too long).
		 */
		@Nullable
		private T waitForResult() {
			try {
				mIsComplete.await(TIME_TO_WAIT_FOR_RESULT_MS, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// Restore interrupt status and fall through.
				Thread.currentThread().interrupt();
			}
			return mResult;
		}
	}
	
	private class StoreCallback implements Callback<Void> {

		private final AtomicInteger mStoresRemaining;
		private final AtomicBoolean mFailureReported;

		public StoreCallback(AtomicInteger storesRemaining, AtomicBoolean failureReported) {
			mStoresRemaining = storesRemaining;
			mFailureReported = failureReported;
		}

		@Override
		public void onSuccess(Void result) {
			// A list of actions succeeded.
			// If this is the last list of actions to complete, call the
			// original callback.
			logger.d("[Store Greeting Callback] Store succesfully executed actions.");
			if (mStoresRemaining.decrementAndGet() == 0) {
				logger.d("[Store Callback] Done with actions, invoking callback");
				mCallback.onSuccess(null);
			}
		}

		@Override
		public void onFailure(Exception error) {
			// A list of actions has failed.
			// Fail the original callback, unless we've already failed once.
			if (!mFailureReported.getAndSet(true)) {
				logger.w("[Store Greeting Callback] Store failed to execute actions, invoking callback");
				mCallback.onFailure(error);
			}
		}
	}
	
}
