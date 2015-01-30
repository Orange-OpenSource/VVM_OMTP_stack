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
