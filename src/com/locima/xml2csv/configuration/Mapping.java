package com.locima.xml2csv.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single column to XPath mapping.
 */
public class Mapping extends AbstractMapping implements IValueMapping {

	private static final Logger LOG = LoggerFactory.getLogger(Mapping.class);

	/**
	 * Creates a new mapping configuration object for fields in the output CSV file.
	 *
	 * @param parent the parent mapping container.
	 * @param baseName the outputName of the field, must a string of length > 0.
	 * @param valueXPath a compiled XPath expression that will extract the values required for this field.
	 * @param format the format to be used for the {@link Mapping} instance that this method creates.
	 * @param groupNumber the group number for this field definition.
	 * @param multiValueBehaviour defines what should happen when multiple values are found for a single evaluation for this mapping.
	 * @param minValueCount the minimum number of values each execution of this mapping must yield.
	 * @param maxValueCount the maximum number of values each execution of this mapping must yield.
	 */
	// CHECKSTYLE:OFF Number of parameters is reasonable here, don't want loads of extra setter methods for final fields.
	public Mapping(IMappingContainer parent, String baseName, NameFormat format, int groupNumber, MultiValueBehaviour multiValueBehaviour,
					XPathValue valueXPath, int minValueCount, int maxValueCount) {
		// CHECKSTYLE:ON
		super(parent, baseName, format, groupNumber, multiValueBehaviour, valueXPath, minValueCount, maxValueCount);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof Mapping) {
			Mapping that = (Mapping) obj;
			return super.equals(that);
		} else {
			return false;
		}
	}

	/**
	 * Returns a hash code solely based on the name of the field, as this is the only thing that really makes a difference between storing and
	 * indexing.
	 *
	 * @return the hash code of the base name of this definition.
	 */
	@Override
	public int hashCode() {
		// There's no state expicit to Mapping, so using the superclass's hashCode implementation will work here
		// TODO Think about the wisdom of this a bit more, it feels wrong.
		return super.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Mapping(");
		final String separator = ", ";
		sb.append(getBaseName());
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
