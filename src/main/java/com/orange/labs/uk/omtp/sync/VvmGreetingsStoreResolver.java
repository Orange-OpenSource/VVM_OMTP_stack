package com.orange.labs.uk.omtp.sync;

import java.util.List;
import java.util.Set;

import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.greetings.Greeting;
import com.orange.labs.uk.omtp.greetings.GreetingUpdateType;
import com.orange.labs.uk.omtp.greetings.GreetingsHelper;

/**
 * Resolves differences between two Greeting messages (stored locally and on remote IMAP server.
 */
public interface VvmGreetingsStoreResolver {

	/**
	 * Resolve all the differences between the {@link Greeting} stored locally in Greetings db
	 * and on remote IMAP server.
	 * 
	 * @param remoteStore
	 * @param localStore
	 * @param result
	 * @param newGreetingType
	 * @param greetingHelper
	 * @param policy ReoslvePolicy
	 */
	public void resolveFullSync(VvmGreetingsStore remoteStore, VvmGreetingsStore localStore,
			Callback<Void> result, Set<GreetingUpdateType> newGreetingType,
			GreetingsHelper greetingsHelper, VvmGreetingsStoreResolver.ResolvePolicy policy);

	/**
	 * A policy for performing the resolution between two VvmGreetingsStore objects.
	 * <p>
	 * You should implement this policy to perform a resolve between two
	 * VvmGreetingsStores. There are three methods you should implement, corresponding to
	 * the case that a given Greeting exists only on one or other store or
	 * exists on both stores. In each case you are given a list of action
	 * objects, to which you can add new actions to perform the sync operation.
	 */
	public interface ResolvePolicy {
		/** Called when the resolve finds a Greeting is only available locally. */
		public void resolveLocalOnlyMessage(Greeting localMessage,
				List<VvmStore.Action> localActions, List<VvmStore.Action> remoteActions);

		/**
		 * Called when the resolve finds a Greeting is only available remotely.
		 */
		public void resolveRemoteOnlyMessage(Greeting remoteMessage,
				List<VvmStore.Action> localActions, List<VvmStore.Action> remoteActions);

		/**
		 * Called in the case that a Greeting is available both locally and
		 * remotely.
		 */
		public void resolveBothLocalAndRemoteMessage(Greeting localMessage,
				Greeting remoteMessage, List<VvmStore.Action> localActions,
				List<VvmStore.Action> remoteActions);
	}

}
