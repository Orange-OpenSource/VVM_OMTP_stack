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

import android.content.Context;
import android.telephony.SmsManager;

import com.orange.labs.uk.omtp.account.OmtpAccountDatabase;
import com.orange.labs.uk.omtp.account.OmtpAccountStoreWrapper;
import com.orange.labs.uk.omtp.account.OmtpAccountStoreWrapperImpl;
import com.orange.labs.uk.omtp.db.DatabaseHelper;
import com.orange.labs.uk.omtp.fetch.VoicemailFetcher;
import com.orange.labs.uk.omtp.fetch.VoicemailFetcherFactory;
import com.orange.labs.uk.omtp.greetings.GreetingsHelper;
import com.orange.labs.uk.omtp.greetings.database.LocalGreetingsProvider;
import com.orange.labs.uk.omtp.imap.AsyncImapVoicemailFetcher;
import com.orange.labs.uk.omtp.imap.OmtpAsyncRequestSender;
import com.orange.labs.uk.omtp.imap.OmtpRequestor;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.notification.ProviderNotification;
import com.orange.labs.uk.omtp.notification.SourceNotifier;
import com.orange.labs.uk.omtp.notification.SourceNotifierImpl;
import com.orange.labs.uk.omtp.provider.OmtpProviderDatabase;
import com.orange.labs.uk.omtp.provider.OmtpProviderInfo;
import com.orange.labs.uk.omtp.provider.OmtpProviderWrapper;
import com.orange.labs.uk.omtp.provider.OmtpProviderWrapperImpl;
import com.orange.labs.uk.omtp.proxy.OmtpSmsManagerProxyImpl;
import com.orange.labs.uk.omtp.proxy.OmtpTelephonyManagerProxy;
import com.orange.labs.uk.omtp.proxy.OmtpTelephonyManagerProxyImpl;
import com.orange.labs.uk.omtp.service.fetch.GreetingsFetchController;
import com.orange.labs.uk.omtp.service.fetch.OmtpFetchController;
import com.orange.labs.uk.omtp.sms.OmtpMessageHandler;
import com.orange.labs.uk.omtp.sms.OmtpMessageHandlerImpl;
import com.orange.labs.uk.omtp.sms.OmtpMessageSender;
import com.orange.labs.uk.omtp.sms.OmtpMessageSenderImpl;
import com.orange.labs.uk.omtp.sms.OmtpSmsParser;
import com.orange.labs.uk.omtp.sms.OmtpSmsParserImpl;
import com.orange.labs.uk.omtp.sms.timeout.SmsTimeoutHandler;
import com.orange.labs.uk.omtp.sms.timeout.SmsTimeoutHandlerImpl;
import com.orange.labs.uk.omtp.sync.LocalGreetingsVvmStore;
import com.orange.labs.uk.omtp.sync.LocalVvmStore;
import com.orange.labs.uk.omtp.sync.MirrorVvmStore;
import com.orange.labs.uk.omtp.sync.OmtpVvmGreetingsStore;
import com.orange.labs.uk.omtp.sync.OmtpVvmStore;
import com.orange.labs.uk.omtp.sync.SerialSynchronizer;
import com.orange.labs.uk.omtp.sync.SyncResolver;
import com.orange.labs.uk.omtp.sync.SyncResolverImpl;
import com.orange.labs.uk.omtp.sync.TuiLanguageUpdaterImpl;
import com.orange.labs.uk.omtp.sync.VvmGreetingStoreResolverImpl;
import com.orange.labs.uk.omtp.sync.VvmGreetingsStore;
import com.orange.labs.uk.omtp.sync.VvmStore;
import com.orange.labs.uk.omtp.sync.VvmStoreResolverImpl;
import com.orange.labs.uk.omtp.sync.policies.GreetingsResolvePolicy;
import com.orange.labs.uk.omtp.sync.policies.NoLocalDeletionResolvePolicy;
import com.orange.labs.uk.omtp.voicemail.LocalVoicemailProvider;
import com.orange.labs.uk.omtp.voicemail.LocalVoicemailProviderImpl;
import com.orange.labs.uk.omtp.voicemail.database.MirrorVoicemailProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

/**
 * Concrete implementation of the {@link StackDependencyResolver} interface.
 * 
 * This class follows the Singleton pattern, and only a single instance is available for the whole
 * application.
 */
