package com.orange.labs.uk.omtp.sync.policies;

import java.util.List;

import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.sync.VvmStore.Action;
import com.orange.labs.uk.omtp.sync.VvmStoreActions;
import com.orange.labs.uk.omtp.voicemail.LocalVoicemailProvider;
import com.orange.labs.uk.omtp.voicemail.Voicemail;
import com.orange.labs.uk.omtp.voicemail.database.MirrorVoicemailProvider;

/**
 * This Resolve Policy never deletes local messages when they are not found on the remote platform.
 * Instead, it marks that message as read to be sure that no remotely deleted message are marked as
 * new locally.
 */
public class NoLocalDeletionResolvePolicy extends AbstractResolvePolicy {

	private static final Logger logger = Logger.getLogger(NoLocalDeletionResolvePolicy.class);

	public NoLocalDeletionResolvePolicy(LocalVoicemailProvider localDb,
			MirrorVoicemailProvider mirrorDb) {
		super(localDb, mirrorDb);
	}

	/**
	 * If a message is only present locally, it means it has been deleted on the voicemail platform.
	 * This policy does not delete the message locally but instead mark it as read if it has never
	 * been done (as a remotely deleted message can be considered as read).
	 */
	@Override
	public void resolveLocalOnlyMessage(Voicemail localMessage, List<Action> localActions,
			List<Action> remoteActions) {
		logger.d("No Local Deletion Resolve Policy - Ignoring Deletion.");
		if (!localMessage.isRead()) {
			logger.d("Unread message - Marking as read.");
			localActions.add(VvmStoreActions.markAsRead(localMessage));
		}
	}

}
