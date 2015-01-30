/*
 * Copyright (C) 2012 Orange Labs UK. All Rights Reserved.
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
package com.orange.labs.uk.omtp.greetings;

/**
 * Greeting types supported by the application (OMTP v1.1 compliment types)
 */
public enum GreetingType {
	/**
	 * Name of the file used to store normal greeting.
	 */
	NORMAL("normal-greeting", "normal_greeting.amr"),
	/**
	 * Name of the file used to store name greeting (voice-signature).
	 */
	VOICE_SIGNATURE("voice-signature", "voice_signature_greeting.amr"),
	/**
	 * Name of unknown greeting type received (just in case for the future).
	 * It will be used as well to store a copy of a current greeting if user
	 * changes his mind during greeting recording process.
	 */
	UNKNOWN("unknown", "unknown_backup_greeting.amr");

	private final String mType;
	private final String mFileName;

	private GreetingType(String type, String fileName) {
		mType = type;
		mFileName = fileName;
	}

	public String getTypeString() {
		return mType;
	}

	public String getFileName() {
		return mFileName;
	}

}
