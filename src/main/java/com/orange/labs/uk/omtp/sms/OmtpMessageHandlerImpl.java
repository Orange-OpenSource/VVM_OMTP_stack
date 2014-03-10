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

import android.content.ContentProvider;
import android.telephony.SmsMessage;

import com.orange.labs.uk.omtp.account.OmtpAccountInfo;
import com.orange.labs.uk.omtp.account.OmtpAccountStoreWrapper;
import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.config.StackStaticConfiguration;
import com.orange.labs.uk.omtp.greetings.GreetingUpdateType;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.notification.MessageNotification;
import com.orange.labs.uk.omtp.notification.NotifChannelNotification;
import com.orange.labs.uk.omtp.notification.SourceNotifier;
import com.orange.labs.uk.omtp.notification.StatusNotification;
import com.orange.labs.uk.omtp.notification.StatusNotification.Builder;
import com.orange.labs.uk.omtp.protocol.Omtp;
import com.orange.labs.uk.omtp.provider.OmtpProviderInfo;
import com.orange.labs.uk.omtp.sms.timeout.SmsTimeoutHandler;
import com.orange.labs.uk.omtp.sync.LocalVvmStore;
import com.orange.labs.uk.omtp.sync.SerialSynchronizer;
import com.orange.labs.uk.omtp.sync.SerialSynchronizer.SyncFlag;
import com.orange.labs.uk.omtp.sync.VvmStore;
import com.orange.labs.uk.omtp.sync.VvmStore.Action;
import com.orange.labs.uk.omtp.sync.VvmStoreActions;
import com.orange.labs.uk.omtp.voicemail.Voicemail;
import com.orange.labs.uk.omtp.voicemail.VoicemailImpl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Omtp SMS handler. Handles SYNC and STATUS messages and takes appropriate action.
 * <p>
 * This implementation is stateless.
 */
@ThreadSafe
public class OmtpMessageHandlerImpl implements OmtpMessageHandler, OmtpMessage.Visitor {
	private static final Logger logger = Logger.getLogger(OmtpMessageHandlerImpl.class);

	private final OmtpSmsParser mSmsParser;

	private final OmtpAccountStoreWrapper mAccountStore;

	private final SourceNotifier mSourceNotifier;

	private final SmsTimeoutHandler mSmsTimeoutHandler;

	/** Used to trigger synchronization on MBU SMS */
	private final SerialSynchronizer mSerialSynchronizer;

	/**
	 * Vvm store insert message into. This should be an instance of the local store.
	 */
	private final VvmStore mLocalVvmStore;

	/**
	 * Provider info instance, used to access provider data
	 */
	private final OmtpProviderInfo mProviderInfo;

	public OmtpMessageHandlerImpl(OmtpSmsParser smsParser, OmtpAccountStoreWrapper accountStore,
			SourceNotifier sourceNotifier, VvmStore localVvmStore,
			SmsTimeoutHandler smsTimeoutHandler, SerialSynchronizer serialSynchronizer,
			OmtpProviderInfo omtpProviderInfo) {
		mAccountStore = accountStore;
		mSmsParser = smsParser;
		mSourceNotifier = sourceNotifier;
		mLocalVvmStore = localVvmStore;
		mSmsTimeoutHandler = smsTimeoutHandler;
		mSerialSynchronizer = serialSynchronizer;
		mProviderInfo = omtpProviderInfo;
	}

	@Override
	public void process(Object[] omtpSmsPdus) {
		// Notes:
		// 1) OMTP message could be split into multiple messages. Merge them
		// together to build the full OMTP text.
		// 2) The omtpMessage is either included in the userData or in the
		// messageBody. This behaviour is likely to vary across different VVM
		// servers. Make sure we can handle both.
		logger.d("Num msgs:" + omtpSmsPdus.length);

        notifySmsReceived();

		String smsOriginatorNumber = null;
		StringBuilder userData = new StringBuilder();
		StringBuilder messageBody = new StringBuilder();
		for (int i = 0; i < omtpSmsPdus.length; i++) {
			SmsMessage sms = SmsMessage.createFromPdu((byte[]) omtpSmsPdus[i]);
			// TODO: Disable detailed logging after SMS receiver is well tested.
			logMessageDetails(sms);
			messageBody.append(sms.getMessageBody());
			userData.append(extractUserData(sms));
			smsOriginatorNumber = sms.getOriginatingAddress();
		}

		try {
			mSmsParser.parse(userData.toString(), smsOriginatorNumber).visit(this);
		} catch (OmtpParseException exceptionUserData) {
			// Failed to parse the user data. Lets try with message body.
			try {
				mSmsParser.parse(messageBody.toString(), smsOriginatorNumber).visit(this);
			} catch (OmtpParseException exceptionMsgBody) {
				// Failed to parse both. Give up!
				logger.e("Failed to parse userData: " + userData, exceptionUserData);
				logger.e("Failed to parse messageBody: " + messageBody, exceptionMsgBody);
			}
		}
	}

