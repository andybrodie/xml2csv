package com.locima.xml2csv.inputparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.locima.xml2csv.extractor.NameToXPathMappings;

/**
 * Abstracts a list of mappings between XPath statements and Column Names with methods only relevant to this application.
 */
public class MappingsSet {

	private List<NameToXPathMappings> mappings = new ArrayList<NameToXPathMappings>();

	public void add(NameToXPathMappings maps) {
		this.mappings.add(maps);
	}

	/**
	 * Get a specific set of mappings by name. Generally used for unit testing but might be handy one day.
	 *
	 * @param name the name of the mapping set to return
	 * @return null if a mapping set with that name could not be found.
	 */
	public NameToXPathMappings get(String name) {
		for (NameToXPathMappings mappings : this.mappings) {
			if (mappings.getName().equals(name)) {
				return mappings;
			}
		}
		return null;

	}

	public NameToXPathMappings[] getAll() {
		return this.mappings.toArray(new NameToXPathMappings[0]);
	}

	public Map<String, List<String>> getHeaders() {
		// This would be a one-liner in LINQ. Unfortunately, Java collections turns it in to a living nightmare.
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		for (NameToXPathMappings mappings : this.mappings) {
			List<String> columnNames = new ArrayList<String>();
			for (String columnDefn : mappings.keySet()) {
				// Remember "first" is the column name, "second" is the XPath
				columnNames.add(columnDefn);
			}
			headers.put(mappings.getName(), columnNames);
		}
		return headers;
	}

	public int size() {
		return this.mappings.size();
	}

}
