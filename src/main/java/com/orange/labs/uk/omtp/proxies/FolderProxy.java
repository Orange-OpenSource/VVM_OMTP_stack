/*
 * Copyright (C) 2011 Google Inc. All Rights Reserved.
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
public interface FolderProxy {
    public String getName();
    public Message getMessage(String a) throws MessagingException;
    public void close(boolean a) throws MessagingException;
    public void close(boolean a, boolean b) throws MessagingException;
    public void delete(boolean a) throws MessagingException;
    public boolean create(Folder.FolderType a) throws MessagingException;
    public void open(Folder.OpenMode a, Folder.PersistentDataCallbacks b) throws MessagingException;
    public boolean isOpen();
    public boolean exists() throws MessagingException;
    public Folder.OpenMode getMode() throws MessagingException;
    public boolean canCreate(Folder.FolderType a);
    public int getMessageCount() throws MessagingException;
    public int getUnreadMessageCount() throws MessagingException;
    public Message[] getMessages(int a, int b, Folder.MessageRetrievalListener c)
            throws MessagingException;
    public Message[] getMessages(Folder.MessageRetrievalListener a) throws MessagingException;
    public Message[] getMessages(String[] a, Folder.MessageRetrievalListener b)
            throws MessagingException;
    public Message[] getMessages(Flag[] a, Flag[] b, Folder.MessageRetrievalListener c)
            throws MessagingException;
    public void setFlags(Message[] a, Flag[] b, boolean c) throws MessagingException;
    public void appendMessages(Message[] a) throws MessagingException;
    public void copyMessages(Message[] a, Folder b, Folder.MessageUpdateCallbacks c)
            throws MessagingException;
    public Message[] expunge() throws MessagingException;
    public Message[] logout() throws MessagingException;
    public void fetch(Message[] a, FetchProfile b, Folder.MessageRetrievalListener c)
            throws MessagingException;
    public Flag[] getPermanentFlags() throws MessagingException;
    public Folder.FolderRole getRole();
    public void localFolderSetupComplete(Folder a) throws MessagingException;
    public Message createMessage(String a) throws MessagingException;
    public int getQuotaRootInformation() throws MessagingException;
}
