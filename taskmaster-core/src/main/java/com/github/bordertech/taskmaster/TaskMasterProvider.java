package com.github.bordertech.taskmaster;

import com.github.bordertech.taskmaster.exception.RejectedTaskException;
import java.io.Serializable;

/**
 * TaskMasterProvider helps projects run ASYNC tasks.
 *
 * @author Jonathan Austin
 * @since 1.0.0
 */
public interface TaskMasterProvider {

	/**
	 * Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be accepted. Invocation has no additional
	 * effect if already shut down.
	 *
	 * @throws SecurityException if a security manager exists and shutting down this ExecutorService may manipulate threads that the caller is not
	 * permitted to modify because it does not hold permission
	 */
	void shutdown();

	/**
	 * Attempts to stop all actively executing tasks, halts the processing of waiting tasks.
	 *
	 * @throws SecurityException if a security manager exists and shutting down this ExecutorService may manipulate threads that the caller is not
	 * permitted to modify because it does not hold permission
	 */
	void shutdownNow();

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
	<T extends Serializable> TaskFuture<T> submit(Runnable task, T result) throws RejectedTaskException;

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
	<T extends Serializable> TaskFuture<T> submit(Runnable task, T result, String pool) throws RejectedTaskException;

}
