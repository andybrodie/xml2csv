package com.locima.xml2csv.output.direct;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.BugException;
import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.extractor.AbstractExtractionContext;
import com.locima.xml2csv.extractor.ContainerExtractionContext;
import com.locima.xml2csv.extractor.MappingExtractionContext;
import com.locima.xml2csv.output.GroupState;
import com.locima.xml2csv.output.IExtractionResults;
import com.locima.xml2csv.output.IExtractionResultsContainer;
import com.locima.xml2csv.output.IExtractionResultsValues;
import com.locima.xml2csv.util.StringUtil;

/**
 * Iterates over a tree of {@link ContainerExtractionContext} to output a set of CSV output lines. This is where the hierarchical results of all the
 * extraction of data for a single document (modelled using {@link AbstractExtractionContext} instances are finally flattened in to a set of records.
 * <p>
 * When initialised, this creates a linked list of {@link GroupState} objects that maintain the state of each group for multi-record mappings, and a
 * special group for all the inline mappings (group number isn't used for inline mappings).
 */
public class DirectOutputRecordIterator implements Iterator<List<String>> {

	private static final Logger LOG = LoggerFactory.getLogger(DirectOutputRecordIterator.class);

	/**
	 * The group state with the lowest group number. Set up by {@link GroupState#createGroupStateList(java.util.Collection)} in {{@link #iterator()}.
	 * <p>
	 * {@link GroupState} is a linked list, so we only need to keep a reference to the head.
	 */
	private GroupState baseGroupState;

	/**
	 * The tree of rootContainer that this iterator is walking.
	 */
	private IExtractionResultsContainer rootContainer;

	/**
	 * Initalises a new iterator. Usually called by {@link ExtractedRecordList#iterator()}.
	 *
	 * @param rootContainer the set of rootContainer that we're going to iterate;
	 */
	public DirectOutputRecordIterator(IExtractionResultsContainer rootContainer) {
		this.rootContainer = rootContainer;
		this.baseGroupState = GroupState.createGroupStateList(rootContainer);
	}

	/**
	 * Adds empty fields as required (based on {@link IMapping#getMinValueCount()} in mapping configurations.
	 * 
	 * @param csvFields the list of CSV fields to append to.
	 * @param context the context to extract results from and append to <code>csvFields</code>.
	 * @param containerIterationCount the count of container instances already output.
	 */
	private void addEmptyCsvFields(List<String> csvFields, IExtractionResultsContainer context, int containerIterationCount) {
		// TODO Check this logic, it was the commented out version, but that seems wrong?
		// addEmptyCsvFields(csvFields, context.getMapping(), context.size());
		addEmptyCsvFields(csvFields, context.getMapping(), containerIterationCount);
	}

	/**
	 * Add empty fields to <code>csvFields</code> based on {@link IMapping#getMinValueCount()} value of the passed <code>mapping</code>.
	 * 
	 * @param csvFields the set of CSV fields to add to.
	 * @param mapping the mapping to work out how many fields are required from.
	 * @param existingResultsCount how many results have already been output.
	 */
	private void addEmptyCsvFields(List<String> csvFields, IMapping mapping, int existingResultsCount) {
		if (mapping instanceof IMappingContainer) {
			int iterationsRequired = mapping.getFieldCountForSingleRecord() - existingResultsCount;
			for (int i = 0; i < iterationsRequired; i++) {
				IMappingContainer container = (IMappingContainer) mapping;
				if (LOG.isDebugEnabled()) {
					LOG.debug("Adding {} iterations of {} blank fields for container {}", iterationsRequired, container.size(), mapping);
				}
				for (IMapping childMapping : container) {
					addEmptyCsvFields(csvFields, childMapping, 0);
				}
			}
		} else {
			int fieldRequired = mapping.getFieldCountForSingleRecord();
			if (LOG.isDebugEnabled()) {
				LOG.debug("Adding {} blank fields for value mapping {}", fieldRequired, mapping);
			}
			for (int fieldCount = 0; fieldCount < fieldRequired; fieldCount++) {
				csvFields.add(null);
			}
		}
	}

