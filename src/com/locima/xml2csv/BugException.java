package com.locima.xml2csv;

public class BugException extends RuntimeException {

	private final static String LINE_SEPARATOR = System.getProperty("line.separator");
	
	private final static String HEADER = "An unexpected condition has occurred that indicates a bug in xml2csv.  Please send the details below to crashreports@locima.com to report this issue.";
	
	public BugException(Exception cause, String fmt, Object... parameters) {
		super(String.format("%s%s%s", HEADER, LINE_SEPARATOR, String.format(fmt, parameters)), cause);
	}
	
	public BugException(String fmt, Object... parameters) {
		this(null, fmt, parameters);
	}
	
}
