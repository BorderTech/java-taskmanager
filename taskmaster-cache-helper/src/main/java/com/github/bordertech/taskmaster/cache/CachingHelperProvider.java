package com.github.bordertech.taskmaster.cache;

import javax.cache.Cache;
import javax.cache.configuration.Configuration;
import javax.cache.expiry.Duration;

/**
 * Caching helper provider based on JSR 107.
 * <p>
 * Allows projects to provide a different mechanism for creating their cache requirements.
 * </p>
 *
 * @author jonathan
 */
public interface CachingHelperProvider {

	/**
	 * Close and release the cache resources.
	 */
	void closeCacheManager();

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
	<K, V> Cache<K, V> getOrCreateCache(String name, Class<K> keyClass, Class<V> valueClass);

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
	<K, V> Cache<K, V> getOrCreateCache(String name, Class<K> keyClass, Class<V> valueClass, Duration duration);

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
	<K, V> Cache<K, V> getOrCreateCache(String name, Class<K> keyClass, Class<V> valueClass, Configuration<K, V> config);

}
