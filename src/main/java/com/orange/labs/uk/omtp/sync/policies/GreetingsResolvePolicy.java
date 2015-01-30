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
package com.orange.labs.uk.omtp.sync.policies;

import java.util.List;

import com.orange.labs.uk.omtp.dependency.StackDependencyResolver;
import com.orange.labs.uk.omtp.dependency.StackDependencyResolverImpl;
import com.orange.labs.uk.omtp.greetings.Greeting;
import com.orange.labs.uk.omtp.greetings.GreetingsHelper;
import com.orange.labs.uk.omtp.greetings.database.LocalGreetingsProvider;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.sync.VvmGreetingsStoreResolver.ResolvePolicy;
import com.orange.labs.uk.omtp.sync.VvmStore.Action;
import com.orange.labs.uk.omtp.sync.VvmStoreActions;

public class GreetingsResolvePolicy implements ResolvePolicy {

	private static final Logger logger = Logger.getLogger(GreetingsResolvePolicy.class);
	private final LocalGreetingsProvider mLocalDb;

	public GreetingsResolvePolicy(LocalGreetingsProvider localGreetingsProvider) {
		mLocalDb = localGreetingsProvider;
	}

	@Override
	public void resolveLocalOnlyMessage(Greeting localMessage, List<Action> localActions,
			List<Action> remoteActions) {
		// If the message is no longer on the server, it should be moved out of
		// local db.
		localActions.add(VvmStoreActions.delete(localMessage));
	}

	@Override
	public void resolveRemoteOnlyMessage(Greeting remoteMessage, List<Action> localActions,
			List<Action> remoteActions) {
		String sourceData = remoteMessage.getVoicemail().getSourceData();
		if (sourceData != null) {
			if (mLocalDb.getGreetingWithUid(sourceData) == null) {
				logger.d("[Remote Greeting Message] Inserting greeting; Absent from Remote Greeting Store.");
				localActions.add(VvmStoreActions.insert(remoteMessage));
				// add an action to fetch Greeting content as well, replacing current audio 
				// files with the new audio files from the server
				remoteActions.add(VvmStoreActions.fetchGreetingContent(remoteMessage));
			} 
		} else {
			logger.w("[Remote Greeting Message] Greeting does not have a source data.");
		}

	}

	@Override
	public void resolveBothLocalAndRemoteMessage(Greeting localMessage, Greeting remoteMessage,
			List<Action> localActions, List<Action> remoteActions) {

		// check is given Greeting has voice attachment downloaded
		// if not, request Greeting Content Fetch
		if (!localMessage.isVoiceContentDownloaded()) {
			logger.d(String.format("It looks like a Greeting is not downloaded %s adding FETCH action", localMessage));
			remoteActions.add(VvmStoreActions.fetchGreetingContent(localMessage));
		} else {
			
			// check if the file is not empty, due to some unpredictable errors...
			// get greetings helper in order to define if the saved greeting message is empty or not
			StackDependencyResolver resolver = StackDependencyResolverImpl.getInstance();
			GreetingsHelper greetingsHelper = resolver.getGreetingsHelper();
			
			// if message has no content add an action to fetch it's content
			if (greetingsHelper.getGreetingFileSize(localMessage.getGreetingType()) < 2) {
				logger.d("Greeting file is empty adding FETCH action");
				remoteActions.add(VvmStoreActions.fetchGreetingContent(localMessage));
			}

		}
	}

}
