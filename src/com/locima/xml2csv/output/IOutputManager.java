package com.locima.xml2csv.output;

import java.io.File;
import java.util.List;

import com.locima.xml2csv.configuration.MappingConfiguration;
import com.locima.xml2csv.extractor.ContainerExtractionContext;
import com.locima.xml2csv.extractor.ExtractedField;

/**
 * Manages writing a set of CSV files for a specific {@link MappingConfiguration}.
 */
public interface IOutputManager {

	/**
	 * Causes this output manager to abort all the writers, releasing as many resources as possible. No exceptions should be thrown from this method,
	 * only output log entries for problems.
	 * <p>
	 * This is not the same as {@link #close()}, which may attempt significant processing to bring everything to a graceful conclusion.
	 */
	void abort();

	/**
	 * Finalises all the writers that this manager is managing.
	 *
	 * @throws OutputManagerException if an error occurs whilst closing any of the {@link ICsvWriter}.
	 */
	void close() throws OutputManagerException;

	/**
	 * Initialises this output manager so that it's ready to receive outputs via {@link #writeRecords(String, ExtractedRecordList)}.
	 *
	 * @param config the mapping configuration that determines what fields will be written to the CSV file.
	 * @param outputDirectory the directory that all outputs will be written to.
	 * @param appendOutput true if output should be appended to an existing files (if present), false if we should overwrite an existing file.
	 * @throws OutputManagerException if an unrecoverable error occurs whilst initialising the output file.
	 */
	void initialise(File outputDirectory, MappingConfiguration config, boolean appendOutput) throws OutputManagerException;

	/**
	 * Writes the records created by the XML data extractor to the output file managed by this instance
	 *
	 * @param iter the extracted data from the XML document that relate to a single output.
	 * @throws OutputManagerException if an unrecoverable error occurs whilst writing to the output file.
	 */
	void writeRecords(String outputName, Iterable<List<ExtractedField>> iter) throws OutputManagerException;

}
