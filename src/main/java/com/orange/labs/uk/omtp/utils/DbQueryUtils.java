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
package com.orange.labs.uk.omtp.utils;

import android.content.ContentValues;
import android.database.DatabaseUtils;
import android.provider.VoicemailContract.Voicemails;
import android.text.TextUtils;

import com.orange.labs.uk.omtp.account.OmtpAccountInfo;
import com.orange.labs.uk.omtp.dependency.StackDependencyResolver;
import com.orange.labs.uk.omtp.dependency.StackDependencyResolverImpl;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.voicemail.Voicemail;

/**
 * Static methods for helping us build database query selection strings.
 */
public class DbQueryUtils {
	
	private static final Logger logger = Logger.getLogger(DbQueryUtils.class);
	
	// Static class with helper methods, so private constructor.
	private DbQueryUtils() {
	}

	/**
	 * Returns a WHERE clause assert equality of a field to a value for the
	 * specified table .
	 */
	public static String getEqualityClause(String table, String field, String value) {
		return getEqualityClause(table + "." + field, value);
	}

	/** Returns a WHERE clause assert equality of a field to a value. */
	public static String getEqualityClause(String field, String value) {
		StringBuilder clause = new StringBuilder();
		clause.append(field);
		clause.append(" = ");
		DatabaseUtils.appendEscapedSQLString(clause, value);
		return clause.toString();
	}

	/** Concatenates any number of clauses using "AND". */
	// TODO: 0. It worries me that I can change the following "AND" to "OR" and
	// the provider tests
	// all pass. I can also remove the braces, and the tests all pass.
	public static String concatenateClausesWithAnd(String... clauses) {
		return concatenateClausesWithOperation("AND", clauses);
	}

	/** Concatenates any number of clauses using "OR". */
	public static String concatenateClausesWithOr(String... clauses) {
		return concatenateClausesWithOperation("OR", clauses);
	}

	/** Concatenates any number of clauses using the specified operation. */
	public static String concatenateClausesWithOperation(String operation, String... clauses) {
		// Nothing to concatenate.
		if (clauses.length == 1) {
			return clauses[0];
		}

		StringBuilder builder = new StringBuilder();

		for (String clause : clauses) {
			if (!TextUtils.isEmpty(clause)) {
				if (builder.length() > 0) {
					builder.append(" ").append(operation).append(" ");
				}
				builder.append("(");
				builder.append(clause);
				builder.append(")");
			}
		}
		return builder.toString();
	}

	/**
	 * Method verifies if sender number is in correct format and updates it to
	 * international format prefixed with "+" I.e. the number is expected to be
	 * in the international format E.164, if the number is not prefixed with "+"
	 * or "00" the method below will add "+" prefix to the number
	 * 
	 * @param voicemail
	 *            received voicemail
	 * @param contentValues
	 *            content values that may require senders number update
	 */
	public static void verifySenderNumberFormat(final Voicemail voicemail,
			ContentValues contentValues) {

		// TODO: Verify with the country of deployment if this logic is correct
		if (voicemail.hasNumber()) {
			StackDependencyResolver depResolver = StackDependencyResolverImpl.getInstance();
			OmtpAccountInfo accountInfo = depResolver.getAccountStore().getAccountInfo();
			if (accountInfo == null) {
				logger.w("Unable to retrieve accountInfo and verify number format.");
				return;
			}
			String tuiNumber = accountInfo.getTuiNumber();
			String senderNumber = voicemail.getNumber();
			// if number is composed of digits but without the international + or 00 prefix
			// and is not the TUI number, add + prefix 
			if ((!senderNumber.matches("(((00|\\+)|0)[0-9]+)")) && (!senderNumber.equals(tuiNumber))) {
				senderNumber = "+" + senderNumber;
				logger.d(String.format("Added \"+\" to sender number, modified number %s",
						senderNumber));
			}
			// if sender number is a String of letters (probably "unknown" or similar)
			// returned by the IMAP server, then replace it with TUI number
			if (senderNumber.matches("((\\+)|[a-zA-Z])+")) {
				logger.d(String.format("Replacing source number:%s with TUI number:%s",
						senderNumber, tuiNumber));
				senderNumber = tuiNumber;
			}
			contentValues.put(Voicemails.NUMBER, senderNumber);
		}
	}
}
