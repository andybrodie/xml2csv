package com.locima.xml2csv.output;

import java.io.File;

import com.locima.xml2csv.model.IMappingContainer;
import com.locima.xml2csv.model.MappingConfiguration;
import com.locima.xml2csv.model.RecordSet;

/**
 * Manages the output for a single CSV file where the results of conversion from XML when the mapping configuration contains a variable number of
 * fields in a record.
 * <p>
 * If the number of fields in a CSV depends on the result of mappings (i.e. executing an XPath statement yields n results in an inline mapping) then
 * we cannot write a CSV file directly. This {@link IOutputManager} does this by writing an intermediate file which is then converted to a CSV file
 * once the number of fields required is known.
 * <p>
 * If the number of fields in a CSV is known from the configuration (either because it contains no inline mappings or they have a fixed cardinality)
 * then {@link DirectCsvWriter} should be used as it's much faster.
 */
public class InlineCsvWriter implements ICsvWriter {

	public InlineCsvWriter() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws OutputManagerException {
		// TODO Auto-generated method stub
	}

	@Override
	public void initialise(File outputDirectory, IMappingContainer container, boolean appendOutput) throws OutputManagerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeRecords(RecordSet records) throws OutputManagerException {
		// TODO Auto-generated method stub
		
	}


}
