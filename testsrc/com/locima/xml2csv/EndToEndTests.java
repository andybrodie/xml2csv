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

import com.locima.xml2csv.extractor.XmlDataExtractor;
import com.locima.xml2csv.inputparser.MappingsSet;
import com.locima.xml2csv.inputparser.xml.XmlFileParser;
import com.locima.xml2csv.output.OutputManager;

public class EndToEndTests {

	private void assertCSVEquals(File expectedFile, File actualFile) throws Exception {
		String[] expected = loadFile(expectedFile);
		String[] actual = loadFile(actualFile);

		for (int i = 0; i < expected.length; i++) {
			if (i >= actual.length) {
				fail(String.format("Unable to compare line %d as actual has run out of lines (expected %d).", i+1, expected.length));
			}
			assertEquals(String.format("Mismatch at line %d", i+1), expected[i], actual[i]);
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
		XmlFileParser parser = new XmlFileParser();
		List<File> files = new ArrayList<File>();
		files.add(new File("testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamilyConfig.xml"));
		parser.load(files);
		MappingsSet set = parser.getMappings();

		TemporaryFolder outputFolder = new TemporaryFolder();
		outputFolder.create();

		OutputManager om = new OutputManager();
		om.setDirectory(outputFolder.getRoot().getAbsolutePath());
		om.createFiles(set.getMappingsHeaders());

		XmlDataExtractor extractor = new XmlDataExtractor();
		extractor.setTrimWhitespace(true);
		extractor.setMappings(set);
		File family1File = new File("testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamily1.xml");
		File family2File = new File("testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamily2.xml");
		extractor.convert(family1File, om);
		extractor.convert(family2File, om);
		om.close();

		assertCSVEquals(new File("testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamilyOutput1.csv"),
						new File(outputFolder.getRoot(), "family.csv"));
		assertCSVEquals(new File("testsrc/com/locima/xml2csv/inputparser/xml/SimpleFamilyOutput2.csv"),
						new File(outputFolder.getRoot(), "people.csv"));

	}

}
