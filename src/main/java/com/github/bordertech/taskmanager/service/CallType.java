package com.github.bordertech.taskmanager.service;

/**
 * Clarify the call type to take.
 *
 * @author Jonathan Austin
 * @since 1.0.0
 */
public enum CallType {

	CALL_SYNC(false, false),
	CALL_ASYNC(false, true),
	REFRESH_SYNC(true, false),
	REFRESH_ASYNC(true, true);

	final boolean async;
	final boolean refresh;

	CallType(boolean refresh, final boolean async) {
		this.refresh = refresh;
		this.async = async;
	}

	public boolean isRefresh() {
		return refresh;
	}

	public boolean isAsync() {
		return async;
	}

}
