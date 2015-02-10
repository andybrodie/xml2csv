package com.locima.xml2csv.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.util.EqualsUtil;

/**
 * Represents a single column to XPath mapping.
 */
public class Mapping extends AbstractMapping implements IValueMapping {

	private static final Logger LOG = LoggerFactory.getLogger(Mapping.class);

	/**
	 * The XPath to execute against an input document to find values for this mapping.
	 */
	private XPathValue valueXPath;

	/**
	 * Creates a new immutable Field Definition.
	 *
	 * @param parent the parent of this mapping. May be null if this is a top level mapping container.
	 * @param baseName the name of this mapping, must be unique within the configuration.
	 * @param minValueCount the fewest number of values, or sets of values, that may be returned by this mapping.
	 * @param maxValueCount the most number of values, or sets of values, that may be returned by this mapping.
	 * @param valueXPath a compiled XPath expression that will extract the values required for this field.
	 * @param format the format to be used for the {@link Mapping} instance that this method creates.
	 * @param groupNumber the group number for this field definition.
	 * @param multiValueBehaviour defines what should happen when multiple values are found for a single evaluation for this mapping.
	 */
	public Mapping(IMappingContainer parent, String baseName, NameFormat format, int groupNumber, MultiValueBehaviour multiValueBehaviour,
					XPathValue valueXPath, int minValueCount, int maxValueCount) {
		super(parent, baseName, format, groupNumber, multiValueBehaviour, minValueCount, maxValueCount);
		this.valueXPath = valueXPath;
	}

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

}
