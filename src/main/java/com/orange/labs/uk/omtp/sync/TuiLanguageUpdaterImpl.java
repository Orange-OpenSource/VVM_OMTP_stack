package com.orange.labs.uk.omtp.sync;

import android.content.Context;

import com.android.email.mail.MessagingException;
import com.orange.labs.uk.omtp.account.OmtpAccountInfo;
import com.orange.labs.uk.omtp.account.OmtpAccountStoreWrapper;
import com.orange.labs.uk.omtp.callbacks.Callback;
import com.orange.labs.uk.omtp.imap.OmtpImapStore;
import com.orange.labs.uk.omtp.logging.Logger;

/**
 * Contains logic necessary to send IMAP commands to update TUI language.
 */
public class TuiLanguageUpdaterImpl implements TuiLanguageUpdater {

	private static Logger logger = Logger.getLogger(TuiLanguageUpdaterImpl.class);
	
	@Override
	public void updateTuiLanguage(Callback<Void> callback, int languageId, Context context,
			OmtpAccountStoreWrapper accountStore) {
		OmtpAccountInfo accountInfo = accountStore.getAccountInfo();
		OmtpImapStore store = null;
		try {
			store = OmtpImapStore.newInstance(accountInfo.getUriString(), context, null);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			logger.e("Cannot change TUI language, Imap Store creation failed.");
		}
		
		if (store != null) {
			store.changeTuiLanguage(languageId, callback);
		} else {
			// Notify of the failure.
			callback.onFailure(new IllegalStateException("Impossible to instantiate a new" +
					" OMTP IMAP store."));
		}
		
	}


}
