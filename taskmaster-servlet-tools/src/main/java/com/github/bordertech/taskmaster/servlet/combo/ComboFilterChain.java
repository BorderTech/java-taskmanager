package com.github.bordertech.taskmaster.servlet.combo;

import java.io.IOException;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Process the combo filters and then the original chain.
 */
public class ComboFilterChain implements FilterChain {

	private final FilterChain chain;
	private final List<Filter> combo;
	private int position = 0;

	/**
	 * @param chain the original servlet filter chain
	 * @param comboFilters the list of combined filters to process
	 */
	public ComboFilterChain(final FilterChain chain, final List<Filter> comboFilters) {
		this.chain = chain;
		this.combo = comboFilters;
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response) throws IOException, ServletException {
		if (position == combo.size()) {
			chain.doFilter(request, response);
		} else {
			Filter nextFilter = combo.get(position++);
			nextFilter.doFilter(request, response, this);
		}
	}

}
