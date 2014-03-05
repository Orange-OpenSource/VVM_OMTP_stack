package com.orange.labs.uk.omtp.sms.timeout;

public interface SmsTimeoutHandler {

    public void setSendingSmsState();

    public void setSentSmsState();

    public void setSmsReceivedState();

}
