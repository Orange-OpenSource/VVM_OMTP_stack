package com.orange.labs.uk.omtp.voicemail.database;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;

import com.orange.labs.uk.omtp.db.DatabaseHelper;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.utils.CloseUtils;
import com.orange.labs.uk.omtp.voicemail.Voicemail;
import com.orange.labs.uk.omtp.voicemail.VoicemailImpl;

/**
 * Database storing OMTP voicemails synchronized by the stack. See
 * {@link MirrorVoicemailProviderColumns} for an explanation about why it is required.
 */
public class MirrorVoicemailProvider {
	private static Logger logger = Logger.getLogger(MirrorVoicemailProvider.class);

	public static final String VOICEMAIL_TABLE_NAME = "voicemails";

	private DatabaseHelper mDatabaseHelper;

	public MirrorVoicemailProvider(DatabaseHelper dbHelper) {
		mDatabaseHelper = dbHelper;
	}

	public boolean updateVoicemail(final Voicemail voicemail) {
		logger.d(String.format("Inserting/Updating the following voicemail: %s",
				voicemail.toString()));

		SQLiteDatabase database = getWritableDatabase();
		if (database == null || voicemail.getSourceData() == null) {
			return false;
		}

		return insertVoicemail(voicemail, database);
	}

	private boolean insertVoicemail(final Voicemail voicemail, final SQLiteDatabase database) {
		ContentValues values = getContentValues(voicemail);
		return (database.replace(VOICEMAIL_TABLE_NAME, null, values) != -1);
	}
	
