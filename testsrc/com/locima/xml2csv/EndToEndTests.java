package com.locima.xml2csv;

import static com.locima.xml2csv.TestHelpers.assertCSVEquals;
import static com.locima.xml2csv.TestHelpers.processFiles;

import java.io.File;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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

}
