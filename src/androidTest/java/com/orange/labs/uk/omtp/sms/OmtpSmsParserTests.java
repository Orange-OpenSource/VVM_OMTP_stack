package com.orange.labs.uk.omtp.sms;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.test.AndroidTestCase;

import com.orange.labs.uk.omtp.sms.OmtpMessage;
import com.orange.labs.uk.omtp.sms.OmtpParseException;
import com.orange.labs.uk.omtp.sms.OmtpSmsParser;
import com.orange.labs.uk.omtp.sms.OmtpSmsParserImpl;

public class OmtpSmsParserTests extends AndroidTestCase {

	private OmtpSmsParser smsParser;
	// Omtp v1.2 example test messages
	private final static String STATUS_TEST_SMS = "//VVM:STATUS:st=N;rc=0;srv=1:10.115.67.251;tui=123;dn=999;ipt=143;spt=25; u=78236487@wirelesscarrier.com;pw=32u4yguetrr34;lang=eng|fre;g_len=25;vs_len=15;pw_len=4-6;smtp_u=super_user@wirelesscarrier.com;smtp_pw=48769463wer;pm=Y;gm=N";
	private final static String STATUS_TEST_SMS2 = "//VVM:STATUS:st=N;rc=0;srv=1:10.115.67.251;tui=123;dn=999;ipt=143;spt=25; u=78236487@wirelesscarrier.com;pw=32u4yguetrr34;lang=1|2|3|4;g_len=25;vs_len=15;pw_len=4-6;smtp_u=super_user@wirelesscarrier.com;smtp_pw=48769463wer;pm=Y;gm=N";
	private final static String STATUS_TEST_SMS3 = "//VVM:STATUS:st=N;rc=0;srv=1:10.115.67.251;tui=123;dn=999;ipt=143i;spt=25; u=78236487@wirelesscarrier.com;pw=32u4yguetrr34;lang=1|2|3|4;g_len=25;vs_len=15;pw_len=4-6;smtp_u=super_user@wirelesscarrier.com;smtp_pw=48769463wer;pm=Y;gm=N";
	private final static String SYNC_TEST_SMS = "//VVM:SYNC:ev=NM;id=3446456;c=1;t=v;s=01234567898;dt=02/08/2008 12:53 +0200;l=30";
	private final static String SYNC_TEST_SMS2 = "//VVM:SYNC:ev=NM;id=3446456;c=1;t=v;dt=02/08/2008 12:53 +0200";
	private final static String SYNC_TEST_SMS3 = "//VVM:SYNC:ev=NM;txt=You have received a new message, please dial 0296086300;id=24;c=1;t=v;s=0476613475;dt=31/05/2012 17:53 +0200;l=7";
	private final static String SYNC_TEST_SMS4 = "//VVM:SYNC:ev=NM;txt=You have received a new message, please dial 0296086300;id=26;c=2;t=v;s=;dt=01/06/2012 11:30 +0200;l=5";
	
	private final static String NUMBER = "0607080910";


	protected void setUp() throws Exception {
		super.setUp();
		smsParser = new OmtpSmsParserImpl(new SimpleDateFormat("dd/MM/yyyy HH:mm z", Locale.FRANCE));

	}
	
	public void testParseStatuscSms() {
		// Omtp.STATUS_SMS_PREFIX
		try {
			OmtpMessage statusMessagePardsed = smsParser.parse(STATUS_TEST_SMS,NUMBER);
		} catch (OmtpParseException e) {
			e.printStackTrace();
			fail("Parser Exception thrown!");
		}
	}
	
	public void testParseStatuscSms2() {
		// Omtp.STATUS_SMS_PREFIX
		try {
			OmtpMessage statusMessagePardsed = smsParser.parse(STATUS_TEST_SMS2,NUMBER);
		} catch (OmtpParseException e) {
			e.printStackTrace();
			fail("Parser Exception thrown!");
		}
	}
	
	public void testParseStatuscSms3() {
		// Omtp.STATUS_SMS_PREFIX
		try {
			OmtpMessage statusMessagePardsed = smsParser.parse(STATUS_TEST_SMS3,NUMBER);
		} catch (OmtpParseException e) {
			e.printStackTrace();
			fail("Parser Exception thrown!");
		}
	}

	public void testParseSyncSms() {
		// Omtp.SYNC_SMS_PREFIX
		try {
			OmtpMessage syncMessagePardsed = smsParser.parse(SYNC_TEST_SMS,NUMBER);
		} catch (OmtpParseException e) {
			e.printStackTrace();
			fail("Parser Exception thrown!" + e.getMessage());
		}
	}
	
	public void testParseSyncSms2() {
		// Omtp.SYNC_SMS_PREFIX
		try {
			OmtpMessage syncMessagePardsed = smsParser.parse(SYNC_TEST_SMS2,NUMBER);
		} catch (OmtpParseException e) {
			e.printStackTrace();
			fail("Parser Exception thrown!" + e.getMessage());
		}
	}
	
	public void testParseSyncSms3() {
		// Omtp.SYNC_SMS_PREFIX
		try {
			OmtpMessage syncMessagePardsed = smsParser.parse(SYNC_TEST_SMS3,NUMBER);
		} catch (OmtpParseException e) {
			e.printStackTrace();
			fail("Parser Exception thrown!" + e.getMessage());
		}
	}
	
	public void testParseSyncSms4() {
		// Omtp.SYNC_SMS_PREFIX
		try {
			OmtpMessage syncMessagePardsed = smsParser.parse(SYNC_TEST_SMS4,NUMBER);
		} catch (OmtpParseException e) {
			e.printStackTrace();
			fail("Parser Exception thrown!" + e.getMessage());
		}
	}

}
