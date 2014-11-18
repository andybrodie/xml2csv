package com.locima.xml2csv.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.FileUtility;
import com.locima.xml2csv.model.IMappingContainer;

public class OutputEntry {

	private static final Logger LOG = LoggerFactory.getLogger(OutputEntry.class);

	public static OutputEntry create(File outputDirectory, IMappingContainer container, boolean appendOutput) throws OutputManagerException {
		String name = container.getContainerName();
		String fileNameBasis = name +  ".csv";
		File file = new File(outputDirectory, FileUtility.convertToPOSIXCompliantFilename(fileNameBasis));
		Writer writer = createWriter(file, appendOutput);
		return new OutputEntry(name, file, writer, container);
	}

	private static Writer createWriter(File file, boolean appendOutput) throws OutputManagerException {
		final String encoding = "UTF8";
		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, appendOutput), encoding));
			LOG.info("Successfully opened output file for writer {}", file.getAbsolutePath());
			return writer;
		} catch (FileNotFoundException fileNotFoundException) {
			// If we can't even create an output file, throw an exception up to abort
			throw new OutputManagerException(fileNotFoundException, "Unable to create output file %s", file.getAbsolutePath());
		} catch (UnsupportedEncodingException uee) {
			// This should never happen as we're hard-coding a known supported encoding in Java
			throw new IllegalStateException("Unexpected unsupported encoding exception: " + encoding, uee);
		}
	}

	private IMappingContainer container;

	private File file;

	private String name;

	private Writer writer;

	public OutputEntry(String outputName, File outputFile, Writer outputWriter, IMappingContainer container) {
		this.name = outputName;
		this.file = outputFile;
		this.writer = outputWriter;
		this.container = container;
	}

	public IMappingContainer getContainer() {
		return this.container;
	}

	public File getOutputFile() {
		return this.file;
	}

	public String getOutputName() {
		return this.name;
	}

	public Writer getWriter() {
		return this.writer;
	}

}
