package com.locima.xml2csv.cmdline;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

import com.locima.xml2csv.ProgramException;
import com.locima.xml2csv.configuration.MappingConfiguration;
import com.locima.xml2csv.extractor.XmlDataExtractor;
import com.locima.xml2csv.inputparser.IConfigParser;
import com.locima.xml2csv.inputparser.xml.XmlFileParser;
import com.locima.xml2csv.output.OutputManager;
import com.locima.xml2csv.util.FileUtility;
import com.locima.xml2csv.util.StringUtil;
import com.locima.xml2csv.util.XmlUtil;

/**
 * Main entry point and logic for the command line application.
 */
// CHECKSTYLE:OFF Class Data Abstraction Coupling. This is the entry point where all program logic is brought together.
public class Program {
	// CHECKSTYLE:ON

	/**
	 * The console width to wrap the command line help and errors to, hardcoded to {@value} .
	 * <p>
	 * There doesn't appear to be a portable way in Java to find this out.
	 */
	public static final int CONSOLE_WIDTH = 140;

	/**
	 * Text header shown on every invocation of xml2csv.
	 */
	private static final String HEADER = "Converts XML files in to CSV files using a user-defined set of rules.";

	private static final Logger LOG = LoggerFactory.getLogger(Program.class);

	/**
	 * Command line option for specifying that existing output files should be appended to: {@value} .
	 */
	public static final String OPT_APPEND_OUTPUT = "a";

	/**
	 * Command line option for specifying a configuration file: {@value} .
	 */
	public static final String OPT_CONFIG_FILE = "c";

	/**
	 * Command line option for display help: {@value} .
	 */
	public static final String OPT_HELP = "h";
	/**
	 * Command line option for specifying an output directory for CSV files: {@value} .
	 */
	public static final String OPT_OUT_DIR = "o";
	/**
	 * Command line option for specifying that whitespace should be preserved: {@value} .
	 */
	public static final String OPT_TRIM_WHITESPACE = "w";

	/**
	 * Command line option for specifying the input directory or filenames to convert: {@value} .
	 */
	public static final String OPT_XML_DIR = "i";

	/**
	 * The name of the property within META-INF/build.properties that contains the timestamp of this build: {@value} .
	 */
	private static final String PROPERTY_BUILD_TSTAMP = "BuildTimeStamp";

	/**
	 * The name of the property within META-INF/build.properties that contains the commit hash of this build: {@value} .
	 */
	private static final String PROPERTY_COMMITHASH = "Commit";

	/**
	 * The name of the property within META-INF/build.properties that contains the version number of this build: {@value} .
	 */
	private static final String PROPERTY_VERSION = "Version";

	/**
	 * Entry point for the command line execution.
	 *
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		new Program().execute(args);
	}

	/**
	 * Creates a header string for all usage and help messages.
	 *
	 * @return a header string for all usage and help messages.
	 */
	private String createHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("xml2csv v");
		Properties props = getBuildProperties();
		sb.append(props.getProperty(PROPERTY_VERSION));
		sb.append("  ");
		sb.append(HEADER);
		sb.append("  ");
		sb.append("Built: ");
		sb.append(props.getProperty(PROPERTY_BUILD_TSTAMP));
		sb.append(".");
		return sb.toString();
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

