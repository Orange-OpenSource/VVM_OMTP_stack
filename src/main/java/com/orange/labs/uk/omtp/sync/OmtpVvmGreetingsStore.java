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
package com.orange.labs.uk.omtp.sync;

import java.util.List;
import java.util.concurrent.Executor;

import android.content.Context;

import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.fetch.VoicemailFetcherFactory;
import com.orange.labs.uk.omtp.greetings.Greeting;
import com.orange.labs.uk.omtp.greetings.GreetingType;
import com.orange.labs.uk.omtp.greetings.GreetingUpdateType;
import com.orange.labs.uk.omtp.greetings.GreetingsHelper;

public class OmtpVvmGreetingsStore extends OmtpVvmStore implements VvmGreetingsStore {

	private final VoicemailFetcherFactory mVoicemailFetcherFactory;

	public OmtpVvmGreetingsStore(VoicemailFetcherFactory voicemailFetcherFactory,
			Executor executor, Context context, MirrorVvmStore store) {
		super(voicemailFetcherFactory, executor, context, store);
		mVoicemailFetcherFactory = voicemailFetcherFactory;
	}

	@Override
	public void getAllGreetingsMessages(Callback<List<Greeting>> callback) {
		mVoicemailFetcherFactory.createVoicemailFetcher().fetchAllGreetings(callback);
	}

	@Override
	public void uploadGreetings(Callback<Greeting> callback, GreetingUpdateType operationType,
			GreetingType greetingType, GreetingsHelper greetingsHelper) {
		mVoicemailFetcherFactory.createVoicemailFetcher().uploadGreetings(callback, operationType,
				greetingType, greetingsHelper);

	}
	
}
