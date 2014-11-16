package com.locima.xml2csv.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.BugException;
import com.locima.xml2csv.NotImplementedException;
import com.locima.xml2csv.StringUtil;

/**
 * Iterates over a {@link RecordSet} to output a set of CSV output lines.
 * <p>
 * When initialised, this creates a linked list of {@link GroupState} objects that maintain the state of each group for
 * multi-record mappings, and a special group for all the inline mappings (group number isn't used for inline mappings).
 */
public class RecordSetIterator implements Iterator<List<String>> {

	private static final Logger LOG = LoggerFactory.getLogger(RecordSetIterator.class);

	/**
	 * The group state with the lowest group number. Set up by {@link GroupState#createGroupStateList(java.util.Collection)} in {{@link #iterator()}.
	 */
	private GroupState groupState;

	/** The list of results that this iterator is initialised with.
	 * 
	 */
	private List<MappingRecord> results;

	/** The total number of records that this iterator will return.  Used for unit testing and debugging mostly.
	 * 
	 */
	private int totalResults;

	/**
	 * Initalises a new iterator.  Usually called by {@link RecordSet#iterator()}.
	 * @param results the set of results that we're going to iterate;
	 */
	public RecordSetIterator(List<MappingRecord> results) {
		this.results = results;
		this.groupState = GroupState.createGroupStateList(results);
		this.totalResults = getTotalNumberOfRecords();
	}

	/**
	 * Creates a list of values, ready to be output in to a CSV file.
	 * @return a possibly empty string containing a mixture of null and non-null values.
	 */
	private List<String> createCsvValues() {
		List<String> csvFields = new ArrayList<String>();

		for (MappingRecord record : this.results) {

			switch (record.getMultiValueBehaviour()) {
			/*
			 * In the case that multi-valued where all but the first element should be discarded, produce a warning or error, only ever go after the
			 * first one. Note that throw an error for "ERROR" case is handled previous in the code (when the record is generated). I.e. no matter
			 * what happens just keep repeating the first record
			 */
				case DISCARD:
				case ERROR:
				case WARN:
					record.reset();
					if (record.hasNext()) {
						csvFields.add(record.next());
					}
					/* Inline is very simple, for every output record, just output all the fields */
				case INLINE:
					record.reset();
					for (String value : record) {
						csvFields.add(value);
					}
					break;
				case MULTI_RECORD:
					int valueIndex = getIndexForGroup(record.getMapping().getGroupNumber());
					csvFields.add(record.getValueAt(valueIndex));
					break;
				case DEFAULT:
					throw new BugException(
									"Found DEFAULT MultiValueBehaviour whilst transforming to output, this should have been resolved by Mapping.getMultiValueBehaviour().");
				default:
					throw new BugException("Found unexpected (%s) value in Mapping.getMultiValueBehaviour().", record.getMultiValueBehaviour());

			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Created record as follows ({})", StringUtil.collectionToString(csvFields, ",", null));
		}
		return csvFields;
	}

	/**
	 * Calculates the active group number for {@link #next()}.
	 * <p>
	 * This is done by looking for the lowest "group" number in all the fields making up a {@link RecordSet} that still has a value to yield (i.e.
	 * {@link MappingRecord#hasNext()} returns <code>true</code>.
	 * <p>
	 * This must not be called unless {@link #hasNext()} has returned <code>true</code>.
	 *
	 * @return the number of the active group, a natural number less than (but not equal to) {@link Integer.MAX_VALUE}.
	 */
	private int getActiveGroupNumber(int greaterThan) {
		int groupNumber = Integer.MAX_VALUE;
		for (MappingRecord record : this.results) {
			if (record.hasNext()) {
				groupNumber = Math.min(groupNumber, record.getMapping().getGroupNumber());
			}
		}
		if (groupNumber == Integer.MAX_VALUE) {
			throw new BugException(
							"getActiveGroupNumber found no active groups, which means that hasNext() should never have allowed us to get this far.");
		}
		return groupNumber;
	}

	private int getIndexForGroup(int group) {
		GroupState existingGroup = this.groupState.findByGroup(group);
		if (existingGroup == null) {
			throw new BugException("Tried to get index for non-existant group %d", group);
		}
		return existingGroup.getCurrentIndex();
	}

	private List<MappingRecord> getRecordsForGroup(int group) {
		List<MappingRecord> records = new ArrayList<MappingRecord>();
		for (MappingRecord record : this.results) {
			if (record.getMapping().getGroupNumber() == group) {
				records.add(record);
			}
		}
		return records;
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
			if ((size > 0) && (record.getMultiValueBehaviour() == MultiValueBehaviour.MULTI_RECORD)) {
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
	 * Returns the current record and moves to the next one ready.
	 */
	@Override
	public List<String> next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		List<String> values = createCsvValues();
		this.groupState.increment();
		return values;
	}

	/**
	 * Not supported, so will throw {@link NotImplementedException}.
	 */
	@Override
	public void remove() {
		throw new NotImplementedException("remove() should never be called on RecordSet: %s", this);
	}

}
