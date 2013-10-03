/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.orange.labs.uk.omtp.voicemail;

import javax.annotation.Nullable;

import android.content.Intent;
import android.os.Bundle;

import com.orange.labs.uk.omtp.greetings.Greeting;
import com.orange.labs.uk.omtp.sync.OmtpVvmStore;

/**
 * Stores and retrieves relevant bits of voicemails in an Intent.
 */
public class VoicemailIntentUtils {
	/** The String used when storing provider data in intents. */
	public static final String PROVIDER_DATA_KEY = VoicemailImpl.class.getName() + ".PROVIDER_DATA";
	// Private constructor, utility class.
	private VoicemailIntentUtils() {
	}

	/**
	 * Creates an {@link Intent} to fetch the content of the provided
	 * {@link Voicemail} from the remote repository.
	 * 
	 * @param message
	 *            Voicemail to fetch the content of.
	 * @param fetchType
	 *            type of the folder to be performed
	 * @return Generated intent
	 */
	public static Intent createFetchIntent(Voicemail message) {
		Intent intent = new Intent(OmtpVvmStore.FETCH_INTENT, message.getUri());
		VoicemailIntentUtils.storeIdentifierInIntent(intent, message);
		return intent;
	}
	
	/**
	 * Creates an {@link Intent} to fetch the content of the provided {@link Greeting}
	 * from remote repository.
	 * 
	 * @param message
	 * @return
	 */
	public static Intent createFetchIntent(Greeting message) {
		Intent intent = new Intent(OmtpVvmStore.FETCH_GREETING_INTENT);
		VoicemailIntentUtils.storeIdentifierInIntent(intent, message);
		return intent;
	}
	
	/**
	 * Stores the {@link Voicemail#getSourceData()} value into an intent.
	 * 
	 * @see #extractIdentifierFromIntent(Intent)
	 */
	public static void storeIdentifierInIntent(Intent intent, Voicemail message) {
		intent.putExtra(PROVIDER_DATA_KEY, message.getSourceData());
	}
	
	/**
	 * Stores the {@link Voicemail#getSourceData()} value from Greeting into an intent.
	 * 
	 * @param intent
	 * @param message
	 */
	private static void storeIdentifierInIntent(Intent intent, Greeting message) {
		intent.putExtra(PROVIDER_DATA_KEY, message.getVoicemail().getSourceData());
	}

	/**
	 * Retrieves the {@link Voicemail#getSourceData()} from an intent.
	 * <p>
	 * Returns null if the Intent contains no such identifier, or has no extras.
	 * 
	 * @see #storeIdentifierInIntent(Intent, Voicemail)
	 */
	@Nullable
	public static String extractIdentifierFromIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		return (extras == null ? null : extras.getString(PROVIDER_DATA_KEY));
	}

	/**
	 * Copies the extras stored by
	 * {@link #storeIdentifierInIntent(Intent, Voicemail)} between two intents.
	 */
	public static void copyExtrasBetween(Intent from, Intent to) {
		Bundle extras = from.getExtras();
		if (extras.containsKey(PROVIDER_DATA_KEY)) {
			to.putExtra(PROVIDER_DATA_KEY, extras.getString(PROVIDER_DATA_KEY));
		}
	}

}
