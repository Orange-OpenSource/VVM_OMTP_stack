package com.orange.labs.uk.omtp.notification;

import android.os.Bundle;

public abstract class AbstractNotification implements SourceNotification {

	private String mAction;
	private Bundle mBundle;

	protected AbstractNotification(String action, Bundle bundle) {
		mAction = action;
		mBundle = bundle;
	}

	@Override
	public String getAction() {
		return mAction;
	}

	@Override
	public Bundle getBundle() {
		return mBundle;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("Notification: [mAction: ");
		sb.append(mAction);
		sb.append("; mBundle: {");
		Object value = null;
		for (String key : mBundle.keySet()) {
			if ((value = mBundle.get(key)) != null) {
				sb.append(key);
				sb.append("=");
				sb.append(value.toString());
				sb.append(" , ");
			}
		}
		sb.append(" }]");
		return sb.toString();
	}

}
