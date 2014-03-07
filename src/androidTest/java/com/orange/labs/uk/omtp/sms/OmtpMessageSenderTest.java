package com.orange.labs.uk.omtp.sms;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.telephony.SmsManager;
import android.test.AndroidTestCase;

import com.orange.labs.uk.omtp.account.OmtpAccountInfoTest;
import com.orange.labs.uk.omtp.dependency.StackDependencyResolverImpl;
import com.orange.labs.uk.omtp.dependency.StackDependencyResolver;
import com.orange.labs.uk.omtp.provider.OmtpProviderInfoTest;
import com.orange.labs.uk.omtp.proxy.OmtpSmsManagerProxyImpl;

public class OmtpMessageSenderTest extends AndroidTestCase {

	private StackDependencyResolver omtpDependencyResolver = null;
	private OmtpMessageSender omtpMessageSender = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		OmtpAccountInfoTest omtpAccountInfoTest = new OmtpAccountInfoTest();
		omtpAccountInfoTest.testAccountInfoCreation();
		OmtpProviderInfoTest omtpProviderInfoTest = new OmtpProviderInfoTest();
		omtpProviderInfoTest.testProviderInfoCreation();
		try{
			StackDependencyResolverImpl.initialize(getContext());
		} catch (IllegalStateException ise){
			// do nothing
		}
		ExecutorService executorService = Executors.newCachedThreadPool();
		omtpDependencyResolver = StackDependencyResolverImpl.getInstance();

		omtpMessageSender = new OmtpMessageSenderImpl(new OmtpSmsManagerProxyImpl(
				SmsManager.getDefault()), omtpDependencyResolver.getAccountStore(),
				omtpDependencyResolver.getProviderStore().getProviderInfo(),
				omtpDependencyResolver.getSourceNotifier(), getContext(), executorService);
	}

	public void testRequestVvmActivation() {
		omtpMessageSender.requestVvmActivation();
		fail("Not yet implemented");
	}

	public void testRequestVvmDeactivation() {
		omtpMessageSender.requestVvmDeactivation();
		fail("Not yet implemented");
	}

	public void testRequestVvmStatus() {
		omtpMessageSender.requestVvmStatus();
		fail("Not yet implemented");
	}

}
