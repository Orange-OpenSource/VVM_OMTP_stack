/*
 * Copyright (C) 2012 Orange Labs UK. All Rights Reserved.
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
package com.orange.labs.uk.omtp.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import android.telephony.TelephonyManager;

import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.proxy.OmtpTelephonyManagerProxy;

/**
 *	Implementation of {@link OmtpProviderWrapper} that uses the Android {@link TelephonyManager}
 *	to identify the Network Operator and can be used to return the associated 
 *	{@link OmtpProviderInfo} if it exists.
 */
public class OmtpProviderWrapperImpl implements OmtpProviderWrapper {

	private static final Logger logger = Logger.getLogger(OmtpProviderWrapperImpl.class);
	
	/**
	 * Telephony Manager used to retrieve the Network Operator from the currently
	 * inserted SIM card.
	 */
	private final OmtpTelephonyManagerProxy mTelephonyManager;
	
	/**
	 * Provider used to store and retrieve the {@link OmtpProviderInfo}.
	 */
	private final OmtpProviderStore mProviderStore;

	/**
	 * Thread pool used to launch providers update operations.
	 */
	private final ExecutorService mExecutorService;

	public OmtpProviderWrapperImpl(OmtpProviderStore store, OmtpTelephonyManagerProxy tm,
			ExecutorService executorService) {
		mProviderStore = store;
		mTelephonyManager = tm;
		mExecutorService = executorService;
	}

	/**
	 * @see OmtpProviderStore#getProviderInfoWithNetworkOperator(String)
	 */
	@Nullable
	@Override
	public OmtpProviderInfo getProviderInfo() {
		OmtpProviderInfo retrievedProvider = null;
		String networkOperator = getSimOperator();
		
		// please note that:
		// in some situations e.g. if device is in Airplane mode, the getSimOperator()
		// method will return null because SIM may not be in SIM_STATE_READY,
		// which is the case for example on Nexus S running Android 4.1.2,
		if ((networkOperator == null || networkOperator.length() == 0)) {
			logger.w("For some reason SIM operator has not been retrieved while trying to get" +
					"providerInfo information");
		} else {
			retrievedProvider = mProviderStore.getCurrentProviderInfoWithNetworkOperator(networkOperator);
		}
		
		return retrievedProvider;
	}
	
	/**
	 * Retrieves the Network Operator (MCC/MNC) of the SIM issuer from the {@link TelephonyManager}.
	 */
	private String getSimOperator() {
		return mTelephonyManager.getSimOperator();
	}

	/**
	 * @see OmtpProviderWrapper#updateProvidersInfo(ArrayList)
	 */
	@Override
	public boolean updateProvidersInfo(ArrayList<OmtpProviderInfo> providers) {
		if(providers == null)
			return false;
		
		boolean inserted = true;
		for (OmtpProviderInfo provider : providers) {
			boolean result = updateProviderOnThreadPool(provider);
			inserted = inserted && result;
		}
		return inserted;
	}
	
	/**
	 * @see OmtpProviderWrapper#updateProviderInfo(OmtpProviderInfo)
	 */
	@Override
	public boolean updateProviderInfo(OmtpProviderInfo infos) {
		return updateProviderOnThreadPool(infos);
	}
	
	/**
	 * Updates Provider info using a ThreadPool.
	 * 
	 * @return true if update was successful
	 */
	private boolean updateProviderOnThreadPool(final OmtpProviderInfo provider) {
		
		Future<Boolean> future = mExecutorService.submit(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				return mProviderStore.updateProviderInfo(provider);
			}
		});
				
		try {
			return future.get();
		} catch (InterruptedException e) {
			logger.w("Impossible to get providerInfo InterruptedException");
			// Re-assert the thread's interrupted status
            Thread.currentThread().interrupt();
            // We don't need the result, so cancel the task too
            future.cancel(true);
		} catch (ExecutionException e) {
			logger.e("Impossible to get providerInfo ExecutionException");
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Returns a list of OMTP providers supported by the SIM
	 */
	public List<OmtpProviderInfo> getSupportedProviders() {
		String networkOperator = getSimOperator();
		List<OmtpProviderInfo> listSupportedProviders = new ArrayList<OmtpProviderInfo>();
		
		// please note that:
		// in some situations e.g. if device is in Airplane mode, the getSimOperator()
		// method will return null because SIM may not be in SIM_STATE_READY,
		// which is the case for example on Nexus S running Android 4.1.2,
		if ((networkOperator == null || networkOperator.length() == 0)) {
			logger.w("For some reason SIM operator has not been retrieved while trying to get" +
					"providerInfo information");
		} else {
			listSupportedProviders = mProviderStore.getProvidersInfoWithNetworkOperator(networkOperator);
		}
		
		return listSupportedProviders;
	}
	
	public boolean removeProviderInfo(OmtpProviderInfo infos) {
		return removeProviderInfoOnThreadPool(infos);
	}
	
	private boolean removeProviderInfoOnThreadPool(final OmtpProviderInfo infos) {
		Future<Boolean> future = mExecutorService.submit(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				return mProviderStore.removeProviderInfo(infos);
			}
		});
				
		try {
			return future.get();
		} catch (InterruptedException e) {
			logger.w("Impossible to remove providerInfo InterruptedException");
			// Re-assert the thread's interrupted status
            Thread.currentThread().interrupt();
            // We don't need the result, so cancel the task too
            future.cancel(true);
		} catch (ExecutionException e) {
			logger.e("Impossible to remove providerInfo ExecutionException");
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public OmtpProviderInfo getProviderInfo(String providerName) {
		OmtpProviderInfo retrievedProvider = mProviderStore.getProviderInfo(providerName);
		return retrievedProvider;
	}
	
}
