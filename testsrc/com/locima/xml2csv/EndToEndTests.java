package com.locima.xml2csv;

import static com.locima.xml2csv.TestHelpers.processFiles;
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
