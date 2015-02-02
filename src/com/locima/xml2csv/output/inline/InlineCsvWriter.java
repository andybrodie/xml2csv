package com.locima.xml2csv.output.inline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.configuration.IValueMapping;
import com.locima.xml2csv.extractor.AbstractExtractionContext;
import com.locima.xml2csv.extractor.ContainerExtractionContext;
import com.locima.xml2csv.extractor.MappingExtractionContext;
import com.locima.xml2csv.output.IExtractionResultsContainer;
import com.locima.xml2csv.output.IOutputManager;
import com.locima.xml2csv.output.IOutputWriter;
import com.locima.xml2csv.output.OutputManagerException;
import com.locima.xml2csv.output.OutputUtil;
import com.locima.xml2csv.output.direct.DirectCsvWriter;
import com.locima.xml2csv.util.FileUtility;

/**
 * Manages the output for a single CSV file where the results of conversion from XML when the mapping configuration contains a variable number of
 * fields in a record.
 * <p>
 * If the number of fields in a CSV depends on the result of mappings (i.e. executing an XPath statement yields n results in an inline mapping) then
 * we cannot write a CSV file directly. This {@link IOutputManager} does this by writing an intermediate file which is then converted to a CSV file
 * once the number of fields required is known.
 * <p>
 * This is done by simply deferring the final conversion of {@link AbstractExtractionContext} results to a CSV file by saving the serialized form of
 * the {@link AbstractExtractionContext} data in a "CSI" file. When all the inputs have been processed, we now know the highest number of iterations
 * any one {@link IMapping} instance has found during execution, and can now insert all the blank fields in the CSV that we need to keep all the data
 * aligned.
 */
public class InlineCsvWriter implements IOutputWriter {

	private static final String CONTAINER_NAME_PREFIX = "C_";

	private static final Logger LOG = LoggerFactory.getLogger(InlineCsvWriter.class);

	private static final String VALUE_NAME_PREFIX = "V_";

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

	private Map<String, IMapping> nameToMapping;

	/**
	 * The final CSV output file that will contain our desired output.
	 */
	private File outputDirectory;

	/**
	 * The name of the output managed by this writer.
	 */
	private String outputName;

	/**
	 * Tidies up (i.e. deletes) all intermediate files. If this fails then only logging will be produced.
	 */
	@Override
	public void abort() {
		if (this.csiWriter != null) {
			try {
				OutputUtil.close(this.outputName, this.csiOutputFile.getAbsolutePath(), this.csiWriter);
				this.csiOutputFile.delete();
			} catch (OutputManagerException e) {
				LOG.error("Unable to close and delete CSI writer during abort", e);
			}
		}
	}

	/**
	 * Closes the intermediate CSI file and converts it to a CSV file using {@link DirectCsvWriter}.
	 *
	 * @throws OutputManagerException if an unexpected error occurs whilst executing.
	 */
	@Override
	public void close() throws OutputManagerException {
		OutputUtil.close(this.outputName, this.csiOutputFile.getAbsolutePath(), this.csiWriter);
		convertCsiToCsv();
	}

	/**
	 * Converts the intermediate CSI file written as the mapping evaluation was going on to it's final CSV form.
	 * <p>
	 * Algorithm as follows:
	 * <ol>
	 * <li>Create a DirectCsvWriter instance of this InlineCsvWriter.
	 * <li>For each serialized AbstractExtractionContext instance written to the CSI file.</li>
	 * <li>Read it</li>
	 * <li>Pass it to a DirectCsvWriter instance.</li>
	 * <li>Close the CSV file.</li>
	 * </ol>
	 *
	 * @throws OutputManagerException if an error occurs whilst creating, openining, or closing the output CSV file.
	 */
	private void convertCsiToCsv() throws OutputManagerException {
		LOG.info("Converting output CSI file {} to output CSV", this.csiOutputFile.getAbsolutePath());

		CsiInputStream csiInput = null;
		csiInput = getCsiInput();

		DirectCsvWriter csvWriter = new DirectCsvWriter();
		csvWriter.initialise(this.outputDirectory, this.container, this.appendOutput);
		try {
			// Go through all the records, reading each snapshot AbstractExtractionContext state then passing it to the DirectCsvWriter
			ContainerExtractionContext cec = csiInput.getNextRecord();
			while (cec != null) {
				csvWriter.writeRecords(cec);
				cec = csiInput.getNextRecord();
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
				csvWriter.close();
			}
		}
	}

