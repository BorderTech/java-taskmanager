package com.github.bordertech.taskmanager.service;

import com.github.bordertech.didums.Didums;
import com.github.bordertech.taskmanager.TaskFuture;
import com.github.bordertech.taskmanager.TaskManager;
import com.github.bordertech.taskmanager.TaskManagerException;
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

	private final static TaskManager TASK_MANAGER = Didums.getService(TaskManager.class);

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
	 */
	public static <S, T> void handleAsyncServiceCall(final Cache<String, ResultHolder> cache, final String cacheKey, final S criteria, final ServiceAction<S, T> action) {

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

		// Future Key
		String futureKey = getFutureKey(cache.getName(), cacheKey);

		// Check if already processing
		if (getFutureCache().containsKey(futureKey)) {
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
			// Cache the future
			getFutureCache().put(futureKey, future);
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
	 * @throws AsyncException async task is missing or cancelled
	 */
	public static synchronized <S, T> ResultHolder<S, T> checkASyncResult(final Cache<String, ResultHolder> cache, final String cacheKey) throws AsyncException {

		// Check cache and cache key provided
		if (cache == null) {
			throw new IllegalArgumentException("A cache must be provided for async processing. ");
		}
		if (cacheKey == null) {
			throw new IllegalArgumentException("A cache key must be provided for async processing. ");
		}

		String futureKey = getFutureKey(cache.getName(), cacheKey);

		// Get the future
		TaskFuture<ResultHolder> future = getFutureCache().get(futureKey);

		// Future has expired or been removed from the Cache
		if (future == null) {
			// Maybe already in the result cache
			ResultHolder cached = cache.get(cacheKey);
			if (cached != null) {
				return cached;
			}
			throw new AsyncException("Future is no longer in the cache");
		}

		// Still processing
		if (!future.isDone()) {
			return null;
		}

		// Been cancelled
		if (future.isCancelled()) {
			// Remove from cache
			clearFutureCache(futureKey);
			throw new AsyncException("Future was cancelled.");
		}

		// Remove from the future cache
		getFutureCache().remove(futureKey);

		// Done, so Extract the result
		ResultHolder result;
		try {
			result = future.get();
			// Cache the result
			cache.put(cacheKey, result);
			return result;
		} catch (Exception e) {
			throw new AsyncException("Could not get result from the future. " + e.getMessage(), e);
		}
	}

	/**
	 * Use a cache to hold a reference to the future so the user context can be serialized. Future Objects are not
	 * serializable.
	 *
	 * @return the cache instance
	 */
	public static Cache<String, ResultHolder> getDefaultResultHolderCache() {
		return getResultHolderCache("taskmanager-result-holder-default");
	}

	/**
	 * Use a cache to hold a reference to the future so the user context can be serialized. Future Objects are not
	 * serializable.
	 *
	 * @param name the cache name
	 * @return the cache instance
	 */
	public static synchronized Cache<String, ResultHolder> getResultHolderCache(final String name) {
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
		String name = "taskmanager-future-default";
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

}
