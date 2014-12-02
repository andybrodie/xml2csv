package com.locima.xml2csv.model;

import static com.locima.xml2csv.TestHelpers.toExtractedFieldArray;
import static com.locima.xml2csv.TestHelpers.toExtractedFieldList;
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

import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.configuration.Mapping;
import com.locima.xml2csv.configuration.MappingList;
import com.locima.xml2csv.configuration.MultiValueBehaviour;
import com.locima.xml2csv.configuration.NameFormat;
import com.locima.xml2csv.extractor.ExtractedField;
import com.locima.xml2csv.extractor.ExtractedRecord;
import com.locima.xml2csv.extractor.ExtractedRecordList;
import com.locima.xml2csv.extractor.RecordSetCsvIterator;
import com.locima.xml2csv.util.StringUtil;
import com.locima.xml2csv.util.XmlUtil;

public class RecordSetTests {

	private final static ExtractedField[] ESA = new ExtractedField[0];

	@Rule
	public ExpectedException exception = ExpectedException.none();

	private ExtractedRecord createMappingRecord(Mapping mapping, List<String> valueList) {
		// ExtractedRecord record = new ExtractedRecord(mapping, toExtractedFieldList(valueList.toArray(new String[0])));
		// return record;
		return null;
	}

	@Test
	public void testDifferentSizeMRGroups() throws Exception {
		ExtractedRecordList rs = new ExtractedRecordList();
		Mapping mapping;
		ExtractedRecord record;

		MappingList ml = new MappingList();
		ml.setOutputName("Test");

		mapping = new Mapping(ml, "Field1", NameFormat.NO_COUNTS, 1, MultiValueBehaviour.LAZY, XmlUtil.createXPathValue(null, "."), 0, 0);
		record = createMappingRecord(mapping, toStringList(new String[] { "A" }));
		rs.add(record);
		mapping = new Mapping(ml, "Field2", NameFormat.NO_COUNTS, 1, MultiValueBehaviour.LAZY, XmlUtil.createXPathValue(null, "."), 0, 0);
		record = createMappingRecord(mapping, toStringList(new String[] { "C", "D" }));
		rs.add(record);

		RecordSetCsvIterator iterator = (RecordSetCsvIterator) rs.iterator();
		assertEquals(2, iterator.getTotalNumberOfRecords());
		assertTrue(iterator.hasNext());
		assertArrayEquals(toExtractedFieldArray("A", "C"), iterator.next().toArray(ESA));
		assertTrue(iterator.hasNext());
		assertArrayEquals(toExtractedFieldArray(null, "D"), iterator.next().toArray(ESA));
		assertTrue(!iterator.hasNext());
	}

	@Test
	public void testDuplicateFieldNames() throws XMLException {
		Mapping mapping = new Mapping(null, "Field", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.GREEDY, XmlUtil.createXPathValue(null, "."), 0, 0);
		ExtractedRecordList rs = new ExtractedRecordList();
		rs.add(createMappingRecord(mapping, toStringList("A")));
		rs.add(createMappingRecord(mapping, toStringList("B")));
		rs.add(createMappingRecord(mapping, toStringList("C")));
		Iterator<List<ExtractedField>> iterator = rs.iterator();
		assertArrayEquals(toExtractedFieldArray("A", "B", "C"), iterator.next().toArray(ESA));
	}

	@Test
	public void testEmpty() throws Exception {
		ExtractedRecordList rs = new ExtractedRecordList();
		Iterator<List<ExtractedField>> iterator = rs.iterator();
		assertTrue(!iterator.hasNext());
		this.exception.expect(NoSuchElementException.class);
		iterator.next();
	}

