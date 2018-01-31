package com.github.bordertech.taskmanager.service;

import com.github.bordertech.didums.Didums;
import com.github.bordertech.taskmanager.TaskFuture;
import com.github.bordertech.taskmanager.TaskManager;
import com.github.bordertech.taskmanager.TaskManagerException;
import java.io.Serializable;
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
 * @author Jonathan Austin
 * @since 1.0.0
 */
public final class ServiceUtil {

	private static final TaskManager TASK_MANAGER = Didums.getService(TaskManager.class);

	private static final String DEFAULT_FUTURE_CACHE_NAME = "taskmanager-future-default";

	/**
	 * Default service result cache name.
	 */
	public static final String DEFAULT_RESULT_CACHE_NAME = "taskmanager-result-holder-default";

	private ServiceUtil() {
	}

	/**
	 * Handle a service call.
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
		try {
			T resp = action.service(criteria);
			return new ResultHolder<>(criteria, resp);
		} catch (Exception e) {
			ServiceException excp = new ServiceException("Error calling service." + e.getMessage(), e);
			return new ResultHolder<>(criteria, excp);
		}
	}

	/**
	 *
	 * Handle a cached service call.
	 *
	 * @param cache the future cache
	 * @param cacheKey the key for the result holder
	 * @param criteria the criteria
	 * @param action the service action
	 * @param callType the call type
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result or null if still processing an async call
	 */
	public static <S, T> ResultHolder<S, T> handleServiceCallType(final Cache<String, ResultHolder> cache, final String cacheKey, final S criteria, final ServiceAction<S, T> action, final CallType callType) {
		if (callType == null) {
			throw new IllegalArgumentException("Call type must be provided.");
		}
		// Refresh the Cache
		if (callType.isRefresh()) {
			cache.remove(cacheKey);
		}
		if (callType.isAsync()) {
			// ASync
			return handleAsyncServiceCall(cache, cacheKey, criteria, action);
		} else {
			// Sync
			return handleCachedServiceCall(cache, cacheKey, criteria, action);
		}
	}

	/**
	 *
	 * Handle a cached service call.
	 *
	 * @param cache the future cache
	 * @param cacheKey the key for the result holder
	 * @param criteria the criteria
	 * @param action the service action
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result
	 */
	public static <S, T> ResultHolder<S, T> handleCachedServiceCall(final Cache<String, ResultHolder> cache, final String cacheKey, final S criteria, final ServiceAction<S, T> action) {

		// Check cache and cache key provided
		if (cache == null) {
			throw new IllegalArgumentException("A cache must be provided.");
		}
		if (cacheKey == null) {
			throw new IllegalArgumentException("A cache key must be provided.");
		}

		// Check cache for result
		ResultHolder cached = cache.get(cacheKey);
		if (cached != null) {
			return cached;
		}
		// Do service call
		ResultHolder<S, T> result = handleServiceCall(criteria, action);
		// Save in the cache
		cache.put(cacheKey, result);

		return result;
	}

