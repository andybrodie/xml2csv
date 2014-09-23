package com.locima.xml2csv.inputparser;

import java.util.List;
import java.util.Map;

/**
 * Used for objects that contain lists of mappings of column name to XPath.
 */
public interface IMappingListContainer {
	/**
	 * Adds a child set of mappings to this mappings set.
	 *
	 * @param maps a set of mappings, must not be null and must have a unique {@link NameToXPathMappings#getName()} value.
	 */
	void addMappings(NameToXPathMappings maps);

	/**
	 * Get a specific set of mappings by name. Generally used for unit testing but might be handy one day.
	 *
	 * @param name the name of the mapping set to return
	 * @return null if a mapping set with that name could not be found.
	 */
	NameToXPathMappings getMappingsByName(String name);

	/**
	 * Gets all the mappings contained within this set as an array.
	 *
	 * @return an array of mappings, possibly empty but never null.
	 */
	NameToXPathMappings[] mappingsToArray();

	/**
	 * Retrieves all "headers" associated with all the mappings.
	 * <p>
	 * The headings are all the output names mapped to the column names that they have. This is useful for initialising all the output files using
	 * {@link com.locima.xml2csv.output.OutputManager#createFiles(Map)}
	 *
	 * @return a map, possibly empty, but never null, or output name to the list of column names.
	 */
	Map<String, List<String>> getMappingsHeaders();

	/**
	 * Gets the number of mappings contained within this set of mappings.
	 *
	 * @return the natural number of mappings contained within this set of mappings.
	 */
	int getNumberOfMappings();

}
