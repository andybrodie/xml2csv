package com.locima.xml2csv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.rules.TemporaryFolder;

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

	public static void assertMappingInstanceCountsCorrect(MappingConfiguration config, Integer... instanceCounts) {
		List<Integer> maxInstanceCounts = new ArrayList<Integer>();
		for (IMappingContainer mappingContainer : config) {
			maxInstanceCounts.add(mappingContainer.getMaxInstanceCount());
			for (IMapping mapping : mappingContainer) {
				maxInstanceCounts.add(mapping.getMaxInstanceCount());
			}
		}
		Assert.assertArrayEquals(instanceCounts, maxInstanceCounts.toArray());

	}

	public static TemporaryFolder processFiles(String configurationFile, String... inputFiles) throws XMLException, DataExtractorException,
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
		extractor.setMappingConfiguration(set);

		for (String filename : inputFiles) {
			File family1File = new File(filename);
			extractor.convert(family1File, om);
		}

		om.close();
		return outputFolder;
	}

}
