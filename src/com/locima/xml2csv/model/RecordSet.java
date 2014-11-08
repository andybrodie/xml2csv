package com.locima.xml2csv.model;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.NotImplementedException;

/**
 * Represents a set of records created by a mapping execution.
 * <p>
 * Each mapping, when executed against a document as configured, will yield 0 or more field values. Each value will be converted to CSV output,
 * however the following are subject to the configuration:
 * <ol>
 * <li>The position of the field within a record.</li>
 * <li>The handling of multi-valued fields with respect to whether multiple values are presented in a single record (known as "inline" mappings) or
 * over multiple records (known as multi-record mappings).
 * </ol>
 * This class contains the logic to convert the mapping configuration and their values (either via {@link #addResults(Mapping, List)} or
 * {@link #mergeFrom(RecordSet)}) and allow them to be iterated over using the implemementation of the {@link Iterator} implemented this class
 * provides.
 */
public class RecordSet implements Iterable<List<String>>, Iterator<List<String>> {

	private static class MappingRecord implements Iterator<String>, Iterable<String> {
		private Mapping mapping;
		private int nextValueIndex = 0;
		private List<String> values;

		public MappingRecord(Mapping mapping, List<String> values) {
			this.mapping = mapping;
			this.values = values;
		}

		public String current() {
			return this.nextValueIndex >= size() ? null : this.values.get(this.nextValueIndex);
		}

		public Mapping getMapping() {
			return this.mapping;
		}

		public MultiValueBehaviour getMultiValueBehaviour() {
			return this.mapping.getMultiValueBehaviour();
		}

		@Override
		public boolean hasNext() {
			return this.nextValueIndex < (this.values.size() - 1);
		}

		@Override
		public Iterator<String> iterator() {
			// TODO Oops, need to put the iterator in another class.
			return this;
		}

		@Override
		public String next() {
			String valueToReturn = this.values.get(this.nextValueIndex);
			this.nextValueIndex++;
			return valueToReturn;
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
	}

	private static final Logger LOG = LoggerFactory.getLogger(RecordSet.class);

	private boolean hasNext;

	/**
	 * Used to protect against concurrenct modification exceptions in {@link RecordSet#addResults(MappingRecord)} and
	 * {@link RecordSet#mergeFrom(RecordSet)}.
	 */
	private boolean inIterator;

	List<MappingRecord> results;

	public RecordSet() {
		this.results = new ArrayList<MappingRecord>();
		this.inIterator = false;
	}

	public void addResults(Mapping mapping, List<String> values) {
		ensureNotIterating();
		this.addResults(new MappingRecord(mapping, values));
	}

	private void addResults(MappingRecord record) {
		this.results.add(record);
	}

	private void ensureNotIterating() {
		if (this.inIterator) {
			throw new ConcurrentModificationException("You cannot add results whilst iterating a RecordSet");
		}
	}

	private int getActiveGroupNumber() {
		int groupNumber = Integer.MAX_VALUE;
		for (MappingRecord record : this.results) {
			if (record.hasNext()) {
				groupNumber = Math.min(groupNumber, record.getMapping().getGroupNumber());
			}
		}
		return groupNumber;
	}

	/**
	 * Determines whether there are any more records to iterate over based on whether all of the mappings have had all their outputs returned from the
	 * iterator.
	 *
	 * @return true if calling {@link #next()} would yield a record, false otherwise.
	 */
	@Override
	public boolean hasNext() {
		for (MappingRecord record : this.results) {
			if (!record.hasNext()) {
				this.hasNext = true;
				return true;
			}
		}
		this.hasNext = false;
		return false;
	}

	@Override
	public Iterator<List<String>> iterator() {
		return this;
	}

	/**
	 * Copy all the records from the passed <code>records</code> and add them to this instance.
	 *
	 * @param records the records to add. If null or empty then no action is taken.
	 */
	public void mergeFrom(RecordSet records) {
		ensureNotIterating();
		if (records != null) {
			for (MappingRecord record : records.results) {
				this.addResults(record);
			}
		}
	}

	@Override
	public List<String> next() {
		this.inIterator = true;
		if (!this.hasNext) {
			throw new NoSuchElementException();
		}

		/*
		 * I need to know which group I'm currently moving forward. If I'm part of that group then I move on, otherwise I stay put. Hmmm, so the
		 * qusetion is how do I work out which group I'm moving forward in this record?
		 */
		int activeGroup = getActiveGroupNumber();
		LOG.debug("Active group is {}", activeGroup);

		/*
		 * Build up a record based on each of the mappings, taking in to account their specified MultiValueBehaviour
		 */
		List<String> csvLine = new ArrayList<String>();
		for (MappingRecord record : this.results) {
			switch (record.getMultiValueBehaviour()) {
				case DISCARD:
				case ERROR:
				case WARN:
					if (record.hasNext()) {
						csvLine.add(record.next());
					}
				case INLINE:
					for (String value : record) {
						csvLine.add(value);
					}
					break;
				case MULTI_RECORD:
					if (record.getMapping().getGroupNumber() == activeGroup) {
						csvLine.add(record.current());
					} else {
						csvLine.add(record.hasNext() ? record.next() : null);
					}
					break;
				case DEFAULT:
					throw new IllegalStateException(
									"Found DEFAULT MultiValueBehaviour whilst transforming to output, this should have been resolved by Mapping.getMultiValueBehaviour().  Bug found!");
				default:
					throw new IllegalStateException("Found unexpected (" + record.getMultiValueBehaviour()
									+ ") value in Mapping.getMultiValueBehaviour().  Bug found.");

			}
		}

		return csvLine;
	}

	@Override
	public void remove() {
		throw new NotImplementedException("remove() should never be called on RecordSet: %s", this);
	}

}
