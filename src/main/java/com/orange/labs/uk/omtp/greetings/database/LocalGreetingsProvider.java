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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.orange.labs.uk.omtp.db.DatabaseHelper;
import com.orange.labs.uk.omtp.greetings.Greeting;
import com.orange.labs.uk.omtp.greetings.GreetingImpl;
import com.orange.labs.uk.omtp.greetings.GreetingType;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.utils.CloseUtils;
import com.orange.labs.uk.omtp.voicemail.Voicemail;
import com.orange.labs.uk.omtp.voicemail.VoicemailImpl;


/**
 * Provides a simple interface to manipulate Greetings within the Greetings
 * content provider.
 */
public class LocalGreetingsProvider {

	private static Logger logger = Logger.getLogger(LocalGreetingsProvider.class);

	public static final String GREETINGS_TABLE_NAME = "greetings";

	private DatabaseHelper mDatabaseHelper;

	public LocalGreetingsProvider(DatabaseHelper dbHelper) {
		mDatabaseHelper = dbHelper;
	}

	/**
	 * Update greeting in db.
	 * @param greeting
	 * @return
	 */
	public boolean updateGreeting(final Greeting greeting) {
		logger.d(String.format("Inserting/Updating the following greeting: %s",
				greeting.toString()));

		SQLiteDatabase database = getWritableDatabase();
		if (database == null || greeting.getVoicemail().getSourceData() == null) {
			return false;
		}

		boolean insertResult = insertGreeting(greeting, database);
		
		// if the new Greeting is active, make the other one inactive.
		if (greeting.isActive()) {
			// fetch all other type Greetings currently present in local db and make them inactive
			List<Greeting> allGreetings = getAllGreetings();
			if (!allGreetings.isEmpty()) {
				for (Greeting fetchedGreeting : allGreetings) {
					if (!fetchedGreeting.getGreetingType().equals(greeting.getGreetingType())) {
						logger.d(String.format("Making other Greeting inactive,  %s", fetchedGreeting));
						insertGreetingAndSetInactive(fetchedGreeting, database);
					}
				}
			}
		}
		
		return insertResult;
	}

	/**
	 * Insert Greeting to local Greetings db and make it inactive.
	 * @param fetchedGreeting
	 * @param database
	 */
	private boolean insertGreetingAndSetInactive(Greeting greeting, SQLiteDatabase database) {
		ContentValues values = getContentValues(greeting);
		if (values.containsKey(LocalGreetingsProviderColumns.IS_GREETING_ACTIVE.getColumnName())) {
			values.put(LocalGreetingsProviderColumns.IS_GREETING_ACTIVE.getColumnName(), false);
		}
		return (database.replace(GREETINGS_TABLE_NAME, null, values) != -1);
		
	}

	/**
	 * Insert greeting to db.
	 * @param greeting
	 * @param database
	 * @return
	 */
	private boolean insertGreeting(final Greeting greeting, final SQLiteDatabase database) {
		ContentValues values = getContentValues(greeting);
		return (database.replace(GREETINGS_TABLE_NAME, null, values) != -1);
	}
	
	/**
	 * Generates {@link ContentValues} from a provided {@link Greeting}.
	 * @param input greeting
	 * @return ContentValue of a given Greeting
	 */
	private ContentValues getContentValues(Greeting greeting) {
		ContentValues cv = new ContentValues();
		if (greeting.hasVoicemail()) {
			cv.put(LocalGreetingsProviderColumns.GREETING_MESSAGE_UID.getColumnName(), greeting
					.getVoicemail().getSourceData());
		}
		cv.put(LocalGreetingsProviderColumns.GREETING_TYPE.getColumnName(), greeting
				.getGreetingType().getTypeString());
		cv.put(LocalGreetingsProviderColumns.IS_GREETING_ACTIVE.getColumnName(),
				greeting.isActive());
		cv.put(LocalGreetingsProviderColumns.IS_CONTENT_DOWNLOADED.getColumnName(),
				greeting.isVoiceContentDownloaded());
		return cv;
	}

	/**
	 * Returns a Writable database or null if an exception occurred.
	 */
	@Nullable
	private SQLiteDatabase getWritableDatabase() {
		SQLiteDatabase database = null;
		try {
			database = mDatabaseHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			logger.e(String.format("Impossible to get a writable database: %s",
					e.getLocalizedMessage()));
		}
		return database;
	}
	
	/**
	 * Delete a particular greeting in the database.
	 * @param greeting
	 * @return
	 */
	public boolean delete(Greeting greeting) {
		SQLiteDatabase database = getWritableDatabase();
		if (database == null) {
			return false;
		}
		
		String greetingUid = greeting.getVoicemail().getSourceData();
		String query = getEqualityClause(LocalGreetingsProviderColumns.GREETING_MESSAGE_UID, greetingUid);
		return (database.delete(GREETINGS_TABLE_NAME, query, null) > 0);
	}

