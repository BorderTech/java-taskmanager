package com.github.bordertech.taskmaster.cache;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;

/**
 * Create Cache helper implementation using JSR107 provider.
 *
 * @author jonathan
 */
public class CacheHelperImpl implements CacheHelper {

	@Override
	public synchronized <K, V> Cache<K, V> getOrCreateCache(final String name, final Class<K> keyClass,
			final Class<V> valueClass, final Duration duration) {
		Cache<K, V> cache = Caching.getCache(name, keyClass, valueClass);
		if (cache == null) {
			final CacheManager mgr = Caching.getCachingProvider().getCacheManager();
			MutableConfiguration<K, V> config = new MutableConfiguration<>();
			config.setTypes(keyClass, valueClass);
			config.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(duration));
			cache = mgr.createCache(name, config);
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

}
