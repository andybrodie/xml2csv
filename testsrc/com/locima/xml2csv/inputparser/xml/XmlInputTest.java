package com.locima.xml2csv.inputparser.xml;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.locima.xml2csv.extractor.NameToXPathMappings;
import com.locima.xml2csv.inputparser.MappingsSet;
import com.locima.xml2csv.inputparser.xml.XmlFileParser;

public class XmlInputTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		XmlFileParser parser = new XmlFileParser();
		List<File> files = new ArrayList<File>();
		files.add(new File("testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamilyConfig.xml"));
		parser.load(files);
		MappingsSet set = parser.getMappings();
		assertNotNull("MappingSet was null, should be non-null",set);
		assertEquals(2, set.size());

		NameToXPathMappings mapping1 = set.get("family");
		assertNotNull("Couldn't find family mapping", mapping1);
	}

}
