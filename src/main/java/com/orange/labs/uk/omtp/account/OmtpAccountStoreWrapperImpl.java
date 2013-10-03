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
package com.orange.labs.uk.omtp.account;

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
 * Implementation of {@link OmtpAccountStoreWrapper} that retrieves the Account ID from the Android
 * {@link TelephonyManager} to store and retrieve the account associated to the currently
 * inserted SIM card.
 */
public class OmtpAccountStoreWrapperImpl implements OmtpAccountStoreWrapper {

	private static Logger logger = Logger.getLogger(OmtpAccountStoreWrapperImpl.class);
	
	private OmtpAccountStore mAccountStore;
	
	private OmtpTelephonyManagerProxy mTelephonyManager;

	private ExecutorService mExecutorsService;
	
	public OmtpAccountStoreWrapperImpl(OmtpAccountStore store, OmtpTelephonyManagerProxy tm,
			ExecutorService executorService) {
		mAccountStore = store;
		mTelephonyManager = tm;
		mExecutorsService = executorService;
	}
	
	/**
	 * Uses the currently inserted SIM card MSISDN to update the provided
	 * {@link OmtpAccountInfo.Builder} and stores it in the store.
	 */
	@Override
	public void updateAccountInfo(final OmtpAccountInfo.Builder accountInfoBuilder) {
		final String accountId = getAccountId();
		
		if (accountId != null) {
			accountInfoBuilder.setAccountId(accountId);
			mExecutorsService.execute(new Runnable() {
				
				@Override
				public void run() {
					mAccountStore.updateAccountInfo(accountInfoBuilder.build());
				}
			});
		} else {
			logger.w("It has not been possible to get account ID (users MSISDN). Account info not updated!");
		}
	}

	/**
	 * @see OmtpAccountStore.getAllAccounts()
	 */
	@Override
	public List<OmtpAccountInfo> getAllAccounts() {

		List<OmtpAccountInfo> accountsInfoList = new ArrayList<OmtpAccountInfo>();

		Future<List<OmtpAccountInfo>> future = mExecutorsService
				.submit(new Callable<List<OmtpAccountInfo>>() {

					@Override
					public List<OmtpAccountInfo> call() throws Exception {
						return mAccountStore.getAllAccounts();
					}
				});

		// get the accountsInfoList from the Future
		try {
			accountsInfoList = future.get();
		} catch (InterruptedException e) {
			logger.w("Impossible to get accountsInfoList InterruptedException");
			// Re-assert the thread's interrupted status
			Thread.currentThread().interrupt();
			// We don't need the result, so cancel the task too
			future.cancel(true);
		} catch (ExecutionException e) {
			logger.e("Impossible to get accountsInfoList ExecutionException");
			e.printStackTrace();
		}
		return accountsInfoList;
	}
	
	/**
	 * @see OmtpAccountStore#getAccountInfo(String)
	 */
	@Override
	public OmtpAccountInfo getAccountInfo() {
		final String msisdn = getAccountId();
		OmtpAccountInfo accountInfo = null;
		
		Future<OmtpAccountInfo> future = mExecutorsService.submit(new Callable<OmtpAccountInfo>() {

			@Override
			public OmtpAccountInfo call() throws Exception {
				if (msisdn != null) {
					return mAccountStore.getAccountInfo(msisdn);
				}
				return null;
			}
		});

		// get the accountInfor from the Future
		try {
			accountInfo = future.get();
		} catch (InterruptedException e) {
			logger.w("Impossible to get accountInfo InterruptedException");
			// Re-assert the thread's interrupted status
            Thread.currentThread().interrupt();
            // We don't need the result, so cancel the task too
            future.cancel(true);
		} catch (ExecutionException e) {
			logger.e("Impossible to get accountInfo ExecutionException");
			e.printStackTrace();
		}
		return (msisdn == null) ? null : accountInfo;
	}

	/**
	 * @see OmtpAccountStore#deleteAll();
	 */
	@Override
	public void deleteAll() {
		mAccountStore.deleteAll();
	}

	/**
	 * Retrieves from the {@link TelephonyManager} the current MSISDN that will be used as an 
	 * account id for the future {@link OmtpAccountInfo}.
	 */
	@Nullable
	private String getAccountId() {
		return mTelephonyManager.getSubscriberId();
	}
}
