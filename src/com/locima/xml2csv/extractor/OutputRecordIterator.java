package com.locima.xml2csv.extractor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.BugException;
import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.output.GroupState;
import com.locima.xml2csv.util.StringUtil;

/**
 * Iterates over a tree of {@link ContainerExtractionContext} to output a set of CSV output lines. This is where the hierarchical results of all the
 * extraction of data for a single document (modelled using {@link ExtractionContext} instances are finally flattened in to a set of records.
 * <p>
 * When initialised, this creates a linked list of {@link GroupState} objects that maintain the state of each group for multi-record mappings, and a
 * special group for all the inline mappings (group number isn't used for inline mappings).
 */
public class OutputRecordIterator implements Iterator<List<ExtractedField>> {

	private static final Logger LOG = LoggerFactory.getLogger(OutputRecordIterator.class);

	/**
	 * The group state with the lowest group number. Set up by {@link GroupState#createGroupStateList(java.util.Collection)} in {{@link #iterator()}.
	 * <p>
	 * {@link GroupState} is a linked list, so we only need to keep a reference to the head.
	 */
	private GroupState baseGroupState;

	/**
	 * The tree of rootContainer that this iterator is walking.
	 */
	private ContainerExtractionContext rootContainer;

	/**
	 * Initalises a new iterator. Usually called by {@link ExtractedRecordList#iterator()}.
	 *
	 * @param rootContainer the set of rootContainer that we're going to iterate;
	 */
	public OutputRecordIterator(ContainerExtractionContext rootContainer) {
		this.rootContainer = rootContainer;
		this.baseGroupState = GroupState.createGroupStateList(rootContainer);
	}

	private void addEmptyCsvFields(List<ExtractedField> csvFields, ContainerExtractionContext context, int containerIterationCount) {
		String namePrefix = getNamePrefix(context, containerIterationCount);
		IMapping container = context.getMapping();
		addEmptyCsvFields(namePrefix, csvFields, container);
	}

	private void addEmptyCsvFields(String namePrefix, List<ExtractedField> csvFields, IMapping mapping) {
		String prefixTemplate = namePrefix + "_";

		if (mapping instanceof IMappingContainer) {
			int iterationsRequired = mapping.getFieldCountForSingleRecord() - mapping.getHighestFoundValueCount();
			for (int i = 0; i < iterationsRequired; i++) {
				IMappingContainer container = (IMappingContainer) mapping;
				int prefix = 0;
				if (LOG.isDebugEnabled()) {
					LOG.debug("Adding {} iterations of {} blank fields for container {}", iterationsRequired, container.size(), mapping);
				}
				for (IMapping childMapping : container) {
					addEmptyCsvFields(prefixTemplate + prefix, csvFields, childMapping);
					prefix++;
				}
			}
		} else {
			int fieldRequired = mapping.getFieldCountForSingleRecord();
			if (LOG.isDebugEnabled()) {
				LOG.debug("Adding {} blank fields for value mapping {}", fieldRequired, mapping);
			}
			for (int fieldCount = 0; fieldCount < fieldRequired; fieldCount++) {
				ExtractedField ef = new ExtractedField(prefixTemplate + fieldCount, StringUtil.EMPTY_STRING);
				csvFields.add(ef);
			}
		}
	}

