package com.orange.labs.uk.omtp.account;

import android.test.AndroidTestCase;

import com.orange.labs.uk.omtp.dependency.StackDependencyResolverImpl;
import com.orange.labs.uk.omtp.protocol.Omtp.ProvisioningStatus;

public class OmtpAccountDatabaseTest extends AndroidTestCase {

	private static final String ACCOUNT_ID = "123456789";
	private static final String IMAP_USER_NAME = "TestUser";
	private static final String IMAP_USER_NAME_ALT = "TestUserUpdated";
	private static final String IMAP_PASSWORD = "TestPassword";
	private static final String IMAP_SERVER = "TestImapServer";
	private static final String IMAP_PORT = "1234";
	private static final String SMS_NUMBER = "9876";
	private static final String TUI_NUMBER = "5432";
	private static final String SUBSCRIPTION_URI = "TestSubscriptionUri";
	private static final ProvisioningStatus PROVISIONING_STATUS = ProvisioningStatus.SUBSCRIBER_READY;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		try {
			StackDependencyResolverImpl.initialize(getContext());
		} catch (IllegalStateException ise) {
			// do nothing
		}
	}

	/**
	 * Method tests update of new account info in the accounts table
	 */
	public void testUpdateAccountInfo() {
		OmtpAccountDatabase accountDb = new OmtpAccountDatabase(StackDependencyResolverImpl
		        .getInstance().getProviderDatabaseHelper());

		// Try without a proper provider
		OmtpAccountInfo.Builder accountBuilder = new OmtpAccountInfo.Builder();
		OmtpAccountInfo accountInfo = accountBuilder.setAccountId(ACCOUNT_ID)
		        .setImapUsername(IMAP_USER_NAME).setImapPassword(IMAP_PASSWORD)
		        .setImapServer(IMAP_SERVER).setImapPort(IMAP_PORT).setSmsNumber(SMS_NUMBER)
		        .setTuiNumber(TUI_NUMBER).setSubscriptionUrl(SUBSCRIPTION_URI)
		        .setProvisionningStatus(PROVISIONING_STATUS).build();

		boolean isInserted = accountDb.updateAccountInfo(accountInfo);
		assertTrue(isInserted);

		OmtpAccountInfo retrieved = accountDb.getAccountInfo(accountInfo.getAccountId());
		assertNotNull(retrieved);

		// Assert if the retrieved data matches inserted data
		assertEquals(accountInfo.getAccountId(), retrieved.getAccountId());
		assertEquals(accountInfo.getImapUsername(), retrieved.getImapUsername());
		assertEquals(accountInfo.getImapPassword(), retrieved.getImapPassword());
		assertEquals(accountInfo.getImapServer(), retrieved.getImapServer());
		assertEquals(accountInfo.getImapPort(), retrieved.getImapPort());
		assertEquals(accountInfo.getSmsNumber(), retrieved.getSmsNumber());
		assertEquals(accountInfo.getTuiNumber(), retrieved.getTuiNumber());
		assertEquals(accountInfo.getSubscriptionUrl(), retrieved.getSubscriptionUrl());
		assertEquals(accountInfo.getProvisionningStatus(), retrieved.getProvisionningStatus());

		// Update one field and update the record in the database.
		OmtpAccountInfo altAccountInfo = accountBuilder.setAccountId(ACCOUNT_ID)
		        .setImapUsername(IMAP_USER_NAME_ALT).setImapPassword(IMAP_PASSWORD)
		        .setImapServer(IMAP_SERVER).setImapPort(IMAP_PORT).setSmsNumber(SMS_NUMBER)
		        .setTuiNumber(TUI_NUMBER).setSubscriptionUrl(SUBSCRIPTION_URI)
		        .setProvisionningStatus(PROVISIONING_STATUS).build();

		boolean isUpdated = accountDb.updateAccountInfo(altAccountInfo);
		assertTrue(isUpdated);

		// Assert newly retrieved corresponds to the alternative client type
		// value.
		retrieved = accountDb.getAccountInfo(ACCOUNT_ID);
		assertNotNull(retrieved);
		assertEquals(retrieved.getImapUsername(), IMAP_USER_NAME_ALT);

		// Remove and assert if the provider info has been removed.
		boolean isRemoved = accountDb.removeAccountInfo(altAccountInfo.getAccountId());
		assertTrue(isRemoved);

		// Assert that the provider info does not exist anymore.
		retrieved = accountDb.getAccountInfo(accountInfo.getAccountId());
		assertNull(retrieved);
	}
}
