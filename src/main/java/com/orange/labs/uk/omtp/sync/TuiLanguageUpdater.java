package com.orange.labs.uk.omtp.sync;

import android.content.Context;

import com.orange.labs.uk.omtp.account.OmtpAccountStoreWrapper;
import com.orange.labs.uk.omtp.callbacks.Callback;

public interface TuiLanguageUpdater {

	/**
	 * Sends IMAP commands to the server in order to update TUI language settings.
	 * @param callback 
	 * 
	 * @param languageId
	 * @param accountStore 
	 * @param context 
	 */
	void updateTuiLanguage(Callback<Void> callback, int languageId, Context context,
			OmtpAccountStoreWrapper accountStore);
}
