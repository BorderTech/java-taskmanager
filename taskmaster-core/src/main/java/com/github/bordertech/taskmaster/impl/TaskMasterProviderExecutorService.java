package com.github.bordertech.taskmaster.impl;

import com.github.bordertech.taskmaster.TaskFuture;
import com.github.bordertech.taskmaster.TaskMasterProvider;
import com.github.bordertech.taskmaster.exception.RejectedTaskException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import javax.inject.Singleton;

/**
 * Handle running tasks via {@link ExecutorService}.
 *
 * @author Jonathan Austin
 * @since 1.0.0
 */
@Singleton
public class TaskMasterProviderExecutorService implements TaskMasterProvider {

	@Override
	public void shutdown() {
		shutdownNow();
	}

	@Override
	public void shutdownNow() {
		// Provide an immediate shutdown of threads without running the waiting threads.
		TaskMasterPoolUtil.shutdownNow();
	}

	@Override
	public <T> TaskFuture<T> submit(final Runnable task, final T result) throws RejectedTaskException {
		return submit(task, result, TaskMasterPoolUtil.DEFAULT_POOL);
	}

	@Override
	public <T> TaskFuture<T> submit(final Runnable task, final T result, final String pool) throws RejectedTaskException {
		if (task == null) {
			throw new IllegalArgumentException("Task cannot be null");
		}
		if (result == null) {
			throw new IllegalArgumentException("Result cannot be null");
		}
		if (pool == null) {
			throw new IllegalArgumentException("Pool cannot be null");
		}
		// Get the executor
		ExecutorService exec = getPool(pool);
		// Submit the task
		try {
			Future<T> future = exec.submit(task, result);
			return new TaskFutureWrapper<>(future);
		} catch (RejectedExecutionException e) {
			throw new RejectedTaskException("Unable to start task in pool [" + pool + "].", e);
		}
	}

	/**
	 * @param pool the pool to execute the task in
	 * @return the ExecutorService for this pool
	 */
	protected ExecutorService getPool(final String pool) {
		return TaskMasterPoolUtil.getPool(pool);
	}

}
