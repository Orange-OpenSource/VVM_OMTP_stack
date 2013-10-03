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

import java.util.Set;
import java.util.concurrent.ExecutorService;

import android.content.Context;

import com.orange.labs.uk.omtp.account.OmtpAccountStoreWrapper;
import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.greetings.GreetingUpdateType;
import com.orange.labs.uk.omtp.greetings.GreetingsHelper;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.sync.VvmStoreResolver.ResolvePolicy;

/**
 * Implementation of {@link SyncResolver}.
 */
public class SyncResolverImpl implements SyncResolver {
	private static final Logger logger = Logger.getLogger(SyncResolverImpl.class);

	private final VvmStoreResolver mResolver;
	private final VvmGreetingsStoreResolver mGreetingsResolver;
	private final ResolvePolicy mResolvePolicy;
	private final VvmGreetingsStoreResolver.ResolvePolicy mGreetingsResolvePolicy;
	private final VvmStore mRemoteStore;
	private final VvmStore mLocalStore;
	private final VvmStore mMirrorStore;
	private final VvmGreetingsStore mRemoteGreetingsStore;
	private final VvmGreetingsStore mGreetingsLocalStore;
	private final ExecutorService mExecutor;
	private final GreetingsHelper mGreetingsHelper;
	private final TuiLanguageUpdater mTuiLanguageUpdater;

	public SyncResolverImpl(VvmStoreResolver resolver, ResolvePolicy resolvePolicy,
			VvmStore remoteStore, VvmStore localStore, VvmStore mirrorStore,
			ExecutorService executor, VvmGreetingsStoreResolver greetingsResolver,
			GreetingsHelper greetingsHelper, VvmGreetingsStore greetingsLocalStore,
			VvmGreetingsStore remoteGreetingsStore,
			VvmGreetingsStoreResolver.ResolvePolicy greetingsResolvePolicy,
			TuiLanguageUpdater tuiLanguageUpdater) {
		mResolver = resolver;
		mResolvePolicy = resolvePolicy;
		mRemoteStore = remoteStore;
		mRemoteGreetingsStore = remoteGreetingsStore;
		mLocalStore = localStore;
		mMirrorStore = mirrorStore;
		mGreetingsLocalStore = greetingsLocalStore;
		mExecutor = executor;
		mGreetingsResolver = greetingsResolver;
		mGreetingsHelper = greetingsHelper;
		mGreetingsResolvePolicy = greetingsResolvePolicy;
		mTuiLanguageUpdater = tuiLanguageUpdater;
	}

	@Override
	public void syncAllMessages(final Callback<Void> callback) {
		logger.d("Performing full sync.");
		mExecutor.execute(new Runnable() {

			@Override
			public void run() {
				mResolver.resolveFullSync(mLocalStore, mRemoteStore, mMirrorStore, mResolvePolicy,
						callback);
			}

		});
	}

	@Override
	public void syncLocalMessages(final Callback<Void> callback) {
		logger.d("Performing specific sync.");
		mExecutor.execute(new Runnable() {

			@Override
			public void run() {
				mResolver.resolveSpecificSync(mLocalStore, mRemoteStore, mMirrorStore,
						mResolvePolicy, callback);
			}

		});
	}

	@Override
	public void syncGreetings(final Callback<Void> callback,
			final Set<GreetingUpdateType> newGreetingToUplaod) {
		logger.d("Performing greetings sync.");
		mExecutor.execute(new Runnable() {

			@Override
			public void run() {
				mGreetingsResolver.resolveFullSync(mRemoteGreetingsStore, mGreetingsLocalStore, callback,
						newGreetingToUplaod, mGreetingsHelper, mGreetingsResolvePolicy);

			}
		});

	}

	@Override
	public void updateTuiLanguage(final Callback<Void> callback, final int newLanguage,
			final Context context, final OmtpAccountStoreWrapper accountStore) {
		logger.d("Performing TUI language update.");
		mExecutor.execute(new Runnable() {

			@Override
			public void run() {
				mTuiLanguageUpdater.updateTuiLanguage(callback, newLanguage, context, accountStore);

			}
		});

	}


}
