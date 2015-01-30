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

import com.orange.labs.uk.omtp.voicemail.Voicemail;

/**
 * Represents a single greeting stored on Imap server or in local db.
 */
public interface Greeting {

	public boolean isActive();
	
	/**
	 * Returns greeting type object.
	 * 
	 * @return Greeting type object
	 */
	public GreetingType getGreetingType();
	
	public boolean hasVoicemail();

	/**
	 * Returns object associated with this Greeting.
	 * 
	 * @return Voicemail object associated with this Greeting
	 */
	public Voicemail getVoicemail();
	
	/**
	 * Determinate if the voice attachment of a given Greeting has been downloaded from the 
	 * server. It should be false when created the object, and become true when the attachment
	 * fetch has finished.
	 * 
	 * @return true if voice attachment has been successfully downloaded, false otherwise
	 */
	public boolean isVoiceContentDownloaded();
}
