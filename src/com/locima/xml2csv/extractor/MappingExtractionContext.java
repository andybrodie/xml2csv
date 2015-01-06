package com.locima.xml2csv.extractor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.ArgumentNullException;
import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IValueMapping;
import com.locima.xml2csv.util.StringUtil;

/**
 * Used to store the context of a set of extracted values for 0..n executions of a {@link IValueMapping} against multiple input documents.
 */
public class MappingExtractionContext extends ExtractionContext {

	private static final Logger LOG = LoggerFactory.getLogger(MappingExtractionContext.class);

	/**
	 * The mapping that should be used to evaluate input documents against.
	 */
	private IValueMapping mapping;

	/**
	 * The set of values extracted as a result of executing this mapping within the context of a single root of the parent. E.g. if the parent found 3
	 * mapping roots then this will create 3 instances of {@link MappingExtractionContext}.
	 */
	private List<String> results;

	public MappingExtractionContext(ContainerExtractionContext parent, IValueMapping mapping) {
		super(parent);
		this.mapping = mapping;
	}

	public MappingExtractionContext(IValueMapping mapping) {
		this(null, mapping);
	}

	/**
	 * Evaluates this mapping, using the passed XML node as a root for all XPath statements.
	 *
	 * @param rootNode the XML node from which to execute all XPath statements contained within mappings. Must not be null.
	 * @param trimWhitespace if true, then leading and trailing whitespace will be removed from all data values.
	 * @return an array of values extracted from the data. May be empty, but never null.
	 * @throws DataExtractorException if an error occurred whilst extracting data (typically this would be caused by bad XPath, or XPath invalid from
	 *             the <code>mappingRoot</code> specified).
	 */
	@Override
	public void evaluate(XdmNode mappingRoot) throws DataExtractorException {
		if (mappingRoot == null) {
			throw new ArgumentNullException("mappingRoot");
		}
		String fieldName = this.mapping.getBaseName();

		if (LOG.isTraceEnabled()) {
			LOG.trace("Extracting value for \"{}\" using XPath \"{}\"", fieldName, this.mapping.getValueXPath().getSource());
		}

		List<String> values = new ArrayList<String>();
		int maxValueCount = this.mapping.getMaxValueCount();

		XPathSelector selector = this.mapping.getValueXPath().evaluate(mappingRoot);
		Iterator<XdmItem> resultIter = selector.iterator();
		while (resultIter.hasNext()) {
			String value = resultIter.next().getStringValue();
			if ((value != null) && this.mapping.requiresTrimWhitespace()) {
				value = value.trim();
			}
			values.add(value);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Field \"{}\" found value({}) \"{}\" found after executing XPath \"{}\" (max: {})", fieldName, values.size(), value,
								this.mapping.getValueXPath().getSource(), maxValueCount);
			}

			if ((maxValueCount > 0) && ((values.size()) == maxValueCount)) {
				if (LOG.isWarnEnabled()) {
					if (resultIter.hasNext()) {
						LOG.warn("Discarded at least 1 value from mapping {} as maxValueCount reached limit of {}", this, maxValueCount);
					}
				}
				break;
			}

		}

		// Keep track of the most number of results we've found for a single invocation
		this.mapping.setHighestFoundValueCount(values.size());

		if (LOG.isTraceEnabled()) {
			LOG.trace("Adding values to {}: {}", this.mapping, StringUtil.collectionToString(values, ",", "\""));
		}
		this.results = values;
	}

	/**
	 * Gets all the values found by this evaluation of the mapping (i.e. against a single root node). This takes in to account the number of fields
	 * required to be output as opposed to just the number found (see {@link IMapping#getFieldCountForSingleRecord()}).
	 *
	 * @param namePrefix the prefix to be applied to each field name.
	 * @return an ordered list of values extracted from this mapping.
	 */
	public List<ExtractedField> getAllValues(String namePrefix) {
		int valueCountRequired = this.mapping.getFieldCountForSingleRecord();
		List<ExtractedField> fields = new ArrayList<ExtractedField>(valueCountRequired);
		for (int i = 0; i < valueCountRequired; i++) {
			fields.add(getValueAt(namePrefix, i));
		}
		return fields;
	}

	@Override
	public IMapping getMapping() {
		return this.mapping;
	}

	@Override
	public String getName() {
		return this.mapping.getBaseName();
	}

	/**
	 * Retrieve an extracted field instance for the value at the index given.
	 *
	 * @param valueIndex the index of the value to retrieve within this mapping.
	 * @return an extracted field for the index given. If there is no value for the field with this <c>valueIndex</c> then an {@link ExtractedField}
	 *         with the {@link ExtractedField#getFieldValue()} return value of <code>null</code> is returned. The
	 *         {@link ExtractedField#getFieldName()} will still be set correctly. This is required when padding is needed to keep the index of fields
	 *         wtihin a record consistent.
	 */
	public ExtractedField getValueAt(String namePrefix, int valueIndex) {

		String value;
		if (this.results.size() > valueIndex) {
			value = this.results.get(valueIndex);
		} else {
			value = null;
		}
		String name = namePrefix + valueIndex;
		ExtractedField field = new ExtractedField(name, value);
		return field;
	}

	@Override
	public int size() {
		return this.results.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("MEC(");
		sb.append(this.mapping);
		sb.append(")");
		return sb.toString();
	}

}
