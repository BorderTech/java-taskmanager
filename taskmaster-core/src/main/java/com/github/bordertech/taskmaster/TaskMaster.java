package com.github.bordertech.taskmaster;

import com.github.bordertech.didums.Didums;
import com.github.bordertech.taskmaster.exception.RejectedTaskException;
import com.github.bordertech.taskmaster.impl.TaskMasterProviderExecutorService;
import java.io.Serializable;

/**
 * TaskMaster helps projects run ASYNC tasks.
 *
 * <p>
 * TaskMaster allows a Runnable task to be submitted for execution and returns a Future representing that task. The Future's get method will return
 * the given result upon successful completion.
 * </p>
 * <p>
 * As Web applications require a Future implementation that can be serializable, the TaskMaster has a custom interface TaskFuture that implements both
 * Future and Serializable. It does not make sense for a Future to be Serializable as it is running on a specific thread on a particular server. To
 * allow a Web Application to keep a reference to the Future, the default implementation of TaskFuture (ie TaskFutureWrapper) wraps the future by
 * putting the Future on a cache and holding onto the cache key that is serializable.
 * </p>
 *
 * @author Jonathan Austin
 * @since 1.0.0
 */
public final class TaskMaster {

	private static final TaskMasterProvider PROVIDER = Didums.getService(TaskMasterProvider.class, TaskMasterProviderExecutorService.class);

	/**
	 * Private constructor.
	 */
	private TaskMaster() {
	}

	/**
	 * @return the TaskMasterProvider
	 */
	public static TaskMasterProvider getProvider() {
		return PROVIDER;
	}

	/**
	 * Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be accepted. Invocation has no additional
	 * effect if already shut down.
	 *
	 * @throws SecurityException if a security manager exists and shutting down this ExecutorService may manipulate threads that the caller is not
	 * permitted to modify because it does not hold permission
	 */
	public static void shutdown() {
		PROVIDER.shutdown();
	}

	/**
	 * Attempts to stop all actively executing tasks, halts the processing of waiting tasks.
	 *
	 * @throws SecurityException if a security manager exists and shutting down this ExecutorService may manipulate threads that the caller is not
	 * permitted to modify because it does not hold permission
	 */
	public static void shutdownNow() {
		PROVIDER.shutdownNow();
	}

	/**
	 * Submits a Runnable task for execution and returns a Future representing that task. The Future's <tt>get</tt>
	 * method will return the given result upon successful completion.
	 * <p>
	 * Uses the default thread pool.
	 * </p>
	 *
	 * @param <T> the type for the future
	 * @param task the task to submit
	 * @param result the result to return
	 * @return a Future representing pending completion of the task
	 * @throws RejectedTaskException if the task cannot be scheduled for execution
	 */
	public static <T extends Serializable> TaskFuture<T> submit(final Runnable task, final T result) throws RejectedTaskException {
		return PROVIDER.submit(task, result);
	}

	/**
	 * Submits a Runnable task for execution and returns a Future representing that task. The Future's <tt>get</tt>
	 * method will return the given result upon successful completion.
	 *
	 * @param <T> the type for the future
	 * @param task the task to submit
	 * @param result the result to return
	 * @param pool the thread pool name, or null if no pool
	 * @return a Future representing pending completion of the task
	 * @throws RejectedTaskException if the task cannot be scheduled for execution
	 */
	public static <T extends Serializable> TaskFuture<T> submit(final Runnable task, final T result, final String pool) throws RejectedTaskException {
		return PROVIDER.submit(task, result, pool);
	}
}
