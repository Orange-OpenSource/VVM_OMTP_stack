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

import com.orange.labs.uk.omtp.greetings.Greeting;
import com.orange.labs.uk.omtp.sync.VvmStore.Operation;
import com.orange.labs.uk.omtp.voicemail.Voicemail;

/**
 * Exception used when an action fails to be applied on a Store. The various constructors build
 * a cause message from actions that failed.
 */
public class VvmStoreException extends Exception {

	private static final long serialVersionUID = 9137155272604749995L;

	private final String mMessage;

	public VvmStoreException(String message) {
		mMessage = message;
	}

	public VvmStoreException(Operation operation, Voicemail message) {
		mMessage = String.format(
				"[%s Failed] Operation could not be applied on following message %s",
				operation, message);
	}
	
	public VvmStoreException(Operation operation, Greeting message) {
		mMessage = String.format(
				"[%s Failed] Operation could not be applied on following greeting message %s",
				operation, message);
	}

	public VvmStoreException(Operation operation, List<?> messages) {
		mMessage = String.format(
				"[%s Failed] Operation could not be applied on following messages  [%s]",
				operation, messages);
	}
	
	
	@Override
	public String getMessage() {
		return mMessage;
	}

	@Override
	public String toString() {
		return mMessage;
	}

}
