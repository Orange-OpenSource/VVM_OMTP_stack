package com.orange.labs.uk.omtp.provider;

import java.util.ArrayList;
import java.util.List;

import android.test.AndroidTestCase;

import com.orange.labs.uk.omtp.dependency.StackDependencyResolver;
import com.orange.labs.uk.omtp.dependency.StackDependencyResolverImpl;
import com.orange.labs.uk.omtp.protocol.Omtp;


public class OmtpProviderWrapperTest extends AndroidTestCase {
	private static final String PROVIDER_NAME = "TestProvider";
	private static final Omtp.ProtocolVersion PROTOCOL_VERSION = Omtp.ProtocolVersion.V1_1;
	private static final String CLIENT_TYPE = "TestClient";
	private static final String SMS_DESTINATION_NUMBER = "0123456789";
	private static final short SMS_DESTINATION_PORT = 123;
	private static final String DATE_FORMAT = "TestDateFormat";
	private static final String NETWORK_OPERATOR = "20801"; // Orange
	private static final boolean IS_CURRENT_PROVIDER = false;
	
	private static final String PROVIDER_NAME2 = "TestProvider2";
	private static final Omtp.ProtocolVersion PROTOCOL_VERSION2 = Omtp.ProtocolVersion.V1_2;
	private static final String CLIENT_TYPE2 = "TestClient2";
	private static final String SMS_DESTINATION_NUMBER2 = "01234567892";
	private static final short SMS_DESTINATION_PORT2 = 124;
	private static final String DATE_FORMAT2 = "TestDateFormat2";
	private static final String NETWORK_OPERATOR2 = "20801"; // Orange
	private static final boolean IS_CURRENT_PROVIDER2 = false;
	
	private static int NUMBER_OF_PROVIDERS_INSERTED = 0;
	
	private StackDependencyResolver mStackDependencyResolver;
	private OmtpProviderWrapper mProviderWrapper;
	
	private OmtpProviderInfo buildProviderInfoWithValidValues() {
		OmtpProviderInfo.Builder builder = new OmtpProviderInfo.Builder();
		OmtpProviderInfo providerInfo = builder.setProviderName(PROVIDER_NAME)
				.setProtocolVersion(PROTOCOL_VERSION)
				.setClientType(CLIENT_TYPE)
				.setSmsDestinationNumber(SMS_DESTINATION_NUMBER)
				.setSmsDestinationPort(SMS_DESTINATION_PORT)
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
				.setNetworkOperator(NETWORK_OPERATOR)
				.setDateFormat(DATE_FORMAT2)
				.setIsCurrentProvider(IS_CURRENT_PROVIDER2).build();
		return providerInfo;
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		mStackDependencyResolver = StackDependencyResolverImpl.initialize(getContext());
		mProviderWrapper = new OmtpProviderWrapperImpl(new OmtpProviderDatabase(
				mStackDependencyResolver.getProviderDatabaseHelper()), 
				mStackDependencyResolver.getTelephonyManager(), 
				mStackDependencyResolver.getSingleExecutorService());
		
		// Clean provider list
		try {
			StackDependencyResolverImpl.initialize(getContext());
			OmtpProviderDatabase providerDb = new OmtpProviderDatabase(
					StackDependencyResolverImpl.getInstance()
					.getProviderDatabaseHelper());
			
			providerDb.deleteTableContent();
			NUMBER_OF_PROVIDERS_INSERTED = 0;
			
		} catch (IllegalStateException ise) {
			// do nothing
		}
	}
	
	public void testGetProviderInfoWhenNotSetAsCurrent() {
		// Get provider info when none exists
		assertNull(mProviderWrapper.getProviderInfo());
		
		OmtpProviderInfo providerInfo = buildProviderInfoWithValidValues();
		assertEquals(false, providerInfo.isCurrentProvider());

		OmtpProviderInfo secondProviderInfo = buildSecondProviderInfoWithValidValues();
		assertEquals(false, secondProviderInfo.isCurrentProvider());
		
		// Add two providers set as non current
		mProviderWrapper.updateProviderInfo(providerInfo);
		mProviderWrapper.updateProviderInfo(secondProviderInfo);
		
		// Get current provider
		OmtpProviderInfo currentProviderInfo = mProviderWrapper.getProviderInfo();
		assertNotNull(currentProviderInfo);
		
		// Assert if the retrieved object corresponds. Should be the first on inserted
		assertEquals(PROVIDER_NAME, currentProviderInfo.getProviderName());
		assertEquals(PROTOCOL_VERSION, currentProviderInfo.getProtocolVersion());
		assertEquals(CLIENT_TYPE, currentProviderInfo.getClientType());
		assertEquals(SMS_DESTINATION_NUMBER, currentProviderInfo.getSmsDestinationNumber());
		assertEquals(SMS_DESTINATION_PORT, currentProviderInfo.getSmsDestinationPort());
		assertEquals(NETWORK_OPERATOR, currentProviderInfo.getNetworkOperator());
		assertEquals(DATE_FORMAT, currentProviderInfo.getDateFormat());
		// Should now be set as current
		assertTrue(currentProviderInfo.isCurrentProvider());
		
		// The other one should be set an non current
		assertFalse(mProviderWrapper.getProviderInfo(PROVIDER_NAME2).isCurrentProvider());
	}
	
