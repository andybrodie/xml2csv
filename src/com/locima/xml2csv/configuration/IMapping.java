package com.locima.xml2csv.configuration;

import java.util.List;

/**
 * The basic interface for any kind of mapping (may map single or multiple data items).
 */
public interface IMapping {

	/**
	 * Retrieve the field names for the mapping.
	 * <p>
	 * TODO Think about whether this belongs here or not. I'm beginning to think it doesn't.
	 *
	 * @param fieldNames the list of field names that this method should add to.
	 * @param parentName the parent of the parent mapping.
	 * @param parentCount the index of the parent value.
	 * @return the number of field names that this method added.
	 */
	int getFieldNames(List<String> fieldNames, String parentName, int parentCount);

	/**
	 * The group number of this mapping. Each mapping in the same group is incremented at the same time.
	 *
	 * @return the group number.
	 */
	int getGroupNumber();

	/**
	 * Retrieves the multi-value behaviour (inline or multi-record) for this mapping.
	 *
	 * @return
	 */
	MultiValueBehaviour getMultiValueBehaviour();

	/**
	 * Defines how multiple values being found by this mapping, for a single input, should be named.
	 *
	 * @return an inline format specification.
	 */
	NameFormat getNameFormat();

	/**
	 * Retrieves the parent of this mapping.
	 *
	 * @return either a valid {@link IMappingContainer} instance, or null if this is the top-level container (i.e. it is contained by the single
	 *         {@link MappingConfiguration}.
	 */
	IMappingContainer getParent();

	/**
	 * Determines whether a mapping will always output the same number of values, or whether it's variable.
	 * <p>
	 * Only unbounded inline and pivot mappings can cause variable numbers of values.
	 *
	 * @return true if the mapping always outputs a fixed number of values,false otherwise.
	 */
	boolean hasFixedOutputCardinality();

	/** Get the most number of results that this mapping can return.  Any found after this number are discarded.  Values of 0 means no maximum limit.
	 * 
	 * @return the most number of results that this mapping can return.  Any found after this number are discarded.  Values of 0 means no maximum limit.
	 */
	int getMaxValueCount();

	/** Get the minimum number of results that this mapping can return.  If there are not enough values found to make up this number, then nulls are added.
	 * 
	 * @return the most number of results that this mapping can return.  If there are not enough values found to make up this number, then nulls are added.
	 */
	int getMinValueCount();


}
