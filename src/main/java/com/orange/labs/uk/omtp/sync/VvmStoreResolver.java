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

import java.util.List;

import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.sync.VvmStore.Action;
import com.orange.labs.uk.omtp.voicemail.Voicemail;

/**
 * Resolves differences between two voicemail message stores.
 */
public interface VvmStoreResolver {
	/**
	 * Perform a full resolve between local and remote stores with the supplied
	 * resolve policy.
	 * <p>
	 * You would call this method when it is indicated that you need to, such as
	 * via the receipt of an SMS message in the case of an OMTP remote source.
	 */
	public void resolveFullSync(VvmStore local, VvmStore remote, VvmStore mirror,
			VvmStoreResolver.ResolvePolicy policy, Callback<Void> result);

	/**
	 * Perform a specific synchronization by detecting changes between the
	 * mirror store and the local store and synchronization these specific
	 * changes with the remote store.
	 */
	public void resolveSpecificSync(VvmStore local, VvmStore remote, VvmStore mirror,
			ResolvePolicy policy, Callback<Void> result);

	/**
	 * A policy for performing the resolution between two VvmStore objects.
	 * <p>
	 * You should implement this policy to perform a resolve between two
	 * VvmStores. There are three methods you should implement, corresponding to
	 * the case that a given voicemail exists only on one or other store or
	 * exists on both stores. In each case you are given a list of action
	 * objects, to which you can add new actions to perform the sync operation.
	 */
	public interface ResolvePolicy {
		/** Called when the resolve finds a Voicemail is only available locally. */
		public void resolveLocalOnlyMessage(Voicemail localMessage,
				List<VvmStore.Action> localActions, List<VvmStore.Action> remoteActions);

		/**
		 * Called when the resolve finds a Voicemail is only available remotely.
		 */
		public void resolveRemoteOnlyMessage(Voicemail remoteMessage,
				List<VvmStore.Action> localActions, List<VvmStore.Action> remoteActions);

		/**
		 * Called in the case that a Voicemail is available both locally and
		 * remotely.
		 */
		public void resolveBothLocalAndRemoteMessage(Voicemail localMessage,
				Voicemail remoteMessage, List<VvmStore.Action> localActions,
				List<VvmStore.Action> remoteActions);

		/**
		 * Called in the case that a Voicemail is available both locally and in
		 * the mirror store.
		 */
		public void resolveBothLocalAndMirrorMessage(Voicemail localMessage,
				Voicemail mirrorMessage, List<Action> remoteActions);

		/**
		 * Called in the case that a Voicemail is only available on the mirror
		 * store but not locally.
		 * 
		 * @param mirrorMessage
		 * @param remoteActions
		 */
		public void resolveMirrorOnlyMessage(Voicemail mirrorMessage, List<Action> remoteActions);
	}
}
