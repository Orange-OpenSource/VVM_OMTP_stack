/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.orange.labs.uk.omtp.voicemail;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import android.net.Uri;

/**
 * Provides a simple interface to manipulate voicemails within the voicemail
 * content provider.
 * <p>
 * Methods on this interface throw checked exceptions only where the
 * corresponding underlying methods perform an operation that itself requires a
 * checked exception. In all other cases a {@link RuntimeException} will be
 * thrown here.
 * <p>
 * These methods are blocking, and will return control to the caller only when
 * the operation completes. You should not call any of these methods from your
 * main ui thread, as this may result in your application becoming unresponsive.
 */
public interface LocalVoicemailProvider {

	/** Sort order to return results by. */
	public enum SortOrder {
		ASCENDING, DESCENDING,
		/**
		 * Default sort order returned by DB. (Typically Ascending, but no
		 * guarantees made).
		 */
		DEFAULT
	}

	/**
	 * Clears all voicemails accessible to this voicemail content provider.
	 * 
	 * @return the number of voicemails deleted
	 */
	public int deleteAll();

	/**
	 * Inserts a new voicemail into the voicemail content provider.
	 * 
	 * @param voicemail
	 *            data to be inserted
	 * @return {@link Uri} of the newly inserted {@link Voicemail} or null if
	 *         the voicemail could not be inserted.
	 * @throws IllegalArgumentException
	 *             if any of the following are true:
	 *             <ul>
	 *             <li>your voicemail is missing a timestamp</li>
	 *             <li>your voiceamil is missing a number</li>
	 *             <li>your voicemail is missing the provider id field</li>
	 *             <li>voicemail has an id (which would indicate that it has
	 *             already been inserted)</li>
	 *             </ul>
	 */
	@Nullable
	public Uri insert(Voicemail voicemail);
	
	/**
	 * Insert a list of new voicemails into the voicemail content provider.
	 * 
	 * @param voicemails
	 *            voicemails to be inserted
	 * @return {@link List} of the newly inserted {@link Voicemail} uris
	 * @throws IllegalArgumentException
	 *             if any of the following are true:
	 *             <ul>
	 *             <li>your voicemail is missing a timestamp</li>
	 *             <li>your voiceamil is missing a number</li>
	 *             <li>your voicemail is missing the provider id field</li>
	 *             <li>voicemail has an id (which would indicate that it has
	 *             already been inserted)</li>
	 *             </ul>
	 */
	public List<Uri> insert(List<Voicemail> list);

	/**
	 * Returns the {@link Voicemail} whose provider data matches the given
	 * value.
	 * <p>
	 * It is expected that there be one such voicemail. Returns null if no such
	 * voicemail exists, and returns one chosen arbitrarily if more than one
	 * exists.
	 */
	public Voicemail findVoicemailBySourceData(String providerData);

	/**
	 * Returns the {@link Voicemail} corresponding to a given Uri. The uri must
	 * correspond to a unique voicemail record.
	 * <p>
	 * Returns null if no voicemail was found that exactly matched the given
	 * uri.
	 */
	public Voicemail findVoicemailByUri(Uri uri);

	/**
	 * Updates an existing voicemail in the content provider.
	 * <p>
	 * Note that <b>only the fields that are set</b> on the {@link Voicemail}
	 * that you provide will be used to perform the update. The remaining fields
	 * will be left unmodified. To mark a voicemail as read, create a new
	 * {@link Voicemail} that is marked as read, and call update.
	 * 
	 * @throws IllegalArgumentException
	 *             if you provide a {@link Voicemail} that already has a Uri
	 *             set, because we don't support altering the Uri of a
	 *             voicemail, and this most likely implies that you're using
	 *             this api incorrectly
	 * @return the number of rows that were updated
	 */
	public int update(Uri uri, Voicemail voicemail);

	/**
	 * Updates multiple existing voicemails in the content provider.
	 * <p>
	 * Note that <b>only the fields that are set</b> on the {@link Voicemail}
	 * that you provide will be used to perform the update. The remaining fields
	 * will be left unmodified. To mark a voicemail as read, create a new
	 * {@link Voicemail} that is marked as read, and call update.
	 * 
	 * @param voicemails
	 *            A map associating voicemail URI and the fields to update as
	 *            explained above.
	 * @throws IllegalArgumentException
	 *             if an empty map of voicemails is provided or if you provide a
	 *             {@link Voicemail} that already has a Uri set, because we
	 *             don't support altering the Uri of a voicemail, and this most
	 *             likely implies that you're using this api incorrectly
	 * @return the number of rows that were updated.
	 */
	public int update(Map<Uri, Voicemail> voicemails);
	
	/**
	 * Delete the voicemail pointed by the provided {@link Uri} from the Content Provider.
	 * @param voicemailUri
	 * 			{@link Uri} that points the record that should be deleted.
	 * @return the number of deleted rows.
	 */
	public int delete(Uri voicemailUri);
	
	/**
	 * Deletes multiple existing voicemails in the content provider. These voicemails are pointed by
	 * their URIs.
	 * @param messagesUris
	 * 				List of {@link Uri} of voicemail to delete.
	 * @throw {@link IllegalArgumentException}
	 * 				if you provide an empty list of Uris.
	 * 
	 * @return the number of rows that were deleted.
	 */
	public int delete(List<Uri> messagesUris);

	/**
	 * Sets the voicemail content from the supplied input stream.
	 * <p>
	 * The inputStream is owned by the caller and must be closed by it as usual
	 * after the call has returned.
	 * 
	 * @throws IOException
	 *             if there is a problem creating the file or no voicemail is
	 *             found matching the given Uri
	 */
	public void setVoicemailContent(Uri voicemailUri, InputStream inputStream, String mimeType)
			throws IOException;

	/**
	 * Sets the voicemail content from the supplied byte array.
	 * 
	 * @throws IOException
	 *             if there is a problem creating the file or no voicemail is
	 *             found matching the given Uri
	 */
	public void setVoicemailContent(Uri voicemailUri, byte[] inputBytes, String mimeType)
			throws IOException;

	/**
	 * Fetch all the voicemails accessible to this voicemail content provider.
	 * 
	 * @return a {@link List} of {@link Voicemail} present in the content provider.
	 */
	public List<Voicemail> getAllVoicemails();

	/**
	 * Same as {@link #getAllVoicemails()} but also sorts them by the requested
	 * column and allows to set a filter.
	 * 
	 * @param filter
	 *            The filter to apply while retrieving voicemails.
	 * @param sortColumn
	 *            The column to sort by. Must be one of the values defined in
	 *            {@link android.provider.VoicemailContract.Voicemails}.
	 * @param sortOrder
	 *            Order to sort by
	 * @return the list of voicemails, sorted by the requested DB column in
	 *         specified sort order.
	 */
	public List<Voicemail> getAllVoicemails(VoicemailFilter filter, String sortColumn,
			SortOrder sortOrder);

	/**
	 * Returns the Uri for the voicemail with the specified message Id.
	 */
	public Uri getUriForVoicemailWithId(long id);

	/**
	 * Find the newest message based on its provider ID.
	 * 
	 * @return The latest stored {@link Voicemail}.
	 */
	public Voicemail getLatestVoicemail();
}
