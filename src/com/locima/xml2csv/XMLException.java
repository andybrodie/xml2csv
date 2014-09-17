package com.locima.xml2csv;

/**
 * Throws if any unmanageable conditions occur whilst processing XML or XPath.
 */
public class XMLException extends ProgramException {

	private static final long serialVersionUID = 3146273025929566845L;

	public XMLException(Exception inner, String fmt, Object... parameters) {
        super(inner, fmt, parameters);
    }

    public XMLException(String fmt, Object... parameters) {
        super(null, fmt, parameters);
    }

}
