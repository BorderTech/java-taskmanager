package com.github.bordertech.taskmaster.service.util;

import com.github.bordertech.taskmaster.cache.CachingHelper;
import com.github.bordertech.taskmaster.cache.impl.CachingProperties;
import com.github.bordertech.taskmaster.service.ResultHolder;
import com.github.bordertech.taskmaster.service.impl.ServiceHelperProperties;
import javax.cache.Cache;
import javax.cache.expiry.Duration;

/**
 * Helper utility for sync and async service calls.
 * <p>
 * This helper provides a default cache or projects can create caches with assigned names.
 * </p>
 * <p>
 * The duration of the caches can be overridden by setting the runtime properties for {@link CachingProperties}.
 * </p>
 *
 * @see CachingProperties
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
		return getResultHolderCache(ServiceHelperProperties.RESULT_HOLDER_CACHE_NAME);
	}

	/**
	 * Provide a result holder cache with an assigned cache name with default duration.
	 *
	 * @param name the cache name
	 * @return the cache instance
	 */
	public static Cache<String, ResultHolder> getResultHolderCache(final String name) {
		return getResultHolderCache(name, ServiceHelperProperties.getResultHolderCacheDuration());
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
