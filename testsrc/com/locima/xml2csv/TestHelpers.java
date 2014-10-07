package com.locima.xml2csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.internal.ArrayComparisonFailure;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.extractor.DataExtractorException;
import com.locima.xml2csv.extractor.XmlDataExtractor;
import com.locima.xml2csv.inputparser.FileParserException;
import com.locima.xml2csv.inputparser.IMapping;
import com.locima.xml2csv.inputparser.IMappingContainer;
import com.locima.xml2csv.inputparser.MappingConfiguration;
import com.locima.xml2csv.inputparser.xml.XmlFileParser;
import com.locima.xml2csv.output.OutputManager;
import com.locima.xml2csv.output.OutputManagerException;

public class TestHelpers {

	private static final Logger LOG = LoggerFactory.getLogger(TestHelpers.class);

	public static void assertCsvEquals(File expectedFile, File actualFile) throws Exception {
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

	public static void assertCsvEquals(String expectedFileName, String actualFileName) throws Exception {
		assertCsvEquals(new File(expectedFileName), new File(actualFileName));
	}

	public static void assertCsvEquals(String expectedFileName, File actualRootDirectory, String actualFileName) throws Exception {
		assertCsvEquals(new File(expectedFileName), new File(actualRootDirectory, actualFileName));
	}

	public static void assertMappingInstanceCountsCorrect(MappingConfiguration config, int... instanceCounts) {
		List<Integer> expectedInstanceCounts = new ArrayList<Integer>();
		for (int instanceCount : instanceCounts) {
			expectedInstanceCounts.add(instanceCount);
		}
		List<Integer> maxInstanceCounts = new ArrayList<Integer>();

		for (IMappingContainer mappingContainer : config) {
			getMaxInstanceCounts(mappingContainer, maxInstanceCounts);
		}
		LOG.info("Checking max instance counts of {}", config);
		try {
			Assert.assertArrayEquals(expectedInstanceCounts.toArray(), maxInstanceCounts.toArray());
		} catch (ArrayComparisonFailure acf) {
			// Print out some more helpful logging before re-throwing
			LOG.debug("Expected: {}", toFlatString(expectedInstanceCounts));
			LOG.debug("Actual: {}", toFlatString(maxInstanceCounts));
			throw acf;
		}
	}

	private static void getMaxInstanceCounts(IMapping mapping, List<Integer> maxInstanceCounts) {
		int maxInstanceCount = mapping.getMaxInstanceCount();
		maxInstanceCounts.add(maxInstanceCount);
		String mappingName = mapping.toString();
		LOG.debug("Found {} instance count value for {}", maxInstanceCount, mappingName);
		if (mapping instanceof IMappingContainer) {
			for (IMapping nestedMapping : (IMappingContainer) mapping) {
				getMaxInstanceCounts(nestedMapping, maxInstanceCounts);
			}
		}
	}

	public static String[] loadFile(File file) throws IOException {
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

	public static MappingConfiguration loadMappingConfiguration(String configurationFile) throws XMLException, FileParserException, IOException {
		XmlFileParser parser = new XmlFileParser();
		List<File> files = new ArrayList<File>();
		files.add(new File(configurationFile));
		parser.load(files);
		MappingConfiguration mappingConfig = parser.getMappings();
		return mappingConfig;
	}

	public static TemporaryFolder processFiles(String configurationFile, String... inputFiles) throws XMLException, DataExtractorException,
	OutputManagerException, FileParserException, IOException {
		MappingConfiguration set = loadMappingConfiguration(configurationFile);

		TemporaryFolder outputFolder = new TemporaryFolder();
		outputFolder.create();

		XmlDataExtractor extractor = new XmlDataExtractor();
		extractor.setTrimWhitespace(true);
		extractor.setMappingConfiguration(set);

		OutputManager om = new OutputManager();
		om.setDirectory(outputFolder.getRoot().getAbsolutePath());
		om.createFiles(set.getMappingsHeaders());

		for (String filename : inputFiles) {
			File file = new File(filename);
			extractor.convert(file, om);
		}		
		om.createFiles(set.getMappingsHeaders());
		for (String filename : inputFiles) {
			File file = new File(filename);
			extractor.convert(file, om);
		}

		om.close();
		return outputFolder;
	}

	public static String toFlatString(Collection<? extends Object> second) {
		StringBuffer buf = new StringBuffer();
		if ((second != null) && (second.size() > 0)) {
			for (Object o : second) {
				String s = o == null ? "<null>" : o.toString();
				buf.append(s);
				buf.append(", ");
			}
			buf = buf.delete(buf.length() - 2, buf.length());
		}
		return buf.toString();
	}

	public static String toFlatString(Object[] second) {
		StringBuffer buf = new StringBuffer();
		if ((second != null) && (second.length > 0)) {
			for (Object o : second) {
				String s = o == null ? "<null>" : o.toString();
				buf.append(s);
				buf.append(", ");
			}
			buf = buf.delete(buf.length() - 2, buf.length());
		}
		return buf.toString();
	}

}
