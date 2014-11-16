package com.locima.xml2csv.model;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.locima.xml2csv.NotImplementedException;

public class MappingRecord implements Iterator<String>, Iterable<String> {
	private Mapping mapping;
	private int nextValueIndex = 0;
	private List<String> values;

	public MappingRecord(Mapping mapping, List<String> values) {
		this.mapping = mapping;
		this.values = values;
	}

	public Mapping getMapping() {
		return this.mapping;
	}

	public MultiValueBehaviour getMultiValueBehaviour() {
		return this.mapping.getMultiValueBehaviour();
	}

	@Override
	public boolean hasNext() {
		return this.nextValueIndex < (this.values.size());
	}

	@Override
	public Iterator<String> iterator() {
		// TODO Oops, need to put the iterator in another class.
		return this;
	}
	
	@Override
	public String next() {
		String valueToReturn = peek();
		if (nextValueIndex>=values.size()) {
			throw new NoSuchElementException();
		}
		this.nextValueIndex++;
		return valueToReturn;
	}
	
	public String peek() {
		return this.nextValueIndex >= size() ? null : this.values.get(this.nextValueIndex);
	}



	@Override
	public void remove() {
		throw new NotImplementedException("Cannot remove items from MappingRecord");
	}

	/** Resets the iterator to its original state before any calls to {@link #next()}. */
	public void reset() {
		this.nextValueIndex = 0;
	}

	/**
	 * Returns the number of values found by this mapping.
	 *
	 * @return an integer of value zero or greater.
	 */
	public int size() {
		return this.values.size();
	}

	public String getValueAt(int index) {
		if (index >= this.values.size()) {
			return null;
		} else {
			return this.values.get(index);
		}
	}
}

