package com.locima.xml2csv.cmdline;

import static org.junit.Assert.*;

import org.junit.Test;

public class ErrorMessagesTests {

	@Test
	public void testSimple() {
		Program p = new Program();

		Throwable child2 = new Throwable("Child2");
		Throwable child1 = new Throwable("Child1", child2);
		Throwable parent = new Throwable("Top level", child1);

		assertEquals(String.format("Top level%nBecause: Child1%nBecause: Child2%n"), p.getAllCauses(parent));
		assertEquals(String.format("Child1%nBecause: Child2%n"), p.getAllCauses(child1));
		assertEquals(String.format("Child2%n"), p.getAllCauses(child2));
	}

	@Test
	public void testNull() {
		assertNull(new Program().getAllCauses(null));
	}

	@Test
	public void testRepeatString() {
		Program p = new Program();
		Throwable child2Rep = new Throwable("Child2");
		Throwable child2 = new Throwable("Child2", child2Rep);
		Throwable child1 = new Throwable("Child1", child2);
		Throwable parent = new Throwable("Top level", child1);

		assertEquals(String.format("Top level%nBecause: Child1%nBecause: Child2%n"), p.getAllCauses(parent));

		Throwable parentRep = new Throwable("Top level", parent);
		assertEquals(String.format("Top level%nBecause: Child1%nBecause: Child2%n"), p.getAllCauses(parentRep));
	}
	
	@Test
	public void testLoop() {
		Program p =new Program();
		
		Throwable loop1 = new Throwable("Loop1");
		Throwable loop2 = new Throwable("Loop2",loop1);
		loop1.initCause(loop2);
		assertEquals(String.format("Loop1%nBecause: Loop2%n"), p.getAllCauses(loop1));
		assertEquals(String.format("Loop2%nBecause: Loop1%n"), p.getAllCauses(loop2));
	}

}
