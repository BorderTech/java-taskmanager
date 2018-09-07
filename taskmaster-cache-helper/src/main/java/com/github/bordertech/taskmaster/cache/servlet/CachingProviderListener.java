package com.github.bordertech.taskmaster.cache.servlet;

import com.github.bordertech.taskmaster.cache.CachingHelper;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Cleanup any cache resources when servlet context destroyed.
 */
public class CachingProviderListener implements ServletContextListener {

	@Override
	public void contextInitialized(final ServletContextEvent sce) {
		// Do nothing
	}

	@Override
	public void contextDestroyed(final ServletContextEvent sce) {
		CachingHelper.getProvider().closeCacheManager();
	}

}
