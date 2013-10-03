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
