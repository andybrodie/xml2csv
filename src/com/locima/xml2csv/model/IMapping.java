package com.locima.xml2csv.model;

import java.util.List;

import net.sf.saxon.s9api.XdmNode;

import com.locima.xml2csv.extractor.DataExtractorException;

/**
 * The basic interface for any kind of mapping (may map single or multiple data items).
 */
public interface IMapping {

	/**
	 * Evaluates this mapping, using the passed XML node as a root for all XPath statements.
	 *
	 * @param rootNode the XML node from which to execute all XPath statements contained within mappings. Must not be null.
	 * @param trimWhitespace if true, then leading and trailing whitespace will be removed from all data values.
	 * @return an array of values extracted from the data. May be empty, but never null.
	 * @throws DataExtractorException if an error occurred whilst extracting data (typically this would be caused by bad XPath, or XPath invalid from
	 *             the <code>mappingRoot</code> specified).
	 */
	RecordSet evaluate(XdmNode rootNode, boolean trimWhitespace) throws DataExtractorException;

	/**
	 * Defines how multiple values being found by this mapping, for a single input, should be named.
	 * @return an inline format specification.
	 */
	NameFormat getNameFormat();

	MultiValueBehaviour getMultiValueBehaviour();
}
