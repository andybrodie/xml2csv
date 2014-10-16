package com.locima.xml2csv;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.extractor.XmlDataExtractor;
import com.locima.xml2csv.inputparser.IConfigParser;
import com.locima.xml2csv.inputparser.xml.XmlFileParser;
import com.locima.xml2csv.model.MappingConfiguration;
import com.locima.xml2csv.output.IOutputManager;
import com.locima.xml2csv.output.OutputManager;

/**
 * Main entry point and logic for the program.
 */
// CHECKSTYLE:OFF Class Data Abstraction Coupling. This is the entry point where all program logic is brought together.
public class Program {
	// CHECKSTYLE:ON

	private static final Logger CONSOLE = LoggerFactory.getLogger("Console");

	private static final Logger LOG = LoggerFactory.getLogger(Program.class);
	public static final int CONSOLE_WIDTH = 80;
	public static final String OPT_HELP = "h";
	public static final String OPT_OUT_DIR = "o";
	public static final String OPT_CONFIG_FILE = "c";
	public static final String OPT_TRIM_WHITESPACE = "w";
	public static final String OPT_XML_DIR = "x";
	public static final int VERSION_MAJOR = 0;
	public static final int VERSION_MINOR = 1;
	private static final String HEADER = String.format("xml2csv v%d.%d converts XML files in to CSV files according to a set of specified rules.",
					VERSION_MAJOR, VERSION_MINOR);

	/**
	 * Entry point for the command line execution.
	 *
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		new Program().execute(args);
	}

	/**
	 * Entry point for code-based execution with all required inputs precisely defined.
	 *
	 * @param configFiles A ist of configuration files that define the mappings from XML to CSV. Must not be null.
	 * @param xmlInputFiles A list of XML input files that should be processed against the <code>configFiles</code>. Must not be null.
	 * @param outputDirectory The directory to which output CSV files should be written. Assumes to exist and be writeable.
	 * @param trimWhitespace If true, then whitespace at the beginning or end of a value extracted will be trimmed.
	 * @throws ProgramException if anything goes wrong that couldn't be recovered.
	 */
	public void execute(List<File> configFiles, List<File> xmlInputFiles, File outputDirectory, boolean trimWhitespace) throws ProgramException {

		LOG.info("Parsing all the input configuration files to create mapping definitions.");
		IConfigParser configParser = new XmlFileParser();
		configParser.load(configFiles);
		MappingConfiguration mappingConfig = configParser.getMappings();

		// Create headers for all the output files
		IOutputManager outputMgr = new OutputManager();
		outputMgr.setDirectory(outputDirectory);
		try {
			outputMgr.createFiles(mappingConfig.getMappingsHeaders());

			// Parse the input XML files
			XmlDataExtractor xde = new XmlDataExtractor();
			xde.setTrimWhitespace(trimWhitespace);
			xde.setMappingConfiguration(mappingConfig);

			// Iterate over all files and write out all the records to the output, managed by the OutputManager
			for (File xmlFile : xmlInputFiles) {
				xde.convert(xmlFile, outputMgr);
			}

			// If there's any inlined elements, run the whole lot again. Yes, this is very inefficient, but it works for now!
			if (mappingConfig.containsInline()) {
				LOG.info("Found inline element, therefore reprocessing with inline values supported.");
				outputMgr.createFiles(mappingConfig.getMappingsHeaders());
				for (File xmlFile : xmlInputFiles) {
					xde.convert(xmlFile, outputMgr);
				}
			}

		} finally {
			// No matter what happens, attempt to close all the OutputManager resources
			outputMgr.close();
		}
	}

	/**
	 * Entry point for code-based execution that just has directory names for configuration and input.
	 *
	 * @param configFileName the configuration file name.
	 * @param xmlInputDirectoryName The directory from which XML files should be read.
	 * @param outputDirectoryName The directory to which output CSV files should be written.
	 * @param trimWhitespace If true, then whitespace at the beginning or end of a value extracted will be trimmed.
	 * @throws ProgramException if anything goes wrong that couldn't be recovered.
	 */
	public void execute(String configFileName, String xmlInputDirectoryName, String outputDirectoryName, boolean trimWhitespace)
					throws ProgramException {
		try {
			File xmlInputDirectory = FileUtility.getDirectory(xmlInputDirectoryName, FileUtility.CAN_READ, false);
			File outputDirectory = FileUtility.getDirectory(outputDirectoryName, FileUtility.CAN_WRITE, true);

			List<File> configFiles = new ArrayList<File>();
			configFiles.add(FileUtility.getFile(configFileName, FileUtility.CAN_READ));
			List<File> xmlInputFiles = FileUtility.getFilesInDirectory(xmlInputDirectory);
			execute(configFiles, xmlInputFiles, outputDirectory, trimWhitespace);
		} catch (IOException ioe) {
			// Only occurs from calls to FileUtility.getDirectory above
			throw new ProgramException(ioe, "There was a problem with a directory you specified: " + ioe.getMessage());
		}

	}

	/**
	 * Main logic for the utility.
	 *
	 * @param args command line arguments
	 */
	public void execute(String[] args) {

		Options options = getOptions();

		try {
			if (LOG.isTraceEnabled()) {
				LOG.trace("Parsing command line arguments {}", StringUtil.toString(args));
			}
			CommandLineParser parser = new BasicParser();
			CommandLine cmdLine = parser.parse(options, args);
			if (cmdLine.hasOption(OPT_HELP)) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp(new PrintWriter(System.out, true), CONSOLE_WIDTH, "java.exe -jar xml2csv.jar", HEADER, options, 0, 0, null, true);
			}
			LOG.trace("Arguments verified ok");
			String trimWhitespaceValue = cmdLine.getOptionValue(OPT_TRIM_WHITESPACE);
			boolean trimWhitespace = Boolean.parseBoolean(trimWhitespaceValue);
			execute(cmdLine.getOptionValue(OPT_CONFIG_FILE), cmdLine.getOptionValue(OPT_XML_DIR), cmdLine.getOptionValue(OPT_OUT_DIR), trimWhitespace);
			CONSOLE.info("Completed succesfully.");
		} catch (ProgramException pe) {
			// All we can do is print out the error and terminate the program
			CONSOLE.error("Program termininating due to error, details follow.", pe);
		} catch (ParseException pe) {
			// Thrown when the command line arguments are invalid
			System.err.println("Invalid arguments specified: " + pe.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(new PrintWriter(System.err, true), CONSOLE_WIDTH, "java.exe -jar xml2csv.jar", HEADER, options, 0, 0, null, true);
		}
	}

	/**
	 * Generates the options that define the command line arguments to this program.
	 *
	 * @return parsed command line arguments
	 */
	public Options getOptions() {
		Options options = new Options();
		Option option = new Option(OPT_CONFIG_FILE, "configFile", true, "A single file containing the configuration to use.");
		option.setRequired(true);
		options.addOption(option);
		option = new Option(OPT_XML_DIR, "xmlDir", true, "The directory containing the XML files from which data will be extracted.");
		option.setRequired(true);
		options.addOption(option);
		option =
						new Option(OPT_OUT_DIR, "outDir", true, "The directory to which the output CSV files will be written.  "
										+ "If not specified, current working directory will be used.");
		options.addOption(option);
		option =
						new Option(OPT_TRIM_WHITESPACE, "preserveWhitespace", false,
										"If specified then whitespace will not be removed from either end of values found.");
		options.addOption(option);
		option = new Option(OPT_HELP, "help", false, "If specified, prints this message and terminates immediately.");
		options.addOption(option);
		return options;
	}
}
