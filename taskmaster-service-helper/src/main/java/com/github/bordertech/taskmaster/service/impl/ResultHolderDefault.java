package com.github.bordertech.taskmaster.service.impl;

import com.github.bordertech.taskmaster.service.ResultHolder;
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
public class ResultHolderDefault<M extends Serializable, T extends Serializable> implements ResultHolder<M, T> {

	private final M metaData;
	private final T result;
	private final Exception exception;

	/**
	 * Hold a successful result.
	 *
	 * @param result the service result
	 */
	public ResultHolderDefault(final T result) {
		this(null, result);
	}

	/**
	 * Hold a successful metadata and result.
	 *
	 * @param metaData the service meta data
	 * @param result the service result
	 */
	public ResultHolderDefault(final M metaData, final T result) {
		this.metaData = metaData;
		this.result = result;
		this.exception = null;
	}

	/**
	 * Hold an exception.
	 *
	 * @param exception the exception that occurred
	 */
	public ResultHolderDefault(final Exception exception) {
		this(null, exception);
	}

	/**
	 * Hold an exception.
	 *
	 * @param metaData the service meta data
	 * @param exception the exception that occurred
	 */
	public ResultHolderDefault(final M metaData, final Exception exception) {
		// Exception must be provided
		if (exception == null) {
			throw new IllegalArgumentException("An exception must be provided.");
		}
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
