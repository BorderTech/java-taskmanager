package com.github.bordertech.taskmaster.servlet.combo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Combo ServletListener that allows a group of Listeners to be defined in one class.
 * <p>
 * This allows multiple ServletListeners to be easily annotated in one class instead of multiple entries in a web.xml.
 * </p>
 */
public abstract class AbstractComboServletListener implements ServletContextListener {

	private static final Log LOGGER = LogFactory.getLog(AbstractComboServletListener.class);

	private final List<ServletContextListener> listeners;

	/**
	 * @param listeners the group of listeners to combine into a single listener
	 */
	public AbstractComboServletListener(final ServletContextListener... listeners) {
		this(Arrays.asList(listeners));
	}

	/**
	 * @param listeners the group of listeners to combine into a single listener
	 */
	public AbstractComboServletListener(final List<ServletContextListener> listeners) {
		this.listeners = Collections.unmodifiableList(listeners);
	}

	/**
	 *
	 * @return the group of listeners to combine
	 */
	public final List<ServletContextListener> getListeners() {
		return listeners;
	}

	@Override
	public void contextInitialized(final ServletContextEvent sce) {
		for (ServletContextListener listener : getListeners()) {
			try {
				listener.contextInitialized(sce);
			} catch (Exception e) {
				LOGGER.error("Error calling initialized servlet context listener [" + listener.getClass().getName() + "]. "
						+ e.getMessage(), e);
			}
		}
	}

	@Override
	public void contextDestroyed(final ServletContextEvent sce) {
		for (ServletContextListener listener : getListeners()) {
			try {
				listener.contextDestroyed(sce);
			} catch (Exception e) {
				LOGGER.error("Error calling destoryed servlet context listener [" + listener.getClass().getName() + "]. "
						+ e.getMessage(), e);
			}
		}
	}

}
