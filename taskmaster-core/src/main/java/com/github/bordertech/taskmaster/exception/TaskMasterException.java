package com.github.bordertech.taskmaster.exception;

/**
 * Task Master Exception has occurred.
 *
 * @author Jonathan Austin
 * @since 1.0.0
 */
public class TaskMasterException extends RuntimeException {

	/**
	 * Creates a TaskMasterException with the specified message.
	 *
	 * @param msg the message.
	 */
	public TaskMasterException(final String msg) {
		super(msg);
	}

	/**
	 * Creates a TaskMasterException with the specified message and cause.
	 *
	 * @param msg the message.
	 * @param throwable the cause of the exception.
	 */
	public TaskMasterException(final String msg, final Throwable throwable) {
		super(msg, throwable);
	}

	/**
	 * Creates a TaskMasterException with the specified cause.
	 *
	 * @param throwable the cause of the exception.
	 */
	public TaskMasterException(final Throwable throwable) {
		super(throwable);
	}
}
