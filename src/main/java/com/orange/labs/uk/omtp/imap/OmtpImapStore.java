/*
 * Copyright (C) 2012 Orange Labs UK. All Rights Reserved.
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
package com.orange.labs.uk.omtp.imap;

import java.io.IOException;
import java.util.List;

import android.content.Context;

import com.android.email.mail.MessagingException;
import com.android.email.mail.store.ImapStore;
import com.android.email.mail.store.imap.ImapConstants;
import com.android.email.mail.store.imap.ImapResponse;
import com.android.email.mail.store.imap.ImapResponseParser.ByeException;
import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.logging.Logger;

/**
 * This class is built on top of the standard ImapStore and implements commands
 * specific to the OMTP protocol. Supported requests are the following:
 * 
 * - Close NUT: Allows to close the New User Tutorial and to pass the
 * provisioning status to Ready.
 * - Change Language: Changes the language of the TUI when the user calls it.
 */
public class OmtpImapStore extends ImapStore {

	private static Logger logger = Logger.getLogger(OmtpImapStore.class);

	/**
	 * Static named constructor.
	 */
	public static OmtpImapStore newInstance(String uri, Context context,
			PersistentDataCallbacks callbacks) throws MessagingException {
		return new OmtpImapStore(context, uri);
	}

	private OmtpImapStore(Context context, String uriString)
			throws MessagingException {
		super(context, uriString);
	}

	/**
	 * Send the NUT (New User Tutorial) closing command to the remote platform.
	 * A boolean that indicates if the command has been successfully sent and
	 * processed is returned by the method.
	 * 
	 * @param	callback
	 * 		Callback used to communicate the result back to the initiator.
	 * 
	 * @return boolean
	 * 		Indicates if the Close NUT command has been sent and processed successfully by the
	 * 		platform.
	 */
	public void closeNutRequest(Callback<Void> callback) {
		ImapConnection connection = getConnection();
		try {
			List<ImapResponse> responses = connection
					.executeSimpleCommand(OmtpImapConstants.CLOSE_NUT_REQUEST);
			for (ImapResponse response : responses) {
				logger.d(response.toString());
				if (response.isOk()
						&& response.contains(OmtpImapConstants.CLOSE_NUT_SUCCESS_RESPONSE)) {
					// if OK response is received, LOGOUT from the server
					// TODO: Check if it is the right thing to do...
					sendLogoutAndCloseConnection(connection, callback);
					callback.onSuccess(null);
				}
			}
		} catch (IOException e) {
			callback.onFailure(handleIoException(connection));
		} catch (MessagingException messEx) {
			callback.onFailure(messEx);
		} finally {
			connection.destroyResponses();
		}
	}
	
	/**
	 * Change the TUI current language to the provided value. This value should be an integer 
	 * between 1 and 16 and should be a supported language by the platform.
	 * @param language
	 * 		Integer between 1 and 16 that desginates the new language for the platform.
	 * @param callback
	 * 		Callback used to communicate the result back to the initiator.
	 * @return
	 * 		Boolean that indicates if the operation has been successful.
	 */
	public void changeTuiLanguage(int language, Callback<Void> callback) {
		if (language < 0 || language > 16) {
			throw new IllegalArgumentException("Language id outside of bounds (1/16)");
		}
		
		ImapConnection connection = getConnection();
		try {
			List<ImapResponse> responses = connection
					.executeSimpleCommand(OmtpImapConstants.CHANGE_LANGUAGE_REQUEST + language);
			for (ImapResponse response : responses) {
				logger.d(response.toString());
				if (response.isOk()
						&& response.contains(OmtpImapConstants.CHANGE_LANGUAGE_RESPONSE)) {
					// if OK response is received, LOGOUT from the server
					sendLogoutAndCloseConnection(connection, callback);
					callback.onSuccess(null);
				} else if (response.contains(OmtpImapConstants.CHANGE_LANGUAGE_INVALID_RESPONSE)) {
					throw new MessagingException("Unsupported TUI language.");
				} else {
					callback.onFailure(new MessagingException("Invalid answer"));
				}
			}

		} catch (IOException e) {
			callback.onFailure(handleIoException(connection));
		} catch (MessagingException messEx) { 
			callback.onFailure(messEx);
		} finally {
			connection.destroyResponses();
		}
	}
	
	/**
	 * Send a Change Password request to the remote platform.
	 * @param oldPassword
	 * 			Old password value used to authenticate.
	 * @param newPassword
	 * 			New password that will replace the old value.
	 * @param callback
	 * 			Callback to communicate the result back to the caller.
	 */
	public void changeTuiPassword(String oldPassword, String newPassword,
			Callback<Void> callback) {
		//TODO: check old and new password are only digits.
		ImapConnection connection = getConnection();
		try {
			List<ImapResponse> responses = connection
					.executeSimpleCommand(String.format(OmtpImapConstants.CHANGE_PASSWORD_REQUEST, 
							oldPassword, newPassword));
			for (ImapResponse response : responses) {
				logger.d(response.toString());
				if (response.isOk()
						&& response.contains(OmtpImapConstants.CHANGE_PASSWORD_RESPONSE)) {
					// if OK response is received, LOGOUT from the server
					sendLogoutAndCloseConnection(connection, callback);
					callback.onSuccess(null);
				} else if (response.contains(OmtpImapConstants.CHANGE_PASSWORD_MISMATCH_RESPONSE)) {
					throw new MessagingException("Incorrect old password");
				} else if (response.contains(OmtpImapConstants.CHANGE_PASSWORD_TOO_LONG_RESPONSE)) {
					throw new MessagingException("New password is too long");
				} else if (response.contains(OmtpImapConstants.CHANGE_PASSWORD_TOO_SHORT_RESPONSE)) {
					throw new MessagingException("New password is too short");
				} else if (response.contains(OmtpImapConstants.CHANGE_PASSWORD_INVALID_RESPONSE)) {
					throw new MessagingException("Password contains invalid characters.");
				} else {
					throw new MessagingException("Impossible to change the password.");
				}
			}

			callback.onFailure(new MessagingException("Invalid answer"));
		} catch (IOException e) {
			callback.onFailure(handleIoException(connection));
		} catch (MessagingException messEx) { 
			callback.onFailure(messEx);
		} finally {
			connection.destroyResponses();
		}
	}
	
	/**
	 * Sends IMAP LOGOUT commend to the server and closes IMAP connection
	 * @param connection
	 * @param callback
	 */
	private void sendLogoutAndCloseConnection(ImapConnection connection, Callback<Void> callback) {
		try {
			connection.executeSimpleCommand(String.format(ImapConstants.LOGOUT));
		} catch (ByeException be) {
			// Do nothing, it's normal
		} catch (IOException ioe) {
			// Only log this exception
			// it is usually ByeException generated after BYE message is received as an
			// answer to LOGOUT command
			logger.w(ioe.getMessage());
		} catch (MessagingException messEx) {
			callback.onFailure(messEx);
		}
		
		if (connection != null) {
			connection.destroyResponses();
			connection.close();
		}
	}

	
	/**
	 * Method handling MessagingExceptions
	 * @param connection
	 * @return
	 */
	private MessagingException handleIoException(ImapConnection connection) {
		if (connection != null) {
			connection.destroyResponses();
			connection.close();
		}
		
		return new MessagingException("IO Exception occured");
	}

}
