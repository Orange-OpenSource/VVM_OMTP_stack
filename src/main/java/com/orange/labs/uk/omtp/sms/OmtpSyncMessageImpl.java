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

import javax.annotation.concurrent.Immutable;

import com.orange.labs.uk.omtp.protocol.Omtp;

/**
 * The default implementation of {@link OmtpSyncMessage}.
 */
@Immutable
/*package*/ class OmtpSyncMessageImpl implements OmtpSyncMessage {
    // Sync event that triggered this message.
    private final Omtp.SyncTriggerEvent mSyncTriggerEvent;
    // Total number of new messages on the server.
    private final Integer mNewMessageCount;
    // UID of the new message.
    private final String mMessageId;
    // Length of the message.
    private final Integer mMessageLength;
    // Content type (voice, video, fax...) of the new message.
    private final Omtp.ContentType mContentType;
    // Sender of the new message.
    private final String mSender;
    // Timestamp (in millis) of the new message.
    private final Long mMsgTimeMillis;
    // SMS originator number (source phone number) 
    private final String mSmsOriginatorNumber;

    @Override
    public String toString() {
        return "SyncMessageImpl [mSyncTriggerEvent=" + mSyncTriggerEvent
                + ", mNewMessageCount=" + mNewMessageCount
                + ", mMessageId=" + mMessageId
                + ", mMessageLength=" + mMessageLength
                + ", mContentType=" + mContentType
                + ", mSender=" + mSender
                + ", mMsgTimeMillis=" + mMsgTimeMillis + "]";
    }

    public OmtpSyncMessageImpl(OmtpWrappedMessageData wrappedData, String smsOriginatorNumber) throws OmtpParseException {
        mSyncTriggerEvent = wrappedData.extractEnum(Omtp.SyncSmsField.SYNC_TRIGGER_EVENT,
                Omtp.SyncTriggerEvent.class);
        if (mSyncTriggerEvent == null) {
            throw new OmtpParseException(Omtp.SyncSmsField.SYNC_TRIGGER_EVENT);
        }
        mMessageId = wrappedData.extractString(Omtp.SyncSmsField.MESSAGE_UID);
        mMessageLength = wrappedData.extractInteger(Omtp.SyncSmsField.MESSAGE_LENGTH);
        mContentType = wrappedData.extractEnum(
                Omtp.SyncSmsField.CONTENT_TYPE, Omtp.ContentType.class);
        mSender = wrappedData.extractString(Omtp.SyncSmsField.SENDER);
        mNewMessageCount = wrappedData.extractInteger(Omtp.SyncSmsField.NUM_MESSAGE_COUNT);
        mMsgTimeMillis = wrappedData.extractTime(Omtp.SyncSmsField.TIME);
        mSmsOriginatorNumber = smsOriginatorNumber;
    }

	@Override
    public void visit(OmtpMessage.Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Omtp.SyncTriggerEvent getSyncTriggerEvent() {
        return mSyncTriggerEvent;
    }

    @Override
    public int getNewMessageCount() {
        return mNewMessageCount;
    }

    @Override
    public boolean hasNewMessageCount() {
        return mNewMessageCount != null;
    }

    @Override
    public String getId() {
        return mMessageId;
    }

    @Override
    public boolean hasId() {
        return mMessageId != null;
    }

    @Override
    public int getLength() {
        return mMessageLength;
    }

    @Override
    public boolean hasLength() {
        return mMessageLength != null;
    }

    @Override
    public String getSender() {
        return mSender;
    }

    @Override
    public boolean hasSender() {
        return mSender != null;
    }

    @Override
    public long getTimestampMillis() {
        return mMsgTimeMillis;
    }

    @Override
    public boolean hasTimestampMillis() {
        return mMsgTimeMillis != null;
    }

    @Override
    public Omtp.ContentType getContentType() {
        return mContentType;
    }

    @Override
    public boolean hasContentType() {
        return mContentType != null;
    }
    
    @Override
    public boolean hasSmsOriginatorNumber() {
        return mSmsOriginatorNumber != null;
    }
    
    @Override
    public String getSmsOriginatorNumber() {
  		return mSmsOriginatorNumber;
  	}
}
