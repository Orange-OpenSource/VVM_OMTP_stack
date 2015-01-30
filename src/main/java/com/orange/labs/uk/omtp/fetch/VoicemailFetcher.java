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
package com.orange.labs.uk.omtp.fetch;


import java.util.List;

import com.android.email.mail.MessagingException;
import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.greetings.Greeting;
import com.orange.labs.uk.omtp.greetings.GreetingType;
import com.orange.labs.uk.omtp.greetings.GreetingUpdateType;
import com.orange.labs.uk.omtp.greetings.GreetingsHelper;
import com.orange.labs.uk.omtp.voicemail.Voicemail;
import com.orange.labs.uk.omtp.voicemail.VoicemailPayload;

/**
 * Interface that defines methods to interact with a remote platform that stores voicemails. That
 * includes fetching, marking as read, and marking as deleted the remote voicemails.
 */
public interface VoicemailFetcher {

	/**
	 * Fetch all voicemails from the platform.
	 * 
	 * @param	callback	Callback used to return the fetched voicemails.
	 */
	public void fetchAllVoicemails(Callback<List<Voicemail>> callback);
	
	/**
	 * Fetch the payload (typically the audio file) of a specified voicemail.
	 * 
	 * @param providerData	Provider identifying the voicemail we want to download the voicemail of.
	 * @param callback	Callback used to return the fetched payload.
	 */
	public void fetchVoicemailPayload(String providerData, Callback<VoicemailPayload> callback);
	
	/**
	 * Mark the provided voicemails as read on the remote platform.
	 * @param callback	Callback executed once the messages have been marked as read.
	 * @param voicemails	Voicemails to mark as read on the platform.
	 */
	public void markVoicemailsAsRead(Callback<Void> callback, Voicemail... voicemails);
	
	/**
	 * Mark the provided voicemails as deleted on the remote platform.
	 * @param callback	Callback executed once the messages have been marked as deleted.
	 * @param voicemails	Voicemails to mark as deleted on the platform.
	 */
	public void markVoicemailsAsDeleted(Callback<Void> callback, Voicemail... voicemails);

	/**
	 * Synchronises greetings (uploads new greeting, fetches greetings content, fetch greeting list).
	 * @param callback
	 * @param operationType
	 * @param greetingType
	 * @param greetingsHelper instance
	 */
	public void uploadGreetings(Callback<Greeting> callback,
			GreetingUpdateType operationType, GreetingType greetingType, GreetingsHelper greetingsHelper);

	/**
	 * Fetches all greetings headers.
	 * @param callback
	 */
	public void fetchAllGreetings(Callback<List<Greeting>> callback);

	/**
	 * Fetches Greeting content.
	 * @param callback
	 * @param greetingsHelper
	 * @param message
	 * @param greetingType
	 * @throws MessagingException
	 */
	public void fetchGreetingPayload(final Callback<VoicemailPayload> callback,
			final Greeting greeting);
	
}
