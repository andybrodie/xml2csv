package com.locima.xml2csv.extractor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.BugException;
import com.locima.xml2csv.configuration.IMapping;

/**
 * Represents an ordered set of records created by executing a top-level mapping list against a single XML document.
 * <p>
 * Each mapping, when executed against a document as configured, will yield 0 or more field values. Each value will be converted to CSV output,
 * however the following are subject to the configuration:
 * <ol>
 * <li>The position of the field within a record.</li>
 * <li>The handling of multi-valued fields with respect to whether multiple values are presented in a single record (known as "inline" mappings) or
 * over multiple records (known as multi-record mappings).</li>
 * </ol>
 */
public class ExtractedRecordList implements Iterable<List<ExtractedField>> {

	private static final Logger LOG = LoggerFactory.getLogger(ExtractedRecordList.class);

	List<ExtractedRecord> results;

	/**
	 * Initialises a new, empty record set.
	 */
	public ExtractedRecordList() {
		this.results = new ArrayList<ExtractedRecord>();
	}

	/**
	 * Adds a new result of executing a mapping to this record set.
	 * <p>
	 * If there is already a set of results for the mapping associated with the <code>record</code> then these results are merged in to the existing
	 * {@link ExtractedRecord}.
	 *
	 * @param record the record to add. Must not be null.
	 */
	public void add(ExtractedRecord record) {
		boolean addedToExisting = false;
		for (ExtractedRecord existingRecord : this.results) {
			if (existingRecord.getMapping() == record.getMapping()) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("Merging {} in to existing record {} for {}", record, existingRecord, record.getMapping());
				}
				existingRecord.addAll(record);
				addedToExisting = true;
				break;
			}
		}
		if (!addedToExisting) {
			this.results.add(record);
		}
	}

	/**
	 * Copy all the records from the passed <code>records</code> and add them to this instance.
	 *
	 * @param records the records to add. If null or empty then no action is taken.
	 */
	public void addAll(ExtractedRecordList records) {
		if (records != null) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("Adding {} records to this set, making a total of {}", records.results.size(), records.results.size() + this.results.size());
			}
			for (ExtractedRecord record : records.results) {
				add(record);
			}
		}
	}

	public void addResults(IMapping mrMapping, List<ExtractedField> extractedFieldList) {
		throw new BugException("Haven't implemented this yet, need a context now!");
		// this.add(new ExtractedRecord(mrMapping, extractedFieldList));
	}

	@Override
	public Iterator<List<ExtractedField>> iterator() {
		return new RecordSetCsvIterator(this.results);
	}

}
