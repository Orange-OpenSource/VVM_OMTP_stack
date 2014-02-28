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

import java.io.UnsupportedEncodingException;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.orange.labs.uk.omtp.account.OmtpAccountInfo;
import com.orange.labs.uk.omtp.account.OmtpAccountStoreWrapper;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.notification.NotifChannelNotification;
import com.orange.labs.uk.omtp.notification.ProviderNotification;
import com.orange.labs.uk.omtp.notification.SourceNotifier;
import com.orange.labs.uk.omtp.protocol.Omtp;
import com.orange.labs.uk.omtp.protocol.Omtp.MoSmsRequest;
import com.orange.labs.uk.omtp.provider.OmtpProviderInfo;
import com.orange.labs.uk.omtp.proxy.OmtpSmsManagerProxy;

/**
 * Implementation of {@link OmtpMessageSender} interface.
 * <p>
 * Provides simple APIs to send different types of mobile originated OMTP SMS to the VVM server.
 */
public final class OmtpMessageSenderImpl implements OmtpMessageSender {
	private static final Logger logger = Logger.getLogger(OmtpMessageSender.class);

	private static final String SMS_SENT_STATUS_ACTION = "com.orange.labs.uk.omtp.action.SMS_SENT_STATUS";
	
	private final OmtpSmsManagerProxy mSmsManager;
	private final OmtpAccountStoreWrapper mAccountStore;
	private final OmtpProviderInfo mProviderInfo;
	private final SourceNotifier mSourceNotifier;
	private final String mClientPrefix;
	private final Context mContext;
	private final Executor mExecutor;

	/**
	 * Creates a provider specific instance of MessageSender with values picked from the supplied
	 * providerConfig.
	 * <p>
	 * Uses {@link Omtp#CLIENT_PREFIX} for clientPrefix.
	 * 
	 * @param smsManager
	 *            To be used to send SMS across
	 * @param accountStoreWrapper
	 *            To be used to fetch SMS destination number
	 * @param providerStoreWrapper
	 *            To be used to get providerInfo which then provides specific provider information,
	 *            such us protocol version,
	 * @param context
	 * @param executor 
	 * @param smsTimeoutHandler
	 */
	public OmtpMessageSenderImpl(OmtpSmsManagerProxy smsManager,
			OmtpAccountStoreWrapper accountStoreWrapper, OmtpProviderInfo providerInfo,
			SourceNotifier sourceNotifier, Context context, Executor executor) {
		mProviderInfo = providerInfo;
		mSmsManager = smsManager;
		mAccountStore = accountStoreWrapper;
		mSourceNotifier = sourceNotifier;
		mContext = context;
		mClientPrefix = Omtp.CLIENT_PREFIX;
		mExecutor = executor;

	}

	@Override
	public void requestVvmActivation() {
		sendMessage(Omtp.MoSmsRequest.ACTIVATE);
	}

	@Override
	public void requestVvmDeactivation() {
		sendMessage(Omtp.MoSmsRequest.DEACTIVATE);
	}

	@Override
	public void requestVvmStatus() {
		sendMessage(Omtp.MoSmsRequest.STATUS);
	}

	private void sendMessage(final MoSmsRequest action) {
		mExecutor.execute(new Runnable() {

			@Override
			public void run() {
				logger.d(String.format("Preparing to send SMS message type:%s", action));
				String messageToSend = buildMessageBody(action);
				if (messageToSend != null) {
					sendSms(messageToSend);
				} else {
					logger.w("Requested SMS has not been build and will not be sent");
					// create error notification
					mSourceNotifier.sendNotification(NotifChannelNotification.messageBuildFailed(mContext));
				}
			}
		});
	}