	/**
	 * Creates a list of values, ready to be output in to a CSV file.
	 *
	 * @return a possibly empty string containing a mixture of null and non-null values.
	 */
	private List<String> createCsvValues() {
		List<String> csvFields = new ArrayList<String>();

		createCsvValues(csvFields, this.rootContainer);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Created record as follows ({})", StringUtil.collectionToString(csvFields, ",", null));
		}
		return csvFields;
	}

	/**
	 * Delegates to {@link #createCsvValuesFromContainer(List, IExtractionResultsContainer)} or
	 * {@link #createCsvValuesFromMapping(List, IExtractionResultsValues)} depending on the type of <code>context</code>.
	 *
	 * @param csvFields the list of CSV files to append to.
	 * @param context the results to extract data from.
	 */
	private void createCsvValues(List<String> csvFields, IExtractionResults context) {
		// BUG Fix this horrible code
		if (context instanceof IExtractionResultsContainer) {
			createCsvValuesFromContainer(csvFields, (IExtractionResultsContainer) context);
		} else {
			createCsvValuesFromMapping(csvFields, (IExtractionResultsValues) context);
		}
	}

	/**
	 * Adds to the passed <code>csvFields</code> list the relevant {@link ExtractedField} values found by this container.
	 *
	 * @param csvFields a list of fields being built up for a single output record.
	 * @param context the {@link AbstractExtractionContext} the relevant (contained) values of which should be added to <code>csvFields</code>.
	 */
	private void createCsvValuesFromContainer(List<String> csvFields, IExtractionResultsContainer context) {
		IMappingContainer mapping = context.getMapping();
		switch (mapping.getMultiValueBehaviour()) {
			case GREEDY:
				int containerIterationCount = 0;
				int resultIndexForTrace = 0;
				List<List<IExtractionResults>> allResults = context.getChildren();
				for (List<IExtractionResults> results : allResults) {
					for (IExtractionResults child : results) {
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
				List<IExtractionResults> results = context.getResultsSetAt(valueIndex);
				for (IExtractionResults child : results) {
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
	 * Adds the fields generated by a single {@link MappingExtractionContext}, taking in to account multi-value behaviour and minimum numbers of
	 * fields.
	 *
	 * @param csvFields the ordered list of CSV fields to add to.
	 * @param context the context to extract the values from.
	 */
	private void createCsvValuesFromMapping(List<String> csvFields, IExtractionResultsValues context) {
		switch (context.getMultiValueBehaviour()) {
			case GREEDY:
				/* Greedy mappings output as much as they can */
				List<String> fields = context.getResults();
				if (LOG.isDebugEnabled()) {
					LOG.debug("Greedily adding all fields {} to as output of {}", StringUtil.collectionToString(fields, ", ", null), context);
				}
				csvFields.addAll(fields);

				// Add extra null fields required for alignment with other records
				int fieldsRequired = context.getMapping().getHighestFoundValueCount();
				int extraFieldsRequired = fieldsRequired - fields.size();
				LOG.debug("Adding {} extra fields to pad out to {}", extraFieldsRequired, fieldsRequired);
				for (int i = 0; i < extraFieldsRequired; i++) {
					csvFields.add(null);
				}

				break;
			case LAZY:
				/* The most typical option: just process the next value and move on */
				int valueIndex = getIndexForGroup(context.getGroupNumber());
				String field = context.getValueAt(valueIndex);
				LOG.debug("Lazy eval of child {} ({}) from {}", valueIndex, field, context);
				csvFields.add(field);
				break;
			case DEFAULT:
				throw new BugException("Found DEFAULT MultiValueBehaviour whilst transforming to output, this should have been resolved by "
								+ "Mapping.getMultiValueBehaviour().");
			default:
				throw new BugException("Found unexpected (%s) value in Mapping.getMultiValueBehaviour().", context.getMultiValueBehaviour());
		}
	}

	/**
	 * Determines the current index of the passed <code>group</code> in the rootContainer set iteration.
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
	 *
	 * @return the next set of values to write to the CSV file.
	 */
	@Override
	public List<String> next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		List<String> values = createCsvValues();
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
