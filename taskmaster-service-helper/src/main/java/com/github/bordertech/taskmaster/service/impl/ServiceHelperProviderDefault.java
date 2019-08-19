package com.github.bordertech.taskmaster.service.impl;

import com.github.bordertech.taskmaster.TaskFuture;
import com.github.bordertech.taskmaster.TaskMaster;
import com.github.bordertech.taskmaster.exception.RejectedTaskException;
import com.github.bordertech.taskmaster.impl.TaskFutureResult;
import com.github.bordertech.taskmaster.service.ResultHolder;
import com.github.bordertech.taskmaster.service.ServiceAction;
import com.github.bordertech.taskmaster.service.ServiceHelperProvider;
import com.github.bordertech.taskmaster.service.exception.RejectedServiceException;
import com.github.bordertech.taskmaster.service.exception.ServiceException;
import com.github.bordertech.taskmaster.service.util.ExceptionUtil;
import java.io.Serializable;
import javax.cache.Cache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default Service Helper provider implementation.
 */
public class ServiceHelperProviderDefault implements ServiceHelperProvider {

	private static final Log LOGGER = LogFactory.getLog(ServiceHelperProviderDefault.class);

	@Override
	public <S extends Serializable, T extends Serializable> TaskFuture<ResultHolder<S, T>> submitAsync(final S criteria, final ServiceAction<S, T> action, final String pool)
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
	public <S extends Serializable, T extends Serializable> TaskFuture<ResultHolder<S, T>> submitAsync(final S criteria, final ServiceAction<S, T> action, final String pool,
			final Cache<String, ResultHolder> cache, final String cacheKey, final boolean cacheException)
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
				LOGGER.debug(buildCacheMessagePrefix(cache, cacheKey) + "Async service call already in cache.");
				return new TaskFutureResult<>(cached);
			}
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
			}
		};

		// Submit task
		return handleSubmitTask(task, result, pool);
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
	protected <S extends Serializable, T extends Serializable> TaskFuture<ResultHolder<S, T>> handleSubmitTask(final Runnable task, final ResultHolder result, final String pool)
			throws RejectedServiceException {
		try {
			return TaskMaster.submit(task, result, pool);
		} catch (RejectedTaskException e) {
			throw new RejectedServiceException(e.getMessage(), e);
		} catch (Exception e) {
			throw new RejectedServiceException("Could not start a thread to process task action. " + e.getMessage(), e);
		}
	}

}
