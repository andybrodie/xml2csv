package com.locima.xml2csv.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.configuration.IValueMapping;
import com.locima.xml2csv.configuration.MappingIndexAncestors;
import com.locima.xml2csv.configuration.MultiValueBehaviour;
import com.locima.xml2csv.util.StringUtil;
import com.locima.xml2csv.util.Tuple;

/**
 * Utility methods required by both the direct and inline CSV writers.
 */
public class OutputUtil {

	/**
	 * The character encoding that will be used for all files.
	 */
	private static final String ENCODING = "UTF-8";

	private static final Logger LOG = LoggerFactory.getLogger(OutputUtil.class);

	public static void close(String outputName, String fileName, OutputStream outputStream) throws OutputManagerException {
		LOG.info("Closing {} ({})", outputName, fileName);
		try {
			outputStream.close();
			LOG.info("Successfully closed {} ({})", outputName, fileName);
		} catch (IOException ioe) {
			throw new OutputManagerException(ioe, "Unable to close output stream %s", fileName);
		}
	}

	public static void close(String outputName, String fileName, Writer writer) throws OutputManagerException {
		LOG.info("Closing writer for {} ({})", outputName, fileName);
		try {
			writer.close();
		} catch (IOException ioe) {
			throw new OutputManagerException(ioe, "Unable to close output writer %s", fileName);
		}
		LOG.info("Successfully closed {} ({})", outputName, fileName);
	}

	public static Writer createCsvWriter(IMappingContainer container, File file, boolean appendOutput) throws OutputManagerException {
		LOG.info("Creating writer for {}", file.getAbsolutePath());
		try {
			boolean createdNew = !file.exists();
			Writer writer = createWriter(file, appendOutput);
			if (appendOutput && !createdNew) {
				LOG.info("Existing file will be appended to, therefore not writing field names", file.getAbsolutePath());
			} else {
				OutputUtil.writeFieldNames(container, writer);
			}
			LOG.info("Successfully opened output file for writer {}", file.getAbsolutePath());
			return writer;
		} catch (OutputManagerException ome) {
			// Possibly thrown by writeFieldNames
			throw ome;
		}
	}

	public static Writer createWriter(File file, boolean appendOutput) throws OutputManagerException {
		LOG.debug("Creating writer for {}", file.getAbsolutePath());
		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, appendOutput), ENCODING));
			LOG.info("Created writer for {}", file.getAbsolutePath());
			return writer;
		} catch (FileNotFoundException fileNotFoundException) {
			// If we can't even create an output file, throw an exception up to abort
			throw new OutputManagerException(fileNotFoundException, "Unable to create output file %s", file.getAbsolutePath());
		} catch (UnsupportedEncodingException uee) {
			// This should never happen as we're hard-coding a known supported encoding in Java
			throw new IllegalStateException("Unexpected unsupported encoding exception: " + ENCODING, uee);
		}
	}

	/**
	 * Retrieve the field names for the mapping container passed.
	 *
	 * @param fieldNames the list of field names that this method should add to.
	 * @return the number of field names that this method added.
	 */
	private static List<String> getFieldNames(IMappingContainer container) {
		List<String> fieldNames = new ArrayList<String>();
		MappingIndexAncestors parentContext = new MappingIndexAncestors();
		getFieldNames(fieldNames, parentContext, container);
		return fieldNames;
	}

	/**
	 * Recursive implementation of {@link #getFieldNames}. This ensures that the parent iteration count is available.
	 *
	 * @param fieldNames the list of column names that is being built up.
	 * @param parentContext a stack of parent name/iteration pairs ({@link Tuple}) that form the ancestor chain of this mapping.
	 * @return the number of columns added by this invocation.
	 */
	private static int getFieldNames(List<String> fieldNames, MappingIndexAncestors parentContext, IMappingContainer container) {
		/*
		 * If this is a non-nested MappingList, i.e. a direct child of MappingConfiguration then the instance count refers to the number of records
		 * output, not the number of fields (as a nested, in-line MappingList would indicate. Therefore, only process as in-line if nested.
		 */
		int repeats = (container.getMultiValueBehaviour() == MultiValueBehaviour.LAZY) ? 1 : container.getFieldCountForSingleRecord();

		LOG.info("Need {} repeats for container {}", repeats, container);
		String name = container.getContainerName();
		int fieldCount = 0;
		for (int containerIteration = 0; containerIteration < repeats; containerIteration++) {
			parentContext.push(name, containerIteration);
			for (IMapping mapping : container) {
				int extraFieldCount;
				if (mapping instanceof IMappingContainer) {
					extraFieldCount = getFieldNames(fieldNames, parentContext, (IMappingContainer) mapping);
				} else {
					extraFieldCount = getFieldNames(fieldNames, parentContext, (IValueMapping) mapping);
				}
				fieldCount += extraFieldCount;
				LOG.info("Added {} fields to fieldCount making a total of {} fields", extraFieldCount, fieldCount);
			}
			parentContext.pop();
		}
		return fieldCount;
	}

	private static int getFieldNames(List<String> fieldNames, MappingIndexAncestors parentContext, IValueMapping mapping) {
		/*
		 * The number of fields output is the maximum number of values found in a single execution of this mapping, constrained by this.minValueCount
		 * and this.maxValueCount. Don't need to consider maxValueCount here though as evaluation is halted once we have enough values to meet
		 * maxValueCount.
		 */
		int repeats = Math.max(mapping.getMinValueCount(), 1);
		int fieldCount;
		switch (mapping.getMultiValueBehaviour()) {
			case LAZY:
				fieldNames.add(mapping.getNameFormat().format(mapping.getBaseName(), 0, parentContext));
				fieldCount = 1;
				break;
			case GREEDY:
				for (fieldCount = 0; fieldCount < repeats; fieldCount++) {
					fieldNames.add(mapping.getNameFormat().format(mapping.getBaseName(), fieldCount, parentContext));
				}
				break;
			default:
				throw new IllegalStateException("Unexpected MultiValueBehaviour: " + mapping.getMultiValueBehaviour());
		}
		return fieldCount;
	}

	/**
	 * Writes the field names that exist within <code>container</code> to the <code>writer</code> passed.
	 *
	 * @param container the mapping container that we are wanting to write the field names for.
	 * @param writer the writer to write the field names to.
	 */
	public static void writeFieldNames(IMappingContainer ctx, Writer writer) throws OutputManagerException {
		try {
			List<String> fieldNames = getFieldNames(ctx);
			String escapedFieldNames = StringUtil.toCsvRecord(fieldNames);
			LOG.info("Writing field names to {}: {}", ctx.getContainerName(), escapedFieldNames);
			writer.write(escapedFieldNames);
			writer.write(StringUtil.getLineSeparator());
		} catch (IOException ioe) {
			throw new OutputManagerException(ioe, "Unable to write field names for container %s", ctx.getContainerName());
		}
	}

	/**
	 * Prevents instaniation.
	 */
	private OutputUtil() {
	}

}
