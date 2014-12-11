package com.locima.xml2csv.extractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.configuration.MappingConfiguration;
import com.locima.xml2csv.output.IOutputManager;
import com.locima.xml2csv.output.OutputManagerException;
import com.locima.xml2csv.util.StringUtil;

public class LoggingOutputManager implements IOutputManager {

	private static final Logger LOG = LoggerFactory.getLogger(LoggingOutputManager.class);
	private List<String> lines;
	
	@Override
	public void abort() {
		 LOG.debug("Closed");
	}

	@Override
	public void close() throws OutputManagerException {
		 LOG.debug("Closing, dump of all output follows");
		logAll();
		 LOG.debug("Closed");
	}

	private void logAll() {
		for (String line : this.lines) {
			LOG.debug(line);
		}
	}

	@Override
	public void initialise(File outputDirectory, MappingConfiguration config, boolean appendOutput) throws OutputManagerException {
		this.lines = new ArrayList<String>();
	}

	@Override
	public void writeRecords(String outputName, Iterable<List<ExtractedField>> iter) throws OutputManagerException {
		for (List<ExtractedField> record : iter) {
			StringBuilder sb = new StringBuilder("Output ");
			sb.append(outputName);
			sb.append(": ");
			sb.append(StringUtil.collectionToString(record, ",", null));
			String outputLine = sb.toString();
			this.lines.add(outputLine);
			LOG.debug(outputLine);
		}
	}

}
