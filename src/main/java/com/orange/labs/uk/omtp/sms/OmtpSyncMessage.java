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

import javax.annotation.Nullable;

import com.orange.labs.uk.omtp.protocol.Omtp;

/**
 * Structured data representation of an OMTP SYNC message.
 */
public interface OmtpSyncMessage extends OmtpMessage {
    /**
     * Returns the event that triggered the sync message. This is a mandatory field and must always
     * be set.
     */
    public Omtp.SyncTriggerEvent getSyncTriggerEvent();

    /**
     * Returns the number of new messages stored on the voicemail server.
     */
    public int getNewMessageCount();

    public boolean hasNewMessageCount();

    /**
     * Returns the message ID of the new message.
     * <p>
     * Expected to be set only for
     * {@link com.google.android.voicemail.example.spec.Omtp.SyncTriggerEvent#NEW_MESSAGE}
     *
     * @return null if the field was not set in the message body or it could not be parsed.
     */
    @Nullable
    public String getId();

    public boolean hasId();

    /**
     * Returns the content type of the new message.
     * <p>
     * Expected to be set only for
     * {@link com.google.android.voicemail.example.spec.Omtp.SyncTriggerEvent#NEW_MESSAGE}
     *
     * @return null if the field was not set in the message body or it could not be parsed.
     */
    @Nullable
    public Omtp.ContentType getContentType();

    public boolean hasContentType();

    /**
     * Returns the message length of the new message.
     * <p>
     * Expected to be set only for
     * {@link com.google.android.voicemail.example.spec.Omtp.SyncTriggerEvent#NEW_MESSAGE}
     */
    @Nullable
    public int getLength();

    public boolean hasLength();

    /**
     * Returns the sender's phone number of the new message specified as MSISDN.
     * <p>
     * Expected to be set only for
     * {@link com.google.android.voicemail.example.spec.Omtp.SyncTriggerEvent#NEW_MESSAGE}
     *
     * @return null if the field was not set in the message body or it could not be parsed.
     */
    @Nullable
    public String getSender();

    public boolean hasSender();

    /**
     * Returns the timestamp as milliseconds for the new message.
     * <p>
     * Expected to be set only for
     * {@link com.google.android.voicemail.example.spec.Omtp.SyncTriggerEvent#NEW_MESSAGE}
     */
    @Nullable
    public long getTimestampMillis();

    public boolean hasTimestampMillis();

	/**
	 * Returns SMS message source phone number
	 */
    public String getSmsOriginatorNumber();

	public boolean hasSmsOriginatorNumber();
}
