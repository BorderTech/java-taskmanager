package com.github.bordertech.taskmaster.impl;

import com.github.bordertech.config.Config;
import com.github.bordertech.taskmaster.RejectedTaskException;
import com.github.bordertech.taskmaster.TaskFuture;
import com.github.bordertech.taskmaster.TaskMaster;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;

/**
 * Handle running tasks via {@link ExecutorService}.
 *
 * @author Jonathan Austin
 * @since 1.0.0
 */
@Singleton
public class TaskMasterExecutorService implements TaskMaster {

	private static final Map<String, ExecutorService> THREAD_POOLS = new HashMap<>();

	private static final String TP_PARAM_PREFIX = "bordertech.taskmaster.pool.";

	private static final String DEFAULT_POOL = Config.getInstance().getString(TP_PARAM_PREFIX + "default", "default");

	private static final int DEFAULT_MAX_THREADS = 20;
	private static final int DEFAULT_QUEUE_LENGTH = 0;

	static {
		// Load thread pools
		String[] pools = Config.getInstance().getStringArray(TP_PARAM_PREFIX + "names");
		for (String pool : pools) {
			THREAD_POOLS.put(pool, buildPool(pool));
		}
		// Check if default pool needs to be created
		if (!THREAD_POOLS.containsKey(DEFAULT_POOL)) {
			THREAD_POOLS.put(DEFAULT_POOL, buildPool(DEFAULT_POOL));
		}
	}

	/**
	 * @param pool the pool name to create
	 * @return the executor service
	 */
	private static ExecutorService buildPool(final String pool) {
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

	@Override
	public void shutdown() {
		for (ExecutorService exec : THREAD_POOLS.values()) {
			exec.shutdown();
		}
	}

	@Override
	public <T> TaskFuture<T> submit(final Runnable task, final T result) throws RejectedTaskException {
		return submit(task, result, DEFAULT_POOL);
	}

	@Override
	public <T> TaskFuture<T> submit(final Runnable task, final T result, final String pool) throws RejectedTaskException {
		String name = pool == null ? DEFAULT_POOL : pool;
		ExecutorService exec = THREAD_POOLS.get(name);
		if (exec == null) {
			throw new IllegalStateException("Thread pool [" + name + "] has not been defined.");
		}
		try {
			Future<T> future = exec.submit(task, result);
			return new TaskFutureWrapper<>(future);
		} catch (RejectedExecutionException e) {
			throw new RejectedTaskException("Unable to start task in pool [" + pool + "].", e);
		}
	}

}
