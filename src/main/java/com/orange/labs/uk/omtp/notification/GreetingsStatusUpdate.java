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
