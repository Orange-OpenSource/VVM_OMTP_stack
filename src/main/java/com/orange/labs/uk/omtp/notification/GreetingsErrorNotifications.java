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
