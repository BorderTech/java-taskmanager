package com.github.bordertech.taskmanager.service;

/**
 * Not found exception.
 *
 * @author Jonathan Austin
 * @since 1.0.0
 */
public class NotFoundException extends RuntimeException {

	/**
	 * @param message the exception message
	 */
	public NotFoundException(final String message) {
		super(message);
	}

	/**
	 * @param message the exception message
	 * @param e the original exception
	 */
	public NotFoundException(final String message, final Throwable e) {
		super(message, e);
	}

}
