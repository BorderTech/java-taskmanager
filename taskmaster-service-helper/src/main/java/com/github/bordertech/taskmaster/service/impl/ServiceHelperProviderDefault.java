package com.github.bordertech.taskmaster.service.impl;

import com.github.bordertech.config.Config;
import com.github.bordertech.taskmaster.TaskFuture;
import com.github.bordertech.taskmaster.TaskMaster;
import com.github.bordertech.taskmaster.cache.CachingHelper;
import com.github.bordertech.taskmaster.exception.RejectedTaskException;
import com.github.bordertech.taskmaster.service.ResultHolder;
import com.github.bordertech.taskmaster.service.ServiceAction;
import com.github.bordertech.taskmaster.service.exception.AsyncServiceException;
import com.github.bordertech.taskmaster.service.exception.ExceptionUtil;
import com.github.bordertech.taskmaster.service.exception.RejectedServiceException;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import javax.cache.expiry.Duration;
import javax.inject.Singleton;

/**
 * Helper utility for sync and async service calls.
 * <p>
 * Service results (successful or exception) are stored as ResultHolders in the named cache.
 * </p>
 * <p>
 * This helper provides a default cache or projects can create caches with assigned names.
 * </p>
 *
 * @author Jonathan Austin
 * @since 1.0.0
 */
@Singleton
public final class ServiceHelperProviderDefault extends AbstractServiceHelperProvider {

	private static final String DEFAULT_RESULT_CACHE_NAME = "taskmaster-resultholder-default";
	private static final Long DEFAULT_RESULT_HOLDER_DURATION_SECONDS
			= Config.getInstance().getLong("bordertech.taskmaster.service.resultholder.cache.duration", Long.valueOf("1800"));
	private static final Duration DEFAULT_RESULT_DURATION = new Duration(TimeUnit.SECONDS, DEFAULT_RESULT_HOLDER_DURATION_SECONDS);

	private static final String DEFAULT_PROCESSING_CACHE_NAME = "taskmaster-processing-default";
	private static final Long DEFAULT_PROCESSING_DURATION_SECONDS
			= Config.getInstance().getLong("bordertech.taskmaster.service.processing.cache.duration", Long.valueOf("300"));
	private static final Duration DEFAULT_PROCESSING_DURATION = new Duration(TimeUnit.SECONDS, DEFAULT_PROCESSING_DURATION_SECONDS);

	@Override
	public Cache<String, ResultHolder> getDefaultResultHolderCache() {
		return getResultHolderCache(DEFAULT_RESULT_CACHE_NAME, DEFAULT_RESULT_DURATION);
	}

	@Override
	public synchronized Cache<String, ResultHolder> getResultHolderCache(final String name) {
		return getResultHolderCache(name, DEFAULT_RESULT_DURATION);
	}

	@Override
	public Cache<String, ResultHolder> getResultHolderCache(final String name, final Duration duration) {
		return CachingHelper.getProvider().getOrCreateCache(name, String.class, ResultHolder.class, duration);
	}

