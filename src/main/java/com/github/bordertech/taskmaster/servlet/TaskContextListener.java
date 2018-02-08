package com.github.bordertech.taskmaster.servlet;

import com.github.bordertech.didums.Didums;
import com.github.bordertech.taskmaster.TaskMaster;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * ContextListener to shutdown the task master (release threads).
 * <p>
 * To include the context listener, declare the listener in the application's web.xml:-
 * </p>
 * <pre>
 * &lt;web-app ...&gt;
 *   &lt;listener&gt;
 *     &lt;listener-class&gt;
 *           com.github.bordertech.taskmaster.servlet.TaskContextListener
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

	private static final TaskMaster TASK_MASTER = Didums.getService(TaskMaster.class);

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
		// Shutdown the task master
		TASK_MASTER.shutdown();
	}
}
