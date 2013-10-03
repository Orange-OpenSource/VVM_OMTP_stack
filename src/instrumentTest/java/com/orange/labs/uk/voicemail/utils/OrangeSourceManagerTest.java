package com.orange.labs.uk.voicemail.utils;

import android.database.Cursor;
import android.provider.VoicemailContract.Status;
import android.test.AndroidTestCase;

import com.orange.vvm.dependency.OrangeDependencyResolver;
import com.orange.vvm.utils.SourceManager;

public class OrangeSourceManagerTest extends AndroidTestCase {

	private static final String TEL_PREFIX = "tel:/";
	
	private static final String TUI_NUMBER = "TestTUI";
	
	private SourceManager mSourceManager;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		try {
			OrangeDependencyResolver.initialize(getContext());
		} catch (IllegalStateException e) {
			// Do nothing.
		}
		
		mSourceManager = OrangeDependencyResolver.getInstance().getSourceManager();
		mSourceManager.register();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		mSourceManager.unregister();
	}

	public void testRegisterOrangeSource() {
		mSourceManager.unregister();
		assertTrue(mSourceManager.register());
		
		// Retrieve voicemail source status and check values.
		Cursor cursor = mSourceManager.getOrangeSource();
		assertNotNull(cursor);
		
		assertEquals(Status.CONFIGURATION_STATE_NOT_CONFIGURED, 
				getIntValueFromCursor(cursor, Status.CONFIGURATION_STATE));
		
		assertEquals(Status.NOTIFICATION_CHANNEL_STATE_OK,
				getIntValueFromCursor(cursor, Status.NOTIFICATION_CHANNEL_STATE));
		
		assertEquals(Status.DATA_CHANNEL_STATE_OK,
				getIntValueFromCursor(cursor, Status.DATA_CHANNEL_STATE));
		
		cursor.close();
	}

	public void testNoNotificationChannel() {
		mSourceManager.noNotificationChannel();
		
		Cursor cursor = mSourceManager.getOrangeSource();
		
		assertEquals(Status.NOTIFICATION_CHANNEL_STATE_NO_CONNECTION, 
				getIntValueFromCursor(cursor, Status.NOTIFICATION_CHANNEL_STATE));
		
		cursor.close();
	}

	public void testNoDataChannel() {
		mSourceManager.noDataChannel();
		
		Cursor cursor = mSourceManager.getOrangeSource();
		
		assertEquals(Status.DATA_CHANNEL_STATE_NO_CONNECTION, 
				getIntValueFromCursor(cursor, Status.DATA_CHANNEL_STATE));
		
		cursor.close();
	}

	public void testMessageWaiting() {
		mSourceManager.messageWaiting();
		
		Cursor cursor = mSourceManager.getOrangeSource();
		
		assertEquals(Status.NOTIFICATION_CHANNEL_STATE_MESSAGE_WAITING, 
				getIntValueFromCursor(cursor, Status.NOTIFICATION_CHANNEL_STATE));
		
		cursor.close();
	}

	public void testDataChannelOk() {
		mSourceManager.dataChannelOk();
		
		Cursor cursor = mSourceManager.getOrangeSource();
		
		assertEquals(Status.DATA_CHANNEL_STATE_OK, 
				getIntValueFromCursor(cursor, Status.DATA_CHANNEL_STATE));
		
		cursor.close();
	}

	public void testNotificationChannelOk() {
		mSourceManager.notificationChannelOk();
		
		Cursor cursor = mSourceManager.getOrangeSource();
		
		assertEquals(Status.NOTIFICATION_CHANNEL_STATE_OK, 
				getIntValueFromCursor(cursor, Status.NOTIFICATION_CHANNEL_STATE));
		
		cursor.close();
	}

	public void testUpdateTuiNumber() {
		mSourceManager.updateTuiNumber(TUI_NUMBER);
		
		Cursor cursor = mSourceManager.getOrangeSource();
		
		assertEquals(TEL_PREFIX + TUI_NUMBER, 
				getStringValueFromCursor(cursor, Status.VOICEMAIL_ACCESS_URI));
		
		cursor.close();
	}

	public void testCanBeConfigured() {
		mSourceManager.canBeConfigured();
		
		Cursor cursor = mSourceManager.getOrangeSource();
		
		assertEquals(Status.CONFIGURATION_STATE_CAN_BE_CONFIGURED, 
				getIntValueFromCursor(cursor, Status.CONFIGURATION_STATE));
		
		cursor.close();
	}

	public void testConfigured() {
		mSourceManager.configured();
		
		Cursor cursor = mSourceManager.getOrangeSource();
		
		assertEquals(Status.CONFIGURATION_STATE_OK, 
				getIntValueFromCursor(cursor, Status.CONFIGURATION_STATE));
		
		cursor.close();
	}

	public void testNotConfigured() {
		mSourceManager.notConfigured();
		
		Cursor cursor = mSourceManager.getOrangeSource();
		
		assertEquals(Status.CONFIGURATION_STATE_NOT_CONFIGURED, 
				getIntValueFromCursor(cursor, Status.CONFIGURATION_STATE));
		
		cursor.close();
	}
	
	public void testDeletion() {
		mSourceManager.unregister();
		
		Cursor cursor = mSourceManager.getOrangeSource();
		assertTrue(cursor.getCount() == 0);
		
		cursor.close();
	}
	
	private static int getIntValueFromCursor(Cursor cursor, String columnName) {
		try {
			return cursor.getInt(cursor.getColumnIndexOrThrow(columnName));
		} catch (IllegalArgumentException e) {
			return -1;
		}	
	}
	
	private static String getStringValueFromCursor(Cursor cursor, String columnName) {
		try {
			return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
		} catch (IllegalArgumentException e) {
			return "";
		}
	}
	
}
