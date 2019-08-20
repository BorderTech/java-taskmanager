package com.github.bordertech.taskmaster.impl;

import com.github.bordertech.taskmaster.TaskFuture;
import com.github.bordertech.taskmaster.exception.TaskMasterException;
import java.io.Serializable;
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
public class TaskFutureResult<T extends Serializable> implements TaskFuture<T> {

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
		T res = getResult();
		if (res instanceof TaskMasterException) {
			TaskMasterException excp = (TaskMasterException) res;
			throw new ExecutionException("Error processing future. " + excp.getMessage(), excp);
		}
		return res;
	}

	@Override
	public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return get();
	}

	/**
	 * @return the result
	 */
	protected T getResult() {
		return result;
	}

}
