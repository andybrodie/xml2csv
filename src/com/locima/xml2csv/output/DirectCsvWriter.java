package com.locima.xml2csv.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.FileUtility;
import com.locima.xml2csv.StringUtil;
import com.locima.xml2csv.model.IMappingContainer;
import com.locima.xml2csv.model.RecordSet;

/**
 * Manages the output of a single CSV file where the results of conversion from XML when the mapping configuration prohibits a variable number of
 * fields in a record.
 * <p>
 * We want to use this {@link IOutputManager} whenever possible as this is much faster to work because it writes CSV files directly. If the number of
 * fields in a CSV depends on the result of mappings (i.e. executing an XPath statement yields n results in an inline mapping) then we have to use a
 * {@link InlineCsvWriter} instead.
 */
public class DirectCsvWriter implements ICsvWriter {

	private static final Logger LOG = LoggerFactory.getLogger(DirectCsvWriter.class);

	private File outputFile;

	private String outputName;

	private Writer writer;

	@Override
	public void close() {
		LOG.info("Flushing and closing writer for {} writer in {}", this.outputName, this.outputFile.getAbsolutePath());
		try {
			LOG.debug("Flushing data for writer {} ({})", this.outputName, this.outputFile.getAbsolutePath());
			this.writer.flush();
		} catch (IOException ioe) {
			// No point throwing the error up as there's no useful action to be taken at this point
			LOG.error("Error flushing data to writer", ioe);
		}
		try {
			LOG.info("Closing writer {} ({})", this.outputName, this.outputFile.getAbsolutePath());
			this.writer.close();
		} catch (IOException ioe) {
			// No point throwing the error up as there's no useful action to be taken at this point
			LOG.error("Error closing writer", ioe);
		}
	}

	private Writer createWriter(IMappingContainer container, File file, boolean appendOutput) throws OutputManagerException {
		LOG.info("Creating writer for {}", file.getAbsolutePath());
		final String encoding = "UTF8";
		try {
			boolean createdNew = !file.exists();
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, appendOutput), encoding));
			if (createdNew) {
				writeFieldNames(container, writer);
			} else {
				LOG.info("File {} already exists, therefore not writing field names", file.getAbsolutePath());
			}
			LOG.info("Successfully opened output file for writer {}", file.getAbsolutePath());
			return writer;
		} catch (FileNotFoundException fileNotFoundException) {
			// If we can't even create an output file, throw an exception up to abort
			throw new OutputManagerException(fileNotFoundException, "Unable to create output file %s", file.getAbsolutePath());
		} catch (UnsupportedEncodingException uee) {
			// This should never happen as we're hard-coding a known supported encoding in Java
			throw new IllegalStateException("Unexpected unsupported encoding exception: " + encoding, uee);
		}
	}

	/**
	 * @param config the mapping configuration that determines what outputs will be written. Must not be null.
	 * @param appendOutput true if output should be appended to existing files, false if new files should overwrite existing ones.
	 * @throws OutputManagerException if an unrecoverable error occurs whilst creating the output files or writing to them.
	 */
	@Override
	public void initialise(File outputDirectory, IMappingContainer container, boolean appendOutput) throws OutputManagerException {
		this.outputName = container.getContainerName();
		String fileNameBasis = this.outputName + ".csv";
		this.outputFile = new File(outputDirectory, FileUtility.convertToPOSIXCompliantFileName(fileNameBasis,true));
		this.writer = createWriter(container, this.outputFile, appendOutput);
	}

	/**
	 * Writes the first row of each file to contain the comma-separated field names.
	 */
	public void writeFieldNames(IMappingContainer container, Writer newWriter) throws OutputManagerException {
		try {
			List<String> fieldNames = container.getFieldNames(null, 0);
			String escapedFieldNames = StringUtil.toCsvRecord(fieldNames);
			LOG.info("Writing field names to {}: {}", this.outputName, escapedFieldNames);
			newWriter.write(escapedFieldNames);
			newWriter.write(StringUtil.getLineSeparator());
		} catch (IOException ioe) {
			close();
			throw new OutputManagerException(ioe, "Unable to write field names to %s (%s)", this.outputName, this.outputFile.getAbsolutePath());
		}
	}

	/**
	 * Writes a set of values out to the specified writer using CSV notation.
	 *
	 * @param data the values to write in a CSV format.
	 * @throws OutputManagerException if an error occurred writing the files.
	 */
	@Override
	public void writeRecords(RecordSet data) throws OutputManagerException {
		for (List<String> record : data) {
			String outputLine = StringUtil.toCsvRecord(record);
			try {
				LOG.trace("Writing output {}: {}", this.outputFile.getAbsolutePath(), outputLine);
				this.writer.write(outputLine);
				this.writer.write(StringUtil.getLineSeparator());
			} catch (IOException ioe) {
				throw new OutputManagerException(ioe, "Unable to write to %1$s(%2$s): %3$s", this.outputFile.getAbsolutePath(), outputLine);
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DirectCsvWriter(");
		sb.append(this.outputName);
		sb.append(", ");
		sb.append(this.outputFile);
		sb.append(")");
		return sb.toString();
	}

}
