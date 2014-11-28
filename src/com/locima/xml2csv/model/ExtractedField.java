package com.locima.xml2csv.model;

import com.locima.xml2csv.EqualsUtil;

public class ExtractedField {

	private String fieldInfo;
	private String value;

	public ExtractedField(String fieldInfo, String value) {
		this.fieldInfo = fieldInfo;
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj == null) || (!(obj instanceof ExtractedField))) {
			return false;
		}
		ExtractedField that = (ExtractedField) obj;
		return EqualsUtil.areEqual(this.fieldInfo, that.fieldInfo) && EqualsUtil.areEqual(this.value, that.value);
	}

	public String getFieldInfo() {
		return this.fieldInfo;
	}

	public String getValue() {
		return this.value;
	}

	@Override
	public int hashCode() {
		return this.fieldInfo.hashCode() ^ this.value.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("EF(");
		sb.append(this.fieldInfo);
		sb.append(',');
		sb.append(this.value);
		sb.append(')');
		return sb.toString();
		
	}
}
