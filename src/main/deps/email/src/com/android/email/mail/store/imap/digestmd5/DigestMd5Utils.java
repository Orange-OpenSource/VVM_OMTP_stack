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

import android.util.Base64;

import com.android.email.mail.MessagingException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility functions used for Digest-MD5 authentication.
 *
 * @author flerda@google.com (Flavio Lerda)
 */
/* package */ class DigestMd5Utils {
    private static final char[] HEX_CHARS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    private DigestMd5Utils() {}

    /** Encodes a string value into base64. */
    public static String b64Encode(String value) {
        return b64Encode(value.getBytes());
    }

    /** Encodes an array of bytes into base64. */
    public static String b64Encode(byte[] value) {
        return Base64.encodeToString(value, Base64.NO_WRAP);
    }

    /** Decodes the given string assume it is base64 encoded. */
    public static String b64Decode(String challenge) {
        return new String(Base64.decode(challenge, 0));
    }

    /** Returns the md5 of a given string. */
    public static byte[] md5Of(String value) {
        return getMd5().digest(value.getBytes());
    }

    /** Returns the md5 digest of a given string in hex. */
    public static String hexDigest(String value) {
        return hexDigest(value.getBytes());
    }

    /** Returns the md5 digest of a given array of bytes in hex. */
    public static String hexDigest(byte[] bytes) {
        return hexOf(getMd5().digest(bytes));
    }

    /** Returns the hex representation of an array of bytes. */
    public static String hexOf(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(HEX_CHARS[(b >> 4) & 0x0F]);
            builder.append(HEX_CHARS[b & 0x0F]);
        }
        return builder.toString();
    }

    /** Concatenates a set of byte arrays into a single byte array. */
    public static byte[] concatenateBytes(byte[]... values) {
        int length = 0;
        for (byte[] value : values) {
            length += value.length;
        }
        byte[] result = new byte[length];
        int offset = 0;
        for (byte[] value : values) {
            System.arraycopy(value, 0, result, offset, value.length);
            offset += value.length;
        }
        return result;
    }

    /**
     * Returns the {@link MessageDigest} for the MD5 algorithm.
     */
    private static MessageDigest getMd5() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // All Android implementation should support MD5.
            throw new IllegalStateException(e);
        }
    }

    /**
     * Parses the fields in the message and returns them as a map from field to value.
     * <p>
     * This is based on RFC 2831: http://www.ietf.org/rfc/rfc2831.txt
     *
     * @throws MessagingException if parsing of the value fails.
     */
    public static Map<String, String> parseSaslMessage(String message) throws MessagingException {
        StringStream stringStream = new StringStream(message);
        Map<String, String> fields = new HashMap<String, String>();

        while (!stringStream.isEmpty()) {
            checkSaslMessageInvariant(stringStream.contains('='), "missing = in message");
            String key = stringStream.until('=');
            checkSaslMessageInvariant(key.length() > 0, "empty key");
            fields.put(key, parseSaslValue(stringStream));
        }
        return fields;
    }

    /**
     * Parses a value from the string stream.
     * <p>
     * The value may be quoted and it terminates at the first unquoted comma, or the end of the
     * string.
     * <p>
     * If a value starts with double quotes, it will end at the first unquoted double quotes, and
     * it should be followed by a comma or the end of the string.
     * <p>
     * This method will consume all characters that are part of value itself and the following
     * comma in the given stream.
     *
     * @return the parsed value, with quotes removed if the value was quoted.
     * @throws MessagingException if parsing of the value fails.
     */
    private static String parseSaslValue(StringStream stringStream) throws MessagingException {
        if (stringStream.isEmpty()) {
            // The string is empty to begin with, the returned value is empty, and there is nothing
            // left.
            return "";
        }

        if (stringStream.peek() != '"') {
            return stringStream.until(',');
        }

        // This is the start of a quoted value.
        // Strip the initial quotes.
        stringStream.skip(1);
        // Continue until you find an unquoted set of quotes.
        StringBuilder valueBuilder = new StringBuilder();
        boolean quoted = false;
        while (!stringStream.isEmpty() && (stringStream.peek() != '"' || quoted)) {
            char ch = stringStream.next();
            if (quoted) {
                quoted = false;
                valueBuilder.append(ch);
            } else if (ch == '\\') {
                quoted = true;
            } else {
                valueBuilder.append(ch);
            }
        }
        checkSaslMessageInvariant(!stringStream.isEmpty(), "unterminated quoted value");
        // Skip the closed quotes.
        stringStream.skip(1);
        if (stringStream.isEmpty()) {
            // There is nothing remaining after the closed quote.
            return valueBuilder.toString();
        }
        checkSaslMessageInvariant(stringStream.peek() == ',', "expected comma after quoted value");
        // Skip the comma.
        stringStream.skip(1);
        return valueBuilder.toString();
    }

    /**
     * Checks that the given condition is true and throws an {@link MessagingException} otherwise.
     *
     * @param condition the condition to check
     * @param message the message to associate with the thrown exception
     *
     * @throws MessagingException if the condition if false
     */
    private static void checkSaslMessageInvariant(boolean condition, String message)
            throws MessagingException {
        if (!condition) {
            throw new MessagingException(message);
        }
    }

    /** A simple stream that reads its data from a string. */
    /*package for testing*/ static class StringStream {
        /** The string that represents the content of the stream. */
        private final String mBuffer;
        /** The current position within {@link #mBuffer}. */
        private int mPos;

        /** Creates a new stream containing the data in the string. */
        public StringStream(String buffer) {
            mBuffer = buffer;
            mPos = 0;
        }

        /** Returns true if the stream contains an instance of the given character. */
        public boolean contains(char ch) {
            return mBuffer.substring(mPos).contains("" + ch);
        }

        /** Returns and consumes the next character in the stream. */
        public char next() {
            char result = peek();
            mPos++;
            return result;
        }

        /** Returns the next character in the stream but it does not consume it. */
        public char peek() {
            notEof();
            return mBuffer.charAt(mPos);
        }

        /**
         * Returns and consumes the content of the stream until the next occurrence of
         * {@code delim}, or the entire remainder of the stream if {@code delim} does not appear in
         * the stream.
         * <p>
         * If the stream is empty, it will return the empty string.
         */
        public String until(char delim) {
            if (isEmpty()) {
                return "";
            }

            int nextPos = mBuffer.substring(mPos).indexOf(delim);
            if (nextPos == -1) {
                return rest();
            } else {
                nextPos += mPos;
                String result = mBuffer.substring(mPos, nextPos);
                mPos = nextPos + 1;
                return result;
            }
        }

        /** Returns and consumes the remainder of the stream. */
        public String rest() {
            String result = mBuffer.substring(mPos);
            mPos = mBuffer.length();
            return result;
        }

        /** Consumes the given number of characters in the stream. */
        public StringStream skip(int count) {
            checkArgument(count > 0, "count should be positive: " + count);
            inBounds(mPos + count - 1);
            mPos += count;
            return this;
        }

        /** Returns true if there are no character left in the stream. */
        public boolean isEmpty() {
            return mPos == mBuffer.length();
        }

        /**
         * Checks that there the stream is not empty.
         *
         * @throws IndexOutOfBoundsException if the stream is empty
         */
        private void notEof() {
            inBounds(mPos);
        }

        /**
         * Checks that the given position is within the bounds of the stream.
         *
         * @throws IndexOutOfBoundsException if the position is outside the bounds
         */
        private void inBounds(int pos) {
            if (pos >= mBuffer.length()) {
                throw new IndexOutOfBoundsException(pos + " >= " + mBuffer.length());
            }
        }
    }

    /**
     * Checks that the given condition is true and throws an {@link IllegalArgumentException}
     * otherwise.
     *
     * @param condition the condition to check
     * @param message the message to associate with the thrown exception
     *
     * @throws IllegalArgumentException if the condition if false
     */
    private static void checkArgument(boolean condition, String message)
            throws IllegalArgumentException {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
