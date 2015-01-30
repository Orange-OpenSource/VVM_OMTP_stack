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
package com.orange.labs.uk.omtp.account;

import java.util.List;

import javax.annotation.Nullable;

/**
 * A wrapper on {@link OmtpAccountStore} that automatically determines the accountId to be used
 * based on the currently inserted SIM and uses that to call respective methods of
 *  {@link OmtpAccountStore}.
 */
public interface OmtpAccountStoreWrapper {

	 /**
     * Updates the account store with the account info data set in the supplied
     * {@link com.orange.labs.uk.omtp.account.OmtpAccountInfo.Builder}.
     * All necessary fields other than the accountId must be already
     * set in the builder. This method will internally set the accountId for you.
     *
     * @see OmtpAccountStore#updateAccountInfo(OmtpAccountInfo)
     */
    public void updateAccountInfo(OmtpAccountInfo.Builder accountInfoBuilder);

    /** @see OmtpAccountStore#getAccountInfo(String) */
    @Nullable
    public OmtpAccountInfo getAccountInfo();
    
    /** @See AccountStore#getAllAccounts() */
    public List<OmtpAccountInfo> getAllAccounts();
    
    /** @see OmtpAccountStore#deleteAll() */
    public void deleteAll();
    
}
