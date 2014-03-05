package com.orange.labs.uk.omtp.sms.timeout;

import com.orange.labs.uk.omtp.config.StackStaticConfiguration;
import com.orange.labs.uk.omtp.logging.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Class handling the tiggering of a timeout task in case there is a problem of commuication with
 * the SMS server
 */
public final class SmsTimeoutHandlerImpl implements SmsTimeoutHandler {
	
	private static final Logger logger = Logger.getLogger(SmsTimeoutHandlerImpl.class);

    /**
     * Holds the scheduled task supposed to pawn the timeout notification
     */
	private static ScheduledFuture<?> sScheduledSmsTimeoutTask;

    /**
     * Whether a Sms is being sent
     */
    private int mCounterSendingSms = 0;

    /**
     * Number sms declared as sent by the hardware level
     */
    private int mCounterSentSms = 0;

    /**
     * Number of sms received as replies so far
     */
    private int mCounterSmsReceived = 0;

    /**
     * Called when a sms is being sent, initialize the sms counters
     */
    @Override
    public synchronized void setSendingSmsState() {
        logger.d("Setting sending sms state");
        mCounterSendingSms = 1;
        mCounterSentSms = 0;
        mCounterSmsReceived = 0;
    }

    /**
     * Called when a sms is known to be sent
     */
    @Override
    public synchronized void setSentSmsState() {
        logger.d("Setting sent sms state");

        mCounterSentSms++;
        logger.d("Messages with sending state: " + mCounterSendingSms);
        logger.d("Messages with sent state: " + mCounterSentSms);
        logger.d("Messages with received state: " + mCounterSmsReceived);

        // This should not happen, unless there are many sms sent at the same time maybe
        if (mCounterSentSms != mCounterSendingSms) {
            logger.d("Number of message being send and actually sent is different, stop here");
            return;
        }

        // If there is 1 reply missing
        if(mCounterSmsReceived == (mCounterSentSms - 1)) {
            logger.d("A sms reply is expected");
            createNewSmsTimeoutTask();
        }
    }

    /**
     * Called when a sms has been received
     */
    @Override
    public synchronized void setSmsReceivedState() {
        mCounterSmsReceived++;
        logger.d("A sms has been received, number of sms received");
        logger.d("Messages with sending state: " + mCounterSendingSms);
        logger.d("Messages with sent state: " + mCounterSentSms);
        logger.d("Messages with received state: " + mCounterSmsReceived);

        cancelOldSmsTimeoutTask();
    }

    /**
     * This method tries to cancel previously scheduled SMS timeout task.
     */
    private void cancelOldSmsTimeoutTask() {
        // try to cancel previously scheduled SMS timeout tasks
        if (sScheduledSmsTimeoutTask != null) {
            boolean isSmsTimeoutCancelled = sScheduledSmsTimeoutTask.cancel(true);
            if (isSmsTimeoutCancelled) {
                logger.d("Previously scheduled SMS timeout task has been cancelled.");
            }
        }
    }

    /**
     * Create a new SMS timeout task used to notify the application when no
     * response has been received for a request. This timeout should be
     * cancelled when a response is received by the source. This method should
     * be called when stack has received a sent confirmation returned in
     * {@link android.app.PendingIntent} sentIntent by
     * {@link android.telephony.SmsManager#sendTextMessage(String, String,
     * String, android.app.PendingIntent, android.app.PendingIntent)}
     *
     */
    private void createNewSmsTimeoutTask() {

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
