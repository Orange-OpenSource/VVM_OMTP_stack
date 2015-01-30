/*
 * Copyright (C) 2012 Orange Labs UK. All Rights Reserved.
 * Copyright (C) 2011 The Android Open Source Project
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
package com.orange.labs.uk.omtp.notification;

import android.os.Bundle;

public class XcloseNutNotification extends AbstractNotification {

	protected XcloseNutNotification(Bundle bundle) {
		super(SourceNotification.XCLOSE_NUT_ACTION, bundle);
	}
	
	/**
	 * Generates a notification indicating that the XCLOSE_NUT IMAP request has been 
	 * successfully completed.
	 */
	public static XcloseNutNotification xCloseNutSucces() {
		return new Builder().setXcloseNutStatus(true).build();
	}
	
	/**
	 * Generates a notification indicating that the XCLOSE_NUT IMAP request has failed.
	 */
	public static XcloseNutNotification xCloseNutFailure() {
		return new Builder().setXcloseNutStatus(false).build();
	}
	
	public static class Builder {

		private Bundle mBundle = new Bundle();
		
		public Builder setXcloseNutStatus(boolean status) {
			mBundle.putBoolean(SourceNotification.XCLOSE_NUT_STATUS_KEY, status);
			return this;
		}

		public XcloseNutNotification build() {
			return new XcloseNutNotification(mBundle);
		}
	}
	

}
