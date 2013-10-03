package com.orange.labs.uk.omtp.provider;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.test.AndroidTestCase;

public class SimInfoFromDeviceTest extends AndroidTestCase {
	
	private TelephonyManager mTelephonyManager;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mTelephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
	}
	
	public void testSimOperator () {
		int simState = mTelephonyManager.getSimState();
		String simOperator = mTelephonyManager.getSimOperator();
		
		if(simState == TelephonyManager.SIM_STATE_READY) {
			assertNotNull(simOperator);
			assertFalse(simOperator.length() == 0);
		}
	}
}
