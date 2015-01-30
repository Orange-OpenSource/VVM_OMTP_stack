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
package com.orange.labs.uk.omtp.imap;

import android.content.Context;

import com.android.email.mail.MessagingException;
import com.orange.labs.uk.omtp.account.OmtpAccountInfo;
import com.orange.labs.uk.omtp.account.OmtpAccountStoreWrapper;
import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.fetch.VoicemailFetcher;
import com.orange.labs.uk.omtp.greetings.Greeting;
import com.orange.labs.uk.omtp.greetings.GreetingType;
import com.orange.labs.uk.omtp.greetings.GreetingUpdateType;
import com.orange.labs.uk.omtp.greetings.GreetingsHelper;
import com.orange.labs.uk.omtp.notification.SourceNotifier;
import com.orange.labs.uk.omtp.voicemail.Voicemail;
import com.orange.labs.uk.omtp.voicemail.VoicemailPayload;

import java.util.List;
import java.util.concurrent.Executor;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Asynchronous fetcher for voicemail from an IMAP server.
 */
@ThreadSafe
public class AsyncImapVoicemailFetcher implements VoicemailFetcher {
	private final Context mContext;
	private final Executor mExecutor;
	private final OmtpAccountStoreWrapper mAccountStore;
	private final SourceNotifier mSourceNotifier;

	/**
	 * The {@link Context} is required for handing to the underlying imap code,
	 * any context will do, the application context is fine.
	 */
	public AsyncImapVoicemailFetcher(Context context, Executor executor,
			OmtpAccountStoreWrapper accountStore, SourceNotifier notifier) {
		mContext = context;
		mExecutor = executor;
		mAccountStore = accountStore;
		mSourceNotifier = notifier;
	}

    private OmtpAccountInfo getAccountDetailsOrFail(final Callback<?> callback) {
        OmtpAccountInfo accountInfo = mAccountStore.getAccountInfo();
        if (accountInfo == null) {
            callback.onFailure(new MessagingException(
                    "Imap Operation Failed - failed, Cannot get AccountDetails"));
        }

        return accountInfo;
    }

	@Override
	public void fetchAllVoicemails(final Callback<List<Voicemail>> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final OmtpAccountInfo accountDetails = getAccountDetailsOrFail(callback);
                if(accountDetails != null) {
                    new OneshotSyncImapVoicemailFetcher(mContext, accountDetails, mSourceNotifier)
                            .fetchAllVoicemails(callback);
                }
            }
        });
	}

	@Override
	public void fetchVoicemailPayload(final String providerData,
                                      final Callback<VoicemailPayload> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
            final OmtpAccountInfo accountDetails = getAccountDetailsOrFail(callback);
            if (accountDetails != null) {
                new OneshotSyncImapVoicemailFetcher(mContext, accountDetails, mSourceNotifier)
                        .fetchVoicemailPayload(providerData, callback);
            }
            }
        });
	}

	@Override
	public void markVoicemailsAsRead(final Callback<Void> callback, final Voicemail... voicemails) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final OmtpAccountInfo accountDetails = getAccountDetailsOrFail(callback);
                if (accountDetails != null) {
                    new OneshotSyncImapVoicemailFetcher(mContext, accountDetails, mSourceNotifier)
                            .markVoicemailsAsRead(callback, voicemails);
                }
            }
        });
	}

	@Override
	public void markVoicemailsAsDeleted(final Callback<Void> callback,
			final Voicemail... voicemails) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final OmtpAccountInfo accountDetails = getAccountDetailsOrFail(callback);
                if (accountDetails != null) {
                    new OneshotSyncImapVoicemailFetcher(mContext, accountDetails, mSourceNotifier)
                            .markVoicemailsAsDeleted(callback, voicemails);
                }
            }
        });
    }



	@Override
	public void uploadGreetings(final Callback<Greeting> callback,
			final GreetingUpdateType operationType, final GreetingType greetingType,
			final GreetingsHelper greetingsHelper) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final OmtpAccountInfo accountDetails = getAccountDetailsOrFail(callback);
                if (accountDetails != null) {
                    new OneshotSyncImapVoicemailFetcher(mContext, accountDetails, mSourceNotifier)
                            .uploadGreetings(callback, operationType, greetingType, greetingsHelper);
                }
            }
        });
	}

	@Override
	public void fetchAllGreetings(final Callback<List<Greeting>> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final OmtpAccountInfo accountDetails = getAccountDetailsOrFail(callback);
                if (accountDetails != null) {
                    new OneshotSyncImapVoicemailFetcher(mContext, accountDetails, mSourceNotifier)
                            .fetchAllGreetings(callback);
                }
            }
        });
	}

	@Override
	public void fetchGreetingPayload(final Callback<VoicemailPayload> callback,
                                     final Greeting greeting) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final OmtpAccountInfo accountDetails = getAccountDetailsOrFail(callback);
                if (accountDetails != null) {
                    new OneshotSyncImapVoicemailFetcher(mContext, accountDetails, mSourceNotifier)
                            .fetchGreetingPayload(callback, greeting);
                }
            }
        });
	}
}
