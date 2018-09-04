package com.github.bordertech.taskmaster.service.impl;

import com.github.bordertech.taskmaster.service.CallType;
import com.github.bordertech.taskmaster.service.ResultHolder;
import com.github.bordertech.taskmaster.service.ServiceAction;
import com.github.bordertech.taskmaster.service.ServiceHelperProvider;
import com.github.bordertech.taskmaster.service.exception.AsyncServiceException;
import com.github.bordertech.taskmaster.service.exception.ExceptionUtil;
import com.github.bordertech.taskmaster.service.exception.RejectedServiceException;
import java.io.Serializable;
import javax.cache.Cache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provide a starter implementation of ServiceHelperProvider.
 */
public abstract class AbstractServiceHelperProvider implements ServiceHelperProvider {

	private static final Log LOGGER = LogFactory.getLog(AbstractServiceHelperProvider.class);

	@Override
	public <S extends Serializable, T extends Serializable> ResultHolder<S, T> handleServiceCall(final S criteria, final ServiceAction<S, T> action) {
		// Check action provided
		if (action == null) {
			throw new IllegalArgumentException("No service action has been provided. ");
		}

		// Do service call
		try {
			T resp = action.service(criteria);
			return new ResultHolder(criteria, resp);
		} catch (Exception e) {
			return new ResultHolder(criteria, ExceptionUtil.getSerializableException(e));
		}
	}

	@Override
	public <S extends Serializable, T extends Serializable> ResultHolder<S, T> handleServiceCallType(final Cache<String, ResultHolder> cache,
			final String cacheKey, final S criteria, final ServiceAction<S, T> action, final CallType callType)
			throws RejectedServiceException {
		return handleServiceCallType(cache, cacheKey, criteria, action, callType, null);
	}

	@Override
	public <S extends Serializable, T extends Serializable> ResultHolder<S, T> handleServiceCallType(final Cache<String, ResultHolder> cache,
			final String cacheKey, final S criteria, final ServiceAction<S, T> action, final CallType callType, final String pool)
			throws RejectedServiceException {
		if (callType == null) {
			throw new IllegalArgumentException("Call type must be provided.");
		}
		// Refresh the Cache
		if (callType.isRefresh()) {
			cache.remove(cacheKey);
		}
		if (callType.isAsync()) {
			// ASync
			return handleAsyncServiceCall(cache, cacheKey, criteria, action, pool);
		} else {
			// Sync
			return handleCachedServiceCall(cache, cacheKey, criteria, action);
		}
	}

	@Override
	public <S extends Serializable, T extends Serializable> ResultHolder<S, T> handleCachedServiceCall(final Cache<String, ResultHolder> cache,
			final String cacheKey, final S criteria, final ServiceAction<S, T> action) {

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
			LOGGER.debug(buildCacheMessagePrefix(cache, cacheKey) + "Cached service call already in cache.");
			return cached;
		}
		// Do service call
		ResultHolder<S, T> result = handleServiceCall(criteria, action);
		// Save in the cache
		cache.put(cacheKey, result);

		return result;
	}

	@Override
	public <S extends Serializable, T extends Serializable> ResultHolder<S, T> handleAsyncServiceCall(final Cache<String, ResultHolder> cache,
			final String cacheKey, final S criteria, final ServiceAction<S, T> action) throws RejectedServiceException {
		return handleAsyncServiceCall(cache, cacheKey, criteria, action, null);
	}

	@Override
	public <S extends Serializable, T extends Serializable> ResultHolder<S, T> handleAsyncServiceCall(final Cache<String, ResultHolder> cache,
			final String cacheKey, final S criteria, final ServiceAction<S, T> action, final String pool) throws RejectedServiceException {

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

		// Maybe already in the result cache
		ResultHolder cached = cache.get(cacheKey);
		if (cached != null) {
			LOGGER.debug(buildCacheMessagePrefix(cache, cacheKey) + "Async service call already in cache.");
			return cached;
		}

		// Check already processing
		if (isAsyncServiceRunning(cache, cacheKey, criteria, action, pool)) {
			LOGGER.debug(buildCacheMessagePrefix(cache, cacheKey) + "Async service call is already processing. A new service call will not be submitted.");
			try {
				return checkASyncResult(cache, cacheKey);
			} catch (AsyncServiceException e) {
				LOGGER.debug(buildCacheMessagePrefix(cache, cacheKey) + "Error checking Async service call. Will be ignored and submitted again.");
			}
		}

		// Submit the service call
		LOGGER.debug(buildCacheMessagePrefix(cache, cacheKey) + "Async service call will be submitted.");
		submitAsyncService(cache, cacheKey, criteria, action, pool);
		return null;
	}

	/**
	 *
	 * @param cache the cache result holder
	 * @param cacheKey the cache key being processed
	 * @return the message suffix
	 */
	protected String buildCacheMessagePrefix(final Cache<String, ResultHolder> cache, final String cacheKey) {
		return "Cache [" + cache.getName() + "] and key [" + cacheKey + "]. ";
	}

	/**
	 * Check if the service is already running.
	 *
	 * @param cache the result holder cache
	 * @param cacheKey the key for the result holder
	 * @param criteria the criteria
	 * @param action the service action
	 * @param pool the service thread pool
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return true if task is already running
	 */
	protected abstract <S extends Serializable, T extends Serializable> boolean isAsyncServiceRunning(final Cache<String, ResultHolder> cache,
			final String cacheKey, final S criteria, final ServiceAction<S, T> action, final String pool);

	/**
	 * Submit the async service call.
	 *
	 * @param cache the result holder cache
	 * @param cacheKey the key for the result holder
	 * @param criteria the criteria
	 * @param action the service action
	 * @param pool the service thread pool
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @throws RejectedServiceException if the task cannot be scheduled for execution
	 */
	protected abstract <S extends Serializable, T extends Serializable> void submitAsyncService(final Cache<String, ResultHolder> cache,
			final String cacheKey, final S criteria, final ServiceAction<S, T> action, final String pool)
			throws RejectedServiceException;

}
