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

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.greetings.Greeting;
import com.orange.labs.uk.omtp.greetings.GreetingType;
import com.orange.labs.uk.omtp.greetings.GreetingUpdateType;
import com.orange.labs.uk.omtp.greetings.GreetingsHelper;
import com.orange.labs.uk.omtp.greetings.database.LocalGreetingsProvider;
import com.orange.labs.uk.omtp.voicemail.Voicemail;

public class LocalGreetingsVvmStore implements VvmGreetingsStore {

	/** Allow the execution of asynchronous operations */
	private final Executor mExecutor;
	
	/** Greetings provider for the SQLite database storing the greetings */
	private final LocalGreetingsProvider mLoclaGreetingsProvider;

	/** Greetings Helper used to manage Greetings Files */
	private final GreetingsHelper mGreetingsHelper;
	
	public LocalGreetingsVvmStore(Executor executor, LocalGreetingsProvider localGreetingsProvider,
			GreetingsHelper greetingsHelper) {
		mExecutor = executor;
		mLoclaGreetingsProvider = localGreetingsProvider;
		mGreetingsHelper = greetingsHelper;
	}

	@Override
	public void getAllGreetingsMessages(final Callback<List<Greeting>> callback) {
		mExecutor.execute(new Runnable() {
			@Override
			public void run() {
				callback.onSuccess(mLoclaGreetingsProvider.getAllGreetings());
			}
		});

	}

	@Override
	public void performActions(List<Action> actions, final Callback<Void> callback) {
		final Map<VvmStore.Operation, List<Greeting>> actionsMap = VvmStoreActions
				.buildGreetingOperationMap(actions);
		
		mExecutor.execute(new Runnable() {

			@Override
			public void run() {
				AtomicBoolean failureReported = new AtomicBoolean(false);

				for (VvmStore.Operation operation : actionsMap.keySet()) {
					List<Greeting> messages = actionsMap.get(operation);
					// No need to check if empty, no operation if not one
					// element at least.
					if (messages.size() > 1) {
						boolean success = performMultipleActions(operation, messages);
						if (!success && !failureReported.getAndSet(true)) {
							callback.onFailure(new VvmStoreException(operation, messages));
						}
					} else {
						Greeting message = messages.get(0);
						boolean success = performSingleAction(operation, message);
						if (!success && !failureReported.getAndSet(true)) {
							callback.onFailure(new VvmStoreException(operation, message));
						}
					}
				}

				// If nothing has failed, invoke callback success method.
				if (!failureReported.get()) {
					callback.onSuccess(null);
				}
			}
		});

	}
	

	/**
	 * Perform single operation on a Greeting.
	 * 
	 * @param operation
	 *            Operation to apply
	 * @param greeting
	 *            Greeting to apply on
	 * @return result boolean
	 */
	protected boolean performSingleAction(Operation operation, Greeting greeting) {
		boolean result = false;
		switch (operation) {
		case INSERT:
			result = mLoclaGreetingsProvider.updateGreeting(greeting);
			break;
		case DELETE:
			result = mLoclaGreetingsProvider.delete(greeting);
			break;
		case DELETE_GREETING_FILE:
			result = mGreetingsHelper.deleteGreetingFile(greeting.getGreetingType());
			mGreetingsHelper.notifySourceAboutGreetingsUpdate(null);
			break;
		case MARK_AS_READ:
			throw new IllegalArgumentException("Making Greeting read is not supported");
		case FETCH_VOICEMAIL_CONTENT:
		case FETCH_GREETING_CONTENT:
			throw new IllegalArgumentException("Fetching content is not supported");
		}

		return result;
	}

	/**
	 * Perform multiple actions on a Greeting.
	 * 
	 * @param operation
	 *            Operation to apply
	 * @param greetings
	 *            Greetings to apply on
	 * @return result boolean
	 */
	protected boolean performMultipleActions(Operation operation, List<Greeting> greetings) {
		boolean result = false;
		switch (operation) {
		case INSERT:
			result = mLoclaGreetingsProvider.updateGreetings(greetings);
			break;
		case DELETE:
			result = mLoclaGreetingsProvider.deleteList(greetings);
			break;
		case DELETE_GREETING_FILE:
			// delete all greeting files
			result = mGreetingsHelper.deleteAllGreetingFiles();
			mGreetingsHelper.notifySourceAboutGreetingsUpdate(null);
			break;
		case MARK_AS_READ:
			throw new IllegalArgumentException("Making Greeting read is not supported");
		case FETCH_VOICEMAIL_CONTENT:
		case FETCH_GREETING_CONTENT:
			throw new IllegalArgumentException("Fetching content is not supported");
		}

		return result;
	}

	@Override
	public void deleteAllMessages(final Callback<Void> callback) {
		mExecutor.execute(new Runnable() {
			@Override
			public void run() {
				int deletions = mLoclaGreetingsProvider.deleteAll();
				if (deletions > 0) {
					callback.onSuccess(null);
				} else {
					callback.onFailure(new VvmStoreException("Failed to delete all greetings"));
				}
			}
		});
	}

	@Override
	public void uploadGreetings(Callback<Greeting> callback, GreetingUpdateType operationType,
			GreetingType greetingType, GreetingsHelper greetingsHelper) {
		// nothing to do here for the moment
	}

	@Override
	public void getAllMessages(Callback<List<Voicemail>> callback) {
		// nothing to do here... no voicemails to return
	}

}
