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
package com.orange.labs.uk.omtp.provider;

import android.content.ContentValues;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.orange.labs.uk.omtp.db.DatabaseHelper;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.utils.CloseUtils;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 *	Utility class that allows the manipulation of the {@link ProviderInfo} stored in the stack
 *	database.
 */
public class OmtpProviderDatabase implements OmtpProviderStore {

	private static Logger logger = Logger.getLogger(OmtpProviderDatabase.class);
	
	public static final String PROVIDERS_TABLE_NAME = "providers";
	
	private DatabaseHelper mDatabaseHelper;

	public OmtpProviderDatabase(DatabaseHelper dbHelper) {
		mDatabaseHelper = dbHelper;
	}

    /**
     * Get a provider by name
     *
     * @param providerName
     * @return
     */
	@Override
	@Nullable
	public OmtpProviderInfo getProviderInfoByName(String providerName) {
		return getProviderInfo(OmtpProviderColumns.PROVIDER_NAME, providerName);
	}

    /**
     * Get a provider for an operator and set as current
     * Will return the first one found if it exists
     *
     * @param networkOperator
     * @return
     */
	@Override
	@Nullable
	public OmtpProviderInfo getCurrentProviderInfoWithNetworkOperator(String networkOperator) {
		// First try to get the provider using the is current provider flag
		OmtpProviderColumns[] columns = {
				OmtpProviderColumns.NETWORK_OPERATOR,
				OmtpProviderColumns.IS_CURRENT_PROVIDER};
		String[] values = {
				networkOperator,
				"1"};
		OmtpProviderInfo providerInfo = getProviderInfo(columns, values);

		return providerInfo;
	}

    /**
     * Return a list of providers for an operator
     *
     * @param networkOperator Network operator name
     * @return
     */
	@Override
	@Nullable
	public List<OmtpProviderInfo> getProvidersInfoWithNetworkOperator(String networkOperator) {
		return getProvidersInfo(OmtpProviderColumns.NETWORK_OPERATOR, networkOperator);
	}

    @Override
    public boolean updateProviderInfo(final OmtpProviderInfo providerInfo) {
        logger.d(String.format("Inserting/Updating provider information named: %s",
                providerInfo.getProviderName()));

        // Get Db access
        SQLiteDatabase database = null;
        try {
            database = mDatabaseHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            logger.e(String.format("Impossible to get a writable database: %s",
                    e.getLocalizedMessage()));
            return false;
        }

        ContentValues values = getContentValues(providerInfo);

        return database.replace(PROVIDERS_TABLE_NAME, null, values) != -1;
    }
	
	/**
	 * Returns a cursor allowing to go through a list of Providers filtered on a column with a specific value
	 * Important: do not forget to close the cursor after using it.
	 * 
	 * @param column Column used for the filter
	 * @param value Value used for the filter
	 * @return Cursor, null if there is a problem during the query
	 */
	@Nullable
	private Cursor getCursorProvidersInfo(OmtpProviderColumns column, String value) {
		logger.d(String.format("Retrieving providers with %s: %s", column.getColumnName(),
				value));
		
		SQLiteDatabase database = getReadableDatabase();
		if (database == null)
			return null;
		
		String query = getEqualityClause(column, value);
		
		// Cursor that will contain the result.
		Cursor cursor = null;
		try {
			cursor = database.query(PROVIDERS_TABLE_NAME, null, query, null, null, null, null);
		}
		catch(SQLiteException sqlException) {
			logger.e("Couldn't get providers", sqlException);
			return null;
		}
		return cursor;
	}
	
