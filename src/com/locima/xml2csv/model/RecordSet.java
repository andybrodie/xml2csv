package com.locima.xml2csv.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.StringUtil;
import com.locima.xml2csv.Tuple;
import com.locima.xml2csv.output.RecordSetCsvIterator;
import com.locima.xml2csv.output.RecordSetInlineIterator;

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
public class RecordSet implements Iterable<List<ExtractedField>> {

	private static final Logger LOG = LoggerFactory.getLogger(RecordSet.class);

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
	 * Adds a new result of executing a mapping to this record set.<p>
	 * If there is already a set of results for the mapping associated with the <code>record</code> then these results are
	 * merged in to the existing {@link MappingRecord}. 
	 *
	 * @param record the record to add. Must not be null.
	 */
	public void add(MappingRecord record) {
		boolean addedToExisting = false;
		for (MappingRecord existingRecord : this.results) {
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
	 * Adds a new set of <code>values</code> for the passed <code>mapping</code> to this set of results.
	 *
	 * @param mapping the mapping configuration that yielded the <code>values</code>.
	 * @param values the values generated from the passed <code>mapping</code>.
	 */
	public void addResults(Mapping mapping, List<ExtractedField> values) {
		if (LOG.isTraceEnabled()) {
			LOG.trace("Adding values to {}: {}", mapping, StringUtil.collectionToString(values, ",", "\""));
		}
		add(new MappingRecord(mapping, values));
	}

	@Override
	public Iterator<List<ExtractedField>> iterator() {
		return new RecordSetCsvIterator(this.results);
	}
	
}
