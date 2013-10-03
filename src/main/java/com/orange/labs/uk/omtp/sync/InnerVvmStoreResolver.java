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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.sync.VvmStoreResolver.ResolvePolicy;
import com.orange.labs.uk.omtp.voicemail.Voicemail;

/**
 * Helper class for use by the VvmStoreResolverImpl.
 * <p>
 * One-shot class, constructed by the VvmStoreResolverImpl in response to the full resolve method,
 * and then thrown away afterwards. You should not use this class directly, and you should not
 * attempt to re-use an instance of this class. The VvmStoreResolverImpl class will construct an
 * instance of this class, call the {@link #resolve()} method, and then drop the instance.
 * <p>
 * This class needs to be thread-safe, because the callbacks involved are complex and may happen on
 * arbitrary threads.
 * <p>
 * This class is in one of the following conceptual 'states':
 * <ul>
 * <li>Constructed, but not yet started.</li>
 * <li>Started, waiting for at least one message store to return its results.</li>
 * <li>Resolving the results of both fetches into actions to be performed on both stores.</li>
 * <li>Waiting for the list of actions to be completed on at least one store.</li>
 * <li>Done.</li>
 * </ul>
 * <p>
 * The original callback passed in with this class is guaranteed to be invoked at most once. Success
 * will only be called after both fetches complete, the sync resolves, and the new actions are
 * successfully completed. In any other situation, onFailure will be called as soon as the failure
 * is detected.
 */
