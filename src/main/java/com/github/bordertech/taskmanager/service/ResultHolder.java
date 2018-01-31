package com.github.bordertech.taskmanager.service;

import java.io.Serializable;

/**
 * Result holder for service calls.
 * <p>
 * The result can be an exception or the result.
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

	public ResultHolder(final M metaData, final T result) {
		this.metaData = metaData;
		this.result = result;
		this.exception = null;
	}

	public ResultHolder(final M metaData, final Exception exception) {
		this.metaData = metaData;
		this.result = null;
		this.exception = exception;
	}

	public M getMetaData() {
		return metaData;
	}

	public T getResult() {
		return result;
	}

	public Exception getException() {
		return exception;
	}

	public boolean isException() {
		return exception != null;
	}

	public boolean isResult() {
		return exception == null;
	}

}
