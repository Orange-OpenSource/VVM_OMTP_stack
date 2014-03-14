package com.orange.labs.uk.omtp.provider;

import android.test.AndroidTestCase;
import android.util.Log;

import com.orange.labs.uk.omtp.dependency.StackDependencyResolverImpl;
import com.orange.labs.uk.omtp.protocol.Omtp;

import java.util.List;

public class OmtpProviderDatabaseTest extends AndroidTestCase {

	private static final String PROVIDER_NAME = "TestProvider";
	private static final Omtp.ProtocolVersion PROTOCOL_VERSION = Omtp.ProtocolVersion.V1_1;
	private static final String CLIENT_TYPE = "TestClient";
	private static final String SMS_DESTINATION_NUMBER = "0123456789";
	private static final short SMS_DESTINATION_PORT = 123;
    private static final String SMS_SERVICE_CENTER = "+4405948020";
    private static final String DATE_FORMAT = "TestDateFormat";
	private static final String NETWORK_OPERATOR = "NetworkOperator";
	private static final boolean IS_CURRENT_PROVIDER = true;
	
	private static final String PROVIDER_NAME2 = "TestProvider2";
	private static final Omtp.ProtocolVersion PROTOCOL_VERSION2 = Omtp.ProtocolVersion.V1_2;
	private static final String CLIENT_TYPE2 = "TestClient2";
	private static final String SMS_DESTINATION_NUMBER2 = "01234567892";
	private static final short SMS_DESTINATION_PORT2 = 124;
    private static final String SMS_SERVICE_CENTER2 = null;
    private static final String DATE_FORMAT2 = "TestDateFormat2";
	private static final String NETWORK_OPERATOR2 = "NetworkOperator2";
	private static final boolean IS_CURRENT_PROVIDER2 = true;