	/**
	 * Returns a cursor allowing to go through a list of Providers filtered on multiple columns with a specific values
	 * Important: do not forget to close the cursor after using it.
	 * 
	 * @param columns Columns used for the filter
	 * @param values Values used for the filter
	 * @return Cursor, null if there is a problem during the query
	 */
	@Nullable
	private Cursor getCursorProvidersInfo(OmtpProviderColumns[] columns, String[] values) {
		logger.d("Retrieving providers");
		
		// Check columns and values have the same size
		if(columns.length != values.length) {
			return null;
		}
		
		SQLiteDatabase database = getReadableDatabase();
		if (database == null)
			return null;
		
		// Build up the query
		String logMessage = "Retrieving providers with filters: ";
		String query = null;
		if(columns.length > 0) {
			query = "";
			for (int i = 0; i < columns.length; i++) {
				OmtpProviderColumns column = columns[i];
				String value = values[i];
				query += getEqualityClause(column, value);
				// If it's not the last clause, add the AND keyword
				if(i < columns.length - 1) {
					query += " AND ";
					logMessage += String.format("%s: %s AND \n", column.getColumnName(), value);
				}
				else {
					logMessage += String.format("%s: %s", column.getColumnName(), value);
				}
			}
		}
		else {
			logMessage += "no filter specified";	
		}
		logger.d(logMessage);
		
		// Cursor that will contain the result.
		Cursor cursor = null;
		try {
			cursor = database.query(PROVIDERS_TABLE_NAME, null, query, null, null, null, null);
		}
		catch(SQLiteException sqlException) {
			logger.e("Couldn't get providers", sqlException);
			return null;
		}
		return cursor;
	}
	
	/**
	 * Retrieve from the database the record corresponding to the column/value combination
	 * <p>
	 * Build a {@link OmtpProviderInfo} instance from the record retrieved from the database and
	 * returns it. If such record cannot be found, or if an error occurs, return null instead.
	 */
	@Nullable
	private OmtpProviderInfo getProviderInfo(OmtpProviderColumns column, String value) {
		// Cursor that will contain the result.
		Cursor cursor = getCursorProvidersInfo(column, value);
        if(cursor == null)
            return null;
		try {
			if (cursor.moveToFirst()) {
				OmtpProviderInfo.Builder builder = new OmtpProviderInfo.Builder();
				OmtpProviderInfo providerInfo = builder.setFieldsFromCursor(cursor).build();
				
				logger.d(String.format("Retrieved Provider Info: %s", providerInfo.toString()));
				
				return providerInfo;
			} else {
				// No provider info has been found.
				return null;
			}
		} finally {
			CloseUtils.closeQuietly(cursor);
		}
	}
	
	/**
	 * Retrieve from the database the record corresponding to the colums/values combinations
	 * <p>
	 * Build a {@link OmtpProviderInfo} instance from the record retrieved from the database and
	 * returns it. If such record cannot be found, or if an error occurs, return null instead.
	 */
	@Nullable
	private OmtpProviderInfo getProviderInfo(OmtpProviderColumns[] columns, String[] values) {
		// Check columns and values have the same size
		if(columns.length != values.length) {
			return null;
		}
		
		// Cursor that will contain the result.
		Cursor cursor = null;
		try {
			cursor = getCursorProvidersInfo(columns, values);
			if (cursor != null 
					&& cursor.moveToFirst()) {
				OmtpProviderInfo.Builder builder = new OmtpProviderInfo.Builder();
				OmtpProviderInfo providerInfo = builder.setFieldsFromCursor(cursor).build();
				
				logger.d(String.format("Retrieved Provider Info: %s", providerInfo.toString()));
				
				return providerInfo;
			} else {
				// No provider info has been found.
				return null;
			}
		}
		catch (SQLiteException sqlException) {
			logger.e("Couldn't get provider info", sqlException);
			return null;
		}
		finally {
			CloseUtils.closeQuietly(cursor);
		}
	}
	
	/**
	 * Retrieve from the database the records corresponding to the column/value combination
     *
	 * @param column
	 * @param value
	 * @return
	 */
	private List<OmtpProviderInfo> getProvidersInfo(OmtpProviderColumns column, String value) {
		// Cursor that will contain the result.
		Cursor cursor = getCursorProvidersInfo(column, value);
		OmtpProviderInfo.Builder builder = new OmtpProviderInfo.Builder();
		List<OmtpProviderInfo> providersInfo = new ArrayList<OmtpProviderInfo>();
		
		try {
			while(cursor.moveToNext()) {
				OmtpProviderInfo providerInfo = builder.setFieldsFromCursor(cursor).build();
				logger.d(String.format("Retrieved Provider Info: %s",
						providerInfo.toString()));
				providersInfo.add(providerInfo);
			}
			return providersInfo;
		} finally {
			CloseUtils.closeQuietly(cursor);
		}
	}

