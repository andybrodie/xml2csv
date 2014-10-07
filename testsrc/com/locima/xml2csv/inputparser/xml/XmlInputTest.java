package com.locima.xml2csv.inputparser.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.model.IMappingContainer;
import com.locima.xml2csv.model.MappingConfiguration;

public class XmlInputTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

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
		MappingConfiguration set = parser.getMappings();
		assertNotNull("MappingSet was null, should be non-null", set);
		assertEquals(2, set.size());

		IMappingContainer mapping1 = set.getMappingsByName("family");
		assertNotNull("Couldn't find family mapping", mapping1);
	}

	@Test
	public void testBadInline() throws Exception {
		XmlFileParser parser = new XmlFileParser();
		List<File> files = new ArrayList<File>();
		files.add(new File("testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamilyConfigBadInline.xml"));
		this.thrown.expect(XMLException.class);
		parser.load(files);
	}
	
	@Test
	public void testNamespaces() throws Exception {
		XmlFileParser parser = new XmlFileParser();
		List<File> files = new ArrayList<File>();
		files.add(new File("testsrc/com/locima/xml2csv/inputparser/xml/FamilyConfigWithNamespaces.xml"));
		parser.load(files);
		MappingConfiguration set = parser.getMappings();
		assertNotNull("MappingSet was null, should be non-null", set);
		assertEquals(2, set.size());

		IMappingContainer mapping1 = set.getMappingsByName("FamilyWithNamespaces");
		assertNotNull("Couldn't find family mapping", mapping1);
		IMappingContainer mapping2 = set.getMappingsByName("FamilyMembersWithNamespaces");
		assertNotNull("Couldn't find family mapping", mapping2);
	}

}
