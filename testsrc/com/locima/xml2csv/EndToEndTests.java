package com.locima.xml2csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.locima.xml2csv.extractor.DataExtractorException;
import com.locima.xml2csv.extractor.XmlDataExtractor;
import com.locima.xml2csv.inputparser.FileParserException;
import com.locima.xml2csv.inputparser.MappingConfiguration;
import com.locima.xml2csv.inputparser.xml.XmlFileParser;
import com.locima.xml2csv.output.OutputManager;
import com.locima.xml2csv.output.OutputManagerException;

public class EndToEndTests {

	private void assertCSVEquals(File expectedFile, File actualFile) throws Exception {
		String[] expected = loadFile(expectedFile);
		String[] actual = loadFile(actualFile);

		for (int i = 0; i < expected.length; i++) {
			if (i >= actual.length) {
				fail(String.format("Unable to compare line %d as actual has run out of lines (expected %d).", i + 1, expected.length));
			}
			assertEquals(String.format("Mismatch at line %d", i + 1), expected[i], actual[i]);
		}
		assertEquals("More lines in actual than expected.", expected.length, actual.length);
	}

	private String[] loadFile(File file) throws IOException {
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		List<String> lines = new ArrayList<String>();
		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
			lines.add(line);
		}
		bufferedReader.close();
		return lines.toArray(new String[lines.size()]);
	}

	@Test
	public void testEndToEnd() throws Exception {
		TemporaryFolder outputFolder =
						ProcessFiles("testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamilyConfig.xml",
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
						ProcessFiles("testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamilyInlineConfig.xml",
										"testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamily1.xml",
										"testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamily2.xml");

		assertCSVEquals(new File("testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamilyInlineOutput.csv"), new File(outputFolder.getRoot(),
						"family.csv"));
	}


	private TemporaryFolder ProcessFiles(String configurationFile, String... inputFiles) throws XMLException, DataExtractorException,
					OutputManagerException, FileParserException, IOException {
		XmlFileParser parser = new XmlFileParser();
		List<File> files = new ArrayList<File>();
		files.add(new File(configurationFile));
		parser.load(files);
		MappingConfiguration set = parser.getMappings();

		TemporaryFolder outputFolder = new TemporaryFolder();
		outputFolder.create();

		OutputManager om = new OutputManager();
		om.setDirectory(outputFolder.getRoot().getAbsolutePath());
		om.createFiles(set.getMappingsHeaders());

		XmlDataExtractor extractor = new XmlDataExtractor();
		extractor.setTrimWhitespace(true);
		extractor.setMappings(set);

		for (String filename : inputFiles) {
			File family1File = new File(filename);
			extractor.convert(family1File, om);
		}
		om.close();
		return outputFolder;
	}

}
