package com.locima.xml2csv.output;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.BugException;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.configuration.MappingConfiguration;
import com.locima.xml2csv.output.direct.DirectCsvWriter;
import com.locima.xml2csv.output.inline.InlineCsvWriter;

/**
 * Used to create {@link IOutputManager} instances, using a concrete implementation that is suitable for the mapping configuration passed.
 */
public class OutputManager implements IOutputManager {

	private static final Logger LOG = LoggerFactory.getLogger(OutputManager.class);

	/**
	 * The directory that all outputs will be written to.
	 */
	private File directory;

	/**
	 * Maps output name to the appropriate writer.
	 */
	private Map<String, IOutputWriter> outputToWriter;

	@Override
	public void abort() {
		LOG.info("Aborting {} ICsvWriters", this.outputToWriter.size());
		for (Entry<String, IOutputWriter> entry : this.outputToWriter.entrySet()) {
			LOG.info("Aborting {} {} ({})", entry.getKey().getClass().getName(), entry.getKey(), entry.getValue());
			entry.getValue().abort();
		}
	}

	/**
	 * Finalises all the output writers managed by this instance.
	 *
	 * @throws OutputManagerException if an error occurs whilst closing an output file.
	 */
	@Override
	public void close() throws OutputManagerException {
		LOG.info("Closing {} ICsvWriters", this.outputToWriter.size());
		for (Entry<String, IOutputWriter> entry : this.outputToWriter.entrySet()) {
			LOG.info("Closing {} ({})", entry.getKey(), entry.getValue());
			entry.getValue().close();
		}
	}

	/**
	 * Creates an appropriate {@link IOutputManager} based on the mapping configuration provided. The decision logic for which implementation to use
	 * is based on whether a mapping configuration contains any unbounded inline mappings. These produce a variable number of field values in any
	 * given record. If one is found then it means that we can't directly stream out a CSV file using {@link DirectCsvWriter} (because we wouldn't
	 * know how many fields to include in any recrd), so we have to use {@link InlineCsvWriter} instead.
	 *
	 * @param outputDirectory the directory to create the output file in.
	 * @param config the mapping configuration that we are going to output the results of.
	 * @param appendOutput if true, an existing file will be appended to.
	 * @throws OutputManagerException if any errors occur whilst creating the output files.
	 */
	@Override
	public void initialise(File outputDirectory, MappingConfiguration config, boolean appendOutput) throws OutputManagerException {
		setDirectory(outputDirectory);

		this.outputToWriter = new HashMap<String, IOutputWriter>();
		for (IMappingContainer mappingContainer : config) {
			String outputName = mappingContainer.getName();
			IOutputWriter writer;
			if (mappingContainer.hasFixedOutputCardinality()) {
				LOG.info("No unbounded mappings detected for {}, therefore using the DirectCsvWriter", outputName);
				writer = new DirectCsvWriter();
			} else {
				LOG.info("Unbounded inline detected for {}, therefore creating InlineCsvWriter", outputName);
				writer = new InlineCsvWriter();
			}
			writer.initialise(outputDirectory, mappingContainer, appendOutput);
			this.outputToWriter.put(outputName, writer);
		}
	}

	/**
	 * Sets the directory to which output files will be written.
	 *
	 * @param directory the output directory.
	 * @throws OutputManagerException If the directory does not exist
	 */
	private void setDirectory(File directory) throws OutputManagerException {
		this.directory = directory;
		if (!directory.isDirectory()) {
			throw new OutputManagerException("Output directory specified is not a directory: %1$s", directory.getAbsolutePath());
		}
		if (!directory.canWrite()) {
			throw new OutputManagerException("Output directory is not writeable: %1$s", directory.getAbsolutePath());
		}
		LOG.info("Configured output directory as {}", this.directory.getAbsolutePath());
	}

	/**
	 * Writes the records created by the XML data extractor to the appropraite output writer managed by this instance.
	 *
	 * @param outputName the name of the output to write to.
	 * @param resultsContainer the records to write out.
	 * @throws OutputManagerException if an unrecoverable error occurs whilst writing to the output file.
	 */
	@Override
	public void writeRecords(String outputName, IExtractionResultsContainer resultsContainer) throws OutputManagerException {
		IOutputWriter writer = this.outputToWriter.get(outputName);
		if (writer != null) {
			writer.writeRecords(resultsContainer);
		} else {
			throw new BugException("writeRecords was asked to write records for a non-existant writer: %s", outputName);
		}
	}

}
