package com.locima.xml2csv.extractor;

import static com.locima.xml2csv.TestHelpers.assertCsvEquals;
import static com.locima.xml2csv.TestHelpers.loadMappingConfiguration;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.locima.xml2csv.TestHelpers;
import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.configuration.MappingConfiguration;
import com.locima.xml2csv.inputparser.FileParserException;
import com.locima.xml2csv.output.IOutputManager;
import com.locima.xml2csv.output.OutputManager;
import com.locima.xml2csv.output.OutputManagerException;
import com.locima.xml2csv.util.XmlUtil;

public class ExtractorTests {

	@Test
	public void testInstanceCounts() throws Exception {
		File outputFolder = testInstanceCounts("HeavilyNestedConfig.xml", new int[] { 4, 1, 1, 3, 1, 6 }, "HeavilyNestedInstance.xml");

		assertCsvEquals("HeavilyNestedInstance1.csv", outputFolder, "HeavilyNestedInstance.csv");
	}

	private File testInstanceCounts(String configFile, int[] instanceCounts, String... inputFiles) throws IOException, XMLException,
	FileParserException, OutputManagerException, DataExtractorException {
		MappingConfiguration config = loadMappingConfiguration(configFile);

		TemporaryFolder outputFolder = new TemporaryFolder();
		outputFolder.create();

		XmlDataExtractor extractor = new XmlDataExtractor();
		extractor.setMappingConfiguration(config);

		File inputFile = TestHelpers.createFile("HeavilyNestedInstance.xml");

		IOutputManager om = new OutputManager();
		om.initialise(outputFolder.getRoot(), config, false);

		extractor.extractTo(XmlUtil.loadXmlFile(inputFile), om);

		om.close();

		return outputFolder.getRoot();
	}

}
