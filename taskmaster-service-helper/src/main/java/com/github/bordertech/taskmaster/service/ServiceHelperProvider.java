package com.github.bordertech.taskmaster.service;

import com.github.bordertech.taskmaster.service.exception.AsyncServiceException;
import com.github.bordertech.taskmaster.service.exception.RejectedServiceException;
import java.io.Serializable;
import javax.cache.Cache;
import javax.cache.expiry.Duration;

/**
 * Service invocation helper provider.
 */
public interface ServiceHelperProvider {

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
	 * @throws RejectedServiceException if the task cannot be scheduled for execution
	 */
	<S extends Serializable, T extends Serializable> ResultHolder<S, T> handleAsyncServiceCall(final Cache<String, ResultHolder> cache,
			final String cacheKey, final S criteria, final ServiceAction<S, T> action) throws RejectedServiceException;

	/**
	 * Handle an async service call with a designated thread pool.
	 *
	 * @param cache the result holder cache
	 * @param cacheKey the key for the result holder
	 * @param criteria the criteria
	 * @param action the service action
	 * @param pool service thread pool
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result or null if still processing
	 * @throws RejectedServiceException if the task cannot be scheduled for execution
	 */
	<S extends Serializable, T extends Serializable> ResultHolder<S, T> handleAsyncServiceCall(final Cache<String, ResultHolder> cache,
			final String cacheKey, final S criteria, final ServiceAction<S, T> action, final String pool) throws RejectedServiceException;

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
	<S extends Serializable, T extends Serializable> ResultHolder<S, T> handleCachedServiceCall(final Cache<String, ResultHolder> cache,
			final String cacheKey, final S criteria, final ServiceAction<S, T> action);

	/**
	 * Handle a service call.
	 *
	 * @param criteria the criteria
	 * @param action the service action
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result
	 */
	<S extends Serializable, T extends Serializable> ResultHolder<S, T> handleServiceCall(final S criteria, final ServiceAction<S, T> action);

	/**
	 *
	 * Handle a cached service call with a particular call type.
	 *
	 * @param cache the result holder cache
	 * @param cacheKey the key for the result holder
	 * @param criteria the criteria
	 * @param action the service action
	 * @param callType the call type
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result or null if still processing an async call
	 * @throws RejectedServiceException if the task cannot be scheduled for execution
	 */
	<S extends Serializable, T extends Serializable> ResultHolder<S, T> handleServiceCallType(final Cache<String, ResultHolder> cache,
			final String cacheKey, final S criteria, final ServiceAction<S, T> action, final CallType callType) throws RejectedServiceException;

	/**
	 *
	 * Handle a cached service call with a particular call type and predefined pool.
	 *
	 * @param cache the result holder cache
	 * @param cacheKey the key for the result holder
	 * @param criteria the criteria
	 * @param action the service action
	 * @param callType the call type
	 * @param pool service thread pool
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result or null if still processing an async call
	 * @throws RejectedServiceException if the task cannot be scheduled for execution
	 */
	<S extends Serializable, T extends Serializable> ResultHolder<S, T> handleServiceCallType(final Cache<String, ResultHolder> cache,
			final String cacheKey, final S criteria, final ServiceAction<S, T> action, final CallType callType, final String pool) throws RejectedServiceException;

	/**
	 * This is the method that checks if the processing task has completed.
	 * <p>
	 * It the task is complete it will return the result and put the result in the result cache.
	 * </p>
	 *
	 * @param cache the result holder cache
	 * @param cacheKey the key for the result holder
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result or null if still processing
	 * @throws AsyncServiceException an exception while processing an Async task
	 */
	<S extends Serializable, T extends Serializable> ResultHolder<S, T> checkASyncResult(final Cache<String, ResultHolder> cache,
			final String cacheKey) throws AsyncServiceException;

}
