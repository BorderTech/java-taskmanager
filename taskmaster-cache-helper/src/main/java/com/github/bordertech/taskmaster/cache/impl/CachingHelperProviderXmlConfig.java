package com.github.bordertech.taskmaster.cache.impl;

import com.github.bordertech.taskmaster.cache.CachingHelperProvider;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
		String config = CachingProperties.getConfigXmlLocation();
		LOGGER.info("Loading cache config [" + config + "].");
		URI uri;
		try {
			URL url = CachingHelperProviderXmlConfig.class.getResource(config);
			if (url == null) {
				throw new IllegalStateException("Could not find cache config [" + config + "].");
			}
			uri = url.toURI();
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
	public synchronized <K, V> Cache<K, V> getOrCreateCache(final String name, final Class<K> keyClass, final Class<V> valueClass) {
		// Return the pre configured cache
		return MANAGER.getCache(name, keyClass, valueClass);
	}

	@Override
	public synchronized <K, V> Cache<K, V> getOrCreateCache(final String name, final Class<K> keyClass,
			final Class<V> valueClass, final Duration duration) {
		// Ignore duration
		return getOrCreateCache(name, keyClass, valueClass);
	}

	@Override
	public synchronized <K, V> Cache<K, V> getOrCreateCache(final String name, final Class<K> keyClass,
			final Class<V> valueClass, final Configuration<K, V> config) {
		// Ignore config
		return getOrCreateCache(name, keyClass, valueClass);
	}

	/**
	 * @return the XML Configured cache manager
	 */
	protected final CacheManager getManager() {
		return MANAGER;
	}
}
