package com.orange.labs.uk.omtp.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;

import com.orange.labs.uk.omtp.config.StackStaticConfiguration;
import com.orange.labs.uk.omtp.logging.Logger;

/**
 * This utility class can be used to figure the reason of network failures. 
 */
public class NetworkManager {
	private static final Logger logger = Logger.getLogger(NetworkManager.class);
	
	private Context mContext;

	private final TelephonyManager mTelephonyManager;
	private final ConnectivityManager mConnectivityManager;

	public NetworkManager(Context context) {
		mContext = context;
		mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	/**
	 * Simply returns a boolean indicating if the phone is the Wi-Fi interface is currently enabled
	 * and connected. If no Wi-Fi interface is present, returns false.
	 * 
	 * @return boolean that indicates the Wi-Fi interface status.
	 */
	public boolean isWifiEnabled() {
		NetworkInfo wifiInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiInfo != null) {
			return wifiInfo.isConnectedOrConnecting();
		}

		return false;
	}
	
	/**
	 * Set up the network so if the Wi-Fi connectivity is enabled, the connection to the IMAP
	 * platforms are redirected through the HiPri mobile data connection.
	 */
	public boolean setUpNetworkIfRequired(String serverAddress) {
		// Wi-Fi is enabled, try to go through HIPRI.
		if (isWifiEnabled()) {
			logger.d("Wi-Fi Enabled - Attempt to switch to HIPRI");

			// Check if HIPRI Mobile Data Connection is connected.
			State state = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI).getState();
			if (0 == state.compareTo(State.CONNECTED) || 0 == state.compareTo(State.CONNECTING)) {
				logger.d("HIPRI Connection already setup, no need to do anything.");
				return routeServerAddressThroughHipri(serverAddress);
			}

			// 0: already enabled, 1: enabled, -1: failure
			int resultInt = mConnectivityManager.startUsingNetworkFeature(
					ConnectivityManager.TYPE_MOBILE, "enableHIPRI");
			if (resultInt == -1) {
				logger.w("Activation of HIPRI has failed.");
				return false;
			} else if (resultInt == 0) {
				logger.d("HIPRI already activated, adding the route.");
				return routeServerAddressThroughHipri(serverAddress);
			}

			// Wait for Hi-Pri to be up & running
			try {
				for (int i = 0; i < StackStaticConfiguration.HIPRI_ACTIVATION_MAX_ATTEMPTS; i++) {
					state = mConnectivityManager.getNetworkInfo(
							ConnectivityManager.TYPE_MOBILE_HIPRI).getState();
					if (state.compareTo(State.CONNECTED) == 0) {
						break;
					}
					Thread.sleep(StackStaticConfiguration.HIPRI_ACTIVATION_DELAY);
				}
			} catch (InterruptedException e) {
				logger.w("InterruptedException thrown while waiting for HIPRI activation");
				// Re-assert the thread's interrupted status
	            Thread.currentThread().interrupt();
			}

			return routeServerAddressThroughHipri(serverAddress);
		}

		return false;
	}

	/**
	 * This method should be executed once the HiPri connection is setup to add a route so the
	 * connection targeting the remote platform goes through the HiPri APN instead of Wi-Fi.
	 */
	private boolean routeServerAddressThroughHipri(String serverAddress) {
		if (serverAddress != null) {
			int ip = ConnectionUtils.lookupHost(serverAddress);
			if (ip != -1) {
				return mConnectivityManager.requestRouteToHost(
						ConnectivityManager.TYPE_MOBILE_HIPRI, ip);
			}
		}

		return false;
	}


	/**
	 * Simply returns a boolean indicating if the phone is currently roaming on the default mobile
	 * data connection. If no data connection is available, returns false.
	 * 
	 * @return boolean that indicates if the phone is currently roaming on the mobile data
	 *         connection.
	 */
	public boolean isRoaming() {
		return mTelephonyManager.isNetworkRoaming()
				&& (mTelephonyManager.getDataState() == TelephonyManager.DATA_DISCONNECTED);
	}

	/**
	 * Simply returns a boolean indicating if the phone is currently in airplane mode.
	 * 
	 * @return boolean that indicates if the phone is currently in airplaine mode.
	 */
	public boolean isInAirplaneMode() {
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = cr.query(System.CONTENT_URI, null, getAirplaneSelection(), null, null);
			return (cursor.getCount() > 0);
		} finally {
			if (cursor != null) {
				CloseUtils.closeQuietly(cursor);
			}
		}
	}

	/**
	 * Simply returns a boolean indicating if a SIM card is currently inserted in the terminal.
	 * 
	 * @return boolean that indicates if the phone has currently a SIM card inserted.
	 */
	public boolean isSimAbsent() {
		return (mTelephonyManager.getSimState() == TelephonyManager.SIM_STATE_ABSENT);
	}

	/**
	 * Returns if the SIM card is ready or not.
	 */
	public boolean isSimReady() {
		String networkOperator = mTelephonyManager.getSimOperator(); 
		Logger.getLogger(NetworkManager.class).d(String.format("Sim Ready: %s", (networkOperator != null && networkOperator.length() > 0)));
		return (networkOperator != null && networkOperator.length() > 0);
	}
	
	/**
	 * Returns the WHERE clause corresponding to an activated airplane mode.
	 * 
	 * @return The WHERE clause corresponding to the activated airplane mode.
	 */
	private String getAirplaneSelection() {
		return DbQueryUtils.concatenateClausesWithAnd(
				DbQueryUtils.getEqualityClause(System.NAME, System.AIRPLANE_MODE_ON),
				DbQueryUtils.getEqualityClause(System.VALUE, "1"));

	}


}
