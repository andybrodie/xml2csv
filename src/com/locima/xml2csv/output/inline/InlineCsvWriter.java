package com.locima.xml2csv.output.inline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.output.ICsvWriter;
import com.locima.xml2csv.output.IExtractionResultsContainer;
import com.locima.xml2csv.output.IOutputManager;
import com.locima.xml2csv.output.OutputManagerException;
import com.locima.xml2csv.output.OutputUtil;
import com.locima.xml2csv.output.direct.DirectCsvWriter;
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

	private static final ExtractedField[] EMPTY_EXTRACTEDFIELD_ARRAY = new ExtractedField[0];

	private static final Logger LOG = LoggerFactory.getLogger(InlineCsvWriter.class);

	/**
	 * Records whether, when creating the CSV file, output should be appended to an existing file or create a new file or overwrite an existing file.
	 */
	private boolean appendOutput;

	private IMappingContainer container;

	/**
	 * The intermediate CSV output file that contains the name of each field before each record.
	 */
	private File csiOutputFile;

	/**
	 * The writer object that can be used to write to {@link #csiOutputFile}.
	 */
	private ObjectOutputStream csiWriter;

	/**
	 * The final CSV output file that will contain our desired output.
	 */
	private File csvOutputFile;

	/**
	 * The name of the output managed by this writer.
	 */
	private String outputName;

	/**
	 * TODO Tidies up (i.e. deletes) all intermediate files. Currently throws an exception as it's not implemented.
	 */
	@Override
	public void abort() {
		throw new UnsupportedOperationException();
		// TODO Auto-generated method stub
	}

	/**
	 * Writes a terminating <code>null</code> to the CSI output, then closes it and converts it to a CSV file.
	 */
	@Override
	public void close() throws OutputManagerException {
		// try {
		// this.csiWriter.write(FILE_TERMINATOR);
		// } catch (IOException ioe) {
		// throw new OutputManagerException(ioe, "Unexpected IOException when trying to write final null to CSI file");
		// }
		OutputUtil.close(this.outputName, this.csiOutputFile.getAbsolutePath(), this.csiWriter);
		convertCsiToCsv();
	}

	/**
	 * Converts the intermediate CSI file written as the mapping evaluation was going on to it's final CSV form.
	 * <p>
	 * Algorithm as follows:
	 * <ol>
	 * <li>Create the CSV file</li>
	 * <li>Determine the field names and write them to the CSV file (if appendOutput is false).</li>
	 * <li>For each record in the CSI file, convert to an array of values, using the value of {@link ExtractedField#getFieldName()} to determine the
	 * correct index.</li>
	 * <li>Convert the array to a CSV record and write to the CSV file.</li>
	 * <li>Close the CSV file.</li>
	 * </ol>
	 *
	 * @throws OutputManagerException if an error occurs whilst creating, openining, or closing the output CSV file.
	 */
	private void convertCsiToCsv() throws OutputManagerException {
		LOG.info("Converting output CSI file {} to output CSV file {}", this.csiOutputFile.getAbsolutePath(), this.csvOutputFile.getAbsolutePath());

		CsiInputStream csiInput = null;
		Writer csvWriter = null;
		try {
			csiInput = getCsiInput();
			csvWriter = OutputUtil.createCsvWriter(this.container, this.csvOutputFile, this.appendOutput);

			// Go through all the records
			ExtractedField[] record = csiInput.getNextRecord();
			while (record != null) {
				String outputCsvRecord = createOutputCsvRecord(record);
				try {
					csvWriter.write(outputCsvRecord);
				} catch (IOException ioe) {
					throw new OutputManagerException(ioe, "Unable to write output record to output CSV file %s (%s).  Data was: %s", this.outputName,
									this.csvOutputFile.getAbsolutePath(), outputCsvRecord);
				}
				record = csiInput.getNextRecord();
			}
		} finally {
			// Close the CSI input stream, log any errors but don't throw as this doesn't impact the overall behaviour of the program.
			if (csiInput != null) {
				try {
					csiInput.close();
				} catch (IOException ioe) {
					LOG.warn("Was unable to close csiInput", ioe);
				}
			}
			if (csvWriter != null) {
				try {
					csvWriter.flush();
					csvWriter.close();
				} catch (IOException ioe) {
					throw new OutputManagerException(ioe, "Unable to flush and close CSV output file %s (%s)", this.outputName,
									this.csvOutputFile.getAbsolutePath());
				}
			}
		}
	}

	private ObjectOutputStream createCsiOutput() throws OutputManagerException {
		File file = this.csiOutputFile;
		LOG.info("Creating csiWriter for {}", file.getAbsolutePath());
		try {
			ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file, this.appendOutput));
			LOG.info("Successfully opened output file for csiWriter {}", file.getAbsolutePath());
			return outputStream;
		} catch (IOException ioe) {
			// If we can't even create an output file, throw an exception up to abort
			throw new OutputManagerException(ioe, "Unable to create output file %s", file.getAbsolutePath());
		}
	}

	/**
	 * Converts an array of {@link ExtractedField} instances to the final output CSV record.
	 * <p>
	 * This method uses the {@link ExtractedField#getFieldName()} value, in co-ordination with {@link #container} attribute to work out the position
	 * of each field within the output CSV record. This is required because CSV records have fixed field positions for any given field, whereas the
	 * input array of {@link ExtractedField} doesn't care about field positions, it's just an ordered list of values that exist
	 * <em>for this record only</em>.
	 *
	 * @param efRecord an ordered list of fields.
	 * @return a CSV record with field positions correctly allocated (i.e. blank fields inserted where record doesn't contain values) and fully
	 *         escaped using {@link StringUtil#escapeForCsv(Object)}.
	 */
	private String createOutputCsvRecord(ExtractedField[] efRecord) {
		StringBuilder csvRecord = new StringBuilder();
		int lastIndex = 0;
		for (ExtractedField field : efRecord) {
			// If the next field to write is not the next one, then insert commas to indicate empty fields until the index is right.
			int fieldIndex = getFieldIndexWithinCsv(field);
			int emptyFieldsRequiredCount = fieldIndex - lastIndex;
			if (emptyFieldsRequiredCount > 1) {
				for (int i = 0; i < (emptyFieldsRequiredCount - 1); i++) {
					csvRecord.append(',');
				}
			}
			lastIndex = fieldIndex;
			csvRecord.append(StringUtil.escapeForCsv(field.getFieldValue()));
		}
		return csvRecord.toString();
	}

	private CsiInputStream getCsiInput() throws OutputManagerException {
		try {
			LOG.info("Re-opening {} to read intermediate file", this.csiOutputFile.getAbsolutePath());
			return new CsiInputStream(new FileInputStream(this.csiOutputFile));
		} catch (IOException e) {
			throw new OutputManagerException(e, "Unable to open CSI file {} for reading.", this.csiOutputFile.getAbsolutePath());
		}
	}

	/**
	 * Works out the index, relative to a CSV field, that a specific extracted field should appear in.
	 *
	 * @param field the field to obtain the index of.
	 * @return an index.
	 */
	private int getFieldIndexWithinCsv(ExtractedField field) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void initialise(File outputDirectory, IMappingContainer container, boolean appendOutput) throws OutputManagerException {
		this.outputName = container.getContainerName();
		String csiFileNameBasis = this.outputName + ".csi";
		this.csiOutputFile = new File(outputDirectory, FileUtility.convertToPOSIXCompliantFileName(csiFileNameBasis, true));
		this.container = container;
		this.csiWriter = createCsiOutput();

		String csvFileNameBasis = this.outputName + ".csv";
		this.csvOutputFile = new File(outputDirectory, FileUtility.convertToPOSIXCompliantFileName(csvFileNameBasis, true));
		/* Append output is only relevant to the output CSV file, so store it away until required */
		this.appendOutput = appendOutput;
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
	public void writeRecords(IExtractionResultsContainer context) throws OutputManagerException {
		Iterator<List<ExtractedField>> recordIterator = new InlineOutputRecordIterator(context);
		while (recordIterator.hasNext()) {
			List<ExtractedField> record = recordIterator.next();
			ExtractedField[] efArray = record.toArray(EMPTY_EXTRACTEDFIELD_ARRAY);
			try {
				if (LOG.isTraceEnabled()) {
					LOG.trace("Writing output {}: {}", this.csiOutputFile.getAbsolutePath(), StringUtil.toString(efArray));
				}
				this.csiWriter.writeObject(efArray);
			} catch (IOException ioe) {
				throw new OutputManagerException(ioe, "Unable to write to %1$s(%2$s): %3$s", this.outputName, this.csiOutputFile.getAbsolutePath(),
								StringUtil.toString(efArray));
			}
		}
	}

	// @Override
	// public void writeRecords(Iterable<List<ExtractedField>> records) throws OutputManagerException {
	// for (List<ExtractedField> record : records) {
	// String outputLine = InlineCsvWriter.toCsvRecord(record);
	// try {
	// if (LOG.isTraceEnabled()) {
	// LOG.trace("Writing output {}: {}", this.csiOutputFile.getAbsolutePath(), outputLine);
	// }
	// this.csiWriter.write(outputLine);
	// this.csiWriter.write(StringUtil.getLineSeparator());
	// } catch (IOException ioe) {
	// throw new OutputManagerException(ioe, "Unable to write to %1$s(%2$s): %3$s", this.outputName, this.csiOutputFile.getAbsolutePath(),
	// outputLine);
	// }
	// }
	// }

}
