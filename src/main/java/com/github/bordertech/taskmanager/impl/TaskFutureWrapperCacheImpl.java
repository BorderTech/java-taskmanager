package com.github.bordertech.taskmanager.impl;

import com.github.bordertech.config.Config;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.inject.Singleton;
import com.github.bordertech.taskmanager.TaskFutureWrapperCache;

/**
 * Uses a cache (JSR107) to hold the future allowing the TaskFuture to reference the serializable cache key.
 * <p>
 * Requires a JSR107 cache provider at runtime.
 * </p>
 * <p>
 * The duration of this cache must exceed the longest allowed service call (ie service timeout).
 * </p>
 *
 * @param <T> the future get type
 * @author Jonathan Austin
 * @since 1.0.0
 */
@Singleton
public class TaskFutureWrapperCacheImpl<T> implements TaskFutureWrapperCache<T> {

	private static final Long DEFAULT_DURATION_SECONDS = Config.getInstance().getLong("bordertech.taskmanager.taskfuture.internal.cache.duration", Long.valueOf("300"));

	private static final String DEFAULT_CACHE_NAME = "bordertech-tm-future-task";

	@Override
	public Future<T> getFuture(final String key) {
		return getCache().get(key);
	}

	@Override
	public void removeFuture(final String key) {
		getCache().remove(key);
	}

	@Override
	public void putFuture(final String key, final Future<T> future) {
		getCache().put(key, future);
	}

	/**
	 * Use a cache to hold a reference to the future so the user context can be serialized. Future Objects are not
	 * serializable.
	 *
	 * @return the cache instance
	 */
	protected synchronized Cache<String, Future> getCache() {
		Cache<String, Future> cache = Caching.getCache(DEFAULT_CACHE_NAME, String.class, Future.class);
		if (cache == null) {
			final CacheManager mgr = Caching.getCachingProvider().getCacheManager();
			MutableConfiguration<String, Future> config = new MutableConfiguration<>();
			config.setTypes(String.class, Future.class);
			config.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, DEFAULT_DURATION_SECONDS)));
			// No need to serialize the result (Future is not serializable)
			config.setStoreByValue(false);
			cache = mgr.createCache(DEFAULT_CACHE_NAME, config);
		}
		return cache;
	}
}
