package com.orange.labs.uk.omtp.notification;

import android.os.Bundle;

/**
 * IMAP Quota exceeded notifications
 * <br>
 * Every time a synchronisation is started, a QUOTA should be sent.
 * <br>
 * - If > 80%, consider the box as almost full.
 * - if > 97%, consider the box as full.
 */
public class QuotaNotification extends AbstractNotification {

	public static final String QUOTA_VALUE_KEY = "quota_value";
	
	private QuotaNotification(Bundle bundle) {
		super(QUOTA_ACTION, bundle);
	}

	public static class Builder {
		
		private Bundle mBundle = new Bundle();
		
		public Builder setQuotaValue(int value) {
			mBundle.putInt(QUOTA_VALUE_KEY, value);
			return this;
		}
		
		public QuotaNotification build() {
			return new QuotaNotification(mBundle);
		}

	}

	public static SourceNotification quotaValue(int quota) {
		return new Builder().setQuotaValue(quota).build();
	}
}
