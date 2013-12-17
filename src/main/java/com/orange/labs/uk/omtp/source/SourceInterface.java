package com.orange.labs.uk.omtp.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.orange.labs.uk.omtp.account.OmtpAccountInfo;
import com.orange.labs.uk.omtp.greetings.GreetingType;
import com.orange.labs.uk.omtp.greetings.GreetingUpdateType;
import com.orange.labs.uk.omtp.provider.OmtpProviderInfo;

/**
 * Interface that describes the commands that can be executed by the OMTP stack. A source can use
 * that interface to easily trigger them instead of digging into the various components.
 */
public interface SourceInterface {

    /**
     * Retrieve a Provider available in the stack by its name
     *
     * @param providerName Name of the provider as filter
     * @return The {@link OmtpProviderInfo} found, null if it does not exists
     */
    @Nullable
    public abstract OmtpProviderInfo getProviderWithName(String providerName);

	/**
	 * This method retrieves the provider associated with the current inserted SIM if one is. It 
	 * uses the MCC + MNC fields of the SIM.
	 * @return
	 * 		the {@link OmtpProviderInfo} instance linked to the current SIM, null if none could be
	 * 		found, such as if the SIM provider is not supported, or the phone is in airplane mode.
	 */
	@Nullable
	public abstract OmtpProviderInfo getCurrentProvider();
	
	/**
	 * Retrieves the providers registered by the stack and valid with the current inserted SIM if one is. 
	 * @return
	 * 		A {@link List} of {@link OmtpProviderInfo} supported by the current SIM card
	 */
	public abstract List<OmtpProviderInfo> getSupportedProviders();

    /**
     * Insert/Update the provider information in the stack
     *
     * @param provider
     *             a {@link OmtpProviderInfo} that should
     *            be inserted/updated in the stack.
     * @return a boolean that indicates if the insertion/update has been done
     *         successfully.
     */
	public abstract boolean updateProvider(OmtpProviderInfo provider);

    /**
     * Insert/Update the provided OMTP providers information into the stack.
     * This initial step is needed to provide services to compatible SIMs.
     *
     * @param providers
     *            an {@link ArrayList} of {@link OmtpProviderInfo} that should
     *            be inserted/updated in the stack.
     *
     * @return a boolean that indicates if the insertion has been done
     *         successfully.
     */
    public abstract boolean updateProviders(ArrayList<OmtpProviderInfo> providers);

    /**
     * Remove the provider information from the stack
     *
     * @param provider
     *             a {@link OmtpProviderInfo} that should
     *            be removed from the stack.
     * @return a boolean that indicates if the removal has been done
     *         successfully.
     */
	public abstract boolean removeProvider(OmtpProviderInfo provider);
	
	/**
	 * This method retrieves the account associated to the currently inserted SIM if one exists.
	 * @return
	 * 		the {@link OmtpAccountInfo} instance linked to the current SIM, null if none could be
	 * 		found (such as when no SIM is inserted, or the SIM provider is not supported).
	 */
	@Nullable
	public abstract OmtpAccountInfo getCurrentAccount();
	
	/**
	 * Retrieves and return a {@link List} of every local {@link OmtpAccountInfo}.
	 */
	public abstract List<OmtpAccountInfo> getAllAccounts();

	/**
	 * Request the service status to the remote platform. The result of this
	 * operation is communicated through a Notification Intent containing the
	 * received status, or the details of the error if one occurred.
	 */
	public abstract void requestServiceStatus();

	/**
	 * Request the service activation to the remote platform. The result of this
	 * operation is communicated through a Notification Intent.
	 */
	public abstract void requestServiceActivation();

	/**
	 * Request the service deactivation to the remote platform. The result of
	 * this operation is communicated through a Notification Intent.
	 */
	public abstract void requestServiceDeactivation();

	/**
	 * Send the CLOSE NUT (New User Tutorial) command to the remote platform to pass the user from
	 * the NEW status to the READY status. This command should typically be sent after the tutorial
	 * has been done by the user.
	 */
	public abstract void requestCloseNut();

	/**
	 * Trigger a full synchronization with the remote OMTP platform.
	 */
	public abstract void triggerFullSynchronization();
	
	/**
	 * Trigger a synchronization that will detect local changes before synchronizing them with the
	 * remote OMTP platform.
	 */
	public abstract void triggerLocalSynchronization();
	
	/**
	 * Trigger suppression of all Voicemail data (Account + Voicemails) associated 
	 * with previous users of the phone.
	 */
	public abstract void wipeVoicemailData();
	
	/**
	 * Trigger suppression of all Greeting files stored in the phone.
	 */
	public abstract void wipeGreetingsData();
	
	/**
	 * Trigger synchronisation of greetings.
	 */
	public abstract void triggerGreetingsSynchronization(Set<GreetingUpdateType> newGreetingAction);
	
	/**
	 * Gets max allowed greeting length received in STATUS message.
	 * @return  max allowed greeting length
	 */
	public int getMaxNormalVoicemailGreetingLength();
	
	/**
	 * Gets max allowed name greeting length received in STATUS message.
	 * @return max allowed name greeting length
	 */
	public int getMaxVoicemailSignatureGreetingLength();
	
	/**
	 * Gets String containing supported languages list as received in STATUS message.
	 * @return supported languages list.
	 */
	public String getSupportedLanguages();
	
	/**
	 * Checks if the directory containing the file exists and returns
	 * the full path to the requested greeting file.
	 * @param greeitngType
	 * @return path to name greetings file
	 */
	public String getGreetingFilePatch(GreetingType greeitngType);
	
	/**
	 * Requests TUI language change.
	 * @param languageNumber
	 */
	public void requestTuiLanguageChange(int languageNumber);
	
	/**
	 * Gets greeting type currently set on the server.
	 * @return greeting type.
	 */
	public GreetingType getCurrentActiveGreeting();
	
	/**
	 * Gets greeting file length (in bytes).
	 * @param greetingType
	 * @return greeting length
	 */
	public long getGreetingSize(GreetingType greetingType);
	
	/**
	 * Sets not downloaded state for a given Greeting Upload type.
	 * @return
	 */
	public boolean setGreetingNotDownloadedState(GreetingType greetingType);

}