package com.orange.labs.uk.omtp.imap;

import android.content.Context;

import com.orange.labs.uk.omtp.account.OmtpAccountInfo;
import com.orange.labs.uk.omtp.account.OmtpAccountStoreWrapper;
import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.notification.DataChannelNotification;
import com.orange.labs.uk.omtp.notification.SourceNotifier;
import com.orange.labs.uk.omtp.notification.XcloseNutNotification;
import com.orange.labs.uk.omtp.protocol.Omtp.ProvisioningStatus;

/**
 * Main interface with the remote OMTP IMAP platform when needed to send OMTP specific request. A
 * notification is systematically sent to the source that triggered the action when the request has
 * been completed (successfully or not).
 */
public final class OmtpRequestor {

	private static final Logger logger = Logger.getLogger(OmtpRequestor.class);

	private final SourceNotifier mSourceNotifier;

	private final OmtpRequestSender mRequestSender;

	private final Context mContext;
	
	private final OmtpAccountStoreWrapper mAccountStore;

	public OmtpRequestor(SourceNotifier notifier, OmtpRequestSender sender, Context context,
			OmtpAccountStoreWrapper omtpAccountStoreWrapper) {
		mSourceNotifier = notifier;
		mRequestSender = sender;
		mContext = context;
		mAccountStore = omtpAccountStoreWrapper;
	}

	public void closeNutAsync() {
		logger.d("Trying to send XCLOSE_NUT command to IMAP server");
		mRequestSender.closeNutRequest(new OmtpXcloseNutRequestorCallback());
	}

	/**
	 * Dedicated call back class for XCLOSE_NUT operation.
	 */
	private class OmtpXcloseNutRequestorCallback implements Callback<Void> {

		@Override
		public void onSuccess(Void result) {
			logger.d("IMAP request succeeded, sending notification to VVM Source application");
			mSourceNotifier.sendNotification(DataChannelNotification.connectivityOk());
			mSourceNotifier.sendNotification(XcloseNutNotification.xCloseNutSucces());
			// TODO: Check if it is a good idea to do it here (it has been introduced mainly to
			// manage properly pop-up display on HomeActivity)
			// change local current provisioning state to READY after success of XCLOSE_NUT
			// operation
			logger.d("XCLOSE_NUT operation suceeded, changing account status to SUBSCRIBER_READY");
			OmtpAccountInfo.Builder builder = new OmtpAccountInfo.Builder()
					.setProvisionningStatus(ProvisioningStatus.SUBSCRIBER_READY);
			mAccountStore.updateAccountInfo(builder);
		}

		@Override
		public void onFailure(Exception error) {
			logger.e(String.format("IMAP Request error: %s", error.getMessage()));
			mSourceNotifier.sendNotification(DataChannelNotification.connectivityKo(mContext));
//			mSourceNotifier.sendNotification(XcloseNutNotification.xCloseNutFailure());
		}

	}

}
