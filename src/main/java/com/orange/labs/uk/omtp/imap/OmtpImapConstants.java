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

/**
 * Final class that contains all constants used by the IMAP commands/responses used specifically by
 * the OMTP protocol.
 */
public final class OmtpImapConstants {
	/** Command sent to the remote IMAP platform to close the New User Tutorial. */
	public static final String CLOSE_NUT_REQUEST = "XCLOSE_NUT";

	/** Response in case of success while sending a NUT command. */
	public static final String CLOSE_NUT_SUCCESS_RESPONSE = "NUT closed";

	/** Request sent to the remote IMAP platform to change the TUI language. */
	public static final String CHANGE_LANGUAGE_REQUEST = "XCHANGE_VM_LANG LANG=";

	/** Response in case of success while sending a Change Language request */
	public static final String CHANGE_LANGUAGE_RESPONSE = "language changed successfully";

	/** Response sent by the platform in case the provided language was invalid */
	public static final String CHANGE_LANGUAGE_INVALID_RESPONSE = "invalid language";

	/** Request sent to the remote IMAP platform to change the TUI password. */
	public static final String CHANGE_PASSWORD_REQUEST = "XCHANGE_TUI_PWD PWD=%s OLD_PWD=%s";

	/** Response when the change password request is successful. */
	public static final String CHANGE_PASSWORD_RESPONSE = "password changed successfully";

	/** Response sent back when the new password is too short. */
	public static final String CHANGE_PASSWORD_TOO_SHORT_RESPONSE = "password too short";

	/** Response sent back when the new password is too long. */
	public static final String CHANGE_PASSWORD_TOO_LONG_RESPONSE = "password too long";

	/** Response sent back when the sent old password does not correspond to the record. */
	public static final String CHANGE_PASSWORD_MISMATCH_RESPONSE = "old password mismatch";

	/** Response sent back when the password contains invalid characters. */
	public static final String CHANGE_PASSWORD_INVALID_RESPONSE = "password contains invalid characters";

	/** Response when authentication fails because the user is blocked */
	public static final String AUTH_USER_BLOCKED = "user is blocked";

	/** Response when authentication fails because the user is unknown */
	public static final String AUTH_USER_UNKNOWN = "unknown user";

	/** Response when authentication fails because the credentials are incorrect */
	public static final String AUTH_INVALID_PASSWORD = "invalid password";

	/** Response when authentication fails because the user account is not provisioned */
	public static final String AUTH_NOT_PROVISIONED = "service is not provisioned";

	/** Response when authentication fails because the user account is not active. */
	public static final String AUTH_NOT_ACTIVATED = "service is not activated";

	/** Response when authentication fails because the user account is not initialized */
	public static final String AUTH_NOT_INITIALIZED = "mailbox not initialized";

	/** Response when authentication fails because the client type is invalid */
	public static final String AUTH_UNKNOWN_CLIENT = "unknown client";
}
