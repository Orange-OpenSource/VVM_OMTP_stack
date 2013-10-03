package com.orange.labs.uk.omtp.sync;

import java.util.Set;

import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.greetings.GreetingUpdateType;
import com.orange.labs.uk.omtp.greetings.GreetingsHelper;

public class VvmGreetingStoreResolverImpl implements VvmGreetingsStoreResolver {

	@Override
	public void resolveFullSync(VvmGreetingsStore remoteStore, VvmGreetingsStore localStore,
			Callback<Void> result, Set<GreetingUpdateType> newGreetingType,
			GreetingsHelper greetingsHelper, VvmGreetingsStoreResolver.ResolvePolicy policy) {
		new InnerGreetingsResolver(remoteStore, localStore, result, newGreetingType,
				greetingsHelper, policy).resolve();
	}

}
