package com.github.bordertech.taskmaster.service.exception;

/**
 * Service Helper had an exception processing the Async task.
 *
 * @author Jonathan Austin
 * @since 1.0.0
 */
public class AsyncServiceException extends Exception {

	/**
	 * Creates a AsyncServiceException with the specified message.
	 *
	 * @param msg the message.
	 */
	public AsyncServiceException(final String msg) {
		super(msg);
	}

	/**
	 * Creates a AsyncServiceException with the specified message and cause.
	 *
	 * @param msg the message.
	 * @param throwable the cause of the exception.
	 */
	public AsyncServiceException(final String msg, final Throwable throwable) {
		super(msg, throwable);
	}

	/**
	 * Creates a RejectedExcepAsyncServiceExceptionion with the specified cause.
	 *
	 * @param throwable the cause of the exception.
	 */
	public AsyncServiceException(final Throwable throwable) {
		super(throwable);
	}
}
