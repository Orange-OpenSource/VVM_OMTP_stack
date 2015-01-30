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
 * Interface defining a notification that can be broadcasted to voicemail
 * sources that use the stack to provide feedback about the current status
 * (including errors) of the stack.
 */
public interface SourceNotification {

	public static final String PROVIDER_ACTION = "com.orange.labs.uk.omtp.notification.PROVIDER";

	public static final String STATUS_ACTION = "com.orange.labs.uk.omtp.notification.STATUS";

	public static final String QUOTA_ACTION = "com.orange.labs.uk.omtp.notification.QUOTA";

	public static final String MESSAGE_ACTION = "com.orange.labs.uk.omtp.notification.MESSAGE";

	public static final String DATA_CHANNEL_ACTION = "com.orange.labs.uk.omtp.notification.DATA_CHANNEL";

	public static final String NOTIF_CHANNEL_ACTION = "com.orange.labs.uk.omtp.notification.NOTIF_CHANNEL";

	public static final String GREETINGS_ERROR_ACTION = "com.orange.labs.uk.omtp.notification.GREETINGS_ERROR";
	
	public static final String GREETINGS_STATUS_UPDATE_ACTION = "com.orange.labs.uk.omtp.notification.GREETINGS_STATUS_UPDATE";
	
	public static final String CHANGE_TUI_LANGUAGE_ACTION = "com.orange.labs.uk.omtp.notification.CHANGE_TUI_LANGUAGE_FAILURE";
	
	public static final String XCLOSE_NUT_ACTION = "com.orange.labs.uk.omtp.notification.XCLOSE_NUT_STATUS_UPDATE";

	public static final String CONNECTIVITY_STATUS_KEY = "connectivity_status";
	
	/** Used to notify about the success of XCLOSE_NUT operation */
	public static final String XCLOSE_NUT_STATUS_KEY = "xclose_nut_status_key";
	
	/** Used to give the cause of a failure */
	public static final String ERROR_CAUSE_KEY = "error_cause";
	
	/**
	 * Used to give the reason of an authentication failure. Should only be
	 * provided in case of authentication error.
	 */
	public static final String AUTH_ERROR_KEY = "authentication_error";
	
	/**
	 * Used to provide information of which type of greeting has been updated.
	 */
	public static final String GREETING_UPDATE_TYPE_KEY = "greeting_update_type";
	
	/**
	 * Type of a greeting that is updated: NORMAL or Voice-Signature.
	 */
	public static final String GREETING_TYPE_KEY = "greeting_type";

	/**
	 * Enumeration containing general error codes.
	 */
	public enum Error {
		UNKNOWN_PROVIDER("unknown_provider_error");

		private final String mKey;

		private Error(String key) {
			mKey = key;
		}

		public String getCode() {
			return mKey;
		}

	}

	/**
	 * Possible connectivity values.
	 */
	public enum ConnectivityStatus {
		CONNECTIVITY_OK, CONNECTIVITY_KO, MESSAGE_WAITING
	}

	/**
	 * List connectivity error causes.
	 */
	public enum ErrorCause {
		TIMEOUT, AUTHENTICATION, AIRPLANE, SIM_ABSENT, SIM_NOT_READY, WIFI_ENABLED, ROAMING, UNKNOWN, 
		GREETING_FILES_ACCESS, GREETING_UPLOAD_ERROR, GREETING_FETCHING_ERROR
	}

	/**
	 * List of authentication error causes. 
	 */
	public enum AuthenticationError {
		UNKNOWN_USER, BLOCKED_USER, INVALID_PASSWORD, NOT_ACTIVATED, NOT_PROVISIONED, NOT_INITIALIZED, UNKNOWN_CLIENT, UNKNOWN
	}

	/**
	 * Returns the action of the Notification.
	 */
	public String getAction();

	/**
	 * Returns the bundle with the parameters associated to the action.
	 */
	public Bundle getBundle();

}
