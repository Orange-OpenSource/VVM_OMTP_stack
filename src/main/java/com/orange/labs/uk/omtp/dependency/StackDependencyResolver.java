/*
 * Copyright (C) 2012 Orange Labs UK. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package com.orange.labs.uk.omtp.dependency;

import java.util.concurrent.ExecutorService;

import android.content.Context;

import com.orange.labs.uk.omtp.account.OmtpAccountStoreWrapper;
import com.orange.labs.uk.omtp.db.DatabaseHelper;
import com.orange.labs.uk.omtp.fetch.VoicemailFetcherFactory;
import com.orange.labs.uk.omtp.greetings.GreetingsHelper;
import com.orange.labs.uk.omtp.greetings.database.LocalGreetingsProvider;
import com.orange.labs.uk.omtp.imap.OmtpRequestor;
import com.orange.labs.uk.omtp.notification.SourceNotifier;
import com.orange.labs.uk.omtp.provider.OmtpProviderWrapper;
import com.orange.labs.uk.omtp.proxy.OmtpTelephonyManagerProxy;
import com.orange.labs.uk.omtp.service.fetch.GreetingsFetchController;
import com.orange.labs.uk.omtp.service.fetch.OmtpFetchController;
import com.orange.labs.uk.omtp.sms.OmtpMessageHandler;
import com.orange.labs.uk.omtp.sms.OmtpMessageSender;
import com.orange.labs.uk.omtp.sms.timeout.SmsTimeoutHandler;
import com.orange.labs.uk.omtp.sync.SerialSynchronizer;
import com.orange.labs.uk.omtp.sync.SyncResolver;
import com.orange.labs.uk.omtp.sync.VvmStore;

//TODO: Document this class.
/**
 * Interface for creating objects used across the application.
 * <p>
 * It is expected that only activities, services and receivers, that can only have default
 * constructor (and hence cannot accept dependencies through constructor), should depend on
 * dependency resolver
 * <p>
 * Methods starting with the prefix "get" will always return the same instance. The instance may be
 * created lazily, i.e., when first requested. The "get" methods should not have any arguments. The
 * members created here will be singletons, no methods are provided for deleting the object and
 * consequently they will all persist for the lifetime of the app, they should only be added for
 * objects that need to be shared as a single instance across multiple activities or services.
 * <p>
 * Methods starting with the prefix "create" will always return a brand-new instance. The "create"
 * methods may have arguments.
 */
public interface StackDependencyResolver {
    /**
     * The {@link Context} associated with the application, set during application's onCreate().
     */
    public Context getAppContext();
    
    /**
     * Returns the singleton instance of {@link DatabaseHelper} held by dependency
     * resolver.
     * <p>
     * This returns an {@link DatabaseHelper} that can be used to access the stack
     * database.
     */
    public DatabaseHelper getProviderDatabaseHelper();
    
    public OmtpTelephonyManagerProxy getTelephonyManager();

	public OmtpMessageSender createOmtpMessageSender();

	public OmtpProviderWrapper getProviderStore();

	public OmtpAccountStoreWrapper getAccountStore();
	
	public OmtpMessageHandler createOmtpMessageHandler();
	
	/**
	 * Returns the {@link SourceNotifier} instance for the stack, creating it if needed.
	 */
	public SourceNotifier getSourceNotifier();

	public VoicemailFetcherFactory getVoicemailFetcherFactory();
	
	public SyncResolver createSyncResolver();

	public VvmStore getLocalStore();

	public VvmStore getRemoteStore();
	
	public VvmStore getMirrorStore();
	
	/**
	 * Returns a {@link OmtpRequestor} used to send OMTP commands to the platform.
	 */
	public OmtpRequestor getRequestor();

	public SmsTimeoutHandler getSmsTimeoutHandler();

	public ExecutorService getExecutorService();

	public ExecutorService getSingleExecutorService();

	public SerialSynchronizer getSerialSynchronizer();

	public OmtpFetchController createFetchController();
	
	public GreetingsFetchController createGreetingsFetchController();
	
	public GreetingsHelper getGreetingsHelper();

	public VvmStore getGreetingsLocalStore();
	
	public LocalGreetingsProvider getLocalGreetingsProvider();

}