	// Get provider info with provider name
	public void testGetProviderInfoByProviderName() {
		assertNull(mProviderWrapper.getProviderInfo("RandomName"));
		
		mProviderWrapper.updateProviderInfo(buildProviderInfoWithValidValues());
		
		OmtpProviderInfo providerInfo = mProviderWrapper.getProviderInfo(PROVIDER_NAME);
		assertNotNull(providerInfo);
		
		// Assert if the retrieved object corresponds.
		assertEquals(PROVIDER_NAME, providerInfo.getProviderName());
		assertEquals(PROTOCOL_VERSION, providerInfo.getProtocolVersion());
		assertEquals(CLIENT_TYPE, providerInfo.getClientType());
		assertEquals(SMS_DESTINATION_NUMBER, providerInfo.getSmsDestinationNumber());
		assertEquals(SMS_DESTINATION_PORT, providerInfo.getSmsDestinationPort());
		assertEquals(NETWORK_OPERATOR, providerInfo.getNetworkOperator());
		assertEquals(DATE_FORMAT, providerInfo.getDateFormat());
		assertEquals(IS_CURRENT_PROVIDER, providerInfo.isCurrentProvider());
		
		assertNull(mProviderWrapper.getProviderInfo("RandomName"));

	}
	
	public void testRemoveProviderInfo() {
		assertFalse(mProviderWrapper.removeProviderInfo(null));
		assertFalse(mProviderWrapper.removeProviderInfo(buildProviderInfoWithValidValues()));
		
		OmtpProviderInfo providerInfo = buildProviderInfoWithValidValues();
		mProviderWrapper.updateProviderInfo(providerInfo);
		assertFalse(mProviderWrapper.removeProviderInfo(buildSecondProviderInfoWithValidValues()));
		assertNotNull(mProviderWrapper.getProviderInfo());
		assertTrue(mProviderWrapper.removeProviderInfo(providerInfo));
		assertNull(mProviderWrapper.getProviderInfo());
		assertFalse(mProviderWrapper.removeProviderInfo(providerInfo));
	}
	
