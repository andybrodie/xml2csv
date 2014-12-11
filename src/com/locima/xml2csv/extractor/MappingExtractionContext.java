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
import com.locima.xml2csv.BugException;
import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IValueMapping;
import com.locima.xml2csv.configuration.NameFormat;
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
	 * The most number of values this has found across all documents.
	 */
	private int maxResultsFound;

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

	@Override
	public void clearResults() {
		throw new BugException("Not implemented");
	}

	private String createName(NameFormat nameFormat, int i) {
		StringBuilder sb = new StringBuilder();
		return nameFormat.format(this.mapping.getBaseName(), i, getParent().getMapping().getContainerName(), getParent().getIndex());
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
		resetContext();
		String fieldName = this.mapping.getBaseName();

		if (LOG.isTraceEnabled()) {
			LOG.trace("Extracting value for \"{}\" using XPath \"{}\"", fieldName, this.mapping.getValueXPath().getSource());
		}

		List<String> values = new ArrayList<String>();
		int maxValueCount = this.mapping.getMaxValueCount();

		XPathSelector selector = this.mapping.getValueXPath().evaluate(mappingRoot);
		Iterator<XdmItem> resultIter = selector.iterator();
		for (int valueCount = 1; resultIter.hasNext(); valueCount++) { 
			// Value count is just a counter, so start at one as it makes more sense for
			// the user, and makes the comparison with maxValueCount simpler.
			String value = resultIter.next().getStringValue();
			if ((value != null) && this.mapping.requiresTrimWhitespace()) {
				value = value.trim();
			}
			values.add(value);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Field \"{}\" found value({}) \"{}\" found after executing XPath \"{}\"", fieldName, values.size(), value,
								this.mapping.getValueXPath().getSource());
			}

			/* If we've found values up to this.maxValueCount then discard all other values.
			 * Don't worry about minValueCount, that's dealt with by the CSV writers
			 */
			if ((this.mapping.getMaxValueCount() > 0) && (valueCount == this.mapping.getMaxValueCount())) {
				if (resultIter.hasNext()) {
					if (LOG.isWarnEnabled()) {
						LOG.warn("Discarded at least 1 value from mapping {} as maxValueCount reached limit of {}", this, maxValueCount);
					}
				}
				break;
			}
			incrementContext();
		}

		this.maxResultsFound = Math.max(this.maxResultsFound, values.size());

		if (LOG.isTraceEnabled()) {
			LOG.trace("Adding values to {}: {}", this.mapping, StringUtil.collectionToString(values, ",", "\""));
		}
		this.results = values;
	}

	public List<ExtractedField> getAllValues() {
		List<ExtractedField> fields = new ArrayList<ExtractedField>();
		for (int i = 0; i < this.results.size(); i++) {
			String name = createName(this.mapping.getNameFormat(), i);
			ExtractedField field = new ExtractedField(name, this.results.get(i));
			fields.add(field);
		}
		return fields;
	}

	@Override
	public IMapping getMapping() {
		return this.mapping;
	}

	public ExtractedField getValueAt(int valueIndex) {
		if (this.results.size() > valueIndex) {
			String name = createName(this.mapping.getNameFormat(), valueIndex);
			ExtractedField field = new ExtractedField(name, this.results.get(valueIndex));
			return field;
		}
		return null;
	}

	/**
	 * Resets our current context.
	 */
	@Override
	public void resetContext() {
		setIndex(0);
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
