/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.orange.labs.uk.omtp.db;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class to create and upgrade an SQLite table.
 */
public class TableCreator {
    private final String mName;
    private final DatabaseColumn[] mColumns;

    public TableCreator(String name, DatabaseColumn[] columns) {
        mName = name;
        mColumns = columns;
    }

    public String getCreateTableQuery(int version) {
        return String.format("CREATE TABLE %s (%s);", mName, getColumns(version));
    }
    
    public String getTableExistsCheckQuery() {
    	return "SELECT name FROM sqlite_master WHERE type='table' AND name= ?";
    }
    
    public List<String> getUpgradeTableQuery(int oldVersion, int newVersion) {
    	List<String> commandsSet = new ArrayList<String>();
        for (DatabaseColumn column : mColumns) {
            int sinceVersion = column.getSinceVersion();
            if (sinceVersion > oldVersion && sinceVersion <= newVersion) {
            	StringBuilder builder = new StringBuilder();
                builder.append("ALTER TABLE ");
                builder.append(mName);
                builder.append(" ADD COLUMN ");
                builder.append(column.getColumnName());
                builder.append(" ");
                builder.append(column.getColumnType());
                builder.append(";");
                commandsSet.add(builder.toString());
            }
        }
        return commandsSet;
    }
    
    public String getTableName() {
    	return mName;
    }

    private String getColumns(int version) {
        StringBuilder builder = new StringBuilder();
        for (DatabaseColumn column : mColumns) {
            if (column.getSinceVersion() <= version) {
                if (builder.length() != 0) {
                    builder.append(", ");
                }
                builder.append(column.getColumnName());
                builder.append(" ");
                builder.append(column.getColumnType());
            }
        }
        return builder.toString();
    }

}
