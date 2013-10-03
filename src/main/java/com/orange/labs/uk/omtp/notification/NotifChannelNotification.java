package com.orange.labs.uk.omtp.notification;

import android.content.Context;
import android.os.Bundle;

import com.orange.labs.uk.omtp.utils.NetworkManager;

public class NotifChannelNotification extends AbstractNotification {

	private NotifChannelNotification(Bundle bundle) {
		super(SourceNotification.NOTIF_CHANNEL_ACTION, bundle);
	}

	/**
	 * Generates a notification indicating that the connectivity is okay, ie. the connection was
	 * successful.
	 */
	public static NotifChannelNotification connectivityOk() {
		return new Builder().setConnectivityStatus(ConnectivityStatus.CONNECTIVITY_OK).build();
	}

	/**
	 * Generates a notification indicating that the connectivity is KO, adding the cause of the
	 * failure.
	 * 
	 * @param context
	 *            Used to determine the reason of the failure.
	 */
	public static NotifChannelNotification connectivityKo(Context context) {
		Builder builder = new Builder().setConnectivityStatus(ConnectivityStatus.CONNECTIVITY_KO);
		addErrorCause(context, builder);
		return builder.build();
	}
	
	/**
	 * Is used to create error notification in a situation when SMS message construction has failed
	 * for some reason. Currently implemented in the same way as {@link #connectivityKo(Context)}
	 * 
	 * @param context
	 * @return CONNECTIVITY_KO notification
	 */
	public static NotifChannelNotification messageBuildFailed(Context context) {
		// TODO: Think if more specific implementation of this method is required.
		return connectivityKo(context);
	}

	/**
	 * Notification useful to indicate that a message is waiting on the server. Despite having a
	 * notification, information were missing and the message could not be stored locally.
	 * 
	 * @return MESSAGE_WAITING notification
	 */
	public static NotifChannelNotification messageWaiting() {
		return new Builder().setConnectivityStatus(ConnectivityStatus.MESSAGE_WAITING).build();
	}

	/**
	 * Notification used to indicate that the SMS sending timeout has expired.
	 * 
	 * @return CONNECTIVITY_KO notification
	 */
	public static NotifChannelNotification connectivityTimeout() {
		return new Builder().setConnectivityStatus(ConnectivityStatus.CONNECTIVITY_KO)
				.setErrorCause(ErrorCause.TIMEOUT).build();
	}

	/**
	 * Investigate the cause of the SMS connectivity error.
	 */
	private static void addErrorCause(Context context, Builder builder) {
		NetworkManager monitor = new NetworkManager(context);
		if (monitor.isInAirplaneMode()) {
			builder.setErrorCause(ErrorCause.AIRPLANE);
		} else if (monitor.isSimAbsent()) {
			builder.setErrorCause(ErrorCause.SIM_ABSENT);
		} else {
			builder.setErrorCause(ErrorCause.UNKNOWN);
		}
	}

	public static class Builder {

		private Bundle mBundle = new Bundle();

		public Builder setConnectivityStatus(ConnectivityStatus status) {
			mBundle.putSerializable(SourceNotification.CONNECTIVITY_STATUS_KEY, status);
			return this;
		}

		public Builder setErrorCause(ErrorCause cause) {
			mBundle.putSerializable(SourceNotification.ERROR_CAUSE_KEY, cause);
			return this;
		}

		public NotifChannelNotification build() {
			return new NotifChannelNotification(mBundle);
		}

	}

}
