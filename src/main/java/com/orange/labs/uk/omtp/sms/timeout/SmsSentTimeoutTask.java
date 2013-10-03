package com.orange.labs.uk.omtp.sms.timeout;

import com.orange.labs.uk.omtp.dependency.StackDependencyResolverImpl;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.notification.NotifChannelNotification;
import com.orange.labs.uk.omtp.notification.SourceNotifier;

/**
 * This {@link Runnable} implementation should be executed once the SMS timeout
 * alarm is triggered. Its purpose is to notify VVM source application of the
 * failure; it uses the OMTP stack notification system for that, sending a
 * {@link ConnectivityNotification}.
 */
// TODO: Should review if that is the best way to do so.
public final class SmsSentTimeoutTask implements Runnable {

	private static final Logger logger = Logger.getLogger(SmsSentTimeoutTask.class);

	private SourceNotifier mNotifier;

	public SmsSentTimeoutTask() {
		mNotifier = StackDependencyResolverImpl.getInstance().getSourceNotifier();
	}

	@Override
	public void run() {
		logger.w("SMS sent timeout expired!, No status SMS message has been received from the server.");
		mNotifier.sendNotification(NotifChannelNotification.connectivityTimeout());
	}
}