	private OmtpProviderDatabase mProviderDb;
	private static int NUMBER_OF_PROVIDERS_INSERTED = 0;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		try {
			StackDependencyResolverImpl.initialize(getContext());
			mProviderDb = new OmtpProviderDatabase(
					StackDependencyResolverImpl.getInstance()
					.getProviderDatabaseHelper());

            Log.d("OmtpProviderDatabaseTest", "deleteTableContent Provider");
            mProviderDb.deleteTableContent();

            NUMBER_OF_PROVIDERS_INSERTED = 0;
		} catch (IllegalStateException ise) {
			// do nothing
		}
	}
	
	private OmtpProviderInfo buildProviderInfoWithValidValues() {
		OmtpProviderInfo.Builder builder = new OmtpProviderInfo.Builder();
		OmtpProviderInfo providerInfo = builder.setProviderName(PROVIDER_NAME)
				.setProtocolVersion(PROTOCOL_VERSION)
				.setClientType(CLIENT_TYPE)
				.setSmsDestinationNumber(SMS_DESTINATION_NUMBER)
				.setSmsDestinationPort(SMS_DESTINATION_PORT)
                .setSmsServiceCenter(SMS_SERVICE_CENTER)
				.setNetworkOperator(NETWORK_OPERATOR)
				.setDateFormat(DATE_FORMAT)
				.setIsCurrentProvider(IS_CURRENT_PROVIDER).build();
		return providerInfo;
	}
	
	private OmtpProviderInfo buildSecondProviderInfoWithValidValues() {
		OmtpProviderInfo.Builder builder = new OmtpProviderInfo.Builder();
		OmtpProviderInfo providerInfo = builder.setProviderName(PROVIDER_NAME2)
				.setProtocolVersion(PROTOCOL_VERSION2)
				.setClientType(CLIENT_TYPE2)
				.setSmsDestinationNumber(SMS_DESTINATION_NUMBER2)
				.setSmsDestinationPort(SMS_DESTINATION_PORT2)
                .setSmsServiceCenter(SMS_SERVICE_CENTER2)
				.setNetworkOperator(NETWORK_OPERATOR2)
				.setDateFormat(DATE_FORMAT2)
				.setIsCurrentProvider(IS_CURRENT_PROVIDER2).build();
		return providerInfo;
	}
	
	// Test insertion
	public void testProviderInfoInsertion() {
		boolean inserted = mProviderDb.updateProviderInfo(buildProviderInfoWithValidValues());
		assertTrue(inserted);
		NUMBER_OF_PROVIDERS_INSERTED++;
		
		OmtpProviderInfo providerInfoRetrieved = mProviderDb.getProviderInfoByName(PROVIDER_NAME);
		
		assertNotNull(providerInfoRetrieved);
		
		// Assert if the retrieved object corresponds.
		assertEquals(PROVIDER_NAME, providerInfoRetrieved.getProviderName());
		assertEquals(PROTOCOL_VERSION, providerInfoRetrieved.getProtocolVersion());
		assertEquals(CLIENT_TYPE, providerInfoRetrieved.getClientType());
		assertEquals(SMS_DESTINATION_NUMBER, providerInfoRetrieved.getSmsDestinationNumber());
		assertEquals(SMS_SERVICE_CENTER, providerInfoRetrieved.getSmsServiceCenter());
        assertEquals(SMS_DESTINATION_PORT, providerInfoRetrieved.getSmsDestinationPort());
        assertEquals(NETWORK_OPERATOR, providerInfoRetrieved.getNetworkOperator());
		assertEquals(DATE_FORMAT, providerInfoRetrieved.getDateFormat());
		assertEquals(IS_CURRENT_PROVIDER, providerInfoRetrieved.isCurrentProvider());
		
		assertEquals(NUMBER_OF_PROVIDERS_INSERTED, mProviderDb.getProvidersInfoWithNetworkOperator(NETWORK_OPERATOR).size());
	}
	
	// Test update
	public void testProviderInfoUpdate() {
		OmtpProviderInfo existingProviderInfo = buildProviderInfoWithValidValues();
		boolean inserted = mProviderDb.updateProviderInfo(existingProviderInfo);
		assertTrue(inserted);
		NUMBER_OF_PROVIDERS_INSERTED++;

		OmtpProviderInfo.Builder builder = new OmtpProviderInfo.Builder();
		builder.setFieldsFromProvider(existingProviderInfo);
		
		OmtpProviderInfo modifiedProviderInfo = builder.setProtocolVersion(PROTOCOL_VERSION2)
												.setClientType(CLIENT_TYPE2)
												.setSmsDestinationNumber(SMS_DESTINATION_NUMBER2)
												.setSmsDestinationPort(SMS_DESTINATION_PORT2)
                                                .setSmsServiceCenter(SMS_SERVICE_CENTER2)
												.setNetworkOperator(NETWORK_OPERATOR2)
												.setDateFormat(DATE_FORMAT2)
												.setIsCurrentProvider(IS_CURRENT_PROVIDER2).build();
			
		boolean updated = mProviderDb.updateProviderInfo(modifiedProviderInfo);
		assertTrue(updated);
		assertEquals(NUMBER_OF_PROVIDERS_INSERTED, mProviderDb.getProvidersInfoWithNetworkOperator(NETWORK_OPERATOR2).size());
		
		OmtpProviderInfo retrievedProviderInfo = mProviderDb.getProviderInfoByName(PROVIDER_NAME);

		
		assertNotNull(retrievedProviderInfo);
		
		// Assert if the retrieved object corresponds.
		assertEquals(PROVIDER_NAME, retrievedProviderInfo.getProviderName());
		assertEquals(PROTOCOL_VERSION2, retrievedProviderInfo.getProtocolVersion());
		assertEquals(CLIENT_TYPE2, retrievedProviderInfo.getClientType());
		assertEquals(SMS_DESTINATION_NUMBER2, retrievedProviderInfo.getSmsDestinationNumber());
		assertEquals(SMS_DESTINATION_PORT2, retrievedProviderInfo.getSmsDestinationPort());
        assertEquals(SMS_SERVICE_CENTER2, retrievedProviderInfo.getSmsServiceCenter());
		assertEquals(NETWORK_OPERATOR2, retrievedProviderInfo.getNetworkOperator());
		assertEquals(DATE_FORMAT2, retrievedProviderInfo.getDateFormat());
		assertEquals(IS_CURRENT_PROVIDER2, retrievedProviderInfo.isCurrentProvider());
	}
	
	// Test remove
	public void testProviderInfoRemove() {
		OmtpProviderInfo providerInfo = buildProviderInfoWithValidValues();
		boolean inserted = mProviderDb.updateProviderInfo(providerInfo);
		assertTrue(inserted);
		NUMBER_OF_PROVIDERS_INSERTED++;
		
		providerInfo = mProviderDb.getProviderInfoByName(PROVIDER_NAME);
		assertNotNull(providerInfo);
		
		boolean removed = mProviderDb.removeProviderInfo(providerInfo);
		assertTrue(removed);
		
		providerInfo = mProviderDb.getProviderInfoByName(PROVIDER_NAME);
		assertNull(providerInfo);
		
		// Remove non existing provider
		providerInfo = buildProviderInfoWithValidValues();
		removed = mProviderDb.removeProviderInfo(providerInfo);
		assertFalse(removed);

	}
	
	// Test retrieve current provider
	public void testGetCurrentProviderInfo() {
		// Test when there is no provider in the db
		OmtpProviderInfo providerInfo = mProviderDb.getCurrentProviderInfoWithNetworkOperator(NETWORK_OPERATOR);
		assertNull(providerInfo);
		
		// Test when there is one provider in the db but not set as current one
		OmtpProviderInfo.Builder builder = new OmtpProviderInfo.Builder();
		builder.setFieldsFromProvider(buildProviderInfoWithValidValues());
		builder.setIsCurrentProvider(false);
		boolean inserted = mProviderDb.updateProviderInfo(builder.build());
		assertTrue(inserted);
		
		builder.setFieldsFromProvider(buildSecondProviderInfoWithValidValues());
		builder.setIsCurrentProvider(false);
		builder.setNetworkOperator(NETWORK_OPERATOR);
		inserted = mProviderDb.updateProviderInfo(builder.build());
		assertTrue(inserted);

		providerInfo = mProviderDb.getCurrentProviderInfoWithNetworkOperator(NETWORK_OPERATOR);
		assertNotNull(providerInfo);
		// Check values
		// Should return the first one inserted and be set as current one
		assertEquals(PROVIDER_NAME, providerInfo.getProviderName());
		assertEquals(PROTOCOL_VERSION, providerInfo.getProtocolVersion());
		assertEquals(CLIENT_TYPE, providerInfo.getClientType());
		assertEquals(SMS_DESTINATION_NUMBER, providerInfo.getSmsDestinationNumber());
		assertEquals(SMS_DESTINATION_PORT, providerInfo.getSmsDestinationPort());
        assertEquals(SMS_SERVICE_CENTER, providerInfo.getSmsServiceCenter());
        assertEquals(NETWORK_OPERATOR, providerInfo.getNetworkOperator());
		assertEquals(DATE_FORMAT, providerInfo.getDateFormat());
		assertEquals(true, providerInfo.isCurrentProvider());
		
		// The other one should be set as non current 
		assertEquals(false, mProviderDb.getProviderInfoByName(PROVIDER_NAME2).isCurrentProvider());
		
		// Test get current provider when true is set
		providerInfo = mProviderDb.getCurrentProviderInfoWithNetworkOperator(NETWORK_OPERATOR);
		assertNotNull(providerInfo);
		// Check values
		// Should return the first one inserted and be set as current one
		assertEquals(PROVIDER_NAME, providerInfo.getProviderName());
		assertEquals(PROTOCOL_VERSION, providerInfo.getProtocolVersion());
		assertEquals(CLIENT_TYPE, providerInfo.getClientType());
		assertEquals(SMS_DESTINATION_NUMBER, providerInfo.getSmsDestinationNumber());
		assertEquals(SMS_DESTINATION_PORT, providerInfo.getSmsDestinationPort());
        assertEquals(SMS_SERVICE_CENTER, providerInfo.getSmsServiceCenter());
        assertEquals(NETWORK_OPERATOR, providerInfo.getNetworkOperator());
		assertEquals(DATE_FORMAT, providerInfo.getDateFormat());
		assertEquals(true, providerInfo.isCurrentProvider());
	}
	
	// Test get providers list
	public void testGetProvidersList() {
		List<OmtpProviderInfo> list = mProviderDb.getProvidersInfoWithNetworkOperator(NETWORK_OPERATOR);
		assertNotNull(list);
		assertTrue(list.isEmpty());
		
		int countProvider = 0;
		boolean inserted = mProviderDb.updateProviderInfo(buildProviderInfoWithValidValues());
		if(inserted)
			countProvider++;
		
		OmtpProviderInfo.Builder builder = new OmtpProviderInfo.Builder();
		builder.setFieldsFromProvider(buildSecondProviderInfoWithValidValues());
		builder.setNetworkOperator(NETWORK_OPERATOR);
		inserted = mProviderDb.updateProviderInfo(builder.build());
		if(inserted)
			countProvider++;
		
		list = mProviderDb.getProvidersInfoWithNetworkOperator(NETWORK_OPERATOR);
		assertEquals(list.size(), countProvider);
		
		OmtpProviderInfo secondProviderInfo = list.get(1);
		assertNotNull(secondProviderInfo);
		assertEquals(PROVIDER_NAME2, secondProviderInfo.getProviderName());
		assertEquals(PROTOCOL_VERSION2, secondProviderInfo.getProtocolVersion());
		assertEquals(CLIENT_TYPE2, secondProviderInfo.getClientType());
		assertEquals(SMS_DESTINATION_NUMBER2, secondProviderInfo.getSmsDestinationNumber());
		assertEquals(SMS_DESTINATION_PORT2, secondProviderInfo.getSmsDestinationPort());
        assertEquals(SMS_SERVICE_CENTER2, secondProviderInfo.getSmsServiceCenter());
        assertEquals(NETWORK_OPERATOR, secondProviderInfo.getNetworkOperator());
		assertEquals(DATE_FORMAT2, secondProviderInfo.getDateFormat());
		assertEquals(IS_CURRENT_PROVIDER2, secondProviderInfo.isCurrentProvider());

	}

}
