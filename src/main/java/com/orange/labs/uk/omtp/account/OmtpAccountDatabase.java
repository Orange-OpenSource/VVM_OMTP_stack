/*
 * Copyright (C) 2012 Orange Labs UK. All Rights Reserved.
 * Copyright (C) 2011 Google
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
package com.orange.labs.uk.omtp.account;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.orange.labs.uk.omtp.db.DatabaseHelper;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.utils.CloseUtils;

import java.util.ArrayList;
import java.util.List;

public class OmtpAccountDatabase implements OmtpAccountStore {

	private static Logger logger = Logger.getLogger(OmtpAccountDatabase.class);

	public static final String ACCOUNT_TABLE_NAME = "accounts";

	private DatabaseHelper mDatabaseHelper;

	public OmtpAccountDatabase(DatabaseHelper dbHelper) {
		mDatabaseHelper = dbHelper;
	}

	@Override
	public boolean updateAccountInfo(final OmtpAccountInfo accountInfo) {

		SQLiteDatabase database = null;
		try {
			database = mDatabaseHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			logger.e(String.format("Impossible to get a writable database: %s",
					e.getLocalizedMessage()));
			return false;
		}

		ContentValues values = getContentValues(accountInfo);
		logger.d(String.format("Inserting/Updating account with ContentValues: %s",
				values.toString()));

		String accountId = accountInfo.getAccountId();
		OmtpAccountInfo account = getAccountInfo(accountId);
		if (account == null) {
			// Insert the record if no account exists.
			return (database.replace(ACCOUNT_TABLE_NAME, null, values) != -1);
		} else {
			// Update the record if an account already exists.
			String query = getEqualityClause(OmtpAccountColumns.ACCOUNT_ID, accountId);
			boolean status = (database.update(ACCOUNT_TABLE_NAME, values, query, null) > 0);
			logger.d(String.format("Updating status: %s", status));
			return status;
		}

	}

	@Override
	public List<OmtpAccountInfo> getAllAccounts() {
		logger.d("Retrieving all accounts from the database");

		List<OmtpAccountInfo> accounts = new ArrayList<OmtpAccountInfo>();

		SQLiteDatabase database = null;
		try {
			database = mDatabaseHelper.getReadableDatabase();
		} catch (SQLiteException e) {
			logger.e(String.format("Impossible to open the OMTP Stack database: %s",
					e.getLocalizedMessage()));
			return accounts;
		}

		Cursor cursor = null;
		try {
			cursor = database.query(ACCOUNT_TABLE_NAME, null, "", new String[0], null, null, null);
			while (cursor.moveToNext()) {
				OmtpAccountInfo.Builder builder = new OmtpAccountInfo.Builder();
				OmtpAccountInfo account = builder.setFieldsFromCursor(cursor).build();
				accounts.add(account);
			}
		} finally {
			CloseUtils.closeQuietly(cursor);
		}

		return accounts;
	}

	@Override
	public OmtpAccountInfo getAccountInfo(final String accountId) {

		logger.d(String.format("Retrieving account with ID: %s", accountId));

		SQLiteDatabase database = null;
		try {
			database = mDatabaseHelper.getReadableDatabase();
		} catch (SQLiteException e) {
			logger.e(String.format("Impossible to open the OMTP Stack database: %s",
					e.getLocalizedMessage()));
			return null;
		}

		String query = getEqualityClause(OmtpAccountColumns.ACCOUNT_ID, accountId);

		// Cursor that will contain the result.
		Cursor cursor = null;
		try {
			cursor = database.query(ACCOUNT_TABLE_NAME, null, query, null, null, null, null);
			if (cursor.moveToFirst()) {
				OmtpAccountInfo.Builder builder = new OmtpAccountInfo.Builder();
				OmtpAccountInfo accountInfo = builder.setFieldsFromCursor(cursor).build();

				logger.d(String.format("Retrieved Account Info: %s", accountInfo.toString()));

				return accountInfo;
			} else {
				// No account info has been found.
				return null;
			}
		} finally {
			CloseUtils.closeQuietly(cursor);
		}
	}

	@Override
	public boolean removeAccountInfo(final String accountId) {

		logger.d(String.format("Removing account information for account id: %s", accountId));

		SQLiteDatabase database = null;
		try {
			database = mDatabaseHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			logger.e(String.format("Impossible to get a writable database: %s",
					e.getLocalizedMessage()));
			return false;
		}

		String query = getEqualityClause(OmtpAccountColumns.ACCOUNT_ID, accountId);

		return (database.delete(ACCOUNT_TABLE_NAME, query, null) > 0);
	}

	/**
	 * Generate a {@link ContentValues} object from the provided {@link OmtpAccountInfo}.
	 */
	private ContentValues getContentValues(final OmtpAccountInfo account) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(OmtpAccountColumns.ACCOUNT_ID.getColumnName(), account.getAccountId());
		if (account.hasImapUsername()) {
			contentValues.put(OmtpAccountColumns.IMAP_USERNAME.getColumnName(),
					account.getImapUsername());
		}
		if (account.hasImapPassword()) {
			contentValues.put(OmtpAccountColumns.IMAP_PASSWORD.getColumnName(),
					account.getImapPassword());
		}
		if (account.hasImapServer()) {
			contentValues.put(OmtpAccountColumns.IMAP_SERVER.getColumnName(),
					account.getImapServer());
		}
		if (account.hasImapPort()) {
			contentValues.put(OmtpAccountColumns.IMAP_PORT.getColumnName(), account.getImapPort());
		}
		if (account.hasClientSmsDestinationNumber()) {
			contentValues
					.put(OmtpAccountColumns.SMS_NUMBER.getColumnName(), account.getSmsNumber());
		}
		if (account.hasTuiNumber()) {
			contentValues
					.put(OmtpAccountColumns.TUI_NUMBER.getColumnName(), account.getTuiNumber());
		}
		if (account.hasSubscriptionUrl()) {
			contentValues.put(OmtpAccountColumns.SUBSCRIPTION_URL.getColumnName(),
					account.getSubscriptionUrl());
		}
		if (account.hasProvisioningStatus()) {
			contentValues.put(OmtpAccountColumns.PROVISIONING_STATUS.getColumnName(), account
					.getProvisionningStatus().getCode());
		}
		if (account.getMaxAllowedGreetingsLength() != 0) {
			contentValues.put(OmtpAccountColumns.MAX_ALLOWED_GREETINGS_LENGTH.getColumnName(), account
					.getMaxAllowedGreetingsLength());
		}
		if (account.getMaxAllowedVoiceSignatureLength() != 0) {
			contentValues.put(OmtpAccountColumns.MAX_ALLOWED_VOICESIGNATURE_LENGTH.getColumnName(), account
					.getMaxAllowedVoiceSignatureLength());
		}
		if (account.hasSupportedLnaguages()) {
			contentValues.put(OmtpAccountColumns.SUPPORTED_LANGUAGES.getColumnName(), account
					.getSupportedLnaguages());
		}
		return contentValues;
	}

	/**
	 * Generate an SQLite equality clause using on the specified column, using the provided value.
	 */
	private String getEqualityClause(OmtpAccountColumns column, String value) {
		StringBuilder clause = new StringBuilder();
		clause.append("(");
		clause.append(ACCOUNT_TABLE_NAME);
		clause.append(".");
		clause.append(column.getColumnName());
		clause.append(" = ");
		DatabaseUtils.appendEscapedSQLString(clause, value);
		clause.append(")");
		return clause.toString();
	}

	@Override
	public void deleteAll() {
		logger.d("Removing all accounts information");

		SQLiteDatabase database = null;
		try {
			database = mDatabaseHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			logger.e(String.format("Impossible to get a writable database: %s",
					e.getLocalizedMessage()));
			return;
		}

		database.delete(ACCOUNT_TABLE_NAME, null, null);
	}

}
