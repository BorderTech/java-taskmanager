package com.github.bordertech.taskmaster.service.exception;

/**
 * Service Helper could not execute this task.
 *
 * @author Jonathan Austin
 * @since 1.0.0
 */
public class RejectedServiceException extends Exception {

	/**
	 * Creates a RejectedException with the specified message.
	 *
	 * @param msg the message.
	 */
	public RejectedServiceException(final String msg) {
		super(msg);
	}

	/**
	 * Creates a RejectedException with the specified message and cause.
	 *
	 * @param msg the message.
	 * @param throwable the cause of the exception.
	 */
	public RejectedServiceException(final String msg, final Throwable throwable) {
		super(msg, throwable);
	}

	/**
	 * Creates a RejectedException with the specified cause.
	 *
	 * @param throwable the cause of the exception.
	 */
	public RejectedServiceException(final Throwable throwable) {
		super(throwable);
	}
}
