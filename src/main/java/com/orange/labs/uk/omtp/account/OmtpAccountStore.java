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

import java.util.List;

import javax.annotation.Nullable;

/**
 * Interface for storing account related data.
 */
public interface OmtpAccountStore {
    /**
     * Updates the account store with supplied account info.
     * <p>
     * It is mandatory to include accountId field in {@link AccountInfo} object.
     *
     * @param accountInfo Account info to be updated
     * @return true if the update was successful, false otherwise
     */
    public boolean updateAccountInfo(OmtpAccountInfo accountInfo);
    
    /**
     * Removes the account for a given accountId (usually MSISDN)
     * 
     * @param accountId
     * @return true if the remove operation was successful, false otherwise
     */
    public boolean removeAccountInfo(String accountId);

    /**
     * Gets the account info associated with the supplied phone number. Returns null if no data
     * associated to this phone number is found.
     *
     * @param accountId The account id for which account info is requested
     */
    @Nullable
    public OmtpAccountInfo getAccountInfo(String accountId);

    /**
     * Retrieves all the accounts from the database and return them within a list.
     */
	public List<OmtpAccountInfo> getAllAccounts();
	
	/**
	 * Deletes all accounts
	 */
	public void deleteAll();
}
