package com.locima.xml2csv.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringUtilTests {

	@Test
	public void testEscape() {
		assertNull(StringUtil.escapeForCsv(null));
		assertNull(StringUtil.escapeForCsv(new Object() {
			@Override
			public String toString() {
				return null;
			}
		}));
		assertEquals("", StringUtil.escapeForCsv(""));
		assertEquals("a", StringUtil.escapeForCsv("a"));
		assertEquals("abc", StringUtil.escapeForCsv("abc"));
		assertEquals("\"\"\"\"", StringUtil.escapeForCsv("\""));
		assertEquals("\"\"\"a\"", StringUtil.escapeForCsv("\"a"));
		assertEquals("\"a\"\"b\"", StringUtil.escapeForCsv("a\"b"));
		assertEquals("\",\"", StringUtil.escapeForCsv(","));
		assertEquals("\",;,;\"", StringUtil.escapeForCsv(",;,;"));
		assertEquals("\";\"", StringUtil.escapeForCsv(";"));
		assertEquals("\"\n\"", StringUtil.escapeForCsv("\n"));
		assertEquals("\"\n\r\"", StringUtil.escapeForCsv("\n\r"));
		assertEquals("\"\n\"\"\n\"", StringUtil.escapeForCsv("\n\"\n"));
	}

}
