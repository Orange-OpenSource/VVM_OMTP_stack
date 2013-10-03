/*
 * Copyright (C) 2012 Orange Labs UK. All Rights Reserved.
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
package com.orange.labs.uk.omtp.provider;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.protocol.Omtp;
import com.orange.labs.uk.omtp.protocol.OmtpUtil;

/**
 * An immutable data object representing an OMTP Voicemail Provider. A Provider is defined by the
 * network operator that provides the service. It also contains various settings necessary to
 * configure and activate the service.
 */
public class OmtpProviderInfo implements Parcelable {

	private static Logger sLogger = Logger.getLogger(OmtpProviderInfo.class);

	private String mProviderName;
	private String mNetworkOperator;
	private Omtp.ProtocolVersion mProtocolVersion;
	private String mClientType;
	private String mSmsDestinationNumber;
	private short mSmsDestinationPort;
	private String mDateFormat;
	private boolean mIsCurrentProvider;

	private OmtpProviderInfo(String providerName, String networkOperator,
			Omtp.ProtocolVersion protocolVersion, String clientType, String smsDestinationNumber,
			short smsDestinationPort, String dateFormat, boolean isCurrentProvider) {

		mProviderName = checkNull("Couldn't instantiate OmtpProviderInfo, providerName is missing", providerName);
		mNetworkOperator = checkNull("Couldn't instantiate OmtpProviderInfo, networkOperator is missing", networkOperator);
		mProtocolVersion = checkNull("Couldn't instantiate OmtpProviderInfo, protocolVersion is missing", protocolVersion);
		mClientType = checkNull("Couldn't instantiate OmtpProviderInfo, clientType is missing", clientType);
		mSmsDestinationNumber = checkNull("Couldn't instantiate OmtpProviderInfo, smsDestinationNumber is missing", smsDestinationNumber);
		mSmsDestinationPort = smsDestinationPort;
		mDateFormat = checkNull("Couldn't instantiate OmtpProviderInfo, dateFormat is missing", dateFormat);
		mIsCurrentProvider = isCurrentProvider;
	}

	private static <T> T checkNull(String message, T object) {
		if (object == null) {
			throw new IllegalArgumentException(message);
		}
		return object;
	}

	public String toString() {
		return "OMTP Provider [mNetworkOperator: " + mNetworkOperator + ", mProviderName: "
				+ mProviderName + ", mProtocolVersion: " + mProtocolVersion + ", mClientType: "
				+ mClientType + ", mSmsDestination: " + mSmsDestinationNumber
				+ ", mSmsDestinationPort: " + mSmsDestinationPort + ", mDateFormat: " + mDateFormat
				+ ", mIsCurrentProvider: " + mIsCurrentProvider
				+ "]";
	}

	public String getNetworkOperator() {
		return mNetworkOperator;
	}

	public String getProviderName() {
		return mProviderName;
	}

	public Omtp.ProtocolVersion getProtocolVersion() {
		return mProtocolVersion;
	}

	public String getClientType() {
		return mClientType;
	}

	public String getSmsDestinationNumber() {
		return mSmsDestinationNumber;
	}

	public short getSmsDestinationPort() {
		return mSmsDestinationPort;
	}

	public String getDateFormat() {
		return mDateFormat;
	}
	
	public boolean isCurrentProvider() {
		return mIsCurrentProvider;
	}

	/**
	 * Builder class for {@link OmtpProviderInfo}
	 * <p>
	 * Allow creation of immutable {@link OmtpProviderInfo} objects either by setting individual
	 * field or from a {@link Cursor} object obtained from {@link OmtpProviderDatabase}
	 */
	public static class Builder {
		private String mProviderName;
		private String mNetworkOperator;
		private Omtp.ProtocolVersion mProtocolVersion;
		private String mClientType;
		private String mSmsDestinationNumber;
		private short mSmsDestinationPort;
		private String mDateFormat;
		private boolean mIsCurrentProvider;

		public Builder setProviderName(String providerName) {
			mProviderName = providerName;
			return this;
		}

		public Builder setNetworkOperator(String networkOperator) {
			mNetworkOperator = networkOperator;
			return this;
		}

		public Builder setProtocolVersion(Omtp.ProtocolVersion protocolVersion) {
			mProtocolVersion = protocolVersion;
			return this;
		}

		public Builder setClientType(String clientType) {
			mClientType = clientType;
			return this;
		}

		public Builder setSmsDestinationNumber(String smsDestinationNumber) {
			mSmsDestinationNumber = smsDestinationNumber;
			return this;
		}

		public Builder setSmsDestinationPort(short smsDestinationPort) {
			mSmsDestinationPort = smsDestinationPort;
			return this;
		}

