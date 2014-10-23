package com.locima.xml2csv.extractor;

import static com.locima.xml2csv.TestHelpers.assertCsvEquals;
import static com.locima.xml2csv.TestHelpers.loadMappingConfiguration;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.TestHelpers;
import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.XmlUtil;
import com.locima.xml2csv.inputparser.FileParserException;
import com.locima.xml2csv.model.MappingConfiguration;
import com.locima.xml2csv.output.OutputManager;
import com.locima.xml2csv.output.OutputManagerException;

public class ExtractorTests {

	private static final Logger LOG = LoggerFactory.getLogger(ExtractorTests.class);

	@Test
	public void testInstanceCounts() throws Exception {
		TemporaryFolder outputFolder =
						testInstanceCounts("HeavilyNestedConfig.xml", new int[] { 4, 1, 1, 3, 1, 6 },
										"HeavilyNestedInstance.xml");

		assertCsvEquals(TestHelpers.createFile("HeavilyNestedInstance1.csv"), new File(outputFolder.getRoot(),
						"HeavilyNestedInstance.csv"));
	}

	private TemporaryFolder testInstanceCounts(String configFile, int[] instanceCounts, String... inputFiles) throws IOException, XMLException,
					FileParserException, OutputManagerException, DataExtractorException {
		MappingConfiguration config = loadMappingConfiguration(configFile);

		TemporaryFolder outputFolder = new TemporaryFolder();
		outputFolder.create();

		XmlDataExtractor extractor = new XmlDataExtractor();
		extractor.setTrimWhitespace(true);
		extractor.setMappingConfiguration(config);

		File inputFile = TestHelpers.createFile("HeavilyNestedInstance.xml");

		extractor.convert(XmlUtil.loadXmlFile(inputFile), null);

		/*
		 * Now each MappingList and Mapping knows the maximum number of iterations that can appear within a single mapping, Now start again to get the
		 * correct column headers. This is just a temporary measure whilst we're adding inline support.
		 */

		LOG.info("*** Starting again now we know the column counts");
		OutputManager om = new OutputManager();
		om.setDirectory(outputFolder.getRoot().getAbsolutePath());
		om.createFiles(config.getMappingsHeaders());
		extractor.convert(XmlUtil.loadXmlFile(inputFile), om);
		om.close();

		return outputFolder;
	}

}
