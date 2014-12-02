package com.locima.xml2csv.output;

import java.io.File;

import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.extractor.ExtractedRecordList;

public interface ICsvWriter {

	/**
	 * Causes this writer to abort, releasing as many resources as possible. No exceptions should be thrown from this method, only output log entries
	 * for problems.
	 * <p>
	 * This is not the same as {@link #close()}, which may attempt significant processing to bring everything to a graceful conclusion (especially in
	 * the case of {@link InlineCsvWriter#close()}.
	 */
	void abort();

	/**
	 * Finalises the output CSV file.
	 *
	 * @throws OutputManagerException if an error occurs whilst closing an output file.
	 */
	void close() throws OutputManagerException;

	/**
	 * Initialises this output manager so that it's ready to receive outputs via {@link #writeRecords(String, ExtractedRecordList)}.
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
	void writeRecords(ExtractedRecordList records) throws OutputManagerException;

}
