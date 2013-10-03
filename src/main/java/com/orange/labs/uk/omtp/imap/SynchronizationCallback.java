package com.orange.labs.uk.omtp.imap;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;

import com.android.email.mail.AuthenticationFailedException;
import com.android.email.mail.MessagingException;
import com.orange.labs.uk.omtp.account.OmtpAccountInfo;
import com.orange.labs.uk.omtp.account.OmtpAccountStoreWrapper;
import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.config.StackStaticConfiguration;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.notification.DataChannelNotification;
import com.orange.labs.uk.omtp.notification.GreetingsErrorNotifications;
import com.orange.labs.uk.omtp.notification.SourceNotification.AuthenticationError;
import com.orange.labs.uk.omtp.notification.SourceNotifier;
import com.orange.labs.uk.omtp.protocol.Omtp.ProvisioningStatus;
import com.orange.labs.uk.omtp.sync.VvmFetchingException;

/**
 * This callback should be used to execute synchronizations. It takes care of posting notifications
 * of failure, authentication error, and success.
 */
public abstract class SynchronizationCallback<T> implements Callback<T> {

	private static final Logger logger = Logger.getLogger(SynchronizationCallback.class);

	private final Context mContext;
	/** Used to notify the source of the connectivity status */
	private final SourceNotifier mNotifier;
	/** Used to update the local account in case of an authentication failure */
	private final OmtpAccountStoreWrapper mAccountStore;
	/** Number of attempts left, we retry until it reaches zero */
	protected final AtomicInteger mAttemptsLeft;
	
	private static final AtomicInteger sVvmFailureCounter= new AtomicInteger(StackStaticConfiguration.MAX_IMAP_ATTEMPTS);

	public SynchronizationCallback(Context context, SourceNotifier notifier,
			OmtpAccountStoreWrapper accountStore, AtomicInteger attempts) {
		mContext = context;
		mNotifier = notifier;
		mAccountStore = accountStore;
		mAttemptsLeft = attempts;
	}

	@Override
	public abstract void onSuccess(T result);

	@Override
	public void onFailure(Exception error) {
		if (authenticationError(error)) {
			logger.d("[Synchronization Failed] Authentication Failure.");
			handleAuthenticationError((AuthenticationFailedException) error);
		} else if (dataChannelError(error)) {
			logger.d("[Synchronization Failed] IMAP Failure.");
			mNotifier.sendNotification(DataChannelNotification.connectivityKo(mContext));
		} else if (vvmFetchingException(error)) {
			logger.d("[Synchronization Failed] fetch/update operation failed.");
		} else {
			logger.d(String.format("[Synchronization Failed] Other Error: %s", error.getMessage()));
		}
	}

	/**
	 * Update the local OMTP account to reflect the platform status for that account as if the
	 * authentication has been refused, it might be linked to change of status.
	 */
	private void handleAuthenticationError(AuthenticationFailedException authException) {
		String alertText = authException.getCause().getMessage().toLowerCase(Locale.UK);
		logger.d("[Authentication Failed] " + alertText);

		OmtpAccountInfo account = mAccountStore.getAccountInfo();
		if (account == null) {
			logger.d("Inexistent account, ignoring update of provisioning status");
			return;
		}

		// If true, the source is advised to send a STATUS SMS to request a newly updated status.
		ProvisioningStatus provStatus = null;
		AuthenticationError error = null;
		if (alertText.contains(OmtpImapConstants.AUTH_USER_BLOCKED)) {
			provStatus = ProvisioningStatus.SUBSCRIBER_BLOCKED;
			error = AuthenticationError.BLOCKED_USER;
		} else if (alertText.contains(OmtpImapConstants.AUTH_USER_UNKNOWN)) {
			provStatus = ProvisioningStatus.SUBSCRIBER_UNKNOWN;
			error = AuthenticationError.UNKNOWN_USER;
		} else if (alertText.contains(OmtpImapConstants.AUTH_INVALID_PASSWORD)) {
			error = AuthenticationError.INVALID_PASSWORD;
		} else if (alertText.contains(OmtpImapConstants.AUTH_NOT_ACTIVATED)) {
			provStatus = ProvisioningStatus.SUBSCRIBER_PROVISIONED;
			error = AuthenticationError.NOT_ACTIVATED;
		} else if (alertText.contains(OmtpImapConstants.AUTH_NOT_INITIALIZED)) {
			provStatus = ProvisioningStatus.SUBSCRIBER_NEW;
			error = AuthenticationError.NOT_INITIALIZED;
		} else if (alertText.contains(OmtpImapConstants.AUTH_NOT_PROVISIONED)) {
			// is there a difference between not provisioned and unknown?
			provStatus = ProvisioningStatus.SUBSCRIBER_UNKNOWN;
			error = AuthenticationError.NOT_PROVISIONED;
		} else if (alertText.contains(OmtpImapConstants.AUTH_UNKNOWN_CLIENT)) {
			error = AuthenticationError.UNKNOWN_CLIENT;
		} else {
			logger.w(String.format("[Authentication Failed] Unknown response: %s", alertText));
			error = AuthenticationError.UNKNOWN;
		}

		// If prov. status is not the same locally, update.
		logger.d(String.format("[Provisioning Status] %s", provStatus));
		if (provStatus != null && !account.getProvisionningStatus().equals(provStatus)) {
			logger.d(String.format(
					"[Authentication Failure] Update account with provisioning status: %s",
					provStatus));
			
			OmtpAccountInfo.Builder builder = new OmtpAccountInfo.Builder()
					.setProvisionningStatus(provStatus);
			mAccountStore.updateAccountInfo(builder);
		}
		
		// Notify the source of the authentication error.
		logger.d(String.format("[Authentication Error] %s", error));
		mNotifier.sendNotification(DataChannelNotification.authenticationFailure(error));
	}