    /**
     * Remove a provider
     *
     * @param providerInfo {@link OmtpProviderInfo} to be removed
     * @return
     */
    @Override
    public boolean removeProviderInfo(OmtpProviderInfo providerInfo) {

        if(providerInfo == null) {
            logger.d("Cannot continue removing provider as it is null");
            return false;
        }

        logger.d(String.format("Removing provider information named: %s", providerInfo.getProviderName()));

        SQLiteDatabase database = null;
        try {
            database = mDatabaseHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            logger.e(String.format("Impossible to get a writable database: %s",
                    e.getLocalizedMessage()));
            return false;
        }

        String query = getEqualityClause(OmtpProviderColumns.PROVIDER_NAME, providerInfo.getProviderName());

        return (database.delete(PROVIDERS_TABLE_NAME, query, null) > 0);
    }

	/**
	 * Generate a {@link ContentValues} object from the provided {@link OmtpProviderInfo}.
	 */
	static ContentValues getContentValues(OmtpProviderInfo providerInfo) {
		// All values are mandatory, so no verification on attribute presence.
		ContentValues contentValues = new ContentValues();
		contentValues.put(OmtpProviderColumns.PROVIDER_NAME.getColumnName(),
				providerInfo.getProviderName());
		contentValues.put(OmtpProviderColumns.NETWORK_OPERATOR.getColumnName(),
				providerInfo.getNetworkOperator());
		contentValues.put(OmtpProviderColumns.CLIENT_TYPE.getColumnName(),
				providerInfo.getClientType());
		contentValues.put(OmtpProviderColumns.DATE_FORMAT.getColumnName(),
				providerInfo.getDateFormat());
		contentValues.put(OmtpProviderColumns.PROTOCOL_VERSION.getColumnName(),
				providerInfo.getProtocolVersion().getCode());
		contentValues.put(OmtpProviderColumns.SMS_DESTINATION_NUMBER.getColumnName(),
				providerInfo.getSmsDestinationNumber());
		contentValues.put(OmtpProviderColumns.SMS_DESTINATION_PORT.getColumnName(),
				providerInfo.getSmsDestinationPort());
        // The short service center number may be null
        if(providerInfo.getSmsServiceCenter() == null) {
            contentValues.putNull(OmtpProviderColumns.SMS_SERVICE_CENTER.getColumnName());
        }
        else {
            contentValues.put(OmtpProviderColumns.SMS_SERVICE_CENTER.getColumnName(),
                    providerInfo.getSmsServiceCenter());
        }
		contentValues.put(OmtpProviderColumns.IS_CURRENT_PROVIDER.getColumnName(),
				providerInfo.isCurrentProvider());
		return contentValues;
	}
	
	/**
	 * Generate an SQLite equality clause using on the specified column, using the provided value.
	 */
	private String getEqualityClause(OmtpProviderColumns column, String value) {
		StringBuilder clause = new StringBuilder();
		clause.append("(");
		clause.append(PROVIDERS_TABLE_NAME);
		clause.append(".");
		clause.append(column.getColumnName());
		clause.append(" = ");
		DatabaseUtils.appendEscapedSQLString(clause, value);
		clause.append(")");
		return clause.toString();
	}

    /**
     * Get an OMTP readable database
     *
     * @return OMTP stack readable database, null if it can't be opened
     */
    @Nullable
    private SQLiteDatabase getReadableDatabase() {
        SQLiteDatabase database = null;
        try {
            database = mDatabaseHelper.getReadableDatabase();
        } catch (SQLiteException e) {
            logger.e(String.format("Impossible to open the OMTP Stack database: %s",
                    e.getLocalizedMessage()));
            return null;
        }
        return database;
    }

	public boolean deleteTableContent() {
		// Get Db access
		SQLiteDatabase database = null;
		try {
			database = mDatabaseHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			logger.e(String.format("Impossible to get a writable database: %s",
					e.getLocalizedMessage()));
			return false;
		}
		
		database.delete(PROVIDERS_TABLE_NAME, null, null);
		return true;
	}
}
