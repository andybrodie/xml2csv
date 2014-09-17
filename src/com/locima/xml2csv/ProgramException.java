package com.locima.xml2csv;

/**
 * Superclass for all exceptions created by this application.
 */
public class ProgramException extends Exception {

	private static final long serialVersionUID = -6607136356786928990L;

	public ProgramException(Exception inner, String fmt, Object... parameters) {
        super(String.format(fmt, parameters), inner);
    }
}
