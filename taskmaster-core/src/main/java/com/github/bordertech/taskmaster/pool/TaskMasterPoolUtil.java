package com.github.bordertech.taskmaster.pool;

import com.github.bordertech.config.Config;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * TaskMaster ExecutorService thread pool utility.
 */
public final class TaskMasterPoolUtil {

	private static final Map<String, ExecutorService> THREAD_POOLS = new HashMap<>();
	private static final String TP_PARAM_PREFIX = "bordertech.taskmaster.pool.";
	private static final int DEFAULT_MAX_THREADS = 20;
	private static final int DEFAULT_QUEUE_LENGTH = 0;

	/**
	 * Default thread pool name.
	 */
	public static final String DEFAULT_POOL = Config.getInstance().getString(TP_PARAM_PREFIX + "default", "default");

	static {
		// Load thread pools
		String[] pools = Config.getInstance().getStringArray(TaskMasterPoolUtil.TP_PARAM_PREFIX + "names");
		for (String pool : pools) {
			THREAD_POOLS.put(pool, TaskMasterPoolUtil.buildPool(pool));
		}
		// Check if default pool needs to be created
		if (!THREAD_POOLS.containsKey(TaskMasterPoolUtil.DEFAULT_POOL)) {
			THREAD_POOLS.put(TaskMasterPoolUtil.DEFAULT_POOL, TaskMasterPoolUtil.buildPool(TaskMasterPoolUtil.DEFAULT_POOL));
		}
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private TaskMasterPoolUtil() {
		// Do nothing
	}

	/**
	 * Build a thread pool with the given name.
	 *
	 * @param pool the pool name to create
	 * @return the executor service
	 */
	public static ExecutorService buildPool(final String pool) {
		// TODO Logging and performance parameters
		// http://www.nurkiewicz.com/2014/11/executorservice-10-tips-and-tricks.html

		// Get the pool type - defaults to cached
		String type = Config.getInstance().getString(TP_PARAM_PREFIX + pool + ".type", "cached");
		switch (type.toLowerCase()) {
			case "single":
				return Executors.newSingleThreadExecutor();
			case "fixed":
				// Number of fixed threads
				int max = Config.getInstance().getInt(TP_PARAM_PREFIX + pool + ".max", DEFAULT_MAX_THREADS);
				if (max < 1) {
					max = DEFAULT_MAX_THREADS;
				}
				// Length of pending queue
				int queue = Config.getInstance().getInt(TP_PARAM_PREFIX + pool + ".queue", DEFAULT_QUEUE_LENGTH);
				// Create executable with the appropriate queue type
				BlockingQueue<Runnable> blkQueue;
				if (queue < 0) {
					// Unlimited
					blkQueue = new LinkedBlockingQueue<>();
				} else if (queue == 0) {
					// No queue
					blkQueue = new SynchronousQueue<>();
				} else {
					// Fixed queue length
					blkQueue = new ArrayBlockingQueue<>(queue);
				}
				return new ThreadPoolExecutor(max, max, 0L, TimeUnit.MILLISECONDS, blkQueue);
			default:
				// Default - Unlimited Threads and No Queue
				return Executors.newCachedThreadPool();
		}
	}

	/**
	 * Shutdown the thread pools.
	 */
	public static void shutdown() {
		for (ExecutorService exec : THREAD_POOLS.values()) {
			// TODO This logic needs to be put into TaskMaster
			exec.shutdownNow();
		}
	}

	/**
	 * Retrieve the thread pool for the given name.
	 *
	 * @param poolName the thread pool name
	 * @return the thread pool for the given name.
	 */
	public static synchronized ExecutorService getPool(final String poolName) {
		String name = poolName == null ? TaskMasterPoolUtil.DEFAULT_POOL : poolName;
		ExecutorService pool = THREAD_POOLS.get(name);
		if (pool == null) {
			throw new IllegalStateException("Pool [" + name + "] has not been defined.");
		}
		// TODO This logic needs to be put into TaskMaster
		// Check if terminated (reactivate)
		if (pool.isTerminated()) {
			// TODO LOG Warning and maybe control this via flag
			// Check not interrupted for some reason (maybe server shutting down)
			if (Thread.currentThread().isInterrupted()) {
				throw new IllegalStateException("Pool [" + name + "] has terminated and thread is interrupted.");
			}
			pool = TaskMasterPoolUtil.buildPool(name);
			THREAD_POOLS.put(name, pool);
		}
		return pool;
	}

}
