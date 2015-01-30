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
package com.orange.labs.uk.omtp.proxy;

import javax.annotation.Nullable;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Implementation of {@link OmtpTelephonyManagerProxy} that uses the default Android Telephony
 * Manager to provide the required information.
 */
public class OmtpTelephonyManagerProxyImpl implements OmtpTelephonyManagerProxy {

	/**
	 * Default Android Telephony Manager
	 */
	private TelephonyManager mTelephonyManager;
	
	public OmtpTelephonyManagerProxyImpl(Context context) {
		mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	}

	@Override
	public String getSimOperator() {
		return mTelephonyManager.getSimOperator();
	}

	@Override
	@Nullable
	public String getSubscriberId() {
		return mTelephonyManager.getSubscriberId();
	}
	
}
