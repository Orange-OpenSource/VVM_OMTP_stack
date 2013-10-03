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
