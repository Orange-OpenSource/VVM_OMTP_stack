package com.orange.labs.uk.omtp.sms.timeout;

public interface SmsTimeoutHandler {

	/**
	 * This method tries to cancel previously scheduled SMS timeout task.
	 */
	public void cancelOldSmsTimeoutTask();

	/**
	 * Create a new SMS timeout task used to notify the application when no
	 * response has been received for a request. This timeout should be
	 * cancelled when a response is received by the source. This method should
	 * be called when stack has received a sent confirmation returned in
	 * {@link android.app.PendingIntent} sentIntent by
	 * {@link android.telephony.SmsManager#sendTextMessage(String, String, 
	 * String, android.app.PendingIntent, android.app.PendingIntent)}
	 * 
	 */
	public void createNewSmsTimeoutTask();
}