	public void testUpdateProviderInfo() {
		// Test insert
		OmtpProviderInfo providerInfo = buildProviderInfoWithValidValues();
		assertTrue(mProviderWrapper.updateProviderInfo(providerInfo));
		
		OmtpProviderInfo providerInfoInserted = mProviderWrapper.getProviderInfo(providerInfo.getProviderName());
		assertNotNull(providerInfoInserted);
		// Assert if the retrieved object corresponds.
		assertEquals(providerInfo.getProviderName(), providerInfoInserted.getProviderName());
		assertEquals(providerInfo.getProtocolVersion(), providerInfoInserted.getProtocolVersion());
		assertEquals(providerInfo.getClientType(), providerInfoInserted.getClientType());
		assertEquals(providerInfo.getSmsDestinationNumber(), providerInfoInserted.getSmsDestinationNumber());
		assertEquals(providerInfo.getSmsDestinationPort(), providerInfoInserted.getSmsDestinationPort());
		assertEquals(providerInfo.getNetworkOperator(), providerInfoInserted.getNetworkOperator());
		assertEquals(providerInfo.getDateFormat(), providerInfoInserted.getDateFormat());
		assertEquals(providerInfo.isCurrentProvider(), providerInfoInserted.isCurrentProvider());
		
		// Insert a second one
		OmtpProviderInfo secondProviderInfo = buildSecondProviderInfoWithValidValues();
		assertTrue(mProviderWrapper.updateProviderInfo(secondProviderInfo));
		OmtpProviderInfo secondProviderInfoInserted = mProviderWrapper.getProviderInfo(secondProviderInfo.getProviderName());
		assertNotNull(secondProviderInfoInserted);
		// Assert if the retrieved object corresponds.
		assertEquals(secondProviderInfo.getProviderName(), secondProviderInfoInserted.getProviderName());
		assertEquals(secondProviderInfo.getProtocolVersion(), secondProviderInfoInserted.getProtocolVersion());
		assertEquals(secondProviderInfo.getClientType(), secondProviderInfoInserted.getClientType());
		assertEquals(secondProviderInfo.getSmsDestinationNumber(), secondProviderInfoInserted.getSmsDestinationNumber());
		assertEquals(secondProviderInfo.getSmsDestinationPort(), secondProviderInfoInserted.getSmsDestinationPort());
		assertEquals(secondProviderInfo.getNetworkOperator(), secondProviderInfoInserted.getNetworkOperator());
		assertEquals(secondProviderInfo.getDateFormat(), secondProviderInfoInserted.getDateFormat());
		assertEquals(secondProviderInfo.isCurrentProvider(), secondProviderInfoInserted.isCurrentProvider());
		
		// Update the second provider
		OmtpProviderInfo.Builder builder = new OmtpProviderInfo.Builder();
		builder.setFieldsFromProvider(secondProviderInfoInserted);
		builder.setProtocolVersion(PROTOCOL_VERSION);
		builder.setClientType(CLIENT_TYPE);
		builder.setSmsDestinationNumber(SMS_DESTINATION_NUMBER);
		builder.setSmsDestinationPort(SMS_DESTINATION_PORT);
		builder.setNetworkOperator(NETWORK_OPERATOR);
		builder.setDateFormat(DATE_FORMAT);
		builder.setIsCurrentProvider(IS_CURRENT_PROVIDER);
		OmtpProviderInfo providerUpdated = builder.build();
		
		assertTrue(mProviderWrapper.updateProviderInfo(providerUpdated));
		OmtpProviderInfo providerUpdatedInDb = mProviderWrapper.getProviderInfo(providerUpdated.getProviderName());
		assertNotNull(providerUpdatedInDb);
		// Assert if the retrieved object corresponds.
		assertEquals(providerUpdated.getProviderName(), providerUpdatedInDb.getProviderName());
		assertEquals(providerUpdated.getProtocolVersion(), providerUpdatedInDb.getProtocolVersion());
		assertEquals(providerUpdated.getClientType(), providerUpdatedInDb.getClientType());
		assertEquals(providerUpdated.getSmsDestinationNumber(), providerUpdatedInDb.getSmsDestinationNumber());
		assertEquals(providerUpdated.getSmsDestinationPort(), providerUpdatedInDb.getSmsDestinationPort());
		assertEquals(providerUpdated.getNetworkOperator(), providerUpdatedInDb.getNetworkOperator());
		assertEquals(providerUpdated.getDateFormat(), providerUpdatedInDb.getDateFormat());
		assertEquals(providerUpdated.isCurrentProvider(), providerUpdatedInDb.isCurrentProvider());
		
	}
	
	public void testUpdateProvidersInfo() {
		assertFalse(mProviderWrapper.updateProvidersInfo(null));
		assertTrue(mProviderWrapper.updateProvidersInfo(new ArrayList<OmtpProviderInfo>()));
		
		OmtpProviderInfo providerInfo = buildProviderInfoWithValidValues();
		OmtpProviderInfo secondProviderInfo = buildSecondProviderInfoWithValidValues();
		ArrayList<OmtpProviderInfo> list = new ArrayList<OmtpProviderInfo>();
		list.add(providerInfo);
		list.add(secondProviderInfo);
		
		assertTrue(mProviderWrapper.updateProvidersInfo(list));
		assertNotNull(mProviderWrapper.getProviderInfo(providerInfo.getProviderName()));
		assertNotNull(mProviderWrapper.getProviderInfo(secondProviderInfo.getProviderName()));
	}
	
	public void testGetSupportedProviders() {
		assertNotNull(mProviderWrapper.getSupportedProviders());
		assertTrue(mProviderWrapper.getSupportedProviders().isEmpty());
		
		OmtpProviderInfo providerInfo = buildProviderInfoWithValidValues();
		OmtpProviderInfo secondProviderInfo = buildSecondProviderInfoWithValidValues();
		OmtpProviderInfo.Builder builder = new OmtpProviderInfo.Builder();
		builder.setFieldsFromProvider(buildProviderInfoWithValidValues());
		builder.setProviderName("NonSupported");
		builder.setNetworkOperator("Unsupported");
		OmtpProviderInfo nonSupportedProviderInfo = builder.build();
		
		assertTrue(mProviderWrapper.updateProviderInfo(providerInfo));
		assertTrue(mProviderWrapper.updateProviderInfo(secondProviderInfo));
		assertTrue(mProviderWrapper.updateProviderInfo(nonSupportedProviderInfo));
		
		List<OmtpProviderInfo> list = mProviderWrapper.getSupportedProviders();
		assertFalse(list.isEmpty());
		assertEquals(2, list.size());
		assertEquals(providerInfo.getProviderName(), list.get(0).getProviderName());
		assertEquals(secondProviderInfo.getProviderName(), list.get(1).getProviderName());
	}
	
	
}
