package com.locima.xml2csv.configuration;

/**
 * A mapping that when executed extracts string values from XML documents.
 */
public interface IValueMapping extends IMapping {

	/**
	 * Return the base name of fields extracted by this mapping.
	 *
	 * @return the base name of fields extracted by this mapping.
	 */
	String getBaseName();

	/**
	 * Retrieve the XPath that will extract values from input XML documents.
	 *
	 * @return the XPath that will extract values from input XML documents.
	 */
	XPathValue getValueXPath();

	/**
	 * Returns whether values found by this mapping should have whitespace trimmed using {@link String#trim()}.
	 *
	 * @return whether values found by this mapping should have whitespace trimmed using {@link String#trim()}.
	 */
	boolean requiresTrimWhitespace();

}
