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

import com.locima.xml2csv.ArgumentNullException;
import com.locima.xml2csv.BugException;
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

	/**
	 * Initial command line options that detect the help ({@link Program#OPT_HELP}) and version info ({@link Program#OPT_VERSION}).
	 * <p>
	 * Set up by the static constructor.
	 */
	public static final Options HELP_OPTIONS;

	private static final Logger LOG = LoggerFactory.getLogger(Program.class);

	/**
	 * The main command line options, used after {@value #HELP_OPTONS} has not found a match. Set up by the static constructor.
	 */
	public static final Options MAIN_OPTIONS;

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

	public static final String OPT_VERSION = "v";

	/**
	 * The name of the property within META-INF/build.properties that contains the timestamp of this build: {@value} .
	 */
	private static final String PROPERTY_BUILD_TSTAMP = "BuildTimeStamp";

	/**
	 * The name of the property within META-INF/build.properties that contains the commit hash of this build: {@value} .
	 */
	private static final String PROPERTY_COMMITHASH = "CommitHash";

	/**
	 * The name of the property within META-INF/build.properties that contains the version number of this build: {@value} .
	 */
	private static final String PROPERTY_VERSION = "Version";

	/*
	 * Sets up MAIN_OPTIONS, the options that define the command line arguments to this program.
	 */
	static {
		Options mainOptions = new Options();
		Option option = new Option(OPT_CONFIG_FILE, "configuration-file", true, "A single file containing the configuration to use.");
		option.setRequired(true);
		mainOptions.addOption(option);
		// Don't ask me why it's formatted like this, blame Eclipse Luna!
		option =
						new Option(OPT_OUT_DIR, "output-directory", true, "The directory to which the output CSV files will be written.  "
										+ "If not specified, current working directory will be used.  Directory must exist and be writeable.");
		mainOptions.addOption(option);
		option =
						new Option(OPT_TRIM_WHITESPACE, "preserve-whitespace", false,
										"If specified then whitespace will not be removed from the start and end of output fields.");
		mainOptions.addOption(option);
		option =
						new Option(OPT_APPEND_OUTPUT, "append-output", false,
										"If specified, all output will be appended to any existing output files.  If an existing file is"
														+ " appended to then field names will not be output.");
		mainOptions.addOption(option);

		Options helpOptions = new Options();

		option = new Option(OPT_HELP, "help", false, "Show help on using xml2csv and terminate.");
		mainOptions.addOption(option);
		helpOptions.addOption(option);

		option = new Option(OPT_VERSION, "version", false, "Show version information and terminate.");
		mainOptions.addOption(option);
		helpOptions.addOption(option);

		MAIN_OPTIONS = mainOptions;
		HELP_OPTIONS = helpOptions;
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
		sb.append(".  ");
		sb.append(HEADER);
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
		if (configFileName == null) {
			throw new ArgumentNullException("configFileName");
		}
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

		try {
			if (!showHelpOrVersion(args)) {
				BasicParser parser = new BasicParser();
				CommandLine cmdLine = parser.parse(MAIN_OPTIONS, args);
				LOG.info("Successfully parsed main options.");
				boolean trimWhitespace = Boolean.parseBoolean(cmdLine.getOptionValue(OPT_TRIM_WHITESPACE));
				boolean appendOutput = Boolean.parseBoolean(cmdLine.getOptionValue(OPT_APPEND_OUTPUT));
				String[] xmlInputs = cmdLine.getArgs();
				String outputDirName = cmdLine.getOptionValue(OPT_OUT_DIR);
				String configFileName = cmdLine.getOptionValue(OPT_CONFIG_FILE);
				execute(configFileName, xmlInputs, outputDirName, appendOutput, trimWhitespace);
			}
		} catch (ProgramException pe) {
			LOG.error("A fatal error caused xml2csv to abort", pe);
			// All we can do is print out the error and terminate the program
			System.err.print(getAllCauses(pe));
		} catch (ParseException pe) {
			// Thrown when the command line arguments are invalid
			LOG.debug("Invalid arguments specified: {}", pe.getMessage());
			System.err.println("Invalid arguments specified: " + pe.getMessage());
			printHelp();
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
		final String defaultPath = this.getClass().getName();
		String path;
		try {
			URI sourceUri = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
			if ("rsrc:./".equals(sourceUri.toString())) {
				LOG.debug("Detected source came from rsrc:./, so guessing that I'm in xml2csv-standalone.jar");
				path = "-jar xml2csv-standalone.jar";
			} else if (sourceUri.toString().endsWith(".jar")) {
				final String jarName = new File(sourceUri).getName();
				LOG.debug("Detected that source was a jar file, so I'm in here: {}", jarName);
				path = "-jar " + jarName;
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
	 * Print help on invocing xml2csv from the command line to the console.
	 */
	private void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(new PrintWriter(System.err, true), CONSOLE_WIDTH, "java.exe " + getExecutableName(), createHeader(), MAIN_OPTIONS, 0, 0,
						null, true);
	}

	/**
	 * Prints version information to the console.
	 */
	private void printVersionInfo() {
		System.out.println("xml2csv by Locima Ltd.  Maintained at http://github.com/andybrodie/xml2csv.");
		Properties props = getBuildProperties();
		System.out.println("Version: " + props.getProperty(PROPERTY_VERSION));
		System.out.println("Build Timestamp " + props.getProperty(PROPERTY_BUILD_TSTAMP));
		System.out.println("Git Commit Hash: " + props.getProperty(PROPERTY_COMMITHASH));
	}

	/**
	 * If the arguments contain a request for help or verson information, then show these and return true, otherwise return false.
	 *
	 * @param args command line arguments.
	 * @return true if the help or version options have been specified, false otherwise (i.e. normal processing should resume).
	 * @throws ParseException if an option parsing exception occurs.
	 */
	private boolean showHelpOrVersion(String[] args) throws ParseException {
		CommandLineParser parser = new BasicParser();
		CommandLine cmdLine = parser.parse(HELP_OPTIONS, args, true);
		if (cmdLine.getOptions().length == 0) {
			return false;
		}

		LOG.info("Showing help or version information and terminating.");
		if (cmdLine.hasOption(OPT_HELP)) {
			printHelp();
		} else if (cmdLine.hasOption(OPT_VERSION)) {
			printVersionInfo();
		} else {
			throw new BugException("Options set up is wrong.  Found help or version, but neither believes they have been passed.");
		}
		return true;
	}
}
