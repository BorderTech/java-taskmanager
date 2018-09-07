package com.github.bordertech.taskmaster.cache.impl;

import com.github.bordertech.config.Config;
import com.github.bordertech.taskmaster.cache.CachingHelperProvider;
import java.net.URI;
import java.net.URISyntaxException;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import javax.inject.Singleton;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CachingHelperProvider that relies on the cache config in a xml file.
 */
@Singleton
public class CachingHelperProviderXmlConfig implements CachingHelperProvider {

	private static final Log LOGGER = LogFactory.getLog(CachingHelperProviderXmlConfig.class);

	private static final CacheManager MANAGER;

	static {
		// Load cache configfile location (default tm-cache.xml)
		String config = Config.getInstance().getString("bordertech.taskmaster.cache.config", "/tm-cache.xml");
		LOGGER.info("Loading cache config [" + config + "].");
		URI uri;
		try {
			uri = CachingHelperProviderXmlConfig.class.getResource(config).toURI();
		} catch (URISyntaxException e) {
			throw new IllegalStateException("Could not load cache config [" + config + "]." + e.getMessage(), e);
		}
		CachingProvider cachingProvider = Caching.getCachingProvider();
		MANAGER = cachingProvider.getCacheManager(uri, CachingHelperProviderXmlConfig.class.getClassLoader());
	}

	@Override
	public synchronized void closeCacheManager() {
		CachingProvider provider = Caching.getCachingProvider();
		if (provider != null && !provider.getCacheManager().isClosed()) {
			provider.getCacheManager().close();
		}
	}

	@Override
	public synchronized <K, V> Cache<K, V> getOrCreateCache(final String name, final Class<K> keyClass,
			final Class<V> valueClass, final Duration duration) {
		// Ignore duration
		return handleGetCache(name, keyClass, valueClass);
	}

	@Override
	public synchronized <K, V> Cache<K, V> getOrCreateCache(final String name, final Class<K> keyClass,
			final Class<V> valueClass, final Configuration<K, V> config) {
		// Ignore config
		return handleGetCache(name, keyClass, valueClass);
	}

	/**
	 * Get the pre-configured cache.
	 *
	 * @param name the cache name
	 * @param keyClass the cache key class
	 * @param valueClass the cache entry class
	 * @return the cache instance
	 * @param <K> the cache key type
	 * @param <V> the cache entry type
	 */
	protected synchronized <K, V> Cache<K, V> handleGetCache(final String name, final Class<K> keyClass,
			final Class<V> valueClass) {
		return MANAGER.getCache(name, keyClass, valueClass);
	}

	/**
	 * @return the XML Configured cache manager
	 */
	protected final CacheManager getManager() {
		return MANAGER;
	}
}
