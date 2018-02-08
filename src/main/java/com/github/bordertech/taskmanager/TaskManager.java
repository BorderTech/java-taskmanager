package com.github.bordertech.taskmanager;

import java.util.concurrent.RejectedExecutionException;

/**
 * TaskManager helps projects run ASYNC tasks.
 *
 * <p>
 * TaskManager allows a Runnable task to be submitted for execution and returns a Future representing that task. The
 * Future's get method will return the given result upon successful completion.
 * </p>
 * <p>
 * As Web applications require a Future implementation that can be serializable, the TaskManager has a custom interface
 * TaskFuture that implements both Future and Serializable. It does not make sense for a Future to be Serializable as it
 * is running on a specific thread on a particular server. To allow a Web Application to keep a reference to the Future,
 * the default implementation of TaskFuture (ie TaskFutureWrapper) wraps the future by putting the Future on a cache and
 * holding onto the cache key that is serializable.
 * </p>
 *
 * @author Jonathan Austin
 * @since 1.0.0
 */
public interface TaskManager {

	/**
	 * Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be
	 * accepted. Invocation has no additional effect if already shut down.
	 *
	 * <p>
	 * This method does not wait for previously submitted tasks to complete execution. Use
	 * {@link #awaitTermination awaitTermination} to do that.
	 *
	 * @throws SecurityException if a security manager exists and shutting down this ExecutorService may manipulate
	 * threads that the caller is not permitted to modify because it does not hold {@link
	 *         java.lang.RuntimePermission}<tt>("modifyThread")</tt>, or the security manager's <tt>checkAccess</tt> method
	 * denies access.
	 */
	void shutdown();

	/**
	 * Submits a Runnable task for execution and returns a Future representing that task. The Future's <tt>get</tt>
	 * method will return the given result upon successful completion.
	 *
	 * @param <T> the type for the future
	 * @param task the task to submit
	 * @param result the result to return
	 * @return a Future representing pending completion of the task
	 * @throws RejectedExecutionException if the task cannot be scheduled for execution
	 * @throws NullPointerException if the task is null
	 */
	<T> TaskFuture<T> submit(final Runnable task, final T result);

}
