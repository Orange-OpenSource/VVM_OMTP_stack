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
