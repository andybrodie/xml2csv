package com.locima.xml2csv;

import java.util.List;

/**
 * Utility methods for dealing with Strings.
 */
public class StringUtil {

	/**
	 * An empty, zero length, non-null string.
	 */
	public static final String EMPTY_STRING = "";

	public static final String NULL_OR_EMPTY_MESSAGE = "Must be a string containing at least one character.";

	/**
	 * Returns true if the passed string is either null or has zero length.
	 * 
	 * @param s the string to test.
	 * @return true if the passed string is either null or has zero length, false otherwise.
	 */
	public static boolean isNullOrEmpty(String s) {
		return (s == null) || (s.length() == 0);
	}

	/**
	 * Prevents instantiation, this is a utility class with only static methods.
	 */
	private StringUtil() {
	}

	/**
	 * Converts an array of Strings to a comma separated list within a single String.
	 * @param strings the array to convert, may be null or empty, in which case an empty string is returned.
	 * @return a string, possibly empty, of all the members of the passed array, separated by commas.
	 */
	public static String toString(String[] strings) {
		if (strings == null || strings.length == 0) {
			return EMPTY_STRING;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < strings.length; i++) {
			sb.append(strings[i]);
			if (i < strings.length - 1) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	/**
	 * Escapes any string so that it can be added to a CSV. Specifically, if the value contains a double-quote, CR or LF then the entire value is
	 * wrapped in double-quotes. Also, any instances of double-quote are replaced with 2 double-quotes.
	 *
	 * @param value Any value that can be converted to a String. If null is passed, null is returned.
	 * @return A string suitable to be embedded in to a CSV file that will be read by Excel.
	 */
	public static String escapeForCsv(Object value) {
		String returnValue = value == null ? null : value.toString();
		boolean quotesRequired = false;
		if (value == null) {
			return null;
		}
		final String quote = "\"";
		if (returnValue.contains(quote)) {
			returnValue = returnValue.replace(quote, "\"\"");
			quotesRequired = true;
		}
		if (returnValue.contains("\n") || returnValue.contains(",") || returnValue.contains(";")) {
			quotesRequired = true;
		}

		return quotesRequired ? quote + returnValue + quote : returnValue;
	}

	/**
	 * Converts a list of values in to a single output line.
	 *
	 * @param fields the collection of strings that are the individual fields to output.
	 * @param fieldSeparator the character to use to separate all the values. Must not be null.
	 * @param wrapper a string to write before and after all the values. May be null (which means no wrapper written).
	 * @return a String, possibly empty, but never null.
	 */
	public static String collectionToString(List<?> fields, String fieldSeparator, String wrapper) {
		if (null == fields) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		if (null != wrapper) {
			sb.append(wrapper);
		}
		int size = fields.size();
		for (int i = 0; i < size; i++) {
			sb.append(StringUtil.escapeForCsv(fields.get(i)));
			if (i < (size - 1)) {
				sb.append(fieldSeparator);
			}
		}
		if (null != wrapper) {
			sb.append(wrapper);
		}
		return sb.toString();
	}


}
