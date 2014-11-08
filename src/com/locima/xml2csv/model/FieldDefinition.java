package com.locima.xml2csv.model;

import com.locima.xml2csv.EqualsUtil;

/**
 * Defines a how a single field will be created in an output CSV file.
 */
public class FieldDefinition {

	/**
	 * The baseName of the field that will be created by this mapping.
	 */
	private String baseName;
	private int groupNumber;
	private MultiValueBehaviour multiValueBehaviour;
	private NameFormat nameFormat;
	private XPathValue valueXPath;

	/**
	 * Creates a new immutable Field Definition.
	 *
	 * @param baseName the outputName of the field, must a string of length > 0.
	 * @param valueXPath a compiled XPath expression that will extract the values required for this field.
	 * @param format the format to be used for the {@link Mapping} instance that this method creates.
	 * @param groupNumber the group number for this field definition.
	 * @param multiValueBehaviour defines what should happen when multiple values are found for a single evaluation for this mapping.
	 */
	public FieldDefinition(String baseName, NameFormat format, int groupNumber, MultiValueBehaviour multiValueBehaviour, XPathValue valueXPath) {
		this.baseName = baseName;
		this.groupNumber = groupNumber;
		this.multiValueBehaviour = multiValueBehaviour;
		this.nameFormat = format == null ? NameFormat.NO_COUNTS : format;
		this.valueXPath = valueXPath;
	}

	/**
	 * Creates a new immutable Field Definition.
	 *
	 * @param baseName the outputName of the field, must a string of length > 0.
	 * @param xPathExpression a compiled XPath expression that will extract the values required for this field.
	 * @param formatSpecifier a formatting string that will be used to tailor the base name, see {@link NameFormat}.
	 * @param groupNumber the group number for this field definition.
	 * @param multiValueBehaviour defines what should happen when multiple values are found for a single evaluation for this mapping.
	 */
	public FieldDefinition(String baseName, String formatSpecifier, int groupNumber, MultiValueBehaviour multiValueBehaviour,
					XPathValue xPathExpression) {
		this(baseName, new NameFormat(formatSpecifier), groupNumber, multiValueBehaviour, xPathExpression);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof FieldDefinition) {
			FieldDefinition that = (FieldDefinition) obj;
			return EqualsUtil.areEqual(this.baseName, that.baseName) && EqualsUtil.areEqual(this.nameFormat, that.nameFormat)
							&& EqualsUtil.areEqual(this.groupNumber, that.groupNumber)
							&& EqualsUtil.areEqual(this.multiValueBehaviour, that.multiValueBehaviour)
							&& EqualsUtil.areEqual(this.valueXPath, that.valueXPath);
		} else {
			return false;
		}
	}

	public String getBaseName() {
		return this.baseName;
	}

	public int getGroupNumber() {
		return this.groupNumber;
	}

	public MultiValueBehaviour getMultiValueBehaviour() {
		return this.multiValueBehaviour;
	}

	NameFormat getNameFormat() {
		return this.nameFormat;
	}

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
		return this.baseName.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("FieldDefinition(");
		sb.append(this.baseName);
		sb.append(',');
		sb.append(this.nameFormat);
		sb.append(',');
		sb.append(this.groupNumber);
		sb.append(',');
		sb.append(this.multiValueBehaviour);
		sb.append(',');
		sb.append(this.valueXPath.getSource());
		sb.append(')');
		return sb.toString();
	}

}
