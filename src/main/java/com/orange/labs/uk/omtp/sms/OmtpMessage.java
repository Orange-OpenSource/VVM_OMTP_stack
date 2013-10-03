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
 * Base interface for all parsed OMTP messages.
 */
public interface OmtpMessage {
    /**
     * OmtpMessage visitor interface. The clients of {@link OmtpMessage} must implement this
     * interface to process different type of Omtp message. When a call to
     * {@link OmtpMessage#visit(Visitor)} is called, the implementation will call the respective
     * visit() method listed here depending on the underlying message type.
     */
    public interface Visitor {
        public void visit(OmtpSyncMessage syncMessage);
        public void visit(OmtpStatusMessage statusMessage);
    }

    /**
     * Invokes the visitors respective visit method.
     *
     * @param visitor The visitor whose method to be invoked
     */
    public void visit(Visitor visitor);
}
