package com.github.bordertech.taskmaster.service.impl;

import java.io.Serializable;

/**
 * Used to hold the service result with the ASync processing cache.
 *
 * @param <M> the meta data type
 * @param <T> the result type
 */
public final class ProcessingMutableResult<M extends Serializable, T extends Serializable> implements Serializable {

	private final M metaData;
	private T result;
	private Exception exception;

	/**
	 * @param metaData the meta data
	 */
	public ProcessingMutableResult(final M metaData) {
		this.metaData = metaData;
	}

	/**
	 * @return the meta data
	 */
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
	 * @param result the result
	 */
	public void setResult(final T result) {
		this.result = result;
		this.exception = null;
	}

	/**
	 * @return the exception or null if has result
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * @param exception the exception when calling the service
	 */
	public void setException(final Exception exception) {
		this.exception = exception;
		this.result = null;
	}

	/**
	 * @return true if holding an exception
	 */
	public boolean isException() {
		return exception != null;
	}

	/**
	 * @return true if holding a result
	 */
	public boolean isResult() {
		return !isException();
	}

}
