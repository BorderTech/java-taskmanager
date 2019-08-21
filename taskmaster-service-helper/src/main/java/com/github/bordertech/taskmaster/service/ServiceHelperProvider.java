package com.github.bordertech.taskmaster.service;

import com.github.bordertech.taskmaster.TaskFuture;
import com.github.bordertech.taskmaster.service.exception.RejectedServiceException;
import com.github.bordertech.taskmaster.service.exception.ServiceException;
import java.io.Serializable;
import javax.cache.Cache;

/**
 * Service invocation helper provider.
 */
public interface ServiceHelperProvider {

	/**
	 * Submit an async service call with a thread pool.
	 *
	 * @param criteria the criteria
	 * @param action the service action
	 * @param pool the thread pool or null for default pool
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the task future to check the async process status
	 * @throws ServiceException exception processing the service call
	 * @throws RejectedServiceException if the task cannot be scheduled for execution
	 */
	<S extends Serializable, T extends Serializable> TaskFuture<ResultHolder<S, T>> submitAsync(S criteria, ServiceAction<S, T> action, String pool)
			throws ServiceException, RejectedServiceException;

	/**
	 * Submit an async service call that uses a cache.
	 *
	 * @param criteria the criteria
	 * @param action the service action
	 * @param pool the thread pool or null for default pool
	 * @param cache the result holder cache
	 * @param cacheKey the key for the result holder
	 * @param cacheException true if cache exception
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result or null if still processing
	 * @throws ServiceException exception processing the service call
	 * @throws RejectedServiceException if the task cannot be scheduled for execution
	 */
	<S extends Serializable, T extends Serializable> TaskFuture<ResultHolder<S, T>> submitAsync(S criteria, ServiceAction<S, T> action, String pool,
			Cache<String, ResultHolder> cache, String cacheKey, boolean cacheException) throws ServiceException, RejectedServiceException;

	/**
	 * Invoke a sync service call.
	 *
	 * @param criteria the criteria
	 * @param action the service action
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the task future to check the async process status
	 * @throws ServiceException exception processing the service call
	 */
	<S extends Serializable, T extends Serializable> ResultHolder<S, T> invokeSync(S criteria, ServiceAction<S, T> action) throws ServiceException;

	/**
	 * Invoke a sync service call that is cached with option of caching the exception.
	 *
	 * @param criteria the criteria
	 * @param action the service action
	 * @param cache the result holder cache
	 * @param cacheKey the key for the result holder
	 * @param cacheException true if cache exception
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result or null if still processing
	 * @throws ServiceException exception processing the service call
	 */
	<S extends Serializable, T extends Serializable> ResultHolder<S, T> invokeSync(S criteria, ServiceAction<S, T> action,
			Cache<String, ResultHolder> cache, String cacheKey, boolean cacheException) throws ServiceException;

}
