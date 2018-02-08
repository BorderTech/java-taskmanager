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
public class ResultHolder<M, T> implements Serializable {

	private final M metaData;
	private final T result;
	private final Exception exception;

	/**
	 * Hold a successful result.
	 *
	 * @param metaData the service meta data
	 * @param result the service result
	 */
	public ResultHolder(final M metaData, final T result) {
		this.metaData = metaData;
		this.result = result;
		this.exception = null;
	}

	/**
	 * Hold an exception.
	 *
	 * @param metaData the service meta data
	 * @param exception the exception that occurred
	 */
	public ResultHolder(final M metaData, final Exception exception) {
		this.metaData = metaData;
		this.result = null;
		this.exception = exception;
	}

	/**
	 * @return the meta data for the service call
	 */
	public M getMetaData() {
		return metaData;
	}

	/**
	 * @return the successful result, can be null
	 */
	public T getResult() {
		return result;
	}

	/**
	 * @return the exception that occurred or null if result was successful
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 *
	 * @return true if the result is an exception
	 */
	public boolean isException() {
		return exception != null;
	}

	/**
	 *
	 * @return true if holding a successful result
	 */
	public boolean isResult() {
		return exception == null;
	}

}
