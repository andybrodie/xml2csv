package com.locima.xml2csv.inputparser;

import java.util.List;

import net.sf.saxon.s9api.XdmNode;

import com.locima.xml2csv.Tuple;
import com.locima.xml2csv.extractor.DataExtractorException;

/**
 * Used for objects that contain lists of mappings of column name to XPath.
 */
public interface IMappingContainer extends IMapping {

	/**
	 * Retrieves all "headers" associated with all the mappings.
	 * <p>
	 * The headings are all the output names mapped to the column names that they have. This is useful for initialising all the output files using
	 * {@link com.locima.xml2csv.output.OutputManager#createFiles(Map)}
	 *
	 * @return a map, possibly empty, but never null, or output name to the list of column names.
	 */
	Tuple<String, List<String>> getMappingsHeaders();

	/**
	 * Returns an output name associated with this mapping container.
	 * 
	 * @return a string, or null if this mapping container is anonymous. Note that top-level mapping containers (i.e. those stored beneath
	 *         {@link MappingConfiguration} cannot be anonymous and must have a valid non-zero length string.
	 */
	String getOutputName();
	
	/**
	 * Evaluates the set of mappings contained within this mapping container.
	 */
	List<List<String>> evaluateToRecords(XdmNode rootNode, boolean trimWhitespace) throws DataExtractorException;


}
