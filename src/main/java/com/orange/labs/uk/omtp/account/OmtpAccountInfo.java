/*
 * Copyright (C) 2012 Orange Labs UK. All Rights Reserved.
 * Copyright (C) 2011 Google
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
package com.orange.labs.uk.omtp.account;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.annotation.Nullable;

import android.database.Cursor;

import com.orange.labs.uk.omtp.protocol.Omtp.ProvisioningStatus;
import com.orange.labs.uk.omtp.protocol.OmtpUtil;

/**
 * Store the information related to an OMTP account.
 * 
 * These accounts are stored in a dedicated database.
 */
public class OmtpAccountInfo {

	// TODO: Decide if not better to use some int fields instead of String
	/**
	 * Account ID (usually MSISDN), identifies the account. Mandatory.
	 */
	private final String mAccountId;
	private final String mImapUsername;
	private final String mImapPassword;
	private final String mImapServer;
	private final String mImapPort;
	private final String mSmsNumber;
	private final String mTuiNumber;
	private final String mSubscriptionUrl;
	private final ProvisioningStatus mProvisionningStatus;
	private final int mMaxAllowedGreetingsLength;
	private final int mMaxAllowedVoiceSignatureLength;
	private final String mSupportedLanguages;

	private OmtpAccountInfo(String mAccountId, String mImapUsername, String mImapPassword,
			String mImapServer, String mImapPort, String mSmsNumber, String mTuiNumber,
			String mSubscriptionUri, ProvisioningStatus mProvisionningStatus, int maxAllowedGreetingsLength,
			int maxAllowedVoiceSignatureLength, String mSupportedLanguages) {

		// check if provided values are not null
		if (mAccountId == null) {
			throw new IllegalStateException("Couldn't instantiate OmtpAccountInfo, Missing required parameter: mAccoundId is null");
		}

		this.mAccountId = mAccountId;
		this.mImapUsername = mImapUsername;
		this.mImapPassword = mImapPassword;
		this.mImapServer = mImapServer;
		this.mImapPort = mImapPort;
		this.mSmsNumber = mSmsNumber;
		this.mTuiNumber = mTuiNumber;
		this.mSubscriptionUrl = mSubscriptionUri;
		this.mProvisionningStatus = mProvisionningStatus;
		this.mMaxAllowedGreetingsLength = maxAllowedGreetingsLength;
		this.mMaxAllowedVoiceSignatureLength = maxAllowedVoiceSignatureLength;
		this.mSupportedLanguages = mSupportedLanguages;
	}

	@Override
	public String toString() {
		return "OmtpAccountInfo [mAccountId=" + mAccountId + ", mImapUsername=" + mImapUsername
				+ ", mImapPassword=" + mImapPassword + ", mImapServer=" + mImapServer
				+ ", mImapPort=" + mImapPort + ", mSmsNumber=" + mSmsNumber + ", mTuiNumber="
				+ mTuiNumber + ", mSubscriptionUri=" + mSubscriptionUrl + ", mProvisionningStatus="
				+ mProvisionningStatus + ", mMaxAllowedGreetingsLength=" + mMaxAllowedGreetingsLength 
				+ ", mMaxAllowedVoiceSignatureLength=" + mMaxAllowedVoiceSignatureLength 
				+ ", mSupportedLanguages=" + mSupportedLanguages + "]";
	}

	public String getAccountId() {
		return mAccountId;
	}

	public String getImapUsername() {
		return mImapUsername;
	}

	public String getImapPassword() {
		return mImapPassword;
	}

	public String getImapServer() {
		return mImapServer;
	}

	public String getImapPort() {
		return mImapPort;
	}

	public String getSmsNumber() {
		return mSmsNumber;
	}

	public String getTuiNumber() {
		return mTuiNumber;
	}

	public String getSubscriptionUrl() {
		return mSubscriptionUrl;
	}

	public ProvisioningStatus getProvisionningStatus() {
		return mProvisionningStatus;
	}

	public int getMaxAllowedGreetingsLength() {
		return mMaxAllowedGreetingsLength;
	}
	
	public int getMaxAllowedVoiceSignatureLength() {
		return mMaxAllowedVoiceSignatureLength;
	}
	
	public String getSupportedLnaguages() {
		return mSupportedLanguages;
	}
	
	public boolean hasSupportedLnaguages() {
		return (mSupportedLanguages != null);
	}
	
	public boolean hasImapUsername() {
		return (mImapUsername != null);
	}

	public boolean hasImapPassword() {
		return (mImapPassword != null);
	}

	public boolean hasImapServer() {
		return (mImapServer != null);
	}

	public boolean hasImapPort() {
		return (mImapPort != null);
	}
	
	public boolean hasClientSmsDestinationNumber() {
		return mSmsNumber != null;
	}

	public boolean hasTuiNumber() {
		return (mTuiNumber != null);
	}

	public boolean hasSubscriptionUrl() {
		return (mSubscriptionUrl != null);
	}

	public boolean hasProvisioningStatus() {
		return (mProvisionningStatus != null);
	}

	@Nullable
	public String getUriString() {
		// TODO: See SSL encryption
		// String scheme = mUseSsl ? "imap+ssl" : "imap";
		if (!hasImapServer() || !hasImapPort() || !hasImapUsername() || !hasImapPassword()) {
			return null;
		}

		return new StringBuilder().append("imap").append("://")
				.append(urlEncode(getImapUsername())).append(":").append(getImapPassword())
				.append("@").append(getImapServer()).append(":").append(getImapPort()).toString();
	}

