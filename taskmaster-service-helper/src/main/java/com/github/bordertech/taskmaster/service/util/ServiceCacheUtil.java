package com.github.bordertech.taskmaster.service.util;

import com.github.bordertech.taskmaster.cache.CachingHelper;
import com.github.bordertech.taskmaster.service.ResultHolder;
import com.github.bordertech.taskmaster.service.impl.ServiceHelperProperties;
import javax.cache.Cache;
import javax.cache.expiry.Duration;

/**
 * Helper utility for sync and async service calls.
 * <p>
 * This helper provides a default cache or projects can create caches with assigned names.
 * </p>
 */
public final class ServiceCacheUtil {

	/**
	 * Private constructor.
	 */
	private ServiceCacheUtil() {
		// Do nothing
	}

	/**
	 * Provide a default result holder cache with the default duration.
	 *
	 * @return the default result holder cache instance
	 */
	public static Cache<String, ResultHolder> getDefaultResultHolderCache() {
		String name = ServiceHelperProperties.getDefaultResultHolderCacheName();
		Duration duration = ServiceHelperProperties.getDefaultResultHolderCacheDuration();
		return getResultHolderCache(name, duration);
	}

	/**
	 * Provide a result holder cache with an assigned cache name with default duration.
	 *
	 * @param name the cache name
	 * @return the cache instance
	 */
	public static Cache<String, ResultHolder> getResultHolderCache(final String name) {
		Duration duration = ServiceHelperProperties.getResultHolderCacheDuration(name);
		return getResultHolderCache(name, duration);
	}

	/**
	 * Provide a result holder cache with an assigned cache name and duration.
	 *
	 * @param name the cache name
	 * @param duration the time to live for cached items
	 * @return the cache instance
	 */
	public static Cache<String, ResultHolder> getResultHolderCache(final String name, final Duration duration) {
		return CachingHelper.getOrCreateCache(name, String.class, ResultHolder.class, duration);
	}

}
