package com.github.bordertech.taskmaster.service;

/**
 * Invoke a service interface.
 *
 * @param <S> the criteria type
 * @param <T> the response type
 * @author Jonathan Austin
 * @since 1.0.0
 */
public interface ServiceAction<S, T> {

	/**
	 * Invoke service call.
	 *
	 * @param criteria the service criteria
	 * @return the service response
	 */
	T service(final S criteria);

}
