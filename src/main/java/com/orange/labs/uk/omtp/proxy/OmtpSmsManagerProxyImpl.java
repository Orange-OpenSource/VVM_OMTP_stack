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
import android.app.PendingIntent.CanceledException;
import android.telephony.SmsManager;

import java.util.ArrayList;

/**
 * Proxy for {@link SmsManager}.
 */
public class OmtpSmsManagerProxyImpl implements OmtpSmsManagerProxy {
    private final SmsManager mSmsManager;

    public OmtpSmsManagerProxyImpl(SmsManager smsManager) {
        this.mSmsManager = smsManager;
    }

    @Override
    public void sendDataMessage(String destinationAddress, String scAddress, short destinationPort,
            byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        try {
            mSmsManager.sendDataMessage(destinationAddress, scAddress, destinationPort, data,
                    sentIntent,
                    deliveryIntent);
        } catch (NullPointerException npe) {
            // (Tested on the Motorola Droid)
            // When there is no SIM inserted, and we attempt to sendDataMessage(), the framework
            // blows up with an NPE. Yuck. This contrasts with the sendTextMessage() method, which
            // in the same situation, fires off the sentItent with
            // SmsManager.RESULT_ERROR_NO_SERVICE as the failure code. Because the NPE being thrown
            // is not useful for our clients, we translate it here to behave the same way as the
            // sendTextMessage() method.
            if (sentIntent != null) {
                try {
                    sentIntent.send(SmsManager.RESULT_ERROR_NO_SERVICE);
                } catch (CanceledException cancelledException) {
                    // If the sentIntent has been cancelled, then we don't need to send it on.
                }
            }
        }
    }

    @Override
    public void sendTextMessage(String destinationAddress, String scAddress,
            String text, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        mSmsManager.sendTextMessage(destinationAddress, scAddress, text, sentIntent,
                deliveryIntent);
    }

    @Override
    public void sendMultipartTextMessage(String destinationAddress, String scAddress,
            ArrayList<String> parts, ArrayList<PendingIntent> sentIntents,
            ArrayList<PendingIntent> deliveryIntents) {
        mSmsManager.sendMultipartTextMessage(destinationAddress, scAddress, parts, sentIntents,
                deliveryIntents);
    }
}
