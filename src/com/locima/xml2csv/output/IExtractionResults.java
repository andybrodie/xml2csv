package com.locima.xml2csv.output;

import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.MultiValueBehaviour;

/**
 * Interface for objects that contain the results of extracting data from XML documents.
 */
public interface IExtractionResults {

	/**
	 * The group number of this mapping. Each mapping in the same group is incremented at the same time.
	 *
	 * @return the group number.
	 */
	int getGroupNumber();

	/**
	 * Retrieves the mapping configuration object that the results for this object have been obtained from.
	 *
	 * @return an {@link IMapping} instance, never null.
	 */
	IMapping getMapping();

	/**
	 * Get the minimum number of results that this mapping can return. If there are not enough values found to make up this number, then nulls are
	 * added.
	 *
	 * @return the most number of results that this mapping can return. If there are not enough values found to make up this number, then nulls are
	 *         added.
	 */
	int getMinCount();

	/**
	 * Retrieves the multi-value behaviour for the mapping these results are based on.
	 *
	 * @return the multi-value behaviour for the mapping these results are based on.
	 */
	MultiValueBehaviour getMultiValueBehaviour();

	/**
	 * Needed for {@link GroupState#createGroupStateList(IExtractionResults)}.
	 *
	 * @return the number of results found by this set of extraction results.
	 */
	int size();

}
