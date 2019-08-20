package com.github.bordertech.taskmaster.impl;

import com.github.bordertech.config.Config;
import com.github.bordertech.taskmaster.cache.impl.CachingProperties;
import java.util.concurrent.TimeUnit;
import javax.cache.expiry.Duration;
import org.apache.commons.configuration.Configuration;

/**
 * This class contains references to all constants and configuration options used by TaskMaster.
 */
public final class TaskMasterProperties {

	private static final String TP_PARAM_PREFIX = "bordertech.taskmaster.pool.";
	private static final int DEFAULT_MAX_THREADS = 20;
	private static final int DEFAULT_QUEUE_LENGTH = 0;

	/**
	 * Private constructor for static class.
	 */
	private TaskMasterProperties() {
		//No-impl
	}

	/**
	 * @return the future task cache name
	 */
	public static String getFutureTaskCacheName() {
		return "bordertech-tm-future-task";
	}

	/**
	 * @return the future task cache duration
	 */
	public static Duration getFutureTaskCacheDuration() {
		Duration defaultDuration = new Duration(TimeUnit.SECONDS, Long.valueOf("300"));
		return CachingProperties.getCacheDuration(getFutureTaskCacheName(), defaultDuration);
	}

	/**
	 * @return the default thread pool name.
	 */
	public static String getDefaultThreadPoolName() {
		return get().getString(TP_PARAM_PREFIX + "default", "default");
	}

	/**
	 * @return the thread pools.
	 */
	public static String[] getThreadPools() {
		return get().getStringArray(TP_PARAM_PREFIX + "names");
	}

	/**
	 * @param pool the thread pool name
	 * @return the thread pool type
	 */
	public static String getThreadPoolType(final String pool) {
		return get().getString(TP_PARAM_PREFIX + pool + ".type", "cached");
	}

	/**
	 * @param pool the thread pool name
	 * @return the max fixed threads
	 */
	public static int getPoolMaxThreads(final String pool) {
		// Number of fixed threads
		int max = get().getInt(TP_PARAM_PREFIX + pool + ".max", DEFAULT_MAX_THREADS);
		if (max < 1) {
			max = DEFAULT_MAX_THREADS;
		}
		return max;
	}

	/**
	 * @param pool the thread pool name
	 * @return the pending queue length
	 */
	public static int getPoolPendingQueueLength(final String pool) {
		// Length of pending queue
		return get().getInt(TP_PARAM_PREFIX + pool + ".queue", DEFAULT_QUEUE_LENGTH);
	}

	/**
	 * @return the logical thread wait interval in milli seconds
	 */
	public static int getLogicalWaitInterval() {
		return get().getInt("bordertech.taskmaster.logicalthreadpool.wait.interval", 300);
	}

	/**
	 * @return the logical thread max wait intervals
	 */
	public static int getLogicalMaxWaitIntervals() {
		return get().getInt("bordertech.taskmaster.logicalthreadpool.wait.max.intervals", 200);
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