	@Test
	public void testIMISameGroupRecord() throws Exception {
		ExtractedRecordList rs = new ExtractedRecordList();
		Mapping ilMapping =
						new Mapping(null, "ILField", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.GREEDY, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] ilValues = new String[] { "A", "B" };
		rs.addResults(ilMapping, toExtractedFieldList(ilValues));
		Mapping mrMapping =
						new Mapping(null, "MRField1", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.LAZY, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] mrValues = new String[] { "C", "D" };
		rs.addResults(mrMapping, toExtractedFieldList(mrValues));
		Mapping ilMapping2 =
						new Mapping(null, "ILField2", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.GREEDY, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] ilValues2 = new String[] { "E", "F" };
		rs.addResults(ilMapping2, toExtractedFieldList(ilValues2));

		List<ExtractedField> values;
		Iterator<List<ExtractedField>> iterator = rs.iterator();
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("A", "B", "C", "E", "F"), values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("A", "B", "D", "E", "F"), values.toArray(ESA));

		assertTrue(!iterator.hasNext());
	}

	@Test
	public void testInlineAndMultiRecord() throws Exception {
		ExtractedRecordList rs = new ExtractedRecordList();
		Mapping ilMapping =
						new Mapping(null, "ILField", NameFormat.NO_COUNTS, 1, MultiValueBehaviour.GREEDY, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] ilValues = new String[] { "E", "F", "G", "H" };
		rs.addResults(ilMapping, toExtractedFieldList(ilValues));
		Mapping mrMapping =
						new Mapping(null, "MRField", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.LAZY, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] mrValues = new String[] { "A", "B", "C", "D" };
		rs.addResults(mrMapping, toExtractedFieldList(mrValues));

		List<ExtractedField> values;

		Iterator<List<ExtractedField>> iterator = rs.iterator();
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("E", "F", "G", "H", "A"), values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("E", "F", "G", "H", "B"), values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("E", "F", "G", "H", "C"), values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("E", "F", "G", "H", "D"), values.toArray(ESA));

		assertTrue(!iterator.hasNext());
	}

	@Test
	public void testMIMSameGroupRecord() throws Exception {
		ExtractedRecordList rs = new ExtractedRecordList();
		Mapping mrMapping =
						new Mapping(null, "MRField", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.LAZY, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] mrValues = new String[] { "A", "B" };
		rs.addResults(mrMapping, toExtractedFieldList(mrValues));
		Mapping ilMapping =
						new Mapping(null, "ILField", NameFormat.NO_COUNTS, 1, MultiValueBehaviour.GREEDY, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] ilValues = new String[] { "C", "D" };
		rs.addResults(ilMapping, toExtractedFieldList(ilValues));
		Mapping mrMapping2 =
						new Mapping(null, "MRField", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.LAZY, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] mrValues2 = new String[] { "E", "F" };
		rs.addResults(mrMapping2, toExtractedFieldList(mrValues2));

		List<ExtractedField> values;
		Iterator<List<ExtractedField>> iterator = rs.iterator();
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("A", "C", "D", "E"), values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("B", "C", "D", "F"), values.toArray(ESA));

		assertTrue(!iterator.hasNext());
	}

	@Test
	public void testMultiInlineRecords() throws Exception {
		ExtractedRecordList rs = new ExtractedRecordList();
		String[][] records = new String[][] { new String[] { "A", "B", "C", "D" }, new String[] { "E", "F", "G", "H" } };
		int count = 0;
		for (String[] record : records) {
			Mapping mapping =
							new Mapping(null, "Field" + count, NameFormat.NO_COUNTS, 0, MultiValueBehaviour.GREEDY, XmlUtil.createXPathValue(null,
											"."), 0, 0);
			count++;
			ExtractedRecord mappingRecord = createMappingRecord(mapping, toStringList(record));
			rs.add(mappingRecord);
		}

		List<ExtractedField> values;
		Iterator<List<ExtractedField>> iterator = rs.iterator();
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray(StringUtil.concatenate(records[0], records[1])), values.toArray(ESA));
		assertTrue(!iterator.hasNext());
	}

	@Test
	public void testMultiMRGroupsAsc() throws Exception {
		ExtractedRecordList rs = new ExtractedRecordList();
		Mapping mrMapping =
						new Mapping(null, "MRField", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.LAZY, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] mrValues = new String[] { "A", "B" };
		rs.addResults(mrMapping, toExtractedFieldList(mrValues));
		Mapping mrMapping2 =
						new Mapping(null, "MRField", NameFormat.NO_COUNTS, 1, MultiValueBehaviour.LAZY, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] mrValues2 = new String[] { "C", "D" };
		rs.addResults(mrMapping2, toExtractedFieldList(mrValues2));

		List<ExtractedField> values;

		Iterator<List<ExtractedField>> iterator = rs.iterator();

		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("A", "C"), values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("B", "C"), values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("A", "D"), values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("B", "D"), values.toArray(ESA));
	}

	@Test
	public void testMultiMRGroupsDesc() throws Exception {
		ExtractedRecordList rs = new ExtractedRecordList();
		Mapping mrMapping =
						new Mapping(null, "MRField", NameFormat.NO_COUNTS, 1, MultiValueBehaviour.LAZY, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] mrValues = new String[] { "A", "B" };
		rs.addResults(mrMapping, toExtractedFieldList(mrValues));
		Mapping mrMapping2 =
						new Mapping(null, "MRField", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.LAZY, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] mrValues2 = new String[] { "C", "D" };
		rs.addResults(mrMapping2, toExtractedFieldList(mrValues2));

		List<ExtractedField> values;

		Iterator<List<ExtractedField>> iterator = rs.iterator();

		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("A", "C"), values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("A", "D"), values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("B", "C"), values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("B", "D"), values.toArray(ESA));

		assertTrue(!iterator.hasNext());
	}

	@Test
	public void testMultipleResultsForSameMapping() throws Exception {
		ExtractedRecordList rs = new ExtractedRecordList();
		Mapping mapping = new Mapping(null, "Field", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.LAZY, XmlUtil.createXPathValue(null, "."), 0, 0);
		rs.addResults(mapping, createMappingRecord(mapping, toStringList("A", "B")));
		rs.addResults(mapping, createMappingRecord(mapping, toStringList("C")));
		rs.addResults(mapping, createMappingRecord(mapping, toStringList("D", "E")));

		Iterator<List<ExtractedField>> iterator = rs.iterator();
		assertArrayEquals(toExtractedFieldArray("A"), iterator.next().toArray(ESA));
		assertArrayEquals(toExtractedFieldArray("B"), iterator.next().toArray(ESA));
		assertArrayEquals(toExtractedFieldArray("C"), iterator.next().toArray(ESA));
		assertArrayEquals(toExtractedFieldArray("D"), iterator.next().toArray(ESA));
		assertArrayEquals(toExtractedFieldArray("E"), iterator.next().toArray(ESA));
	}

	@Test
	public void testMultiRecordAndInline() throws Exception {
		ExtractedRecordList rs = new ExtractedRecordList();
		Mapping mapping1 = new Mapping(null, "MRField", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.LAZY, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] values1 = new String[] { "A", "B", "C", "D" };
		rs.addResults(mapping1, toExtractedFieldList(values1));
		Mapping mapping2 =
						new Mapping(null, "ILField", NameFormat.NO_COUNTS, 1, MultiValueBehaviour.GREEDY, XmlUtil.createXPathValue(null, "."), 0, 0);
		String[] values2 = new String[] { "E", "F", "G", "H" };
		rs.addResults(mapping2, toExtractedFieldList(values2));

		List<ExtractedField> values;

		Iterator<List<ExtractedField>> iterator = rs.iterator();
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("A", "E", "F", "G", "H"), values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("B", "E", "F", "G", "H"), values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("C", "E", "F", "G", "H"), values.toArray(ESA));
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("D", "E", "F", "G", "H"), values.toArray(ESA));

		assertTrue(!iterator.hasNext());
	}

	@Test
	public void testSingleInlineRecord() throws Exception {
		ExtractedRecordList rs = new ExtractedRecordList();
		String[] expectedValues = new String[] { "A", "B", "C", "D" };
		Mapping mapping = new Mapping(null, "Field", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.GREEDY, XmlUtil.createXPathValue(null, "."), 0, 0);
		ExtractedRecord record = createMappingRecord(mapping, toStringList(expectedValues));
		rs.add(record);

		Iterator<List<ExtractedField>> iterator = rs.iterator();
		assertTrue(iterator.hasNext());
		List<ExtractedField> values = iterator.next();
		assertArrayEquals(toExtractedFieldArray(expectedValues), values.toArray(ESA));

		assertTrue(!iterator.hasNext());
		this.exception.expect(NoSuchElementException.class);
		iterator.next();
	}

	@Test
	public void testSingleMultiRecord() throws Exception {
		ExtractedRecordList rs = new ExtractedRecordList();
		String[] expectedValues = new String[] { "A", "B", "C", "D" };
		Mapping mapping = new Mapping(null, "Field", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.LAZY, XmlUtil.createXPathValue(null, "."), 0, 0);
		ExtractedRecord record = createMappingRecord(mapping, toStringList(expectedValues));
		rs.add(record);

		List<ExtractedField> values;

		Iterator<List<ExtractedField>> iterator = rs.iterator();
		assertTrue(iterator.hasNext());
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("A"), values.toArray(ESA));
		assertTrue(iterator.hasNext());
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("B"), values.toArray(ESA));
		assertTrue(iterator.hasNext());
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("C"), values.toArray(ESA));
		assertTrue(iterator.hasNext());
		values = iterator.next();
		assertArrayEquals(toExtractedFieldArray("D"), values.toArray(ESA));
		assertTrue(!iterator.hasNext());

		this.exception.expect(NoSuchElementException.class);
		iterator.next();
	}

	@Test
	public void testThreeMRGroups() throws Exception {
		ExtractedRecordList rs = new ExtractedRecordList();
		Mapping mapping;
		ExtractedRecord record;

		MappingList ml = new MappingList();
		ml.setOutputName("Test");

		mapping = new Mapping(ml, "Field1", NameFormat.NO_COUNTS, 1, MultiValueBehaviour.LAZY, XmlUtil.createXPathValue(null, "."), 0, 0);
		record = createMappingRecord(mapping, toStringList(new String[] { "A", "B" }));
		rs.add(record);
		mapping = new Mapping(ml, "Field2", NameFormat.NO_COUNTS, 2, MultiValueBehaviour.LAZY, XmlUtil.createXPathValue(null, "."), 0, 0);
		record = createMappingRecord(mapping, toStringList(new String[] { "C", "D" }));
		rs.add(record);
		mapping = new Mapping(ml, "Field3", NameFormat.NO_COUNTS, 3, MultiValueBehaviour.LAZY, XmlUtil.createXPathValue(null, "."), 0, 0);
		record = createMappingRecord(mapping, toStringList(new String[] { "E", "F" }));
		rs.add(record);

		RecordSetCsvIterator iterator = (RecordSetCsvIterator) rs.iterator();
		assertEquals(8, iterator.getTotalNumberOfRecords());
		assertTrue(iterator.hasNext());
		assertArrayEquals(toExtractedFieldArray("A", "C", "E"), iterator.next().toArray(ESA));
		assertArrayEquals(toExtractedFieldArray("B", "C", "E"), iterator.next().toArray(ESA));
		assertArrayEquals(toExtractedFieldArray("A", "D", "E"), iterator.next().toArray(ESA));
		assertArrayEquals(toExtractedFieldArray("B", "D", "E"), iterator.next().toArray(ESA));
		assertArrayEquals(toExtractedFieldArray("A", "C", "F"), iterator.next().toArray(ESA));
		assertArrayEquals(toExtractedFieldArray("B", "C", "F"), iterator.next().toArray(ESA));
		assertArrayEquals(toExtractedFieldArray("A", "D", "F"), iterator.next().toArray(ESA));
		assertArrayEquals(toExtractedFieldArray("B", "D", "F"), iterator.next().toArray(ESA));
	}
}
