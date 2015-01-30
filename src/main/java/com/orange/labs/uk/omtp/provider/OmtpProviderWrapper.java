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
package com.orange.labs.uk.omtp.provider;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 *	A wrapper around {@link OmtpProviderStore} that automatically retrieves the Network Operator
 *	of the current SIM and gives access to the associated {@link OmtpProviderInfo} if one exists.
 */
public interface OmtpProviderWrapper {

    /**
     * Return the current provider
     */
    @Nullable
    public OmtpProviderInfo getProviderInfo();

    /**
     * @see OmtpProviderStore#getProviderInfoByName(String)
     */
    @Nullable
    public OmtpProviderInfo getProviderInfo(String providerName);

    /**
     * Returns a list of OMTP providers supported by the SIM
     */
    public List<OmtpProviderInfo> getSupportedProviders();

    /**
     * Insert in the storage the provided list of {@link OmtpProviderInfo}.
     * @return
     *
     * @see OmtpProviderStore#updateProviderInfo(OmtpProviderInfo)
     */
    public boolean updateProvidersInfo(ArrayList<OmtpProviderInfo> infos);

    /**
     * Insert/update in the storage the provided {@link OmtpProviderInfo}.
     * @return
     *
     * @see OmtpProviderStore#updateProviderInfo(OmtpProviderInfo)
     */
    public boolean updateProviderInfo(OmtpProviderInfo infos);

    /**
     * @see OmtpProviderStore#removeProviderInfo(OmtpProviderInfo)
     */
    public boolean removeProviderInfo(OmtpProviderInfo infos);

}
