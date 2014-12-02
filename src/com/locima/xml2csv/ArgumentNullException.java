package com.locima.xml2csv;

/**
 * Thrown when an argument or parameter to a method was null when this is not allowed.
 */
public class ArgumentNullException extends ArgumentException {

	private static final long serialVersionUID = 3495119751302269911L;

	/**
	 * Thrown when an argument or parameter to a method was null when this is not allowed.
	 *
	 * @param argumentName the name of the argument or parameter that was null.
	 */
	public ArgumentNullException(String argumentName) {
		super(argumentName, "was null, which is not allowed.");
	}

}
