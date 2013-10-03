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

import java.util.Map;

/**
 * Implementation of {@link DigestMd5Authenticator}.
 * 
 * This class has been modified to avoid throwing an exception if the realm parameter is absent.
 * The IMAP RFC states that this parameter SHOULD be present and not that it MUST be present. Thus,
 * some OMTP platforms do not send it (such as Unisys).
 *
 * @author flerda@google.com (Flavio Lerda)
 */
public class DigestMd5AuthenticatorImpl implements DigestMd5Authenticator {
    private static final byte[] COLON_BYTES = new byte[]{':'};

    private final String mUsername;
    private final String mPassword;
    private final NonceGenerator mNonceGenerator;

    public DigestMd5AuthenticatorImpl(String username, String password,
            NonceGenerator nonceGenerator) {
        mUsername = username;
        mPassword = password;
        mNonceGenerator = nonceGenerator;
    }

    @Override
    public String respondToChallenge(String challenge) throws MessagingException {
        String decodedChallenge = DigestMd5Utils.b64Decode(challenge);
        Map<String, String> fields = DigestMd5Utils.parseSaslMessage(decodedChallenge);
        String realm = ""; // Absent realm is now authorized.
        if (fields.containsKey("realm")) {
        	realm = fields.get("realm");
        }
        if (!fields.containsKey("nonce")) {
            throw new MessagingException("challenge did not contain nonce");
        }
        String nonce = fields.get("nonce");
        String cnonce = mNonceGenerator.next();
        String digestUri = String.format("imap/%s", realm);
        String challengeResponse;
        challengeResponse = getSaslResponse(
                mUsername, realm, mPassword, nonce, cnonce,
                digestUri);
        String response = String.format(
                "username=\"%s\",realm=\"%s\",nonce=\"%s\",nc=00000001,cnonce=\"%s\","
                + "digest-uri=\"%s\",response=%s,qop=auth",
                mUsername, realm, nonce, cnonce, digestUri, challengeResponse);
        return DigestMd5Utils.b64Encode(response);
    }

    private String getSaslResponse(String username, String realm,
            String password, String nonce, String cnonce,
            String digestUri) {
        byte[] urp = DigestMd5Utils.md5Of(String.format("%s:%s:%s", username, realm, password));
        byte[] a1 = DigestMd5Utils.concatenateBytes(
                urp, COLON_BYTES, nonce.getBytes(), COLON_BYTES, cnonce.getBytes());
        String a2 = String.format("AUTHENTICATE:%s", digestUri);
        String a3 = String.format("%s:%s:00000001:%s:auth:%s",
                DigestMd5Utils.hexDigest(a1),
                nonce,
                cnonce,
                DigestMd5Utils.hexDigest(a2.getBytes()));
        return DigestMd5Utils.hexDigest(a3);
    }
}