	private void sendSms(String text) {
		// find destination
		String destinationAddress;
		OmtpAccountInfo accountInfo = mAccountStore.getAccountInfo();
		if (accountInfo != null && accountInfo.hasClientSmsDestinationNumber()) {
			destinationAddress = accountInfo.getSmsNumber();
		} else {
			logger.d("Using default destination number from Provider settings");
			destinationAddress = mProviderInfo.getSmsDestinationNumber();
		}

        // Get the Short message Service center number, it can be null
        String shortMessageServiceCenterNumber = mProviderInfo.getSmsServiceCenter();

		// create PendingIntent for SMS sent status
		Intent intentSmsSendStatus = new Intent(SMS_SENT_STATUS_ACTION);
		PendingIntent sentIntent = PendingIntent.getBroadcast(mContext, 0, intentSmsSendStatus,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// If application port is set to 0 then send simple text message, else
		// send data message.
		if (mProviderInfo.getSmsDestinationPort() == 0) {
			logger.d(String.format("Sending TEXT sms '%s' to %s via sc: %s", text, destinationAddress, shortMessageServiceCenterNumber));
			mSmsManager.sendTextMessage(destinationAddress, shortMessageServiceCenterNumber, text, sentIntent, null);
		} else {
			byte[] data;
			try {
				data = text.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException("Failed to encode: " + text);
			}
			logger.d(String.format("Sending BINARY sms '%s' to %s:%d via sc: %s", text, destinationAddress,
					mProviderInfo.getSmsDestinationPort(), shortMessageServiceCenterNumber));
			mSmsManager.sendDataMessage(destinationAddress, shortMessageServiceCenterNumber,
					mProviderInfo.getSmsDestinationPort(), data, sentIntent, null);

		}
	}

	// Evolution of MO messages.
	//
	// Activate message:
	// V1.1: Activate:pv=<value>;ct=<value>
	// V1.2: Activate:pv=<value>;ct=<value>;pt=<value>;<Clientprefix>
	// V1.3: Activate:pv=<value>;ct=<value>;pt=<value>;<Clientprefix>
	//
	// Deactivate message:
	// V1.1: Deactivate:pv=<value>;ct=<string>
	// V1.2: Deactivate:pv=<value>;ct=<string>
	// V1.3: Deactivate:pv=<value>;ct=<string>
	//
	// Status message:
	// V1.1: STATUSjava illegalstateexception when to use
	// V1.2: STATUS
	// V1.3: STATUS:pv=<value>;ct=<value>;pt=<value>;<Clientprefix>
	@Nullable
	private String buildMessageBody(Omtp.MoSmsRequest request) {

		// check if there is a providerInfo object initialised (i.e. it will be
		// null if VVM configuration for the current SIM card issuing operator
		// is not present)
		if (mProviderInfo == null) {
			mSourceNotifier.sendNotification(ProviderNotification.error(mContext));
			return null;
		}

		StringBuilder sb = new StringBuilder();

		// Request code.
		sb.append(request.getCode());
		// Other fields, specific to the request and protocol version.
		switch (request) {
		case ACTIVATE:
			appendProtocolVersionAndClientType(sb);
			if (mProviderInfo.getProtocolVersion().isGreaterOrEqualTo(Omtp.ProtocolVersion.V1_2)) {
				appendApplicationport(sb);
				appendClientPrefix(sb);
			}
			break;
		case DEACTIVATE:
			appendProtocolVersionAndClientType(sb);
			break;
		case STATUS:
			if (mProviderInfo.getProtocolVersion().isGreaterOrEqualTo(Omtp.ProtocolVersion.V1_3)) {
				appendProtocolVersionAndClientType(sb);
				appendApplicationport(sb);
				appendClientPrefix(sb);
			}
			break;
		}
		return sb.toString();
	}

	void appendProtocolVersionAndClientType(StringBuilder sb) {
		sb.append(Omtp.SMS_PREFIX_SEPARATOR);
		appendField(sb, Omtp.MoSmsFields.PROTOCOL_VERSION, mProviderInfo.getProtocolVersion()
				.getCode());
		sb.append(Omtp.SMS_FIELD_SEPARATOR);
		appendField(sb, Omtp.MoSmsFields.CLIENT_TYPE, mProviderInfo.getClientType());
	}

	void appendApplicationport(StringBuilder sb) {
		sb.append(Omtp.SMS_FIELD_SEPARATOR);
		appendField(sb, Omtp.MoSmsFields.APPLICATION_PORT, mProviderInfo.getSmsDestinationPort());
	}

	void appendClientPrefix(StringBuilder sb) {
		sb.append(Omtp.SMS_FIELD_SEPARATOR);
		sb.append(mClientPrefix);
	}

	private void appendField(StringBuilder sb, Omtp.MoSmsFields field, Object value) {
		sb.append(field.getKey()).append(Omtp.SMS_KEY_VALUE_SEPARATOR).append(value);
	}
}
