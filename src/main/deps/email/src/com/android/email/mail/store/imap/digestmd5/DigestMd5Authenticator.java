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

/**
 * Interface to handle the challenge-response protocol of Digest MD5 authentication over IMAP.
 * <p>
 * This is based on RFC 2831: http://www.ietf.org/rfc/rfc2831.txt
 *
 * @author flerda@google.com (Flavio Lerda)
 */
public interface DigestMd5Authenticator {
    /**
     * Returns the response to the given challenge.
     *
     * @throws MessagingException if it is unable to generate a response for the challenge, e.g.,
     *         if the challenge is malformed
     */
    String respondToChallenge(String challenge) throws MessagingException;
}
