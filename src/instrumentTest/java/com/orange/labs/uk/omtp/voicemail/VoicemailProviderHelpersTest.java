package com.orange.labs.uk.omtp.voicemail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.net.Uri;
import android.provider.VoicemailContract.Voicemails;
import android.test.AndroidTestCase;

import com.orange.labs.uk.omtp.dependency.StackDependencyResolverImpl;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.voicemail.LocalVoicemailProvider.SortOrder;

public class VoicemailProviderHelpersTest extends AndroidTestCase {

	private static final Logger logger = Logger.getLogger(VoicemailProviderHelpersTest.class);

	private LocalVoicemailProvider packageScopedVoicemailProvider;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// get voicemail provider for current test package
		packageScopedVoicemailProvider = LocalVoicemailProviderImpl
				.createPackageScopedVoicemailProvider(getContext());
		
		// initialise StackDependencyResolver
		StackDependencyResolverImpl.initialize(getContext());
	}

	public void testInsert() {
		// delete all
		packageScopedVoicemailProvider.deleteAll();

		// create a Voicemail with some test data
		Voicemail voicemail = createTestVoicemail("12", false);
		// insert Voicemail
		Uri uriOfInsertedVoicemail = packageScopedVoicemailProvider.insert(voicemail);
		assertNotNull(uriOfInsertedVoicemail);
	}

	public void testUpdate() {

		final String VOICEMAIL_ID_BEFORE_UPDATE = "12";
		final String VOICEMAIL_ID_AFTER_UPDATE = "22";
		// delete all Voicemails
		packageScopedVoicemailProvider.deleteAll();

		// create a Voicemail with some test data
		Voicemail voicemail = createTestVoicemail(VOICEMAIL_ID_BEFORE_UPDATE, false);
		// insert Voicemail
		Uri uriOfInitialVoicemail = packageScopedVoicemailProvider.insert(voicemail);
		// create new voicemail
		Voicemail updatedVoicemail = createTestVoicemail(VOICEMAIL_ID_AFTER_UPDATE, true);
		// update voicemail
		packageScopedVoicemailProvider.update(uriOfInitialVoicemail, updatedVoicemail);

		List<Voicemail> voicemails = packageScopedVoicemailProvider.getAllVoicemails();
		boolean isVoicemailInfoUpdated = false;
		if (!(voicemails.isEmpty())) {
			for (Voicemail storedVoicemail : voicemails) {
				if (VOICEMAIL_ID_AFTER_UPDATE.equals(storedVoicemail.getSourceData())
						&& (storedVoicemail.isRead())) {
					isVoicemailInfoUpdated = true;
				}
			}
		} else {
			isVoicemailInfoUpdated = false;
			fail("No Voicemails stored! Nothing has been updated!");
		}
		assertTrue(isVoicemailInfoUpdated);

	}

	public void testSetVoicemailContentUriInputStreamString() {

		// delete all
		packageScopedVoicemailProvider.deleteAll();

		// create a Voicemail with some test data
		Voicemail testVoicemail = createTestVoicemail("99", false);
		// insert Voicemail
		Uri uriOfInsertedVoicemail = packageScopedVoicemailProvider.insert(testVoicemail);

		final String testMimeType = "audio/basic";
		final byte[] payloadTestBytes = { (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
				(byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09 };
		final InputStream testInputStream = new ByteArrayInputStream(payloadTestBytes);

		try {
			packageScopedVoicemailProvider.setVoicemailContent(uriOfInsertedVoicemail,
					testInputStream, testMimeType);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Exception thrown while inserting test voicemail");
		}

		// check if we can find this inserted voicemail
		Voicemail voicemailFound = packageScopedVoicemailProvider
				.findVoicemailByUri(uriOfInsertedVoicemail);

		// compare Numbers of both voicemails
		assertEquals(testVoicemail.getNumber(), voicemailFound.getNumber());
		// compare timestamp of both emails
		assertEquals(testVoicemail.getTimestampMillis(), voicemailFound.getTimestampMillis());
		// TODO: Check if this test is good enough...

	}

	public void testSetVoicemailContentUriByteArrayString() {

		// delete all
		packageScopedVoicemailProvider.deleteAll();

		// create a Voicemail with some test data
		Voicemail testVoicemail = createTestVoicemail("98", false);
		// insert Voicemail
		Uri uriOfInsertedVoicemail = packageScopedVoicemailProvider.insert(testVoicemail);

		final String testMimeType = "audio/basic";
		final byte[] payloadTestBytes = { (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
				(byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09 };

		try {
			packageScopedVoicemailProvider.setVoicemailContent(uriOfInsertedVoicemail,
					payloadTestBytes, testMimeType);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Exception thrown while inserting test voicemail");
		}

		// check if we can find this inserted voicemail
		Voicemail voicemailFound = packageScopedVoicemailProvider
				.findVoicemailByUri(uriOfInsertedVoicemail);

		// compare Numbers of both voicemails
		assertEquals(testVoicemail.getNumber(), voicemailFound.getNumber());
		// compare timestamp of both emails
		assertEquals(testVoicemail.getTimestampMillis(), voicemailFound.getTimestampMillis());
		// TODO: Check if this test is good enough...

	}

	public void testFindVoicemailBySourceData() {

		final String sourceData = "24";

		// delete all
		packageScopedVoicemailProvider.deleteAll();

		// create a Voicemail with some test data
		Voicemail voicemail = createTestVoicemail(sourceData, false);
		// insert voicemail
		packageScopedVoicemailProvider.insert(voicemail);
		// get email by source data
		Voicemail voicemailFound = packageScopedVoicemailProvider
				.findVoicemailBySourceData(sourceData);
		// compare Numbers of both voicemails
		assertEquals(voicemail.getNumber(), voicemailFound.getNumber());
		// compare timestamp of both emails
		assertEquals(voicemail.getTimestampMillis(), voicemailFound.getTimestampMillis());
		// TODO: Check if different/other assert statement is required
	}

	public void testFindVoicemailByUri() {
		// delete all
		packageScopedVoicemailProvider.deleteAll();

		// create a Voicemail with some test data
		Voicemail voicemail = createTestVoicemail("12", false);
		// insert Voicemail
		Uri uriOfInsertedVoicemail = packageScopedVoicemailProvider.insert(voicemail);
		Voicemail voicemailFound = packageScopedVoicemailProvider
				.findVoicemailByUri(uriOfInsertedVoicemail);

		// compare Numbers of both voicemails
		assertEquals(voicemail.getNumber(), voicemailFound.getNumber());
		// compare timestamp of both emails
		assertEquals(voicemail.getTimestampMillis(), voicemail.getTimestampMillis());
	}

	public void testDeleteAll() {
		// delete all messages
		packageScopedVoicemailProvider.deleteAll();
		int currentNumberOfMessages = getMessagesCount();
		assertTrue(currentNumberOfMessages == 0);
	}

	public void testGetAllVoicemails() {
		// delete all Voicemails
		packageScopedVoicemailProvider.deleteAll();

		// insert 2 voicemails one with read state and another not
		packageScopedVoicemailProvider.insert(createTestVoicemail("1", false));
		packageScopedVoicemailProvider.insert(createTestVoicemail("2", true));

		List<Voicemail> voicemails = packageScopedVoicemailProvider.getAllVoicemails();
		int currentNumberOfMessages = voicemails.size();
		assertTrue(currentNumberOfMessages == 2);
	}

	public void testGetAllVoicemailsVoicemailFilterStringSortOrder() {

		// delete all Voicemails
		packageScopedVoicemailProvider.deleteAll();

		// insert 2 voicemails one with read state and another not
		Voicemail voicemail1 = createTestVoicemail("1", false);
		Voicemail voicemail2 = createTestVoicemail("2", true);
		packageScopedVoicemailProvider.insert(voicemail1);
		packageScopedVoicemailProvider.insert(voicemail2);

		// get email with read status
		VoicemailFilter fileterRead = VoicemailFilterFactory.createWithReadStatus(true);
		List<Voicemail> readVoicemails = packageScopedVoicemailProvider.getAllVoicemails(
				fileterRead, Voicemails.NUMBER, SortOrder.DEFAULT);
		VoicemailFilter fileterNotRead = VoicemailFilterFactory.createWithReadStatus(true);
		List<Voicemail> notReadVoicemails = packageScopedVoicemailProvider.getAllVoicemails(
				fileterNotRead, Voicemails.NUMBER, SortOrder.DEFAULT);

		assertTrue(readVoicemails.size() == 1);
		assertTrue(notReadVoicemails.size() == 1);
	}

	public void testBatchInsert() {
		// Delete all voicemails
		packageScopedVoicemailProvider.deleteAll();

		List<Voicemail> voicemails = new ArrayList<Voicemail>();
		voicemails.add(createTestVoicemail("1", false));
		voicemails.add(createTestVoicemail("2", true));

		List<Uri> uris = packageScopedVoicemailProvider.insert(voicemails);
		assertNotNull(uris);
		assertEquals(2, uris.size());

		List<Voicemail> retrievedVoicemails = packageScopedVoicemailProvider.getAllVoicemails();
		assertEquals(2, retrievedVoicemails.size());
	}

	public void testBatchUpdate() {
		// Delete all voicemails
		packageScopedVoicemailProvider.deleteAll();

		List<Voicemail> voicemails = new ArrayList<Voicemail>();
		voicemails.add(createTestVoicemail("1", false));
		voicemails.add(createTestVoicemail("2", false));

		List<Uri> uris = packageScopedVoicemailProvider.insert(voicemails);

		Map<Uri, Voicemail> updateMap = new HashMap<Uri, Voicemail>();
		updateMap.put(uris.get(0), VoicemailImpl.createEmptyBuilder().setIsRead(true).build());

		int rowUpdated = packageScopedVoicemailProvider.update(updateMap);
		assertEquals(1, rowUpdated);

		List<Voicemail> readVoicemails = packageScopedVoicemailProvider.getAllVoicemails(
				VoicemailFilterFactory.createWithReadStatus(true), Voicemails.NUMBER,
				SortOrder.DEFAULT);
		assertEquals(1, readVoicemails.size());
	}

	public void testDelete() {
		packageScopedVoicemailProvider.deleteAll();

		Voicemail voicemail1 = createTestVoicemail("1", false);
		Uri voicemailUri = packageScopedVoicemailProvider.insert(voicemail1);
		assertNotNull(voicemailUri);

		List<Voicemail> voicemails = packageScopedVoicemailProvider.getAllVoicemails();
		assertEquals(1, voicemails.size());

		assertEquals(1, packageScopedVoicemailProvider.delete(voicemailUri));

		voicemails = packageScopedVoicemailProvider.getAllVoicemails();
		assertEquals(0, voicemails.size());
	}

	public void testBatchDelete() {
		packageScopedVoicemailProvider.deleteAll();

		List<Voicemail> voicemails = new ArrayList<Voicemail>();
		voicemails.add(createTestVoicemail("1", false));
		voicemails.add(createTestVoicemail("2", true));
		voicemails.add(createTestVoicemail("3", true));

		List<Uri> uris = packageScopedVoicemailProvider.insert(voicemails);
		assertNotNull(uris);
		assertEquals(3, uris.size());

		// Delete first uri
		uris.remove(0);

		int rowDeleted = packageScopedVoicemailProvider.delete(uris);
		assertEquals(2, rowDeleted);

		voicemails = packageScopedVoicemailProvider.getAllVoicemails();
		assertEquals(1, voicemails.size());
		assertEquals("1", voicemails.get(0).getSourceData());
	}

	/**
	 * Creates Voicemail
	 * 
	 * @param msgid
	 *            Voicemail Source Data
	 * @param isRead
	 *            Status of Voicemail (Read or not)
	 * @return created Voicemail object
	 */
	private Voicemail createTestVoicemail(String msgid, boolean isRead) {
		String sender = "07973524";
		long time = System.currentTimeMillis();
		long duration = 10;

		Voicemail voicemail = VoicemailImpl.createForInsertion(time, sender).setDuration(duration)
				.setSourcePackage(getContext().getPackageName()).setSourceData(msgid)
				.setIsRead(isRead).build();
		return voicemail;
	}

	/**
	 * Get current number of messages
	 * 
	 * @return number of Voicemail messages
	 */
	private int getMessagesCount() {
		List<Voicemail> voicemails = packageScopedVoicemailProvider.getAllVoicemails();
		logger.d(String.format("Voicemail count: %d", voicemails.size()));
		for (Voicemail voicemail : voicemails) {
			logger.d(voicemail.toString());
		}
		return voicemails.size();

	}

}