	/**
	 * Delete all greetings currently stored in the store.
	 */
	public int deleteAll() {
		logger.d("Removing all greetings in db.");
		SQLiteDatabase database = getWritableDatabase();
		if (database == null) {
			return 0;
		}
		
		return database.delete(GREETINGS_TABLE_NAME, null, new String[0]);
	}
	
	
	/**
	 * Get all Greetings from the db.
	 * @return List of Greetings
	 */
	public List<Greeting> getAllGreetings() {
		SQLiteDatabase database = null;
		List<Greeting> greetings = new ArrayList<Greeting>();
		try {
			database = mDatabaseHelper.getReadableDatabase();
		} catch (SQLiteException e) {
			logger.e(String.format("Impossible to open the OMTP Stack database: %s",
					e.getLocalizedMessage()));
			return greetings;
		}

		Cursor cursor = null;
		try {
			cursor = database.query(GREETINGS_TABLE_NAME, null, null, null, null, null, null);
			logger.d(String.format("Cursor returned rows number %d", cursor.getCount()));
			while (cursor.moveToNext()) {
				Greeting greeting = getGreetingFromCursor(cursor);
				greetings.add(greeting);
			}
		} finally {
			CloseUtils.closeQuietly(cursor);
		}

		return greetings;
	}
	
	/**
	 * Get a greeting based on its messageUid.
	 * @param greetingUri
	 * @return Greeting object
	 */
	@Nullable
	public Greeting getGreetingWithUid(String greetingUri) {
		String query = getEqualityClause(LocalGreetingsProviderColumns.GREETING_MESSAGE_UID, greetingUri);
		return getGreetingFromQuery(query);
	}
	
	/**
	 * Get {@link Greeting} using greeting type.
	 * @param greetingType
	 * @return Greeting object
	 */
	@Nullable
	public Greeting getGreetingByType(GreetingType greetingType) {
		String query = getEqualityClause(LocalGreetingsProviderColumns.GREETING_TYPE,
				greetingType.getTypeString());
		return getGreetingFromQuery(query);
	}
	
	/**
	 * Returns first returned from local db active greeting type.
	 * @return
	 */
	public GreetingType getActiveGreetingType() {
		GreetingType greetingType = GreetingType.UNKNOWN;
		String query = getEqualityClause(LocalGreetingsProviderColumns.IS_GREETING_ACTIVE,
				"1");
		Greeting greetingFromQuery = getGreetingFromQuery(query);
		if (greetingFromQuery != null) {
			greetingType = greetingFromQuery.getGreetingType();
		}
		return greetingType;
	}
	
	/**
	 * Get {@link Greeting} object from database query.
	 * @param query
	 * @return Greeting object
	 */
	private Greeting getGreetingFromQuery(String query) {
		logger.d(String.format("Retrieving greeting with query: %s", query));

		SQLiteDatabase database = null;
		try {
			database = mDatabaseHelper.getReadableDatabase();
		} catch (SQLiteException e) {
			logger.e(String.format("Impossible to open the OMTP Stack database: %s",
					e.getLocalizedMessage()));
			return null;
		}

		// Cursor that will contain the result.
		Greeting greeting = null;
		Cursor cursor = null;
		try {
			cursor = database.query(GREETINGS_TABLE_NAME, null, query, null, null, null, null);
			logger.d(String.format("Cursor returned rows number %d", cursor.getCount()));
			if (cursor.moveToFirst()) {
				greeting = getGreetingFromCursor(cursor);
				logger.d(String.format("Retrieved Greeting: %s", greeting.toString()));
			} else {
				// No greeting has been found.
				logger.i(String.format("No greeting has been found with the query: %s", query));
			}
		} finally {
			CloseUtils.closeQuietly(cursor);
		}
		
		return greeting;
	}
	
	
	/**
	 * Create {@link Greeting} object from cursor received from a database.
	 * @param cursor
	 * @return Greeting object
	 */
	private Greeting getGreetingFromCursor(Cursor cursor) {
		VoicemailImpl.Builder voicemailBuilder = VoicemailImpl.createEmptyBuilder().setSourceData(
				cursor.getString(cursor
						.getColumnIndexOrThrow(LocalGreetingsProviderColumns.GREETING_MESSAGE_UID
								.getColumnName())));
		Voicemail voicemail = voicemailBuilder.build();

		String greetingType = cursor
				.getString(cursor.getColumnIndexOrThrow(LocalGreetingsProviderColumns.GREETING_TYPE
						.getColumnName()));

		boolean isGreetingActive = cursor.getInt(cursor
				.getColumnIndexOrThrow(LocalGreetingsProviderColumns.IS_GREETING_ACTIVE
						.getColumnName())) > 0;

		boolean isContentDownloaded = cursor.getInt(cursor
				.getColumnIndexOrThrow(LocalGreetingsProviderColumns.IS_CONTENT_DOWNLOADED
						.getColumnName())) > 0;

		GreetingImpl.Builder greetingBuilder = GreetingImpl.createFromFetch(greetingType,
				voicemail, isGreetingActive, isContentDownloaded);
		return greetingBuilder.build();
	}

