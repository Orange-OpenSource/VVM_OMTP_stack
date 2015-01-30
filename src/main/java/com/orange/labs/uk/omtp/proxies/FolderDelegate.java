/*
 * Copyright (C) 2011 The Android Open Source Project Inc. All Rights Reserved.
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
package com.orange.labs.uk.omtp.proxies;

import com.android.email.mail.FetchProfile;
import com.android.email.mail.Flag;
import com.android.email.mail.Folder;
import com.android.email.mail.Message;
import com.android.email.mail.MessagingException;

/** This class was auto-generated using ProxyGen (http://goto/proxygen). */
public class FolderDelegate implements FolderProxy {
    private final Folder mDelegate;

    public FolderDelegate(Folder delegate) {
        mDelegate = delegate;
    }

    @Override
    public String toString() {
        return mDelegate.toString();
    }

    @Override
    public String getName() {
        return mDelegate.getName();
    }

    @Override
    public Message getMessage(String a) throws MessagingException {
        return mDelegate.getMessage(a);
    }

    @Override
    public void close(boolean a) throws MessagingException {
        mDelegate.close(a);
    }
    
    @Override
    public void close(boolean a, boolean b) throws MessagingException {
    	mDelegate.close(a,b);
    }

    @Override
    public void delete(boolean a) throws MessagingException {
        mDelegate.delete(a);
    }

    @Override
    public boolean create(Folder.FolderType a) throws MessagingException {
        return mDelegate.create(a);
    }

    @Override
    public void open(Folder.OpenMode a, Folder.PersistentDataCallbacks b) throws MessagingException {
        mDelegate.open(a, b);
    }

    @Override
    public boolean isOpen() {
        return mDelegate.isOpen();
    }

    @Override
    public boolean exists() throws MessagingException {
        return mDelegate.exists();
    }

    @Override
    public Folder.OpenMode getMode() throws MessagingException {
        return mDelegate.getMode();
    }

    @Override
    public boolean canCreate(Folder.FolderType a) {
        return mDelegate.canCreate(a);
    }

    @Override
    public int getMessageCount() throws MessagingException {
        return mDelegate.getMessageCount();
    }

    @Override
    public int getUnreadMessageCount() throws MessagingException {
        return mDelegate.getUnreadMessageCount();
    }

    @Override
    public Message[] getMessages(int a, int b, Folder.MessageRetrievalListener c)
            throws MessagingException {
        return mDelegate.getMessages(a, b, c);
    }

    @Override
    public Message[] getMessages(Folder.MessageRetrievalListener a) throws MessagingException {
        return mDelegate.getMessages(a);
    }

    @Override
    public Message[] getMessages(String[] a, Folder.MessageRetrievalListener b)
            throws MessagingException {
        return mDelegate.getMessages(a, b);
    }

    @Override
    public Message[] getMessages(Flag[] a, Flag[] b, Folder.MessageRetrievalListener c)
            throws MessagingException {
        return mDelegate.getMessages(a, b, c);
    }

    @Override
    public void setFlags(Message[] a, Flag[] b, boolean c) throws MessagingException {
        mDelegate.setFlags(a, b, c);
    }

    @Override
    public void appendMessages(Message[] a) throws MessagingException {
        mDelegate.appendMessages(a);
    }

    @Override
    public void copyMessages(Message[] a, Folder b, Folder.MessageUpdateCallbacks c)
            throws MessagingException {
        mDelegate.copyMessages(a, b, c);
    }

    @Override
    public Message[] expunge() throws MessagingException {
        return mDelegate.expunge();
    }
    
    @Override
    public Message[] logout() throws MessagingException {
    	return mDelegate.expunge();
    }

    @Override
    public void fetch(Message[] a, FetchProfile b, Folder.MessageRetrievalListener c)
            throws MessagingException {
        mDelegate.fetch(a, b, c);
    }

    @Override
    public Flag[] getPermanentFlags() throws MessagingException {
        return mDelegate.getPermanentFlags();
    }

    @Override
    public Folder.FolderRole getRole() {
        return mDelegate.getRole();
    }

    @Override
    public void localFolderSetupComplete(Folder a) throws MessagingException {
        mDelegate.localFolderSetupComplete(a);
    }

    @Override
    public Message createMessage(String a) throws MessagingException {
        return mDelegate.createMessage(a);
    }

	@Override
	public int getQuotaRootInformation() throws MessagingException {
		return mDelegate.getQuotaRoot();
		
	}
}
