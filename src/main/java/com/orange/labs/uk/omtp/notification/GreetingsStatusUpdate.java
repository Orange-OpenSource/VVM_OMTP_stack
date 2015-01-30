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

import android.os.Bundle;

import com.orange.labs.uk.omtp.greetings.GreetingUpdateType;
import com.orange.labs.uk.omtp.source.SourceInterface;

/**
 * Notification used to communicate with OMTP source application responsible for
 * presenting the currently selected greeting option.
 * Upon reception of this notification a given activity should ask the stack about the 
 * currently selected greeting through {@link SourceInterface#getCurrentActiveGreeting()} 
 * and update the View accordingly. 
 */
public class GreetingsStatusUpdate extends AbstractNotification {

	private GreetingsStatusUpdate(Bundle bundle) {
		super(SourceNotification.GREETINGS_STATUS_UPDATE_ACTION, bundle);
	}
	
	/**
	 * Generates a notification indicating that new greetings status (selected message) has been
	 * fetched from IMAP server.
	 */
	public static Builder reportNewStatus(GreetingUpdateType type) {
		return new Builder().setGreetingStatusUpdate(type);
	}
	
	public static class Builder {
		private Bundle mBundle = new Bundle();
		
		public Builder setGreetingStatusUpdate(GreetingUpdateType type) {
			mBundle.putSerializable(SourceNotification.GREETING_UPDATE_TYPE_KEY, type);
			return this;
		}
		
		public GreetingsStatusUpdate build() {
			return new GreetingsStatusUpdate(mBundle);
		}
	}

}