	private String urlEncode(String input) {
		try {
			return URLEncoder.encode(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new Error("Not possible, all JVMs support UTF-8");
		}
	}

	/**
	 * Builder class for {@link OmtpAccountInfo} <b>
	 * 
	 */
	public static class Builder {

		private String mAccountId;
		private String mImapUsername;
		private String mImapPassword;
		private String mImapServer;
		private String mImapPort;
		private String mSmsNumber;
		private String mTuiNumber;
		private String mSubscriptionUrl;
		private ProvisioningStatus mProvisionningStatus;
		private int mMaxAllowedGreetingsLength;
		private int mMaxAllowedVoiceSignatureLength;
		private String mSupportedLanguages;

		public Builder setAccountId(String accountId) {
			mAccountId = accountId;
			return this;
		}

		public Builder setImapUsername(String imapUsername) {
			mImapUsername = imapUsername;
			return this;
		}

		public Builder setImapPassword(String imapPassword) {
			mImapPassword = imapPassword;
			return this;
		}

		public Builder setImapServer(String imapServer) {
			mImapServer = imapServer;
			return this;
		}

		public Builder setImapPort(String imapPort) {
			mImapPort = imapPort;
			return this;
		}

		public Builder setSmsNumber(String smsNumber) {
			mSmsNumber = smsNumber;
			return this;
		}

		public Builder setTuiNumber(String tuiNumber) {
			mTuiNumber = tuiNumber;
			return this;
		}

		public Builder setSubscriptionUrl(String subscriptionUrl) {
			mSubscriptionUrl = subscriptionUrl;
			return this;
		}

		public Builder setProvisionningStatus(ProvisioningStatus provisionningStatus) {
			mProvisionningStatus = provisionningStatus;
			return this;
		}
		
		public Builder setMaxAllowedGreetingsLength(int maxAllowedGreetingsLength) {
			mMaxAllowedGreetingsLength = maxAllowedGreetingsLength;
			return this;
		}
		
		public Builder setMaxAllowedVoiceSignatureLength(int maxAllowedVoiceSignatureLength) {
			mMaxAllowedVoiceSignatureLength = maxAllowedVoiceSignatureLength;
			return this;
		}
		
		public Builder setSupportedLanguages(String supportedLanguages) {
			mSupportedLanguages = supportedLanguages;
			return this;
		}

		public Builder setFieldsFromCursor(Cursor cursor) {
			mAccountId = getStringValueFromCursor(OmtpAccountColumns.ACCOUNT_ID, cursor);
			mImapUsername = getStringValueFromCursor(OmtpAccountColumns.IMAP_USERNAME, cursor);
			mImapPassword = getStringValueFromCursor(OmtpAccountColumns.IMAP_PASSWORD, cursor);
			mImapServer = getStringValueFromCursor(OmtpAccountColumns.IMAP_SERVER, cursor);
			mImapPort = getStringValueFromCursor(OmtpAccountColumns.IMAP_PORT, cursor);
			mSmsNumber = getStringValueFromCursor(OmtpAccountColumns.SMS_NUMBER, cursor);
			mTuiNumber = getStringValueFromCursor(OmtpAccountColumns.TUI_NUMBER, cursor);
			mSubscriptionUrl = getStringValueFromCursor(OmtpAccountColumns.SUBSCRIPTION_URL, cursor);
			mMaxAllowedGreetingsLength = getIntValueFromCursor(
					OmtpAccountColumns.MAX_ALLOWED_GREETINGS_LENGTH, cursor);
			mMaxAllowedVoiceSignatureLength = getIntValueFromCursor(
					OmtpAccountColumns.MAX_ALLOWED_VOICESIGNATURE_LENGTH, cursor);
			mSupportedLanguages = getStringValueFromCursor(OmtpAccountColumns.SUPPORTED_LANGUAGES, 
					cursor);
			

			String code = getStringValueFromCursor(OmtpAccountColumns.PROVISIONING_STATUS, cursor);
			mProvisionningStatus = OmtpUtil.omtpValueToEnumValue(code, ProvisioningStatus.class);

			return this;
		}

		@Nullable
		private String getStringValueFromCursor(OmtpAccountColumns column, Cursor cursor) {
			int columnIndex = cursor.getColumnIndex(column.getColumnName());
			if (columnIndex != -1) {
				return cursor.getString(columnIndex);
			} else {
				return null;
			}
		}

		private int getIntValueFromCursor(OmtpAccountColumns column, Cursor cursor) {
			int columnIndex = cursor.getColumnIndex(column.getColumnName());
			if (columnIndex != -1) {
				return cursor.getInt(columnIndex);
			} else {
				return 0;
			}
		}
		
		public OmtpAccountInfo build() {
			return new OmtpAccountInfo(mAccountId, mImapUsername, mImapPassword, mImapServer,
					mImapPort, mSmsNumber, mTuiNumber, mSubscriptionUrl, mProvisionningStatus,
					mMaxAllowedGreetingsLength, mMaxAllowedVoiceSignatureLength, mSupportedLanguages);
		}

	}
}
