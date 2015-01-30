/*
 * Copyright (C) 2012 Orange Labs UK. All Rights Reserved.
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
 * limitations under the License
 */
package com.orange.labs.uk.omtp.protocol;

/**
 * Utility class with helper methods to deal with Omtp spec related data types.
 */
public class OmtpUtil {
    /**
     * Matches the provided OMTP string value to the corresponding enum defined in {@link Omtp}
     * class.
     *
     * @param <T> The {@link Omtp.EnumValue} type
     * @param omtpStringValue The string value for the enum that we are looking for
     * @param enumClass The enum class to which we want to map the stringValue to
     * @return The {@link Omtp.EnumValue} object to which the string matches with
     * @throws IllegalArgumentException if it could not matched against any of the values of the
     *             said {@link Omtp.EnumValue} class
     */
    public static <T extends Enum<T> & Omtp.EnumValue> T omtpValueToEnumValue(
            String omtpStringValue,
            Class<T> enumClass) {
        for (T enumValue : enumClass.getEnumConstants()) {
            if (enumValue.getCode().equals(omtpStringValue)) {
                return enumClass.cast(enumValue);
            }
        }
        throw new IllegalArgumentException(
                "No matching enum value found for '" + omtpStringValue + "'"
                + " in " + enumClass.getSimpleName());
    }
}
