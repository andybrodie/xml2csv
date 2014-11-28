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
	RecordSet evaluate(XdmNode rootNode, ExtractionContext context, boolean trimWhitespace) throws DataExtractorException;

	/**
	 * Defines how multiple values being found by this mapping, for a single input, should be named.
	 * 
	 * @return an inline format specification.
	 */
	NameFormat getNameFormat();

	/**
	 * Retrieves the multi-value behaviour (inline or multi-record) for this mapping.
	 * @return
	 */
	MultiValueBehaviour getMultiValueBehaviour();

	/**
	 * Retrieve the field names for the mapping.
	 * @param fieldNames the list of field names that this method should add to. 
	 * @param parentName the parent of the parent mapping. 
	 * @param parentCount the index of the parent value.
	 * @return the number of field names that this method added.
	 */
	int getFieldNames(List<String> fieldNames, String parentName, int parentCount);

	/**
	 * The group number of this mapping.  Each mapping in the same group is incremented at the same time.
	 * @return the group number.
	 */
	int getGroupNumber();

	/**
	 * Determines whether a mapping will always output the same number of values, or whether it's variable.
	 * <p>
	 * Only unbounded inline and pivot mappings can cause variable numbers of values.
	 * @return true if the mapping always outputs a fixed number of values,false otherwise.  
	 */
	boolean hasFixedOutputCardinality();
	
	/**
	 * Retrieves the parent of this mapping.
	 * @return either a valid {@link IMappingContainer} instance, or null if this is the top-level container (i.e. it is contained by the single
	 * {@link MappingConfiguration}.
	 */
	IMappingContainer getParent();

}
