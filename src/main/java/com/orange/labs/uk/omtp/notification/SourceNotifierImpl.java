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
package com.orange.labs.uk.omtp.notification;

import android.content.Context;
import android.content.Intent;

import com.orange.labs.uk.omtp.logging.Logger;

/**
 *	Implementation of {@link SourceNotifier} that uses Android intents to broadcast status and
 *  error update to the system and to voicemail sources. 
 */
public class SourceNotifierImpl implements SourceNotifier {

	private static Logger logger = Logger.getLogger(SourceNotifierImpl.class);
	
	private Context mContext;

	public SourceNotifierImpl(Context context) {
		mContext = context;
	}

	/**
	 * Broadcasts an {@link Intent} containing the notification and its parameters (stored in a
	 * bundle).
	 */
	@Override
	public void sendNotification(SourceNotification notification) {
		logger.d(String.format("Broadcasting notification: %s", notification));
		
		Intent intent = new Intent(notification.getAction());
		intent.putExtras(notification.getBundle());
		mContext.sendOrderedBroadcast(intent, null); // no permission required
	}
	
}
