package com.locima.xml2csv.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class RecordSet implements Iterable<List<String>> {

	private static final Logger LOG = LoggerFactory.getLogger(RecordSet.class);

	List<MappingRecord> results;

	public RecordSet() {
		this.results = new ArrayList<MappingRecord>();
	}

	/**
	 * Adds a new set of <code>values</code> for the passed <code>mapping</code> to this set of results.
	 *
	 * @param mapping the mapping configuration that yielded the <code>values</code>.
	 * @param values the values generated from the passed <code>mapping</code>.
	 */
	public void addResults(Mapping mapping, List<String> values) {
		this.addResults(new MappingRecord(mapping, values));
	}

	public void addResults(MappingRecord record) {
		for (MappingRecord existingRecord : this.results) {
			if (existingRecord.getMapping() == record.getMapping()) {
				throw new IllegalStateException("Attempted to put in two sets of records for the same mapping " + record.getMapping()
								+ ", this should never happen. BUG");
			}
		}
		this.results.add(record);
	}

	/**
	 * Determines whether there are records that are cofigured with {@link MultiValueBehaviour#MULTI_RECORD} that are <strong>not</strong> active, but
	 * have more values to yield.
	 * <p>
	 * This is required for {@link #next()} to know whether it can move the active group on or not.
	 *
	 * @param activeGroupNumber the current active group number
	 * @return true if there are more records based on non-active groups to yield.
	 */
	public boolean currentNonActiveMultiRecordHasNext(int activeGroupNumber) {
		for (MappingRecord record : this.results) {
			if (record.hasNext() && (record.getMapping().getGroupNumber() != activeGroupNumber)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<List<String>> iterator() {
		return new RecordSetIterator(this.results);
	}

	/**
	 * Copy all the records from the passed <code>records</code> and add them to this instance.
	 *
	 * @param records the records to add. If null or empty then no action is taken.
	 */
	public void mergeFrom(RecordSet records) {
		if (records != null) {
			for (MappingRecord record : records.results) {
				this.addResults(record);
			}
		}
	}

}
