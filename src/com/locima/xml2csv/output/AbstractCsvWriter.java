package com.locima.xml2csv.output;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.ArgumentNullException;
import com.locima.xml2csv.model.IMappingContainer;
import com.locima.xml2csv.model.MappingConfiguration;

public abstract class AbstractCsvWriter implements IOutputManager {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractCsvWriter.class);

	public static List<OutputEntry> createEntries(File outputDirectory, MappingConfiguration config, boolean appendOutput)
					throws OutputManagerException {
		if (config == null) {
			throw new ArgumentNullException("config");
		}
		if (outputDirectory == null) {
			throw new ArgumentNullException("outputDirectory");
		}
		List<OutputEntry> outputs = new ArrayList<OutputEntry>();
		for (IMappingContainer container : config) {
			outputs.add(OutputEntry.create(outputDirectory, container, appendOutput));
		}
		return outputs;
	}

	private File directory;

	/**
	 * Set when this output manager has been initialised. Set by {@link #createFiles(Map)} and used by {@link #writeRecords(String, List)}, which will
	 * throw an exception if {@link #createFiles(Map)} hasn't previously been called.
	 */
	private boolean initialised;

	protected List<OutputEntry> outputs = new ArrayList<OutputEntry>();

	@Override
	public void close() {
		LOG.info("Closing {} outputs", this.outputs.size());
		for (OutputEntry output : this.outputs) {
			try {
				LOG.debug("Flushing data for writer {} ({})", output.getOutputName(), output.getOutputFile().getAbsolutePath());
				output.getWriter().flush();
			} catch (IOException ioe) {
				// No point throwing the error up as there's no useful action to be taken at this point
				LOG.error("Error flushing data to writer", ioe);
			}
			try {
				LOG.info("Closing writer {} ({})", output.getOutputName(), output.getOutputFile().getAbsolutePath());
				output.getWriter().close();
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
		LOG.info("Creating {} writers in {}", this.outputs.size(), this.directory.getAbsolutePath());
		this.outputs = createEntries(this.directory, config, appendOutput);
	}

	/**
	 * Sets the directory to which output files will be written.
	 *
	 * @param directory the output directory.
	 * @throws OutputManagerException If the directory does not exist
	 */
	@Override
	public void setDirectory(File directory) throws OutputManagerException {
		this.directory = directory;
		if (!directory.isDirectory()) {
			throw new OutputManagerException("Output directory specified is not a directory: %1$s", directory.getAbsolutePath());
		}
		if (!directory.canWrite()) {
			throw new OutputManagerException("Output directory is not writeable: %1$s", directory.getAbsolutePath());
		}
		LOG.info("Configured output directory as {}", this.directory.getAbsolutePath());
	}

}
