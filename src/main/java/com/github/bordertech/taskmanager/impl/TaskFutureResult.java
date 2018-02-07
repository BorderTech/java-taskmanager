package com.github.bordertech.taskmanager.impl;

import com.github.bordertech.taskmanager.TaskFuture;
import com.github.bordertech.taskmanager.TaskManagerException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A Serializable Future used to a hold a result.
 *
 * @param <T> the result type
 *
 * @author Jonathan Austin
 * @since 1.0.0
 */
public class TaskFutureResult<T> implements TaskFuture<T> {

	private final T result;

	/**
	 * @param result the future's result
	 */
	public TaskFutureResult(final T result) {
		this.result = result;
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
		return true;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		if (result instanceof TaskManagerException) {
			Exception excp = (TaskManagerException) result;
			throw new ExecutionException("Error processing future. " + excp.getMessage(), excp);
		}
		return result;
	}

	@Override
	public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return get();
	}

}
