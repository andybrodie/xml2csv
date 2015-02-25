package com.locima.xml2csv.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TupleTest {

	@Test
	public void testTupleEquals() {
		Tuple<String, String> t1 = new Tuple<String, String>("S1", "S2");
		assertTrue(t1.equals(t1));
		assertFalse(t1.equals(null));

		Tuple<String, String> t2 = new Tuple<String, String>("S1", "S2");
		assertTrue(t1.equals(t2));
		assertTrue(t2.equals(t1));

		Tuple<String, String> t3 = new Tuple<String, String>("S2", "S1");
		assertFalse(t1.equals(t3));
		assertFalse(t3.equals(t1));

		assertEquals(new Tuple<Integer, Integer>(null, null), new Tuple<String, String>(null, null));
	}

	@Test
	public void testTupleHashcode() {
		Tuple<String, String> t1 = new Tuple<String, String>("S1", "S2");
		Tuple<String, String> t2 = new Tuple<String, String>("S1", "S2");
		assertEquals(t1.hashCode(), t2.hashCode());

		Tuple<String, String> t3 = new Tuple<String, String>(null, null);
		t3.hashCode();
	}

	@Test
	public void testTupleToString() {

	}

}
