package com.locima.xml2csv.model;

/**
 * Represents a single field mapping that retrieves one mapped value from an XML input.
 * <p>
 * By "one mapped value" we mean a single specification of where data comes from. Of course we might be multiple <em>instances</em> of that one mapped
 * value. E.g. <code>Name_1</code>, <code>Name_2</code>, <code>Name_3</code>, and so on.
 */
public interface ISingleMapping extends IMapping {

	/**
	 * Retrieves the name of the column that this mapping will return.
	 * 
	 * @return the name of the column (written to the first line of an output file).
	 */
	String getColumnName();

	/**
	 * Defines how multiple values being found by this mapping, for a single input, should be named.
	 * @return an inline format specification.
	 */
	InlineFormat getInlineFormat();

}
