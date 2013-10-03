package com.orange.labs.uk.omtp.notification;

import com.orange.labs.uk.omtp.protocol.Omtp;

import android.os.Bundle;

/**
 * Extends {@link AbstractNotification} to propose Status notification. A status notification should
 * be generated when a notification or status SMS is received from the platform.
 */
public class StatusNotification extends AbstractNotification {

	public static final String PROVISIONING_STATUS_KEY = "provisioning_status";

	public static final String TUI_NUMBER_KEY = "tui_number";

	public static final String SUBSCRIPTION_URL_KEY = "subscription_url";

	protected StatusNotification(Bundle bundle) {
		super(STATUS_ACTION, bundle);
	}

	public static StatusNotification provisioningNotification(Omtp.ProvisioningStatus status) {
		return new Builder().setProvisioningStatus(status).build();
	}

	public static class Builder {

		private Bundle mBundle = new Bundle();

		public Builder setProvisioningStatus(Omtp.ProvisioningStatus status) {
			mBundle.putSerializable(PROVISIONING_STATUS_KEY, status);
			return this;
		}

		public Builder setTuiNumberUpdate(String tuiNumber) {
			mBundle.putString(TUI_NUMBER_KEY, tuiNumber);
			return this;
		}

		public Builder setSubscriptionUrlUpdate(String url) {
			mBundle.putString(SUBSCRIPTION_URL_KEY, url);
			return this;
		}

		public StatusNotification build() {
			return new StatusNotification(mBundle);
		}

	}

}
