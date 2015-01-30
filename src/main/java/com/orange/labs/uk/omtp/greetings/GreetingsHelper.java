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
package com.orange.labs.uk.omtp.greetings;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nullable;

import android.content.Context;

import com.orange.labs.uk.omtp.dependency.StackDependencyResolver;
import com.orange.labs.uk.omtp.dependency.StackDependencyResolverImpl;
import com.orange.labs.uk.omtp.greetings.database.LocalGreetingsProvider;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.notification.GreetingsErrorNotifications;
import com.orange.labs.uk.omtp.notification.GreetingsStatusUpdate;
import com.orange.labs.uk.omtp.notification.SourceNotifier;

/**
 * Utility methods used to retrieve names of the greeting files. Used to store
 * and synchronise greetings.
 */
public final class GreetingsHelper {
	
	private static final Logger logger = Logger.getLogger(GreetingsHelper.class);
	private static StackDependencyResolver resolver = StackDependencyResolverImpl.getInstance();
	private static SourceNotifier mSourceNotifier = resolver.getSourceNotifier();
	private static final  byte[] EMPTY_BYTE_ARRAY = new byte[0];
	private static volatile GreetingType mCurrentActiveGreeting = null;
	private final Context mApplicationContext;
	private final LocalGreetingsProvider mLocalGreetingsProvider;
	
	/**
	 * Directory used to store greetings
	 */
	public static final String GREETINGS_DIRECTORY = "VoicemailGreetings"; 
	
	
	/**
	 * Private constructor of greeting types.
	 * @param appContext 
	 * @param localGreetingsProvider 
	 */
	private GreetingsHelper(Context appContext, LocalGreetingsProvider localGreetingsProvider) {
		mApplicationContext = appContext;
		mLocalGreetingsProvider = localGreetingsProvider;
	}
	
	public static GreetingsHelper newInstance(Context appContext,
			LocalGreetingsProvider localGreetingsProvider) {
		return new GreetingsHelper(appContext, localGreetingsProvider);
	}

