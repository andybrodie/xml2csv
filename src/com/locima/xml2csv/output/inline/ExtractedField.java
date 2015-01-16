package com.locima.xml2csv.output.inline;

import com.locima.xml2csv.util.EqualsUtil;

/**
 * A simple name/value pair of field name mapped to field value within a single output record.
 */
public class ExtractedField implements java.io.Serializable {

	/**
	 * Ensure that this class is serialisable.
	 */
	private static final long serialVersionUID = 0L;

	private final int[] fieldName;
	private final String value;

	public ExtractedField(int[] fieldName, String fieldValue) {
		this.fieldName = fieldName;
		this.value = fieldValue;
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj == null) || (!(obj instanceof ExtractedField))) {
			return false;
		}
		ExtractedField that = (ExtractedField) obj;
		return EqualsUtil.areEqual(this.fieldName, that.fieldName) && EqualsUtil.areEqual(this.value, that.value);
	}

	public int[] getFieldName() {
		return this.fieldName;
	}

	public String getFieldValue() {
		return this.value;
	}

	@Override
	public int hashCode() {
		return this.fieldName.hashCode() ^ this.value.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("EF(");
		sb.append(this.fieldName);
		sb.append(',');
		sb.append(this.value);
		sb.append(')');
		return sb.toString();

	}
}
