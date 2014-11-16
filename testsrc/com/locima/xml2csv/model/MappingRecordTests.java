package com.locima.xml2csv.model;

import static com.locima.xml2csv.TestHelpers.toStringList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.locima.xml2csv.XmlUtil;

public class MappingRecordTests {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void testEmptyRecord() throws Exception {
		Mapping mapping = new Mapping("Field", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."));
		MappingRecord record = new MappingRecord(mapping, new ArrayList<String>());

		assertNull(record.peek());
		assertTrue(!record.hasNext());
		assertNull(record.peek());
	}

	@Test
	public void testNextWithEmptyRecord() throws Exception {
		this.exception.expect(NoSuchElementException.class);
		Mapping mapping = new Mapping("Field", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."));
		MappingRecord record = new MappingRecord(mapping, new ArrayList<String>());
		record.next();
	}

	@Test
	public void testNextWithExpiredIterator() throws Exception {
		Mapping mapping = new Mapping("Field", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."));
		MappingRecord record = new MappingRecord(mapping, toStringList("A", "B"));
		record.next();
		record.next();
		this.exception.expect(NoSuchElementException.class);
		record.next();
	}

	@Test
	public void testSimpleRecord() throws Exception {
		Mapping mapping = new Mapping("Field", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."));
		MappingRecord record = new MappingRecord(mapping, toStringList("A", "B", "C", "D"));

		assertEquals("A", record.peek());
		assertEquals("A", record.peek());
		assertTrue(record.hasNext());
		assertEquals("A", record.peek());
		assertEquals("A", record.peek());
		assertEquals("A", record.next());
		assertTrue(record.hasNext());
		assertEquals("B", record.peek());
		assertEquals("B", record.peek());
		assertEquals("B", record.next());
		assertEquals("C", record.peek());
		assertEquals("C", record.peek());
		assertTrue(record.hasNext());
		assertEquals("C", record.peek());
		assertEquals("C", record.peek());
		assertEquals("C", record.next());
		assertTrue(record.hasNext());
		assertEquals("D", record.peek());
		assertEquals("D", record.peek());
		assertEquals("D", record.next());
		assertEquals(null, record.peek());
		assertEquals(null, record.peek());
		assertTrue(!record.hasNext());
		assertEquals(null, record.peek());
		assertEquals(null, record.peek());

		record.reset();

		assertEquals("A", record.peek());
		assertEquals("A", record.peek());
		assertTrue(record.hasNext());
		assertEquals("A", record.peek());
		assertEquals("A", record.peek());
		assertEquals("A", record.next());
		assertTrue(record.hasNext());
		assertEquals("B", record.peek());
		assertEquals("B", record.peek());
		assertEquals("B", record.next());
		assertEquals("C", record.peek());
		assertEquals("C", record.peek());
		assertTrue(record.hasNext());
		assertEquals("C", record.peek());
		assertEquals("C", record.peek());
		assertEquals("C", record.next());
		assertTrue(record.hasNext());
		assertEquals("D", record.peek());
		assertEquals("D", record.peek());
		assertEquals("D", record.next());
		assertEquals(null, record.peek());
		assertEquals(null, record.peek());
		assertTrue(!record.hasNext());
		assertEquals(null, record.peek());
		assertEquals(null, record.peek());
	}
}
