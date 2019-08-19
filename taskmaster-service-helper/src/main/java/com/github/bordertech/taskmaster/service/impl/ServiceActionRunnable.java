package com.github.bordertech.taskmaster.service.impl;

import com.github.bordertech.taskmaster.service.ServiceAction;
import com.github.bordertech.taskmaster.service.util.ExceptionUtil;
import java.io.Serializable;

/**
 * Runnable that processes the service action.
 *
 * @param <S> the criteria type
 * @param <T> the service response
 */
public class ServiceActionRunnable<S extends Serializable, T extends Serializable> implements Runnable {

	private final S criteria;
	private final ServiceAction<S, T> action;
	private final ResultHolderMutable<S, T> result;

	/**
	 * @param criteria the service criteria
	 * @param action the service action
	 * @param result the result holder
	 */
	public ServiceActionRunnable(final S criteria, final ServiceAction<S, T> action, final ResultHolderMutable<S, T> result) {
		this.criteria = criteria;
		this.action = action;
		this.result = result;
	}

	@Override
	public void run() {
		try {
			T resp = action.service(criteria);
			result.setResult(resp);
		} catch (Exception e) {
			// Check exception is serializable to be held in the cache (sometimes they arent)
			result.setException(ExceptionUtil.getSerializableException(e));
		}
	}

}
