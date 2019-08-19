package com.github.bordertech.taskmaster.ehcache;

import com.github.bordertech.config.Config;
import com.github.bordertech.taskmaster.cache.impl.CachingHelperProviderXmlConfig;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.cache.Cache;
import javax.inject.Singleton;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ehcache.config.CacheRuntimeConfiguration;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.jsr107.Eh107Configuration;

/**
 * CachingHelperProvider that configures ehcache with properties and wraps the cache with JSR107 interface.
 */
@Singleton
public class CachingHelperProviderEhCache extends CachingHelperProviderXmlConfig {

	private static final Log LOGGER = LogFactory.getLog(CachingHelperProviderEhCache.class);
	private static final String CACHE_PREFIX = "Cache [";
	private final Map<String, Triple<String, Class, Class>> caches = new HashMap<>();

	/**
	 * Construct Helper.
	 */
	public CachingHelperProviderEhCache() {
		// Add property change listener
		PropertyChangeListener listener = new RefreshCachePropertyChangeListener();
		Config.addPropertyChangeListener(listener);
	}

	@Override
	public <K, V> Cache<K, V> getOrCreateCache(final String name, final Class<K> keyClass, final Class<V> valueClass) {
		Cache<K, V> cache = super.getOrCreateCache(name, keyClass, valueClass);
		if (!caches.containsKey(name)) {
			configCachePropertyValues(name, cache);
			caches.put(name, new ImmutableTriple(name, keyClass, valueClass));
		}
		return cache;
	}

	/**
	 * Update cache heap properties. DISK resources cannot be updated on the fly.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param name the cache name
	 * @param cache the cache
	 */
	private synchronized <K, V> void configCachePropertyValues(final String name, final Cache<K, V> cache) {

		LOGGER.info("Config cache [" + name + "] properties.");

		Eh107Configuration<K, V> eh107Configuration = cache.getConfiguration(Eh107Configuration.class);
		CacheRuntimeConfiguration<K, V> config = eh107Configuration.unwrap(CacheRuntimeConfiguration.class);

		ResourcePoolsBuilder builder = ResourcePoolsBuilder.newResourcePoolsBuilder();
		boolean changed = false;
		// Heap size (Megabytes)
		Integer value = getIntegerProperty(name, "heap.size");
		if (value != null) {
			LOGGER.info(CACHE_PREFIX + name + "] heap size [" + value + "MB].");
			builder = builder.heap(value, MemoryUnit.MB);
			changed = true;
		}
		// Heap entries (ie limit of entries)
		value = getIntegerProperty(name, "heap.entries");
		if (value != null) {
			LOGGER.info(CACHE_PREFIX + name + "] heap entries [" + value + "].");
			builder = builder.heap(value, EntryUnit.ENTRIES);
			changed = true;
		}
		if (changed) {
			LOGGER.info(CACHE_PREFIX + name + "] config updated.");
			ResourcePools pools = builder.build();
			config.updateResourcePools(pools);
		}

	}

	/**
	 * @return the parameter configuration
	 */
	private Configuration getParams() {
		return Config.getInstance();
	}

	/**
	 * Get an integer cache property.
	 *
	 * @param name the cache name
	 * @param property the cache property
	 * @return the integer property value or null
	 */
	private Integer getIntegerProperty(final String name, final String property) {
		// Cache property then default
		return getParams().getInteger(getKey(name, property), null);
	}

	/**
	 * Derive parameter key for the cache and property.
	 *
	 * @param name the cache name
	 * @param property the cache property
	 * @return the property key
	 */
	private String getKey(final String name, final String property) {
		return "bordertech.taskmaster.cache." + name + "." + property;
	}

	/**
	 * Property change listener that handles updating cache properties.
	 */
	private class RefreshCachePropertyChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			LOGGER.info("Refresh cache properties.");
			for (Triple<String, Class, Class> value : caches.values()) {
				String name = value.getLeft();
				Cache cache = getManager().getCache(name, value.getMiddle(), value.getRight());
				if (cache != null) {
					configCachePropertyValues(name, cache);
				}
			}
		}
	}
}
