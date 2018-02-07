package com.github.bordertech.taskmanager.impl;

import com.github.bordertech.config.Config;
import com.github.bordertech.didums.Didums;
import com.github.bordertech.taskmanager.TaskFuture;
import com.github.bordertech.taskmanager.TaskManagerException;
import com.github.bordertech.taskmanager.cache.CacheHelper;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.cache.Cache;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;

/**
 * Uses a cache to wrap the future allowing the cache key reference to be serializable.
 *
 * @param <T> the future get type
 * @author Jonathan Austin
 * @since 1.0.0
 */
public class TaskFutureWrapper<T> implements TaskFuture<T> {

	private static final CacheHelper CACHE_HELPER = Didums.getService(CacheHelper.class);

	private static final Cache<String, Future> CACHE;

	private final String id = UUID.randomUUID().toString();

	static {
		// Get default duration
		Long duration = Config.getInstance().getLong("bordertech.taskmanager.future.cache.duration", Long.valueOf("300"));

		// Setup cache config
		MutableConfiguration<String, Future> config = new MutableConfiguration<>();
		config.setTypes(String.class, Future.class);
		config.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, duration)));
		// No need to serialize the result (Future is not serializable)
		config.setStoreByValue(false);

		// Create Cache
		CACHE = CACHE_HELPER.getOrCreateCache("bordertech-tm-future-task", String.class, Future.class, config);
	}

	/**
	 * @param future the backing future
	 */
	public TaskFutureWrapper(final Future<T> future) {
		setFuture(future);
	}

	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		return getFuture().cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return getFuture().isCancelled();
	}

	@Override
	public boolean isDone() {
		return getFuture().isDone();
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		return getFuture().get();
	}

	@Override
	public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return getFuture().get(timeout, unit);
	}

	/**
	 * @param future the future to save in the cache
	 */
	protected final void setFuture(final Future<T> future) {
		CACHE.put(id, future);
	}

	/**
	 * @return the future object from the cache
	 */
	protected final Future<T> getFuture() {
		Future<T> future = CACHE.get(id);
		if (future == null) {
			// Future has expired or been removed from the cache
			future = new TaskFutureResult(new TaskManagerException("Future has been removed from the cache"));
			CACHE.put(id, future);
		}
		return future;
	}

}