	/**
	 * Verifies if it is necessary to start another thread and retry the IMAP connection.
	 * 
	 * @param error
	 * @return true if retry should be triggered
	 */
	protected boolean shouldRetry(Exception error) {
		logger.d(String.format("In shouldRetry(), checking error:%s", error));
		boolean isItAuthenticationError = authenticationError(error);
		if (isItAuthenticationError || mAttemptsLeft.decrementAndGet() == 0) {
			mAttemptsLeft.set(0); // in case of auth error

			if (isItAuthenticationError) {
				// if authentication error occurred, set vvm Failure counter to 0 
				// therefore shouldRetryGreetingsUpload() should send a notification later
				// (if/when VvmFetchingException is received) to the source about greetings 
				// update issue.
				sVvmFailureCounter.set(0);
				return false;
			}
			
		}
		
		if (vvmFetchingException(error)) {
			return false;
		}

		if (mAttemptsLeft.get() > 0) {
			return true;
		} else {
			mAttemptsLeft.set(0);
			return false;
		}
	}

	/**
	 * Returns if the error that occurred is related to IMAP authentication. If that is the case, a
	 * notification is sent to the source.
	 */
	protected boolean authenticationError(Exception error) {
		return (error.getClass().equals(AuthenticationFailedException.class));
	}
	
	/**
	 * Returns true if that error is a VvmFetch error. Occurred during Greetings or Voicemails
	 * fetch/upload operations.
	 * 
	 * @param error
	 *            Exception object to test
	 * @return true if the Exception type is successfully verified
	 */
	protected boolean vvmFetchingException(Exception error) {
		return (error.getClass().equals(VvmFetchingException.class));
	}
	
	/**
	 * Verifies if it is necessary to send a Greeting error notification to source application
	 * after N (normally N=3) retries, i.e. if all N threads have failed after connection
	 * to IMAP server and if the greeting upload/update action should be repeated.
	 * @param error
	 */
	protected void shouldNotifyAboutGreetingsUploadFailure(VvmFetchingException exception) {
		// check if we can access GreetingUpdate info
		if (exception != null && exception.getGreetingUpdateType() != null) {
			logger.d(String.format(
					"in shouldNotifyAboutGreetingsUploadFailure() with an update type:%s",
					exception.getGreetingUpdateType()));
			logger.i(String
					.format("Is it good moment to send a Greeting Error notification to Source? counter:%s",
							sVvmFailureCounter.getAndDecrement()));
			
			if (sVvmFailureCounter.get() < 1) {
				switch (exception.getGreetingUpdateType()) {
					case ONLY_CHANGE_REQUIRED:
					case UPLOAD_REQUIRED:
						sendGreetingUpdateError(exception);
						break;
							
					case FETCH_GREETINGS_CONTENT:
					case UNKNOWN:
						sendGreetingsFetchingError();
						break;
						
					default:
						logger.d(String.format("Received %s, but no notification will be sent", 
								exception.getGreetingUpdateType()));
						break;
				}
				sVvmFailureCounter.set(StackStaticConfiguration.MAX_IMAP_ATTEMPTS);
			}
		} else {
			logger.d("Received an exception which is not Greetings Releated");
		}
	}

	/**
	 * Sends Greeting update/upload error notification to source application.
	 * Source application can enter "pending greeting upload" state after reception 
	 * of this notification.
	 * @param VvmFetchingException
	 */
	private void sendGreetingUpdateError(VvmFetchingException exception) {
		logger.d("Sending greeting upload/change error notification to source");
		mNotifier.sendNotification(GreetingsErrorNotifications.uploadError(
				exception.getGreetingUpdateType(), exception.getGreetingToActivate())
				.build());
	}
	
	/**
	 * Sends Greetings fetching error notification to source application.
	 */
	private void sendGreetingsFetchingError() {
		logger.d("Sending greetings fetching error notification to source");
		mNotifier.sendNotification(GreetingsErrorNotifications.fetchingError().build());
	}

	/**
	 * Returns if the error that occurred is related to the IMAP exchange/connectivity.
	 */
	protected boolean dataChannelError(Exception error) {
		return (error.getClass().equals(MessagingException.class));
	}

}