public class StackDependencyResolverImpl implements StackDependencyResolver {

	private static final Logger logger = Logger.getLogger(StackDependencyResolverImpl.class);

	/**
	 * Singleton Instance of the Dependency Resolver used for the whole application.
	 */
	private static StackDependencyResolverImpl sInstance = null;

	/**
	 * Initialize the singleton instance, it requires the application context.
	 * 
	 * @param appContext
	 *            Application context
	 * @return {@link StackDependencyResolverImpl} singleton instance
	 */
	public static synchronized StackDependencyResolverImpl initialize(final Context appContext) {
		if (sInstance == null) {
			sInstance = new StackDependencyResolverImpl(appContext);
		}
		return sInstance;
	}

    /**
     * Reset the singleton instance, it requires the application context
     * it may be need when for any reason the application is running with a new application context
     *
     * @param appContext New application context
     * @return {@link StackDependencyResolverImpl} singleton instance
     */
    public static synchronized  StackDependencyResolverImpl reset(final Context appContext) {
        sInstance = null;
        return StackDependencyResolverImpl.initialize(appContext);
    }

	/**
	 * Retrieve the singleton instance of the dependency resolver.
	 * 
	 * @return {@link StackDependencyResolverImpl} singleton instance
	 * @throws IllegalStateException
	 *             if the singleton has not been initialized.
	 */
	public static StackDependencyResolverImpl getInstance() {
		if (sInstance == null) {
			throw new IllegalStateException("The dependency resolver has not been initialized "
					+ "using the application context. Call StackDependencyResolverImpl.initialize");
		}
		return sInstance;
	}

	private final Context mApplicationContext;
	private final DatabaseHelper mProviderDatabaseHelper;

	private OmtpTelephonyManagerProxy mTelephonyManager;
	private OmtpAccountStoreWrapper mAccountStore;
	private OmtpProviderWrapper mProviderStore;
	private OmtpMessageHandler mMessageHandler;
	private SourceNotifier mSourceNotifier;
	private VoicemailFetcherFactory mVoicemailFetcherFactory;

	private ExecutorService mExecutorService;
	private ExecutorService mSingleExecutorService;

	private SerialSynchronizer mSerialSynchronizer;

	private LocalVoicemailProvider mVoicemailProvider;
	private MirrorVoicemailProvider mMirrorProvider;
	private LocalGreetingsProvider mGreetingsProvider;

	private VvmStore mLocalStore;
	private VvmStore mRemoteStore;
	private VvmStore mMirrorStore;
	private VvmGreetingsStore mGreetingsLocalStore;

	private OmtpRequestor mRequestor;
	private SmsTimeoutHandler mSmsTimeoutHandler;

	private GreetingsHelper mGreetingsHelper;



	/**
	 * Private Constructor
	 * 
	 * @param appContext
	 *            Application context
	 */
	protected StackDependencyResolverImpl(final Context appContext) {
		mApplicationContext = appContext;
		mProviderDatabaseHelper = new DatabaseHelper(mApplicationContext, getExecutorService());
	}

	@Override
	public Context getAppContext() {
		return mApplicationContext;
	}

	@Override
	public synchronized ExecutorService getExecutorService() {
		if (mExecutorService == null) {
			mExecutorService = Executors.newCachedThreadPool();
		}

		return mExecutorService;
	}
	
	@Override
	public synchronized ExecutorService getSingleExecutorService() {
		if (mSingleExecutorService == null) {
			mSingleExecutorService = Executors.newSingleThreadExecutor();
		}

		return mSingleExecutorService;
	}

	@Override
	public synchronized DatabaseHelper getProviderDatabaseHelper() {
		return mProviderDatabaseHelper;
	}

	@Override
	public synchronized OmtpProviderWrapper getProviderStore() {
		if (mProviderStore == null) {
			mProviderStore = new OmtpProviderWrapperImpl(new OmtpProviderDatabase(
					getProviderDatabaseHelper()), getTelephonyManager(), getSingleExecutorService());
		}

		return mProviderStore;
	}

	@Override
	public synchronized OmtpAccountStoreWrapper getAccountStore() {
		if (mAccountStore == null) {
			mAccountStore = new OmtpAccountStoreWrapperImpl(new OmtpAccountDatabase(
					getProviderDatabaseHelper()), getTelephonyManager(), getSingleExecutorService());
		}
		return mAccountStore;
	}

