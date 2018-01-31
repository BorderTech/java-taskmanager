package com.github.bordertech.taskmanager.impl;

import com.github.bordertech.taskmanager.TaskFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Holds an exception when processing the future.
 *
 * @param <T> the result type
 * @author Jonathan Austin
 * @since 1.0.0
 */
public class DefaultExceptionTaskFuture<T> implements TaskFuture<T> {

	private final Exception exception;

	/**
	 * @param exception the future's result
	 */
	public DefaultExceptionTaskFuture(final Exception exception) {
		this.exception = exception;
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
		throw new ExecutionException("Error processing future. " + exception.getMessage(), exception);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return get();
	}

}
