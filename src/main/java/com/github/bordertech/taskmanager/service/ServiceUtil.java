package com.github.bordertech.taskmanager.service;

import com.github.bordertech.locator.LocatorUtil;
import com.github.bordertech.taskmanager.TaskFuture;
import com.github.bordertech.taskmanager.TaskManager;
import com.github.bordertech.taskmanager.TaskManagerException;
import com.github.bordertech.taskmanager.impl.DefaultTaskFuture;
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

//	/**
//	 * @param cacheKey the key for the result holder
//	 * @return the result holder
//	 * @param <S> the criteria type
//	 * @param <T> the service response
//	 */
//	public static <S, T> ResultHolder<S, T> getResultHolder(final String cacheKey) {
//		return getResultHolder(getDefaultFutureCache(), cacheKey);
//	}
//
//	/**
//	 * Save the result in the cache.
//	 *
//	 * @param cacheKey the key for the result holder
//	 * @param resultHolder the result holder
//	 */
//	public static void setResultHolder(final String cacheKey, final ResultHolder resultHolder) {
//		setResultHolder(getDefaultFutureCache(), cacheKey, resultHolder);
//	}
//
//	/**
//	 * Clear the result cache.
//	 *
//	 * @param cacheKey the key for the result holder
//	 */
//	public static void clearResult(final String cacheKey) {
//		clearResult(getDefaultFutureCache(), cacheKey);
//	}
//
//	/**
//	 * Clear all the results in the cache.
//	 */
//	public static void clearCache() {
//		clearCache(getDefaultFutureCache());
//	}
//
//	/**
//	 * @param cacheKey the key for the result holder
//	 * @param criteria the criteria
//	 * @param action the service action
//	 * @param <S> the criteria type
//	 * @param <T> the service response
//	 * @return the result
//	 */
//	public static <S, T> ResultHolder<S, T> handleCachedServiceCall(final String cacheKey, final S criteria, final ServiceAction<S, T> action) {
//		return handleCachedServiceCall(getDefaultFutureCache(), cacheKey, criteria, action);
//	}
//
//	/**
//	 * @param cacheKey the key for the result holder
//	 * @param criteria the criteria
//	 * @param action the service action
//	 * @param <S> the criteria type
//	 * @param <T> the service response
//	 */
//	public static <S, T> void handleAsyncServiceCall(final String cacheKey, final S criteria, final ServiceAction<S, T> action) {
//		handleAsyncServiceCall(getDefaultFutureCache(), cacheKey, criteria, action);
//	}
//
//	/**
//	 * This is the method to call to check if the future task has completed.
//	 *
//	 * @param cacheKey the key for the result holder
//	 * @param <S> the criteria type
//	 * @param <T> the service response
//	 * @return the result
//	 */
//	public static <S, T> ResultHolder<S, T> checkASyncResult(final String cacheKey) {
//		return checkASyncResult(getDefaultFutureCache(), cacheKey);
//	}
//
//	/**
//	 * Check the service status.
//	 *
//	 * @param cacheKey the key for the result holder
//	 * @return the result
//	 */
//	public static ServiceStatus getServiceStatus(final String cacheKey) {
//		return getServiceStatus(getDefaultFutureCache(), cacheKey);
//	}
	/**
	 * @param cache the future cache
	 * @param cacheKey the key for the result holder
	 * @return the result holder
	 * @param <S> the criteria type
	 * @param <T> the service response
	 */
	public static <S, T> ResultHolder<S, T> getResultHolder(final Cache<String, TaskFuture> cache, final String cacheKey) {
		TaskFuture<ResultHolder> future = cache.get(cacheKey);
		if (future != null && future.isDone()) {
			try {
				return future.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new ServiceException("Could not get result from future. " + e.getMessage(), e);
			}
		}
		return null;
	}

	/**
	 * Save the result in the cache.
	 *
	 * @param cache the future cache
	 * @param cacheKey the key for the result holder
	 * @param resultHolder the result holder
	 */
	public static void setResultHolder(final Cache<String, TaskFuture> cache, final String cacheKey, final ResultHolder resultHolder) {
		// Check cache and cache key provided
		if (cache == null) {
			throw new IllegalArgumentException("A cache must be provided.");
		}
		if (cacheKey == null) {
			throw new IllegalArgumentException("A cache key must be provided.");
		}
		if (resultHolder == null) {
			throw new IllegalArgumentException("A result holder must be provided.");
		}
		cache.put(cacheKey, new DefaultTaskFuture(resultHolder));
	}

	/**
	 * Clear the result in the cache.
	 *
	 * @param cache the future cache
	 * @param cacheKey the key for the result holder
	 */
	public static void clearResult(final Cache<String, TaskFuture> cache, final String cacheKey) {
		TaskFuture future = cache.get(cacheKey);
		if (future != null) {
			if (!future.isDone() && !future.isCancelled()) {
				future.cancel(true);
			}
			cache.remove(cacheKey);
		}
	}

	/**
	 * Clear all the results in the cache.
	 *
	 * @param cache the future cache
	 */
	public static void clearCache(final Cache<String, TaskFuture> cache) {
		cache.removeAll();
	}

	/**
	 *
	 * @param criteria the criteria
	 * @param action the service action
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result
	 */
	public static <S, T> ResultHolder<S, T> handleServiceCall(final S criteria, final ServiceAction<S, T> action) {
		// Check action provided
		if (action == null) {
			throw new IllegalArgumentException("No service action has been provided. ");
		}

		// Do service call
		ResultHolder<S, T> result = new ResultHolder(criteria);
		try {
			T resp = action.service(criteria);
			result.setResult(resp);
		} catch (Exception e) {
			ServiceException excp = new ServiceException("Error calling service." + e.getMessage(), e);
			result.setException(excp);
		}
		return result;
	}

	/**
	 * @param cache the future cache
	 * @param cacheKey the key for the result holder
	 * @param criteria the criteria
	 * @param action the service action
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result
	 */
	public static <S, T> ResultHolder<S, T> handleCachedServiceCall(final Cache<String, TaskFuture> cache, final String cacheKey, final S criteria, final ServiceAction<S, T> action) {

		// Check cache and cache key provided
		if (cache == null) {
			throw new IllegalArgumentException("A cache must be provided.");
		}
		if (cacheKey == null) {
			throw new IllegalArgumentException("A cache key must be provided.");
		}

		// Check cache for result
		ResultHolder cached = getResultHolder(cache, cacheKey);
		if (cached != null) {
			return cached;
		}

		// Do service call
		ResultHolder<S, T> result = handleServiceCall(criteria, action);

		// Save in the cache as a Future
		setResultHolder(cache, cacheKey, result);

		return result;
	}

	/**
	 * @param cache the future cache
	 * @param cacheKey the key for the result holder
	 * @param criteria the criteria
	 * @param action the service action
	 * @param <S> the criteria type
	 * @param <T> the service response
	 */
	public static <S, T> void handleAsyncServiceCall(final Cache<String, TaskFuture> cache, final String cacheKey, final S criteria, final ServiceAction<S, T> action) {

		// Check cache and cache key provided
		if (cache == null) {
			throw new IllegalArgumentException("A cache must be provided for async processing.");
		}
		if (cacheKey == null) {
			throw new IllegalArgumentException("A cache key must be provided for async processing. ");
		}

		// Check action provided
		if (action == null) {
			throw new IllegalArgumentException("No service action has been provided. ");
		}

		// Check if already processing
		if (cache.containsKey(cacheKey)) {
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
			cache.put(cacheKey, future);
		} catch (Exception e) {
			throw new TaskManagerException("Could not start thread to process task action. " + e.getMessage());
		}
	}

	/**
	 * This is the method to call to check if the future task has completed.
	 *
	 * @param cache the future cache
	 * @param cacheKey the key for the result holder
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result
	 */
	public static synchronized <S, T> ResultHolder<S, T> checkASyncResult(final Cache<String, TaskFuture> cache, final String cacheKey) {

		// Check cache and cache key provided
		if (cache == null) {
			throw new IllegalArgumentException("A cache must be provided for async processing. ");
		}
		if (cacheKey == null) {
			throw new IllegalArgumentException("A cache key must be provided for async processing. ");
		}

		TaskFuture<ResultHolder> future = cache.get(cacheKey);

		// Future has expired or been removed from the Cache
		if (future == null) {
			// Put an exception in the cache
			ResultHolder result = new ResultHolder(null, new ServiceException("Future is no longer in the cache"));
			setResultHolder(cache, cacheKey, result);
			return result;
		}
		if (!future.isDone()) {
			return null;
		}
		if (future.isCancelled()) {
			// Put an exception in the cache
			ResultHolder result = new ResultHolder(null, new ServiceException("Future was cancelled."));
			setResultHolder(cache, cacheKey, result);
			return result;
		}
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new TaskManagerException("Error processing the service. " + e.getMessage(), e);
		}
	}

	/**
	 * Check the service status.
	 *
	 * @param cache the future cache
	 * @param cacheKey the key for the result holder
	 * @return the result
	 */
	public static ServiceStatus getServiceStatus(final Cache<String, TaskFuture> cache, final String cacheKey) {

		// Check cache and cache key provided
		if (cache == null) {
			throw new IllegalArgumentException("A cache must be provided for async processing. ");
		}
		if (cacheKey == null) {
			throw new IllegalArgumentException("A cache key must be provided for async processing. ");
		}

		// Check if in cache
		TaskFuture<ResultHolder> future = cache.get(cacheKey);
		if (future == null) {
			return ServiceStatus.NOT_STARTED;
		}

		// Processing
		if (!future.isDone()) {
			return ServiceStatus.PROCESSING;
		}

		// Check for result
		try {
			ResultHolder holder = future.get();
			return holder.isException() ? ServiceStatus.ERROR : ServiceStatus.COMPLETE;
		} catch (InterruptedException | ExecutionException e) {
			throw new ServiceException("Could not get result from future. " + e.getMessage(), e);
		}
	}

	/**
	 * Use a cache to hold a reference to the future so the user context can be serialized. Future Objects are not
	 * serializable.
	 *
	 * @return the cache instance
	 */
	public static Cache<String, TaskFuture> getDefaultFutureCache() {
		return getFutureCache("taskmanager-future-default");
	}

	/**
	 * Use a cache to hold a reference to the future so the user context can be serialized. Future Objects are not
	 * serializable.
	 *
	 * @param name the cache name
	 * @return the cache instance
	 */
	public static synchronized Cache<String, TaskFuture> getFutureCache(final String name) {
		Cache<String, TaskFuture> cache = Caching.getCache(name, String.class, TaskFuture.class);
		if (cache == null) {
			final CacheManager mgr = Caching.getCachingProvider().getCacheManager();
			MutableConfiguration<String, TaskFuture> config = new MutableConfiguration<>();
			config.setTypes(String.class, TaskFuture.class);
			config.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 30)));
			cache = mgr.createCache(name, config);
		}
		return cache;
	}

}
