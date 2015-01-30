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
package com.orange.labs.uk.omtp.imap;

import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.voicemail.Voicemail;

/**
 * A helper interface to abstract commands sent across IMAP interface for a given account. The
 * result is always conveyed through a client supplied callback method.
 * <p>
 * It is up to the implementation to decide if the callback is invoked synchronously or
 * asynchronously.
 */
/*package protected*/ interface ImapHelper {
    /** Mark the list of voicemail as read on the imap server. */
    public void markMessagesAsRead(Callback<Void> callback, Voicemail... voicemails);

    /** Mark the list of voicemail as deleted on the imap server. */
    public void markMessagesAsDeleted(Callback<Void> callback, Voicemail... voicemails);
}
