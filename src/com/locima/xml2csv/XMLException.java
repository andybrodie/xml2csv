package com.locima.xml2csv;

/**
 * Throws if any unmanageable conditions occur whilst processing XML or XPath.
 */
public class XMLException extends ProgramException {

	private static final long serialVersionUID = 3146273025929566845L;

	/**
	 * Constructs an instance using the details provided.
	 * 
	 * @param inner The cause.
	 * @param fmt A formatting string, passed to String.format.
	 * @param parameters Parameters passed to the formatting string.
	 */

	public XMLException(Exception inner, String fmt, Object... parameters) {
		super(inner, fmt, parameters);
	}

	/**
	 * Constructs an instance using the details provided.
	 * 
	 * @param fmt A formatting string, passed to String.format.
	 * @param parameters Parameters passed to the formatting string.
	 */
	public XMLException(String fmt, Object... parameters) {
		super(null, fmt, parameters);
	}

}
