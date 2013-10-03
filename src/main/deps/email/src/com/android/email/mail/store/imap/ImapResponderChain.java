/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.email.mail.store.imap;

import com.android.email.mail.MessagingException;

/**
 * A {@link ImapResponder} that allows chaining different responders.
 * <p>
 * You should call {@link #setNextResponder(ImapResponder)} when you create the object to set who
 * should respond to the first server response. After that, upon processing a response, the current
 * responder can call {@link #setNextResponder(ImapResponder)} again to determine who has to handle
 * the next response.
 * <p>
 * This class is thread safe.
 *
 * @author flerda@google.com (Flavio Lerda)
 */
public class ImapResponderChain implements ImapResponder {
    private volatile ImapResponder mResponder;

    public void setNextResponder(ImapResponder responder) {
        mResponder = responder;
    }

    @Override
    public String onResponse(ImapResponse response) throws MessagingException {
        if (mResponder == null) {
            throw new IllegalStateException("responder not set");
        }
        ImapResponder responder = mResponder;
        mResponder = null;
        return responder.onResponse(response);
    }
}
