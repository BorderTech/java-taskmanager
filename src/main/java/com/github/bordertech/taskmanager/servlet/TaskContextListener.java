package com.github.bordertech.taskmanager.servlet;

import com.github.bordertech.didums.Didums;
import com.github.bordertech.taskmanager.TaskManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * ContextListener to shutdown the task manager (release threads).
 * <p>
 * To include the context listener, declare the listener in the application's web.xml:-
 * </p>
 * <pre>
 * &lt;web-app ...&gt;
 *   &lt;listener&gt;
 *     &lt;listener-class&gt;>
 *           com.github.bordertech.taskmanager.servlet.TaskContextListener
 *     &lt;/listener-class&gt;
 *   &lt;/listener&gt;
 * &lt;/web-app&gt;
 * </pre>
 * <p>
 * For Servlet container 3.x, you can annotate the listener with @WebListener, no need to declare in web.xml.
 * </p>
 *
 * @author Jonathan Austin
 * @since 1.0.0
 */
public class TaskContextListener implements ServletContextListener {

	private static final TaskManager TASK_MANAGER = Didums.getService(TaskManager.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {
		// Shutdown the task manager
		TASK_MANAGER.shutdown();
	}
}
