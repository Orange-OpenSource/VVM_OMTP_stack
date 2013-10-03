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
