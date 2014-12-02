package com.locima.xml2csv.extractor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.configuration.IValueMapping;
import com.locima.xml2csv.util.StringUtil;

/**
 * Used to store the context of a set of extracted values for 0..n executions of a {@link IValueMapping} against multiple input documents.
 */
public class MappingExtractionContext extends ExtractionContext {

	private static final Logger LOG = LoggerFactory.getLogger(MappingExtractionContext.class);
	private IValueMapping mapping;
	private int maxResultsFound;

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
	public ExtractedRecordList evaluate(XdmNode mappingRoot) throws DataExtractorException {
		String fieldName = this.mapping.getBaseName();
		LOG.trace("Extracting value for \"{}\" using XPath \"{}\"", fieldName, this.mapping.getValueXPath().getSource());
		List<ExtractedField> values = new ArrayList<ExtractedField>();
		int minValueCount = this.mapping.getMinValueCount();
		int maxValueCount = this.mapping.getMaxValueCount();

		// If there is no mapping root, that means that the XPath
		if (mappingRoot != null) {
			XPathSelector selector = this.mapping.getValueXPath().evaluate(mappingRoot);
			int valueCount;
			Iterator<XdmItem> resultIter = selector.iterator();
			for (valueCount = 1; resultIter.hasNext(); valueCount++) { // Value count is just a counter, so start at one as it makes more sense for
				// the user, and makes the comparison with maxValueCount simpler.
				String value = resultIter.next().getStringValue();
				if ((value != null) && this.mapping.requiresTrimWhitespace()) {
					value = value.trim();
				}
				ExtractedField ef = new ExtractedField(fieldName, value);
				values.add(ef);

				if (LOG.isDebugEnabled()) {
					LOG.debug("Field \"{}\" found {} value(s) \"{}\" found after executing XPath \"{}\"", fieldName, values.size(), ef,
									this.mapping.getValueXPath().getSource());
				}

				// If we've found values up to this.maxValueCount then abort the loop.
				// Don't worry about minValueCount, that's dealt with by the CSV writers
				if ((this.mapping.getMaxValueCount() > 0) && (valueCount == this.mapping.getMaxValueCount())) {
					if (resultIter.hasNext()) {
						if (LOG.isWarnEnabled()) {
							LOG.warn("Discarded at least 1 value from mapping {} as maxValueCount reached limit of {}", this, maxValueCount);
						}
					}
					break;
				}
			}
			if (LOG.isInfoEnabled() && (minValueCount > 0) && (values.size() < minValueCount)) {
				LOG.info("Adding another {} empty values to {} mapping to take up to minValueCount of {}", minValueCount - values.size(), this,
								minValueCount);
			}
		}

		this.maxResultsFound = Math.max(this.maxResultsFound, values.size());

		ExtractedRecordList rs = new ExtractedRecordList();
		if (LOG.isTraceEnabled()) {
			LOG.trace("Adding values to {}: {}", this.mapping, StringUtil.collectionToString(values, ",", "\""));
		}
		rs.add(new ExtractedRecord(this, values));
		return rs;
	}
}
