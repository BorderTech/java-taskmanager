package com.github.bordertech.taskmanager.service;

import javax.cache.Cache;
import javax.cache.expiry.Duration;

/**
 * Service invoker helper.
 *
 * @author jonathan
 */
public interface ServiceHelper {

	/**
	 * This is the method checks if the processing task has completed.
	 * <p>
	 * If the future is done, then it will transition the result from the processing cache into the result holder cache.
	 * </p>
	 *
	 * @param cache the result holder cache
	 * @param cacheKey the key for the result holder
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result or null if still processing
	 */
	<S, T> ResultHolder<S, T> checkASyncResult(final Cache<String, ResultHolder> cache, final String cacheKey);

	/**
	 * Provide a default result holder cache with the default duration.
	 *
	 * @return the default result holder cache instance
	 */
	Cache<String, ResultHolder> getDefaultResultHolderCache();

	/**
	 * Provide a result holder cache with an assigned cache name with default duration.
	 *
	 * @param name the cache name
	 * @return the cache instance
	 */
	Cache<String, ResultHolder> getResultHolderCache(final String name);

	/**
	 * Provide a result holder cache with an assigned cache name and duration.
	 *
	 * @param name the cache name
	 * @param duration the time to live for cached items
	 * @return the cache instance
	 */
	Cache<String, ResultHolder> getResultHolderCache(final String name, final Duration duration);

	/**
	 * Handle an async service call.
	 *
	 * @param cache the result holder cache
	 * @param cacheKey the key for the result holder
	 * @param criteria the criteria
	 * @param action the service action
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result or null if still processing
	 */
	<S, T> ResultHolder<S, T> handleAsyncServiceCall(final Cache<String, ResultHolder> cache, final String cacheKey,
			final S criteria, final ServiceAction<S, T> action);

	/**
	 *
	 * Handle a cached service call.
	 *
	 * @param cache the result holder cache
	 * @param cacheKey the key for the result holder
	 * @param criteria the criteria
	 * @param action the service action
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result
	 */
	<S, T> ResultHolder<S, T> handleCachedServiceCall(final Cache<String, ResultHolder> cache, final String cacheKey,
			final S criteria, final ServiceAction<S, T> action);

	/**
	 * Handle a service call.
	 *
	 * @param criteria the criteria
	 * @param action the service action
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result
	 */
	<S, T> ResultHolder<S, T> handleServiceCall(final S criteria, final ServiceAction<S, T> action);

	/**
	 *
	 * Handle a cached service call.
	 *
	 * @param cache the result holder cache
	 * @param cacheKey the key for the result holder
	 * @param criteria the criteria
	 * @param action the service action
	 * @param callType the call type
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result or null if still processing an async call
	 */
	<S, T> ResultHolder<S, T> handleServiceCallType(final Cache<String, ResultHolder> cache, final String cacheKey,
			final S criteria, final ServiceAction<S, T> action, final CallType callType);

}
