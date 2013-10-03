package com.orange.labs.uk.omtp.voicemail.database;

import java.util.ArrayList;
import java.util.List;

import android.net.Uri;
import android.test.AndroidTestCase;

import com.orange.labs.uk.omtp.dependency.StackDependencyResolverImpl;
import com.orange.labs.uk.omtp.dependency.StackDependencyResolver;
import com.orange.labs.uk.omtp.voicemail.Voicemail;
import com.orange.labs.uk.omtp.voicemail.VoicemailImpl;

public class LocalVoicemailDbTest extends AndroidTestCase {

	private static String SOURCE_DATA = "21";

	private StackDependencyResolver mDependencyResolver;
	private MirrorVoicemailProvider mVoicemailDb;

	protected void setUp() throws Exception {
		super.setUp();
		try {
			StackDependencyResolverImpl.initialize(getContext());
		} catch (IllegalStateException ise) {
			// do nothing
		}

		mDependencyResolver = StackDependencyResolverImpl.getInstance();
		mVoicemailDb = new MirrorVoicemailProvider(mDependencyResolver.getProviderDatabaseHelper());
	}

	public void testSingleVoicemailOperation() {
		Voicemail voicemail = VoicemailImpl.createEmptyBuilder().setSourceData(SOURCE_DATA).build();

		assertTrue(mVoicemailDb.updateVoicemail(voicemail));

		Voicemail retrievedVoicemail = mVoicemailDb.findVoicemailBySourceData(SOURCE_DATA);

		// Check validity of stored voicemail.
		assertNotNull(retrievedVoicemail);
		assertEquals(voicemail.getSourceData(), retrievedVoicemail.getSourceData());
		assertFalse(retrievedVoicemail.isRead());

		Uri uri = Uri.parse("http://www.orange.co.uk");
		assertNotNull(uri);

		Voicemail withUriVoicemail = VoicemailImpl.createCopyBuilder(voicemail).setUri(uri).build();
		assertTrue(mVoicemailDb.updateVoicemail(withUriVoicemail));

		retrievedVoicemail = mVoicemailDb.findVoicemailBySourceData(SOURCE_DATA);
		assertNotNull(retrievedVoicemail);
		assertEquals(retrievedVoicemail.getSourceData(), withUriVoicemail.getSourceData());
		assertEquals(retrievedVoicemail.getUri(), withUriVoicemail.getUri());

		Voicemail retrievedUriVoicemail = mVoicemailDb.getVoicemailWithUri(uri.toString());
		assertNotNull(retrievedUriVoicemail);
		assertEquals(retrievedVoicemail.getSourceData(), retrievedUriVoicemail.getSourceData());
		assertEquals(retrievedVoicemail.getUri(), retrievedUriVoicemail.getUri());

		// Remove and assert it has beendone correctly.
		assertTrue(mVoicemailDb.delete(voicemail));
		assertNull(mVoicemailDb.findVoicemailBySourceData(SOURCE_DATA));
	}

	public void testBatchVoicemailOperation() {
		List<Voicemail> voicemailList = new ArrayList<Voicemail>(5);
		for (int i = 0; i < voicemailList.size(); i++) {
			Voicemail voicemail = VoicemailImpl.createEmptyBuilder()
					.setSourceData(String.valueOf(i)).build();
			assertTrue(mVoicemailDb.updateVoicemail(voicemail));
		}

		// Check if messages are properly inserted.
		for (Voicemail voicemail : voicemailList) {
			Voicemail retrievedVoicemail = mVoicemailDb.findVoicemailBySourceData(voicemail
					.getSourceData());
			assertNotNull(retrievedVoicemail);
			assertEquals(voicemail.getSourceData(), retrievedVoicemail.getSourceData());
		}

		// Delete all messages
		assertTrue(mVoicemailDb.deleteList(voicemailList));

		// Check if properly deleted
		for (Voicemail voicemail : voicemailList) {
			Voicemail retrievedVoicemail = mVoicemailDb.findVoicemailBySourceData(voicemail
					.getSourceData());
			assertNull(retrievedVoicemail);
		}
	}

}
