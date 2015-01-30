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

import com.orange.labs.uk.omtp.greetings.GreetingType;
import com.orange.labs.uk.omtp.greetings.GreetingUpdateType;

/**
 * Exception generated when there was a problem with a download of {@link Voicemail} or
 * {@link Greeting} messages from IMAP server.
 */
public class VvmFetchingException extends Exception {

	private static final long serialVersionUID = 5437591465994459948L;
	private final GreetingType mGreetingToActivate;
	private final GreetingUpdateType mGreetingUpdateType;

	// constructor used normally by voicemails fetch
	VvmFetchingException(String message) {
		super(message);
		mGreetingToActivate = null;
		mGreetingUpdateType = null;
	}

	// constructor used normally by Greetings fetch/upload 
	VvmFetchingException(String message, GreetingType greetingToActivate,
			GreetingUpdateType greetingUpdateType) {
		super(message);
		mGreetingToActivate = greetingToActivate;
		mGreetingUpdateType = greetingUpdateType;
	}

	public GreetingType getGreetingToActivate() {
		return mGreetingToActivate;
	}

	public GreetingUpdateType getGreetingUpdateType() {
		return mGreetingUpdateType;
	}

}
