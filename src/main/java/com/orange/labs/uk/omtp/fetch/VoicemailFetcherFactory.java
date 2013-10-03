/*
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
package com.orange.labs.uk.omtp.fetch;


/**
 * A factory to create voicemail fetcher objects that is appropriate to use for the current
 * "fake mode" settings.
 * <p>
 * It is important that for each request a fresh object is obtained through this interface. This
 * ensures consistent behavior reflecting the current settings.
 */
public interface VoicemailFetcherFactory {
    public VoicemailFetcher createVoicemailFetcher();
}