	/**
	 * Entry point for code-based execution that just has directory names for configuration and input.
	 *
	 * @param configFileName the configuration file name.
	 * @param xmlInputSpecification a pattern that when expanded will contain a list of files.
	 * @param outputDirectoryName The directory to which output CSV files should be written.
	 * @param trimWhitespace If true, then whitespace at the beginning or end of a value extracted will be trimmed.
	 * @param appendOutput If true, then all output will be appended to if an output file already exists.
	 * @throws ProgramException if anything goes wrong that couldn't be recovered.
	 */
	public void execute(String configFileName, String xmlInputSpecification, String outputDirectoryName, boolean appendOutput, boolean trimWhitespace)
					throws ProgramException {
		List<File> xmlInputFiles;
		xmlInputFiles = FileUtility.getFiles(xmlInputSpecification);
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
		execute(configFiles, xmlInputFiles, outputDirectory, appendOutput, trimWhitespace);
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
				formatter.printHelp(new PrintWriter(System.out, true), CONSOLE_WIDTH, "java.exe -jar xml2csv.jar", createHeader(), options, 0, 0,
								null, true);
			}
			LOG.trace("Arguments verified.");
			boolean trimWhitespace = Boolean.parseBoolean(cmdLine.getOptionValue(OPT_TRIM_WHITESPACE));
			boolean appendOutput = Boolean.parseBoolean(cmdLine.getOptionValue(OPT_APPEND_OUTPUT));
			String xmlDirName = cmdLine.getOptionValue(OPT_XML_DIR);
			String outputDirName = cmdLine.getOptionValue(OPT_OUT_DIR);
			String configFileName = cmdLine.getOptionValue(OPT_CONFIG_FILE);
			execute(configFileName, xmlDirName, outputDirName, appendOutput, trimWhitespace);
		} catch (ProgramException pe) {
			LOG.error("A fatal error caused xml2csv to abort", pe);
			// All we can do is print out the error and terminate the program
			System.err.print(getAllCauses(pe));
		} catch (ParseException pe) {
			// Thrown when the command line arguments are invalid
			LOG.debug("Invalid arguments specified: {}", pe.getMessage());
			System.err.println("Invalid arguments specified: " + pe.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(new PrintWriter(System.err, true), CONSOLE_WIDTH, "java.exe -jar " + getExecutableName(), createHeader(), options, 0,
							0, null, true);
		}
	}

	/**
	 * Prints all the causes of the exception passed, regardless of how many there are nested within it.
	 *
	 * @param t the exception to print the causes of.
	 * @return a string, usually sent to {@link System#err}.
	 */
	public String getAllCauses(Throwable t) {
		if (t == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		List<Throwable> seen = new ArrayList<Throwable>();
		List<String> messages = new ArrayList<String>();
		String message = t.getMessage();
		sb.append(String.format("%s%n", message));
		messages.add(message);
		Throwable cause = t.getCause();
		while (cause != null) {
			message = cause.getMessage();
			if (!messages.contains(message)) {
				messages.add(message);
				sb.append(String.format("Because: %s%n", message));
			}
			cause = cause.getCause();
			// Stop infinite loops
			if (seen.contains(cause)) {
				cause = null;
			} else {
				seen.add(cause);
			}
		}
		return sb.toString();
	}

	/**
	 * Retrieve the build properties from a locally available file <code>META-INF/build.properties</code> or provide default values if not available.
	 *
	 * @return a set of properties about the build currently in use.
	 */
	private Properties getBuildProperties() {
		Properties props = new Properties();
		final String propFilename = "META-INF/build.properties";
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(propFilename);
		if (inputStream != null) {
			try {
				props.load(inputStream);
			} catch (IOException e) {
				LOG.error("Unable to load " + propFilename, e);
			}
			return props;
		}
		if (!props.containsKey(PROPERTY_VERSION)) {
			props.setProperty(PROPERTY_VERSION, "<No version>");
		}
		if (!props.containsKey(PROPERTY_COMMITHASH)) {
			props.setProperty(PROPERTY_COMMITHASH, "<None>");
		}
		if (!props.containsKey(PROPERTY_BUILD_TSTAMP)) {
			props.setProperty(PROPERTY_BUILD_TSTAMP, "<N/A>");
		}
		return props;
	}

	/**
	 * Retrieves the container that's being executed. If this is a Jar file then the jar file name will be returned. If we're not in a jar file then
	 * the name of this class (the application entry point) is returned.
	 *
	 * @return a string (never null or zero length) containing the jar or class name to execute this app.
	 */
	private String getExecutableName() {
		final String defaultPath = "com.locima.xml2cv.cmdline.Program";
		String path;
		try {
			URI sourceUri = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
			if ("rsrc:./".equals(sourceUri.toString())) {
				path = "xml2csv-standalone.jar";
			} else if (sourceUri.toString().endsWith(".jar")) {
				path = new File(sourceUri).getName();
			} else {
				path = defaultPath;
			}
			// CHECKSTYLE:OFF I don't want this code to EVER cause a problem in the application
		} catch (RuntimeException e) {
			// CHECKSTYLE:ON
			// A problem occurred whilst getting the name of the executing container, so return "xml2csv.jar"
			path = defaultPath;
		} catch (URISyntaxException e) {
			path = defaultPath;
		}
		return path;
	}

	/**
	 * Generates the options that define the command line arguments to this program.
	 *
	 * @return parsed command line arguments.
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
		option =
						new Option(OPT_APPEND_OUTPUT, "append-output", false,
										"If specified, all output will be appended to any existing output files.  If an existing file is"
														+ " appended to then field names will not be output.");
		options.addOption(option);
		return options;
	}
}
