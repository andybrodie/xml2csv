package com.locima.xml2csv.output;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.StringUtil;
import com.locima.xml2csv.model.RecordSet;

public class DirectCsvWriter extends AbstractCsvWriter {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private static final Logger LOG = LoggerFactory.getLogger(DirectCsvWriter.class);

	/**
	 * Writes the first row of each file to contain the comma-separated field names.
	 */
	public void writeFieldNames() throws OutputManagerException {
		for (OutputEntry entry : this.outputs) {
			try {
				Writer writer = entry.getWriter();
				List<String> fieldNames = entry.getContainer().getFieldNames(null, 0);
				List<String> escapedFieldNames = StringUtil.toStringList(fieldNames, new StringUtil.IConverter<String>() {
					public String convert(String input) {
						return StringUtil.escapeForCsv(input);
					}
				});
				writer.write(StringUtil.collectionToString(escapedFieldNames, ",", null));
				writer.write(LINE_SEPARATOR);
			} catch (IOException ioe) {
				close();
				throw new OutputManagerException(ioe, "Unable to write field names to %s (%s)", entry.getOutputName(),
								entry.getOutputFile().getAbsolutePath());
			}
		}
	}

	/**
	 * Writes a set of values out to the specified writer using CSV notation.
	 *
	 * @param writerName the name of the writer to send the values to.
	 * @param data the values to write in a CSV format.
	 * @throws OutputManagerException if an error occurred writing the files.
	 */
	@Override
	public void writeRecords(String writerName, RecordSet data) throws OutputManagerException {
//		for (List<String> record : getRecords(data)) {
//			String outputLine = collectionToString(record, ",", null);
//			try {
//				LOG.trace("Writing output {}: {}", writerFileName, outputLine);
//				writer.write(outputLine);
//				writer.write(LINE_SEPARATOR);
//			} catch (IOException ioe) {
//				throw new OutputManagerException(ioe, "Unable to write to %1$s(%2$s): %3$s", writerName, writerFileName, outputLine);
//			}
//		}
	}

}
