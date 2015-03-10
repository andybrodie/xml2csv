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

import com.locima.xml2csv.cmdline.Program;

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
	public void testParsedEntryPoint() throws Exception {
		Xml2Csv xml2csv = new Xml2Csv();
		List<File> configFiles = new ArrayList<File>();
		configFiles.add(TestHelpers.createFile("SimpleFamilyConfig.xml"));

		List<File> inputFiles = new ArrayList<File>();
		inputFiles.add(TestHelpers.createFile("SimpleFamily1.xml"));
		inputFiles.add(TestHelpers.createFile("SimpleFamily2.xml"));

		TemporaryFolder outputFolder = new TemporaryFolder();
		outputFolder.create();

		xml2csv.execute(configFiles, inputFiles, outputFolder.getRoot().getAbsoluteFile(), false, true);

		assertCsvEquals("SimpleFamilyOutput1.csv", outputFolder.getRoot(), "family.csv");
		assertCsvEquals("SimpleFamilyOutput2.csv", outputFolder.getRoot(), "people.csv");
	}

	@Test
	public void testUnparsedEntryPoint() throws Exception {
		
		List<String> parameters = new ArrayList<String>();
		parameters.add("-c");
		parameters.add(TestHelpers.createFile("SimpleFamilyConfig.xml").getAbsolutePath());
		parameters.add(TestHelpers.createFile("SimpleFamily1.xml").getAbsolutePath());
		parameters.add(TestHelpers.createFile("SimpleFamily2.xml").getAbsolutePath());

		TemporaryFolder outputFolder = new TemporaryFolder();
		outputFolder.create();
		File outputDir = outputFolder.getRoot();
		parameters.add("-o");
		parameters.add(outputDir.getAbsolutePath());

		Program.main(parameters.toArray(new String[0]));
		
		assertCsvEquals("SimpleFamilyOutput1.csv", outputFolder.getRoot(), "family.csv");
		assertCsvEquals("SimpleFamilyOutput2.csv", outputFolder.getRoot(), "people.csv");
	}
	
	@Test
	public void testInvalidInvocation() throws Exception {
		Program xml2csv = new Program();
		xml2csv.execute(new String[0]);
	}

}
