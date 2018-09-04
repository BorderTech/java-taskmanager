package com.github.bordertech.taskmaster;

import com.github.bordertech.taskmaster.exception.RejectedTaskException;

/**
 * TaskMasterProvider helps projects run ASYNC tasks.
 *
 * @author Jonathan Austin
 * @since 1.0.0
 */
public interface TaskMasterProvider {

	/**
	 * Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be
	 * accepted. Invocation has no additional effect if already shut down.
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
	<T> TaskFuture<T> submit(final Runnable task, final T result) throws RejectedTaskException;

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
	<T> TaskFuture<T> submit(final Runnable task, final T result, final String pool) throws RejectedTaskException;

}
