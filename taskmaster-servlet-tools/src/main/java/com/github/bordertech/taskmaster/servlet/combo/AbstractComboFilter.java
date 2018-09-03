package com.github.bordertech.taskmaster.servlet.combo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Combo Filter that allows a group of Filters to be defined in one class.
 * <p>
 * This allows multiple filters to be easily annotated in one class instead of multiple entries in a web.xml.
 * </p>
 */
public abstract class AbstractComboFilter implements Filter {

	private final List<Filter> filters;

	/**
	 * @param filters the group of filters to combine into a single filter
	 */
	public AbstractComboFilter(final Filter... filters) {
		this(Arrays.asList(filters));
	}

	/**
	 * @param filters the group of filters to combine into a single filter
	 */
	public AbstractComboFilter(final List<Filter> filters) {
		this.filters = Collections.unmodifiableList(filters);
	}

	/**
	 * @return the list of filters to be combined
	 */
	public final List<Filter> getFilters() {
		return filters;
	}

	@Override
	public void init(final FilterConfig config) throws ServletException {
		// Config each filter
		for (Filter filter : getFilters()) {
			filter.init(config);
		}
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
		ComboFilterChain combo = new ComboFilterChain(chain, getFilters());
		combo.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// Destroy in reverse order
		for (int i = getFilters().size(); i-- > 0;) {
			Filter filter = getFilters().get(i);
			filter.destroy();
		}
	}

}
