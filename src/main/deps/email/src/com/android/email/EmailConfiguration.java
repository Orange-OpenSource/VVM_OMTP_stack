/*
 * Copyright (C) 2008 The Android Open Source Project
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
 * limitations under the License.
 */

package com.android.email;

import android.content.Context;

import java.io.File;

public class EmailConfiguration {
    public static final String LOG_TAG = "Email";

    /**
     * Specifies how many messages will be shown in a folder by default. This number is set
     * on each new folder and can be incremented with "Load more messages..." by the
     * VISIBLE_LIMIT_INCREMENT
     */
    public static final int VISIBLE_LIMIT_DEFAULT = 25;

    /**
     * Number of additional messages to load when a user selects "Load more messages..."
     */
    public static final int VISIBLE_LIMIT_INCREMENT = 25;

    private static File sTempDirectory;

    public static void setTempDirectory(Context context) {
        sTempDirectory = context.getCacheDir();
    }

    public static File getTempDirectory() {
        if (sTempDirectory == null) {
            throw new RuntimeException(
                    "TempDirectory not set.  " +
                    "If in a unit test, call Email.setTempDirectory(context) in setUp().");
        }
        return sTempDirectory;
    }
}
