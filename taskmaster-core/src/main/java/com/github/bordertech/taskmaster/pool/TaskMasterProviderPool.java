package com.github.bordertech.taskmaster.pool;

import com.github.bordertech.taskmaster.exception.RejectedTaskException;
import com.github.bordertech.taskmaster.TaskFuture;
import com.github.bordertech.taskmaster.impl.TaskFutureWrapper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import javax.inject.Singleton;
import com.github.bordertech.taskmaster.TaskMasterProvider;

/**
 * TaskMaster implementation that allows for Thread Pools.
 */
@Singleton
public class TaskMasterProviderPool implements TaskMasterProvider {

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
