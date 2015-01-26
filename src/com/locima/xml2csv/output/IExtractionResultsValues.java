package com.locima.xml2csv.output;

import java.util.List;

import com.locima.xml2csv.configuration.IValueMapping;
import com.locima.xml2csv.extractor.MappingExtractionContext;

/**
 * Specifies the methods required for obtaining results from a mapping execution (see {@link MappingExtractionContext}).
 */
public interface IExtractionResultsValues extends IExtractionResults {

	/**
	 * Retrieves the mapping configuration object that the results for this object have been obtained from.
	 *
	 * @return a mapping container configuration instance, never null.
	 */
	IValueMapping getMapping();

	/**
	 * Needed for {@link IValueMapping} only.
	 *
	 * @return the values found by executing this mapping.
	 */
	List<String> getResults();

	/**
	 * Retrieve an extracted field instance for the value at the index given.
	 *
	 * @param index the index of the value to retrieve within this mapping.
	 * @return an extracted field for the index given. If there is no value for a field with this index, then null is returned (never throws an
	 *         exception for this).
	 */
	String getValueAt(int index);

}
