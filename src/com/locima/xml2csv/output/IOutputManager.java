package com.locima.xml2csv.output;

import java.util.List;
import java.util.Map;

/**
 * Provided ONLY for unit testing.
 */
public interface IOutputManager {

	/**
	 * Close all the writers managed by this class. All exceptions are suppressed as there's nothing we're going to do about it anyway.
	 */
	void close();

	/**
	 * Creates the CSV files that will be used for the different writers.
	 *
	 * @param outputConfiguration a map of output names (used for file names within the output directory) and the columns or fields that will be
	 *            present in each one.
	 * @throws OutputManagerException if an unrecoverable error occurs whilst creating the output files or writing to them.
	 */
	void createFiles(Map<String, List<String>> outputConfiguration) throws OutputManagerException;

	/**
	 * Sets the directory to which output files will be written.
	 *
	 * @param outputDirectoryName the name of the output directory.
	 * @throws OutputManagerException If the directory does not exist
	 */
	void setDirectory(String outputDirectoryName) throws OutputManagerException;

	/**
	 * Writes a set of values out to the specified writer.
	 *
	 * @param writerName the name of the writer to send the values to.
	 * @param values the values to write.
	 * @throws OutputManagerException if an error occurred writing the files.
	 */
	void writeRecords(String writerName, List<String> values) throws OutputManagerException;

}
