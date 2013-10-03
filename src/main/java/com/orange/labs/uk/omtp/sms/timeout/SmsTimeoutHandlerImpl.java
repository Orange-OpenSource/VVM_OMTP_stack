package com.orange.labs.uk.omtp.sms.timeout;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.orange.labs.uk.omtp.config.StackStaticConfiguration;
import com.orange.labs.uk.omtp.logging.Logger;

public final class SmsTimeoutHandlerImpl implements SmsTimeoutHandler {
	
	private static final Logger logger = Logger.getLogger(SmsTimeoutHandlerImpl.class);

	private static ScheduledFuture<?> sScheduledSmsTimeoutTask;
	
	@Override
	public synchronized void cancelOldSmsTimeoutTask() {
		// try to cancel previously scheduled SMS timeout tasks
		if (sScheduledSmsTimeoutTask != null) {
			boolean isSmsTimeoutCancelled = sScheduledSmsTimeoutTask.cancel(true);
			if (isSmsTimeoutCancelled) {
				logger.d("Previously scheduled SMS timeout task has been cancelled.");
			}
		}
	}
	
	@Override
	public synchronized void createNewSmsTimeoutTask() {
		// Cancel previously created timeout task.
		cancelOldSmsTimeoutTask();
		
		logger.d(String.format("Scheduling new SMS timeout handler with timeout:%ds.",
				StackStaticConfiguration.SMS_TIMEOUT));
		
		ScheduledExecutorService smsTimeoutScheduler = Executors.newScheduledThreadPool(1);
		Runnable smsSentTimeoutTask = new SmsSentTimeoutTask();
		sScheduledSmsTimeoutTask = smsTimeoutScheduler.schedule(smsSentTimeoutTask,
				StackStaticConfiguration.SMS_TIMEOUT, TimeUnit.SECONDS);
	}


}
