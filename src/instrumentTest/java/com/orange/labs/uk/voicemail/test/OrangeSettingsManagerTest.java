package com.orange.labs.uk.voicemail.test;

import android.test.AndroidTestCase;

import com.orange.vvm.config.SettingsManager;
import com.orange.vvm.dependency.OrangeDependencyResolver;

public class OrangeSettingsManagerTest extends AndroidTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		try {
			OrangeDependencyResolver.initialize(getContext());
		} catch (IllegalStateException e) {
			// Do nothing.
		}
	}
	
	public void testProvidersKey() {
		OrangeDependencyResolver resolver = OrangeDependencyResolver.getInstance();
		SettingsManager settingsManager = resolver.getSettingsManager();
		
		// Clear settings before starting.
		settingsManager.clearSettings();
		
		assertFalse(settingsManager.areProvidersInserted());
		settingsManager.setProvidersInserted(true);
		assertTrue(settingsManager.areProvidersInserted());
		settingsManager.setProvidersInserted(false);
		
		// Clear settings now that we are done.
		settingsManager.clearSettings();
	}
	
}
