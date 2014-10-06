package com.locima.xml2csv.inputparser;

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.ArgumentException;
import com.locima.xml2csv.StringUtil;
import com.locima.xml2csv.extractor.DataExtractorException;

/**
 * Represents a single column to XPath mapping.
 */
public class Mapping implements IMapping {

	private static final Logger LOG = LoggerFactory.getLogger(Mapping.class);

	/**
	 * The name of the column that will be created by this mapping.
	 */
	private String columnName;

	private InlineFormat inlineFormat = InlineFormat.WithCount;

	/**
	 * Tracks the number of instances found at once by this mapping. This is needed when doing inline mappings.
	 */
	private int maxInstanceCount;

	private int minimumInstanceCount = 1;

	/**
	 * The XPath expression that is executed against an XML element to find a mapped value.
	 */
	private XPathValue xPathExpr;

	/**
	 * Constructs a new instance.
	 *
	 * @param columnName specified the name of the column in the output that will be extracted from the XML. Must not be null or empty.
	 * @param xPathExpression the XPath expression that will extract the data for the column. Must not be null.
	 */
	public Mapping(String columnName, XPathValue xPathExpression) {
		if (StringUtil.isNullOrEmpty(columnName)) {
			throw new ArgumentException("columnName", "must be non-null and greater than zero length.");
		}
		if (xPathExpression == null) {
			throw new ArgumentException("xPathExpression", "must not be null.");
		}
		this.columnName = columnName;
		this.xPathExpr = xPathExpression;
	}

	@Override
	public List<String> evaluate(XdmNode mappingRoot, boolean trimWhitespace) throws DataExtractorException {
		LOG.trace("Extracting value for {} using {}", this.columnName, this.xPathExpr.getSource());
		XPathSelector selector = this.xPathExpr.evaluate(mappingRoot);
		List<String> values = new ArrayList<String>();
		for (XdmItem item : selector) {
			String value = item.getStringValue();
			if ((value != null) && trimWhitespace) {
				value = value.trim();
			}
			values.add(value);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Column {} value {} {} found after executing XPath {}", this.columnName, values.size(), value, this.xPathExpr.getSource());
			}
		}
		int instanceCount = values.size();

		// Add any blanks where maxInstanceCount is more than valuesSize
		if (instanceCount < this.maxInstanceCount) {
			LOG.trace("Adding {} blank fields to make up to {}", this.maxInstanceCount - instanceCount, this.maxInstanceCount);
			for (int i = instanceCount; i < instanceCount; i++) {
				values.add(StringUtil.EMPTY_STRING);
			}
		}

		this.maxInstanceCount = Math.max(this.maxInstanceCount, instanceCount);
		if (instanceCount == 0) {
			LOG.debug("No value for Column {} was found after executing XPath {}", this.columnName, this.xPathExpr.getSource());
		}
		return values;
	}

	public String getColumnName() {
		return this.columnName;
	}

	@Override
	public InlineFormat getInstanceFormat() {
		return this.inlineFormat;
	}

	@Override
	public int getMaxInstanceCount() {
		return Math.max(this.maxInstanceCount, this.minimumInstanceCount);
	}

	@Override
	public void setInlineFormat(InlineFormat format) {
		this.inlineFormat = (format == null) ? InlineFormat.NoCounts : format;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Mapping(");
		sb.append(this.columnName);
		sb.append(")");
		return sb.toString();
	}

	@Override
	public int getColumnNames(List<String> columnNames, String parentName, int parentCount) {
		String mappingName = this.getColumnName();
		int count = this.getMaxInstanceCount();
		InlineFormat format = this.getInstanceFormat();
		for (int mappingIterationCount = 0; mappingIterationCount < count; mappingIterationCount++) {
			columnNames.add(format.format(mappingName, mappingIterationCount, parentName, parentCount));
		}
		return count;
	}

}