	@Override
	public synchronized <S extends Serializable, T extends Serializable> ResultHolder<S, T> checkASyncResult(final Cache<String, ResultHolder> cache,
			final String cacheKey) throws AsyncServiceException {

		// Check cache and cache key provided
		if (cache == null) {
			throw new IllegalArgumentException("A cache must be provided for async processing. ");
		}
		if (cacheKey == null) {
			throw new IllegalArgumentException("A cache key must be provided for async processing. ");
		}

		String processingKey = getProcessingKey(cache, cacheKey);

		// Get the future
		TaskFuture<ProcessingMutableResult> future = getProcessingCache().get(processingKey);

		// Future has expired or been removed from the Cache
		if (future == null) {
			// Maybe already in the result cache
			ResultHolder cached = cache.get(cacheKey);
			if (cached != null) {
				return cached;
			}
			throw new AsyncServiceException("Future is no longer in the processing cache");
		}

		// Still processing
		if (!future.isDone()) {
			return null;
		}

		// Future was cancelled
		if (future.isCancelled()) {
			// Remove from cache
			clearProcessingCache(processingKey);
			throw new AsyncServiceException("Future was cancelled.");
		}

		// Remove from the processing cache
		getProcessingCache().remove(processingKey);

		// Done, so Extract the result
		ProcessingMutableResult serviceResult;
		try {
			serviceResult = future.get();
		} catch (InterruptedException e) {
			// Restore interrupted state...
			Thread.currentThread().interrupt();
			throw new AsyncServiceException("Getting result from Future but thread was interrupted. " + e.getMessage(), e);
		} catch (ExecutionException e) {
			throw new AsyncServiceException("Could not get result from the future. " + e.getMessage(), e);
		}
		ResultHolder result;
		if (serviceResult.isException()) {
			result = new ResultHolder(serviceResult.getMetaData(), serviceResult.getException());
		} else {
			result = new ResultHolder(serviceResult.getMetaData(), serviceResult.getResult());
		}
		// Cache the result
		cache.put(cacheKey, result);
		return result;
	}

	@Override
	protected <S extends Serializable, T extends Serializable> boolean isAsyncServiceRunning(final Cache<String, ResultHolder> cache,
			final String cacheKey, final S criteria, final ServiceAction<S, T> action, final String pool) {
		String processingKey = getProcessingKey(cache, cacheKey);
		return getProcessingCache().containsKey(processingKey);
	}

	@Override
	protected <S extends Serializable, T extends Serializable> void submitAsyncService(final Cache<String, ResultHolder> cache,
			final String cacheKey, final S criteria, final ServiceAction<S, T> action, final String pool) throws RejectedServiceException {
		// Setup the bean to hold the service result
		final ProcessingMutableResult<S, T> result = new ProcessingMutableResult(cacheKey);
		// Runnable to submit
		Runnable task = new Runnable() {
			@Override
			public void run() {
				try {
					T resp = action.service(criteria);
					result.setResult(resp);
				} catch (Exception e) {
					// Check exception is serializable to be held in the cache (sometimes they arent)
					result.setException(ExceptionUtil.getSerializableException(e));
				}
			}
		};
		// Processing key
		String processingKey = getProcessingKey(cache, cacheKey);
		try {
			TaskFuture future = TaskMaster.getProvider().submit(task, result, pool);
			// Cache the future
			getProcessingCache().put(processingKey, future);
		} catch (RejectedTaskException e) {
			throw new RejectedServiceException(e.getMessage(), e);
		} catch (Exception e) {
			throw new RejectedServiceException("Could not start a thread to process task action. " + e.getMessage(), e);
		}
	}

	/**
	 * Use a cache to hold a reference to the services currently being processed.
	 * <p>
	 * Note - Depending on the TaskFuture implementation, the TaskFuture internally maybe caching the future as well but
	 * this utility should not care or be affected by the internal task cache.
	 * </p>
	 *
	 * @return the processing cache instance
	 */
	protected Cache<String, TaskFuture> getProcessingCache() {
		return CachingHelper.getProvider().getOrCreateCache(DEFAULT_PROCESSING_CACHE_NAME, String.class, TaskFuture.class, DEFAULT_PROCESSING_DURATION);
	}

	/**
	 * @param key the cache key to remove from the processing cache
	 */
	protected void clearProcessingCache(final String key) {
		TaskFuture future = getProcessingCache().get(key);
		if (future != null) {
			if (!future.isDone() && !future.isCancelled()) {
				future.cancel(true);
			}
			getProcessingCache().remove(key);
		}
	}

	/**
	 * Helper method to build the processing cache key.
	 *
	 * @param cache the result holder cache
	 * @param cacheKey the cache key
	 * @return the processing cache key
	 */
	protected String getProcessingKey(final Cache<String, ResultHolder> cache, final String cacheKey) {
		return cache.getName() + "-" + cacheKey;
	}

}
