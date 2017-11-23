package com.github.bordertech.taskmanager.service;

import com.github.bordertech.locator.LocatorUtil;
import com.github.bordertech.taskmanager.TaskFuture;
import com.github.bordertech.taskmanager.TaskManager;
import com.github.bordertech.taskmanager.TaskManagerException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;

/**
 * Helper utility for sync and async service calls.
 *
 * @author jonathan
 */
public class ServiceUtil {

	private final static TaskManager TASK_MANAGER = LocatorUtil.getService(TaskManager.class);

	private ServiceUtil() {
	}

	/**
	 * @param cacheKey the key for the result holder
	 * @return the result holder
	 */
	public static ResultHolder getResultHolder(final String cacheKey) {
		return getResultCache().get(cacheKey);
	}

	/**
	 * @param cacheKey the key for the result holder
	 * @param result the result holder
	 */
	public static void setResultHolder(final String cacheKey, final ResultHolder result) {
		getResultCache().put(cacheKey, result);
	}

	/**
	 * Clear the result cache.
	 *
	 * @param cacheKey the key for the result holder
	 */
	public static void clearResult(final String cacheKey) {
		getResultCache().remove(cacheKey);
		clearFuture(cacheKey);
	}

	public static <S, T> ResultHolder<S, T> handleServiceCall(final S criteria, final ServiceAction<S, T> action) {
		return handleServiceCall(null, criteria, action);
	}

	public static <S, T> ResultHolder<S, T> handleServiceCall(final String cacheKey, final S criteria, final ServiceAction<S, T> action) {

		// Check action provided
		if (action == null) {
			throw new IllegalStateException("No service action has been provided. ");
		}

		// Check Cache
		if (cacheKey != null) {
			ResultHolder<S, T> cached = getResultHolder(cacheKey);
			if (cached != null) {
				return cached;
			}
		}

		ResultHolder<S, T> result = new ResultHolder(criteria);
		try {
			T resp = action.service(criteria);
			result.setResult(resp);
		} catch (Exception e) {
			ServiceException excp = new ServiceException("Error calling service." + e.getMessage(), e);
			result.setException(excp);
		}

		// Save in result cache
		if (cacheKey != null) {
			setResultHolder(cacheKey, result);
		}

		return result;
	}

	public static <S, T> void handleAsyncServiceCall(final String cacheKey, final S criteria, final ServiceAction<S, T> action) {

		// Check cache key provided
		if (cacheKey == null) {
			throw new IllegalStateException("A cache key must be provided for async processing. ");
		}

		// Check action provided
		if (action == null) {
			throw new IllegalStateException("No service action has been provided. ");
		}

		// Check if already processing
		if (getFuture(cacheKey) != null) {
			return;
		}

		final ResultHolder<S, T> result = new ResultHolder(cacheKey);
		Runnable task = new Runnable() {
			@Override
			public void run() {
				try {
					T resp = action.service(criteria);
					result.setResult(resp);
				} catch (Exception e) {
					ServiceException excp = new ServiceException("Error calling service." + e.getMessage(), e);
					result.setException(excp);
				}
			}
		};
		try {
			TaskFuture future = TASK_MANAGER.submit(task, result);
			// Save the future
			setFuture(cacheKey, future);
		} catch (Exception e) {
			throw new TaskManagerException("Could not start thread to process task action. " + e.getMessage());
		}
	}

	/**
	 * This is the method to call to check if the future task has completed and move it into the result cache.
	 *
	 * @param cacheKey the cache key of the action
	 * @return true if processed and available in the result cache
	 */
	public static synchronized ResultHolder checkASyncResult(final String cacheKey) {

		// Check cache key provided
		if (cacheKey == null) {
			throw new IllegalStateException("A cache key must be provided for async processing. ");
		}

		TaskFuture<ResultHolder> future = getFuture(cacheKey);
		if (future == null) {
			return null;
		}
		if (!future.isDone()) {
			return null;
		}
		if (future.isCancelled()) {
			clearFuture(cacheKey);
			return null;
		}
		try {
			ResultHolder holder = future.get();
			// Put result in the result cache
			setResultHolder(cacheKey, holder);
			return holder;
		} catch (InterruptedException | ExecutionException e) {
			throw new TaskManagerException("Error processing the service. " + e.getMessage(), e);
		} finally {
			clearFuture(cacheKey);
		}
	}

	public static ServiceStatus getServiceStatus(final String cacheKey) {

		// Check cache key provided
		if (cacheKey == null) {
			throw new IllegalStateException("A cache key must be provided for async processing. ");
		}

		// Check if processing
		TaskFuture<ResultHolder> future = getFuture(cacheKey);
		if (future != null) {
			return ServiceStatus.PROCESSING;
		}

		// Check for result
		ResultHolder holder = getResultHolder(cacheKey);
		if (holder != null) {
			return holder.isException() ? ServiceStatus.ERROR : ServiceStatus.COMPLETE;
		}

		// Not available
		return ServiceStatus.NOT_STARTED;

	}

	/**
	 * @param cacheKey the key for the future
	 * @return the future holding the service action
	 */
	private static TaskFuture<ResultHolder> getFuture(final String cacheKey) {
		return getFutureCache().get(cacheKey);
	}

	/**
	 * @param cacheKey the key for the future
	 * @param future the future holding the result
	 */
	private static void setFuture(final String cacheKey, final TaskFuture<ResultHolder> future) {
		getFutureCache().put(cacheKey, future);
	}

	/**
	 * Cancel and clear the future if there is one already running.
	 *
	 * @param cacheKey the key for the future
	 */
	private static void clearFuture(final String cacheKey) {
		TaskFuture future = getFuture(cacheKey);
		if (future != null) {
			if (!future.isDone() && !future.isCancelled()) {
				future.cancel(true);
			}
			getFutureCache().remove(cacheKey);
		}
	}

	/**
	 * Use a cache to hold a reference to the future so the user context can be serialized. Future Objects are not
	 * serializable.
	 *
	 * @return the cache instance
	 */
	private static synchronized Cache<String, ResultHolder> getResultCache() {
		String name = "wc-service-result";
		Cache<String, ResultHolder> cache = Caching.getCache(name, String.class, ResultHolder.class);
		if (cache == null) {
			final CacheManager mgr = Caching.getCachingProvider().getCacheManager();
			MutableConfiguration<String, ResultHolder> config = new MutableConfiguration<>();
			config.setTypes(String.class, ResultHolder.class);
			config.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 30)));
			cache = mgr.createCache(name, config);
		}
		return cache;
	}

	/**
	 * Use a cache to hold a reference to the future so the user context can be serialized. Future Objects are not
	 * serializable.
	 *
	 * @return the cache instance
	 */
	private static synchronized Cache<String, TaskFuture> getFutureCache() {
		String name = "wc-service-future";
		Cache<String, TaskFuture> cache = Caching.getCache(name, String.class, TaskFuture.class);
		if (cache == null) {
			final CacheManager mgr = Caching.getCachingProvider().getCacheManager();
			MutableConfiguration<String, TaskFuture> config = new MutableConfiguration<>();
			config.setTypes(String.class, TaskFuture.class);
			config.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 5)));
			cache = mgr.createCache(name, config);
		}
		return cache;
	}

}
