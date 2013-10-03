// Copyright 2011 Google Inc. All Rights Reserved.

package com.android.email.mail.store.imap;

import com.android.email.mail.MessagingException;

/**
 * Responds to a response obtained from the IMAP server.
 */
public interface ImapResponder {
    /**
     * Processes a server response and generates the counter-response to be sent back to the server.
     *
     * @param response the response received from the server.
     * @return the counter-response
     * @throws MessagingException if an error occurs when generating the response
     */
    public String onResponse(ImapResponse response) throws MessagingException;
}
