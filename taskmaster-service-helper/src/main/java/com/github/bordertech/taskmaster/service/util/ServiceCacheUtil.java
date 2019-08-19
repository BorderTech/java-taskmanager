package com.github.bordertech.taskmaster.service.util;

import com.github.bordertech.config.Config;
import com.github.bordertech.taskmaster.cache.CachingHelper;
import com.github.bordertech.taskmaster.service.ResultHolder;
import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import javax.cache.expiry.Duration;

/**
 * Helper utility for sync and async service calls.
 * <p>
 * This helper provides a default cache or projects can create caches with assigned names.
 * </p>
 */
public final class ServiceCacheUtil {

	private static final String DEFAULT_RESULT_CACHE_NAME = "taskmaster-resultholder-default";
	private static final Long DEFAULT_RESULT_HOLDER_DURATION_SECONDS
			= Config.getInstance().getLong("bordertech.taskmaster.service.resultholder.cache.duration", Long.valueOf("1800"));
	private static final Duration DEFAULT_RESULT_DURATION = new Duration(TimeUnit.SECONDS, DEFAULT_RESULT_HOLDER_DURATION_SECONDS);

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
		return getResultHolderCache(DEFAULT_RESULT_CACHE_NAME, DEFAULT_RESULT_DURATION);
	}

	/**
	 * Provide a result holder cache with an assigned cache name with default duration.
	 *
	 * @param name the cache name
	 * @return the cache instance
	 */
	public static Cache<String, ResultHolder> getResultHolderCache(final String name) {
		return getResultHolderCache(name, DEFAULT_RESULT_DURATION);
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
