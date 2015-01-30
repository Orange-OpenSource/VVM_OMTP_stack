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

import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.greetings.Greeting;
import com.orange.labs.uk.omtp.voicemail.Voicemail;

/**
 * Abstraction for communicating with a service that stores voicemail messages.
 * <p>
 * In practice represented by a local store (backed by the content provider) or a remote store (an
 * OMTP based voicemail source, for example).
 * <p>
 * All methods that accept a {@link Callback} may be, but are not required to be, asynchronous in
 * operation. They may invoke the supplied callback before returning if the result is already known,
 * or alternatively they may perform processing in the background and invoke the callback later when
 * the result subsequently becomes known. No guarantee is made about which thread a callback will be
 * invoked on.
 * <p>
 * You must supply a valid callback to all methods that request one, you cannot pass null. If you
 * are not interested in the result of the call, you can pass in
 * {@link com.orange.labs.uk.omtp.callbacks.Callbacks#emptyCallback()}.
 */
public interface VvmStore {
	/**
	 * Fetches all the voicemails contained by this store.
	 */
	public void getAllMessages(Callback<List<Voicemail>> callback);

	/**
	 * An enumeration of the different operations to perform for a given Voicemail.
	 */
	public enum Operation {
		INSERT, DELETE, FETCH_VOICEMAIL_CONTENT, MARK_AS_READ, FETCH_GREETING_CONTENT, DELETE_GREETING_FILE
	}

	/**
	 * Represents an action that is to be performed on the VvmStore.
	 * <p>
	 * Each action has an {@link Operation} and a {@link Voicemail} to which it applies.
	 */
	public interface Action {
		/**
		 * Get the {@link Voicemail} for which the action is to be performed.
		 */
		public Voicemail getVoicemail();

		/**
		 * Get the Operation to be applied to the {@link Voicemail}.
		 */
		public Operation getOperation();
		
		/**
		 * Get the {@link Greeting} for which the action is to be performed.
		 */
		public Greeting getGreeting();
	}

	/**
	 * Perform a list of actions.
	 */
	public void performActions(List<VvmStore.Action> actions, Callback<Void> callback);

	/**
	 * Delete all messages currently stored in the store.
	 */
	public void deleteAllMessages(Callback<Void> callback);
	
}
