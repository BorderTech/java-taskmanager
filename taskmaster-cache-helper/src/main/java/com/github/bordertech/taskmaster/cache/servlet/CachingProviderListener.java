package com.github.bordertech.taskmaster.cache.servlet;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Cleanup any task master threads.
 */
public class CachingProviderListener implements ServletContextListener {

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        // Do nothing
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        CachingProvider provider = Caching.getCachingProvider();
        if (provider != null && !provider.getCacheManager().isClosed()) {
            provider.getCacheManager().close();
        }
    }

}
