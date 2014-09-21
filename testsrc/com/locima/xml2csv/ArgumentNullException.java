package com.locima.xml2csv;

public class ArgumentNullException extends ArgumentException {

	private static final long serialVersionUID = 3495119751302269911L;
	
	public ArgumentNullException(String argumentName) {
		super(argumentName, "was null, which is not allowed.");
	}

}
