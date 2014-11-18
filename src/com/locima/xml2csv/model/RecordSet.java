package com.locima.xml2csv.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.StringUtil;

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
 */
public class RecordSet implements Iterable<List<String>> {

	private static final Logger LOG = LoggerFactory.getLogger(RecordSet.class);

	private String outputName;

	List<MappingRecord> results;

	/**
	 * Initialises a new, empty record set.
	 */
	public RecordSet() {
		this.results = new ArrayList<MappingRecord>();
	}

	/**
	 * Copy all the records from the passed <code>records</code> and add them to this instance.
	 *
	 * @param records the records to add. If null or empty then no action is taken.
	 */
	public void addAll(RecordSet records) {
		if (records != null) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("Adding {} records to this set, making a total of {}", records.results.size(), records.results.size() + this.results.size());
			}
			for (MappingRecord record : records.results) {
				add(record);
			}
		}
	}

	/**
	 * Adds a new result of executing a mapping to this record set.
	 *
	 * @param record the record to add. Must not be null.
	 */
	public void add(MappingRecord record) {
		boolean addedToExisting = false;
		for (MappingRecord existingRecord : this.results) {
			if (existingRecord.getMapping() == record.getMapping()) {
				existingRecord.addAll(record);
				addedToExisting = true;
				break;
				// throw new IllegalStateException("Attempted to add a second set of results for mapping " + record.getMapping());
			}
		}
		if (!addedToExisting) {
			this.results.add(record);
		}
	}

	/**
	 * Adds a new set of <code>values</code> for the passed <code>mapping</code> to this set of results.
	 *
	 * @param mapping the mapping configuration that yielded the <code>values</code>.
	 * @param values the values generated from the passed <code>mapping</code>.
	 */
	public void addResults(Mapping mapping, List<String> values) {
		if (LOG.isTraceEnabled()) {
			LOG.trace("Adding values to {}: {}", mapping, StringUtil.collectionToString(values, ",", "\""));
		}
		add(new MappingRecord(mapping, values));
	}

	@Override
	public Iterator<List<String>> iterator() {
		return new RecordSetIterator(this.results);
	}

}
