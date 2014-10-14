package com.locima.xml2csv;
import static com.locima.xml2csv.TestHelpers.assertCsvEquals;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ProgramTests {

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
		Program p = new Program();
		List<File> configFiles = new ArrayList<File>();
		configFiles.add(new File("testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamilyConfig.xml"));

		List<File> inputFiles = new ArrayList<File>();
		inputFiles.add(new File("testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamily1.xml"));
		inputFiles.add(new File("testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamily2.xml"));

		TemporaryFolder outputFolder = new TemporaryFolder();
		outputFolder.create();

		p.execute(configFiles, inputFiles, outputFolder.getRoot().getAbsoluteFile(), true);
		
		assertCsvEquals(new File("testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamilyOutput1.csv"),
						new File(outputFolder.getRoot(), "family.csv"));
		assertCsvEquals(new File("testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamilyOutput2.csv"),
						new File(outputFolder.getRoot(), "people.csv"));
	}

}
