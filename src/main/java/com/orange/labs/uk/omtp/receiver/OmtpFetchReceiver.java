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
package com.orange.labs.uk.omtp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.service.fetch.OmtpFetchService;
import com.orange.labs.uk.omtp.sync.OmtpVvmStore;
import com.orange.labs.uk.omtp.voicemail.VoicemailIntentUtils;

/**
 * Receives and handles broadcasts sent to indicate that we should download
 * voicemails/greetings or upload greetings.
 * <p>
 * BroadcastReceivers can't do any long running work in their
 * {@link #onReceive(Context, Intent)} methods, so we just send an Intent on to
 * the
 * {@link com.google.android.voicemail.example.service.fetch.OmtpFetchService}
 * which will take care of downloading for us.
 * <p>
 * This class is thread-confined, it will only be called from the application
 * main thread.
 */
public class OmtpFetchReceiver extends BroadcastReceiver {
	private static final Logger logger = Logger.getLogger(OmtpFetchReceiver.class);

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null) {
			logger.d("Received intent: " + intent);
			logger.d("Extras: " + intent.getExtras());
			String action = intent.getAction();
			Uri data = intent.getData();
			// outgoing intent
			Intent outgoing = null;

			if (OmtpVvmStore.FETCH_INTENT.equals(action)
					|| OmtpVvmStore.ANDROID_FETCH_INTENT.equals(action)) {

				if (action.equals(OmtpVvmStore.ANDROID_FETCH_INTENT)
						&& !data.getQueryParameter("source_package").equals(
								context.getPackageName())) {
					return;
				}

				outgoing = new Intent(intent.getAction(), data, context,
						OmtpFetchService.class);

				if (action.equals(OmtpVvmStore.FETCH_INTENT)) {
					// Internal intent, contains the remote ID.
					VoicemailIntentUtils.copyExtrasBetween(intent, outgoing);
				}

			} else if (OmtpVvmStore.FETCH_GREETING_INTENT.equals(action)) {
				outgoing = new Intent(intent.getAction(), data, context,
						OmtpFetchService.class);
				// Internal intent, contains the remote ID.
				VoicemailIntentUtils.copyExtrasBetween(intent, outgoing);
			}
			
			if (outgoing != null) {
				context.startService(outgoing);
			}
		}
	}
}
