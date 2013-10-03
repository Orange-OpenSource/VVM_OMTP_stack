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
package com.orange.labs.uk.omtp.service.fetch;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;

import android.app.IntentService;
import android.content.Intent;

import com.orange.labs.uk.omtp.dependency.StackDependencyResolver;
import com.orange.labs.uk.omtp.dependency.StackDependencyResolverImpl;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.sync.OmtpVvmStore;

/**
 * Service to download the OMTP voicemail bodies.
 * <p>
 * This service will be triggered by a call to {@link #startService(Intent)} which will have been
 * made by the BroadcastReceiver responsible for handling the fetch intents.
 * <p>
 * This class contains no logic, but just delegates to the {@link OmtpFetchController}.
 * <p>
 * This class extends the {@link IntentService}, which makes the following guaranatees:
 * <ul>
 * <li>requests are handled on a single worker thread</li>
 * <li>only one request will be processed at a time</li>
 * </ul>
 * <p>
 * Unfortunately the {@link IntentService} does <b>not</b> guarantee that this will be the same
 * thread each time, and indeed if one intent is completed before another is sent, then the service
 * will be stopped and restarted, and a different thread will be used. That it guarantees sequential
 * execution of the {@link #onHandleIntent(Intent)} method is great, but that it doesn't guarantee
 * memory visibility between two different calls to {@link #onHandleIntent(Intent)} is annoying. In
 * practice I can see from the implementation that a MessageQueue is used, which contains enough
 * synchronization to guarantee visibility also, but it seems brave to rely on this.
 * <p>
 * I therefore make {@link #onHandleIntent(Intent)} synchronized, which will serve to guarantee
 * memory visibility between successive calls, and coincidentally provides thread safety.
 */
@ThreadSafe
public class OmtpFetchService extends IntentService {
	private static final Logger logger = Logger.getLogger(OmtpFetchService.class);
	private static final String WORKER_THREAD_NAME = "OmtpFetchServiceWorkerThread";
	private static final AtomicInteger THREAD_NUMBER = new AtomicInteger(0);

	private OmtpFetchController mOmtpFetchController;
	private GreetingsFetchController mGreetingsFetchController;

	public OmtpFetchService() {
		super(WORKER_THREAD_NAME + "_" + THREAD_NUMBER.incrementAndGet());
	}

	@Override
	protected synchronized void onHandleIntent(Intent intent) {
		if (intent != null) {
			String action = intent.getAction();
			if (action != null) {
				if (action.equals(OmtpVvmStore.FETCH_GREETING_INTENT)) {
					getGreetingsFetchController().onHandleFetchIntent(intent);
				} else { // standard case
					getOmtpFetchController().onHandleFetchIntent(intent);
				}
			} else {
				logger.w("Received an Intent to fetch a message, but the action is null...");
			}
		}
	}

	/** Lazily initializes the fetch controller. */
	private OmtpFetchController getOmtpFetchController() {
		if (mOmtpFetchController == null) {
			StackDependencyResolver resolver = StackDependencyResolverImpl.getInstance();
			mOmtpFetchController = resolver.createFetchController();
		}
		return mOmtpFetchController;
	}
	
	/** Lazily initializes the greeting fetch controller. */
	private GreetingsFetchController getGreetingsFetchController() {
		if (mGreetingsFetchController == null) {
			StackDependencyResolver resolver = StackDependencyResolverImpl.getInstance();
			mGreetingsFetchController = resolver.createGreetingsFetchController();
		}
		return mGreetingsFetchController;
	}
}