	/**
	 * Insert or replace a list of {@link Voicemail} in the database using a SQL Transaction.
	 * @param voicemails
	 * 			List of voicemails to insert or replace.
	 * @return a boolean that indicates if the transaction has been successful.
	 */
	public boolean updateVoicemails(List<Voicemail> voicemails) {
		logger.d(String.format("Inserting/Updating %d voicemails", voicemails.size()));
		SQLiteDatabase database = getWritableDatabase();
		if (database == null || voicemails.size() == 0) {
			return false;
		}
		
		try {
			boolean status = true;
			database.beginTransaction();
			for (Voicemail voicemail : voicemails) {
				status = status && insertVoicemail(voicemail, database);
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
	 * Mark the provided {@link Voicemail} as read in the local voicemail database.
	 */
	public boolean markAsRead(Voicemail message) {
		logger.d(String.format("Marking voicemail as read: %s", message.getSourceData()));
		Voicemail readVoicemail = VoicemailImpl.createCopyBuilder(message).setIsRead(true).build();
		return updateVoicemail(readVoicemail);
	}
	
	/**
	 * Mark the provided {@link List} of {@link Voicemail} as read in the local voicemail database.
	 */
	public boolean markAsRead(List<Voicemail> messages) {
		logger.d(String.format("Marking %d voicemails as read.", messages.size()));
		List<Voicemail> readMessages = new ArrayList<Voicemail>(messages.size());
		for (Voicemail message : messages) {
			readMessages.add(VoicemailImpl.createCopyBuilder(message).setIsRead(true).build());
		}
		return updateVoicemails(readMessages);
	}
	
	public List<Voicemail> getAllVoicemails() {
		SQLiteDatabase database = null;
		List<Voicemail> voicemails = new ArrayList<Voicemail>();
		try {
			database = mDatabaseHelper.getReadableDatabase();
		} catch (SQLiteException e) {
			logger.e(String.format("Impossible to open the OMTP Stack database: %s",
					e.getLocalizedMessage()));
			return voicemails;
		}
		
		Cursor cursor = null;
		try {
			cursor = database.query(VOICEMAIL_TABLE_NAME, null, null, null, null, null, null);
			logger.d(String.format("Cursor returned rows number %d", cursor.getCount()));
			while (cursor.moveToNext()) {
				Voicemail voicemail = getVoicemailFromCursor(cursor);
				voicemails.add(voicemail);
			}
		} finally {
			CloseUtils.closeQuietly(cursor);
		}

		return voicemails;
	}

	@Nullable
	public Voicemail findVoicemailBySourceData(String uid) {
		String query = getEqualityClause(MirrorVoicemailProviderColumns.MESSAGE_UID, uid);
		return getVoicemailFromQuery(query);
	}

	@Nullable
	public Voicemail getVoicemailWithUri(String voicemailUri) {
		String query = getEqualityClause(MirrorVoicemailProviderColumns.MESSAGE_URI, voicemailUri);
		return getVoicemailFromQuery(query);
	}

	/**
	 * Build a {@link Voicemail} from the result {@link Cursor} of the execution of the provided
	 * SQL query. 
	 * @param query
	 * 			Query to execute against the database.
	 * @return
	 * 			Corresponding Voicemail record if it exists.
	 */
	private Voicemail getVoicemailFromQuery(String query) {
		logger.d(String.format("Retrieving voicemail with query: %s", query));

		SQLiteDatabase database = null;
		try {
			database = mDatabaseHelper.getReadableDatabase();
		} catch (SQLiteException e) {
			logger.e(String.format("Impossible to open the OMTP Stack database: %s",
					e.getLocalizedMessage()));
			return null;
		}

		// Cursor that will contain the result.
		Voicemail voicemail = null;
		Cursor cursor = null;
		try {
			cursor = database.query(VOICEMAIL_TABLE_NAME, null, query, null, null, null, null);
			logger.d(String.format("Cursor returned rows number %d", cursor.getCount()));
			if (cursor.moveToFirst()) {
				voicemail = getVoicemailFromCursor(cursor);
				logger.d(String.format("Retrieved Voicemail: %s", voicemail.toString()));
			} else {
				// No voicemail has been found.
				logger.i(String.format("No voicemail has been found with the query: %s", query));
			}
		} finally {
			CloseUtils.closeQuietly(cursor);
		}
		
		return voicemail;
	}

	public boolean delete(Voicemail voicemail) {
		logger.d(String.format("Removing voicemail from local DB: %s", voicemail.toString()));

		SQLiteDatabase database = getWritableDatabase();
		if (database == null) {
			return false;
		}

		return delete(voicemail, database);
	}

	/**
	 * Remove the provided {@link Voicemail} from the {@link SQLiteDatabase}.
	 * @param voicemail
	 * 				Voicemail which record should be deleted.
	 * @param database
	 * 				Database to delete from.
	 * @return a boolean that indicaites if the operation was successful.
	 */
	private boolean delete(Voicemail voicemail, SQLiteDatabase database) {
		String voicemailUid = voicemail.getSourceData();
		String query = getEqualityClause(MirrorVoicemailProviderColumns.MESSAGE_UID, voicemailUid);
		return (database.delete(VOICEMAIL_TABLE_NAME, query, null) > 0);
	}
	
	/**
	 * Removes a {@link List} of {@link Voicemail} from the database.
	 * @return a boolean indicating if the transaction has been successful.
	 */
	public boolean deleteList(List<Voicemail> messages) {
		logger.d(String.format("Removing %d voicemails.", messages.size()));
		SQLiteDatabase database = getWritableDatabase();
		if (database == null) {
			return false;
		}
		
		try {
			boolean status = true;
			database.beginTransaction();
			for (Voicemail voicemail : messages) {
				status = status && delete(voicemail, database);
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
	 * Delete all voicemails currently stored in the store.
	 */
	public int deleteAll() {
		logger.d("Removing all voicemails.");
		SQLiteDatabase database = getWritableDatabase();
		if (database == null) {
			return 0;
		}
		
		return database.delete(VOICEMAIL_TABLE_NAME, null, new String[0]);
	}

	/**
	 * Generate a {@link ContentValues} object from the provided
	 * {@link Voicemail}.
	 */
	private ContentValues getContentValues(Voicemail voicemail) {
		ContentValues cv = new ContentValues();
		cv.put(MirrorVoicemailProviderColumns.MESSAGE_UID.getColumnName(), voicemail.getSourceData());
		if (voicemail.hasUri()) {
			cv.put(MirrorVoicemailProviderColumns.MESSAGE_URI.getColumnName(), voicemail.getUri().toString());
		}
		if (voicemail.hasRead()) {
			cv.put(MirrorVoicemailProviderColumns.READ.getColumnName(), voicemail.isRead() ? 1 : 0);
		}
		return cv;
	}

	/**
	 * Build a {@link Voicemail} object from the specified {@link Cursor}
	 */
	private Voicemail getVoicemailFromCursor(Cursor cursor) {
		VoicemailImpl.Builder voicemailBuilder = VoicemailImpl
				.createEmptyBuilder()
				.setSourceData(
						cursor.getString(cursor
								.getColumnIndexOrThrow(MirrorVoicemailProviderColumns.MESSAGE_UID
										.getColumnName())))
				.setIsRead(
						cursor.getInt(cursor.getColumnIndexOrThrow(MirrorVoicemailProviderColumns.READ
								.getColumnName())) == 1);
		
		// check if the given voiecmail has an Uri, we insert it if so
		String uriString = cursor.getString(cursor
				.getColumnIndexOrThrow(MirrorVoicemailProviderColumns.MESSAGE_URI.getColumnName()));
		if (uriString != null) {
			logger.d(String.format("Retrieved Voicemail has an URI: %s", uriString));
			voicemailBuilder.setUri(Uri.parse(uriString));
		}
		
		Voicemail voicemail = voicemailBuilder.build();
		return voicemail;
	}

	/**
	 * Generate an SQLite equality clause using on the specified column, using
	 * the provided value.
	 */
	private String getEqualityClause(MirrorVoicemailProviderColumns column, String value) {
		StringBuilder clause = new StringBuilder();
		clause.append("(");
		clause.append(VOICEMAIL_TABLE_NAME);
		clause.append(".");
		clause.append(column.getColumnName());
		clause.append(" = ");
		DatabaseUtils.appendEscapedSQLString(clause, value);
		clause.append(")");
		return clause.toString();
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

}
