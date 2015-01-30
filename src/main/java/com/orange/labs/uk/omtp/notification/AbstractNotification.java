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

public abstract class AbstractNotification implements SourceNotification {

	private String mAction;
	private Bundle mBundle;

	protected AbstractNotification(String action, Bundle bundle) {
		mAction = action;
		mBundle = bundle;
	}

	@Override
	public String getAction() {
		return mAction;
	}

	@Override
	public Bundle getBundle() {
		return mBundle;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("Notification: [mAction: ");
		sb.append(mAction);
		sb.append("; mBundle: {");
		Object value = null;
		for (String key : mBundle.keySet()) {
			if ((value = mBundle.get(key)) != null) {
				sb.append(key);
				sb.append("=");
				sb.append(value.toString());
				sb.append(" , ");
			}
		}
		sb.append(" }]");
		return sb.toString();
	}

}
