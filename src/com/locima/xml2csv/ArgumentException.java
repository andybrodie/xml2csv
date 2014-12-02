package com.locima.xml2csv;

/**
 * Throws if an argument passed to a method was invalid, using a standardised error message.
 */
public class ArgumentException extends IllegalArgumentException {

	private static final long serialVersionUID = 1739962004704462299L;

	/**
	 * Throws if an argument to a method was invalid with a default message.
	 *
	 * @param argumentName the name of the argument / parameter than was invalid.
	 */
	public ArgumentException(String argumentName) {
		super(argumentName + " argument was invalid.");
	}

	/**
	 * Throws if an argument to a method was invalid.
	 *
	 * @param argumentName the name of the argument / parameter than was invalid.
	 * @param reason a reason as to why the argument was invalid.
	 */
	public ArgumentException(String argumentName, String reason) {
		super(argumentName + " " + reason);
	}

}
