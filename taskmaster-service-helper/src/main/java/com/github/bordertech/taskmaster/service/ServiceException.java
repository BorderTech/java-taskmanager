package com.github.bordertech.taskmaster.service;

/**
 * Service exception that can be thrown while processing a service request.
 *
 * @author Jonathan Austin
 * @since 1.0.0
 */
public class ServiceException extends RuntimeException {

	/**
	 * @param message the exception message
	 */
	public ServiceException(final String message) {
		super(message);
	}

	/**
	 * @param message the exception message
	 * @param original the original exception
	 */
	public ServiceException(final String message, final Throwable original) {
		super(message, original);
	}

}
