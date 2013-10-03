package com.orange.labs.uk.omtp.sync;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.concurrent.ThreadSafe;

import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.voicemail.Voicemail;
import com.orange.labs.uk.omtp.voicemail.database.MirrorVoicemailProvider;

/**
 * This store is a copy of the remote Omtp Vvm Store. It contains a snapshot of the state of the
 * remote VVM repository taken during the last synchronization.
 * 
 * <p>
 * This store is necessary as Android does not provide the ID, or URI of a message when a Mark As
 * Read or Deletion occurs.
 */
@ThreadSafe
public class MirrorVvmStore implements VvmStore {

	private static final Logger logger = Logger.getLogger(MirrorVvmStore.class);

	/** Allow the execution of asynchronous operation */
	private final Executor mExecutor;

	/** Voicemail provider for the SQLite database storing the voicemails */
	private final MirrorVoicemailProvider mMirrorProvider;

	public MirrorVvmStore(Executor executor, MirrorVoicemailProvider mirrorProvider) {
		mExecutor = executor;
		mMirrorProvider = mirrorProvider;
	}

	@Override
	public void getAllMessages(final Callback<List<Voicemail>> callback) {
		mExecutor.execute(new Runnable() {
			@Override
			public void run() {
				callback.onSuccess(mMirrorProvider.getAllVoicemails());
			}
		});
	}

	@Override
	public void deleteAllMessages(final Callback<Void> callback) {
		mExecutor.execute(new Runnable() {

			@Override
			public void run() {
				int deletions = mMirrorProvider.deleteAll();
				if (deletions > 0) {
					callback.onSuccess(null);
				} else {
					callback.onFailure(new VvmStoreException("Failed to delete all messages"));
				}
			}
		});
	}

	@Override
	public void performActions(final List<Action> actions, final Callback<Void> callback) {
		final Map<VvmStore.Operation, List<Voicemail>> actionsMap = VvmStoreActions
				.buildMap(actions);

		mExecutor.execute(new Runnable() {

			@Override
			public void run() {
				AtomicBoolean failureReported = new AtomicBoolean(false);

				for (VvmStore.Operation operation : actionsMap.keySet()) {
					List<Voicemail> messages = actionsMap.get(operation);
					// No need to check if empty, no operation if not one
					// element at least.
					if (messages.size() > 1) {
						boolean success = performMultipleActions(operation, messages);
						if (!success && !failureReported.getAndSet(true)) {
							callback.onFailure(new VvmStoreException(operation, messages));
						}
					} else {
						Voicemail message = messages.get(0);
						boolean success = performSingleAction(operation, message);
						if (!success && !failureReported.getAndSet(true)) {
							callback.onFailure(new VvmStoreException(operation, message));
						}
					}
				}

				// If nothing has failed, invoke callback success method.
				if (!failureReported.get()) {
					callback.onSuccess(null);
				}
			}

		});

	}

	/**
	 * Perform a single action on the store and uses the callback to indicate of the result.
	 */
	public void performActions(final Action action, final Callback<Void> callback) {
		logger.d(String.format("Performing Single Action: %s", action.toString()));

		mExecutor.execute(new Runnable() {

			@Override
			public void run() {
				Operation operation = action.getOperation();
				Voicemail message = action.getVoicemail();

				if (performSingleAction(operation, message)) {
					logger.d("< Action Succeeded >");
					callback.onSuccess(null);
				} else {
					logger.d("< Action Failed >");
					callback.onFailure(new VvmStoreException(operation, message));
				}
			}

		});
	}

	/**
	 * Perform a single operation on the store, on the specified voicemail.
	 * 
	 * @param operation
	 *            Operation to apply
	 * @param voicemail
	 *            Voicemail to apply on
	 */
	private boolean performSingleAction(Operation operation, Voicemail voicemail) {
		boolean result = false;
		switch (operation) {
		case INSERT:
			result = mMirrorProvider.updateVoicemail(voicemail);
			break;
		case MARK_AS_READ:
			result = mMirrorProvider.markAsRead(voicemail);
			break;
		case DELETE:
			result = mMirrorProvider.delete(voicemail);
			break;
		case FETCH_VOICEMAIL_CONTENT:
			throw new IllegalArgumentException("Fetching content is not supported");
		case FETCH_GREETING_CONTENT:
		case DELETE_GREETING_FILE:
			// nothing to do here, no Greetings mirroring implemented, return true
			result = true;
			break;
		}

		return result;
	}

	/**
	 * Perform an operation on a list of {@link Voicemail}.
	 * 
	 * @param operation
	 *            Operation to apply
	 * @param voicemails
	 *            Voicemails to apply on
	 * @return Boolean indicating if the operation has succeeded.
	 */
	private boolean performMultipleActions(Operation operation, List<Voicemail> voicemails) {
		boolean result = false;
		switch (operation) {
		case INSERT:
			result = mMirrorProvider.updateVoicemails(voicemails);
			break;
		case MARK_AS_READ:
			result = mMirrorProvider.markAsRead(voicemails);
			break;
		case DELETE:
			result = mMirrorProvider.deleteList(voicemails);
			break;
		case FETCH_VOICEMAIL_CONTENT:
			throw new IllegalArgumentException("Fetching content is not supported");
		case FETCH_GREETING_CONTENT:
		case DELETE_GREETING_FILE:
			// nothing to do here, no Greetings mirroring implemented, return true
			result = true;
			break;
		}

		return result;
	}
}
