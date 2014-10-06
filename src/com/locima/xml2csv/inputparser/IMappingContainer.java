package com.locima.xml2csv.inputparser;

import java.util.List;

import net.sf.saxon.s9api.XdmNode;

import com.locima.xml2csv.Tuple;
import com.locima.xml2csv.extractor.DataExtractorException;

/**
 * Used for objects that contain lists of mappings of column name to XPath.
 */
public interface IMappingContainer extends IMapping, Iterable<IMapping> {

	/**
	 * Returns an output name associated with this mapping container.
	 * 
	 * @return a string, or null if this mapping container is anonymous. Note that top-level mapping containers (i.e. those stored beneath
	 *         {@link MappingConfiguration} cannot be anonymous and must have a valid non-zero length string.
	 */
	String getOutputName();
	
	/**
	 * Containers can produce either one record (via {@link IMapping#evaluate(XdmNode, boolean)) or multiple records via this method.
	 * All nested mappings can only produce one record (which is part of a larger parent record), however top level mappings can 
	 * produce multiple records using this method.
	 * @param rootNode the root from which to evaluate all mappings within this container.
	 * @param trimWhitespace if true, then leading and trailing whitespace will be removed from all data values.
	 * @return a list of records, each record consists of a list of strings which make up the fields.  Never returns null.
	 * @throws DataExtractorException if an error occurred whilst extracting data (typically this would be caused by bad XPath, or XPath invalid from
	 *             the <code>mappingRoot</code> specified).
	 */
	List<List<String>> evaluateToRecordList(XdmNode rootNode, boolean trimWhitespace) throws DataExtractorException;

	
	int getColumnNames(List<String> columnNames);

}
