package com.github.bordertech.taskmaster.impl;

import com.github.bordertech.taskmaster.exception.RejectedTaskException;
import com.github.bordertech.taskmaster.TaskFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import javax.inject.Singleton;
import com.github.bordertech.taskmaster.TaskMasterProvider;

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

	/**
	 * TODO This needs to be put in TaskMaster.
	 */
	public void shutdownNow() {
		// Provide an immediate shutdown of threads without running the waiting threads.
		TaskMasterPoolUtil.shutdown();
	}

	@Override
	public <T> TaskFuture<T> submit(final Runnable task, final T result) throws RejectedTaskException {
		return submit(task, result, TaskMasterPoolUtil.DEFAULT_POOL);
	}

	@Override
	public <T> TaskFuture<T> submit(final Runnable task, final T result, final String pool) throws RejectedTaskException {
		ExecutorService exec = TaskMasterPoolUtil.getPool(pool);
		try {
			Future<T> future = exec.submit(task, result);
			return new TaskFutureWrapper<>(future);
		} catch (RejectedExecutionException e) {
			throw new RejectedTaskException("Unable to start task in pool [" + pool + "].", e);
		}
	}

}
