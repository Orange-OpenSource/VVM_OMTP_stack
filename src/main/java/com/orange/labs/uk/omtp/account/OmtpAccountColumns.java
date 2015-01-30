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

import com.orange.labs.uk.omtp.db.DatabaseColumn;

/**
 * Database columns in the OMTP account database.
 * 
 * Accounts are used to store voicemail sources that register to the stack, and their specific 
 * parameters to access the remote platform.
 */
public enum OmtpAccountColumns implements DatabaseColumn {
	ACCOUNT_ID("account_id", "TEXT PRIMARY KEY", 1),
	IMAP_USERNAME("imap_username", "TEXT", 1),
	IMAP_PASSWORD("imap_password", "TEXT", 1),
	IMAP_SERVER("imap_server", "TEXT", 1),
	IMAP_PORT("imap_port", "TEXT", 1),
	SMS_NUMBER("sms_number", "TEXT", 1),
	TUI_NUMBER("tui_number", "TEXT", 1),
	SUBSCRIPTION_URL("subs_url", "TEXT", 1),
	PROVISIONING_STATUS("status", "TEXT", 1),
	MAX_ALLOWED_GREETINGS_LENGTH("max_allowed_greetings_length", "INTEGER", 2),
	MAX_ALLOWED_VOICESIGNATURE_LENGTH("max_voicesignature_length", "INTEGER", 2),
	SUPPORTED_LANGUAGES("supported_languages", "TEXT", 2);
	
	private final String mName;
	private final String mSqlType;
	private final int mSinceVersion;

	private OmtpAccountColumns(String name, String sqlType, int sinceVersion) {
		mName = name;
		mSqlType = sqlType;
		mSinceVersion = sinceVersion;
	}
	
	@Override
	public String getColumnName() {
		return mName;
	}

	@Override
	public String getColumnType() {
		return mSqlType;
	}

	@Override
	public int getSinceVersion() {
		return mSinceVersion;
	}

}
