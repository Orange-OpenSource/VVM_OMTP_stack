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

package com.android.email.mail;

/**
 * Store is the access point for an email message store. It's location can be
 * local or remote and no specific protocol is defined. Store is intended to
 * loosely model in combination the JavaMail classes javax.mail.Store and
 * javax.mail.Folder along with some additional functionality to improve
 * performance on mobile devices. Implementations of this class should focus on
 * making as few network connections as possible.
 */
public abstract class Store {
    
    /**
     * String constants for known store schemes.
     */
    public static final String STORE_SCHEME_IMAP = "imap";

    public static final String STORE_SECURITY_SSL = "+ssl";
    public static final String STORE_SECURITY_TLS = "+tls";
    public static final String STORE_SECURITY_TRUST_CERTIFICATES = "+trustallcerts";

    /**
     * A global suggestion to Store implementors on how much of the body
     * should be returned on FetchProfile.Item.BODY_SANE requests.
     */
    public static final int FETCH_BODY_SANE_SUGGESTED_SIZE = (50 * 1024);

    /**
     * Some stores cannot download a message based only on the uid, and need the message structure
     * to be preloaded and provided to them.  This method allows a remote store to signal this
     * requirement.  Most stores do not need this and do not need to overload this method, which
     * simply returns "false" in the base class.
     * @return Return true if the remote store requires structure prefetch
     */
    public boolean requireStructurePrefetch() {
        return false;
    }
    
    /**
     * Some protocols require that a sent message be copied (uploaded) into the Sent folder
     * while others can take care of it automatically (ideally, on the server).  This function
     * allows a given store to indicate which mode(s) it supports.
     * @return true if the store requires an upload into "sent", false if this happens automatically
     * for any sent message.
     */
    public boolean requireCopyMessageToSentFolder() {
        return true;
    }
    
    public abstract Folder getFolder(String name) throws MessagingException;

    public abstract Folder[] getPersonalNamespaces() throws MessagingException;
    
    public abstract void checkSettings() throws MessagingException;
    
    /**
     * Delete Store and its corresponding resources.
     * @throws MessagingException
     */
    public void delete() throws MessagingException {
    }
    
    /**
     * If a Store intends to implement callbacks, it should be prepared to update them
     * via overriding this method.  They may not be available at creation time (in which case they
     * will be passed in as null.
     * @param callbacks The updated provider of store callbacks
     */
    protected void setPersistentDataCallbacks(PersistentDataCallbacks callbacks) {
    }
    
    /**
     * Callback interface by which a Store can read and write persistent data.
     * TODO This needs to be made more generic & flexible
     */
    public interface PersistentDataCallbacks {
        
        /**
         * Provides a small place for Stores to store persistent data.
         * @param key identifier for the data (e.g. "sync.key" or "folder.id")
         * @param value The data to persist.  All data must be encoded into a string,
         * so use base64 or some other encoding if necessary.
         */
        public void setPersistentString(String key, String value);

        /**
         * @param key identifier for the data (e.g. "sync.key" or "folder.id")
         * @param defaultValue The data to return if no data was ever saved for this store
         * @return the data saved by the Store, or null if never set.
         */
        public String getPersistentString(String key, String defaultValue);
    }
}
