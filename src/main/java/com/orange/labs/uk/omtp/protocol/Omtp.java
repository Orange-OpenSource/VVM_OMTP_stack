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
 * Wrapper class to hold relevant OMTP constants and enums as defined in the OMTP spec.
 * <p>
 * In essence this is a programmatic representation of the relevant portions of OMTP spec.
 */
public class Omtp {
    public static final String SMS_FIELD_SEPARATOR = ";";
    public static final String SMS_KEY_VALUE_SEPARATOR = "=";
    public static final String SMS_PREFIX_SEPARATOR = ":";

    public static final String CLIENT_PREFIX = "//VVM";
    public static final String SYNC_SMS_PREFIX = CLIENT_PREFIX + ":SYNC:";
    public static final String STATUS_SMS_PREFIX = CLIENT_PREFIX + ":STATUS:";

    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm Z";

    /**
     * Interface for all OMTP parsed fields.
     */
    public interface Field {
        /**
         * Returns the string constant that is used as the key for this OMTP field.
         */
        public String getKey();
    }

    /**
     * Interface for all OMTP parsed values that are represented as enums (i.e valid values are from
     * a predefined set).
     */
    public interface EnumValue {
        /**
         * Returns a string constant that maps to the specific enum value.
         */
        public String getCode();
    }

    /**
     * Enumeration of OMTP protocol versions.
     */
    public enum ProtocolVersion implements EnumValue {
        V1_1("11"),
        V1_2("12"),
        V1_3("13");

        private final String mCode;

        private ProtocolVersion(String code) {
            mCode = code;
        }

        @Override
        public String getCode() {
            return mCode;
        }

        public boolean isGreaterOrEqualTo(ProtocolVersion other) {
            return this.ordinal() >= other.ordinal();
        }

        public boolean isLessThan(ProtocolVersion other) {
            return this.ordinal() < other.ordinal();
        }
    }

    //////////////////////////////// Sync SMS fields ////////////////////////////

    /**
     * Enumeration of Sync SMS fields.
     * <p>
     * Each field defined in this enum is associated to a string constant. This string constant is
     * nothing but the field's key in the SMS body which is used by the parser to identify the
     * field's value, if present, in the SMS body.
     */
    public enum SyncSmsField implements Field {
        /** The event that triggered this SYNC SMS. See {@link SyncTriggerEvent} */
        SYNC_TRIGGER_EVENT("ev"),
        MESSAGE_UID("id"),
        MESSAGE_LENGTH("l"),
        NUM_MESSAGE_COUNT("c"),
        /** See {@link ContentType} */
        CONTENT_TYPE("t"),
        SENDER("s"),
        TIME("dt");

        private final String mKey;

        private SyncSmsField(String key) {
            this.mKey = key;
        }

        @Override
        public String getKey() {
            return mKey;
        }
    }

    /**
     * Enumeration of SYNC message trigger events.
     * <p>
     * These are the possible values of {@link SyncSmsField#SYNC_TRIGGER_EVENT}
     */
    public enum SyncTriggerEvent implements EnumValue {
        NEW_MESSAGE("NM"),
        MAILBOX_UPDATE("MBU"),
        GREETINGS_UPDATE("GU");

        private final String mCode;

        private SyncTriggerEvent(String code) {
            mCode = code;
        }

        @Override
        public String getCode() {
            return mCode;
        }
    }

    /**
     * Enumeration of content types supported by OMTP VVM.
     * <p>
     * These are the possible values of {@link SyncSmsField#CONTENT_TYPE}
     */
    public enum ContentType implements EnumValue {
        VOICE("v"),
        VIDEO("o"),
        FAX("f"),
        /** Voice message deposited by an external application */
        INFOTAINMENT("i"),
        /** Empty Call Capture - i.e. voicemail with no voice message. */
        ECC("e");

        private final String mCode;

        private ContentType(String code) {
            mCode = code;
        }

        @Override
        public String getCode() {
            return mCode;
        }
    }

    ////////////////////////////// Status SMS fields ////////////////////////////

