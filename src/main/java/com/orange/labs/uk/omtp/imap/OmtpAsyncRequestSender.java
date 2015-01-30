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
package com.orange.labs.uk.omtp.imap;

import android.content.Context;

import com.android.email.mail.MessagingException;
import com.orange.labs.uk.omtp.account.OmtpAccountInfo;
import com.orange.labs.uk.omtp.account.OmtpAccountStoreWrapper;
import com.orange.labs.uk.omtp.callbacks.Callback;

import java.util.concurrent.Executor;

import javax.annotation.Nullable;

/**
 *	Issue OMTP request to the platform asynchronously.
 */
public class OmtpAsyncRequestSender implements OmtpRequestSender {

	private final Context mContext;
	private final Executor mExecutor;
	private final OmtpAccountStoreWrapper mAccountStore;

	public OmtpAsyncRequestSender(Context context, Executor executor,
			OmtpAccountStoreWrapper accountStore) {
		mContext = context;
		mExecutor = executor;
		mAccountStore = accountStore;
	}

    //TODO: some methods as this one are shared between command issuer and voicemail fetcher.
    private OmtpAccountInfo getAccountDetails() {
        return mAccountStore.getAccountInfo();
    }

	/**
	 * @see OmtpRequestSender#closeNutRequest(Callback)
	 */
	@Override
	public void closeNutRequest(final Callback<Void> callback) {
		mExecutor.execute(new Runnable() {
			
			@Override
			public void run() {
                final OmtpAccountInfo accountDetails = getAccountDetails();
                if (accountDetails == null) {
                    // Could not fetch account details. Fail!
                    callback.onFailure(new Exception(
                            "closeNutRequest failed, we can't get AccountDetails"));
                    return;
                }

				OmtpImapStore store = getImapStore(accountDetails);
				if (store != null) {
					store.closeNutRequest(callback);
				} else {
					// Notify of the failure.
					callback.onFailure(new IllegalStateException("Impossible to instantiate a new" +
							" OMTP IMAP store."));
				}
			}
		});
	}

	/**
	 * @see OmtpRequestSender#changeTuiPassword(String, String, Callback<Void>)
	 */
	@Override
	public void changeTuiPassword(final String oldPassword, final String newPassword,
			final Callback<Void> callback) {

		mExecutor.execute(new Runnable() {
			
			@Override
			public void run() {
                final OmtpAccountInfo accountDetails = getAccountDetails();
                if (accountDetails == null) {
                    // Could not fetch account details. Fail!
                    callback.onFailure(new Exception(
                            "changeTuiLanguage failed, we can't get AccountDetails"));
                    return;
                }

				OmtpImapStore store = getImapStore(accountDetails);
				if (store != null) {
					store.changeTuiPassword(oldPassword, newPassword, callback);
				} else {
					callback.onFailure(new IllegalStateException("Impossible to instantiate a new" +
							" OMTP IMAP store."));
				}
			}
		});
	}

	
	/**
	 * Returns a new {@link OmtpImapStore} used to send the requests to the server, or null if it
	 * could not be instantiated.
	 * @param accountDetails
	 * 			Account Details that provides the URI pointing the remote OMTP platform.
	 * @return
	 * 			{@link OmtpImapStore} instance that can be used to issue requets, null otherwise.
	 */
	@Nullable
	private OmtpImapStore getImapStore(OmtpAccountInfo accountDetails) {
		try {
			if (accountDetails.getUriString() != null) {
				return OmtpImapStore.newInstance(accountDetails.getUriString(), mContext, null);
			} else {
				return null;
			}
		} catch (MessagingException e) {
			return null;
		}
	}
}
