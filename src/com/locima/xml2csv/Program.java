package com.locima.xml2csv;

import java.io.File;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.extractor.XmlDataExtractor;
import com.locima.xml2csv.inputparser.IConfigParser;
import com.locima.xml2csv.inputparser.MappingsSet;
import com.locima.xml2csv.inputparser.xml.XmlFileParser;
import com.locima.xml2csv.output.OutputManager;

/**
 * Main entry point and logic for the program.
 */
public class Program {

	private static final Logger CONSOLE = LoggerFactory.getLogger("Console");
	private static final Logger LOG = LoggerFactory.getLogger(Program.class);

	public static final String OPT_OUT_DIR = "o";
	public static final String OPT_SQL_DIR = "s";
	public static final String OPT_XML_DIR = "x";
	public static final String OPT_TRIM_WHITESPACE = "w";

	/**
	 * Entry point for the command line execution.
	 */
	public static void main(String[] args) {
		new Program().execute(args);
	}

	/**
	 * Entry point for code-based execution.
	 *
	 * @param configDirectory The directory from which configuration files should be read that define the mappings from XML to CSV.
	 * @param xmlInputDirectory The directory from which XML files should be read.
	 * @param outputDirectory The directory to which output CSV files should be written.
	 * @throws ProgramException if anything goes wrong that couldn't be recovered.
	 */
	public void execute(String configDirectory, String xmlInputDirectory, String outputDirectory, boolean trimWhitespace) throws ProgramException {
		LOG.info("Finding all the input configuration files.");
		List<File> inputConfigFiles = FileUtility.getFilesInDirectory(configDirectory);

		LOG.info("Parsing all the input configuration files to create mapping definitions.");
		IConfigParser configParser = new XmlFileParser();
		configParser.load(inputConfigFiles);
		MappingsSet mappings = configParser.getMappings();

		// Create headers for all the output files
		OutputManager outputMgr = new OutputManager();
		outputMgr.setDirectory(outputDirectory);
		try {
			outputMgr.createFiles(mappings.getHeaders());

			// Parse the input XML files
			XmlDataExtractor xde = new XmlDataExtractor();
			xde.setTrimWhitespace(trimWhitespace);
			xde.setMappings(mappings);

			// Iterate over all files and write out all the records to the output, managed by the OutputManager
			List<File> xmlFiles = FileUtility.getFilesInDirectory(xmlInputDirectory);
			for (File xmlFile : xmlFiles) {
				xde.convert(xmlFile, outputMgr);
			}
		} finally {
			// No matter what happens, attempt to close all the OutputManager resources
			outputMgr.close();
		}
	}

	/**
	 * Main logic for the utility.
	 *
	 * @param args command line arguments
	 */
	public void execute(String[] args) {
		CONSOLE.info("Initialised");

		try {
			LOG.trace("Parsing command line arguments");
			CommandLine cmdLine = getOptions(args);
			LOG.trace("Arguments valid, proceeding");

			execute(cmdLine.getOptionValue(OPT_SQL_DIR), cmdLine.getOptionValue(OPT_XML_DIR), cmdLine.getOptionValue(OPT_OUT_DIR),
							(boolean) cmdLine.getParsedOptionValue(OPT_TRIM_WHITESPACE));

			CONSOLE.info("Completed succesfully.");
		} catch (final ProgramException pe) {
			// All we can do is print out the error and terminate the program
			CONSOLE.error("Program termininating due to error, details follow.", pe);
		} catch (ParseException pe) {
			// Thrown when the command line arguments are invalid
			CONSOLE.error("Invalid arguments specified: " + pe.getMessage());
		}
	}

	/**
	 * Parse the command line arguments in to a proper set of specified options using Apache Commons CLI 1.2.
	 *
	 * @param args command line arguments
	 * @return parsed command line arguments
	 * @throws ParseException if there's a problem with parsing
	 */
	public CommandLine getOptions(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption(new Option(OPT_SQL_DIR, "sqlDir", true, "The directory containing the SQL files that define what outputs are required."));
		options.addOption(new Option(OPT_XML_DIR, "xmlDir", true, "The directory containing the XML files from which data will be extracted."));
		options.addOption(new Option(OPT_OUT_DIR, "outDir", true, "The directory to which the output CSV files will be written."));
		options.addOption(new Option(OPT_TRIM_WHITESPACE, "trimWhitespace", false,
						"If set then all whitespace at the front and end of values will be removed."));
		CommandLineParser parser = new BasicParser();
		return parser.parse(options, args);
	}
}
