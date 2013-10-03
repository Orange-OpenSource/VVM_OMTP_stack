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
