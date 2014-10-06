package com.locima.xml2csv.extractor;

import static com.locima.xml2csv.TestHelpers.assertCSVEquals;
import static com.locima.xml2csv.TestHelpers.assertMappingInstanceCountsCorrect;
import static com.locima.xml2csv.TestHelpers.loadMappingConfiguration;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.inputparser.FileParserException;
import com.locima.xml2csv.inputparser.MappingConfiguration;
import com.locima.xml2csv.output.OutputManager;
import com.locima.xml2csv.output.OutputManagerException;

public class ExtractorTests {

	@Test
	public void testInstanceCounts() throws Exception {
		TemporaryFolder outputFolder =
						testInstanceCounts("testsrc/com/locima/xml2csv/extractor/HeavilyNestedConfig.xml", new int[] { 4, 1, 1, 3, 1, 6 },
										"testsrc/com/locima/xml2csv/extractor/HeavilyNestedInstance.xml");

		assertCSVEquals(new File("testsrc/com/locima/xml2csv/extractor/HeavilyNestedInstance1.csv"), new File(outputFolder.getRoot(),
						"HeavilyNestedInstance.csv"));
	}

	private TemporaryFolder testInstanceCounts(String configFile, int[] instanceCounts, String... inputFiles) throws IOException, XMLException,
					FileParserException, OutputManagerException, DataExtractorException {
		MappingConfiguration config = loadMappingConfiguration(configFile);

		TemporaryFolder outputFolder = new TemporaryFolder();
		outputFolder.create();

		OutputManager om = new OutputManager();
		om.setDirectory(outputFolder.getRoot().getAbsolutePath());
		om.createFiles(config.getMappingsHeaders());

		XmlDataExtractor extractor = new XmlDataExtractor();
		extractor.setTrimWhitespace(true);
		extractor.setMappingConfiguration(config);

		File inputFile = new File("testsrc/com/locima/xml2csv/extractor/HeavilyNestedInstance.xml");

		extractor.convert(inputFile, om);
		
		/* Now each MappingList and Mapping knows the maximum number of iterations that can appear within a single mapping,
		 * Now start again to get the correct column headers.
		 * This is just a temporary measure whilst we're adding inline support.
		 */
		om.createFiles(config.getMappingsHeaders());
		extractor.convert(inputFile, om);
		om.close();

		assertMappingInstanceCountsCorrect(config, instanceCounts);

		return outputFolder;
	}

}
