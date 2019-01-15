package com.github.bordertech.taskmaster.logical;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link LogicalThreadPool}.
 */
public class LogicalThreadPoolTest {

	@Test
	public void testConstructor1() {
		LogicalThreadPool pool = new LogicalThreadPool();
		Assert.assertEquals("Incorrect pool name for default constructor", "default", pool.getName());
		Assert.assertEquals("Incorrect max threads for default constructor", 0, pool.getMax());
	}

	@Test
	public void testConstructor2() {
		LogicalThreadPool pool = new LogicalThreadPool("foo", 5);
		Assert.assertEquals("Incorrect name for constructor", "foo", pool.getName());
		Assert.assertEquals("Incorrect default max threads for constructor", 5, pool.getMax());
	}

	@Test
	public void testDefaultShutdown() {
		Assert.assertFalse("Pool should not be shutdown.", new LogicalThreadPool().isShutdown());
	}

	@Test
	public void testNameDefault() {
		Assert.assertEquals("Pool name should be default for default constructor", "default", new LogicalThreadPool().getName());
	}

	@Test
	public void testNameNull() {
		Assert.assertEquals("Pool name should be default for null in constructor", "default", new LogicalThreadPool(null, 0).getName());
	}

	@Test
	public void testNameEmpty() {
		Assert.assertEquals("Pool name should be default for empty in constructor", "default", new LogicalThreadPool("", 0).getName());
	}

	@Test
	public void testNameValue() {
		Assert.assertEquals("Pool name should be default for value in constructor", "foo", new LogicalThreadPool("foo", 0).getName());
	}

	@Test
	public void testMaxDefault() {
		Assert.assertEquals("Pool max should default to zero (no limit).", 0, new LogicalThreadPool().getMax());
	}

	@Test
	public void testMaxNegative() {
		Assert.assertEquals("Pool max should be zero for negative constructor value.", 0, new LogicalThreadPool(null, -1).getMax());
	}

	@Test
	public void testMaxZero() {
		Assert.assertEquals("Pool max should be zero for zero constructor value.", 0, new LogicalThreadPool(null, 0).getMax());
	}

	@Test
	public void testMaxPositive() {
		Assert.assertEquals("Pool max should be positive value.", 10, new LogicalThreadPool(null, 10).getMax());
	}

}