	/**
	 * Method that should be executed when a SMS is received. Timeout handler should be cancelled
	 * and a notification should be broadcast to indicate the notificatoin channel works perfectly.
	 */
	private void notifySmsReceived() {
		mSmsTimeoutHandler.setSmsReceivedState();
		mSourceNotifier.sendNotification(NotifChannelNotification.connectivityOk());
	}

	/**
	 * Extracts the User Data part of the incoming SMS message using the UTF-8 encoding.
	 */
	private String extractUserData(SmsMessage sms) {
		try {
			// OMTP spec does not tell about the encoding. We assume ASCII.
			// UTF-8 sounds safer as it can handle ascii as well as other
			// charsets.
			return new String(sms.getUserData(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("This should have never happened", e);
		}
	}

	@Override
	public void process(String omtpMsgText, String smsOriginatorNumber) {
		try {
			mSmsParser.parse(omtpMsgText, smsOriginatorNumber).visit(this);
		} catch (OmtpParseException e) {
			logger.e("Error while parsing: " + omtpMsgText, e);
		}
	}

	/**
	 * Lookup the event that contains the sync message and process it accordingly. A synchronisation
	 * message can either indicate:
	 * <ul>
	 * <li>A new voicemail: the message is processed and inserted in the local store.</li>
	 * <li>A mailbox update: the remote store has been updated, indicate a synchronisation should be
	 * performed.</li>
	 * <li>A greeting update: the greetings have changed on the remote platform. This case is not
	 * supported for now.</li>
	 * </ul>
	 * 
	 * @param syncMessage
	 *            The synchronisation message that needs to be processed.
	 */
	@Override
	public void visit(OmtpSyncMessage syncMessage) {
		logger.d("Received SYNC message:\n" + syncMessage);
		switch (syncMessage.getSyncTriggerEvent()) {
		case NEW_MESSAGE:
			processNewMessage(syncMessage);
			break;
		case MAILBOX_UPDATE:
			triggerFullSynchronization();
			break;
		case GREETINGS_UPDATE:
			triggerGreetingsDownload();
			break;
		}
	}

	/**
	 * Triggers download of all greetings.
	 */
	private void triggerGreetingsDownload() {
		mSerialSynchronizer.executeGreeting(EnumSet.of(GreetingUpdateType.FETCH_GREETINGS_CONTENT));
		
	}

	/**
	 * Trigger a full synchronization with the remote voicemail repository.
	 */
	private void triggerFullSynchronization() {
		mSerialSynchronizer.execute(SyncFlag.FULL_SYNCHRONIZATION);
	}

	private void processNewMessage(OmtpSyncMessage syncMessage) {
		// Check if the message has been received from the configured provider, if not ignore it.
		if (!syncMessage.getSmsOriginatorNumber().equals(mProviderInfo.getSmsDestinationNumber())) {
			logger.w(String
					.format("Current provider number :%s and SMS message originating number :%s"
							+ " are different. Ignoring the message. Are you using the right server?",
							mProviderInfo.getSmsDestinationNumber(),
							syncMessage.getSmsOriginatorNumber()));
			return;
		}

		String msgId = syncMessage.getId();
		String sender = syncMessage.getSender();
		long duration = 0;
		long time = 0;
		if (syncMessage.hasLength()) {
			duration = syncMessage.getLength();
		}
		if (syncMessage.hasTimestampMillis()) {
			time = syncMessage.getTimestampMillis();
		}

		// Check the type of the message, if configuration allows exclusively voice messages,
		// return if not the case.
		if (StackStaticConfiguration.VOICE_MESSAGES_ONLY
				&& !syncMessage.getContentType().equals(Omtp.ContentType.VOICE)) {
			logger.i(String.format("Sync SMS has been ignored as the message has type : %s",
					syncMessage.getContentType()));
			return;
		}

		OmtpAccountInfo account = mAccountStore.getAccountInfo();
		// Check if an account exits locally and if this account is linked to the current inserted
		// SIM card. The sender can be null within that notification.
		if (account == null) {
			logger.w("No account set up for this SIM. Notifying the user to call his TUI.");
			sendNewMessageNotification(sender, duration, time);
			return;
		} else {
			// notify source that just new message SMS has been received
			MessageNotification.Builder builder = new MessageNotification.Builder();
			builder.justNewMessageDeposited();
			mSourceNotifier.sendNotification(builder.build());
		}

		// If only the sender is not available (unknown caller), we set the number to the TUI.
		if ((sender == null) || (sender.isEmpty())) {
			sender = account.getTuiNumber();
		}

		// if sender, message length(duration) or message time are missing in this case we should
		// set VoicemailContract.Status NOTIFICATION_CHANNEL_STATE to
		// NOTIFICATION_CHANNEL_STATE_MESSAGE_WAITING in the source application
		if ((!syncMessage.hasLength()) || (!syncMessage.hasTimestampMillis()) || (duration == 0)
				|| (time == 0)) {
			sendMessageWaitingNotification();
		}

		// Source name automatically be determined by the content provider.
		String sourcePackageName = null;
		Voicemail voicemail = VoicemailImpl.createForInsertion(time, sender).setDuration(duration)
				.setSourcePackage(sourcePackageName).setSourceData(msgId).build();
		sendInsertRequest(voicemail);
	}

	/**
	 * Method creates and sends a notification to Source application to update
	 * NOTIFICATION_CHANNEL_STATE to NOTIFICATION_CHANNEL_STATE_MESSAGE_WAITING <br>
	 * If sender, message length(duration) or message time are missing in this case we should set
	 * VoicemailContract.Status NOTIFICATION_CHANNEL_STATE to
	 * NOTIFICATION_CHANNEL_STATE_MESSAGE_WAITING in the source application <br>
	 * see {@link android.provider.VoicemailContract.Status}
	 */
	private void sendMessageWaitingNotification() {
		logger.d("Sending Message Waiting notification to Source application");
		mSourceNotifier.sendNotification(NotifChannelNotification.messageWaiting());
	}

	private void sendInsertRequest(Voicemail voicemail) {
		List<Action> actions = new ArrayList<VvmStore.Action>();
		actions.add(VvmStoreActions.insert(voicemail));
		// TODO: We might want to acquire a wake lock around this operation.
		mLocalVvmStore.performActions(actions, new ActionCompletedCallback());
	}

	/**
	 * Callback used after a message has been inserted in the {@link LocalVvmStore} to reflect the
	 * change on {@link VvmStore}. It also triggers a full synchronization.
	 */
	private class ActionCompletedCallback implements Callback<Void> {

		@Override
		public void onSuccess(Void result) {
			logger.i("New message has been inserted successfully.");
		}

		@Override
		public void onFailure(Exception error) {
			// Insertion has failed. Log the error.
			logger.w(String.format("An error has occured while inserting the message: %s",
					error.getMessage()));

			// Synchronize on failure. This exists to work around a bug involving Unisys platforms
			// and long messages (same ID sent for different messages).
			synchronizeIfEnabled();
		}

		/**
		 * Check the configuration parameter that tells if a synchronization should be performed
		 * automatically after a New_Msg notification and trigger the synchronization if active.
		 */
		private void synchronizeIfEnabled() {
			if (StackStaticConfiguration.FULL_SYNC_ON_NEW_MSG) {
				triggerFullSynchronization();
			}
		}
	}

	@Override
	public void visit(OmtpStatusMessage statusMessage) {
		logger.d(String.format("Received STATUS message:%s\n", statusMessage));

		// A status message can include the TUI number, we check here that this number has not been
		// updated since we last stored. If it has, we include this information with the Status
		// notification.
		boolean tuiUpdated = false;
		if (statusMessage.getTuiAccessNumber() != null) {
			OmtpAccountInfo account = mAccountStore.getAccountInfo();
			if (account == null || account.getTuiNumber() == null
					|| !account.getTuiNumber().equals(statusMessage.getTuiAccessNumber())) {
				tuiUpdated = true;
			}
		}

		OmtpAccountInfo.Builder accountInfoBuilder = new OmtpAccountInfo.Builder()
				.setProvisionningStatus(statusMessage.getProvisioningStatus())
				.setSubscriptionUrl(statusMessage.getSubscriptionUrl())
				.setImapServer(statusMessage.getServerAddress())
				.setImapPort(String.valueOf(statusMessage.getImapPort()))
				.setImapUsername(statusMessage.getImapUserName())
				.setImapPassword(statusMessage.getImapPassword())
				.setTuiNumber(statusMessage.getTuiAccessNumber())
				.setSmsNumber(statusMessage.getClientSmsDestinationNumber())
				.setMaxAllowedGreetingsLength(statusMessage.getMaxAllowedGreetingsLength())
				.setMaxAllowedVoiceSignatureLength(statusMessage.getMaxAllowedVoiceSignatureLength())
				.setSupportedLanguages(statusMessage.getSupportedLanguages());

		mAccountStore.updateAccountInfo(accountInfoBuilder);

		// notify the source that the status has been updated.
		sendStatusNotification(statusMessage, tuiUpdated);
	}

	/**
	 * Broadcast a notification notifying of the Status change.
	 * 
	 * @param statusMessage
	 *            Status Message that we parsed.
	 * @param tuiUpdated
	 *            Boolean indicating if the TUI Access number has been updated since the last msg.
	 */
	private void sendStatusNotification(OmtpStatusMessage statusMessage, boolean tuiUpdated) {
		Builder builder = new StatusNotification.Builder();
		if (tuiUpdated) {
			builder.setTuiNumberUpdate(statusMessage.getTuiAccessNumber());
		}
		builder.setProvisioningStatus(statusMessage.getProvisioningStatus());

		mSourceNotifier.sendNotification(builder.build());
	}

	/**
	 * Send a Notification to the source that a new message has arrived. This method should be only
	 * called when no account has been configured, as in that case, the message is directly inserted
	 * in the Android {@link ContentProvider} and the system will notify the user. This notification
	 * includes a few information about the message such as its sender, duration and time.
	 */
	private void sendNewMessageNotification(String sender, long duration, long time) {
		logger.d("Sending notification to source, incoming message but no account");

		MessageNotification.Builder builder = new MessageNotification.Builder();
		builder.setSender(sender).setDuration(duration).setTimestamp(time);
		mSourceNotifier.sendNotification(builder.build());
	}

	private static void logMessageDetails(SmsMessage sms) {
		StringBuilder sb = new StringBuilder();
		addToString(sb, "sender", sms.getOriginatingAddress());
		addToString(sb, "body", sms.getMessageBody());
		addToString(sb, "pdu", sms.getPdu());
		addToString(sb, "userData", sms.getUserData());
		addToString(sb, "msgClass", sms.getMessageClass().toString());
		addToString(sb, "indexOnIcc", sms.getIndexOnIcc());
		addToString(sb, "serviceCenterAddress", sms.getServiceCenterAddress());
		addToString(sb, "status", sms.getStatus());
		addToString(sb, "isCphsMwiMessage", sms.isCphsMwiMessage());
		addToString(sb, "isEmail", sms.isEmail());
		addToString(sb, "isMWIClearMessage", sms.isMWIClearMessage());
		addToString(sb, "isMWISetMessage", sms.isMWISetMessage());
		addToString(sb, "isMwiDontStore", sms.isMwiDontStore());
		addToString(sb, "isReplace", sms.isReplace());
		addToString(sb, "isReplyPathPresent", sms.isReplyPathPresent());
		addToString(sb, "isStatusReportMessage", sms.isStatusReportMessage());

		logger.d(sb.toString());
	}

	private static void addToString(StringBuilder sb, String name, String value) {
		sb.append(name).append(":").append(value).append("\n");
	}

	private static void addToString(StringBuilder sb, String name, int value) {
		sb.append(name).append(":").append(value).append("\n");
	}

	private static void addToString(StringBuilder sb, String name, boolean value) {
		sb.append(name).append(":").append(value).append("\n");
	}

	private static void addToString(StringBuilder sb, String name, byte[] value) {
		sb.append(name).append(":").append(Arrays.toString(value)).append("\n");
	}
}
