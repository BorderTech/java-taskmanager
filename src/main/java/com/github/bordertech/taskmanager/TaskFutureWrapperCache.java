package com.github.bordertech.taskmanager;

import java.util.concurrent.Future;

/**
 * Provide a caching mechanism for the wrapper task future.
 * <p>
 * Allows a serializable key to be used to reference the Future which is not Serializable. This cache allows projects to
 * hold onto the TaskFuture reference (which is serializable) while internally it holds a reference to the future held
 * in a cache. The cache implementation must be able to cater for non-serializable objects (is the Future).
 * </p>
 * <p>
 * The duration of this cache must exceed the longest allowed service call (ie service timeout).
 * </p>
 *
 * @param <T> the future get result type
 * @author Jonathan Austin
 * @since 1.0.0
 */
public interface TaskFutureWrapperCache<T> {

	/**
	 * @param key the key to the future
	 * @return the future or null
	 */
	Future<T> getFuture(final String key);

	/**
	 * @param key the key of the future to remove
	 */
	void removeFuture(final String key);

	/**
	 *
	 * @param key the key to the future
	 * @param future the future to cache
	 */
	void putFuture(final String key, final Future<T> future);

}
