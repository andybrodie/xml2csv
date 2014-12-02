package com.locima.xml2csv.extractor;

import com.locima.xml2csv.util.EqualsUtil;

public class ExtractedField {

	private String fieldName;
	private String value;

	public ExtractedField(String fieldInfo, String value) {
		this.fieldName = fieldInfo;
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj == null) || (!(obj instanceof ExtractedField))) {
			return false;
		}
		ExtractedField that = (ExtractedField) obj;
		return EqualsUtil.areEqual(this.fieldName, that.fieldName) && EqualsUtil.areEqual(this.value, that.value);
	}

	public String getFieldInfo() {
		return this.fieldName;
	}

	public String getValue() {
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
