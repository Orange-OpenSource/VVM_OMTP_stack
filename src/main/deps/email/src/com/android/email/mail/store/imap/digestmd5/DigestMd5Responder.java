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

package com.android.email.mail.store.imap.digestmd5;

import com.android.email.mail.MessagingException;
import com.android.email.mail.store.imap.ImapResponder;
import com.android.email.mail.store.imap.ImapResponderChain;
import com.android.email.mail.store.imap.ImapResponse;

/**
 * Responder for the AUTHENTICATE DIGEST-MD5 IMAP command.
 *
 * @author flerda@google.com (Flavio Lerda)
 */
public final class DigestMd5Responder implements ImapResponder {
    private final ImapResponderChain mResponderChain;

    public DigestMd5Responder(String username, String password) {
        mResponderChain = new ImapResponderChain();
        mResponderChain.setNextResponder(new ChallengeResponder(username, password));
    }

    @Override
    public String onResponse(ImapResponse response) throws MessagingException {
        return mResponderChain.onResponse(response);
    }

    /** Responds to the server challenge. */
    private class ChallengeResponder implements ImapResponder {
        private final String mUsername;
        private final String mPassword;

        public ChallengeResponder(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        public String onResponse(ImapResponse response) throws MessagingException {
            mResponderChain.setNextResponder(new ServerAuthResponder());

            String challenge = response.getStringOrEmpty(0).getString();
            DigestMd5Authenticator authenticator =
                    new DigestMd5AuthenticatorImpl(mUsername, mPassword, new NonceGeneratorImpl());
            return authenticator.respondToChallenge(challenge);
        }
    }

    /**
     * Responds to the server's own authentication.
     * <p>
     * This implementation actually ignores the server response, and always accepts the response.
     */
    private class ServerAuthResponder implements ImapResponder {
        @Override
        public String onResponse(ImapResponse response) {
            // There should be no further response.
            mResponderChain.setNextResponder(null);
            return "";
        }
    }
}
