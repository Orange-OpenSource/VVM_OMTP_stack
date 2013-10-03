package com.orange.labs.uk.omtp.sync;

import com.orange.labs.uk.omtp.greetings.GreetingType;
import com.orange.labs.uk.omtp.greetings.GreetingUpdateType;

/**
 * Exception generated when there was a problem with a download of {@link Voicemail} or
 * {@link Greeting} messages from IMAP server.
 */
public class VvmFetchingException extends Exception {

	private static final long serialVersionUID = 5437591465994459948L;
	private final GreetingType mGreetingToActivate;
	private final GreetingUpdateType mGreetingUpdateType;

	// constructor used normally by voicemails fetch
	VvmFetchingException(String message) {
		super(message);
		mGreetingToActivate = null;
		mGreetingUpdateType = null;
	}

	// constructor used normally by Greetings fetch/upload 
	VvmFetchingException(String message, GreetingType greetingToActivate,
			GreetingUpdateType greetingUpdateType) {
		super(message);
		mGreetingToActivate = greetingToActivate;
		mGreetingUpdateType = greetingUpdateType;
	}

	public GreetingType getGreetingToActivate() {
		return mGreetingToActivate;
	}

	public GreetingUpdateType getGreetingUpdateType() {
		return mGreetingUpdateType;
	}

}
