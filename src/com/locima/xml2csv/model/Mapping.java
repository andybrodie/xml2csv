package com.locima.xml2csv.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.EqualsUtil;
import com.locima.xml2csv.extractor.DataExtractorException;

/**
 * Represents a single column to XPath mapping.
 */
public class Mapping extends AbstractMapping implements IMapping {

	private static final Logger LOG = LoggerFactory.getLogger(Mapping.class);

	/**
	 * The baseName of the field that will be created by this mapping.
	 */
	private String baseName;

	/**
	 * Creates a new immutable Field Definition.
	 *
	 * @param baseName the outputName of the field, must a string of length > 0.
	 * @param valueXPath a compiled XPath expression that will extract the values required for this field.
	 * @param format the format to be used for the {@link Mapping} instance that this method creates.
	 * @param groupNumber the group number for this field definition.
	 * @param multiValueBehaviour defines what should happen when multiple values are found for a single evaluation for this mapping.
	 */
	public Mapping(String baseName, NameFormat format, int groupNumber, MultiValueBehaviour multiValueBehaviour, XPathValue valueXPath) {
		super(format, groupNumber, multiValueBehaviour, valueXPath);
		this.baseName = baseName;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof Mapping) {
			Mapping that = (Mapping) obj;
			return EqualsUtil.areEqual(this.baseName, that.baseName) && super.equals(that);
		} else {
			return false;
		}
	}

	@Override
	public RecordSet evaluate(XdmNode mappingRoot, boolean trimWhitespace) throws DataExtractorException {
		String fieldName = getBaseName();
		LOG.trace("Extracting value for {} using {}", fieldName, getValueXPath().getSource());
		List<String> values = new ArrayList<String>();

		if (mappingRoot != null) {
			XPathSelector selector = getValueXPath().evaluate(mappingRoot);
			for (XdmItem item : selector) {
				String value = item.getStringValue();
				if ((value != null) && trimWhitespace) {
					value = value.trim();
				}
				values.add(value);
				if (LOG.isDebugEnabled()) {
					LOG.debug("{} found {} value(s) {} found after executing XPath {}", fieldName, values.size(), value, getValueXPath().getSource());
				}
			}
		}
		int instanceCount = values.size();

		// // Add any blanks where maxInstanceCount is more than valuesSize
		// int maxInstances = getMaxInstanceCount();
		// if (instanceCount < maxInstances) {
		// if (LOG.isTraceEnabled()) {
		// LOG.trace("Adding {} blank fields to make up to {} in mapping {}", this.maxInstanceCount - instanceCount, this.getMaxInstanceCount,
		// this) ;
		// }
		// for (int i = instanceCount; i < maxInstances; i++) {
		// values.add(StringUtil.EMPTY_STRING);
		// }
		// }
		//
		// this.maxInstanceCount = Math.max(this.maxInstanceCount, instanceCount);
		// if (instanceCount == 0) {
		// LOG.debug("No value for {} was found after executing XPath {}", this.field, this.xPathExpr.getSource());
		// }

		RecordSet rs = new RecordSet();
		rs.addResults(this, values);
		return rs;
	}

	public String getBaseName() {
		return this.baseName;
	}

	/**
	 * Returns a hash code solely based on the name of the field, as this is the only thing that really makes a difference between storing and
	 * indexing.
	 *
	 * @return the hash code of the base name of this definition.
	 */
	@Override
	public int hashCode() {
		return this.baseName.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("FieldDefinition(");
		sb.append(this.baseName);
		sb.append(',');
		sb.append(getNameFormat());
		sb.append(',');
		sb.append(getGroupNumber());
		sb.append(',');
		sb.append(getMultiValueBehaviour());
		sb.append(',');
		sb.append(getValueXPath().getSource());
		sb.append(')');
		return sb.toString();
	}

}
