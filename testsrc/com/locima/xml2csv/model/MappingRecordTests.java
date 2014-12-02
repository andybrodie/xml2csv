package com.locima.xml2csv.model;

import static com.locima.xml2csv.TestHelpers.toExtractedFieldList;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.locima.xml2csv.configuration.Mapping;
import com.locima.xml2csv.configuration.MappingList;
import com.locima.xml2csv.configuration.MultiValueBehaviour;
import com.locima.xml2csv.configuration.NameFormat;
import com.locima.xml2csv.extractor.ExtractedField;
import com.locima.xml2csv.extractor.MappingExtractionContext;
import com.locima.xml2csv.extractor.ExtractedRecord;
import com.locima.xml2csv.util.XmlUtil;

public class MappingRecordTests {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void testEmptyMappingRecord() throws Exception {
		MappingList ml = new MappingList();
		Mapping mapping = new Mapping(ml, "Field", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.GREEDY, XmlUtil.createXPathValue(null, "."), 0, 0);
		ExtractedRecord record = new ExtractedRecord(new MappingExtractionContext(mapping), new ArrayList<ExtractedField>());

		Assert.assertNull(record.getFirstOrDefault());
		Assert.assertNull(record.getValueAt(0));
		Assert.assertNull(record.getValueAt(1));
		Assert.assertNull(record.getValueAt(2));
	}

	@Test
	public void testMappingRecord() throws Exception {
		MappingList ml = new MappingList();
		Mapping mapping = new Mapping(ml, "Field", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.GREEDY, XmlUtil.createXPathValue(null, "."), 0, 0);
		ExtractedRecord record = new ExtractedRecord(new MappingExtractionContext(mapping), toExtractedFieldList("A", "B"));

		Assert.assertEquals(new ExtractedField("0", "A"), record.getFirstOrDefault());
		Assert.assertEquals(new ExtractedField("0", "A"), record.getValueAt(0));
		Assert.assertEquals(new ExtractedField("0", "B"), record.getValueAt(1));
		Assert.assertNull(record.getValueAt(2));
	}

}
