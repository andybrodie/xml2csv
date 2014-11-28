package com.locima.xml2csv.output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.BugException;
import com.locima.xml2csv.StringUtil;
import com.locima.xml2csv.model.ExtractedField;
import com.locima.xml2csv.model.GroupState;
import com.locima.xml2csv.model.MappingRecord;
import com.locima.xml2csv.model.MultiValueBehaviour;
import com.locima.xml2csv.model.RecordSet;

/**
 * Iterates over a {@link RecordSet} to output a set of CSV output lines.
 * <p>
 * When initialised, this creates a linked list of {@link GroupState} objects that maintain the state of each group for multi-record mappings, and a
 * special group for all the inline mappings (group number isn't used for inline mappings).
 */
public class RecordSetInlineIterator implements Iterator<List<ExtractedField>> {

	private static final Logger LOG = LoggerFactory.getLogger(RecordSetInlineIterator.class);

	/**
	 * The group state with the lowest group number. Set up by {@link GroupState#createGroupStateList(java.util.Collection)} in {{@link #iterator()}.
	 */
	private GroupState groupState;

	/**
	 * The list of results that this iterator is initialised with.
	 */
	private List<MappingRecord> results;

	/**
	 * The total number of records that this iterator will return. Used for unit testing and debugging mostly.
	 */
	private int totalResults;

	/**
	 * Initalises a new iterator. Usually called by {@link RecordSet#iterator()}.
	 *
	 * @param results the set of results that we're going to iterate;
	 */
	public RecordSetInlineIterator(List<MappingRecord> results) {
		this.results = results;
		this.groupState = GroupState.createGroupStateList(results);
		this.totalResults = getTotalNumberOfRecords();
	}

	/**
	 * Creates a list of values, ready to be output in to a CSV file.
	 *
	 * @return a possibly empty string containing a mixture of null and non-null values.
	 */
	private List<ExtractedField> createValues() {
		List<ExtractedField> inlineFields = new ArrayList<ExtractedField>();

		for (MappingRecord record : this.results) {
			switch (record.getMultiValueBehaviour()) {
				case GREEDY:
					for (ExtractedField value : record) {
						inlineFields.add(value);
					}
					break;
				case LAZY:
					/*
					 * The most typical option: one new record for each value found
					 */
					int valueIndex = getIndexForGroup(record.getMapping().getGroupNumber());
					inlineFields.add(record.getValueAt(valueIndex));
					break;
				case DEFAULT:
					throw new BugException("Found DEFAULT MultiValueBehaviour whilst transforming to output, this should have been resolved by "
									+ "Mapping.getMultiValueBehaviour().");
				default:
					throw new BugException("Found unexpected (%s) value in Mapping.getMultiValueBehaviour().", record.getMultiValueBehaviour());

			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Created record as follows ({})", StringUtil.collectionToString(inlineFields, ",", null));
		}
		return inlineFields;
	}

	/**
	 * Determines the current index of the passed <code>group<code> in the results set iteation.
	 * 
	 * @param group the group to get the current index of. Must be a valid group.
	 * @return the index of the result to return (min 0, unbounded max)
	 */
	private int getIndexForGroup(int group) {
		GroupState existingGroup = this.groupState.findByGroup(group);
		if (existingGroup == null) {
			throw new BugException("Tried to get index for non-existant group %d", group);
		}
		return existingGroup.getCurrentIndex();
	}

	/**
	 * Get the total number of records expected to be returned.
	 *
	 * @return calculates the total number of records that will be returned when iterated.
	 */
	public int getTotalNumberOfRecords() {
		if (this.results.isEmpty()) {
			return 0;
		}

		Map<Integer, Integer> groupSizes = new HashMap<Integer, Integer>();
		for (MappingRecord record : this.results) {
			int group = record.getMapping().getGroupNumber();
			int size = record.size();
			if ((size > 0) && (record.getMultiValueBehaviour() == MultiValueBehaviour.LAZY)) {
				if (groupSizes.containsKey(group)) {
					if (record.size() > groupSizes.get(group)) {
						groupSizes.put(group, record.size());
					}
				} else {
					groupSizes.put(group, record.size());
				}
			}
		}
		int total = 1;
		for (Integer size : groupSizes.values()) {
			total *= size;
		}

		return total;
	}

	/**
	 * Determines whether there are any more records to iterate over based on whether all of the mappings have had all their outputs returned from the
	 * iterator.
	 *
	 * @return true if calling {@link #next()} would yield a record, false otherwise.
	 */
	@Override
	public boolean hasNext() {
		if (this.groupState == null) {
			return false;
		} else {
			return this.groupState.hasNext();
		}
	}

	/**
	 * Moves on to the next record, preparing the CSV values and returning them.
	 */
	@Override
	public List<ExtractedField> next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		List<ExtractedField> values = createValues();
		this.groupState.increment();
		return values;
	}

	/**
	 * Not supported, so will throw {@link UnsupportedOperationException}.
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove() should never be called on RecordSet: " + this);
	}

}