	@Override
	public synchronized OmtpTelephonyManagerProxy getTelephonyManager() {
		if (mTelephonyManager == null) {
			mTelephonyManager = new OmtpTelephonyManagerProxyImpl(getAppContext());
		}
		return mTelephonyManager;
	}

	@Override
	public synchronized SourceNotifier getSourceNotifier() {
		if (mSourceNotifier == null) {
			mSourceNotifier = new SourceNotifierImpl(getAppContext());
		}
		return mSourceNotifier;
	}

	@Override
	public synchronized VoicemailFetcherFactory getVoicemailFetcherFactory() {
		if (mVoicemailFetcherFactory == null) {
			mVoicemailFetcherFactory = createVoicemailFetcherFactory();
		}
		return mVoicemailFetcherFactory;
	}

	@Override
	public synchronized VvmStore getLocalStore() {
		if (mLocalStore == null) {
			mLocalStore = createLocalStore();
		}
		return mLocalStore;
	}

	@Override
	public synchronized VvmStore getRemoteStore() {
		if (mRemoteStore == null) {
			mRemoteStore = createRemoteStore();
		}
		return mRemoteStore;
	}
	
	public synchronized VvmGreetingsStore getRemoteGreetingStore() {
		return new OmtpVvmGreetingsStore(getVoicemailFetcherFactory(), getExecutorService(),
				getAppContext(), (MirrorVvmStore) getMirrorStore());
	}

	@Override
	public synchronized VvmStore getMirrorStore() {
		if (mMirrorStore == null) {
			mMirrorStore = createMirrorStore();
		}

		return mMirrorStore;
	}
	
	@Override
	public synchronized VvmGreetingsStore getGreetingsLocalStore() {
		if (mGreetingsLocalStore == null) {
			mGreetingsLocalStore = createGreetingsLocalStore();
		}
		
		return mGreetingsLocalStore;
	}

	@Override
	public synchronized SerialSynchronizer getSerialSynchronizer() {
		if (mSerialSynchronizer == null) {
			mSerialSynchronizer = new SerialSynchronizer(getAppContext(), getSourceNotifier(),
					getAccountStore());
		}

		return mSerialSynchronizer;
	}

	@Nullable
	@Override
	public OmtpMessageSender createOmtpMessageSender() {
		OmtpProviderInfo providerInfo = getProviderStore().getProviderInfo();
		if (providerInfo != null) {
			return new OmtpMessageSenderImpl(new OmtpSmsManagerProxyImpl(SmsManager.getDefault()),
                    getSmsTimeoutHandler(), getAccountStore(), providerInfo, getSourceNotifier(),
                    getAppContext(), getExecutorService());
		} else {
			logger.w("OmtpMessageSenderImpl has not ben created! providerInfo is null!");
			SourceNotifier sourceNotifier = getSourceNotifier();
			if (sourceNotifier != null) {
				sourceNotifier.sendNotification(ProviderNotification.error(getAppContext()));
			}
			return null;
		}
	}

	@Nullable
	@Override
	public synchronized OmtpMessageHandler createOmtpMessageHandler() {
		if (mMessageHandler == null) {
			OmtpSmsParser smsParser = createSmsParser();
			OmtpProviderInfo providerInfo = getProviderStore().getProviderInfo();
			if (smsParser != null && providerInfo != null) {
				mMessageHandler = new OmtpMessageHandlerImpl(smsParser, getAccountStore(),
						getSourceNotifier(), getLocalStore(), getSmsTimeoutHandler(),
						getSerialSynchronizer(), providerInfo);
			} else {
				logger.w("OmtpMessageHandlerImpl has not been created, smsParser or providerInfo are null");
			}
		}

		return mMessageHandler;
	}

	@Override
	public SyncResolver createSyncResolver() {
		return new SyncResolverImpl(new VvmStoreResolverImpl(), new NoLocalDeletionResolvePolicy(
				getVoicemailProvider(), getMirrorVoicemailProvider()), getRemoteStore(),
				getLocalStore(), getMirrorStore(), getExecutorService(),
				new VvmGreetingStoreResolverImpl(), getGreetingsHelper(), getGreetingsLocalStore(),
				getRemoteGreetingStore(), new GreetingsResolvePolicy(getLocalGreetingsProvider()),
				new TuiLanguageUpdaterImpl());
	}


