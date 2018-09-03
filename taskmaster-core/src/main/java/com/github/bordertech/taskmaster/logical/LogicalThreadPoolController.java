package com.github.bordertech.taskmaster.logical;

/**
 * Component that controls a logical thread pool.
 */
public interface LogicalThreadPoolController {

	/**
	 *
	 * @return true if thread available
	 */
	boolean acquireThread();

	/**
	 * Release the thread back to the pool.
	 */
	void releaseThread();

}
