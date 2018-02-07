package com.github.bordertech.taskmanager.impl;

import com.github.bordertech.didums.Didums;
import com.github.bordertech.taskmanager.TaskFuture;
import com.github.bordertech.taskmanager.TaskFutureWrapperCache;
import com.github.bordertech.taskmanager.TaskManagerException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Uses a cache to wrap the future allowing the cache key reference to be serializable.
 *
 * @param <T> the future get type
 * @author Jonathan Austin
 * @since 1.0.0
 */
public class TaskFutureWrapper<T> implements TaskFuture<T> {

	private static final TaskFutureWrapperCache CACHE = Didums.getService(TaskFutureWrapperCache.class);

	private final String id = UUID.randomUUID().toString();

	/**
	 * @param future the backing future
	 */
	public TaskFutureWrapper(final Future<T> future) {
		setFuture(future);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		return getFuture().cancel(mayInterruptIfRunning);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCancelled() {
		return getFuture().isCancelled();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDone() {
		return getFuture().isDone();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T get() throws InterruptedException, ExecutionException {
		return getFuture().get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return getFuture().get(timeout, unit);
	}

	/**
	 * @param future the future to save in the cache
	 */
	protected final void setFuture(final Future<T> future) {
		CACHE.putFuture(id, future);
	}

	/**
	 * @return the future object from the cache
	 */
	protected final Future<T> getFuture() {
		Future<T> future = CACHE.getFuture(id);
		if (future == null) {
			// Future has expired or been removed from the cache
			future = new TaskFutureResult(new TaskManagerException("Future has been removed from the cache"));
			CACHE.putFuture(id, future);
		}
		return future;
	}

}
