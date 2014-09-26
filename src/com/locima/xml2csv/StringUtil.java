package com.locima.xml2csv;

/**
 * Utility methods for dealing with Strings.
 */
public class StringUtil {

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

}
