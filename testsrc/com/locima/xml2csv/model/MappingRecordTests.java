package com.locima.xml2csv.model;

import static com.locima.xml2csv.TestHelpers.toStringList;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.locima.xml2csv.XmlUtil;

public class MappingRecordTests {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void testEmptyMappingRecord() throws Exception {
		Mapping mapping = new Mapping("Field", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."), 0, 0);
		MappingRecord record = new MappingRecord(mapping, new ArrayList<String>());

		Assert.assertNull(record.getFirstOrDefault());
		Assert.assertNull(record.getValueAt(0));
		Assert.assertNull(record.getValueAt(1));
		Assert.assertNull(record.getValueAt(2));
	}

	@Test
	public void testMappingRecord() throws Exception {
		Mapping mapping = new Mapping("Field", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.INLINE, XmlUtil.createXPathValue(null, "."), 0, 0);
		MappingRecord record = new MappingRecord(mapping, toStringList("A", "B"));

		Assert.assertEquals("A", record.getFirstOrDefault());
		Assert.assertEquals("A", record.getValueAt(0));
		Assert.assertEquals("B", record.getValueAt(1));
		Assert.assertNull(record.getValueAt(2));
	}

}
