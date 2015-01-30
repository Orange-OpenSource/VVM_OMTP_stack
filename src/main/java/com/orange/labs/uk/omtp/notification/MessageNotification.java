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

/**
 * A {@link MessageNotification} can be used by the OMTP stack to indicate to the source that an
 * OMTP Sync Message has been received, but without an account configured (for that SIM, or
 * generally). The source is then free to display the information to the user.
 */
public class MessageNotification extends AbstractNotification {

	public static final String SENDER_KEY = "sender";
	
	public static final String DURATION_KEY = "duration";
	
	public static final String TIME_KEY = "time";
	
	public static final String NEW_MESSAGE_DEPOSITED = "new_message_deposited";
	
	protected MessageNotification(Bundle bundle) {
		super(MESSAGE_ACTION, bundle);
	}

	public static class Builder {
		private Bundle mBundle = new Bundle();

		public Builder setSender(String sender) {
			mBundle.putString(SENDER_KEY, sender);
			return this;
		}

		public Builder setDuration(long duration) {
			mBundle.putLong(DURATION_KEY, duration);
			return this;
		}

		public Builder setTimestamp(long time) {
			mBundle.putLong(TIME_KEY, time);
			return this;
		}

		public void justNewMessageDeposited() {
			mBundle.putBoolean(NEW_MESSAGE_DEPOSITED, true);
		}
		
		public MessageNotification build() {
			return new MessageNotification(mBundle);
		}

	}

}
