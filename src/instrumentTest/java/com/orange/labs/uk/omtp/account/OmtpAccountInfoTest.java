package com.orange.labs.uk.omtp.account;

import com.orange.labs.uk.omtp.protocol.Omtp.ProvisioningStatus;

import junit.framework.TestCase;

public class OmtpAccountInfoTest extends TestCase {

	private static final String ACCOUNT_ID = "123456789";
	private static final String IMAP_USER_NAME = "TestUser";
	private static final String IMAP_PASSWORD = "TestPassword";
	private static final String IMAP_SERVER = "TestImapServer";
	private static final String IMAP_PORT = "1234";
	private static final String SMS_NUMBER = "9876";
	private static final String TUI_NUMBER = "5432";
	private static final String SUBSCRIPTION_URI = "TestSubscriptionUri";
	private static final ProvisioningStatus PROVISSIONING_STATUS = ProvisioningStatus.SUBSCRIBER_READY;

	public void testAccountInfoCreation() {
		OmtpAccountInfo.Builder builder = new OmtpAccountInfo.Builder();
		OmtpAccountInfo accountInfo = builder.setAccountId(ACCOUNT_ID)
				.setImapUsername(IMAP_USER_NAME).setImapPassword(IMAP_PASSWORD)
				.setImapServer(IMAP_SERVER).setImapPort(IMAP_PORT).setSmsNumber(SMS_NUMBER)
				.setTuiNumber(TUI_NUMBER).setSubscriptionUrl(SUBSCRIPTION_URI)
				.setProvisionningStatus(PROVISSIONING_STATUS)
				.build();

		assertEquals(accountInfo.getAccountId(), ACCOUNT_ID);
		assertEquals(accountInfo.getImapUsername(), IMAP_USER_NAME);
		assertEquals(accountInfo.getImapPassword(), IMAP_PASSWORD);
		assertEquals(accountInfo.getImapServer(), IMAP_SERVER);
		assertEquals(accountInfo.getImapPort(), IMAP_PORT);
		assertEquals(accountInfo.getSmsNumber(), SMS_NUMBER);
		assertEquals(accountInfo.getTuiNumber(), TUI_NUMBER);
		assertEquals(accountInfo.getSubscriptionUrl(), SUBSCRIPTION_URI);
		assertEquals(accountInfo.getProvisionningStatus(), PROVISSIONING_STATUS);
	}
}
