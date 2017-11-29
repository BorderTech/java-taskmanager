package com.github.bordertech.taskmanager.impl;

import com.github.bordertech.taskmanager.TaskFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Holds a result.
 *
 * @author Jonathan Austin
 * @param <T> the result type
 * @since 1.0.0
 */
public class DefaultTaskFuture<T> implements TaskFuture<T> {

	private final T result;

	/**
	 * @param result the future's result
	 */
	public DefaultTaskFuture(final T result) {
		this.result = result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCancelled() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDone() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T get() throws InterruptedException, ExecutionException {
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return result;
	}

}
