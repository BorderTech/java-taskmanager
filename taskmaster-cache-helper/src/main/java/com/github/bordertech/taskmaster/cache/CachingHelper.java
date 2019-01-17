package com.github.bordertech.taskmaster.cache;

import com.github.bordertech.didums.Didums;
import com.github.bordertech.taskmaster.cache.impl.CachingHelperProviderDefault;

/**
 * Caching helper based on JSR 107.
 * <p>
 * Allows projects to provide a different mechanism for creating their cache requirements.
 * </p>
 *
 * @author jonathan
 */
public final class CachingHelper {

	private static final CachingHelperProvider PROVIDER = Didums.getService(CachingHelperProvider.class, CachingHelperProviderDefault.class);

	/**
	 * Private constructor.
	 */
	private CachingHelper() {
	}

	/**
	 * @return the CachingHelper Provider
	 */
	public static CachingHelperProvider getProvider() {
		return PROVIDER;
	}

}
