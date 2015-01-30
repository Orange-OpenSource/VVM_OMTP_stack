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
package com.orange.labs.uk.omtp.provider;

import com.orange.labs.uk.omtp.db.DatabaseColumn;

/**
 * Database columns for the OMTP Provider database.
 */
public enum OmtpProviderColumns implements DatabaseColumn {
	PROVIDER_NAME("name", "TEXT PRIMARY KEY", 1),
	NETWORK_OPERATOR("network_operator", "TEXT", 1),
	PROTOCOL_VERSION("prot_version", "TEXT", 1),
	CLIENT_TYPE("client_type", "TEXT", 1),
	SMS_DESTINATION_NUMBER("sms_destination", "TEXT", 1),
	SMS_DESTINATION_PORT("sms_port", "INTEGER", 1),
    SMS_SERVICE_CENTER("sms_service_center", "TEXT", 4),
	DATE_FORMAT("date_format", "TEXT", 1),
	IS_CURRENT_PROVIDER("is_current_provider", "INTEGER", 3);
	
	private String mName;
	private String mSqlType;
	private int mSinceVersion;
	
	private OmtpProviderColumns(String name, String sqlType, int sinceVersion) {
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
