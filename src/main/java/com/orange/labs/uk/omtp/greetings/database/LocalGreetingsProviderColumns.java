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
package com.orange.labs.uk.omtp.greetings.database;

import com.orange.labs.uk.omtp.db.DatabaseColumn;

/**
 * This enumeration defines the database columns used to store an OMTP greetings message. 
 * It is required to store greetings information in order to compare if the greeting currently
 * downloaded or being downloaded is the same as the greeting currently present on the server.
 */
public enum LocalGreetingsProviderColumns implements DatabaseColumn {
	GREETING_MESSAGE_UID("greeting_msg_uid", "TEXT PRIMARY KEY", 2), // IMAP message id (from the server)
	GREETING_TYPE("greeting_type", "TEXT", 2), // greeting type (normal|voice_signature)
	IS_GREETING_ACTIVE("is_greeting_active", "INTEGER", 2), // activation status
	IS_CONTENT_DOWNLOADED("is_content_downloaded", "INTEGER", 2); // voice attachment download status
	
	private final String mColumnName;
	private final String mColumnType;
	private final int mSinceVersion;

	private LocalGreetingsProviderColumns(String columnName, String columnType, int sinceVersion) {
		mColumnName = columnName;
		mColumnType = columnType;
		mSinceVersion = sinceVersion;
	}
	
	@Override
	public String getColumnName() {
		return mColumnName;
	}

	@Override
	public String getColumnType() {
		return mColumnType;
	}

	@Override
	public int getSinceVersion() {
		return mSinceVersion;
	}

}
