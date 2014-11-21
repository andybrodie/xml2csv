package com.locima.xml2csv.model;

import static com.locima.xml2csv.TestHelpers.toStringList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.locima.xml2csv.StringUtil;
import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.XmlUtil;

public class RecordSetTests {

	private final static String[] ESA = new String[0];

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void testDuplicateFieldNames() throws XMLException {
		Mapping mapping = new Mapping("Field", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."), 0, 0);
		RecordSet rs = new RecordSet();
		rs.add(new MappingRecord(mapping, toStringList("A")));
		rs.add(new MappingRecord(mapping, toStringList("B")));
		rs.add(new MappingRecord(mapping, toStringList("C")));
		Iterator<List<String>> iterator = rs.iterator();
		assertArrayEquals(new String[] { "A", "B", "C" }, iterator.next().toArray(ESA));
	}

	@Test
	public void testEmpty() throws Exception {
		RecordSet rs = new RecordSet();
		Iterator<List<String>> iterator = rs.iterator();
		assertTrue(!iterator.hasNext());
		this.exception.expect(NoSuchElementException.class);
		iterator.next();
	}

	@Test
	public void testIMISameGroupRecord() throws Exception {
		RecordSet rs = new RecordSet();
		Mapping ilMapping = new Mapping("ILField", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] ilValues = new String[] { "A", "B" };
		rs.addResults(ilMapping, toStringList(ilValues));
		Mapping mrMapping =
						new Mapping("MRField1", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] mrValues = new String[] { "C", "D" };
		rs.addResults(mrMapping, toStringList(mrValues));
		Mapping ilMapping2 = new Mapping("ILField2", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."), 0, 0);
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
	public void testInlineAndMultiRecord() throws Exception {
		RecordSet rs = new RecordSet();
		Mapping ilMapping = new Mapping("ILField", NameFormat.NO_COUNTS, 1, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] ilValues = new String[] { "E", "F", "G", "H" };
		rs.addResults(ilMapping, toStringList(ilValues));
		Mapping mrMapping =
						new Mapping("MRField", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."), 0, 0);
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
		Mapping mrMapping =
						new Mapping("MRField", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] mrValues = new String[] { "A", "B" };
		rs.addResults(mrMapping, toStringList(mrValues));
		Mapping ilMapping = new Mapping("ILField", NameFormat.NO_COUNTS, 1, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] ilValues = new String[] { "C", "D" };
		rs.addResults(ilMapping, toStringList(ilValues));
		Mapping mrMapping2 =
						new Mapping("MRField", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."), 0, 0);
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
	public void testMultiInlineRecords() throws Exception {
		RecordSet rs = new RecordSet();
		String[][] records = new String[][] { new String[] { "A", "B", "C", "D" }, new String[] { "E", "F", "G", "H" } };
		int count = 0;
		for (String[] record : records) {
			Mapping mapping =
							new Mapping("Field" + count, NameFormat.NO_COUNTS, 0, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."), 0,
											0);
			count++;
			MappingRecord mappingRecord = new MappingRecord(mapping, toStringList(record));
			rs.add(mappingRecord);
		}

		List<String> values;
		Iterator<List<String>> iterator = rs.iterator();
		values = iterator.next();
		assertArrayEquals(StringUtil.concatenate(records[0], records[1]), values.toArray(ESA));
		assertTrue(!iterator.hasNext());
	}

	@Test
	public void testMultiMRGroupsAsc() throws Exception {
		RecordSet rs = new RecordSet();
		Mapping mrMapping =
						new Mapping("MRField", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] mrValues = new String[] { "A", "B" };
		rs.addResults(mrMapping, toStringList(mrValues));
		Mapping mrMapping2 =
						new Mapping("MRField", NameFormat.NO_COUNTS, 1, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."), 0, 0);
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
		Mapping mrMapping =
						new Mapping("MRField", NameFormat.NO_COUNTS, 1, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] mrValues = new String[] { "A", "B" };
		rs.addResults(mrMapping, toStringList(mrValues));
		Mapping mrMapping2 =
						new Mapping("MRField", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."), 0, 0);
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
	public void testMultipleResultsForSameMapping() throws Exception {
		RecordSet rs = new RecordSet();
		Mapping mapping = new Mapping("Field", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."), 0, 0);
		rs.addResults(mapping, new MappingRecord(mapping, toStringList("A", "B")));
		rs.addResults(mapping, new MappingRecord(mapping, toStringList("C")));
		rs.addResults(mapping, new MappingRecord(mapping, toStringList("D", "E")));

		Iterator<List<String>> iterator = rs.iterator();
		assertArrayEquals(new String[] { "A" }, iterator.next().toArray(ESA));
		assertArrayEquals(new String[] { "B" }, iterator.next().toArray(ESA));
		assertArrayEquals(new String[] { "C" }, iterator.next().toArray(ESA));
		assertArrayEquals(new String[] { "D" }, iterator.next().toArray(ESA));
		assertArrayEquals(new String[] { "E" }, iterator.next().toArray(ESA));
	}

	@Test
	public void testMultiRecordAndInline() throws Exception {
		RecordSet rs = new RecordSet();
		Mapping mapping1 =
						new Mapping("MRField", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] values1 = new String[] { "A", "B", "C", "D" };
		rs.addResults(mapping1, toStringList(values1));
		Mapping mapping2 = new Mapping("ILField", NameFormat.NO_COUNTS, 1, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."), 0, 0);
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
	public void testSingleInlineRecord() throws Exception {
		RecordSet rs = new RecordSet();
		String[] expectedValues = new String[] { "A", "B", "C", "D" };
		Mapping mapping = new Mapping("Field", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."), 0, 0);
		MappingRecord record = new MappingRecord(mapping, toStringList(expectedValues));
		rs.add(record);

		Iterator<List<String>> iterator = rs.iterator();
		assertTrue(iterator.hasNext());
		List<String> values = iterator.next();
		assertArrayEquals(expectedValues, values.toArray(ESA));

		assertTrue(!iterator.hasNext());
		this.exception.expect(NoSuchElementException.class);
		iterator.next();
	}

	@Test
	public void testSingleMultiRecord() throws Exception {
		RecordSet rs = new RecordSet();
		String[] expectedValues = new String[] { "A", "B", "C", "D" };
		Mapping mapping = new Mapping("Field", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."), 0, 0);
		MappingRecord record = new MappingRecord(mapping, toStringList(expectedValues));
		rs.add(record);

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

		this.exception.expect(NoSuchElementException.class);
		iterator.next();
	}

	@Test
	public void testThreeMRGroups() throws Exception {
		RecordSet rs = new RecordSet();
		Mapping mapping;
		MappingRecord record;

		mapping = new Mapping("Field1", NameFormat.NO_COUNTS, 1, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."), 0, 0);
		record = new MappingRecord(mapping, toStringList(new String[] { "A", "B" }));
		rs.add(record);
		mapping = new Mapping("Field2", NameFormat.NO_COUNTS, 2, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."), 0, 0);
		record = new MappingRecord(mapping, toStringList(new String[] { "C", "D" }));
		rs.add(record);
		mapping = new Mapping("Field3", NameFormat.NO_COUNTS, 3, MultiValueBehaviour.MULTI_RECORD, XmlUtil.createXPathValue(null, "."), 0, 0);
		record = new MappingRecord(mapping, toStringList(new String[] { "E", "F" }));
		rs.add(record);

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
