package com.github.bordertech.taskmaster.service;

/**
 * The service call type to invoke.
 *
 * @author Jonathan Austin
 * @since 1.0.0
 */
public enum CallType {

	/**
	 * Sync call and use cache.
	 */
	CALL_SYNC(false, false),
	/**
	 * Async call and use cache.
	 */
	CALL_ASYNC(false, true),
	/**
	 * Sync call and refresh cache.
	 */
	REFRESH_SYNC(true, false),
	/**
	 * Async call and refresh cache.
	 */
	REFRESH_ASYNC(true, true);

	private final boolean async;
	private final boolean refresh;

	/**
	 *
	 * @param refresh true if refresh cache
	 * @param async true if invoke an async call
	 */
	CallType(final boolean refresh, final boolean async) {
		this.refresh = refresh;
		this.async = async;
	}

	/**
	 *
	 * @return true if refresh cache
	 */
	public boolean isRefresh() {
		return refresh;
	}

	/**
	 *
	 * @return true if invoke an async call
	 */
	public boolean isAsync() {
		return async;
	}

}
