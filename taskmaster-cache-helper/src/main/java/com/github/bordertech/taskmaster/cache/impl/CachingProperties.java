package com.github.bordertech.taskmaster.cache.impl;

import com.github.bordertech.config.Config;
import java.util.concurrent.TimeUnit;
import javax.cache.expiry.Duration;
import org.apache.commons.configuration.Configuration;

/**
 * This class contains references to all constants and configuration options used by Caching.
 */
public final class CachingProperties {

	private static final String DEFAULT_PREFIX = "bordertech.taskmaster.cache.default.";
	private static final String AMOUNT = "amount";
	private static final String UNIT = "unit";

	/**
	 * Private constructor for static class.
	 */
	private CachingProperties() {
		//No-impl
	}

	/**
	 * @return the config XML location
	 */
	public static String getConfigXmlLocation() {
		return get().getString("bordertech.taskmaster.cache.config", "/tm-cache.xml");
	}

	/**
	 * @return the default duration
	 */
	public static Duration getDefaultDuration() {
		return new Duration(getDefaultDurationUnit(), getDefaultDurationAmount());
	}

	/**
	 * @param cacheName the cache name
	 * @return the default duration
	 */
	public static Duration getCacheDuration(final String cacheName) {
		return getCacheDuration(cacheName, getDefaultDuration());
	}

	/**
	 * @param cacheName the cache name
	 * @param defaultDuration default duration
	 * @return the cache duration
	 */
	public static Duration getCacheDuration(final String cacheName, final Duration defaultDuration) {
		TimeUnit unit = getCacheDurationUnit(cacheName, defaultDuration.getTimeUnit());
		Long amount = getCacheDurationAmount(cacheName, defaultDuration.getDurationAmount());
		return new Duration(unit, amount);
	}

	/**
	 * @return the default duration amount
	 */
	private static Long getDefaultDurationAmount() {
		return get().getLong(DEFAULT_PREFIX + AMOUNT, Long.valueOf("1800"));
	}

	/**
	 * @return the default time unit
	 */
	private static TimeUnit getDefaultDurationUnit() {
		String paramUnit = get().getString(DEFAULT_PREFIX + UNIT);
		return convertParamToTimeUnit(paramUnit, TimeUnit.SECONDS);
	}

	/**
	 * @param cacheName the cache name
	 * @param defaultAmount the default amount
	 * @return the cache duration amount
	 */
	private static Long getCacheDurationAmount(final String cacheName, final Long defaultAmount) {
		return get().getLong(getCachePrefix(cacheName) + AMOUNT, defaultAmount);
	}

	/**
	 * @param cacheName the cache name
	 * @param defaultUnit the default unit
	 * @return the cache time unit
	 */
	private static TimeUnit getCacheDurationUnit(final String cacheName, final TimeUnit defaultUnit) {
		String paramUnit = get().getString(getCachePrefix(cacheName) + UNIT);
		return convertParamToTimeUnit(paramUnit, defaultUnit);
	}

	/**
	 * @param cacheName the cache name
	 * @return the cache parameter prefix
	 */
	private static String getCachePrefix(final String cacheName) {
		return "bordertech.taskmaster.cache." + cacheName + ".duration.";
	}

	/**
	 * Convert parameter to time unit.
	 *
	 * @param paramUnit the parameter value
	 * @param defaultUnit the default unit
	 * @return the time unit or null if o match
	 */
	private static TimeUnit convertParamToTimeUnit(final String paramUnit, final TimeUnit defaultUnit) {

		if (paramUnit == null) {
			return defaultUnit;
		}

		// Get unit
		switch (paramUnit) {
			case "d":
				return TimeUnit.DAYS;
			case "h":
				return TimeUnit.HOURS;
			case "m":
				return TimeUnit.MINUTES;
			case "s":
				return TimeUnit.SECONDS;
			case "mi":
				return TimeUnit.MILLISECONDS;
			case "n":
				return TimeUnit.NANOSECONDS;
			default:
				return defaultUnit;
		}
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
