package com.orange.labs.uk.omtp.notification;

/**
 * Interface that defines a notifier for voicemail sources that use the stack. A notifier broadcasts
 * events that reflect status updates and errors from the stack.
 */
public interface SourceNotifier {

	/**
	 * Broadcast the provided notification to subscribers.
	 */
	public void sendNotification(SourceNotification notification);
	
}
