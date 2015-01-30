/*
 * Copyright (C) 2012 Orange Labs UK. All Rights Reserved.
 * Copyright (C) 2011 The Android Open Source Project
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
package com.orange.labs.uk.omtp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.VoicemailContract;

import com.orange.labs.uk.omtp.dependency.StackDependencyResolverImpl;
import com.orange.labs.uk.omtp.logging.Logger;
import com.orange.labs.uk.omtp.sync.SerialSynchronizer.SyncFlag;

/**
 * A broadcast receiver that listens to provider change events from vvm content provider and if
 * needed, triggers a sync of local changes to the server.
 */
public class VoicemailProviderChangeReceiver extends BroadcastReceiver {
	private static final Logger logger = Logger.getLogger(VoicemailProviderChangeReceiver.class);

	@Override
	public void onReceive(Context context, Intent intent) {
		logger.d(String.format("New intent received: %s", intent));
		if (Intent.ACTION_PROVIDER_CHANGED.equals(intent.getAction())) {
			if (!intent.hasExtra(VoicemailContract.EXTRA_SELF_CHANGE)) {
				logger.e(String.format("Extra %s not found in intent. Ignored!",
						VoicemailContract.EXTRA_SELF_CHANGE));
				return;
			}

			// Sync is required only if the change was not triggered by self.
			if (!intent.getBooleanExtra(VoicemailContract.EXTRA_SELF_CHANGE, false)) {
				logger.d("not by self, performing local change detection");
				StackDependencyResolverImpl.getInstance().getSerialSynchronizer()
						.execute(SyncFlag.LOCAL_SYNCHRONIZATION);
			} else {
			logger.d("Changed by self. Ignored!");
			}
		}
	}
}
