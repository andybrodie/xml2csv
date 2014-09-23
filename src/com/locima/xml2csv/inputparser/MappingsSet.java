package com.locima.xml2csv.inputparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.locima.xml2csv.ArgumentException;
import com.locima.xml2csv.ArgumentNullException;

/**
 * Abstracts a list of mappings between XPath statements and Column Names with methods only relevant to this application.
 */
public class MappingsSet implements IMappingListContainer {

	/**
	 * The list of mappings maintained by this object.
	 */
	private List<NameToXPathMappings> mappings = new ArrayList<NameToXPathMappings>();

	/**
	 * Adds a child set of mappings to this mappings set.
	 *
	 * @param maps a set of mappings, must not be null and must have a unique {@link NameToXPathMappings#getName()} value.
	 */
	@Override
	public void addMappings(NameToXPathMappings maps) {
		if (maps == null) {
			throw new ArgumentNullException("maps");
		}
		// Ensure that the mapping set name is unique
		String mappingSetName = maps.getName();
		if (mappingSetName == null) {
			throw new ArgumentException("maps", "contains a null name.");
		}
		if (getMappingsByName(maps.getName()) != null) {
			throw new ArgumentException("maps", "must contain a unique name");
		}
		this.mappings.add(maps);
	}

	/**
	 * Get a specific set of mappings by name. Generally used for unit testing but might be handy one day.
	 *
	 * @param name the name of the mapping set to return
	 * @return null if a mapping set with that name could not be found.
	 */
	@Override
	public NameToXPathMappings getMappingsByName(String name) {
		for (NameToXPathMappings mapping : this.mappings) {
			if (mapping.getName().equals(name)) {
				return mapping;
			}
		}
		return null;
	}

	/**
	 * Gets all the mappings contained within this set as an array.
	 *
	 * @return an array of mappings, possibly empty but never null.
	 */
	@Override
	public NameToXPathMappings[] mappingsToArray() {
		return this.mappings.toArray(new NameToXPathMappings[0]);
	}

	/**
	 * Retrieves all "headers" associated with all the mappings.
	 * <p>
	 * The headings are all the output names mapped to the column names that they have. This is useful for initialising all the output files using
	 * {@link com.locima.xml2csv.output.OutputManager#createFiles(Map)}
	 *
	 * @return a map, possibly empty, but never null, or output name to the list of column names.
	 */
	@Override
	public Map<String, List<String>> getMappingsHeaders() {
		// This would be a one-liner in LINQ. Unfortunately, Java collections turns it in to a living nightmare.
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		for (NameToXPathMappings mapping : this.mappings) {
			List<String> columnNames = new ArrayList<String>();
			for (String columnDefn : mapping.keySet()) {
				// Remember "first" is the column name, "second" is the XPath
				columnNames.add(columnDefn);
			}
			headers.put(mapping.getName(), columnNames);
		}
		return headers;
	}

	/**
	 * Gets the number of mappings contained within this set of mappings.
	 *
	 * @return the natural number of mappings contained within this set of mappings.
	 */
	@Override
	public int getNumberOfMappings() {
		return this.mappings.size();
	}

}
