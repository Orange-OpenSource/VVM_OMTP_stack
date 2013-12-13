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
package com.orange.labs.uk.omtp.provider;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import android.content.ContentValues;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.orange.labs.uk.omtp.db.DatabaseHelper;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.utils.CloseUtils;

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
	

	@Override
	public synchronized boolean updateProviderInfo(final OmtpProviderInfo providerInfo) {
		
		// check if providers table exists and create it if not before inserting 
		// any data to it
		mDatabaseHelper.checkIfTableExists(PROVIDERS_TABLE_NAME);

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
		

		boolean updatedOrInserted = false;
		String providerName = providerInfo.getProviderName();
		OmtpProviderInfo providerInfoFromDb = getProviderInfo(providerName);
		if(providerInfoFromDb == null) {
			// Insert it
			updatedOrInserted = database.replace(PROVIDERS_TABLE_NAME, null, values) != -1;
		}
		else {
			// Update the record
			String query = getEqualityClause(OmtpProviderColumns.PROVIDER_NAME, providerName);
			updatedOrInserted = (database.update(PROVIDERS_TABLE_NAME, values, query, null) > 0);
		}
		
		// Set the others providers as non current if this one is set as current 
		boolean otherProvidersUpdated = true;
		if(updatedOrInserted) {
			if(providerInfo.isCurrentProvider()) {
				// Get all the other providers from the db corresponding to the same operator
				List<OmtpProviderInfo> providersForSameOperator = getProvidersInfoWithNetworkOperator(providerInfo.getNetworkOperator());
				
				if(! providersForSameOperator.isEmpty()) {
					for (OmtpProviderInfo omtpProviderForSameOperator : providersForSameOperator) {
						// If it's not the provider set as current one and it is set as a current one
						if(! providerInfo.getProviderName().equals(omtpProviderForSameOperator.getProviderName())
								&& omtpProviderForSameOperator.isCurrentProvider()) {
							
							logger.d(String.format("Making omtp provider non current, %s", omtpProviderForSameOperator));
							boolean providerReplaced = replaceProviderInfoAndSetAsNonCurrent(omtpProviderForSameOperator, database);
							otherProvidersUpdated = otherProvidersUpdated && providerReplaced;
						}
					}
				}
			}
		}
		
		return updatedOrInserted && otherProvidersUpdated;
	}
	
	private boolean replaceProviderInfoAndSetAsNonCurrent(OmtpProviderInfo providerInfo, SQLiteDatabase database) {
		return replaceProviderInfoAndSetIsCurrentProviderValue(providerInfo, database, false);
	}
	
	private boolean replaceProviderInfoAndSetIsCurrentProviderValue(OmtpProviderInfo providerInfo, SQLiteDatabase database, boolean isCurrentProvider) {
		ContentValues values = getContentValues(providerInfo);
		if(values.containsKey(OmtpProviderColumns.IS_CURRENT_PROVIDER.getColumnName())) {
			values.put(OmtpProviderColumns.IS_CURRENT_PROVIDER.getColumnName(), isCurrentProvider);
		}
		
		// Update the record
		String query = getEqualityClause(OmtpProviderColumns.PROVIDER_NAME, values.getAsString(OmtpProviderColumns.PROVIDER_NAME.getColumnName()));
		boolean updated = (database.update(PROVIDERS_TABLE_NAME, values, query, null) > 0);

		return updated;
	}
	
	
	@Override
	public synchronized boolean removeProviderInfo(OmtpProviderInfo providerInfo) {
		
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
	
	@Override
	@Nullable
	public synchronized OmtpProviderInfo getProviderInfo(String providerName) {
		return getProviderInfo(OmtpProviderColumns.PROVIDER_NAME, providerName);
	}
	
	@Override
	@Nullable
	public synchronized OmtpProviderInfo getProviderInfoWithNetworkOperator(String networkOperator) {
		return getProviderInfo(OmtpProviderColumns.NETWORK_OPERATOR, networkOperator);
	}
	
	@Override
	@Nullable
	public synchronized OmtpProviderInfo getCurrentProviderInfoWithNetworkOperator(String networkOperator) {
		// First try to get the provider using the is current provider flag
		OmtpProviderColumns[] columns = {
				OmtpProviderColumns.NETWORK_OPERATOR,
				OmtpProviderColumns.IS_CURRENT_PROVIDER};
		String[] values = {
				networkOperator,
				"1"};
		OmtpProviderInfo providerInfo = getProviderInfo(columns, values);
		
		// In case it fails, return the first provider we can find corresponding to the network operator, we can't really fail, provider is important...
		if(providerInfo == null) {
			OmtpProviderInfo providerInfoCompatible = getProviderInfoWithNetworkOperator(networkOperator);
			// If we get it, set it as current one
			if(providerInfoCompatible != null) {
				OmtpProviderInfo.Builder builder = new OmtpProviderInfo.Builder();
				builder.setFieldsFromProvider(providerInfoCompatible);
				builder.setIsCurrentProvider(true);
				providerInfo = builder.build();

                // Set this provider as the current one
				updateProviderInfo(providerInfo);
			}
		}
		return providerInfo;
	}
	
	@Override
	@Nullable
	public synchronized List<OmtpProviderInfo> getProvidersInfoWithNetworkOperator(String networkOperator) {

		return getProvidersInfo(OmtpProviderColumns.NETWORK_OPERATOR, networkOperator);
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
	 * Retrieve from the database the records corresponding to the provider Provider Name.
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
