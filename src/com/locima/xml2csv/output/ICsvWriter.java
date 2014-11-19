package com.locima.xml2csv.output;

import java.io.File;

import com.locima.xml2csv.model.IMappingContainer;
import com.locima.xml2csv.model.RecordSet;

public interface ICsvWriter {

	/**
	 * Finalises the output CSV file.
	 *
	 * @throws OutputManagerException if an error occurs whilst closing an output file.
	 */
	void close() throws OutputManagerException;

	/**
	 * Initialises this output manager so that it's ready to receive outputs via {@link #writeRecords(String, RecordSet)}.
	 *
	 * @param container the mapping configuration that determines what fields will be written to the CSV file.
	 * @param appendOutput true if output should be appended to an existing files (if present), false if we should overwrite an existing file.
	 * @param outputDirectory the name of the output directory. Directory must exist and be writeable.
	 * @throws OutputManagerException if an unrecoverable error occurs whilst initialising the output file.
	 */
	void initialise(File outputDirectory, IMappingContainer container, boolean appendOutput) throws OutputManagerException;

	/**
	 * Writes the records created by the XML data extractor to the output file managed by this instance
	 *
	 * @param records the records to write out.
	 * @throws OutputManagerException if an unrecoverable error occurs whilst writing to the output file.
	 */
	void writeRecords(RecordSet records) throws OutputManagerException;

}
