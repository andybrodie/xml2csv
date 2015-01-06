package com.locima.xml2csv.output;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.configuration.IValueMapping;
import com.locima.xml2csv.configuration.ParentContext;
import com.locima.xml2csv.extractor.ContainerExtractionContext;
import com.locima.xml2csv.util.StringUtil;
import com.locima.xml2csv.util.Tuple;

public class CsvWriterUtil {

	private static final Logger LOG = LoggerFactory.getLogger(CsvWriterUtil.class);

	/**
	 * Prevents instantiation.
	 */
	private CsvWriterUtil() {
	}

}
