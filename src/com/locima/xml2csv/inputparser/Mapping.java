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

	private String columnName;

	private XPathValue xPathExpr;

	private int maxInstanceCount;

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
		int valuesSize = values.size();
		this.maxInstanceCount = Math.max(this.maxInstanceCount, valuesSize);
		if (valuesSize == 0) {
			LOG.debug("No value for Column {} was found after executing XPath {}", this.columnName, this.xPathExpr.getSource());
		}
		return values;
	}

	@Override
	public List<String> getColumnNames() {
		List<String> columnNames = new ArrayList<String>(1);
		columnNames.add(this.columnName);
		return columnNames;
	}
	
	@Override
	public int getMaxInstanceCount() {
		return this.maxInstanceCount;
	}

}
