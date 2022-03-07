package com.github.bordertech.taskmaster.service.impl;

import com.github.bordertech.taskmaster.service.ResultHolder;
import com.github.bordertech.taskmaster.service.util.ExceptionUtil;
import java.io.Serializable;

/**
 * Used to hold the service result with the ASync processing.
 *
 * @param <M> the meta data type
 * @param <T> the result type
 */
public final class ResultHolderMutable<M extends Serializable, T extends Serializable> implements ResultHolder<M, T> {

	private M metaData;
	private T result;
	private Exception exception;

	/**
	 * Default constructor.
	 */
	public ResultHolderMutable() {
		// Do nothing
	}

	/**
	 * @param metaData the meta data
	 */
	public ResultHolderMutable(final M metaData) {
		this.metaData = metaData;
	}

	/**
	 * @return the meta data
	 */
	@Override
	public M getMetaData() {
		return metaData;
	}

	/**
	 * @param metaData the meta data
	 */
	public void setMetaData(final M metaData) {
		this.metaData = metaData;
	}

	/**
	 * @return the service result
	 */
	@Override
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
	@Override
	public Exception getException() {
		return exception;
	}

	/**
	 * @param exception the exception when calling the service
	 */
	public void setException(final Exception exception) {
		// Check exception is serializable (sometimes they arent)
		this.exception = ExceptionUtil.getSerializableException(exception);
		this.result = null;
	}

	/**
	 * @return true if holding an exception
	 */
	@Override
	public boolean isException() {
		return exception != null;
	}

	/**
	 * @return true if holding a result
	 */
	@Override
	public boolean isResult() {
		return !isException();
	}

}
