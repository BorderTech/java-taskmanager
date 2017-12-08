package com.github.bordertech.taskmanager.service;

/**
 * Async exception has occurred. Maybe missing in cache or cancelled.
 *
 * @author Jonathan Austin
 * @since 1.0.0
 */
public class AsyncException extends Exception {

	/**
	 * @param message the exception message
	 */
	public AsyncException(final String message) {
		super(message);
	}

	/**
	 * @param message the exception message
	 * @param original the original exception
	 */
	public AsyncException(final String message, final Throwable original) {
		super(message, original);
	}

}
