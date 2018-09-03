package com.github.bordertech.taskmaster.service.impl;

import com.github.bordertech.config.Config;
import com.github.bordertech.didums.Didums;
import com.github.bordertech.taskmaster.RejectedTaskException;
import com.github.bordertech.taskmaster.TaskFuture;
import com.github.bordertech.taskmaster.TaskMaster;
import com.github.bordertech.taskmaster.TaskMasterException;
import com.github.bordertech.taskmaster.cache.CacheHelper;
import com.github.bordertech.taskmaster.service.CallType;
import com.github.bordertech.taskmaster.service.ExceptionUtil;
import com.github.bordertech.taskmaster.service.ResultHolder;
import com.github.bordertech.taskmaster.service.ServiceAction;
import com.github.bordertech.taskmaster.service.ServiceException;
import com.github.bordertech.taskmaster.service.ServiceHelper;
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
public final class ServiceHelperDefault implements ServiceHelper {

	private static final TaskMaster TASK_MASTER = Didums.getService(TaskMaster.class);
	private static final CacheHelper CACHE_HELPER = Didums.getService(CacheHelper.class);

	private static final String DEFAULT_RESULT_CACHE_NAME = "taskmaster-resultholder-default";
	private static final Long DEFAULT_RESULT_HOLDER_DURATION_SECONDS = Config.getInstance().getLong("bordertech.taskmaster.service.resultholder.cache.duration", Long.valueOf("1800"));
	private static final Duration DEFAULT_RESULT_DURATION = new Duration(TimeUnit.SECONDS, DEFAULT_RESULT_HOLDER_DURATION_SECONDS);

	private static final String DEFAULT_PROCESSING_CACHE_NAME = "taskmaster-processing-default";
	private static final Long DEFAULT_PROCESSING_DURATION_SECONDS = Config.getInstance().getLong("bordertech.taskmaster.service.processing.cache.duration", Long.valueOf("300"));
	private static final Duration DEFAULT_PROCESSING_DURATION = new Duration(TimeUnit.SECONDS, DEFAULT_PROCESSING_DURATION_SECONDS);

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
			throws RejectedTaskException {
		return handleServiceCallType(cache, cacheKey, criteria, action, callType, null);
	}

	@Override
	public <S extends Serializable, T extends Serializable> ResultHolder<S, T> handleServiceCallType(final Cache<String, ResultHolder> cache,
			final String cacheKey, final S criteria, final ServiceAction<S, T> action, final CallType callType, final String pool)
			throws RejectedTaskException {
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
			final String cacheKey, final S criteria, final ServiceAction<S, T> action) throws RejectedTaskException {
		return handleAsyncServiceCall(cache, cacheKey, criteria, action, null);
	}

	@Override
	public <S extends Serializable, T extends Serializable> ResultHolder<S, T> handleAsyncServiceCall(final Cache<String, ResultHolder> cache,
			final String cacheKey, final S criteria, final ServiceAction<S, T> action, final String pool) throws RejectedTaskException {

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
			return cached;
		}

		// Check already processing
		String processingKey = getProcessingKey(cache.getName(), cacheKey);
		if (getProcessingCache().get(processingKey) != null) {
			cached = checkASyncResult(cache, cacheKey);
			// Null for still processing or has result
			return cached;
		}

		// Setup the bean to hold the service result
		final ProcessingMutableResult<S, T> result = new ProcessingMutableResult(cacheKey);
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
		try {
			TaskFuture future = TASK_MASTER.submit(task, result, pool);
			// Cache the future
			getProcessingCache().put(processingKey, future);
			return null;
		} catch (RejectedTaskException e) {
			throw e;
		} catch (Exception e) {
			throw new TaskMasterException("Could not start thread to process task action. " + e.getMessage());
		}
	}

	@Override
	public synchronized <S extends Serializable, T extends Serializable> ResultHolder<S, T> checkASyncResult(final Cache<String, ResultHolder> cache,
			final String cacheKey) {

		// Check cache and cache key provided
		if (cache == null) {
			throw new IllegalArgumentException("A cache must be provided for async processing. ");
		}
		if (cacheKey == null) {
			throw new IllegalArgumentException("A cache key must be provided for async processing. ");
		}

		String processingKey = getProcessingKey(cache.getName(), cacheKey);

		// Get the future
		TaskFuture<ProcessingMutableResult> future = getProcessingCache().get(processingKey);

		// Future has expired or been removed from the Cache
		if (future == null) {
			// Maybe already in the result cache
			ResultHolder cached = cache.get(cacheKey);
			if (cached != null) {
				return cached;
			}
			throw new ServiceException("Future is no longer in the processing cache");
		}

		// Still processing
		if (!future.isDone()) {
			return null;
		}

		// Future was cancelled
		if (future.isCancelled()) {
			// Remove from cache
			clearProcessingCache(processingKey);
			throw new ServiceException("Future was cancelled.");
		}

		// Remove from the processing cache
		getProcessingCache().remove(processingKey);

		// Done, so Extract the result
		try {
			ProcessingMutableResult serviceResult = future.get();
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
		return CACHE_HELPER.getOrCreateCache(name, String.class, ResultHolder.class, duration);
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
		return CACHE_HELPER.getOrCreateCache(DEFAULT_PROCESSING_CACHE_NAME, String.class, TaskFuture.class, DEFAULT_PROCESSING_DURATION);
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
	 * @param cacheName the cache name
	 * @param cacheKey the cache key
	 * @return the processing cache key
	 */
	protected String getProcessingKey(final String cacheName, final String cacheKey) {
		return cacheName + "-" + cacheKey;
	}

	/**
	 * Used to hold the service result with the ASync processing cache.
	 *
	 * @param <M> the meta data type
	 * @param <T> the result type
	 */
	public static final class ProcessingMutableResult<M extends Serializable, T extends Serializable> implements Serializable {

		private final M metaData;
		private T result;
		private Exception exception;

		/**
		 * @param metaData the meta data
		 */
		public ProcessingMutableResult(final M metaData) {
			this.metaData = metaData;
		}

		/**
		 * @return the meta data
		 */
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
		 * @param result the result
		 */
		public void setResult(final T result) {
			this.result = result;
			this.exception = null;
		}

		/**
		 * @return the exception or null if has result
		 */
		public Exception getException() {
			return exception;
		}

		/**
		 * @param exception the exception when calling the service
		 */
		public void setException(final Exception exception) {
			this.exception = exception;
			this.result = null;
		}

		/**
		 * @return true if holding an exception
		 */
		public boolean isException() {
			return exception != null;
		}

		/**
		 * @return true if holding a result
		 */
		public boolean isResult() {
			return !isException();
		}

	}

}
