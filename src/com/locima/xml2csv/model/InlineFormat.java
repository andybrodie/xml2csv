package com.locima.xml2csv.model;

import com.locima.xml2csv.ArgumentNullException;

/**
 * <p>
 * The fields in a formatting string are:
 * <table>
 * <thead>
 * <tr>
 * <th>Parameter index</th>
 * <th>Meaning</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>1</td>
 * <td>The current mapping name for the mapping.</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>The current mapping iteration number within the parent.</td>
 * </tr>
 * <tr>
 * <td>3</td>
 * <td>The current mapping name of the parent.</td>
 * </tr>
 * <tr>
 * <td>4</td>
 * <td>The current mapping's parent iteration number within it's own parent.</td>
 * </tr>
 * <tr>
 * <td>5</td>
 * <td></td>
 * </tr>
 * </tbody>
 * </table>
 */
public class InlineFormat {

	public static final InlineFormat NO_COUNTS = new InlineFormat("%1$s");

	public static final InlineFormat WITH_COUNT = new InlineFormat("%1$s_%2$d");

	public static final InlineFormat WITH_COUNT_AND_PARENT_COUNT = new InlineFormat("%1$s_%4$d_%2$d");
	
	public static final InlineFormat WITH_PARENT_COUNT = new InlineFormat("%1$s_%4$d");
	
	/**
	 * The formatting string of this instance.
	 */
	private String format;

	/**
	 * Creates an inline format with a specific syntax (see {@link InlineFormat} for a description of that syntax).
	 * @param format the string that defined the inline format.  Must not be null.
	 */
	public InlineFormat(String format) {
		if (null == format) {
			throw new ArgumentNullException("format");
		}
		this.format = format;
	}

	/**
	 * Applies this inline format to the parameters passed to give a full column name.
	 * @param baseColumnName the base column name of the mapping; e.g. <code>Name</code>, <code>Age</code> or <code>Address Line</code>
	 * @param iterationNumber a number, starting at 0, that indicates the index of the value we've found, then encountering multiple
	 * values for a single mapping.
	 * @param parentName the name of the parent of this mapping, typically {@link IMappingContainer#getOutputName()}.
	 * @param parentIterationNumber a number, starting at 0, that indicates the index of the value of the parent. 
	 * @return a formatted string, used as a column name.
	 */
	public String format(String baseColumnName, int iterationNumber, String parentName, int parentIterationNumber) {
		return String.format(this.format, baseColumnName, iterationNumber + 1, parentName, parentIterationNumber + 1);
	}
}
