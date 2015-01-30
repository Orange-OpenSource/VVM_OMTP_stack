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

import android.content.Context;
import android.os.Bundle;

import com.orange.labs.uk.omtp.utils.NetworkManager;

public class ProviderNotification extends AbstractNotification {

	private ProviderNotification(Bundle bundle) {
		super(SourceNotification.PROVIDER_ACTION, bundle);
	}
	
	public static ProviderNotification error(Context context) {
		Builder providerNotification = new Builder();
		addErrorCause(context, providerNotification);
		return providerNotification.build();
	}
	
	private static Builder addErrorCause(Context context, Builder errorNotification) {
		NetworkManager monitor = new NetworkManager(context);
		if (monitor.isInAirplaneMode()) {
			errorNotification.setErrorCause(ErrorCause.AIRPLANE);
		} else if (monitor.isSimAbsent()) {
			errorNotification.setErrorCause(ErrorCause.SIM_ABSENT);
		} else if (!monitor.isSimReady()) {
			errorNotification.setErrorCause(ErrorCause.SIM_NOT_READY);
		} else {
			errorNotification.setErrorCause(ErrorCause.UNKNOWN);
		}
		return errorNotification;
	}
	
	public static class Builder {

		private Bundle mBundle = new Bundle();

		public Builder setErrorCause(ErrorCause cause) {
			mBundle.putSerializable(SourceNotification.ERROR_CAUSE_KEY, cause);
			return this;
		}

		public ProviderNotification build() {
			return new ProviderNotification(mBundle);
		}

	}


}
