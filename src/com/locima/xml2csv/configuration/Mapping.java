package com.locima.xml2csv.configuration;

import com.locima.xml2csv.util.EqualsUtil;

/**
 * Represents a single column to XPath mapping.
 */
public class Mapping extends AbstractMapping implements IValueMapping {

	/**
	 * The XPath to execute against an input document to find values for this mapping.
	 */
	private XPathValue valueXPath;

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof Mapping) {
			// CHECKSTYLE:OFF Can't think of another way to provide a base class implementation of equals
			Mapping that = (Mapping) obj;
			// CHECKSTYLE:ON
			return EqualsUtil.areEqual(getNameFormat(), that.getNameFormat()) && EqualsUtil.areEqual(getGroupNumber(), that.getGroupNumber())
							&& EqualsUtil.areEqual(getMultiValueBehaviour(), that.getMultiValueBehaviour())
							&& EqualsUtil.areEqual(this.valueXPath, that.valueXPath) && EqualsUtil.areEqual(getName(), that.getName())
							&& EqualsUtil.areEqual(getMinValueCount(), that.getMinValueCount())
							&& EqualsUtil.areEqual(getMaxValueCount(), that.getMaxValueCount());
		} else {
			return false;
		}
	}

	/**
	 * Retrieve the XPath statement that will execute this mapping.
	 *
	 * @return the XPath statement that will execute this mapping.
	 */
	@Override
	public XPathValue getValueXPath() {
		return this.valueXPath;
	}

	/**
	 * Returns a hash code solely based on the name of the field, as this is the only thing that really makes a difference between storing and
	 * indexing.
	 *
	 * @return the hash code of the base name of this definition.
	 */
	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	/**
	 * Returns whether or not whitespace should be trimmed from found values in the document.
	 *
	 * @return whether or not whitespace should be trimmed from found values in the document.
	 */
	@Override
	public boolean requiresTrimWhitespace() {
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Mapping(");
		final String separator = ", ";
		sb.append(getName());
		sb.append(separator);
		sb.append(getNameFormat());
		sb.append(separator);
		sb.append(getGroupNumber());
		sb.append(separator);
		sb.append(getMultiValueBehaviour());
		sb.append(", \"");
		sb.append(getValueXPath().getSource());
		sb.append("\", ");
		sb.append(getMinValueCount());
		sb.append(separator);
		sb.append(getMaxValueCount());
		sb.append(separator);
		sb.append(getHighestFoundValueCount());
		sb.append(')');
		return sb.toString();
	}

	/**
	 * Sets the XPath expression that, when executed, will find the values to insert in the CSV output.
	 * 
	 * @param valueXPath the XPath expression that, when executed, will find the values to insert in the CSV output.
	 */
	public void setValueXPath(XPathValue valueXPath) {
		this.valueXPath = valueXPath;
	}

}
