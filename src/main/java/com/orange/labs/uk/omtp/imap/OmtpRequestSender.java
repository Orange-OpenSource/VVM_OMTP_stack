package com.orange.labs.uk.omtp.imap;

import com.orange.labs.uk.omtp.callbacks.Callback;

/**
 * Interface that defines the methods proposed to send OMTP specific IMAP requests to the remote
 * platform.
 */
public interface OmtpRequestSender {

	/**
	 * Send a Close NUT (New User Tutorial), using the provided callback to notify
	 * of the result.
	 * 
	 * @param callback
	 * 				{@link Callback} used to communicate the result back to the caller.
	 */
	public void closeNutRequest(Callback<Void> callback);
	
	/**
	 * Change the TUI password to the new provided value, using the provided callback to communicate
	 *  the result of the request back the caller.
	 * @param oldPassword
	 * 			Old password value. Should be exclusively digits.
	 * @param newPassword
	 * 			New password value. Should be exclusively digits.
	 * @param callback
	 * 			{@link Callback} used to communicate the result back to the caller.
	 */
	public void changeTuiPassword(String oldPassword, String newPassword, Callback<Void> callback);
}
