package com.locima.xml2csv.extractor;

import java.util.ArrayList;
import java.util.Collection;

import com.locima.xml2csv.ArgumentException;
import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.MultiValueBehaviour;

/**
 * Contains both a {#IMapping} and the results of executing that mapping on a single input.
 */
public class ExtractedRecord extends ArrayList<ExtractedField> {

	/**
	 *
	 */
	private static final long serialVersionUID = 5749366795534325082L;
	private ExtractionContext mapping;

	/**
	 * Creates a instance that ties together a mapping and the values it yielded.
	 *
	 * @param mapping the mapping that creates the <code>values</code> passed.
	 * @param values the values created by executing the <code>mapping</code> against a single input document.
	 */
	public ExtractedRecord(ExtractionContext mapping, Collection<? extends ExtractedField> values) {
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
	public ExtractedField getFirstOrDefault() {
		return getValueAt(0);
	}

	/**
	 * Retrieves mapping that created these results.
	 *
	 * @return the mapping that creates these results, never null.
	 */
	public IMapping getMapping() {
		// return this.mapping;
		// TODO Implement this
		return null;
	}

	/**
	 * Retrieves the multi value behaviour for this mapping.
	 *
	 * @return the multi value behaviour for this mapping.
	 */
	public MultiValueBehaviour getMultiValueBehaviour() {
		// return this.mapping.getMultiValueBehaviour();
		// TODO Implement this
		return null;
	}

	/**
	 * Returns the value within this record at the <code>index</code> passed, or null if it exceeds the number of values available.
	 *
	 * @param index the index of the element to return.
	 * @return either a valid value at the index, or null if out of range.
	 */
	public ExtractedField getValueAt(int index) {
		return index >= size() ? null : get(index);
	}

}
