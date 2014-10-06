package com.locima.xml2csv;

import static com.locima.xml2csv.TestHelpers.assertCSVEquals;
import static com.locima.xml2csv.TestHelpers.processFiles;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.locima.xml2csv.inputparser.IMappingContainer;
import com.locima.xml2csv.inputparser.MappingConfiguration;
import com.locima.xml2csv.inputparser.xml.XmlFileParser;

public class EndToEndTests {

	@Test
	public void testEndToEnd() throws Exception {
		TemporaryFolder outputFolder =
						processFiles("testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamilyConfig.xml",
										"testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamily1.xml",
										"testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamily2.xml");
		assertCSVEquals(new File("testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamilyOutput1.csv"),
						new File(outputFolder.getRoot(), "family.csv"));
		assertCSVEquals(new File("testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamilyOutput2.csv"),
						new File(outputFolder.getRoot(), "people.csv"));

	}

	@Test
	public void testEndToEndWithInline() throws Exception {
		TemporaryFolder outputFolder =
						processFiles("testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamilyInlineConfig.xml",
										"testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamily1.xml",
										"testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamily2.xml");

		assertCSVEquals(new File("testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamilyInlineOutput.csv"), new File(outputFolder.getRoot(),
						"family.csv"));
	}
	
	@Test
	public void testNamespaces() throws Exception {
		TemporaryFolder outputFolder = processFiles("testsrc/com/locima/xml2csv/inputparser/xml/FamilyConfigWithNamespaces.xml",
						"testsrc/com/locima/xml2csv/inputparser/xml/FamilyWithNamespaces.xml");
		assertCSVEquals(new File("testsrc/com/locima/xml2csv/inputparser/xml/FamilyWithNamespaces.csv"),
						new File(outputFolder.getRoot(), "FamilyWithNamespaces.csv"));
		assertCSVEquals(new File("testsrc/com/locima/xml2csv/inputparser/xml/FamilyMembersWithNamespaces.csv"),
						new File(outputFolder.getRoot(), "FamilyMembersWithNamespaces.csv"));
	}


}
