package com.locima.xml2csv;

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

}
