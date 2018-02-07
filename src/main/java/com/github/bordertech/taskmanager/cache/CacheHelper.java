package com.github.bordertech.taskmanager.cache;

import javax.cache.Cache;
import javax.cache.configuration.Configuration;
import javax.cache.expiry.Duration;

/**
 * Create Cache helper based on JSR 107.
 * <p>
 * Allows projects to provide a different mechanism for creating their cache requirements.
 * </p>
 *
 * @author jonathan
 */
public interface CacheHelper {

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
	<K, V> Cache<K, V> getOrCreateCache(final String name, final Class<K> keyClass, final Class<V> valueClass, final Duration duration);

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
	<K, V> Cache<K, V> getOrCreateCache(final String name, final Class<K> keyClass, final Class<V> valueClass, final Configuration<K, V> config);

}
