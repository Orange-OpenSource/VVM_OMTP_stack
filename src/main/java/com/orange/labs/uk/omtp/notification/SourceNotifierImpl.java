package com.orange.labs.uk.omtp.notification;

import android.content.Context;
import android.content.Intent;

import com.orange.labs.uk.omtp.logging.Logger;

/**
 *	Implementation of {@link SourceNotifier} that uses Android intents to broadcast status and
 *  error update to the system and to voicemail sources. 
 */
public class SourceNotifierImpl implements SourceNotifier {

	private static Logger logger = Logger.getLogger(SourceNotifierImpl.class);
	
	private Context mContext;

	public SourceNotifierImpl(Context context) {
		mContext = context;
	}

	/**
	 * Broadcasts an {@link Intent} containing the notification and its parameters (stored in a
	 * bundle).
	 */
	@Override
	public void sendNotification(SourceNotification notification) {
		logger.d(String.format("Broadcasting notification: %s", notification));
		
		Intent intent = new Intent(notification.getAction());
		intent.putExtras(notification.getBundle());
		mContext.sendOrderedBroadcast(intent, null); // no permission required
	}
	
}
