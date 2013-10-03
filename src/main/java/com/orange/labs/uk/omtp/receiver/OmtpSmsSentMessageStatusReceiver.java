package com.orange.labs.uk.omtp.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.orange.labs.uk.omtp.dependency.StackDependencyResolver;
import com.orange.labs.uk.omtp.dependency.StackDependencyResolverImpl;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.notification.NotifChannelNotification;
import com.orange.labs.uk.omtp.notification.SourceNotifier;

/**
 * Broadcast receiver used to handle status of send SMS messages returned by
 * {@link android.telephony.SmsManager#sendDataMessage} method in {@link android.app.PendingIntent}
 * sentIntent
 * 
 * <p>
 * if not NULL this PendingIntent is broadcast when the message is successfully sent, or failed. The
 * result code will be Activity.RESULT_OK for success, or one of these errors:
 * RESULT_ERROR_GENERIC_FAILURE RESULT_ERROR_RADIO_OFF RESULT_ERROR_NULL_PDU For
 * RESULT_ERROR_GENERIC_FAILURE the sentIntent may include the extra "errorCode" containing a radio
 * technology specific value, generally only useful for troubleshooting. The per-application based
 * SMS control checks sentIntent. If sentIntent is NULL the caller will be checked against all
 * unknown applications, which cause smaller number of SMS to be sent in checking period.
 * 
 */
public class OmtpSmsSentMessageStatusReceiver extends BroadcastReceiver {

	private static final Logger logger = Logger.getLogger(OmtpSmsSentMessageStatusReceiver.class);

	@Override
	public void onReceive(Context context, Intent intent) {

		logger.d(String.format("Received intent:%s", intent.getAction()));
		StackDependencyResolver resolver = StackDependencyResolverImpl.getInstance();
		SourceNotifier notifier = resolver.getSourceNotifier();

		if (getResultCode() == Activity.RESULT_OK) {
			notifier.sendNotification(NotifChannelNotification.connectivityOk());
			// schedule new SMS sent timeout
			resolver.getSmsTimeoutHandler().createNewSmsTimeoutTask();
		} else {
			// report SMS connectivity error
			notifier.sendNotification(NotifChannelNotification.connectivityKo(context));
		}
	}

}
