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
	List<String> evaluate(XdmNode rootNode, boolean trimWhitespace) throws DataExtractorException;

	/**
	 * Returns the most number of values found when executing this mapping. This is useful when processing "inline" fields.
	 * 
	 * @return the most number of values found when executing this mapping.
	 */
	int getMaxInstanceCount();

	/**
	 * Retrieves the column names the will be output by this mapping and adds them to the passed <code>columnNames</code> list parameter.
	 * 
	 * @param columnNames the existing list of column names to append to. This will never be null.
	 * @param parentName the name of the parent {@link IMappingContainer}. This may be required by this instance to retrieve the correct column names,
	 *            depending on the {@link InlineFormat} used by this mapping.
	 * @param parentCount the iteration count of the parent that we are currently evaluating. This may be required by this instance to retrieve the
	 *            correct column names, depending on the {@link InlineFormat} used by this mapping.
	 * @return the number of columns added by this execution.
	 */
	int getColumnNames(List<String> columnNames, String parentName, int parentCount);
}
