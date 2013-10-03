package com.orange.labs.uk.omtp.notification;

import android.os.Bundle;

public final class ChangeTuiLanguageNotification extends AbstractNotification {

	private ChangeTuiLanguageNotification(Bundle bundle) {
		super(SourceNotification.CHANGE_TUI_LANGUAGE_ACTION, bundle);
	}

	public static ChangeTuiLanguageNotification reportUpdateError() {
		return new Builder().build();
	}
	
	public static class Builder {
		private Bundle mBundle = new Bundle();
	
		public ChangeTuiLanguageNotification build() {
			return new ChangeTuiLanguageNotification(mBundle);
		}
	}
}