	/**
	 * Generate an SQLite equality clause using on the specified column, using
	 * the provided value.
	 */
	private String getEqualityClause(LocalGreetingsProviderColumns column, String value) {
		StringBuilder clause = new StringBuilder();
		clause.append("(");
		clause.append(GREETINGS_TABLE_NAME);
		clause.append(".");
		clause.append(column.getColumnName());
		clause.append(" = ");
		DatabaseUtils.appendEscapedSQLString(clause, value);
		clause.append(")");
		return clause.toString();
	}

	/**
	 * Delete Greetings List.
	 * @param greetings
	 * @return
	 */
	public boolean deleteList(List<Greeting> greetings) {
		logger.d(String.format("Removing %d greetings.", greetings.size()));
		SQLiteDatabase database = getWritableDatabase();
		if (database == null) {
			return false;
		}
		
		try {
			boolean status = true;
			database.beginTransaction();
			for (Greeting greeting : greetings) {
				status = status && delete(greeting, database);
			}
			
			if (status) {
				database.setTransactionSuccessful();
				return true;
			}
		} finally {
			database.endTransaction();
		}
		
		logger.w("Removing has failed.");
		return false;
	}
	
	/**
	 * Delete single message from Greetings database.
	 * @param greeting
	 * @param database
	 * @return
	 */
	private boolean delete(Greeting greeting, SQLiteDatabase database) {
		String greetingUid = greeting.getVoicemail().getSourceData();
		String query = getEqualityClause(LocalGreetingsProviderColumns.GREETING_MESSAGE_UID, greetingUid);
		return (database.delete(GREETINGS_TABLE_NAME, query, null) > 0);
	}

	/**
	 * Update Greetings List.
	 * @param greetings
	 * @return
	 */
	public boolean updateGreetings(List<Greeting> greetings) {
		logger.d(String.format("Inserting/Updating %d greetings", greetings.size()));
		SQLiteDatabase database = getWritableDatabase();
		if (database == null || greetings.size() == 0) {
			return false;
		}
		
		try {
			boolean status = true;
			database.beginTransaction();
			for (Greeting greeting : greetings) {
				status = status && insertGreeting(greeting, database);
			}
			
			if (status) {
				database.setTransactionSuccessful();
				return true;
			}
		} finally {
			database.endTransaction();
		}
		
		logger.w("Insertion/updating has failed...");
		return false;
	}

	
	/**
	 * Updates given Greeting downloaded state and sets it to true.
	 * 
	 * @param greeting
	 */
	public boolean setDownloadedStateTrue(Greeting greeting) {
		SQLiteDatabase database = getWritableDatabase();
		if (database == null) {
			return false;
		}
		ContentValues values = getContentValues(greeting);
		logger.d(String.format("Setting downloaded state true for Greeting %s", greeting));
		values.put(LocalGreetingsProviderColumns.IS_CONTENT_DOWNLOADED.getColumnName(), true);
		return (database.replace(GREETINGS_TABLE_NAME, null, values) != -1);
	}
	
	/**
	 * Updates given Greeting downloaded state and sets it to false.
	 * 
	 * @param greetingType
	 */
	public boolean setDownloadedStateFalse(GreetingType greetingType) {
		Greeting greeting = getGreetingByType(greetingType);
		if (greeting == null) {
			logger.d(String.format("Unable to set downloaded state for the greeting type",
					greetingType.getTypeString()));
			return false;
		}
		
		SQLiteDatabase database = getWritableDatabase();
		if (database == null) {
			return false;
		}
		ContentValues values = getContentValues(greeting);
		if (values.containsKey(LocalGreetingsProviderColumns.IS_CONTENT_DOWNLOADED.getColumnName())) {
			logger.d(String.format("Setting downloaded state false for Greeting %s", greeting));
			values.put(LocalGreetingsProviderColumns.IS_CONTENT_DOWNLOADED.getColumnName(), false);
		}
		return (database.replace(GREETINGS_TABLE_NAME, null, values) != -1);
	}
	
}
