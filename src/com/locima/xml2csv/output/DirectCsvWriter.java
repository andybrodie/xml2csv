package com.locima.xml2csv.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import com.locima.xml2csv.configuration.MultiValueBehaviour;
import com.locima.xml2csv.configuration.ParentContext;
import com.locima.xml2csv.extractor.ContainerExtractionContext;
import com.locima.xml2csv.extractor.ExtractedField;
import com.locima.xml2csv.util.FileUtility;
import com.locima.xml2csv.util.StringUtil;
import com.locima.xml2csv.util.Tuple;

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

	public static String toCsvRecord(List<ExtractedField> inputCollection) {
		return StringUtil.toString(inputCollection, ",", new StringUtil.IConverter<ExtractedField>() {

			@Override
			public String convert(ExtractedField input) {
				if (input == null) {
					return null;
				} else {
					return StringUtil.escapeForCsv(input.getFieldValue());
				}
			}

		});
	}

	private File outputFile;

	private String outputName;

	private Writer writer;

	/**
	 * In the case of a direct CSV writer, all we can do is attempt to close the file.
	 */
	@Override
	public void abort() {
		close();
	}

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
				this.writeFieldNames(this.outputName, container, writer);
			} else {
				LOG.info("File {} already exists, therefore not writing field names", file.getAbsolutePath());
			}
			LOG.info("Successfully opened output file for writer {}", file.getAbsolutePath());
			return writer;
		} catch (OutputManagerException ome) {
			// Possibly thrown by writeFieldNames
			abort();
			throw ome;
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
		this.outputFile = new File(outputDirectory, FileUtility.convertToPOSIXCompliantFileName(fileNameBasis, true));
		this.writer = createWriter(container, this.outputFile, appendOutput);
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

	/**
	 * Writes a set of values out to the specified writer using CSV notation.
	 *
	 * @param data the values to write in a CSV format.
	 * @throws OutputManagerException if an error occurred writing the files.
	 */
	@Override
	public void writeRecords(Iterable<List<ExtractedField>> records) throws OutputManagerException {
		for (List<ExtractedField> record : records) {
			String outputLine = DirectCsvWriter.toCsvRecord(record);
			try {
				if (LOG.isTraceEnabled()) {
					LOG.trace("Writing output {}: {}", this.outputFile.getAbsolutePath(), outputLine);
				}
				this.writer.write(outputLine);
				this.writer.write(StringUtil.getLineSeparator());
			} catch (IOException ioe) {
				throw new OutputManagerException(ioe, "Unable to write to %1$s(%2$s): %3$s", this.outputName, this.outputFile.getAbsolutePath(),
								outputLine);
			}
		}
	}

	/**
	 * Writes the field names that exist within <code>container</code> to the <code>writer</code> passed.
	 *
	 * @param outputName the name of the output that we are writing to (included purely for logging).
	 * @param container the mapping container that we are wanting to write the field names for.
	 * @param writer the writer to write the field names to.
	 */
	private void writeFieldNames(String outputName, IMappingContainer ctx, Writer writer) throws OutputManagerException {
		try {
			List<String> fieldNames = getFieldNames(ctx);
			String escapedFieldNames = StringUtil.toCsvRecord(fieldNames);
			LOG.info("Writing field names to {}: {}", outputName, escapedFieldNames);
			writer.write(escapedFieldNames);
			writer.write(StringUtil.getLineSeparator());
		} catch (IOException ioe) {
			throw new OutputManagerException(ioe, "Unable to write field names to %s", outputName);
		}
	}

	/**
	 * Retrieve the field names for the mapping.
	 *
	 * @param fieldNames the list of field names that this method should add to.
	 * @return the number of field names that this method added.
	 */
	private List<String> getFieldNames(IMappingContainer container) {
		List<String> fieldNames = new ArrayList<String>();
		ParentContext parentContext = new ParentContext();
		this.getFieldNames(fieldNames, parentContext, container);
		return fieldNames;
	}

	/**
	 * Recursive implementation of {@link #getFieldNames}. This ensures that the parent iteration count is available.
	 *
	 * @param fieldNames the list of column names that is being built up.
	 * @param parentContext a stack of parent name/iteration pairs ({@link Tuple}) that form the ancestor chain of this mapping.
	 * @return the number of columns added by this invocation.
	 */
	public int getFieldNames(List<String> fieldNames, ParentContext parentContext, IMappingContainer container) {
		int columnCount = 0;
		/*
		 * If this is a non-nested MappingList, i.e. a direct child of MappingConfiguration then the instance count refers to the number of records
		 * output, not the number of fields (as a nested, in-line MappingList would indicate. Therefore, only process as in-line if nested.
		 */
		int repeats = (parentContext.isEmpty() || container.getMultiValueBehaviour() == MultiValueBehaviour.LAZY) ? 1 : container.getMaxValueCount();
		
		String name = container.getContainerName();
		for (int containerIteration = 0; containerIteration < repeats; containerIteration++) {
			parentContext.push(name, containerIteration);
			for (IMapping mapping : container) {
				if (mapping instanceof IMappingContainer) {
					columnCount += getFieldNames(fieldNames, parentContext,(IMappingContainer)mapping);
				} else {
					columnCount += getFieldNames(fieldNames, parentContext,(IValueMapping)mapping);
				}
			}
			parentContext.pop();
		}
		return columnCount;
	}

	public int getFieldNames(List<String> fieldNames, ParentContext parentContext, IValueMapping mapping) {
		/*
		 * The number of fields output is the maximum number of values found in a single execution of this mapping, constrained by this.minValueCount
		 * and this.maxValueCount. Don't need to consider maxValueCount here though as evaluation is halted once we have enough values to meet
		 * maxValueCount.
		 */
		int repeats = Math.max(mapping.getMaxValueCount(), 1);
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

}
