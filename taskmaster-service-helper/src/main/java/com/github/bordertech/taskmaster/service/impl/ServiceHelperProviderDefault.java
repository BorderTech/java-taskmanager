package com.github.bordertech.taskmaster.service.impl;

import com.github.bordertech.config.Config;
import com.github.bordertech.taskmaster.TaskFuture;
import com.github.bordertech.taskmaster.TaskMaster;
import com.github.bordertech.taskmaster.cache.CachingHelper;
import com.github.bordertech.taskmaster.exception.RejectedTaskException;
import com.github.bordertech.taskmaster.impl.TaskFutureResult;
import com.github.bordertech.taskmaster.service.ResultHolder;
import com.github.bordertech.taskmaster.service.ServiceAction;
import com.github.bordertech.taskmaster.service.ServiceHelperProvider;
import com.github.bordertech.taskmaster.service.exception.RejectedServiceException;
import com.github.bordertech.taskmaster.service.exception.ServiceException;
import com.github.bordertech.taskmaster.service.util.ExceptionUtil;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import javax.cache.expiry.Duration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default Service Helper provider implementation.
 */
public class ServiceHelperProviderDefault implements ServiceHelperProvider {

	private static final Log LOGGER = LogFactory.getLog(ServiceHelperProviderDefault.class);

	private static final boolean IN_PROGRESS_ENABLED = Config.getInstance().getBoolean("bordertech.taskmaster.service.inprogress.enabled", false);
	private static final Cache<String, Boolean> IN_PROGRESS_CACHE;

	static {
		if (IN_PROGRESS_ENABLED) {
			// Setup the cache for tracking in progress ASync Cached service calls
			Long interval = Config.getInstance().getLong("bordertech.taskmaster.service.inprogress.cache.duration", Long.valueOf("300"));
			Duration duration = new Duration(TimeUnit.SECONDS, interval);
			IN_PROGRESS_CACHE = CachingHelper.getOrCreateCache("taskmaster-inprogress-default", String.class, Boolean.class, duration);
		} else {
			IN_PROGRESS_CACHE = null;
		}
	}

	@Override
	public <S extends Serializable, T extends Serializable> TaskFuture<ResultHolder<S, T>> submitAsync(
			final S criteria, final ServiceAction<S, T> action, final String pool)
			throws ServiceException, RejectedServiceException {

		// Check action provided
		if (action == null) {
			throw new IllegalArgumentException("No service action has been provided. ");
		}

		// Setup the bean to hold the service result
		final ResultHolderMutable<S, T> result = new ResultHolderMutable(criteria);

		// Setup task to run service action
		Runnable task = new ServiceActionRunnable(criteria, action, result);

		// Submit task
		return handleSubmitTask(task, result, pool);
	}

	@Override
	public <S extends Serializable, T extends Serializable> TaskFuture<ResultHolder<S, T>> submitAsync(
			final S criteria, final ServiceAction<S, T> action, final String pool, final Cache<String, ResultHolder> cache, final String cacheKey,
			final boolean cacheException)
			throws ServiceException, RejectedServiceException {

		// Check action provided
		if (action == null) {
			throw new IllegalArgumentException("No service action has been provided. ");
		}

		// Check cache and cache key provided
		if (cache == null) {
			throw new IllegalArgumentException("A cache must be provided for async processing.");
		}
		if (cacheKey == null) {
			throw new IllegalArgumentException("A cache key must be provided for async processing. ");
		}

		// Maybe already in the result cache
		ResultHolder cached = cache.get(cacheKey);
		if (cached != null) {
			// Check for a cached exception
			if (cached.isException() && !cacheException) {
				// Invalidate cache and continue onto service call
				cache.remove(cacheKey);
			} else {
				LOGGER.debug(buildCacheMessagePrefix(cache, cacheKey) + "Async service already in cache so Future will hold the result.");
				return new TaskFutureResult<>(cached);
			}
		}

		// Check already in progress (if tracking enabled)
		if (isInProgressEnabled() && checkInProgress(cache, cacheKey)) {
			// Return a task future that checks the cache for the result
			LOGGER.debug(buildCacheMessagePrefix(cache, cacheKey) + "Async service already in progress so Future will wait for result in cache.");
			return new TaskFutureInProgress(cache, cacheKey);
		}

		LOGGER.debug(buildCacheMessagePrefix(cache, cacheKey) + "Async service call will be submitted.");

		// Setup the bean to hold the service result
		final ResultHolderMutable<S, T> result = new ResultHolderMutable(criteria);

		// Setup task to run service action and save the result in the cache
		Runnable task = new ServiceActionRunnable(criteria, action, result) {
			@Override
			public void run() {
				super.run();
				// Put result in the cache (unless is an exception and not caching exceptions)
				if (result.isResult() || (result.isException() && cacheException)) {
					cache.put(cacheKey, result);
				}
				// Clear in progress flag (if tracking enabled)
				if (isInProgressEnabled()) {
					clearInProgress(cache, cacheKey);
				}
			}
		};

		// Submit task
		TaskFuture<ResultHolder<S, T>> future = handleSubmitTask(task, result, pool);
		// Save in progress flag (if tracking enabled)
		if (isInProgressEnabled()) {
			saveInProgress(cache, cacheKey);
		}
		return future;
	}

