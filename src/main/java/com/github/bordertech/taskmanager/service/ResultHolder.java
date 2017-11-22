package com.github.bordertech.taskmanager.service;

import java.io.Serializable;

/**
 * Service result holder to use in the Future.
 * <p>
 * The result can be an exception or the result. Does not need to serializable as it is held on a Future that is not
 * Serializable.
 * </p>
 *
 * @author Jonathan Austin
 * @param <M> the meta type
 * @param <T> the result type
 * @since 1.0.0
 */
public class ResultHolder<M, T> implements Serializable {

	private final M metaData;
	private T result;
	private Exception exception;

	public ResultHolder(final M metaData) {
		this.metaData = metaData;
	}

	public ResultHolder(final M metaData, final T result) {
		this(metaData);
		this.result = result;
	}

	public ResultHolder(final M metaData, final Exception exception) {
		this(metaData);
		this.exception = exception;
	}

	public M getMetaData() {
		return metaData;
	}

	/**
	 * @return the polling result
	 */
	public T getResult() {
		return result;
	}

	/**
	 * @param result the polling result
	 */
	public void setResult(final T result) {
		this.result = result;
		this.exception = null;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(final Exception exception) {
		this.exception = exception;
		this.result = null;
	}

	public boolean isException() {
		return exception != null;
	}

	public boolean isResult() {
		return !isException();
	}

	public boolean isNotFoundException() {
		return exception instanceof NotFoundException;
	}

	public boolean isServiceException() {
		return exception instanceof ServiceException;
	}

}
