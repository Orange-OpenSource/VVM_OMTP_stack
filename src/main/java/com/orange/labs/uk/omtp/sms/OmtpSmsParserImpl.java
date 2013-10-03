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
package com.orange.labs.uk.omtp.sms;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.concurrent.ThreadSafe;

import com.orange.labs.uk.omtp.protocol.Omtp;

/**
 * The default implementation of {@link OmtpSmsParser}. This class is thread safe.
 */
@ThreadSafe
public class OmtpSmsParserImpl implements OmtpSmsParser {
    private final DateFormat mDateFormat;

    /**
     * @param dateFormat Dateformat to be used for parsing date field in the message.
     */
    public OmtpSmsParserImpl(DateFormat dateFormat) {
        mDateFormat = dateFormat;
    }

    @Override
    public OmtpMessage parse(String smsBody, String mSmsOriginatorNumber) throws OmtpParseException {
        if (smsBody == null) {
            throw new IllegalArgumentException("Null SMS body");
        }
        if (smsBody.startsWith(Omtp.SYNC_SMS_PREFIX)) {
            return new OmtpSyncMessageImpl(parseSyncSmsBody(smsBody), mSmsOriginatorNumber);
        } else if (smsBody.startsWith(Omtp.STATUS_SMS_PREFIX)) {
            return new OmtpStatusMessageImpl(parseStatusSmsBody(smsBody));
        } else {
            throw new OmtpParseException("Unknown OMTP message: " + smsBody);
        }
    }

    /**
     * Converts a String object containing a set of key/value pairs, to a Map object.
     *
     * @param in a String containing key/value pairs
     * @param delimEntry delimiter between map entries
     * @param delimKey delimiter between key and values for each entry
     */
	private Map<String, String> stringToMap(String in, String delimEntry, String delimKey) {
		Map<String, String> keyValues = new HashMap<String, String>();
		String[] entries = in.split(delimEntry);
		for (String entry : entries) {
			String[] keyValue = entry.split(delimKey);
			if (keyValue.length != 2) {
				keyValues.put(keyValue[0].trim(), "");
			} else {
				keyValues.put(keyValue[0].trim(), keyValue[1].trim());
			}
		}
        return keyValues;
    }

    private OmtpWrappedMessageData parseStatusSmsBody(String smsBody) throws OmtpParseException {
        return new OmtpWrappedMessageData(stringToMap(
                smsBody.substring(Omtp.STATUS_SMS_PREFIX.length()),
                Omtp.SMS_FIELD_SEPARATOR, Omtp.SMS_KEY_VALUE_SEPARATOR), mDateFormat);
    }

    private OmtpWrappedMessageData parseSyncSmsBody(String smsBody) throws OmtpParseException {
        return new OmtpWrappedMessageData(stringToMap(smsBody.substring(Omtp.SYNC_SMS_PREFIX.length()),
                Omtp.SMS_FIELD_SEPARATOR, Omtp.SMS_KEY_VALUE_SEPARATOR), mDateFormat);
    }
}
