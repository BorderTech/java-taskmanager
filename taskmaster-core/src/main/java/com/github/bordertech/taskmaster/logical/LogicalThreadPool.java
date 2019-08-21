package com.github.bordertech.taskmaster.logical;

import com.github.bordertech.taskmaster.impl.TaskMasterProperties;
import java.io.Serializable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User session logical thread pool details.
 * <p>
 * Can be used to control a logical pool of threads per user session.
 * </p>
 */
public class LogicalThreadPool implements Serializable {

	private static final Log LOGGER = LogFactory.getLog(LogicalThreadPool.class);
	private final String name;
	private final int max;
	private final Semaphore semaphore;
	private boolean shutdown;

	/**
	 * Default constructor with default name and no limit to threads.
	 */
	public LogicalThreadPool() {
		this(null, 0);
	}

	/**
	 * @param name thread pool name
	 * @param max the maximum threads. Zero means no limit.
	 */
	public LogicalThreadPool(final String name, final int max) {
		this.name = StringUtils.isEmpty(name) ? "default" : name;
		this.max = max > 0 ? max : 0;
		this.semaphore = new Semaphore(this.max, true);
	}

	/**
	 * @return the thread pool name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @return the maximum threads
	 */
	public final int getMax() {
		return max;
	}

	/**
	 * @return the current threads in use
	 */
	public int getCurrent() {
		return max - semaphore.availablePermits();
	}

	/**
	 * @return true if thread pool is shutdown
	 */
	public synchronized boolean isShutdown() {
		return shutdown;
	}

	/**
	 * Shutdown the thread pool.
	 */
	public void shutdownPool() {
		setShutdown(true);
	}

	/**
	 * Start the thread pool.
	 */
	public void startPool() {
		setShutdown(false);
	}

	/**
	 * Called when a thread finishes processing.
	 */
	public void finished() {
		if (getCurrent() > 0) {
			semaphore.release();
		}
	}

	/**
	 * @return true if thread pool has available threads
	 */
	public boolean getAccess() {
		checkPoolStatus();
		if (max <= 0) {
			return true;
		}
		// No block
		return semaphore.tryAcquire();
	}

	/**
	 * @param timeout the wait interval
	 * @param unit the interval unit
	 * @return true if the thread pool has available threads
	 */
	public boolean getAccess(final long timeout, final TimeUnit unit) {
		checkPoolStatus();
		if (max <= 0) {
			return true;
		}
		boolean result = false;
		try {
			result = semaphore.tryAcquire(timeout, unit);
		} catch (InterruptedException e) {
			// Restore interrupted state...
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while trying to gain access to thread pool ["
					+ getName() + "].", e);
		}
		return result;
	}

	/**
	 * Wait till a thread is available.
	 * <p>
	 * Must call finished on pool to release the thread in the pool.
	 * <p>
	 */
	public void waitAccess() {
		int wait = TaskMasterProperties.getLogicalWaitInterval();
		int maxIntervals = TaskMasterProperties.getLogicalMaxWaitIntervals();
		waitAccess(wait, maxIntervals);
	}

	/**
	 * Wait till a thread is available.
	 * <p>
	 * Must call finished on pool to release the thread in the pool.
	 * <p>
	 *
	 * @param waitInterval the wait interval in milliseconds
	 * @param maxWaitIntervals the max number of waits
	 */
	public void waitAccess(final int waitInterval, final int maxWaitIntervals) {
		checkPoolStatus();
		int counts = 0;
		while (!getAccess(waitInterval, TimeUnit.MILLISECONDS)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Waiting for thread pool [" + getName() + "]. Max: "
						+ getMax() + " Current: " + getCurrent() + ".");
			}
			if (counts++ > maxWaitIntervals) {
				throw new IllegalStateException("Maximum attempts to get a thread in pool ["
						+ getName() + "] exceeded.");
			}
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Thread is available in pool " + getName() + ".");
		}
	}

	/**
	 * Check if the pool is OK for processing.
	 */
	protected void checkPoolStatus() {
		if (isShutdown()) {
			throw new IllegalStateException("Thread pool is shutdown for processing.");
		}
	}

	/**
	 * @param shutdown true if pool shutdown for processing
	 */
	protected synchronized void setShutdown(final boolean shutdown) {
		this.shutdown = shutdown;
	}

}
