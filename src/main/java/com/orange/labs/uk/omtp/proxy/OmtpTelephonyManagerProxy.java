package com.orange.labs.uk.omtp.proxy;

import android.telephony.TelephonyManager;

/**
 * Proxy around the Android {@link TelephonyManager} to help unitary testing features depending
 * of the currently inserted SIM Card.
 */
public interface OmtpTelephonyManagerProxy {

	/**
	 * Returns the current SIM operator retrieved from the currently inserted SIM card. 
	 */
	public String getSimOperator();
	
	/**
	 * Returns the subscriber ID (usually the MSISDN) from the currently inserted SIM card.
	 */
	public String getSubscriberId();
	
}
