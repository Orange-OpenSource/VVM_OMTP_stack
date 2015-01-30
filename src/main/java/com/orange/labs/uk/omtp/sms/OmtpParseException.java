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

import com.orange.labs.uk.omtp.protocol.Omtp;

/**
 * Exception thrown by SMS parser if there are parse failures.
 */
public class OmtpParseException extends Exception {
	
	private static final long serialVersionUID = -8847084050243653323L;

	public OmtpParseException(String msg) {
        super(msg);
    }

    /**
     * To be used when parsing failed due to an underlying exception.
     *
     * @param field Field for which parsing failed
     * @param value Value that failed to parse
     * @param e Underlying exception that caused parse failure
     */
    public OmtpParseException(Omtp.Field field, String value, Throwable e) {
        super(buildMessage(field, value), e);
    }

    /**
     * To be used when parsing failed because the value was invalid for the given field.
     *
     * @param field Field for which parsing failed
     * @param value Value that failed to parse
     */
    public OmtpParseException(Omtp.Field field, String value) {
        super(buildMessage(field, value));
    }

    /**
     * To be used when parsing failed because a mandatory field was missing.
     *
     * @param missingField Mandatory field which was found to be missing
     */
    public OmtpParseException(Omtp.Field missingField) {
        super(buildMessage(missingField));
    }

    /**
     * To be used when parsing failed because the value did not match with any of the valid enum
     * values of the supplied enum class.
     *
     * @param field Field for which parsing failed
     * @param value Value that failed to parse
     * @param enumClass The enum class that the value was expected from to match
     */
    public <T extends Enum<T> & Omtp.EnumValue> OmtpParseException(Omtp.Field field, String value,
            Class<T> enumClass) {
        super(buildMessage(field, value, enumClass.getEnumConstants()));
    }

    private static String buildMessage(Omtp.Field missingField) {
        return new StringBuilder()
                .append("Mandatory field '").append(missingField.getKey()).append("' missing.")
                .toString();
    }

    private static String buildMessage(Omtp.Field field, String value) {
        return new StringBuilder()
                .append("Field: ").append(field.getKey())
                .append(", value: ").append(value)
                .toString();
    }

    private static String buildMessage(Omtp.Field field, String value,
            Omtp.EnumValue[] possibleValues) {
        StringBuilder sb = new StringBuilder();
        sb.append("Field: ").append(field.getKey());
        sb.append(", value: ").append(value);
        sb.append(", Allowed enum values: [");
        for (Omtp.EnumValue enumValue : possibleValues) {
            sb.append(enumValue.getCode()).append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
