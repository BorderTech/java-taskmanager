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
	 * Default result holder cache name.
	 */
	public static final String RESULT_HOLDER_CACHE_NAME = "taskmaster-resultholder";

	/**
	 * In progress cache name.
	 */
	public static final String INPROGRESS_CACHE_NAME = "taskmaster-inprogress";

	private static final Duration RESULT_HOLDER_CACHE_DURATION = new Duration(TimeUnit.SECONDS, Long.valueOf("1800"));
	private static final Duration INPROGRESS_CACHE_DURATION = new Duration(TimeUnit.SECONDS, Long.valueOf("300"));

	/**
	 * Private constructor for static class.
	 */
	private ServiceHelperProperties() {
		//No-impl
	}

	/**
	 * @return the default result holder cache duration
	 */
	public static Duration getResultHolderCacheDuration() {
		// Check for overrides
		return CachingProperties.getCacheDuration(ServiceHelperProperties.RESULT_HOLDER_CACHE_NAME, RESULT_HOLDER_CACHE_DURATION);
	}

	/**
	 * @return the in progress cache duration
	 */
	public static Duration getInProgressCacheDuration() {
		// Check for overrides
		return CachingProperties.getCacheDuration(ServiceHelperProperties.INPROGRESS_CACHE_NAME, INPROGRESS_CACHE_DURATION);
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
