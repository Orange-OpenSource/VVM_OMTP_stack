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

import com.orange.labs.uk.omtp.sync.VvmStore;
import com.orange.labs.uk.omtp.sync.VvmStoreActions;
import com.orange.labs.uk.omtp.voicemail.LocalVoicemailProvider;
import com.orange.labs.uk.omtp.voicemail.Voicemail;
import com.orange.labs.uk.omtp.voicemail.database.MirrorVoicemailProvider;

/**
 * Resolve policy that fully synchronize voicemails in both direction (remote to local, local to
 * remote). It notably deletes local messages if they cannot be found on the remote voicemail
 * platform.
 */
public class FullSyncResolvePolicy extends AbstractResolvePolicy {

	public FullSyncResolvePolicy(LocalVoicemailProvider localDb, MirrorVoicemailProvider mirrorDb) {
		super(localDb, mirrorDb);
	}

	@Override
	public void resolveLocalOnlyMessage(Voicemail localMessage, List<VvmStore.Action> localActions,
			List<VvmStore.Action> remoteActions) {
		// If the message is no longer on the server, it should be moved out of
		// local inbox.
		localActions.add(VvmStoreActions.delete(localMessage));
	}

}
