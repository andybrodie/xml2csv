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
import com.locima.xml2csv.model.IMappingContainer;
import com.locima.xml2csv.model.MappingConfiguration;
import com.locima.xml2csv.model.RecordSet;

public class DirectCsvWriter extends AbstractCsvWriter {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private static final Logger LOG = LoggerFactory.getLogger(DirectCsvWriter.class);

	private File directory;

	private Map<String, Tuple<File, Writer>> writers;

	private MappingConfiguration mappingConfig;

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
	 * @param config the mapping configuration that determines what outputs will be written. Must not be null.
	 * @param appendOutput true if output should be appended to existing files, false if new files should overwrite existing ones.
	 * @throws OutputManagerException if an unrecoverable error occurs whilst creating the output files or writing to them.
	 */
	@Override
	public void initialise(MappingConfiguration config, boolean appendOutput) throws OutputManagerException {
//		this.writers = new HashMap<String, Tuple<File, Writer>>();
//		this.mappingConfig = config;
//		final String encoding = "UTF8";
//
//		List<File> outputsWithNames = getOutputFileNames(directory, config);
//		LOG.info("Creating {} writers in {}", outputsWithNames.size(), this.directory.getAbsolutePath());
//		try {
//			for (File writerFile : outputsWithNames) {
//				// needToWriteHeaders controls whether a first record of column names is written. Set to false if appending to existing file.
//				boolean needToWriteHeaders = true;
//				if (appendOutput && writerFile.exists()) {
//					LOG.debug("Appending to existing file for writer {} ({})", writerName, writerFile.getAbsolutePath());
//					needToWriteHeaders = false;
//				} else {
//					LOG.debug("Creating output file for writer {} ({})", writerName, writerFile.getAbsolutePath());
//				}
//				Writer writerFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writerFile, appendOutput), encoding));
//
//				this.writers.put(writerName, new Tuple<File, Writer>(writerFile, writerFileWriter));
//				LOG.info("Successfully opened output file for writer {} ({})", writerName, writerFile.getAbsolutePath());
//
//				// Write field names to the first row of the file, if not appending.
//				if (needToWriteHeaders) {
//					try {
//						writerFileWriter.write(com.locima.xml2csv.StringUtil.collectionToString(entry.getValue(), ",", null));
//						writerFileWriter.write(LINE_SEPARATOR);
//					} catch (IOException ioe) {
//						close();
//						throw new OutputManagerException("Unable to write field names to " + writerName, ioe);
//					}
//				}
//			}
//		} catch (FileNotFoundException fileNotFoundException) {
//			// If we can't even create an output file, throw an exception up to abort
//			throw new OutputManagerException(fileNotFoundException, "Unable to create output file %s", writerFile.getAbsolutePath());
//		} catch (UnsupportedEncodingException uee) {
//			// This should never happen as we're hard-coding a known supported encoding in Java
//			throw new IllegalStateException("Unexpected unsupported encoding exception: " + encoding, uee);
//		}

	}

	
	/**
	 * Writes a set of values out to the specified writer using CSV notation.
	 *
	 * @param writerName the name of the writer to send the values to.
	 * @param data the values to write in a CSV format.
	 * @throws OutputManagerException if an error occurred writing the files.
	 */
	@Override
	public void writeRecords(RecordSet data) throws OutputManagerException {
		// Tuple<File, Writer> writerTuple = this.writers.get(writerName);
		// if (writerTuple == null) {
		// throw new OutputManagerException("Attempt to write to non-existant writer: %1$s", writerName);
		// }
		//
		// File writerFileName = writerTuple.getFirst();
		// Writer writer = writerTuple.getSecond();
		//
		// for (List<String> record : getRecords(data)) {
		// String outputLine = collectionToString(record, ",", null);
		// try {
		// LOG.trace("Writing output {}: {}", writerFileName, outputLine);
		// writer.write(outputLine);
		// writer.write(LINE_SEPARATOR);
		// } catch (IOException ioe) {
		// throw new OutputManagerException(ioe, "Unable to write to %1$s(%2$s): %3$s", writerName, writerFileName, outputLine);
		// }
		// }
	}
	

}
