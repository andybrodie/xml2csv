package com.locima.xml2csv;

public class ArgumentException extends IllegalArgumentException {

	private static final long serialVersionUID = 1739962004704462299L;
	
	public ArgumentException(String argumentName) {
		super(argumentName +  " argument was invalid.");
	}
	
	public ArgumentException(String argumentName, String reason) {
		super(argumentName + " " + reason);
	}
	

}