    /**
     * Enumeration of Status SMS fields, along with its key as defined by OMTP.
     */
    public enum StatusSmsField implements Field {
        /** See {@link ProvisioningStatus} */
        PROVISIONING_STATUS("st"),
        /** See {@link ReturnCode} */
        RETURN_CODE("rc"),
        /** URL to send users to for activation VVM */
        SUBSCRIPTION_URL("rs"),
        /** IMAP4/SMTP server IP address or fully qualified domain name */
        SERVER_ADDRESS("srv"),
        /** Phone number to access voicemails through Telephony User Interface */
        TUI_ACCESS_NUMBER("tui"),
        /** Number to send client origination SMS */
        CLIENT_SMS_DESTINATION_NUMBER("dn"),
        IMAP_PORT("ipt"),
        IMAP_USER_NAME("u"),
        IMAP_PASSWORD("pw"),
        SMTP_PORT("spt"),
        SMTP_USER_NAME("smtp_u"),
        SMTP_PASSWORD("smtp_pw"),
        /** '|' separated list of languages supported by VVM system */
        SUPPORTED_LANGUAGES("lang"),
        /** Max allowed greetings length in seconds. */
        MAX_GREETINGS_LENGTH("g_len"),
        /** Max allowed voice signature length in seconds. */
        MAX_VOICE_SIGNATURE_LENGTH("vs_len"),
        /**
         * TUI password length range supported by the system, represented as <min len>-<max len>.
         */
        TUI_PASSWORD_LENGTH_RANGE("pw_len"),
        /**
         * Whether the subscriber is required to reset the pin on service activation. See
         * {@link ResetPinOnActivation}
         */
        RESET_PIN_ON_ACTIVATION("pm"),
        /**
         * Which greetings, if any, the subscriber is required to reset on service activation. See
         * {@link ResetGreetingOnActivation}
         */
        RESET_GREETINGS_ON_ACTIVATION("gm");

        private final String mKey;

        private StatusSmsField(String key) {
            this.mKey = key;
        }

        @Override
        public String getKey() {
            return mKey;
        }
    }

    /**
     * Enumeration of user provisioning status.
     * <p>
     * Referred by {@link StatusSmsField#PROVISIONING_STATUS}
     */
    public enum ProvisioningStatus implements EnumValue {
        // TODO: As per the spec the code could be either be with or w/o quotes ("N"/N). Currently
        // this only handles the w/o quotes values.
        SUBSCRIBER_NEW("N"),
        SUBSCRIBER_READY("R"),
        SUBSCRIBER_PROVISIONED("P"),
        SUBSCRIBER_UNKNOWN("U"),
        SUBSCRIBER_BLOCKED("B");

        private final String mCode;

        private ProvisioningStatus(String code) {
            mCode = code;
        }

        @Override
        public String getCode() {
            return mCode;
        }
    }

    /**
     * Enumeration of return code included in a status message.
     * <p>
     * These are the possible values of {@link StatusSmsField#RETURN_CODE}
     */
    public enum ReturnCode implements EnumValue {
        SUCCESS("0"),
        SYSTEM_ERROR("1"),
        SUBSCRIBER_ERROR("2"),
        MAILBOX_UNKNOWN("3"),
        VVM_NOT_ACTIVATED("4"),
        VVM_NOT_PROVISIONED("5"),
        VVM_CLIENT_UKNOWN("6"),
        VVM_MAILBOX_NOT_INITIALIZED("7");

        private final String mCode;

        private ReturnCode(String code) {
            mCode = code;
        }

        @Override
        public String getCode() {
            return mCode;
        }
    }

    /**
     * Enumeration of reset pin field in a status message.
     * <p>
     * These are the possible values of {@link StatusSmsField#RESET_PIN_ON_ACTIVATION}
     */
    public enum ResetPinOnActivation implements EnumValue {
        YES("Y"),
        NO("N");

        private final String mCode;

        private ResetPinOnActivation(String code) {
            mCode = code;
        }

        @Override
        public String getCode() {
            return mCode;
        }
    }

    /**
     * Enumeration of reset greetings field in a status message.
     * <p>
     * These are the possible values of {@link StatusSmsField#RESET_GREETINGS_ON_ACTIVATION}
     */
    public enum ResetGreetingOnActivation implements EnumValue {
        /** Required to reset only the normal greeting. */
        NORMAL_GREETING("G"),
        /** Required to reset only the voice signature. */
        VOICE_SIGNATURE("V"),
        /** Required to reset both. */
        BOTH("B"),
        /** Required to reset none. */
        NONE("N");

        private final String mCode;

        private ResetGreetingOnActivation(String code) {
            mCode = code;
        }

        @Override
        public String getCode() {
            return mCode;
        }
    }

    ///////////////////////// Client/Mobile originated SMS //////////////////////

    /** Enumeration of Mobile Originated requests */
    public enum MoSmsRequest implements EnumValue {
        /** Activate VVM. */
        ACTIVATE("Activate"),
        /** Deactivate VVM. */
        DEACTIVATE("Deactivate"),
        STATUS("STATUS");

        private final String mCode;

        private MoSmsRequest(String code) {
            mCode = code;
        }

        @Override
        public String getCode() {
            return mCode;
        }
    }

    /** Enumeration of fields that can be present in a Mobile Originated OMTP SMS */
    public enum MoSmsFields implements Omtp.Field {
        CLIENT_TYPE("ct"),
        APPLICATION_PORT("pt"),
        PROTOCOL_VERSION("pv");

        private final String mKey;

        private MoSmsFields(String key) {
            this.mKey = key;
        }

        @Override
        public String getKey() {
            return mKey;
        }
    }
}
