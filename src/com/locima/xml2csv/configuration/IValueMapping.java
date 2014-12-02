package com.locima.xml2csv.configuration;

public interface IValueMapping extends IMapping {

	String getBaseName();

	int getMaxValueCount();

	int getMinValueCount();

	XPathValue getValueXPath();

	boolean requiresTrimWhitespace();

}
