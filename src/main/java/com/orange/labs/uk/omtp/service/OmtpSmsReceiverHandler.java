package com.orange.labs.uk.omtp.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.orange.labs.uk.omtp.config.StackStaticConfiguration;
import com.orange.labs.uk.omtp.dependency.StackDependencyResolverImpl;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.sms.OmtpMessageHandler;

/**
 * Service responsible for handling OMTP-related binary SMS messages the service
 * is started on reception of an SMS message.
 */
public class OmtpSmsReceiverHandler extends IntentService {

	private static final String NAME = "OmtpSmsReceiverHandler";

	private static final Logger logger = Logger.getLogger(OmtpSmsReceiverHandler.class);

	/**
	 * A constructor is required, and must call the super IntentService(String)
	 * constructor with a name for the worker thread.
	 */
	public OmtpSmsReceiverHandler() {
		super(NAME);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		int portNumber = intent.getData().getPort();
		logger.i(intent.getAction() + ", Port: " + portNumber);
		if (portNumber != StackStaticConfiguration.SMS_PORT_FOR_OMTP_RECEIVER) {
			logger.d(String
					.format("Received binary SMS not destinaded for OMTP VVM application (%d), ignoring it.",
							StackStaticConfiguration.SMS_PORT_FOR_OMTP_RECEIVER));
			return;
		} else {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				OmtpMessageHandler messageHandler = StackDependencyResolverImpl.getInstance()
						.createOmtpMessageHandler();
				if (messageHandler != null) {
					messageHandler.process((Object[]) bundle.get("pdus"));
				} else {
					logger.w("Impossible to instantiate a Message Handler");
				}
			}
		}
	}
}
