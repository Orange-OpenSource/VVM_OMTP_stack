package com.orange.labs.uk.omtp.greetings;

import com.orange.labs.uk.omtp.voicemail.Voicemail;

/**
 * Represents a single greeting stored on Imap server or in local db.
 */
public interface Greeting {

	public boolean isActive();
	
	/**
	 * Returns greeting type object.
	 * 
	 * @return Greeting type object
	 */
	public GreetingType getGreetingType();
	
	public boolean hasVoicemail();

	/**
	 * Returns object associated with this Greeting.
	 * 
	 * @return Voicemail object associated with this Greeting
	 */
	public Voicemail getVoicemail();
	
	/**
	 * Determinate if the voice attachment of a given Greeting has been downloaded from the 
	 * server. It should be false when created the object, and become true when the attachment
	 * fetch has finished.
	 * 
	 * @return true if voice attachment has been successfully downloaded, false otherwise
	 */
	public boolean isVoiceContentDownloaded();
}
