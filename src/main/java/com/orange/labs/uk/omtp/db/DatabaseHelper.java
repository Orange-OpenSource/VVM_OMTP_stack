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
package com.orange.labs.uk.omtp.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.orange.labs.uk.omtp.account.OmtpAccountColumns;
import com.orange.labs.uk.omtp.account.OmtpAccountDatabase;
import com.orange.labs.uk.omtp.greetings.database.LocalGreetingsProvider;
import com.orange.labs.uk.omtp.greetings.database.LocalGreetingsProviderColumns;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.provider.OmtpProviderColumns;
import com.orange.labs.uk.omtp.provider.OmtpProviderDatabase;
import com.orange.labs.uk.omtp.utils.CloseUtils;
import com.orange.labs.uk.omtp.voicemail.database.MirrorVoicemailProvider;
import com.orange.labs.uk.omtp.voicemail.database.MirrorVoicemailProviderColumns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Helper class to create a database helper for a set of columns.
 * <p>
 * This class simply wraps several {@link TableCreator} instances for each of the table of the
 * application. It also contains the global properties of the database such as its name, version
 * and principle tables.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static Logger logger = Logger.getLogger(DatabaseHelper.class);
	
    private static final String DB_NAME = "omtpstack.db";
    public static final int DB_VERSION = 4;
    private static final HashMap<String, DatabaseColumn[]> DB_COLUMNS = 
    		new HashMap<String, DatabaseColumn[]>();
    		
    static {
    	DB_COLUMNS.put(OmtpProviderDatabase.PROVIDERS_TABLE_NAME, OmtpProviderColumns.values());
    	DB_COLUMNS.put(OmtpAccountDatabase.ACCOUNT_TABLE_NAME, OmtpAccountColumns.values());
    	DB_COLUMNS.put(MirrorVoicemailProvider.VOICEMAIL_TABLE_NAME, MirrorVoicemailProviderColumns.values());
    	DB_COLUMNS.put(LocalGreetingsProvider.GREETINGS_TABLE_NAME, LocalGreetingsProviderColumns.values());
    }
    		
	
    /** The version of the database to create. */
    private final int mVersion;
    /** A helper object to create the table. */
    private ArrayList<TableCreator> mTableCreators;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

        mVersion = DB_VERSION;
        mTableCreators = new ArrayList<TableCreator>();

        generateTableCreators();
    }

    /**
     * Generates {@link TableCreator} objects for different tables.
     */
	private void generateTableCreators() {
		for (String tableName : DB_COLUMNS.keySet()) {
			logger.d(String.format("creating TableCreator for table:%s", tableName));
			mTableCreators.add(new TableCreator(tableName, DB_COLUMNS.get(tableName)));
		}
	}

    @Override
    public void onCreate(final SQLiteDatabase db) {
        logger.d("onCreate() on db called");
        for (TableCreator tableCreator : mTableCreators) {
            if (!(tableCreator.getTableName().equals(OmtpProviderDatabase.PROVIDERS_TABLE_NAME) ||
                    tableCreator.getTableName().equals(OmtpAccountDatabase.ACCOUNT_TABLE_NAME))){
                logger.d(String.format("Creating table %s.", tableCreator.getTableName()));
                db.execSQL(tableCreator.getCreateTableQuery(mVersion));
            }
        }
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        logger.d(String.format("onUpgrage() on db called with oldVersion=%d, newVersion=%d",
                oldVersion, newVersion));
        List<String> upgradeTableQueryList = new ArrayList<String>();
        for (TableCreator tableCreator : mTableCreators) {
            logger.d(String.format("processing TableCreator for table:%s", tableCreator.getTableName()));
            // check if table already exists in the db
            if (!tableAlreadyExists(db, tableCreator)) {
                // try to create new tables for this version, and add it on the top of the list
                upgradeTableQueryList.add(0, tableCreator.getCreateTableQuery(newVersion));
            } else {
                // get SQL queries for existing tables (to add new columns)
                upgradeTableQueryList.addAll(tableCreator.getUpgradeTableQuery(oldVersion,
                        newVersion));
            }
        }
        // execute queries
        for (String upgradeTableQuery : upgradeTableQueryList) {
            logger.d(String.format("Executing db update with query:%s", upgradeTableQuery));
            db.execSQL(upgradeTableQuery);
        }
	}
    
    /**
     * Checks the existence of a table and creates it if it has not been already created.
     * @param table name
     */
	public void checkIfTableExists(String tableName) {
		TableCreator tableCreator = new TableCreator(tableName, DB_COLUMNS.get(tableName));
		
		SQLiteDatabase db = getWritableDatabase();
		if (!tableAlreadyExists(db, tableCreator)) {
			logger.d(String.format("%s table didn't exist, creating it now", tableName));
			db.execSQL(tableCreator.getCreateTableQuery(mVersion));
		}
	}
    
    
    private boolean tableAlreadyExists(SQLiteDatabase db, TableCreator tableCreator) {
    	String tableExistsCheckQuery = tableCreator.getTableExistsCheckQuery();
    	Cursor cursor = null;
    	boolean tableAlreadyExists = false;
    	try {
    		cursor = db.rawQuery(tableExistsCheckQuery, new String[] {tableCreator.getTableName()});
    		tableAlreadyExists = cursor.moveToFirst();
    	}
    	catch (Exception e) {
    		logger.e("Exception while checking if a table already exists", e);
		} finally {
			CloseUtils.closeQuietly(cursor);
		}
    	
    	return tableAlreadyExists;
	}

	@Override
    public synchronized SQLiteDatabase getWritableDatabase() {
    	SQLiteDatabase db =  super.getWritableDatabase();
    	if (db != null) {
            db.execSQL("PRAGMA foreign_keys = ON;");
    	}
    	return db;
    }
}
