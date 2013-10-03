package com.orange.labs.uk.omtp.voicemail.database;

import com.orange.labs.uk.omtp.db.DatabaseColumn;

/**
 * This enumeration defines the database columns used to store an OMTP voicemail. It is required to
 * store voicemail information despite Android already storing these because of lacks in the actual
 * Google implementation:
 * - Google does not notify of the voicemail that has been deleted after a user deletes a message, 
 * it simply indicates something has changed.
 * - When we get a notification from the Google API that something has changed on a particular 
 * message, the information store in this database helps us know what happened.
 */
public enum MirrorVoicemailProviderColumns implements DatabaseColumn {
	MESSAGE_UID("msg_uid", "TEXT PRIMARY KEY", 1), // remote server id
	MESSAGE_URI("msg_uri", "TEXT", 1), // local id
	READ("read", "INTEGER", 1); // read status
	
	private String mColumnName;
	private String mColumnType;
	private int mSinceVersion;
	
	private MirrorVoicemailProviderColumns(String columnName, String columnType, int sinceVersion) {
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