	@Override
	public <S extends Serializable, T extends Serializable> ResultHolder<S, T> invokeSync(final S criteria, final ServiceAction<S, T> action)
			throws ServiceException {

		// Check action provided
		if (action == null) {
			throw new IllegalArgumentException("No service action has been provided. ");
		}

		// Do service call
		try {
			T resp = action.service(criteria);
			return new ResultHolderDefault<>(criteria, resp);
		} catch (Exception e) {
			return new ResultHolderDefault(criteria, ExceptionUtil.getSerializableException(e));
		}
	}

	@Override
	public <S extends Serializable, T extends Serializable> ResultHolder<S, T> invokeSync(final S criteria, final ServiceAction<S, T> action,
			final Cache<String, ResultHolder> cache, final String cacheKey, final boolean cacheException)
			throws ServiceException {

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
			// Check for a cached exception
			if (cached.isException() && !cacheException) {
				// Invalidate cache and continue onto service call
				cache.remove(cacheKey);
			} else {
				LOGGER.debug(buildCacheMessagePrefix(cache, cacheKey) + "Cached service call already in cache.");
				return cached;
			}
		}

		// Do service call
		ResultHolder<S, T> result = invokeSync(criteria, action);

		// Put result in the cache (unless is an exception and not caching exceptions)
		if (result.isResult() || (result.isException() && cacheException)) {
			cache.put(cacheKey, result);
		}

		return result;
	}

	/**
	 * Build the message prefix for logging.
	 *
	 * @param cache the cache result holder
	 * @param cacheKey the cache key being processed
	 * @return the message suffix
	 */
	protected String buildCacheMessagePrefix(final Cache<String, ResultHolder> cache, final String cacheKey) {
		return "Cache [" + cache.getName() + "] and key [" + cacheKey + "]. ";
	}

	/**
	 * Handle submitting the task to be run.
	 *
	 * @param <S> the criteria type
	 * @param <T> the response type
	 * @param task the task to submit for processing
	 * @param result the result holder
	 * @param pool the thread pool
	 * @return the task future
	 * @throws RejectedServiceException if the task cannot be scheduled for execution
	 */
	protected <S extends Serializable, T extends Serializable> TaskFuture<ResultHolder<S, T>> handleSubmitTask(
			final Runnable task, final ResultHolder result, final String pool)
			throws RejectedServiceException {
		try {
			return TaskMaster.submit(task, result, pool);
		} catch (RejectedTaskException e) {
			throw new RejectedServiceException(e.getMessage(), e);
		} catch (Exception e) {
			throw new RejectedServiceException("Could not start a thread to process task action. " + e.getMessage(), e);
		}
	}

	/**
	 * Flag if tracking in progress ASync cached service calls which helps avoid multiple calls for the same service call.
	 * <p>
	 * It only makes sense to track cached ASync calls as cached results are expected to be called multiple times and Sync calls require the result
	 * immediately anyway.
	 * </p>
	 *
	 * @return true if tracking in progress ASync cached service calls
	 */
	protected boolean isInProgressEnabled() {
		return IN_PROGRESS_ENABLED;
	}

	/**
	 * Check if this service call is already in progress.
	 *
	 * @param cache the result holder cache
	 * @param cacheKey the cache key
	 * @return the task future or null if not already processing
	 */
	protected boolean checkInProgress(final Cache<String, ResultHolder> cache, final String cacheKey) {
		return getInProgressCache().containsKey(getInProgressKey(cache, cacheKey));
	}

	/**
	 * Save in progress flag for this service call.
	 *
	 * @param cache the result holder cache
	 * @param cacheKey the cache key
	 */
	protected void saveInProgress(final Cache<String, ResultHolder> cache, final String cacheKey) {
		getInProgressCache().put(getInProgressKey(cache, cacheKey), Boolean.TRUE);
	}

	/**
	 * Clear the in progress flag for this service call.
	 *
	 * @param cache the result holder cache
	 * @param cacheKey the cache key
	 */
	protected void clearInProgress(final Cache<String, ResultHolder> cache, final String cacheKey) {
		getInProgressCache().remove(getInProgressKey(cache, cacheKey));
	}

	/**
	 * Helper method to build the in progress cache key.
	 *
	 * @param cache the result holder cache
	 * @param cacheKey the cache key
	 * @return the in progress cache key
	 */
	protected String getInProgressKey(final Cache<String, ResultHolder> cache, final String cacheKey) {
		return cache.getName() + "-" + cacheKey;
	}

	/**
	 * @return the in progress cache, or null if not enabled
	 */
	protected Cache<String, Boolean> getInProgressCache() {
		return IN_PROGRESS_CACHE;
	}

}