@ThreadSafe
/* package */final class InnerVvmStoreResolver {
	private static final Logger logger = Logger.getLogger(InnerVvmStoreResolver.class);

	/** The callback to be invoked when the resolve is complete. */
	private final Callback<Void> mCallback;
	/** The local store holding voicemails, expected to be the content provider. */
	private final VvmStore mLocalStore;
	/** The remote store holding voicemails. */
	private final VvmStore mRemoteStore;
	/** The local mirror holding voicemails. */
	private final VvmStore mMirrorStore;
	/** Policy used to resolve conflicts and detect changes */
	private final ResolvePolicy mResolvePolicy;

	/**
	 * Checks that we never call the resolve() method more than once, as per class contract.
	 */
	private final AtomicBoolean mHasResolveBeenCalled;

	public InnerVvmStoreResolver(VvmStore localStore, VvmStore remoteStore, VvmStore mirrorStore,
			ResolvePolicy resolvePolicy, Callback<Void> callback) {
		mCallback = callback;
		mLocalStore = localStore;
		mRemoteStore = remoteStore;
		mMirrorStore = mirrorStore;
		mResolvePolicy = resolvePolicy;

		mHasResolveBeenCalled = new AtomicBoolean(false);
	}

	/**
	 * Performs the resolve between the remote and local repositories provided through the
	 * constructor. using synchronous callbacks. To resolve the differences between the two
	 * repositories, the {@link ResolvePolicy} that has been provided to the constructor will be
	 * used.
	 * 
	 * <p>
	 * See the class documentation for a fuller description of how this method will behave.
	 * 
	 * @throws IllegalStateException
	 *             if you call this method more than once.
	 */
	public void resolve() {
		if (mHasResolveBeenCalled.getAndSet(true)) {
			throw new IllegalStateException("You cannot use this class more than once.");
		}

		FetchMessagesCallback localCallback = new FetchMessagesCallback();
		FetchMessagesCallback remoteCallback = new FetchMessagesCallback();

		mRemoteStore.getAllMessages(remoteCallback);
		mLocalStore.getAllMessages(localCallback);

		List<Voicemail> localVoicemails = localCallback.waitForResult();
		List<Voicemail> remoteVoicemails = remoteCallback.waitForResult();

		// In case of error, the list is null
		if (localVoicemails != null && remoteVoicemails != null) {
			logger.d(String.format("[Local voicemails] %s", localVoicemails));
			logger.d(String.format("[Remote Voicemails] %s", remoteVoicemails));
			performResolve(localVoicemails, remoteVoicemails);
		} else {
			mCallback.onFailure(new VvmFetchingException("Local/remote Voicemail fetch failure"));
			logger.w("It has not been possible to fetch local and remote Voicemails");
		}
	}

	/**
	 * Performs the resolve between the local store and mirror store supplied through the
	 * constructor. Reflect these changes on the remote store if needed.
	 * <p>
	 * See the class documentation for a fuller description of how this method will behave.
	 * 
	 * 
	 * @throws IllegalStateException
	 *             if you call this method more than once.
	 */
	public void resolveLocalChanges() {
		if (mHasResolveBeenCalled.getAndSet(true)) {
			throw new IllegalStateException("You cannot use this class more than once.");
		}

		FetchMessagesCallback localCallback = new FetchMessagesCallback();
		FetchMessagesCallback mirrorCallback = new FetchMessagesCallback();

		mLocalStore.getAllMessages(localCallback);
		mMirrorStore.getAllMessages(mirrorCallback);

		List<Voicemail> localVoicemails = localCallback.waitForResult();
		List<Voicemail> mirrorVoicemails = mirrorCallback.waitForResult();

		logger.d(String.format("[Local Voicemails] %s", localVoicemails.toString()));
		logger.d(String.format("[Mirror Voicemails] %s", mirrorVoicemails.toString()));

		if (localVoicemails != null && mirrorVoicemails != null) {
			performLocalResolve(localVoicemails, mirrorVoicemails);
		} else {
			logger.w("It has not been possible to fetch local and remote Voicemails");
		}
	}

	/**
	 * Resolve the voicemails present in the local store (localResults) and mirror store
	 * (mirrorResults). It resolves both lists into two types of operations:
	 * 
	 * <p>
	 * The message only exists only in the mirror. The message has been deleted from the local
	 * store.
	 * <p>
	 * The message exists in the local and mirror stores. The read status of the message can have
	 * changed.
	 * 
	 * A list of actions for the {@link OmtpVvmStore} is created from these operations and executed.
	 * 
	 * @param localResults
	 *            {@link List} of {@link Voicemail} present in the {@link LocalVvmStore}.
	 * @param mirrorResults
	 *            {@link List} of {@link Voicemail} present in the {@link MirrorVvmStore}.
	 */
	private void performLocalResolve(List<Voicemail> localResults, List<Voicemail> mirrorResults) {
		List<VvmStore.Action> remoteActions = new ArrayList<VvmStore.Action>();

		Map<String, Voicemail> mirrorMap = buildMap(mirrorResults);
		for (Voicemail localMessage : localResults) {
			if (mirrorMap.containsKey(localMessage.getSourceData())) {
				Voicemail mirrorMessage = mirrorMap.remove(localMessage.getSourceData());
				mResolvePolicy.resolveBothLocalAndMirrorMessage(localMessage, mirrorMessage,
						remoteActions);
			}
		}

		// If still messages in mirror, means they have been deleted, apply
		// on server.
		for (Voicemail mirrorMessage : mirrorMap.values()) {
			mResolvePolicy.resolveMirrorOnlyMessage(mirrorMessage, remoteActions);
		}

		logger.d(String.format("[Remote Actions] %s", remoteActions));
		StoreCallback callback = new StoreCallback(new AtomicInteger(1), new AtomicBoolean(false));
		mRemoteStore.performActions(remoteActions, callback);
	}

	/**
	 * Resolve the voicemails present in the local store (localResults) and remote repository
	 * (remoteResults). It resolves both lists into three types of operations:
	 * 
	 * <p>
	 * A message exists only locally.
	 * <p>
	 * A message exists only remotely.
	 * <p>
	 * A message exists locally and remotely.
	 * 
	 * In each case, it calls the appropriate method from the {@link ResolvePolicy} instance
	 * provided through the constructor.
	 * 
	 * @param localResults
	 *            {@link List} of {@link Voicemail} present in the {@link LocalVvmStore}.
	 * @param remoteResults
	 *            {@link List} of {@link Voicemail} present in the {@link OmtpVvmStore}.
	 */
	private void performResolve(List<Voicemail> localResults, List<Voicemail> remoteResults) {
		List<VvmStore.Action> localActions = new ArrayList<VvmStore.Action>();
		List<VvmStore.Action> remoteActions = new ArrayList<VvmStore.Action>();
		Map<String, Voicemail> remoteMap = buildMap(remoteResults);
		for (Voicemail localMessage : localResults) {
			if (remoteMap.containsKey(localMessage.getSourceData())) {
				Voicemail remoteMessage = remoteMap.remove(localMessage.getSourceData());
				mResolvePolicy.resolveBothLocalAndRemoteMessage(localMessage, remoteMessage,
						localActions, remoteActions);
			} else {
				mResolvePolicy.resolveLocalOnlyMessage(localMessage, localActions, remoteActions);
			}
		}
		// Because we did a remove() from remoteMap during the loop through the list of local
		// results, we know that remoteMap's values() contains only messages that are missing
		// locally.
		for (Voicemail remoteMessage : remoteMap.values()) {
			mResolvePolicy.resolveRemoteOnlyMessage(remoteMessage, localActions, remoteActions);
		}

		logger.d("localActions: " + localActions);
		logger.d("remoteActions: " + remoteActions);

		AtomicInteger storesRemaining = new AtomicInteger(2);
		AtomicBoolean failureReported = new AtomicBoolean(false);

		mRemoteStore.performActions(remoteActions, new StoreCallback(storesRemaining,
				failureReported));
		mLocalStore.performActions(localActions,
				new StoreCallback(storesRemaining, failureReported));
	}

	/**
	 * Callback provided to {@link VvmStore} when executing actions.
	 */
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
			logger.d("[Store Callback] Store succesfully executed actions.");
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
				logger.w("[Store Callback] Store failed to execute actions, invoking callback");
				mCallback.onFailure(error);
			}
		}
	}

	/**
	 * Synchronous callbacks used to fetch {@link Voicemail} from {@link VvmStore} before resolving.
	 */
	private class FetchMessagesCallback implements Callback<List<Voicemail>> {
		private final CountDownLatch mIsComplete = new CountDownLatch(1);
		private volatile List<Voicemail> mResult;

		@Override
		public void onFailure(Exception error) {
			mCallback.onFailure(error);
			mIsComplete.countDown();
		}

		@Override
		public void onSuccess(List<Voicemail> result) {
			mResult = result;
			mIsComplete.countDown();
		}

		@Nullable
		private List<Voicemail> waitForResult() {
			try {
				mIsComplete.await();
			} catch (InterruptedException e) {
				// Restore interrupt status and fall through.
				Thread.currentThread().interrupt();
			}
			return mResult;
		}
	}

	/**
	 * Builds a map from provider data to message for the given collection of voicemails.
	 */
	private static Map<String, Voicemail> buildMap(Collection<Voicemail> messages) {
		Map<String, Voicemail> map = new HashMap<String, Voicemail>();
		for (Voicemail message : messages) {
			map.put(message.getSourceData(), message);
		}
		return map;
	}

}
