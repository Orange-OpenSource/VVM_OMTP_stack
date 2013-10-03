package com.orange.labs.uk.omtp.notification;

import android.os.Bundle;

public class XcloseNutNotification extends AbstractNotification {

	protected XcloseNutNotification(Bundle bundle) {
		super(SourceNotification.XCLOSE_NUT_ACTION, bundle);
	}
	
	/**
	 * Generates a notification indicating that the XCLOSE_NUT IMAP request has been 
	 * successfully completed.
	 */
	public static XcloseNutNotification xCloseNutSucces() {
		return new Builder().setXcloseNutStatus(true).build();
	}
	
	/**
	 * Generates a notification indicating that the XCLOSE_NUT IMAP request has failed.
	 */
	public static XcloseNutNotification xCloseNutFailure() {
		return new Builder().setXcloseNutStatus(false).build();
	}
	
	public static class Builder {

		private Bundle mBundle = new Bundle();
		
		public Builder setXcloseNutStatus(boolean status) {
			mBundle.putBoolean(SourceNotification.XCLOSE_NUT_STATUS_KEY, status);
			return this;
		}

		public XcloseNutNotification build() {
			return new XcloseNutNotification(mBundle);
		}
	}
	

}
