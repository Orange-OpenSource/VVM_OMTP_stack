/*
 * Copyright (C) 2012 Orange Labs UK. All Rights Reserved.
 * Copyright (C) 2011 Google
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

import android.telephony.TelephonyManager;

import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.proxy.OmtpTelephonyManagerProxy;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

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


	public OmtpProviderWrapperImpl(OmtpProviderStore store, OmtpTelephonyManagerProxy tm) {
		mProviderStore = store;
		mTelephonyManager = tm;
	}

    /**
     * Retrieves the Network Operator (MCC/MNC) of the SIM issuer from the {@link TelephonyManager}.
     */
    private String getSimOperator() {
        return mTelephonyManager.getSimOperator();
    }

	/**
	 * @see OmtpProviderStore#getCurrentProviderInfoWithNetworkOperator(String)
	 */
	@Nullable
	@Override
	public synchronized OmtpProviderInfo getProviderInfo() {
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

    @Override
    @Nullable
    public synchronized OmtpProviderInfo getProviderInfo(String providerName) {
        OmtpProviderInfo retrievedProvider = mProviderStore.getProviderInfoByName(providerName);
        return retrievedProvider;
    }

    /**
     * Returns a list of OMTP providers supported by the SIM
     */
    public synchronized List<OmtpProviderInfo> getSupportedProviders() {
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

	/**
	 * @see OmtpProviderWrapper#updateProvidersInfo(ArrayList)
	 */
	@Override
	public synchronized boolean updateProvidersInfo(ArrayList<OmtpProviderInfo> providers) {
		if(providers == null)
			return false;
		
		boolean inserted = true;
		for (OmtpProviderInfo provider : providers) {
			boolean result = updateProviderInfo(provider);
			inserted = inserted && result;
		}
		return inserted;
	}
	
	/**
	 * @see OmtpProviderWrapper#updateProviderInfo(OmtpProviderInfo)
	 */
	@Override
	public synchronized boolean updateProviderInfo(OmtpProviderInfo infos) {
        // If the provider is set a current provider
        if(infos.isCurrentProvider()) {

            // See if there are other providers for the same operator set as current
            List<OmtpProviderInfo> providersOperator =
                    mProviderStore.getProvidersInfoWithNetworkOperator(infos.getNetworkOperator());

            for (java.util.Iterator providerInfoIterator = providersOperator.iterator();
                 providerInfoIterator.hasNext(); ) {

                OmtpProviderInfo providerInfoIter =  (OmtpProviderInfo) providerInfoIterator.next();

                // Other provider, set as current
                if(! providerInfoIter.getProviderName().equals(infos.getProviderName())
                        && providerInfoIter.isCurrentProvider()) {

                    // if yes set it as non current
                    OmtpProviderInfo.Builder builder = new  OmtpProviderInfo.Builder();
                    builder.setFieldsFromProvider(providerInfoIter);
                    builder.setIsCurrentProvider(false);
                    providerInfoIter = builder.build();
                    mProviderStore.updateProviderInfo(providerInfoIter);
                }
            }
        }
        return mProviderStore.updateProviderInfo(infos);
	}

	public synchronized boolean removeProviderInfo(OmtpProviderInfo infos) {
        return mProviderStore.removeProviderInfo(infos);
    }
}
