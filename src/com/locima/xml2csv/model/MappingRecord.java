package com.locima.xml2csv.model;

import java.util.ArrayList;
import java.util.Collection;

import com.locima.xml2csv.ArgumentException;

/**
 * Contains both a {#IMapping} and the results of executing that mapping on a single input.
 */
public class MappingRecord extends ArrayList<String> {
	private static final long serialVersionUID = -7528682553696301462L;
	private IMapping mapping;

	/**
	 * Creates a instance that ties together a mapping and the values it yielded.
	 *
	 * @param mapping the mapping that creates the <code>values</code> passed.
	 * @param values the values created by executing the <code>mapping</code> against a single input document.
	 */
	public MappingRecord(Mapping mapping, Collection<? extends String> values) {
		super(values);
		if (mapping == null) {
			throw new ArgumentException("mapping must not be null.");
		}
		this.mapping = mapping;
	}

	/**
	 * Returns the first element of the list, or null if the list is empty.
	 *
	 * @return the first element of the list, or null if the list is empty.
	 */
	public String getFirstOrDefault() {
		return getValueAt(0);
	}

	/**
	 * Retrieves mapping that created these results.
	 *
	 * @return the mapping that creates these results, never null.
	 */
	public IMapping getMapping() {
		return this.mapping;
	}

	/**
	 * Retrieves the multi value behaviour for this mapping.
	 *
	 * @return the multi value behaviour for this mapping.
	 */
	public MultiValueBehaviour getMultiValueBehaviour() {
		return this.mapping.getMultiValueBehaviour();
	}

	/**
	 * Returns the value within this record at the <code>index</code> passed, or null if it exceeds the number of values available.
	 *
	 * @param index the index of the element to return.
	 * @return either a valid value at the index, or null if out of range.
	 */
	public String getValueAt(int index) {
		return index >= size() ? null : get(index);
	}

}
