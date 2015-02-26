package com.locima.xml2csv;

import java.io.File;
import java.util.List;

import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.configuration.MappingConfiguration;
import com.locima.xml2csv.extractor.XmlDataExtractor;
import com.locima.xml2csv.inputparser.IConfigParser;
import com.locima.xml2csv.inputparser.xml.XmlFileParser;
import com.locima.xml2csv.output.OutputManager;
import com.locima.xml2csv.util.XmlUtil;

/**
 * The main entry point for xml2csv for code-based execution.
 */
public class Xml2Csv {

	private static final Logger LOG = LoggerFactory.getLogger(Xml2Csv.class);

	/**
	 * Entry point for code-based execution with all required inputs precisely defined.
	 *
	 * @param configFiles A ist of configuration files that define the mappings from XML to CSV. Must not be null.
	 * @param xmlInputFiles A list of XML input files that should be processed against the <code>configFiles</code>. Must not be null.
	 * @param outputDirectory The directory to which output CSV files should be written. Assumes to exist and be writeable.
	 * @param trimWhitespace If true, then whitespace at the beginning or end of a value extracted will be trimmed.
	 * @param appendOutput If true, then all output will be appended to if an output file already exists.
	 * @throws ProgramException if anything goes wrong that couldn't be recovered.
	 */
	public void execute(List<File> configFiles, List<File> xmlInputFiles, File outputDirectory, boolean appendOutput, boolean trimWhitespace)
					throws ProgramException {

		LOG.info("Parsing all the input configuration files to create mapping definitions.");
		IConfigParser configParser = new XmlFileParser();
		configParser.load(configFiles);
		MappingConfiguration mappingConfig = configParser.getMappings();

		// Create headers for all the output files
		OutputManager outputMgr = new OutputManager();
		try {
			outputMgr.initialise(outputDirectory, mappingConfig, appendOutput);

			// Parse the input XML files
			XmlDataExtractor extractor = new XmlDataExtractor();
			extractor.setMappingConfiguration(mappingConfig);

			// Iterate over all files that pass filters and write out all the records to the output, managed by the OutputManager
			for (File xmlFile : xmlInputFiles) {
				if (mappingConfig.include(xmlFile)) {
					XdmNode docToConvert = XmlUtil.loadXmlFile(xmlFile);
					if (mappingConfig.include(docToConvert)) {
						extractor.extractTo(docToConvert, outputMgr);
					} else {
						LOG.debug("Excluding {} due to document content filters", xmlFile.getAbsolutePath());
					}
				} else {
					LOG.debug("Excluding {} due to file filters", xmlFile.getAbsolutePath());
				}
			}
		} finally {
			/*
			 * No matter what happens, attempt to close all the OutputManager resources so at least we won't leave resources open.
			 */
			outputMgr.close();
		}
	}

}
