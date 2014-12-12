package com.locima.xml2csv.configuration;

public interface IValueMapping extends IMapping {

	String getBaseName();

	XPathValue getValueXPath();

	boolean requiresTrimWhitespace();

}
