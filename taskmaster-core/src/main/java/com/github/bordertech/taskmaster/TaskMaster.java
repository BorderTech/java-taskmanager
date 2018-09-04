package com.github.bordertech.taskmaster;

import com.github.bordertech.didums.Didums;
import com.github.bordertech.taskmaster.impl.TaskMasterProviderExecutorService;

/**
 * TaskMaster helps projects run ASYNC tasks.
 *
 * <p>
 * TaskMaster allows a Runnable task to be submitted for execution and returns a Future representing that task. The
 * Future's get method will return the given result upon successful completion.
 * </p>
 * <p>
 * As Web applications require a Future implementation that can be serializable, the TaskMaster has a custom interface
 * TaskFuture that implements both Future and Serializable. It does not make sense for a Future to be Serializable as it
 * is running on a specific thread on a particular server. To allow a Web Application to keep a reference to the Future,
 * the default implementation of TaskFuture (ie TaskFutureWrapper) wraps the future by putting the Future on a cache and
 * holding onto the cache key that is serializable.
 * </p>
 *
 * @author Jonathan Austin
 * @since 1.0.0
 */
public final class TaskMaster {

	private static final TaskMasterProvider PROVIDER;

	static {
		// Check if instance defined
		if (Didums.hasService(TaskMasterProvider.class)) {
			PROVIDER = Didums.getService(TaskMasterProvider.class);
		} else {
			// Default Implementation
			PROVIDER = new TaskMasterProviderExecutorService();
		}
	}

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

}
