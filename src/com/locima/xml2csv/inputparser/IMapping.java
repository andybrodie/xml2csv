package com.locima.xml2csv.inputparser;

import java.util.List;

import net.sf.saxon.s9api.XdmNode;

import com.locima.xml2csv.extractor.DataExtractorException;
import com.locima.xml2csv.output.IOutputManager;

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
	List<String> evaluate(XdmNode rootNode, boolean trimWhitespace) throws DataExtractorException;

	/**
	 * Get all the column names that this mapping will output.
	 * @return a collection of at least one entry.  Never returns null or an empty list. 
	 */
	List<String> getColumnNames();

	/**
	 * Returns the most number of values found when executing this mapping.  This is useful when processing "inline" fields.
	 * @return the most number of values found when executing this mapping.
	 */
	int getMaxInstanceCount();

}
