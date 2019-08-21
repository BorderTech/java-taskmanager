package com.github.bordertech.taskmaster.service;

import com.github.bordertech.didums.Didums;
import com.github.bordertech.taskmaster.TaskFuture;
import com.github.bordertech.taskmaster.service.exception.RejectedServiceException;
import com.github.bordertech.taskmaster.service.exception.ServiceException;
import com.github.bordertech.taskmaster.service.impl.ServiceHelperProviderDefault;
import java.io.Serializable;
import javax.cache.Cache;

/**
 * Service invocation helper.
 */
public final class ServiceHelper {

	private static final ServiceHelperProvider PROVIDER = Didums.getService(ServiceHelperProvider.class, ServiceHelperProviderDefault.class);

	/**
	 * Private constructor.
	 */
	private ServiceHelper() {
	}

	/**
	 * @return the ServiceHelperProvider
	 */
	public static ServiceHelperProvider getProvider() {
		return PROVIDER;
	}

	/**
	 * Submit an async service call.
	 *
	 * @param criteria the criteria
	 * @param action the service action
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the task future to check the async process status
	 * @throws ServiceException exception processing the service call
	 * @throws RejectedServiceException if the task cannot be scheduled for execution
	 */
	public static <S extends Serializable, T extends Serializable> TaskFuture<ResultHolder<S, T>> submitAsync(
			final S criteria, final ServiceAction<S, T> action)
			throws ServiceException, RejectedServiceException {
		return PROVIDER.submitAsync(criteria, action, null);
	}

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
	public static <S extends Serializable, T extends Serializable> TaskFuture<ResultHolder<S, T>> submitAsync(
			final S criteria, final ServiceAction<S, T> action, final String pool)
			throws ServiceException, RejectedServiceException {
		return PROVIDER.submitAsync(criteria, action, pool);
	}

	/**
	 * Submit an async service call that uses a cache.
	 * <p>
	 * Defaults to not caching exceptions.
	 * </p>
	 *
	 * @param criteria the criteria
	 * @param action the service action
	 * @param cache the result holder cache
	 * @param cacheKey the key for the result holder
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result or null if still processing
	 * @throws ServiceException exception processing the service call
	 * @throws RejectedServiceException if the task cannot be scheduled for execution
	 */
	public static <S extends Serializable, T extends Serializable> TaskFuture<ResultHolder<S, T>> submitAsync(
			final S criteria, final ServiceAction<S, T> action, final Cache<String, ResultHolder> cache, final String cacheKey)
			throws ServiceException, RejectedServiceException {
		return PROVIDER.submitAsync(criteria, action, null, cache, cacheKey, false);
	}

	/**
	 * Submit an async service call that uses a cache and thread pool.
	 * <p>
	 * Defaults to not caching exceptions.
	 * </p>
	 *
	 * @param criteria the criteria
	 * @param action the service action
	 * @param cache the result holder cache
	 * @param cacheKey the key for the result holder
	 * @param pool the thread pool, or null for default pool
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result or null if still processing
	 * @throws ServiceException exception processing the service call
	 * @throws RejectedServiceException if the task cannot be scheduled for execution
	 */
	public static <S extends Serializable, T extends Serializable> TaskFuture<ResultHolder<S, T>> submitAsync(
			final S criteria, final ServiceAction<S, T> action, final Cache<String, ResultHolder> cache, final String cacheKey, final String pool)
			throws ServiceException, RejectedServiceException {
		return PROVIDER.submitAsync(criteria, action, pool, cache, cacheKey, false);
	}

	/**
	 * Submit an async service call that uses a cache and option of caching exceptions.
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
	 * @throws RejectedServiceException if the task cannot be scheduled for execution
	 */
	public static <S extends Serializable, T extends Serializable> TaskFuture<ResultHolder<S, T>> submitAsync(
			final S criteria, final ServiceAction<S, T> action, final Cache<String, ResultHolder> cache, final String cacheKey,
			final boolean cacheException)
			throws ServiceException, RejectedServiceException {
		return PROVIDER.submitAsync(criteria, action, null, cache, cacheKey, cacheException);
	}

	/**
	 * Submit an async service call that uses a cache and option of thread pool and caching exceptions.
	 *
	 * @param criteria the criteria
	 * @param action the service action
	 * @param cache the result holder cache
	 * @param cacheKey the key for the result holder
	 * @param pool the thread pool, or null for default pool
	 * @param cacheException true if cache exception
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result or null if still processing
	 * @throws ServiceException exception processing the service call
	 * @throws RejectedServiceException if the task cannot be scheduled for execution
	 */
	public static <S extends Serializable, T extends Serializable> TaskFuture<ResultHolder<S, T>> submitAsync(
			final S criteria, final ServiceAction<S, T> action, final Cache<String, ResultHolder> cache, final String cacheKey,
			final String pool, final boolean cacheException)
			throws ServiceException, RejectedServiceException {
		return PROVIDER.submitAsync(criteria, action, pool, cache, cacheKey, cacheException);
	}

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
	public static <S extends Serializable, T extends Serializable> ResultHolder<S, T> invokeSync(final S criteria, final ServiceAction<S, T> action)
			throws ServiceException {
		return PROVIDER.invokeSync(criteria, action);
	}

	/**
	 * Invoke a sync service call that is cached.
	 * <p>
	 * Defaults to not caching exceptions.
	 * </p>
	 *
	 * @param criteria the criteria
	 * @param action the service action
	 * @param cache the result holder cache
	 * @param cacheKey the key for the result holder
	 * @param <S> the criteria type
	 * @param <T> the service response
	 * @return the result or null if still processing
	 * @throws ServiceException exception processing the service call
	 */
	public static <S extends Serializable, T extends Serializable> ResultHolder<S, T> invokeSync(
			final S criteria, final ServiceAction<S, T> action, final Cache<String, ResultHolder> cache, final String cacheKey)
			throws ServiceException {
		return PROVIDER.invokeSync(criteria, action, cache, cacheKey, false);
	}

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
	public static <S extends Serializable, T extends Serializable> ResultHolder<S, T> invokeSync(
			final S criteria, final ServiceAction<S, T> action, final Cache<String, ResultHolder> cache, final String cacheKey,
			final boolean cacheException)
			throws ServiceException {
		return PROVIDER.invokeSync(criteria, action, cache, cacheKey, cacheException);
	}

}
