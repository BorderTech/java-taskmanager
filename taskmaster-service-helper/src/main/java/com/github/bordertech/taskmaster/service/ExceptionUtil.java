package com.github.bordertech.taskmaster.service;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

/**
 * Exception Helper Util.
 */
public final class ExceptionUtil {

	/**
	 * Private constructor.
	 */
	private ExceptionUtil() {
		// Do nothing
	}

	/**
	 * Check the exception is Serializable (sometimes they arent).
	 *
	 * @param excp the original exception to check
	 * @return the original exception if Serializable or a ServiceException with the original exception message
	 */
	public static Exception getSerializableException(final Exception excp) {
		if (isSerializableException(excp)) {
			return excp;
		}
		// Wrap exception as a Service Exception
		return new ServiceException(excp.getMessage() + " Original exception [" + excp.getClass().getName() + "] not Serializable.");
	}

	/**
	 *
	 * Determine if the Exception is Serializable (as sometimes they arent).
	 *
	 * @param excp the Exception to check is serializable
	 * @return true if Exception is serializable
	 */
	public static boolean isSerializableException(final Exception excp) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(bos);
			os.writeObject(excp);
			bos.toByteArray();
			// Serializable
			return true;
		} catch (Exception ex) {
			// Not Serializable
			return false;
		}
	}

}
