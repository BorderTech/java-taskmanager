package com.github.bordertech.taskmaster.cache.impl;

import com.github.bordertech.config.Config;
import com.github.bordertech.taskmaster.cache.CachingHelperProvider;
import java.util.concurrent.TimeUnit;
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

	private static final Duration DEFAULT_DURATION;

	static {
		Long interval = Config.getInstance().getLong("bordertech.taskmaster.caching.default.duration", Long.valueOf("1800"));
		String unitType = Config.getInstance().getString("bordertech.taskmaster.caching.default.unit", "s");

		// Get unit
		TimeUnit unit;
		switch (unitType) {
			case "d":
				unit = TimeUnit.DAYS;
				break;
			case "h":
				unit = TimeUnit.HOURS;
				break;
			case "m":
				unit = TimeUnit.MINUTES;
				break;
			default:
				unit = TimeUnit.SECONDS;
		}
		DEFAULT_DURATION = new Duration(unit, interval);
	}

	@Override
	public synchronized void closeCacheManager() {
		CachingProvider provider = Caching.getCachingProvider();
		if (provider != null && !provider.getCacheManager().isClosed()) {
			provider.getCacheManager().close();
		}
	}

	@Override
	public synchronized <K, V> Cache<K, V> getOrCreateCache(final String name, final Class<K> keyClass, final Class<V> valueClass) {
		return getOrCreateCache(name, keyClass, valueClass, getDefaultDuration());
	}

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

	/**
	 * @return the default duration
	 */
	protected Duration getDefaultDuration() {
		return DEFAULT_DURATION;
	}

}
