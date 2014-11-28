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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.FileUtility;
import com.locima.xml2csv.Tuple;
import com.locima.xml2csv.model.ExtractedField;
import com.locima.xml2csv.model.IMapping;
import com.locima.xml2csv.model.IMappingContainer;
import com.locima.xml2csv.model.RecordSet;

/**
 * Manages the output for a single CSV file where the results of conversion from XML when the mapping configuration contains a variable number of
 * fields in a record.
 * <p>
 * If the number of fields in a CSV depends on the result of mappings (i.e. executing an XPath statement yields n results in an inline mapping) then
 * we cannot write a CSV file directly. This {@link IOutputManager} does this by writing an intermediate file which is then converted to a CSV file
 * once the number of fields required is known.
 * <p>
 * If the number of fields in a CSV is known from the configuration (either because it contains no inline mappings or they have a fixed cardinality)
 * then {@link DirectCsvWriter} should be used as it's much faster.
 */
public class InlineCsvWriter implements ICsvWriter {

	private static final Logger LOG = LoggerFactory.getLogger(InlineCsvWriter.class);

	private File csiOutputFile;
	private String outputName;
	private Writer csiWriter;

	private File csvOutputFile;

	@Override
	public void abort() {
		throw new UnsupportedOperationException();
		// TODO Auto-generated method stub
	}

	@Override
	public void close() throws OutputManagerException {
		closeFile(this.outputName, this.csiOutputFile.getAbsolutePath(), this.csiWriter);
		
		convertCsiToCsv();
	}

	private void convertCsiToCsv() {
		// Base the CSV file name off of the CSI filename by just changing the last letter of the extension.
		LOG.info("Converting output CSI file {} to output CSV file {}", this.csiOutputFile.getAbsolutePath(), this.csvOutputFile.getAbsolutePath());
		throw new UnsupportedOperationException();
	}

	private void closeFile(String outputName, String fileName, Writer writer) throws OutputManagerException {
		LOG.info("Flushing and closing csiWriter for {} csiWriter in {}", outputName, fileName);
		try {
			LOG.debug("Flushing data for csiWriter {} ({})", outputName, fileName);
			this.csiWriter.flush();
		} catch (IOException ioe) {
			throw new OutputManagerException(ioe, "Unable to flush output file %s", fileName);
		}
		try {
			LOG.info("Closing csiWriter {} ({})", this.outputName, fileName);
			this.csiWriter.close();
		} catch (IOException ioe) {
			throw new OutputManagerException(ioe, "Unable to close output file %s", fileName);
		}
	}

	private Writer createWriter(IMappingContainer container, File file, boolean appendOutput) throws OutputManagerException {
		LOG.info("Creating csiWriter for {}", file.getAbsolutePath());
		final String encoding = "UTF8";
		try {
			boolean createdNew = !file.exists();
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, appendOutput), encoding));
			if (createdNew) {
				CsvWriterUtil.writeFieldNames(this.outputName, container, writer);
			} else {
				LOG.info("File {} already exists, therefore not writing field names", file.getAbsolutePath());
			}
			LOG.info("Successfully opened output file for csiWriter {}", file.getAbsolutePath());
			return writer;
		} catch (FileNotFoundException fileNotFoundException) {
			// If we can't even create an output file, throw an exception up to abort
			throw new OutputManagerException(fileNotFoundException, "Unable to create output file %s", file.getAbsolutePath());
		} catch (UnsupportedEncodingException uee) {
			// This should never happen as we're hard-coding a known supported encoding in Java
			throw new IllegalStateException("Unexpected unsupported encoding exception: " + encoding, uee);
		}
	}

	@Override
	public void initialise(File outputDirectory, IMappingContainer container, boolean appendOutput) throws OutputManagerException {
		this.outputName = container.getContainerName();
		String csiFileNameBasis = this.outputName + ".csi";
		this.csiOutputFile = new File(outputDirectory, FileUtility.convertToPOSIXCompliantFileName(csiFileNameBasis, true));
		this.csiWriter = createWriter(container, this.csiOutputFile, appendOutput);

		String csvFileNameBasis = this.outputName + ".csv";
		this.csvOutputFile = new File(outputDirectory, FileUtility.convertToPOSIXCompliantFileName(csvFileNameBasis, true));
	}

	@Override
	public void writeRecords(RecordSet records) throws OutputManagerException {

		for (List<ExtractedField> fields : records) {
			throw new UnsupportedOperationException();
		}
		
	}

}
