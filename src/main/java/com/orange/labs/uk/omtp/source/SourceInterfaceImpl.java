package com.orange.labs.uk.omtp.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import android.content.Context;

import com.orange.labs.uk.omtp.account.OmtpAccountInfo;
import com.orange.labs.uk.omtp.callbacks.Callbacks;
import com.orange.labs.uk.omtp.dependency.StackDependencyResolver;
import com.orange.labs.uk.omtp.dependency.StackDependencyResolverImpl;
import com.orange.labs.uk.omtp.greetings.GreetingType;
import com.orange.labs.uk.omtp.greetings.GreetingUpdateType;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.provider.OmtpProviderInfo;
import com.orange.labs.uk.omtp.sms.OmtpMessageSender;
import com.orange.labs.uk.omtp.sync.SerialSynchronizer.SyncFlag;

public class SourceInterfaceImpl implements SourceInterface {
	
	private static final Logger logger = Logger.getLogger(SourceInterfaceImpl.class);

	private StackDependencyResolver mDependencyResolver;

	public SourceInterfaceImpl(Context context) {
		mDependencyResolver = StackDependencyResolverImpl.initialize(context);
	}

    public SourceInterfaceImpl(Context applicationContext, boolean resetSourceInterface) {
        if(resetSourceInterface) {
            mDependencyResolver = StackDependencyResolverImpl.reset(applicationContext);
        }
        else {
            mDependencyResolver = StackDependencyResolverImpl.initialize(applicationContext);
        }
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.orange.labs.uk.omtp.source.SourceInterface#updateProviders(java.util.ArrayList)
	 */
	@Override
	public boolean updateProviders(ArrayList<OmtpProviderInfo> providers) {
		return mDependencyResolver.getProviderStore().updateProvidersInfo(providers);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.orange.labs.uk.omtp.source.SourceInterface#updateProvider(com.orange.labs.uk.omtp.provider.OmtpProviderInfo)
	 */
	@Override
	public boolean updateProvider(OmtpProviderInfo provider) {
		return mDependencyResolver.getProviderStore().updateProviderInfo(provider);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.orange.labs.uk.omtp.source.SourceInterface#getSupportedProviders()
	 */
	@Override
	public List<OmtpProviderInfo> getSupportedProviders() {
		return mDependencyResolver.getProviderStore().getSupportedProviders();
	}
	
	@Override
	public boolean removeProvider(OmtpProviderInfo provider) {
		return mDependencyResolver.getProviderStore().removeProviderInfo(provider);
	}
	
	@Override
	public OmtpProviderInfo getProviderWithName(String providerName) {
		return mDependencyResolver.getProviderStore().getProviderInfo(providerName);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.orange.labs.uk.omtp.source.SourceInterface#getCurrentAccount()
	 */
	@Override
	@Nullable
	public OmtpAccountInfo getCurrentAccount() {
		return mDependencyResolver.getAccountStore().getAccountInfo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.orange.labs.uk.omtp.source.SourceInterface#getAllAccounts()
	 */
	@Override
	public List<OmtpAccountInfo> getAllAccounts() {
		return mDependencyResolver.getAccountStore().getAllAccounts();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.orange.labs.uk.omtp.source.SourceInterface#getCurrentProvider()
	 */
	@Override
	@Nullable
	public OmtpProviderInfo getCurrentProvider() {
		return mDependencyResolver.getProviderStore().getProviderInfo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.orange.labs.uk.omtp.source.SourceInterface#requestServiceStatus()
	 */
	@Override
	public void requestServiceStatus() {
		OmtpMessageSender omtpMessageSender = mDependencyResolver.createOmtpMessageSender();
		if (omtpMessageSender != null) {
			omtpMessageSender.requestVvmStatus();
		} else {
			logger.w("Unalbe to request service status, message sender is null!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.orange.labs.uk.omtp.source.SourceInterface#requestServiceActivation()
	 */
	@Override
	public void requestServiceActivation() {
		OmtpMessageSender omtpMessageSender = mDependencyResolver.createOmtpMessageSender();
		if (omtpMessageSender != null) {
			omtpMessageSender.requestVvmActivation();
		} else {
			logger.w("Unalbe to request service activation, message sender is null!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.orange.labs.uk.omtp.source.SourceInterface#requestServiceDeactivation()
	 */
	@Override
	public void requestServiceDeactivation() {
		OmtpMessageSender omtpMessageSender = mDependencyResolver.createOmtpMessageSender();
		if (omtpMessageSender != null) {
			omtpMessageSender.requestVvmDeactivation();
		} else {
			logger.w("Unalbe to request service deactivation, message sender is null!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.orange.labs.uk.omtp.source.SourceInterface#requestCloseNut()
	 */
	@Override
	public void requestCloseNut() {
		mDependencyResolver.getRequestor().closeNut();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.orange.labs.uk.omtp.source.SourceInterface#triggerFullSynchronization()
	 */
	@Override
	public void triggerFullSynchronization() {
		mDependencyResolver.getSerialSynchronizer().execute(SyncFlag.FULL_SYNCHRONIZATION);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.orange.labs.uk.omtp.source.SourceInterface#triggerLocalSynchronization()
	 */
	@Override
	public void triggerLocalSynchronization() {
		mDependencyResolver.getSerialSynchronizer().execute(SyncFlag.LOCAL_SYNCHRONIZATION);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.orange.labs.uk.omtp.source.SourceInterface#wipeVoicemailData()
	 */
	@Override
	public void wipeVoicemailData() {
		// Delete accounts
		mDependencyResolver.getAccountStore().deleteAll();
		// delete voicemail messages for this account from LovalVvmStore and MirrorVvmStore
		mDependencyResolver.getLocalStore().deleteAllMessages(Callbacks.<Void>emptyCallback());
		mDependencyResolver.getMirrorStore().deleteAllMessages(Callbacks.<Void>emptyCallback());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.orange.labs.uk.omtp.source.SourceInterface#wipeGreetingsData()
	 */
	@Override
	public void wipeGreetingsData() {
		mDependencyResolver.getGreetingsHelper().deleteAllGreetingFiles();
		// delete local Greetings messages
		mDependencyResolver.getGreetingsLocalStore().deleteAllMessages(Callbacks.<Void>emptyCallback());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.orange.labs.uk.omtp.source.SourceInterface#triggerGreetingsSynchronization
	 */
	@Override
	public void triggerGreetingsSynchronization(Set<GreetingUpdateType> newGreetingToUpload) {
		mDependencyResolver.getSerialSynchronizer().executeGreeting(newGreetingToUpload);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.orange.labs.uk.omtp.source.SourceInterface#getMaxNormalVoicemailLength
	 */
	@Override
	@Nullable
	public int getMaxNormalVoicemailGreetingLength() {
		int maxNormalVoicemailGreetingLength = 0;
		OmtpAccountInfo account = getCurrentAccount();
		if (account != null) {
			maxNormalVoicemailGreetingLength = account.getMaxAllowedGreetingsLength();
		}
		return maxNormalVoicemailGreetingLength;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.orange.labs.uk.omtp.source.SourceInterface#getMaxVoicemailSignatureLength
	 */
	@Override
	@Nullable
	public int getMaxVoicemailSignatureGreetingLength() {
		int maxVoicemailSignatureGreetingLength = 0;
		OmtpAccountInfo account = getCurrentAccount();
		if (account != null) {
			maxVoicemailSignatureGreetingLength = account.getMaxAllowedVoiceSignatureLength();
		}
		return maxVoicemailSignatureGreetingLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.orange.labs.uk.omtp.source.SourceInterface#getSupportedLanguages
	 */
	@Override
	@Nullable
	public String getSupportedLanguages() {
		OmtpAccountInfo account = getCurrentAccount();
		if (account != null) {
			return account.getSupportedLnaguages();
		} else {
			return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.orange.labs.uk.omtp.source.SourceInterface#getGreetingFilePatch(com.orange.labs.uk.omtp
	 * .greetings.GreetingType)
	 */
	@Override
	@Nullable
	public String getGreetingFilePatch(GreetingType greeitngType) {
		return mDependencyResolver.getGreetingsHelper().getFilePath(greeitngType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.orange.labs.uk.omtp.source.SourceInterface#requestTuiLanguageChange
	 */
	@Override
	public void requestTuiLanguageChange(int languageNumber) {
		mDependencyResolver.getSerialSynchronizer().executeTuiChange(SyncFlag.TUI_LANGUAGE_CHANGE,
				languageNumber);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.orange.labs.uk.omtp.source.SourceInterface#getCurrentActiveGreeting
	 */
	@Override
	public GreetingType getCurrentActiveGreeting() {
		return mDependencyResolver.getGreetingsHelper().getCurrentActiveGreeting();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.orange.labs.uk.omtp.source.SourceInterface#getGreetingSize(com.orange.labs.uk.omtp.greetings.GreetingType)
	 */
	@Override
	public long getGreetingSize(GreetingType greetingType) {
		return mDependencyResolver.getGreetingsHelper().getGreetingFileSize(greetingType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.orange.labs.uk.omtp.source.SourceInterface#setGreetingDownloadedState(com.orange.labs
	 * .uk.omtp.greetings.GreetingType)
	 */
	@Override
	public boolean setGreetingNotDownloadedState(GreetingType greetingType) {
		mDependencyResolver.getLocalGreetingsProvider().setDownloadedStateFalse(greetingType);
		return false;
	}

}
