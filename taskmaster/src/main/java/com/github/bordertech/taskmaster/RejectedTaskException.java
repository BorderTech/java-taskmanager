package com.github.bordertech.taskmaster;

/**
 * Task Master could not execute this task.
 *
 * @author Jonathan Austin
 * @since 1.0.0
 */
public class RejectedTaskException extends Exception {

	/**
	 * Creates a RejectedException with the specified message.
	 *
	 * @param msg the message.
	 */
	public RejectedTaskException(final String msg) {
		super(msg);
	}

	/**
	 * Creates a RejectedException with the specified message and cause.
	 *
	 * @param msg the message.
	 * @param throwable the cause of the exception.
	 */
	public RejectedTaskException(final String msg, final Throwable throwable) {
		super(msg, throwable);
	}

	/**
	 * Creates a RejectedException with the specified cause.
	 *
	 * @param throwable the cause of the exception.
	 */
	public RejectedTaskException(final Throwable throwable) {
		super(throwable);
	}
}
