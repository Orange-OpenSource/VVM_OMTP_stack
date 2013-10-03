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

import javax.annotation.Nullable;

import com.orange.labs.uk.omtp.protocol.Omtp;

/**
 * Structured data representation of OMTP STATUS message.
 */
public interface OmtpStatusMessage extends OmtpMessage {
    /**
     * Returns the subscriber's VVM provisiong status.
     *
     * @return null if the field was not set in the message body or it could not be parsed.
     */
    @Nullable
    public Omtp.ProvisioningStatus getProvisioningStatus();

    public boolean hasProvisioningStatus();

    /**
     * Returns the return-code of the status SMS.
     *
     * @return null if the field was not set in the message body or it could not be parsed.
     */
    @Nullable
    public Omtp.ReturnCode getReturnCode();

    public boolean hasReturnCode();

    /**
     * Returns the URL of the voicemail server. This is the URL to send the users to for subscribing
     * to visual voicemail service.
     *
     * @return null if the field was not set in the message body or it could not be parsed.
     */
    @Nullable
    public String getSubscriptionUrl();

    public boolean hasSubscriptionUrl();

    /**
     * Returns the voicemail server address. Either server IP address or fully qualified domain
     * name.
     *
     * @return null if the field was not set in the message body or it could not be parsed.
     */
    @Nullable
    public String getServerAddress();

    public boolean hasServerAddress();

    /**
     * Returns the Telephony User Interface number to call to access voicemails directly from the
     * IVR.
     *
     * @return null if the field was not set in the message body or it could not be parsed.
     */
    @Nullable
    public String getTuiAccessNumber();

    public boolean hasTuiAccessNumber();

    /**
     * Returns the number to which client originated SMSes should be sent to.
     *
     * @return null if the field was not set in the message body or it could not be parsed.
     */
    @Nullable
    public String getClientSmsDestinationNumber();

    public boolean hasClientSmsDestinationNumber();

    /**
     * Returns the IMAP server port to talk to.
     *
     * @return null if the field was not set in the message body or it could not be parsed.
     */
    @Nullable
    public Integer getImapPort();

    public boolean hasImapPort();

    /**
     * Returns the IMAP user name to be used for authentication.
     *
     * @return null if the field was not set in the message body or it could not be parsed.
     */
    @Nullable
    public String getImapUserName();

    public boolean hasImapUserName();

    /**
     * Returns the IMAP password to be used for authentication.
     *
     * @return null if the field was not set in the message body or it could not be parsed.
     */
    @Nullable
    public String getImapPassword();

    public boolean hasImapPassword();

    /**
     * Returns the SMTP server port to talk to.
     *
     * @return null if the field was not set in the message body or it could not be parsed.
     */
    @Nullable
    public String getSmtpPort();

    public boolean hasSmtpPort();

    /**
     * Returns the SMTP user name to be used for SMTP authentication.
     *
     * @return null if the field was not set in the message body or it could not be parsed.
     */
    @Nullable
    public String getSmtpUserName();

    public boolean hasSmtpUserName();

    /**
     * Returns the SMTP password to be used for SMTP authentication.
     *
     * @return null if the field was not set in the message body or it could not be parsed.
     */
    @Nullable
    public String getSmtpPassword();

    public boolean hasSmtpPassword();

    /**
     * Returns the max allowed greetings length in seconds.
     *
     * @return 0 if the field was not set in the message body or it could not be parsed.
     */
    public int getMaxAllowedGreetingsLength();

    public boolean hasMaxAllowedGreetingsLength();

    /**
     * Returns the max allowed voice signature length in seconds.
     *
     * @return 0 if the field was not set in the message body or it could not be parsed.
     */
    public int getMaxAllowedVoiceSignatureLength();

    public boolean hasMaxAllowedVoiceSignatureLength();
    
    /**
     * Returns supported languages list retrieved from status message.
     * 
     * @return null if language information has not been provided.
     */
    public String getSupportedLanguages();
    
    public boolean hasSupportedLanguages();
    
    @Override
    public String toString();
}