	/**
	 * Saves greetings bytes to a dedicated greetings file.
	 * 
	 * @param bytes
	 *            greeting bytes content
	 * @param string
	 * @param grretingType
	 *            type of the greeting (normal-greeting or voice-signature for OMTP v1.1)
	 * 
	 * @return true if everything went well
	 */
	public boolean updateGreetingsFile(byte[] bytes, GreetingType greetingType) {

		// get an appropriate file name depending on greeting type
		// in this version we support only normal-greeting & voice-signature
		String greetingFileName = getFilePath(greetingType);
		if (greetingFileName == null) {
			logger.e("Cannot save greeting file, because it has not been possible to access greeting file.");
			return false;
		}

		// check if we have received any data
		if (bytes == null) {
			logger.e("Cannot save greeting file, because no information has been received.");
			return false;
		}

		// try to save it to a file
		File file = new File(greetingFileName);
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file));
			try {
				out.write(bytes);
				out.close();
				logger.d(String.format("Greeting saved to %s", greetingFileName));
				return true;
			} finally {
				// no need to check for null
				// any exceptions thrown here will be caught by
				// the outer catch block
				out.close();
			}
		} catch (FileNotFoundException e) {
			logger.e("Saving greeting file failed!, File not found");
			return false;
		} catch (IOException e) {
			logger.e("Saving greeting file failed!");
			return false;
		}
	}

	
	/**
	 * Get InputStream representing the greeting.
	 * @param greetingType
	 * @return InputStream with the greeting content.
	 */
	@Nullable
	public InputStream getGreetingsFileInputStream(GreetingType greetingType) {
		String greetingFilePath = getFilePath(greetingType);
		if (greetingFilePath == null) {
			logger.e("Cannot read greeting file, it has not been possible to access greeting file.");
			return null;
		}

		File file = new File(greetingFilePath);
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			logger.e("Getting greeting file failed!, File not found");
			// If we cannot read the file we just return an empty stream.
			in = new ByteArrayInputStream(EMPTY_BYTE_ARRAY);
		}

		return in;

	}

	/**
	 * Gets current greeting type received after connection to IMAP server.
	 * @return GreetingType
	 */
	public GreetingType getCurrentActiveGreeting() {
		if (mCurrentActiveGreeting != null) {
			logger.d(String.format("Current greeting type:%s", mCurrentActiveGreeting.getTypeString()));
			return mCurrentActiveGreeting;
		} else {
			GreetingType activeGreetingTypeFromDb = mLocalGreetingsProvider.getActiveGreetingType();
			if (activeGreetingTypeFromDb != null) {
				logger.d(String.format(
						"Current greeting has been retrieved from local db, active type:%s",
						activeGreetingTypeFromDb));
				mCurrentActiveGreeting = activeGreetingTypeFromDb;
				return activeGreetingTypeFromDb;
			} else {
				logger.d("Current greeting is null! It will be set to UNKNOWN.");
				return GreetingType.UNKNOWN;
			}
		}
	}
	
	/**
	 * Sets current greeting type.
	 * @param type
	 */
	public void setCurrentActiveGreeting(GreetingType type, GreetingUpdateType updateType) {
		logger.d(String.format("Current greeting type set to:%s", type.getTypeString()));
		mCurrentActiveGreeting = type;
		notifySourceAboutGreetingsUpdate(updateType);
	}
	
	/**
	 * Creates and sends Intent notification, inviting OMTP source application to fetch 
	 * updates current active greeting status.
	 * @param updateType 
	 */
	public void notifySourceAboutGreetingsUpdate(GreetingUpdateType updateType) {
		mSourceNotifier.sendNotification(GreetingsStatusUpdate.reportNewStatus(updateType).build());
	}

	/**
	 * Returns path to a given greetings file or creates given file if it does
	 * not exist.
	 * 
	 * @param greetingFileName
	 * @return file path or null if an error occurred
	 */
	@Nullable
	private String getGreetingFilePath(String greetingFileName) {

		// get greetings directory
		String greetingsDir = getGreetingsDir();
		if (greetingsDir == null) {
			return null;
		}
		// continue with files creation
		String testFileName = getGreetingsDir() + File.separator + greetingFileName;
		File testFile = new File(testFileName);
		if (testFile.exists()) {
			return testFileName;
		} else {
			try {
				if (testFile.createNewFile()) {
					return testFileName;
				} else {
					logger.e(String.format("Problem creating new greeting file %s",
							testFile.getAbsolutePath()));
					mSourceNotifier.sendNotification(GreetingsErrorNotifications.filesAccessError()
							.build());
					return null;
				}

			} catch (IOException e) {
				logger.e(String.format("IOException while creating a greeting file %s",
						testFile.getAbsolutePath()));
				mSourceNotifier.sendNotification(GreetingsErrorNotifications.filesAccessError().build());
				return null;
			}
		}
	}
	
	/**
	 * Returns a Voicemal Greetings storage directory path in internal storage or creates
	 * it if it does not exist.
	 * 
	 * @returns filesPath greetings directory path, or just system directory if
	 *          it has not been possible to create a dedicated directory
	 */
	@Nullable
	private String getGreetingsDir() {
		String filesPath = mApplicationContext.getFilesDir().getPath()
				+ File.separator + GREETINGS_DIRECTORY;
		File testDir = new File(filesPath);
		if (!testDir.exists()) {
			if (!testDir.mkdirs()) {
				logger.e(String.format("Unable to create greetings directory!", filesPath));
				return null;
			}
		}
		return filesPath;
	}
	
	/**
	 * Returns path to the greeting file.
	 * @param type
	 * @return
	 */
	public String getFilePath(GreetingType type) {
		return getGreetingFilePath(type.getFileName());
	}
	
	/**
	 * Creates and send error notification to source app.
	 * @param mGreetingToActivate 
	 */
	public void createAndSendGreetingErrorNotification(GreetingUpdateType updateType,
			GreetingType greetingToActivate) {
		mSourceNotifier.sendNotification(GreetingsErrorNotifications.uploadError(
				updateType, greetingToActivate).build());
	}
	
	/**
	 * Gets greeting file size.
	 * @param greetingType
	 * @return length of the file or 0 if impossible to access it
	 */
	public long getGreetingFileSize(GreetingType greetingType) {
		String greetingFilePath = getFilePath(greetingType);
		if (greetingFilePath == null) {
			logger.e("Cannot read greeting file, it has not been possible to access greeting file.");
			return 0;
		}

		File file = new File(greetingFilePath);
		return file.length();
	}

	/**
	 * Delete greetings files content.
	 * 
	 * @return true if everything goes well
	 */
	public boolean deleteAllGreetingFiles() {
		boolean result = false;
		logger.d("Deleting all greetings files");
		result = updateGreetingsFile(EMPTY_BYTE_ARRAY, GreetingType.NORMAL)
				&& updateGreetingsFile(EMPTY_BYTE_ARRAY, GreetingType.VOICE_SIGNATURE)
				&& updateGreetingsFile(EMPTY_BYTE_ARRAY, GreetingType.UNKNOWN);
		return result;
	}

	/**
	 * Deletes given Greeting file
	 * @param greetingType type of the Greeting
	 * @return true if everything goes well
	 */
	public boolean deleteGreetingFile(GreetingType greetingType) {
		logger.d(String.format("Deleting greetings files type %s", greetingType.getTypeString()));
		return updateGreetingsFile(EMPTY_BYTE_ARRAY, greetingType);
	}
	
}
