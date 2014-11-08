package com.locima.xml2csv.output;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.model.MappingConfiguration;

public class OutputManagerFactory {

	private static final Logger LOG = LoggerFactory.getLogger(OutputManagerFactory.class);

	private static boolean includesPotentialInline(MappingConfiguration config) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public static IOutputManager create(MappingConfiguration config ){
		IOutputManager outputManager;
		if (includesPotentialInline(config)) {
			LOG.info("Potential inline detected, therefore using the InlineCsvWriter");
			outputManager = new InlineCsvWriter();
		} else {
			LOG.info("No inline potential detected, therefore using the DirectCsvWriter");
			outputManager = new DirectCsvWriter();
		}
		return outputManager;

	}
}
