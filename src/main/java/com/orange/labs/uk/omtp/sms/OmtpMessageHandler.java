/*
 * Copyright (C) 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package com.orange.labs.uk.omtp.sms;

/**
 * A simple interface to handle OMTP SMS.
 */
public interface OmtpMessageHandler {
    /** Processes an OMTP message, whose text has been supplied. */
    public void process(String omtpMsgText, String smsOriginatorNumber);

    /**
     * Processes as OMTP sms, whose pdus have been supplied.
     * <p>
     * The method tries to parse both the user data and message body extracted out of the PDUs.
     * Until one of the succeed or both fail.
     *
     * @param omtpSmsPdus Array of pdus. Each pdu object represent one single SMS and is internally
     *            just a raw byte array.
     */
    public void process(Object[] omtpSmsPdus);

}
