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

import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.greetings.Greeting;
import com.orange.labs.uk.omtp.greetings.GreetingType;
import com.orange.labs.uk.omtp.greetings.GreetingUpdateType;
import com.orange.labs.uk.omtp.greetings.GreetingsHelper;

/**
 * VvmStore used to store {@link Greeting} objects
 */
public interface VvmGreetingsStore extends VvmStore {
	
	/**
	 * Gets all the Greetings from local or remote VvmGreetingStores.
	 * 
	 * @param callback
	 */
	public void getAllGreetingsMessages(final Callback<List<Greeting>> callback);

	/**
	 * Executes operations on greetings, such as fetching all greetings content, getting Greetings
	 * list, uploading new greeting.
	 * 
	 * @param callback
	 * @param operationType
	 * @param greetingType
	 */
	public void uploadGreetings(Callback<Greeting> callback, GreetingUpdateType operationType,
			GreetingType greetingType, GreetingsHelper greetingsHelper);
}
