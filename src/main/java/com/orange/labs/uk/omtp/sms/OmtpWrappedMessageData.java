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
package com.orange.labs.uk.omtp.sms;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.protocol.Omtp;
import com.orange.labs.uk.omtp.protocol.OmtpUtil;

/**
 * Class wrapping the raw OMTP message data, internally represented as as map of all key-value pairs
 * found in the SMS body.
 * <p>
 * Provides convenience methods to extract parse fields of different types.
 * <p>
 * All the methods return null if either the field was not present or it could not be parsed.
 */
/*package*/ class OmtpWrappedMessageData {
	
	private static final Logger logger = Logger.getLogger(OmtpWrappedMessageData.class);
	
    private final DateFormat mDateFormat;

    private final Map<String, String> mFields;

    @Override
    public String toString() {
        return "WrappedMessageData [mFields=" + mFields + "]";
    }

    OmtpWrappedMessageData(Map<String, String> keyValues, DateFormat dateFormat) {
        mFields = new HashMap<String, String>(keyValues);
        mDateFormat = dateFormat;
    }

    /**
     * Extracts the requested field from underlying data and returns the String value as is.
     *
     * @return the parsed string value, or null if the field was not present
     */
    @Nullable
    String extractString(final Omtp.Field field) {
        return mFields.get(field.getKey());
    }

    /**
     * Extracts the requested field from underlying data and parses it as an {@link Integer}.
     *
     * @return the parsed integer value, or null if the field was not present
     * @throws OmtpParseException if the field's value could not be parsed
     */
    @Nullable
    Integer extractInteger(final Omtp.Field field) throws OmtpParseException {
        String value = mFields.get(field.getKey());
        if (value == null) {
            return null;
        }

        try {
            return Integer.parseInt(value);
        } catch (RuntimeException e) {
            throw new OmtpParseException(field, value, e);
        }
    }

    /**
     * Extracts the requested field from underlying data and parses it as a date/time represented in
     * {@link Omtp#DATE_TIME_FORMAT} format.
     *
     * @param field The omtp field to be extracted
     * @return the parsed string value, or null if the field was not present
     * @throws OmtpParseException if the field's value could not be parsed
     */
    @Nullable
    Long extractTime(final Omtp.Field field) throws OmtpParseException {
        String value = mFields.get(field.getKey());
        if (value == null) {
            return null;
        }

        try {
            return mDateFormat.parse(value).getTime();
        } catch (ParseException e) {
        	logger.e("Parsing date field in the SMS message has failed! Date field is null");
            return null;
        }
    }

    /**
     * Extracts the requested field from underlying data and parses it as the specified enumClass.
     *
     * @return null if field was not present or.
     * @throws OmtpParseException if the field's value could not be mapped to the list of
     *             possibleValues
     */
    @Nullable
    <T extends Enum<T> & Omtp.EnumValue> T extractEnum(final Omtp.Field field,
            Class<T> enumClass) throws OmtpParseException {
        String value = mFields.get(field.getKey());
        if (value == null) {
            return null;
        }

        try {
            return OmtpUtil.omtpValueToEnumValue(value, enumClass);
        } catch (IllegalArgumentException e) {
            throw new OmtpParseException(field, value, enumClass);
        }
    }
}
