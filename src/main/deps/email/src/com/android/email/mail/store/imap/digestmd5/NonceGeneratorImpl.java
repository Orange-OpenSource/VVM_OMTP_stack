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

import java.security.SecureRandom;
import java.util.Random;

/**
 * Implementation of {@link NonceGenerator}.
 *
 * @author flerda@google.com (Flavio Lerda)
 */
public class NonceGeneratorImpl implements NonceGenerator {
    private Random random = new SecureRandom();

    @Override
    public String next() {
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return DigestMd5Utils.b64Encode(bytes);
    }
}
