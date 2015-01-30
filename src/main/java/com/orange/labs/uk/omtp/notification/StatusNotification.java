/*
 * Copyright (C) 2012 Orange Labs UK. All Rights Reserved.
 * Copyright (C) 2011 The Android Open Source Project
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
