/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.orange.labs.uk.omtp.proxy;

import android.app.PendingIntent;
import android.telephony.SmsManager;

import java.util.ArrayList;

/**
 * Wrapper interface around {@link SmsManager}. Using this interface to access {@link SmsManager}
 * functionality makes it simpler to unit test classes that uses {@link SmsManager}.
 */
public interface OmtpSmsManagerProxy {
    /**
     * @see SmsManager#sendDataMessage(String, String, short, byte[], PendingIntent, PendingIntent)
     */
    public void sendDataMessage(String destinationAddress, String scAddress, short destinationPort,
            byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent);

    /** @see SmsManager#sendTextMessage(String, String, String, PendingIntent, PendingIntent) */
    public void sendTextMessage(String destinationAddress, String scAddress,
            String text, PendingIntent sentIntent, PendingIntent deliveryIntent);

    /** @see SmsManager#sendMultipartTextMessage(String, String, ArrayList, ArrayList, ArrayList) */
    public void sendMultipartTextMessage(String destinationAddress, String scAddress,
            ArrayList<String> parts, ArrayList<PendingIntent> sentIntents,
            ArrayList<PendingIntent> deliveryIntents);
}