	@Override
	public OmtpFetchController createFetchController() {
		return new OmtpFetchController(getAppContext(), getAccountStore(),
				getVoicemailFetcherFactory(), getVoicemailProvider(), getSourceNotifier());
	}
	
	@Override
	public GreetingsFetchController createGreetingsFetchController() {
		return new GreetingsFetchController(getAppContext(), getAccountStore(), getGreetingsHelper(),
				getVoicemailFetcherFactory(), getSourceNotifier(), getLocalGreetingsProvider());
	}
	
	@Override
	public synchronized OmtpRequestor getRequestor() {
		if (mRequestor == null) {
			mRequestor = new OmtpRequestor(getSourceNotifier(), new OmtpAsyncRequestSender(
					getAppContext(), getExecutorService(), getAccountStore()), getAppContext(), 
					getAccountStore());
		}
		return mRequestor;
	}

	@Override
	public synchronized SmsTimeoutHandler getSmsTimeoutHandler() {
		if (mSmsTimeoutHandler == null) {
			mSmsTimeoutHandler = new SmsTimeoutHandlerImpl();
		}
		return mSmsTimeoutHandler;
	}
	
	@Override
	public GreetingsHelper getGreetingsHelper() {
		if (mGreetingsHelper == null) {
			mGreetingsHelper = GreetingsHelper.newInstance(getAppContext(),
					getLocalGreetingsProvider());
		}
		return mGreetingsHelper;
	}

	private VoicemailFetcherFactory createVoicemailFetcherFactory() {
		return new VoicemailFetcherFactory() {

			@Override
			public VoicemailFetcher createVoicemailFetcher() {
				return new AsyncImapVoicemailFetcher(getAppContext(), getExecutorService(),
						getAccountStore(), getSourceNotifier());
			}
		};
	}

	@Nullable
	private OmtpSmsParser createSmsParser() {
		OmtpProviderInfo currentProvider = getProviderStore().getProviderInfo();
		if (currentProvider == null) {
			logger.w("Unable to find a provider corresponding to the currently inserted SIM.");
			return null;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(currentProvider.getDateFormat(), 
				Locale.UK);

		logger.d(String.format("Found following date format: %s", dateFormat.format(new Date(0))));

		return new OmtpSmsParserImpl(dateFormat);
	}

	/**
	 * Creates a new Local Store used to store locally the voicemail messages.
	 * 
	 * @return A {@link VvmStore} instance, here a {@link LocalVvmStore}.
	 */
	private VvmStore createLocalStore() {
		return new LocalVvmStore(getExecutorService(), getVoicemailProvider(), getAppContext(),
				(MirrorVvmStore) getMirrorStore());
	}

	/**
	 * Creates a new Remote Store used to store the voicemail messages on the remote platform.
	 * 
	 * @return A {@link VvmStore} instance, here a {@link OmtpVvmStore}.
	 */
	private VvmStore createRemoteStore() {
		return new OmtpVvmStore(getVoicemailFetcherFactory(), getExecutorService(),
				getAppContext(), (MirrorVvmStore) getMirrorStore());
	}

	private VvmStore createMirrorStore() {
		return new MirrorVvmStore(getExecutorService(), getMirrorVoicemailProvider());
	}
	
	private VvmGreetingsStore createGreetingsLocalStore() {
		return new LocalGreetingsVvmStore(getExecutorService(), getLocalGreetingsProvider(),
				getGreetingsHelper());
	}

	private synchronized LocalVoicemailProvider getVoicemailProvider() {
		if (mVoicemailProvider == null) {
			mVoicemailProvider = LocalVoicemailProviderImpl
					.createPackageScopedVoicemailProvider(getAppContext());
		}
		return mVoicemailProvider;
	}

	private synchronized MirrorVoicemailProvider getMirrorVoicemailProvider() {
		if (mMirrorProvider == null) {
			mMirrorProvider = new MirrorVoicemailProvider(getProviderDatabaseHelper());
		}

		return mMirrorProvider;
	}
	
	@Override
	public synchronized LocalGreetingsProvider getLocalGreetingsProvider() {
		if (mGreetingsProvider == null) {
			mGreetingsProvider = new LocalGreetingsProvider(getProviderDatabaseHelper());
		}
		
		return mGreetingsProvider;
	}
}
