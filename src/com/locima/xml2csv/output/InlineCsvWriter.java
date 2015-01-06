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

import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.extractor.ExtractedField;
import com.locima.xml2csv.util.FileUtility;
import com.locima.xml2csv.util.StringUtil;

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

	/**
	 * Converts an ordered collection of {@link ExtractedField} instances to the intermediate record format, ready for writing to the
	 * {@link #csiOutputFile} file.
	 *
	 * @param inputCollection a collection of name/value pairs.
	 * @return a (possibly empty) string.
	 */
	public static String toCsvRecord(List<ExtractedField> inputCollection) {
		return StringUtil.toString(inputCollection, ",", new StringUtil.IConverter<ExtractedField>() {

			@Override
			public String convert(ExtractedField input) {
				if (input == null) {
					return null;
				} else {
					return StringUtil.escapeForCsv(input.getFieldName()) + "," + StringUtil.escapeForCsv(input.getFieldValue());
				}
			}

		});
	}

	private IMappingContainer container;

	/**
	 * The intermediate CSV output file that contains the name of each field before each record.
	 */
	private File csiOutputFile;

	/**
	 * The writer object that can be used to write to {@link #csiOutputFile}.
	 */
	private Writer csiWriter;

	/**
	 * The final CSV output file that will contain our desired output.
	 */
	private File csvOutputFile;

	/**
	 * The name of the output managed by this writer.
	 */
	private String outputName;

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

	private void convertCsiToCsv() {
		LOG.info("Converting output CSI file {} to output CSV file {}", this.csiOutputFile.getAbsolutePath(), this.csvOutputFile.getAbsolutePath());
		/*
		 * 1. Create the CSV file 2. Add all the headers 3. Open the CSI file 4. Read in a CSI record 5. Write out the appropriate CSV record. 6.
		 * Close
		 */
		throw new UnsupportedOperationException();
	}

	private Writer createWriter(IMappingContainer container, File file, boolean appendOutput) throws OutputManagerException {
		LOG.info("Creating csiWriter for {}", file.getAbsolutePath());
		this.container = container;
		final String encoding = "UTF8";
		try {
			boolean createdNew = !file.exists();
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, appendOutput), encoding));
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
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("InlineCsvWriter(");
		sb.append(this.outputName);
		sb.append(", ");
		sb.append(this.csiOutputFile);
		sb.append(", ");
		sb.append(this.csvOutputFile);
		sb.append(")");
		return sb.toString();
	}

	@Override
	public void writeRecords(Iterable<List<ExtractedField>> records) throws OutputManagerException {
		for (List<ExtractedField> record : records) {
			String outputLine = InlineCsvWriter.toCsvRecord(record);
			try {
				if (LOG.isTraceEnabled()) {
					LOG.trace("Writing output {}: {}", this.csiOutputFile.getAbsolutePath(), outputLine);
				}
				this.csiWriter.write(outputLine);
				this.csiWriter.write(StringUtil.getLineSeparator());
			} catch (IOException ioe) {
				throw new OutputManagerException(ioe, "Unable to write to %1$s(%2$s): %3$s", this.outputName, this.csiOutputFile.getAbsolutePath(),
								outputLine);
			}
		}
	}

}
