package com.locima.xml2csv;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.XdmNode;

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
import com.locima.xml2csv.output.OutputManager;

/**
 * Main entry point and logic for the program.
 */
// CHECKSTYLE:OFF Class Data Abstraction Coupling. This is the entry point where all program logic is brought together.
public class Program {
	// CHECKSTYLE:ON

	public static final int CONSOLE_WIDTH = 80;
	private static final String HEADER = "xml2csv v0.1.  Converts XML files in to CSV files according to a set of specified rules.";
	private static final Logger LOG = LoggerFactory.getLogger(Program.class);
	public static final String OPT_APPEND_OUTPUT = "a";
	public static final String OPT_CONFIG_FILE = "c";
	public static final String OPT_HELP = "h";
	public static final String OPT_OUT_DIR = "o";
	public static final String OPT_TRIM_WHITESPACE = "w";
	public static final String OPT_XML_DIR = "i";

	/**
	 * Entry point for the command line execution.
	 *
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		shutLogbackUp();
		new Program().execute(args);
	}

	/**
	 * Logback unfortunately starts logging debugging information to the console if you don't configure it.
	 * <p>
	 * This sucks for the user (who shouldn't need to have to learn how to configure logback). Also I don't want to tightly-couple my code to logback,
	 * so if the user isn't hasn't tried to get logging working, then let's get it to quietly STFU.
	 */
	private static void shutLogbackUp() {

	}

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
	public void execute(List<File> configFiles, List<File> xmlInputFiles, File outputDirectory, boolean trimWhitespace, boolean appendOutput)
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
			extractor.setTrimWhitespace(trimWhitespace);
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

	/**
	 * Entry point for code-based execution that just has directory names for configuration and input.
	 *
	 * @param configFileName the configuration file name.
	 * @param xmlInputDirectoryName The directory from which XML files should be read.
	 * @param outputDirectoryName The directory to which output CSV files should be written.
	 * @param trimWhitespace If true, then whitespace at the beginning or end of a value extracted will be trimmed.
	 * @param appendOutput If true, then all output will be appended to if an output file already exists.
	 * @throws ProgramException if anything goes wrong that couldn't be recovered.
	 */
	public void execute(String configFileName, String xmlInputDirectoryName, String outputDirectoryName, boolean trimWhitespace, boolean appendOutput)
					throws ProgramException {
		File xmlInputDirectory;
		try {
			xmlInputDirectory = FileUtility.getDirectory(xmlInputDirectoryName, FileUtility.CAN_READ, false);
		} catch (IOException ioe) {
			throw new ProgramException(ioe, "Problem with XML input directory: %s", ioe.getMessage());
		}
		File outputDirectory;
		try {
			outputDirectory = FileUtility.getDirectory(outputDirectoryName, FileUtility.CAN_WRITE, true);
		} catch (IOException ioe) {
			throw new ProgramException(ioe, "Problem with output directory: %s", ioe.getMessage());
		}

		List<File> configFiles = new ArrayList<File>();
		try {
			configFiles.add(FileUtility.getFile(configFileName, FileUtility.CAN_READ));
		} catch (IOException ioe) {
			throw new ProgramException(ioe, "Problem with configuration file: %s", ioe.getMessage());
		}
		List<File> xmlInputFiles = FileUtility.getFiles(xmlInputDirectory, false);
		execute(configFiles, xmlInputFiles, outputDirectory, trimWhitespace, appendOutput);
	}

	/**
	 * Main logic for the utility.
	 *
	 * @param args command line arguments
	 */
	public void execute(String[] args) {
		if (LOG.isInfoEnabled()) {
			LOG.info("xml2csv execute invoked {}", StringUtil.toString(args));
		}
		Options options = getOptions();

		try {
			CommandLineParser parser = new BasicParser();
			CommandLine cmdLine = parser.parse(options, args);
			if (cmdLine.hasOption(OPT_HELP)) {
				HelpFormatter formatter = new HelpFormatter();
				LOG.debug("User asked for help, so outputting usage message and terminating");
				formatter.printHelp(new PrintWriter(System.out, true), CONSOLE_WIDTH, "java.exe -jar xml2csv.jar", HEADER, options, 0, 0, null, true);
			}
			LOG.trace("Arguments verified.");
			String trimWhitespaceValue = cmdLine.getOptionValue(OPT_TRIM_WHITESPACE);
			boolean trimWhitespace = Boolean.parseBoolean(trimWhitespaceValue);
			String appendOutputValue = cmdLine.getOptionValue(OPT_APPEND_OUTPUT);
			boolean appendOutput = Boolean.parseBoolean(appendOutputValue);
			execute(cmdLine.getOptionValue(OPT_CONFIG_FILE), cmdLine.getOptionValue(OPT_XML_DIR), cmdLine.getOptionValue(OPT_OUT_DIR),
							trimWhitespace, appendOutput);
			System.out.println("Completed succesfully.");
		} catch (ProgramException pe) {
			// All we can do is print out the error and terminate the program
			System.err.println(pe.getMessage());
			Throwable cause = pe.getCause();
			if (cause != null) {
				System.err.println(cause.getMessage());
			}
		} catch (ParseException pe) {
			// Thrown when the command line arguments are invalid
			LOG.debug("Invalid arguments specified: {}", pe.getMessage());
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
		option = new Option(OPT_XML_DIR, "inputDir", true, "The directory containing the XML files from which data will be extracted.");
		option.setRequired(true);
		options.addOption(option);
		// Don't ask me why it's formatted like this, blame Eclipse Luna!
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
		option = new Option(OPT_APPEND_OUTPUT, "append-output", false, "If specified, all output will be appended to any existing output files.");
		return options;
	}
}