	/**
	 * Creates the output stream for the CSI file.
	 *
	 * @return an output stream to write {@link ExtractionContext} instances to.
	 * @throws OutputManagerException if anything goes wrong creating the output stream.
	 */
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
	 * Creates a mapping from an {@link IMapping} name to the {@link IMapping} instance. This is used by the {@link CsiInputStream} to allow
	 * {@link ContainerExtractionContext} and {@link MappingExtractionContext} to restore the reference to the {@link IMapping} instance that they
	 * related to (as we don't serialize {@link IMapping} instances, just the name).
	 *
	 * @return a map, never null, never empty.
	 */
	private Map<String, IMapping> createMappingMap() {
		Map<String, IMapping> dic = new HashMap<String, IMapping>();
		// Implemented with a tree iteration, no recursion
		Stack<IMapping> toDo = new Stack<IMapping>();
		toDo.push(this.container);
		while (!toDo.empty()) {
			IMapping current = toDo.pop();
			if (current instanceof IValueMapping) {
				dic.put(VALUE_NAME_PREFIX + ((IValueMapping) current).getBaseName(), current);
			} else {
				IMappingContainer currentContainer = (IMappingContainer) current;
				dic.put(CONTAINER_NAME_PREFIX + ((IMappingContainer) current).getContainerName(), current);
				for (IMapping child : currentContainer) {
					toDo.add(child);
				}
			}
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace("Created Mapping Dictionary for InlineCsvWriter as follows:");
			for (Entry<String, IMapping> x : dic.entrySet()) {
				LOG.trace("\t{} = {}", x.getKey(), x.getValue());
			}
		}
		return dic;
	}

	/**
	 * Opens the CSI file for reading back (to create the CSV file).
	 *
	 * @return an input stream for the contexts stored in the CSI file.
	 * @throws OutputManagerException if an unexpected error occurs whilst writing to the CSI file.
	 */
	private CsiInputStream getCsiInput() throws OutputManagerException {
		this.nameToMapping = createMappingMap();

		try {
			
			LOG.info("Re-opening {} to read intermediate file", this.csiOutputFile.getAbsolutePath());
			return new CsiInputStream(this.nameToMapping, new FileInputStream(this.csiOutputFile));
		} catch (IOException e) {
			throw new OutputManagerException(e, "Unable to open CSI file {} for reading.", this.csiOutputFile.getAbsolutePath());
		}
	}

	@Override
	// CHECKSTYLE:OFF Field hiding is fine here because this is for initialising the obeject after a constructor call.
	public void initialise(File outputDirectory, IMappingContainer container, boolean appendOutput) throws OutputManagerException {
		// CHECKSTYLE:ON
		this.outputName = container.getContainerName();
		this.outputDirectory = outputDirectory;

		String csiFileNameBasis = this.outputName;
		this.csiOutputFile = new File(this.outputDirectory, FileUtility.convertToPOSIXCompliantFileName(csiFileNameBasis, "CSI", true));
		this.container = container;
		this.csiWriter = createCsiOutput();

		/* Append output is only relevant to the output CSV file, so store it away until required */
		this.appendOutput = appendOutput;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("InlineCsvWriter(");
		sb.append(this.outputName);
		sb.append(", ");
		sb.append(this.outputDirectory);
		sb.append(", ");
		sb.append(this.csiOutputFile);
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Uses Java serialization to write out the <code>context</code> passed to the CSI file.
	 *
	 * @param context write the records for this context to the CSI file.
	 * @throws OutputManagerException if an unexpected error occurs whilst writing to the CSI file.
	 */
	@Override
	public void writeRecords(IExtractionResultsContainer context) throws OutputManagerException {
		try {
			this.csiWriter.writeObject(context);
		} catch (IOException e) {
			throw new OutputManagerException(e, "Unable to write CEC to CSI file %s", this.csiOutputFile.getAbsolutePath());
		}
	}
}