	/**
	 * Creates a list of values, ready to be output in to a CSV file.
	 *
	 * @return a possibly empty string containing a mixture of null and non-null values.
	 */
	private List<ExtractedField> createCsvValues() {
		List<ExtractedField> csvFields = new ArrayList<ExtractedField>();

		createCsvValues(csvFields, this.rootContainer);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Created record as follows ({})", StringUtil.collectionToString(csvFields, ",", null));
		}
		return csvFields;
	}

	private void createCsvValues(List<ExtractedField> csvFields, ExtractionContext context) {
		if (context instanceof ContainerExtractionContext) {
			createCsvValuesFromContainer(csvFields, (ContainerExtractionContext) context);
		} else {
			createCsvValuesFromMapping(csvFields, (MappingExtractionContext) context);
		}
	}

	/**
	 * Adds to the passed <code>csvFields</code> list the relevant {@link ExtractedField} values found by this container.
	 *
	 * @param csvFields a list of fields being built up for a single output record.
	 * @param context the {@link ExtractionContext} the relevant (contained) values of which should be added to <code>csvFields</code>.
	 */
	private void createCsvValuesFromContainer(List<ExtractedField> csvFields, ContainerExtractionContext context) {
		IMappingContainer mapping = context.getMapping();
		switch (mapping.getMultiValueBehaviour()) {
			case GREEDY:
				int containerIterationCount = 0;
				int resultIndexForTrace = 0;
				List<List<ExtractionContext>> allResults = context.getChildren();
				for (List<ExtractionContext> results : allResults) {
					for (ExtractionContext child : results) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("Greedy eval of child {}.{} ({}) from {}", containerIterationCount, resultIndexForTrace++, child, context);
						}
						createCsvValues(csvFields, child);
					}
					containerIterationCount++;
					resultIndexForTrace = 0;
				}

				/*
				 * If required, add extra empty fields where a minimum number of iterations of a container are required but not enough results were
				 * found to satisfy this requirement (i.e. add lots of empty fields).
				 */
				addEmptyCsvFields(csvFields, context, containerIterationCount);

				break;
			case LAZY:
				int valueIndex = getIndexForGroup(context.getMapping().getGroupNumber());
				List<ExtractionContext> results = context.getResultsSetAt(valueIndex);
				for (ExtractionContext child : results) {
					LOG.debug("Lazy eval of child {} ({}) from {}", valueIndex++, child, context);
					createCsvValues(csvFields, child);
				}
				break;
			case DEFAULT:
				throw new BugException("Found DEFAULT MultiValueBehaviour whilst transforming to output, this should have been resolved by "
								+ "Mapping.getMultiValueBehaviour().");
			default:
				throw new BugException("Found unexpected (%s) value in Mapping.getMultiValueBehaviour().",
								context.getMapping().getMultiValueBehaviour());
		}
	}

	
	/**
	 * Adds the fields generated by a single {@link MappingExtractionContext}, taking in to account multi-value behaviour and
	 * minimum numbers of fields.
	 * @param csvFields the ordered list of CSV fields to add to.
	 * @param context the context to extract the values from.
	 */
	private void createCsvValuesFromMapping(List<ExtractedField> csvFields, MappingExtractionContext context) {
		String namePrefix = getPrefix(context);
		switch (context.getMapping().getMultiValueBehaviour()) {
			case GREEDY:
				/* Greedy mappings output as much as they can */
				List<ExtractedField> fields = context.getAllValues(namePrefix);
				if (LOG.isDebugEnabled()) {
					LOG.debug("Greedily adding all fields {} to as output of {}", StringUtil.collectionToString(fields, ", ", null), context);
				}
				csvFields.addAll(fields);
				break;
			case LAZY:
				/* The most typical option: just process the next value and move on */
				int valueIndex = getIndexForGroup(context.getMapping().getGroupNumber());
				ExtractedField field = context.getValueAt(namePrefix, valueIndex);
				LOG.debug("Lazy eval of child {} ({}) from {}", valueIndex, field, context);
				csvFields.add(field);
				break;
			case DEFAULT:
				throw new BugException("Found DEFAULT MultiValueBehaviour whilst transforming to output, this should have been resolved by "
								+ "Mapping.getMultiValueBehaviour().");
			default:
				throw new BugException("Found unexpected (%s) value in Mapping.getMultiValueBehaviour().",
								context.getMapping().getMultiValueBehaviour());
		}
	}

	/**
	 * Determines the current index of the passed <code>group<code> in the rootContainer set iteration.
	 *
	 * @param group the group to get the current index of. Must be a valid group.
	 * @return the index of the result to return (min 0, unbounded max)
	 */
	private int getIndexForGroup(int group) {
		GroupState existingGroup = this.baseGroupState.findByGroup(group);
		if (existingGroup == null) {
			throw new BugException("Tried to get index for non-existant group %d", group);
		}
		return existingGroup.getCurrentIndex();
	}

	private String getNamePrefix(ContainerExtractionContext context, int containerIterationCount) {
		return "TEMPORARY HACK";
	}

	private String getPrefix(MappingExtractionContext context) {
		StringBuilder sb = new StringBuilder();
		ContainerExtractionContext current = context.getParent();
		while (current != null) {
			sb.insert(0, '_');
			sb.insert(0, current.getIndex());
			current = current.getParent();
		}
		return sb.toString();
	}

	/**
	 * Determines whether there are any more records to iterate over based on whether all of the mappings have had all their outputs returned from the
	 * iterator.
	 *
	 * @return true if calling {@link #next()} would yield a record, false otherwise.
	 */
	@Override
	public boolean hasNext() {
		return (this.baseGroupState == null) ? false : this.baseGroupState.hasNext();
	}

	/**
	 * Moves on to the next record, preparing the CSV values and returning them.
	 */
	@Override
	public List<ExtractedField> next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		List<ExtractedField> values = createCsvValues();
		this.baseGroupState.increment();
		return values;
	}

	/**
	 * Not supported, so will throw {@link UnsupportedOperationException}.
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove() should never be called on ExtractedRecordList: " + this);
	}

}
