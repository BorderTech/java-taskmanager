package com.github.bordertech.taskmaster.service.impl;

import com.github.bordertech.taskmaster.TaskFuture;
import com.github.bordertech.taskmaster.cache.CachingHelper;
import com.github.bordertech.taskmaster.service.ResultHolder;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.cache.Cache;

/**
 * Task Future that is waiting for the result to be in the cache as another task is already processing the service request.
 *
 * @param <S> the meta type
 * @param <T> the result type
 */
public class TaskFutureInProgress<S extends Serializable, T extends Serializable> implements TaskFuture<ResultHolder<S, T>> {

	// Cache instance is transient as TaskFuture needs to be Serializable
	private transient Cache<String, ResultHolder> cache;
	private final String cacheName;
	private final String cacheKey;
	private ResultHolder result;

	/**
	 * @param cache the result holder cache
	 * @param cacheKey the cache key
	 */
	public TaskFutureInProgress(final Cache<String, ResultHolder> cache, final String cacheKey) {
		this.cache = cache;
		this.cacheName = cache.getName();
		this.cacheKey = cacheKey;
	}

	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return getResult() != null;
	}

	@Override
	public ResultHolder get() throws InterruptedException, ExecutionException {
		return getResult();
	}

	@Override
	public ResultHolder get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return get();
	}

	/**
	 * @return the cache key
	 */
	protected String getCacheKey() {
		return cacheKey;
	}

	/**
	 * @return the result holder cache
	 */
	protected Cache<String, ResultHolder> getCache() {
		if (cache == null) {
			// Need to reinstate the cache instance as must have been serialized
			// Assumes the cache is already setup and configured so no duration specified
			cache = CachingHelper.getOrCreateCache(cacheName, String.class, ResultHolder.class);
		}
		return cache;
	}

	/**
	 * @return the result holder or null if not in the cache
	 */
	protected ResultHolder<S, T> getResult() {
		if (result == null) {
			result = getCache().get(getCacheKey());
		}
		return result;
	}

}
