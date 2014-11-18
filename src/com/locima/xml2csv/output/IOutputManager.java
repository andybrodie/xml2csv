package com.locima.xml2csv.output;

import java.io.File;

import com.locima.xml2csv.model.MappingConfiguration;
import com.locima.xml2csv.model.RecordSet;

/**
 * Provided ONLY for unit testing.
 */
public interface IOutputManager {

	/**
	 * Finalises all the output files.
	 * @throws OutputManagerException if an error occurs whilst closing an output file.
	 */
	void close() throws OutputManagerException;

	/**
	 * Creates the CSV files that will be used for the different writers.
	 *
	 * @param config the mapping configuration that determines what outputs will be written. Must not be null.
	 * @param appendOutput true if output should be appended to existing files, false if new files should overwrite existing ones.
	 * @throws OutputManagerException if an unrecoverable error occurs whilst creating the output files or writing to them.
	 */
	void initialise(MappingConfiguration config, boolean appendOutput) throws OutputManagerException;

	/**
	 * Sets the directory to which output files will be written.
	 *
	 * @param outputDirectoryName the name of the output directory. Directory must exist and be writeable.
	 * @throws OutputManagerException If the directory does not exist.
	 */
	void setDirectory(File outputDirectoryName) throws OutputManagerException;

	void writeRecords(String writerName, RecordSet records) throws OutputManagerException;

}
