package com.locima.xml2csv.model;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

public class ExtractionContextTests {

	@Test
	public void singleDepthTest() {
		ExtractionContext ctx = new ExtractionContext();
		assertEquals("", ctx.toContextString());
		ctx.addContext(0);
		assertEquals("0", ctx.toContextString());
		ctx.increment();
		assertEquals("1", ctx.toContextString());
		ctx.increment();
		assertEquals("2", ctx.toContextString());
		ctx.removeContext();
		assertEquals("", ctx.toContextString());
	}

	@Test
	public void multiDepthTest() {
		ExtractionContext ctx = new ExtractionContext();
		ctx.addContext(0);
		ctx.addContext(1);
		ctx.addContext(5);
		assertEquals("0_1_5", ctx.toContextString());
		ctx.increment();
		assertEquals("0_1_6", ctx.toContextString());
		ctx.removeContext();
		assertEquals("0_1", ctx.toContextString());
		ctx.increment();
		assertEquals("0_2", ctx.toContextString());
		ctx.removeContext();
		assertEquals("0", ctx.toContextString());
		ctx.addContext(1000);
		assertEquals("0_1000", ctx.toContextString());
		ctx.increment();
		assertEquals("0_1001", ctx.toContextString());
		ctx.removeContext();
		assertEquals("0", ctx.toContextString());
		ctx.removeContext();
		assertEquals("", ctx.toContextString());
		
		
		
		
	}

}
