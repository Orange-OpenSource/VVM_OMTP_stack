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

import com.orange.labs.uk.omtp.greetings.GreetingType;
import com.orange.labs.uk.omtp.greetings.GreetingUpdateType;

/**
 * Notification errors related to Voicemail Greetings.
 *
 */
public class GreetingsErrorNotifications extends AbstractNotification {

	private GreetingsErrorNotifications(Bundle bundle) {
		super(SourceNotification.GREETINGS_ERROR_ACTION, bundle);
	}
	
	/**
	 * Generates a notification indicating it has not been possible to access greeting files.
	 */
	public static Builder filesAccessError() {
		return new Builder().setErrorCause(ErrorCause.GREETING_FILES_ACCESS);
	}
	
	/**
	 * Generates a notification indicating it has not been possible to upload/modify greeting files.
	 */
	public static Builder uploadError(GreetingUpdateType updateType, GreetingType greetingType) {
		return new Builder().setErrorCause(ErrorCause.GREETING_UPLOAD_ERROR)
				.setGreetingType(greetingType).setUpdateType(updateType);
	}
	
	/*
	 * Generates a notification that greetings fetching operation has failed.
	 */
	public static Builder fetchingError() {
		return new Builder().setErrorCause(ErrorCause.GREETING_FETCHING_ERROR);
	}
	
	public static class Builder {
		private Bundle mBundle = new Bundle();
		
		public Builder setErrorCause(ErrorCause cause) {
			mBundle.putSerializable(SourceNotification.ERROR_CAUSE_KEY, cause);
			return this;
		}
		
		public Builder setUpdateType(GreetingUpdateType type) {
			mBundle.putSerializable(SourceNotification.GREETING_UPDATE_TYPE_KEY, type);
			return this;
		}
		
		public Builder setGreetingType(GreetingType type) {
			mBundle.putSerializable(SourceNotification.GREETING_TYPE_KEY, type);
			return this;
		}
		
		public GreetingsErrorNotifications build() {
			return new GreetingsErrorNotifications(mBundle);
		}
	}

}
