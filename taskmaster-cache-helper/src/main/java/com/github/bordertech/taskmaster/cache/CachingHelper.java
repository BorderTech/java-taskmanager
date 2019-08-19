package com.github.bordertech.taskmaster.cache;

import com.github.bordertech.didums.Didums;
import com.github.bordertech.taskmaster.cache.impl.CachingHelperProviderDefault;
import javax.cache.Cache;
import javax.cache.configuration.Configuration;
import javax.cache.expiry.Duration;

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

	/**
	 * Close and release the cache resources.
	 */
	public static void closeCacheManager() {
		PROVIDER.closeCacheManager();
	}

	/**
	 * Create a cache with the default configuration.
	 *
	 * @param name the cache name
	 * @param keyClass the key class type
	 * @param valueClass the value class type
	 * @param <K> the cache entry key type
	 * @param <V> the cache entry value value
	 * @return the cache instance
	 */
	public static <K, V> Cache<K, V> getOrCreateCache(final String name, final Class<K> keyClass, final Class<V> valueClass) {
		return PROVIDER.getOrCreateCache(name, keyClass, valueClass);
	}

	/**
	 * Create a cache with the specified duration.
	 *
	 * @param name the cache name
	 * @param keyClass the key class type
	 * @param valueClass the value class type
	 * @param duration the cache entry duration
	 * @param <K> the cache entry key type
	 * @param <V> the cache entry value value
	 * @return the cache instance
	 */
	public static <K, V> Cache<K, V> getOrCreateCache(final String name, final Class<K> keyClass, final Class<V> valueClass, final Duration duration) {
		return PROVIDER.getOrCreateCache(name, keyClass, valueClass, duration);
	}

	/**
	 * Create a cache with the specified configuration.
	 *
	 * @param <K> the cache entry key type
	 * @param <V> the cache entry value value
	 * @param name the cache name
	 * @param keyClass the key class type
	 * @param valueClass the value class type
	 * @param config the cache configuration
	 * @return the cache instance
	 */
	public static <K, V> Cache<K, V> getOrCreateCache(final String name, final Class<K> keyClass, final Class<V> valueClass, final Configuration<K, V> config) {
		return PROVIDER.getOrCreateCache(name, keyClass, valueClass, config);
	}

}
