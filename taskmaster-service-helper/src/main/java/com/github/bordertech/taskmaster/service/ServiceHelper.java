package com.github.bordertech.taskmaster.service;

import com.github.bordertech.didums.Didums;
import com.github.bordertech.taskmaster.service.impl.ServiceHelperProviderDefault;

/**
 * Service invocation helper.
 *
 * @author jonathan
 */
public final class ServiceHelper {

	private static final ServiceHelperProvider PROVIDER;

	static {
		// Check if instance defined
		if (Didums.hasService(ServiceHelperProvider.class)) {
			PROVIDER = Didums.getService(ServiceHelperProvider.class);
		} else {
			// Default Implementation
			PROVIDER = new ServiceHelperProviderDefault();
		}
	}

	/**
	 * Private constructor.
	 */
	private ServiceHelper() {
	}

	/**
	 * @return the ServiceHelperProvider
	 */
	public static ServiceHelperProvider getProvider() {
		return PROVIDER;
	}

}
