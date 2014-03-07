package com.orange.labs.uk.omtp.provider;

import android.os.Bundle;
import android.os.Parcel;

import com.orange.labs.uk.omtp.protocol.Omtp;

import junit.framework.TestCase;

public class OmtpProviderInfoTest extends TestCase {

	private static final String PROVIDER_NAME = "TestProvider";
	private static final Omtp.ProtocolVersion PROTOCOL_VERSION = Omtp.ProtocolVersion.V1_1;
	private static final String CLIENT_TYPE = "TestClient";
	private static final String SMS_DESTINATION_NUMBER = "0123456789";
	private static final short SMS_DESTINATION_PORT = 123;
    private static final String SMS_SERVICE_CENTER = "+449604303495";
    private static final String DATE_FORMAT = "TestDateFormat";
	private static final String NETWORK_OPERATOR = "NetworkOperator";
	private static final boolean IS_CURRENT_PROVIDER = true;

	private OmtpProviderInfo.Builder getProviderInfoBuilderWithValidValues() {
		OmtpProviderInfo.Builder builder = new OmtpProviderInfo.Builder();
		builder.setProviderName(PROVIDER_NAME)
				.setProtocolVersion(PROTOCOL_VERSION)
				.setClientType(CLIENT_TYPE)
				.setSmsDestinationNumber(SMS_DESTINATION_NUMBER)
				.setSmsDestinationPort(SMS_DESTINATION_PORT)
                .setSmsServiceCenter(SMS_SERVICE_CENTER)
				.setDateFormat(DATE_FORMAT)
				.setNetworkOperator(NETWORK_OPERATOR)
				.setIsCurrentProvider(IS_CURRENT_PROVIDER);
		return builder;
	}
	
	private void assertProviderInfoValuesEqualsValidFields(final OmtpProviderInfo providerInfo) {
		assertEquals(PROVIDER_NAME, providerInfo.getProviderName());
		assertEquals(PROTOCOL_VERSION, providerInfo.getProtocolVersion());
		assertEquals(CLIENT_TYPE, providerInfo.getClientType());
		assertEquals(SMS_DESTINATION_NUMBER, providerInfo.getSmsDestinationNumber());
		assertEquals(SMS_DESTINATION_PORT, providerInfo.getSmsDestinationPort());
        assertEquals(SMS_SERVICE_CENTER, providerInfo.getSmsServiceCenter());
        assertEquals(DATE_FORMAT, providerInfo.getDateFormat());
		assertEquals(NETWORK_OPERATOR, providerInfo.getNetworkOperator());
		assertEquals(IS_CURRENT_PROVIDER, providerInfo.isCurrentProvider());
	}
	
	// Test creation with good parameters
	public void testProviderInfoCreation() {
		OmtpProviderInfo providerInfo = getProviderInfoBuilderWithValidValues().build();		
		assertProviderInfoValuesEqualsValidFields(providerInfo);
	}
	
	// Test creation with provider name null value
	public void testProviderInfoCreationWithProviderNameNull() {
		OmtpProviderInfo.Builder builder = getProviderInfoBuilderWithValidValues();
		OmtpProviderInfo providerInfo = builder.setProviderName(null).build();
		
		assertNull(providerInfo);
	}
	
	// Test creation with protocol version null value
	public void testProviderInfoCreationWithProtocolVersionNull() {
		OmtpProviderInfo.Builder builder = getProviderInfoBuilderWithValidValues();
		OmtpProviderInfo providerInfo = builder.setProtocolVersion(null).build();
		
		assertNull(providerInfo);
	}
	
	// Test creation with provider name null value
	public void testProviderInfoCreationWithClientTypeNull() {
		OmtpProviderInfo.Builder builder = getProviderInfoBuilderWithValidValues();
		OmtpProviderInfo providerInfo = builder.setClientType(null).build();
		
		assertNull(providerInfo);
	}
	
	// Test creation with sms destination number null value
	public void testProviderInfoCreationWithSmsDestinationNumberNull() {
		OmtpProviderInfo.Builder builder = getProviderInfoBuilderWithValidValues();
		OmtpProviderInfo providerInfo = builder.setSmsDestinationNumber(null).build();
		
		assertNull(providerInfo);
	}

    // Test creation with sms service center null value
    public void testProviderInfoCreationWithSmsServiceCenterNull() {
        OmtpProviderInfo.Builder builder = getProviderInfoBuilderWithValidValues();
        OmtpProviderInfo providerInfo = builder.setSmsServiceCenter(null).build();

        // Accept null value
        assertNotNull(providerInfo);
    }

	// Test creation with date format null value
	public void testProviderInfoCreationWithDateFormatNull() {
		OmtpProviderInfo.Builder builder = getProviderInfoBuilderWithValidValues();
		OmtpProviderInfo providerInfo = builder.setDateFormat(null).build();
		
		assertNull(providerInfo);
	}
	
	// Test creation with network operator null value
	public void testProviderInfoCreationWithNetworkOperatorNull() {
		OmtpProviderInfo.Builder builder = getProviderInfoBuilderWithValidValues();
		OmtpProviderInfo providerInfo = builder.setNetworkOperator(null).build();
		
		assertNull(providerInfo);
	}
	
