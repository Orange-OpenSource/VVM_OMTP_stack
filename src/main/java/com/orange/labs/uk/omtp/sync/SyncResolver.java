/*
 * Copyright (C) 2011 Google Inc. All Rights Reserved.
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
package com.orange.labs.uk.omtp.sync;

import java.util.Set;

import android.content.Context;

import com.orange.labs.uk.omtp.account.OmtpAccountStoreWrapper;
import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.greetings.GreetingUpdateType;

/**
 * Interface to perform omtp source sync between an application specific remote store (omtp server)
 * and local store (voicemail content provider).
 * <p>
 * This can be viewed as a wrapper on {@link VvmStoreResolver} but simplifies its usage by hiding
 * details like what local store, what remote store and what resolve policy has to be used. For a
 * given application these params never change.
 */
public interface SyncResolver {
    /** Sync all messages on both sides. */
    public void syncAllMessages(Callback<Void> callback);

    /** Sync only the changes that have been made locally with the platform. */
    public void syncLocalMessages(Callback<Void> callback);
    
	/** Sync greetings 
	 * @param mAttempts */
	public void syncGreetings(Callback<Void> callback, Set<GreetingUpdateType> newGreetingToUpload);

	/** Update TUI language 
	 * @param mAccountStore 
	 * @param mContext */
	public void updateTuiLanguage(Callback<Void> callback, int mNewLanguage, Context mContext, OmtpAccountStoreWrapper mAccountStore);
}
