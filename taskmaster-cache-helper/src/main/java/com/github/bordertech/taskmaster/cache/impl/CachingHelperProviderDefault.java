package com.github.bordertech.taskmaster.cache.impl;

import com.github.bordertech.taskmaster.cache.CachingHelperProvider;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;

/**
 * Default CachingHelperProvider implementation using JSR107 provider.
 *
 * @author jonathan
 */
public class CachingHelperProviderDefault implements CachingHelperProvider {

	@Override
	public synchronized void closeCacheManager() {
		CachingProvider provider = Caching.getCachingProvider();
		if (provider != null && !provider.getCacheManager().isClosed()) {
			provider.getCacheManager().close();
		}
	}

	@Override
	public synchronized <K, V> Cache<K, V> getOrCreateCache(final String name, final Class<K> keyClass, final Class<V> valueClass) {
		Cache<K, V> cache = Caching.getCache(name, keyClass, valueClass);
		if (cache == null) {
			// Get cache duration
			Duration duration = CachingProperties.getCacheDuration(name);
			cache = createCache(name, keyClass, valueClass, duration);
		}
		return cache;
	}

	@Override
	public synchronized <K, V> Cache<K, V> getOrCreateCache(final String name, final Class<K> keyClass,
			final Class<V> valueClass, final Duration duration) {
		Cache<K, V> cache = Caching.getCache(name, keyClass, valueClass);
		if (cache == null) {
			// Check for duration override
			Duration durationOverride = CachingProperties.getCacheDuration(name, duration);
			cache = createCache(name, keyClass, valueClass, durationOverride);
		}
		return cache;
	}

	@Override
	public synchronized <K, V> Cache<K, V> getOrCreateCache(final String name, final Class<K> keyClass,
			final Class<V> valueClass, final Configuration<K, V> config) {
		Cache<K, V> cache = Caching.getCache(name, keyClass, valueClass);
		if (cache == null) {
			final CacheManager mgr = Caching.getCachingProvider().getCacheManager();
			cache = mgr.createCache(name, config);
		}
		return cache;
	}

	/**
	 * Create a cache.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param name the cache name
	 * @param keyClass the key class type
	 * @param valueClass the value class type
	 * @param duration the cache duration amount
	 * @return the cache
	 */
	protected synchronized <K, V> Cache<K, V> createCache(final String name, final Class<K> keyClass,
			final Class<V> valueClass, final Duration duration) {
		final CacheManager mgr = Caching.getCachingProvider().getCacheManager();
		MutableConfiguration<K, V> config = new MutableConfiguration<>();
		config.setTypes(keyClass, valueClass);
		config.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(duration));
		return mgr.createCache(name, config);
	}

}
