package com.orange.labs.uk.omtp.greetings;

import com.android.email.mail.MessagingException;
import com.android.email.mail.internet.MimeMessage;

public interface GreetingCreator {
	
	/**
	 * Build the final message to be sent (or saved). If there is another
	 * message quoted in this one, it will be baked into the final message here.
	 * 
	 * @return Message to be sent.
	 * @throws MessagingException
	 */
	public MimeMessage createMessage() throws MessagingException;

}
