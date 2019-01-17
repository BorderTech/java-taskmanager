package com.github.bordertech.taskmaster.service;

import com.github.bordertech.didums.Didums;
import com.github.bordertech.taskmaster.service.impl.ServiceHelperProviderDefault;

/**
 * Service invocation helper.
 */
public final class ServiceHelper {

	private static final ServiceHelperProvider PROVIDER = Didums.getService(ServiceHelperProvider.class, ServiceHelperProviderDefault.class);

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
