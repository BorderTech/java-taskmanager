package com.github.bordertech.taskmaster.service;

import java.io.Serializable;

/**
 * Result holder for service calls.
 * <p>
 * The result can be an exception or the service response.
 * </p>
 *
 * @param <M> the meta type
 * @param <T> the result type
 * @author Jonathan Austin
 * @since 1.0.0
 */
public interface ResultHolder<M extends Serializable, T extends Serializable> extends Serializable {

	/**
	 * @return the meta data for the service call
	 */
	M getMetaData();

	/**
	 * @return the successful result, can be null
	 */
	T getResult();

	/**
	 * @return the exception that occurred or null if result was successful
	 */
	Exception getException();

	/**
	 *
	 * @return true if the result is an exception
	 */
	boolean isException();

	/**
	 *
	 * @return true if holding a successful result
	 */
	boolean isResult();

}
