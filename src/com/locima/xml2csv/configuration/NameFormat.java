package com.locima.xml2csv.configuration;

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
public class NameFormat {

	public static final NameFormat NO_COUNTS = new NameFormat("%1$s");

	public static final NameFormat WITH_COUNT = new NameFormat("%1$s_%2$d");

	public static final NameFormat WITH_COUNT_AND_PARENT_COUNT = new NameFormat("%1$s_%4$d_%2$d");

	public static final NameFormat WITH_PARENT_COUNT = new NameFormat("%1$s_%4$d");

	/**
	 * Parse the predefined format name or bespoke format specification in to an {@link NameFormat} instance.
	 *
	 * @param predefinedFormatName the name of a specific style.
	 * @param formatSpec if <code>predefinedFormatName</code> is {@link , this allows a custom style to be defined.
	 * @param defaultValue a default value to return if neither of the other parameters yield a usable format.
	 * @return a name format.
	 */
	public static NameFormat parse(String predefinedFormatName, String formatSpec, NameFormat defaultValue) {
		NameFormat format;
		if (formatSpec != null) {
			format = new NameFormat(formatSpec);
		} else if (predefinedFormatName != null) {
			if ("NoCounts".equals(predefinedFormatName)) {
				format = NameFormat.NO_COUNTS;
			} else if ("WithCount".equals(predefinedFormatName)) {
				format = NameFormat.WITH_COUNT;
			} else if ("WithParentCount".equals(predefinedFormatName)) {
				format = NameFormat.WITH_PARENT_COUNT;
			} else if ("WithCountAndParentCount".equals(predefinedFormatName)) {
				format = NameFormat.WITH_COUNT_AND_PARENT_COUNT;
			} else if ("Custom".equals(predefinedFormatName)) {
				format = new NameFormat(formatSpec);
			} else {
				throw new IllegalStateException(
								"Unknown format found, this means that the XSD is wrong as it's permitted a value that isn't supported.");
			}
		} else {
			format = defaultValue;
		}
		return format;
	}

	/**
	 * The formatting string of this instance.
	 */
	private String format;

	/**
	 * Creates an inline format with a specific syntax (see {@link NameFormat} for a description of that syntax).
	 *
	 * @param format the string that defined the inline format. Must not be null.
	 */
	public NameFormat(String format) {
		if (null == format) {
			throw new ArgumentNullException("format");
		}
		this.format = format;
	}

	/**
	 * Applies this inline format to the parameters passed to give a full column name.
	 *
	 * @param baseFieldName the base column name of the mapping; e.g. <code>Name</code>, <code>Age</code> or <code>Address Line</code>
	 * @param iterationNumber a number, starting at 0, that indicates the index of the value we've found, then encountering multiple values for a
	 *            single mapping.
	 * @param ancestorContext names and indices of ancestors of the current mapping.
	 * @return a formatted string, used as a column name.
	 */
	public String format(String baseFieldName, int iterationNumber, MappingIndexAncestors ancestorContext) {
		return String.format(this.format, ancestorContext.getFormatArgs(baseFieldName, iterationNumber));
	}

	@Override
	public String toString() {
		return "NameFormat(\"" + this.format + "\")";
	}
}
