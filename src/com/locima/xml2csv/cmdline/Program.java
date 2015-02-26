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
import com.locima.xml2csv.Xml2Csv;
import com.locima.xml2csv.util.FileUtility;
import com.locima.xml2csv.util.StringUtil;

/**
 * Main entry point and parameter parsing logic for the command line application.
 */
public class Program {

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
	 * Generates the options that define the command line arguments to this program.
	 *
	 * @return parsed command line arguments.
	 */
	public static Options getOptions() {
		Options options = new Options();
		Option option = new Option(OPT_CONFIG_FILE, "configurationFile", true, "A single file containing the configuration to use.");
		option.setRequired(true);
		options.addOption(option);
		// Don't ask me why it's formatted like this, blame Eclipse Luna!
		option =
						new Option(OPT_OUT_DIR, "outputDirectory", true, "The directory to which the output CSV files will be written.  "
										+ "If not specified, current working directory will be used.  Directory must exist and be writeable.");
		options.addOption(option);
		option =
						new Option(OPT_TRIM_WHITESPACE, "preserveWhitespace", false,
										"If specified then whitespace will not be removed from the start and end of output fields.");
		options.addOption(option);
		option = new Option(OPT_HELP, "help", false, "If specified, prints this message and terminates immediately.");
		options.addOption(option);
		option =
						new Option(OPT_APPEND_OUTPUT, "appendOutput", false,
										"If specified, all output will be appended to any existing output files.  If an existing file is"
														+ " appended to then field names will not be output.");
		options.addOption(option);
		return options;
	}

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
	 * Entry point for code-based execution that just has directory names for configuration and input.
	 *
	 * @param configFileName the configuration file name.
	 * @param xmlInputs a pattern that when expanded will contain a list of files.
	 * @param outputDirectoryName The directory to which output CSV files should be written.
	 * @param trimWhitespace If true, then whitespace at the beginning or end of a value extracted will be trimmed.
	 * @param appendOutput If true, then all output will be appended to if an output file already exists.
	 * @throws ProgramException if anything goes wrong that couldn't be recovered.
	 */
	public void execute(String configFileName, String[] xmlInputs, String outputDirectoryName, boolean appendOutput, boolean trimWhitespace)
					throws ProgramException {
		List<File> xmlInputFiles;
		xmlInputFiles = FileUtility.getFiles(xmlInputs);
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
		new Xml2Csv().execute(configFiles, xmlInputFiles, outputDirectory, appendOutput, trimWhitespace);
	}

	/**
	 * Instance entry point for command line invocation.
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
			String[] xmlInputs = cmdLine.getArgs();
			String outputDirName = cmdLine.getOptionValue(OPT_OUT_DIR);
			String configFileName = cmdLine.getOptionValue(OPT_CONFIG_FILE);
			execute(configFileName, xmlInputs, outputDirName, appendOutput, trimWhitespace);
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
	 * <p>
	 * This method keeps track of every exception that it has seen (to ensure we don't end up in a recursive loop). It also won't repeat any messages
	 * that have been printed before (duplicate messages inside nested exceptions) because it's pointless for the user.
	 *
	 * @param throwable the exception to print the causes of.
	 * @return a string, usually sent to {@link System#err}.
	 */
	public String getAllCauses(Throwable throwable) {
		if (throwable == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		// Stores exceptions we have seen already
		List<Throwable> seen = new ArrayList<Throwable>();
		// Stores messages that we have seen already
		List<String> messages = new ArrayList<String>();
		String message = throwable.getMessage();
		sb.append(String.format("%s%n", message));
		messages.add(message);
		Throwable cause = throwable.getCause();
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
}