	/**
	 * Handle an async service call.
	 *
	 * @param cache the future cache
	 * @param cacheKey the key for the result holder
	 * @param criteria the criteria
	 * @param action the service action
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result or null if still processing
	 */
	public static <S, T> ResultHolder<S, T> handleAsyncServiceCall(final Cache<String, ResultHolder> cache, final String cacheKey, final S criteria, final ServiceAction<S, T> action) {

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

		// Maybe already processing or in the result cache
		ResultHolder cached = cache.get(cacheKey);
		if (cached != null) {
			return cached;
		}

		// Check already processing
		String futureKey = getFutureKey(cache.getName(), cacheKey);
		if (getFutureCache().get(futureKey) != null) {
			cached = checkASyncResult(cache, cacheKey);
			// Null for still processing or has result
			return cached;
		}

		// Setup the bean to hold the service result
		final FutureServiceResult<S, T> result = new FutureServiceResult(cacheKey);
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
			// Cache the future
			getFutureCache().put(futureKey, future);
			return null;
		} catch (Exception e) {
			throw new TaskManagerException("Could not start thread to process task action. " + e.getMessage());
		}
	}

	/**
	 * This is the method to call to check if the future task has completed.
	 * <p>
	 * If the future is done, then it will transition the result from the future cache into the result holder cache.
	 * </p>
	 *
	 * @param cache the future cache
	 * @param cacheKey the key for the result holder
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result or null if still processing
	 */
	public static synchronized <S, T> ResultHolder<S, T> checkASyncResult(final Cache<String, ResultHolder> cache, final String cacheKey) {

		// Check cache and cache key provided
		if (cache == null) {
			throw new IllegalArgumentException("A cache must be provided for async processing. ");
		}
		if (cacheKey == null) {
			throw new IllegalArgumentException("A cache key must be provided for async processing. ");
		}

		String futureKey = getFutureKey(cache.getName(), cacheKey);

		// Get the future
		TaskFuture<FutureServiceResult> future = getFutureCache().get(futureKey);

		// Future has expired or been removed from the Cache
		if (future == null) {
			// Maybe already in the result cache
			ResultHolder cached = cache.get(cacheKey);
			if (cached != null) {
				return cached;
			}
			throw new ServiceException("Future is no longer in the cache");
		}

		// Still processing
		if (!future.isDone()) {
			return null;
		}

		// Future was cancelled
		if (future.isCancelled()) {
			// Remove from cache
			clearFutureCache(futureKey);
			throw new ServiceException("Future was cancelled.");
		}

		// Remove from the future cache
		getFutureCache().remove(futureKey);

		// Done, so Extract the result
		try {
			FutureServiceResult serviceResult = future.get();
			ResultHolder result;
			if (serviceResult.isException()) {
				result = new ResultHolder(serviceResult.getMetaData(), serviceResult.getException());
			} else {
				result = new ResultHolder(serviceResult.getMetaData(), serviceResult.getResult());
			}
			// Cache the result
			cache.put(cacheKey, result);
			return result;
		} catch (InterruptedException | ExecutionException e) {
			throw new ServiceException("Could not get result from the future. " + e.getMessage(), e);
		}
	}

	/**
	 * Use a cache to hold a reference to the future so the user context can be serialized. Future Objects are not
	 * serializable.
	 *
	 * @return the cache instance
	 */
	public static Cache<String, ResultHolder> getDefaultResultHolderCache() {
		return getResultHolderCache(DEFAULT_RESULT_CACHE_NAME);
	}

	/**
	 * Use a cache to hold a reference to the future so the user context can be serialized. Future Objects are not
	 * serializable.
	 *
	 * @param name the cache name
	 * @return the cache instance
	 */
	public static synchronized Cache<String, ResultHolder> getResultHolderCache(final String name) {
		Cache<String, ResultHolder> cache = Caching.getCache(name, String.class, ResultHolder.class
		);
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
		Cache<String, TaskFuture> cache = Caching.getCache(DEFAULT_FUTURE_CACHE_NAME, String.class, TaskFuture.class);
		if (cache == null) {
			final CacheManager mgr = Caching.getCachingProvider().getCacheManager();
			MutableConfiguration<String, TaskFuture> config = new MutableConfiguration<>();
			config.setTypes(String.class, TaskFuture.class);
			config.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 5)));
			cache = mgr.createCache(DEFAULT_FUTURE_CACHE_NAME, config);
		}
		return cache;
	}

	private static void clearFutureCache(final String key) {
		TaskFuture future = getFutureCache().get(key);
		if (future != null) {
			if (!future.isDone() && !future.isCancelled()) {
				future.cancel(true);
			}
			getFutureCache().remove(key);
		}
	}

	private static String getFutureKey(final String cacheName, final String cacheKey) {
		return cacheName + "-" + cacheKey;
	}

	/**
	 * Used to hold the service result with the Future processing.
	 */
	private static class FutureServiceResult<M, T> implements Serializable {

		private final M metaData;
		private T result;
		private Exception exception;

		public FutureServiceResult(final M metaData) {
			this.metaData = metaData;
		}

		public M getMetaData() {
			return metaData;
		}

		/**
		 * @return the polling result
		 */
		public T getResult() {
			return result;
		}

		/**
		 * @param result the polling result
		 */
		public void setResult(final T result) {
			this.result = result;
			this.exception = null;
		}

		public Exception getException() {
			return exception;
		}

		public void setException(final Exception exception) {
			this.exception = exception;
			this.result = null;
		}

		public boolean isException() {
			return exception != null;
		}

		public boolean isResult() {
			return !isException();
		}

	}

}
