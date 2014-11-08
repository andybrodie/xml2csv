package com.locima.xml2csv.output;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.FileUtility;
import com.locima.xml2csv.model.IMappingContainer;
import com.locima.xml2csv.model.MappingConfiguration;

public abstract class AbstractCsvWriter implements IOutputManager {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractCsvWriter.class);
	/**
	 * Set when this output manager has been initialised. Set by {@link #createFiles(Map)} and used by {@link #writeRecords(String, List)}, which will
	 * throw an exception if {@link #createFiles(Map)} hasn't previously been called.
	 */
	private boolean initialised;


	protected File directory;

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

	public List<File> getOutputFileNames(File outputDirectory, MappingConfiguration config) {
		List<File> files = new ArrayList<File>();
		for (IMappingContainer container : config) {
			String outputName = container.getContainerName();
			outputName += ".csv";
			File outputFile = new File(outputDirectory, FileUtility.convertToPOSIXCompliantFilename(outputName));
		}
		return files;
	}

	
}
