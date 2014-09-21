package com.locima.xml2csv.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.Tuple;

/**
 * Manages the writing of extracted data to a file, or files.
 */
public class OutputManager implements IOutputManager {

	private static final Logger LOG = LoggerFactory.getLogger(OutputManager.class);

	private static final String QUOTE = "\"";

	/**
	 * Escapes any string so that it can be added to a CSV. Specifically, if the value contains a double-quote, CR or LF then the entire value is
	 * wrapped in double-quotes. Also, any instances of double-quote are replaced with 2 double-quotes.
	 *
	 * @param value Any value that can be converted to a String. If null is passed, null is returned.
	 * @return A string suitable to be embedded in to a CSV file that will be read by Excel.
	 */
	static String escapeForCsv(Object value) {
		String returnValue = value == null ? null : value.toString();
		boolean quotesRequired = false;
		if (value == null) {
			return null;
		}
		if (returnValue.contains(QUOTE)) {
			returnValue = returnValue.replace(QUOTE, "\"\"");
			quotesRequired = true;
		}
		if (returnValue.contains("\n") || returnValue.contains(",") || returnValue.contains(";")) {
			quotesRequired = true;
		}

		return quotesRequired ? QUOTE + returnValue + QUOTE : returnValue;
	}

	private File outputDirectory;

	private Map<String, Tuple<File, Writer>> writers;

	/**
	 * Close all the writers managed by this class. All exceptions are suppressed as there's nothing we're going to do about it anyway.
	 */
	@Override
	public void close() {
		LOG.info("Closing {} writers", this.writers.size());
		for (Entry<String, Tuple<File, Writer>> writer : this.writers.entrySet()) {
			String writerName = writer.getKey();
			String writerFileName = writer.getValue().getFirst().getAbsolutePath();
			Writer writerFileWriter = writer.getValue().getSecond();
			try {
				LOG.trace("Flushing data for writer {} ({})", writerName, writerFileName);
				writerFileWriter.flush();
			} catch (IOException ioe) {
				// No point throwing the error up as there's no useful action to be taken at this point
				LOG.error("Error flushing data to writer", ioe);
			}
			try {
				LOG.trace("Closing writer {} ({})", writerName, writerFileName);
				writerFileWriter.close();
			} catch (IOException ioe) {
				// No point throwing the error up as there's no useful action to be taken at this point
				LOG.error("Error closing writer", ioe);
			}
		}
	}

	/**
	 * Converts a list of values in to a single output line.
	 *
	 * @param fields the collection of strings that are the individual fields to output.
	 * @param fieldSeparator the character to use to separate all the values. Must not be null.
	 * @param wrapper a string to write before and after all the values. May be null (which means no wrapper written).
	 * @return a String, possibly empty, but never null.
	 */
	private String collectionToString(List<?> fields, String fieldSeparator, String wrapper) {
		if (null == fields) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		if (null != wrapper) {
			sb.append(wrapper);
		}
		int size = fields.size();
		for (int i = 0; i < size; i++) {
			sb.append(escapeForCsv(fields.get(i)));
			if (i < (size - 1)) {
				sb.append(fieldSeparator);
			}
		}
		if (null != wrapper) {
			sb.append(wrapper);
		}
		return sb.toString();
	}

	/**
	 * Creates the CSV files that will be used for the different writers.
	 * 
	 * @param outputConfiguration a map of output names (used for file names within the output directory) and the columns or fields that will be
	 *            present in each one.
	 * @throws OutputManagerException if an unrecoverable error occurs whilst creating the output files or writing to them.
	 */
	@Override
	public void createFiles(Map<String, List<String>> outputConfiguration) throws OutputManagerException {
		this.writers = new HashMap<String, Tuple<File, Writer>>();
		final String encoding = "UTF8";

		File writerFile = null;
		LOG.info("Creating {} writers in {}", outputConfiguration.size(), this.outputDirectory.getAbsolutePath());
		try {
			for (Map.Entry<String, List<String>> entry : outputConfiguration.entrySet()) {
				String writerName = entry.getKey();
				writerFile = new File(this.outputDirectory, writerName + ".csv");
				LOG.trace("Creating output file for writer {} ({})", writerName, writerFile.getAbsolutePath());
				Writer writerFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writerFile), encoding));

				this.writers.put(writerName, new Tuple<File, Writer>(writerFile, writerFileWriter));
				LOG.info("Successfully created output file for writer {} ({})", writerName, writerFile.getAbsolutePath());

				// Write field names to the first row of the file.
				try {
					writerFileWriter.write(collectionToString(entry.getValue(), ",", null));
					writerFileWriter.write(System.getProperty("line.separator"));
				} catch (IOException ioe) {
					close();
					throw new OutputManagerException("Unable to write field names to " + writerName, ioe);
				}
			}
		} catch (FileNotFoundException fileNotFoundException) {
			// If we can't even create an output file, throw an exception up to abort
			throw new OutputManagerException(fileNotFoundException, "Unable to create output file %s", writerFile.getAbsolutePath());
		} catch (UnsupportedEncodingException uee) {
			// This should never happen as we're hard-coding a known supported encoding in Java
			throw new AssertionError("Unexpected unsupported encoding exception: " + encoding, uee);
		}
		LOG.info("Successfully created {} writers in {}", outputConfiguration.size(), this.outputDirectory.getAbsolutePath());
	}

	/**
	 * Retrieve all the writers managed by this instance.
	 * 
	 * @return a map between the output name and a tuple of the {@link File} that the output is written to and an open file writer to that file.
	 */
	@Override
	public Map<String, Tuple<File, Writer>> getWriterFiles() {
		return this.writers;
	}

	/**
	 * Sets the directory to which output files will be written.
	 *
	 * @param outputDirectoryName the name of the output directory.
	 * @throws OutputManagerException If the directory does not exist
	 */
	@Override
	public void setDirectory(String outputDirectoryName) throws OutputManagerException {
		File dir = new File(outputDirectoryName);
		if (!dir.isDirectory()) {
			throw new OutputManagerException("Output directory specified is not a directory: %1$s", outputDirectoryName);
		}
		if (!dir.canWrite()) {
			throw new OutputManagerException("Output directory is not writeable: %1$s", outputDirectoryName);
		}
		this.outputDirectory = dir;
		LOG.info("Configured output directory as {}", this.outputDirectory.getAbsolutePath());
	}

	/**
	 * Writes a set of values out to the specified writer using CSV notation.
	 *
	 * @param writerName the name of the writer to send the values to.
	 * @param values the values to write in a CSV format.
	 * @throws OutputManagerException if an error occurred writing the files.
	 */
	@Override
	public void writeRecords(String writerName, List<String> values) throws OutputManagerException {
		Tuple<File, Writer> writerTuple = this.writers.get(writerName);
		if (writerTuple == null) {
			throw new OutputManagerException("Attempt to write to non-existant writer: %1$s", writerName);
		}

		File writerFileName = writerTuple.getFirst();
		Writer writer = writerTuple.getSecond();
		String lineSeparator = System.getProperty("line.separator");

		String outputLine = collectionToString(values, ",", null);
		try {
			LOG.trace("Writing output {}: {}", writerFileName, outputLine);
			writer.write(outputLine);
			writer.write(lineSeparator);
		} catch (IOException ioe) {
			throw new OutputManagerException(ioe, "Unable to write to %1$s(%2$s): %3$s", writerName, writerFileName, outputLine);
		}
	}

}
