package com.locima.xml2csv.inputparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.locima.xml2csv.ArgumentNullException;
import com.locima.xml2csv.extractor.NameToXPathMappings;

/**
 * Abstracts a list of mappings between XPath statements and Column Names with methods only relevant to this application.
 */
public class MappingsSet {

	private List<NameToXPathMappings> mappings = new ArrayList<NameToXPathMappings>();

	/**
	 * Adds another set of mappings to this mappings set.
	 *
	 * @param maps a set of mappings, must not be null.
	 */
	public void add(NameToXPathMappings maps) {
		if (maps == null) {
			throw new ArgumentNullException("maps");
		}
		this.mappings.add(maps);
	}

	/**
	 * Get a specific set of mappings by name. Generally used for unit testing but might be handy one day.
	 *
	 * @param name the name of the mapping set to return
	 * @return null if a mapping set with that name could not be found.
	 */
	public NameToXPathMappings get(String name) {
		for (NameToXPathMappings mapping : this.mappings) {
			if (mapping.getName().equals(name)) {
				return mapping;
			}
		}
		return null;

	}

	/**
	 * Gets all the mappings contained wtihin this set as an array.
	 *
	 * @return an array of mappings, possibly empty but never null.
	 */
	public NameToXPathMappings[] getAll() {
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
	public Map<String, List<String>> getHeaders() {
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
	 * @return the natural number of mappings contained within this set of mappings.
	 */
	public int size() {
		return this.mappings.size();
	}

}
