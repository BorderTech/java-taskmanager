package com.github.bordertech.taskmaster.service.impl;

import com.github.bordertech.config.Config;
import com.github.bordertech.taskmaster.cache.impl.CachingProperties;
import java.util.concurrent.TimeUnit;
import javax.cache.expiry.Duration;
import org.apache.commons.configuration.Configuration;

/**
 * This class contains references to all constants and configuration options used by TaskMaster.
 */
public final class ServiceHelperProperties {

	/**
	 * Private constructor for static class.
	 */
	private ServiceHelperProperties() {
		//No-impl
	}

	/**
	 * @return the default result holder cache name
	 */
	public static String getDefaultResultHolderCacheName() {
		return "taskmaster-resultholder";
	}

	/**
	 * @return the default result holder cache duration
	 */
	public static Duration getDefaultResultHolderCacheDuration() {
		Duration defaultDuration = new Duration(TimeUnit.SECONDS, Long.valueOf("1800"));
		return CachingProperties.getCacheDuration(getDefaultResultHolderCacheName(), defaultDuration);
	}

	/**
	 * @param name the result holder cache name
	 * @return the result holder cache duration
	 */
	public static Duration getResultHolderCacheDuration(final String name) {
		Duration defaultDuration = getDefaultResultHolderCacheDuration();
		return CachingProperties.getCacheDuration(name, defaultDuration);
	}

	/**
	 * @return the in progress cache name
	 */
	public static String getInProgressCacheName() {
		return "taskmaster-inprogress";
	}

	/**
	 * @return the in progress cache duration
	 */
	public static Duration getInProgressCacheDuration() {
		Duration defaultDuration = new Duration(TimeUnit.SECONDS, Long.valueOf("300"));
		return CachingProperties.getCacheDuration(getInProgressCacheName(), defaultDuration);
	}

	/**
	 * @return true if in progress async caching service calls are tracked
	 */
	public static boolean isInProgressEnabled() {
		return get().getBoolean("bordertech.taskmaster.service.inprogress.enabled", false);
	}

	/**
	 * Shorthand convenience method to get the Configuration instance.
	 *
	 * @return the Configuration instance.
	 */
	private static Configuration get() {
		return Config.getInstance();
	}

}
