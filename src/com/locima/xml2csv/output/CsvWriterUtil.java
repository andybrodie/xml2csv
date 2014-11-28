package com.locima.xml2csv.output;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.StringUtil;
import com.locima.xml2csv.model.IMappingContainer;

public class CsvWriterUtil {

	private static final Logger LOG = LoggerFactory.getLogger(CsvWriterUtil.class);

	/**
	 * Writes the field names that exist within <code>container</code> to the <code>writer</code> passed.
	 *
	 * @param outputName the name of the output that we are writing to (included purely for logging).
	 * @param container the mapping container that we are wanting to write the field names for.
	 * @param writer the writer to write the field names to.
	 */
	public static void writeFieldNames(String outputName, IMappingContainer container, Writer writer) throws OutputManagerException {
		try {
			List<String> fieldNames = container.getFieldNames(null, 0);
			String escapedFieldNames = StringUtil.toCsvRecord(fieldNames);
			LOG.info("Writing field names to {}: {}", outputName, escapedFieldNames);
			writer.write(escapedFieldNames);
			writer.write(StringUtil.getLineSeparator());
		} catch (IOException ioe) {
			throw new OutputManagerException(ioe, "Unable to write field names to %s", outputName);
		}
	}

	/**
	 * Prevents instantiation.
	 */
	private CsvWriterUtil() {
	}

}