		public Builder setDateFormat(String dateFormat) {
			mDateFormat = dateFormat;
			return this;
		}
		
		public Builder setIsCurrentProvider(boolean isCurrentProvider) {
			mIsCurrentProvider = isCurrentProvider;
			return this;
		}

		public Builder setFieldsFromCursor(Cursor cursor) {
			mProviderName = getStringValueFromCursor(OmtpProviderColumns.PROVIDER_NAME, cursor);
			mNetworkOperator = getStringValueFromCursor(OmtpProviderColumns.NETWORK_OPERATOR,
					cursor);
			mProtocolVersion = OmtpUtil.omtpValueToEnumValue(
					getStringValueFromCursor(OmtpProviderColumns.PROTOCOL_VERSION, cursor),
					Omtp.ProtocolVersion.class);
			mClientType = getStringValueFromCursor(OmtpProviderColumns.CLIENT_TYPE, cursor);
			mSmsDestinationNumber = getStringValueFromCursor(
					OmtpProviderColumns.SMS_DESTINATION_NUMBER, cursor);
			mSmsDestinationPort = getShortValueFromCursor(OmtpProviderColumns.SMS_DESTINATION_PORT,
					cursor);
			mDateFormat = getStringValueFromCursor(OmtpProviderColumns.DATE_FORMAT, cursor);
			mIsCurrentProvider = getBooleanValueFromCursor(OmtpProviderColumns.IS_CURRENT_PROVIDER, cursor);
			return this;
		}

		private String getStringValueFromCursor(OmtpProviderColumns column, Cursor cursor) {
			int columnIndex = cursor.getColumnIndex(column.getColumnName());
			return cursor.getString(columnIndex);
		}

		private short getShortValueFromCursor(OmtpProviderColumns column, Cursor cursor) {
			int columnIndex = cursor.getColumnIndex(column.getColumnName());
			return cursor.getShort(columnIndex);
		}
		
		private boolean getBooleanValueFromCursor(OmtpProviderColumns column, Cursor cursor) {
			int columnIndex = cursor.getColumnIndex(column.getColumnName());
			if(cursor.getInt(columnIndex) > 0) {
				return true;
			}
			return false;
		}
		
		public Builder setFieldsFromProvider(final OmtpProviderInfo providerInfo) {
			if(providerInfo != null) {
				mProviderName = providerInfo.getProviderName();
				mNetworkOperator = providerInfo.getNetworkOperator();
				mProtocolVersion = providerInfo.getProtocolVersion();
				mClientType = providerInfo.getClientType();
				mSmsDestinationNumber = providerInfo.getSmsDestinationNumber();
				mSmsDestinationPort = providerInfo.getSmsDestinationPort();
				mDateFormat = providerInfo.getDateFormat();
				mIsCurrentProvider = providerInfo.isCurrentProvider();
			}
			return this;
		}

		/**
		 * Build and return a {@link OmtpProviderInfo} instance from the information stored in the
		 * builder.
		 * 
		 * @return {@link OmtpProviderInfo} instance or null if the instance could not be built.
		 */
		public OmtpProviderInfo build() {
			OmtpProviderInfo provider = null;
			try {
				provider = new OmtpProviderInfo(mProviderName, mNetworkOperator, mProtocolVersion,
						mClientType, mSmsDestinationNumber, mSmsDestinationPort, mDateFormat, mIsCurrentProvider);
			} catch (IllegalArgumentException e) {
				sLogger.w("Missing required parameter when building a OmtpProviderInfo object");
			}

			return provider;
		}
	}
	
	private OmtpProviderInfo(Parcel in) {
        this(	in.readString(),
        		in.readString(),
        		OmtpUtil.omtpValueToEnumValue(in.readString(), Omtp.ProtocolVersion.class),
				in.readString(),
				in.readString(),
				(short) in.readInt(),
				in.readString(),
				(in.readInt() != 0));
    }

	public static final Parcelable.Creator<OmtpProviderInfo> CREATOR = new Parcelable.Creator<OmtpProviderInfo>() {
		public OmtpProviderInfo createFromParcel(Parcel in) {
			OmtpProviderInfo provider = new OmtpProviderInfo(in);
			sLogger.d("Create provider from parcel: " + provider.toString());
			return provider;
		}

		public OmtpProviderInfo[] newArray(int size) {
			return new OmtpProviderInfo[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mProviderName);
		dest.writeString(mNetworkOperator);
		dest.writeString(mProtocolVersion.getCode());
		dest.writeString(mClientType);
		dest.writeString(mSmsDestinationNumber);
		dest.writeInt((int) mSmsDestinationPort);
		dest.writeString(mDateFormat);
		dest.writeInt((mIsCurrentProvider ? 1:0));
	};

}
