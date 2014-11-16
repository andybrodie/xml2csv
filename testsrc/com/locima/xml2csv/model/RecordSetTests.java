package com.locima.xml2csv.model;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.locima.xml2csv.StringUtil;
import com.locima.xml2csv.TestHelpers;
import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.XmlUtil;

import static com.locima.xml2csv.TestHelpers.toStringList;

public class RecordSetTests {

	private final static String[] ESA = new String[0];

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void testSingleInlineRecord() throws Exception {
		RecordSet rs = new RecordSet();
		String[] expectedValues = new String[] { "A", "B", "C", "D" };
		Mapping mapping = new Mapping("Field", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."));
		MappingRecord record = new MappingRecord(mapping, toStringList(expectedValues));
		rs.addResults(record);

		Iterator<List<String>> iterator = rs.iterator();
		assertTrue(iterator.hasNext());
		List<String> values = iterator.next();
		assertArrayEquals(expectedValues, values.toArray(ESA));

		assertTrue(!iterator.hasNext());
		exception.expect(NoSuchElementException.class);
		iterator.next();
	}

	@Test
	public void testMultipleResultsForSameMappingRejected() throws Exception {
		RecordSet rs = new RecordSet();
		Mapping mapping = new Mapping("Field", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."));
		String[][] records = new String[][] { new String[] { "A", "B", "C", "D" }, new String[] { "E", "F", "G", "H" } };
		exception.expect(IllegalStateException.class);
		for (String[] record : records) {
			MappingRecord mappingRecord = new MappingRecord(mapping, toStringList(record));
			rs.addResults(mappingRecord);
		}
	}

	@Test
	public void testDuplicateFieldNamesRejected() throws XMLException {
		Mapping mapping = new Mapping("Field", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."));
		RecordSet rs = new RecordSet();
		MappingRecord mappingRecord = new MappingRecord(mapping, toStringList("A"));
		rs.addResults(mappingRecord);
		exception.expect(IllegalStateException.class);
		rs.addResults(mappingRecord);
	}

	@Test
	public void testMultiInlineRecords() throws Exception {
		RecordSet rs = new RecordSet();
		String[][] records = new String[][] { new String[] { "A", "B", "C", "D" }, new String[] { "E", "F", "G", "H" } };
		int count = 0;
		for (String[] record : records) {
			Mapping mapping = new Mapping("Field" + count, NameFormat.NO_COUNTS, 0, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."));
			count++;
			MappingRecord mappingRecord = new MappingRecord(mapping, toStringList(record));
			rs.addResults(mappingRecord);
		}

		List<String> values;
		Iterator<List<String>> iterator = rs.iterator();
		values = iterator.next();
		assertArrayEquals(StringUtil.concatenate(records[0], records[1]), values.toArray(ESA));
		assertTrue(!iterator.hasNext());
	}

	@Test
	public void testMultiRecordAndInline() throws Exception {
		RecordSet rs = new RecordSet();
		Mapping mapping1 = new Mapping("MRField", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."));
		String[] values1 = new String[] { "A", "B", "C", "D" };
		rs.addResults(mapping1, toStringList(values1));
		Mapping mapping2 = new Mapping("ILField", NameFormat.NO_COUNTS, 1, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."));
		String[] values2 = new String[] { "E", "F", "G", "H" };
		rs.addResults(mapping2, toStringList(values2));

		List<String> values;

		Iterator<List<String>> iterator = rs.iterator();
		values = iterator.next();
		assertArrayEquals(new String[] { "A", "E", "F", "G", "H" }, values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(new String[] { "B", "E", "F", "G", "H" }, values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(new String[] { "C", "E", "F", "G", "H" }, values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(new String[] { "D", "E", "F", "G", "H" }, values.toArray(ESA));

		assertTrue(!iterator.hasNext());
	}

	@Test
	public void testInlineAndMultiRecord() throws Exception {
		RecordSet rs = new RecordSet();
		Mapping ilMapping = new Mapping("ILField", NameFormat.NO_COUNTS, 1, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."));
		String[] ilValues = new String[] { "E", "F", "G", "H" };
		rs.addResults(ilMapping, toStringList(ilValues));
		Mapping mrMapping = new Mapping("MRField", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."));
		String[] mrValues = new String[] { "A", "B", "C", "D" };
		rs.addResults(mrMapping, toStringList(mrValues));

		List<String> values;

		Iterator<List<String>> iterator = rs.iterator();
		values = iterator.next();
		assertArrayEquals(new String[] { "E", "F", "G", "H", "A" }, values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(new String[] { "E", "F", "G", "H", "B" }, values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(new String[] { "E", "F", "G", "H", "C" }, values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(new String[] { "E", "F", "G", "H", "D" }, values.toArray(ESA));

		assertTrue(!iterator.hasNext());
	}

	@Test
	public void testMIMSameGroupRecord() throws Exception {
		RecordSet rs = new RecordSet();
		Mapping mrMapping = new Mapping("MRField", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."));
		String[] mrValues = new String[] { "A", "B" };
		rs.addResults(mrMapping, toStringList(mrValues));
		Mapping ilMapping = new Mapping("ILField", NameFormat.NO_COUNTS, 1, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."));
		String[] ilValues = new String[] { "C", "D" };
		rs.addResults(ilMapping, toStringList(ilValues));
		Mapping mrMapping2 = new Mapping("MRField", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."));
		String[] mrValues2 = new String[] { "E", "F" };
		rs.addResults(mrMapping2, toStringList(mrValues2));

		List<String> values;
		Iterator<List<String>> iterator = rs.iterator();
		values = iterator.next();
		assertArrayEquals(new String[] { "A", "C", "D", "E" }, values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(new String[] { "B", "C", "D", "F" }, values.toArray(ESA));

		assertTrue(!iterator.hasNext());
	}

	@Test
	public void testIMISameGroupRecord() throws Exception {
		RecordSet rs = new RecordSet();
		Mapping ilMapping = new Mapping("ILField", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."));
		String[] ilValues = new String[] { "A", "B" };
		rs.addResults(ilMapping, toStringList(ilValues));
		Mapping mrMapping = new Mapping("MRField1", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."));
		String[] mrValues = new String[] { "C", "D" };
		rs.addResults(mrMapping, toStringList(mrValues));
		Mapping ilMapping2 = new Mapping("ILField2", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."));
		String[] ilValues2 = new String[] { "E", "F" };
		rs.addResults(ilMapping2, toStringList(ilValues2));

		List<String> values;
		Iterator<List<String>> iterator = rs.iterator();
		values = iterator.next();
		assertArrayEquals(new String[] { "A", "B", "C", "E", "F" }, values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(new String[] { "A", "B", "D", "E", "F" }, values.toArray(ESA));

		assertTrue(!iterator.hasNext());
	}

	@Test
	public void testMultiMRGroupsAsc() throws Exception {
		RecordSet rs = new RecordSet();
		Mapping mrMapping = new Mapping("MRField", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."));
		String[] mrValues = new String[] { "A", "B" };
		rs.addResults(mrMapping, toStringList(mrValues));
		Mapping mrMapping2 = new Mapping("MRField", NameFormat.NO_COUNTS, 1, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."));
		String[] mrValues2 = new String[] { "C", "D" };
		rs.addResults(mrMapping2, toStringList(mrValues2));

		List<String> values;

		Iterator<List<String>> iterator = rs.iterator();

		values = iterator.next();
		assertArrayEquals(new String[] { "A", "C" }, values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(new String[] { "B", "C" }, values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(new String[] { "A", "D" }, values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(new String[] { "B", "D" }, values.toArray(ESA));
	}

	@Test
	public void testMultiMRGroupsDesc() throws Exception {
		RecordSet rs = new RecordSet();
		Mapping mrMapping = new Mapping("MRField", NameFormat.NO_COUNTS, 1, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."));
		String[] mrValues = new String[] { "A", "B" };
		rs.addResults(mrMapping, toStringList(mrValues));
		Mapping mrMapping2 = new Mapping("MRField", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."));
		String[] mrValues2 = new String[] { "C", "D" };
		rs.addResults(mrMapping2, toStringList(mrValues2));

		List<String> values;

		Iterator<List<String>> iterator = rs.iterator();

		values = iterator.next();
		assertArrayEquals(new String[] { "A", "C" }, values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(new String[] { "A", "D" }, values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(new String[] { "B", "C" }, values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(new String[] { "B", "D" }, values.toArray(ESA));

		assertTrue(!iterator.hasNext());
	}

	@Test
	public void testSingleMultiRecord() throws Exception {
		RecordSet rs = new RecordSet();
		String[] expectedValues = new String[] { "A", "B", "C", "D" };
		Mapping mapping = new Mapping("Field", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."));
		MappingRecord record = new MappingRecord(mapping, toStringList(expectedValues));
		rs.addResults(record);

		List<String> values;

		Iterator<List<String>> iterator = rs.iterator();
		assertTrue(iterator.hasNext());
		values = iterator.next();
		assertArrayEquals(new String[] { "A" }, values.toArray(ESA));
		assertTrue(iterator.hasNext());
		values = iterator.next();
		assertArrayEquals(new String[] { "B" }, values.toArray(ESA));
		assertTrue(iterator.hasNext());
		values = iterator.next();
		assertArrayEquals(new String[] { "C" }, values.toArray(ESA));
		assertTrue(iterator.hasNext());
		values = iterator.next();
		assertArrayEquals(new String[] { "D" }, values.toArray(ESA));
		assertTrue(!iterator.hasNext());

		exception.expect(NoSuchElementException.class);
		iterator.next();
	}

	@Test
	public void testEmpty() throws Exception {
		RecordSet rs = new RecordSet();
		Iterator<List<String>> iterator = rs.iterator();
		assertTrue(!iterator.hasNext());
		exception.expect(NoSuchElementException.class);
		iterator.next();
	}
	
	
	@Test
	public void testThreeMRGroups() throws Exception {
		RecordSet rs = new RecordSet();
		Mapping mapping;
		MappingRecord record;

		mapping = new Mapping("Field1", NameFormat.NO_COUNTS, 1, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."));
		record = new MappingRecord(mapping, toStringList(new String[] { "A", "B" }));
		rs.addResults(record);
		mapping = new Mapping("Field2", NameFormat.NO_COUNTS, 2, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."));
		record = new MappingRecord(mapping, toStringList(new String[] { "C", "D" }));
		rs.addResults(record);
		mapping = new Mapping("Field3", NameFormat.NO_COUNTS, 3, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."));
		record = new MappingRecord(mapping, toStringList(new String[] { "E", "F" }));
		rs.addResults(record);


		RecordSetIterator iterator = (RecordSetIterator) rs.iterator();
		assertEquals(8, iterator.getTotalNumberOfRecords());
		assertTrue(iterator.hasNext());
		assertArrayEquals(new String[] { "A", "C", "E" }, iterator.next().toArray(ESA));
		assertArrayEquals(new String[] { "B", "C", "E" }, iterator.next().toArray(ESA));
		assertArrayEquals(new String[] { "A", "D", "E" }, iterator.next().toArray(ESA));
		assertArrayEquals(new String[] { "B", "D", "E" }, iterator.next().toArray(ESA));
		assertArrayEquals(new String[] { "A", "C", "F" }, iterator.next().toArray(ESA));
		assertArrayEquals(new String[] { "B", "C", "F" }, iterator.next().toArray(ESA));
		assertArrayEquals(new String[] { "A", "D", "F" }, iterator.next().toArray(ESA));
		assertArrayEquals(new String[] { "B", "D", "F" }, iterator.next().toArray(ESA));
	}

}