	// Test creation with existing provider
	public void testProviderInfoCreationFromExistingProvider() {
		OmtpProviderInfo.Builder builder = getProviderInfoBuilderWithValidValues();
		OmtpProviderInfo providerInfo = builder.build();
		
		OmtpProviderInfo duplicatedProviderInfo = builder.setFieldsFromProvider(providerInfo).build();
		assertProviderInfoValuesEqualsValidFields(duplicatedProviderInfo);
	}
	
	public void testProviderInfoCreationFromExistingProviderNull() {
		OmtpProviderInfo.Builder builder = new OmtpProviderInfo.Builder();
		OmtpProviderInfo duplicatedProviderInfo = builder.setFieldsFromProvider(null).build();
		assertNull(duplicatedProviderInfo);
	}
	
	// Test parcel
	public void testParcelCreation() {
		OmtpProviderInfo providerInfo = getProviderInfoBuilderWithValidValues().build();
		
		Bundle bundle = new Bundle();
		final String KEY_BUNDLE = "KEY_BUNDLE";
		bundle.putParcelable(KEY_BUNDLE, providerInfo);
		
		//Save bundle to parcel
	    Parcel parcel = Parcel.obtain();
	    bundle.writeToParcel(parcel, 0);
	    
	    //Extract bundle from parcel
	    parcel.setDataPosition(0);
	    Bundle bundleDestination = parcel.readBundle();
	    bundleDestination.setClassLoader(OmtpProviderInfo.class.getClassLoader());
	    OmtpProviderInfo providerInfoCreatedFromParcel = bundleDestination.getParcelable(KEY_BUNDLE);

        assertNotNull(providerInfoCreatedFromParcel);

        assertEquals(providerInfo.getProviderName(), providerInfoCreatedFromParcel.getProviderName());
		assertEquals(providerInfo.getProtocolVersion(), providerInfoCreatedFromParcel.getProtocolVersion());
		assertEquals(providerInfo.getClientType(), providerInfoCreatedFromParcel.getClientType());
		assertEquals(providerInfo.getSmsDestinationNumber(), providerInfoCreatedFromParcel.getSmsDestinationNumber());
		assertEquals(providerInfo.getSmsDestinationPort(), providerInfoCreatedFromParcel.getSmsDestinationPort());
        assertEquals(providerInfo.getSmsServiceCenter(), providerInfoCreatedFromParcel.getSmsServiceCenter());
        assertEquals(providerInfo.getDateFormat(), providerInfoCreatedFromParcel.getDateFormat());
		assertEquals(providerInfo.getNetworkOperator(), providerInfoCreatedFromParcel.getNetworkOperator());
		assertEquals(providerInfo.isCurrentProvider(), providerInfoCreatedFromParcel.isCurrentProvider());
		
	}

    // Test parcel
    public void testParcelCreationWithSmscNull() {
        OmtpProviderInfo.Builder builder = getProviderInfoBuilderWithValidValues();
        OmtpProviderInfo providerInfo = builder.setSmsServiceCenter(null).build();

        Bundle bundle = new Bundle();
        final String KEY_BUNDLE = "KEY_BUNDLE";
        bundle.putParcelable(KEY_BUNDLE, providerInfo);

        //Save bundle to parcel
        Parcel parcel = Parcel.obtain();
        bundle.writeToParcel(parcel, 0);

        //Extract bundle from parcel
        parcel.setDataPosition(0);
        Bundle bundleDestination = parcel.readBundle();
        bundleDestination.setClassLoader(OmtpProviderInfo.class.getClassLoader());
        OmtpProviderInfo providerInfoCreatedFromParcel = bundleDestination.getParcelable(KEY_BUNDLE);

        assertNotNull(providerInfoCreatedFromParcel);

        assertEquals(providerInfo.getProviderName(), providerInfoCreatedFromParcel.getProviderName());
        assertEquals(providerInfo.getProtocolVersion(), providerInfoCreatedFromParcel.getProtocolVersion());
        assertEquals(providerInfo.getClientType(), providerInfoCreatedFromParcel.getClientType());
        assertEquals(providerInfo.getSmsDestinationNumber(), providerInfoCreatedFromParcel.getSmsDestinationNumber());
        assertEquals(providerInfo.getSmsDestinationPort(), providerInfoCreatedFromParcel.getSmsDestinationPort());
        assertEquals(providerInfo.getSmsServiceCenter(), providerInfoCreatedFromParcel.getSmsServiceCenter());
        assertNull(providerInfoCreatedFromParcel.getSmsServiceCenter());
        assertEquals(providerInfo.getDateFormat(), providerInfoCreatedFromParcel.getDateFormat());
        assertEquals(providerInfo.getNetworkOperator(), providerInfoCreatedFromParcel.getNetworkOperator());
        assertEquals(providerInfo.isCurrentProvider(), providerInfoCreatedFromParcel.isCurrentProvider());

    }
}
