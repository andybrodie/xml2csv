package com.locima.xml2csv.inputparser;

import com.locima.xml2csv.ProgramException;

/**
 * Throws if any unmanageable conditions occur whilst parsing configuration files.
 */
public class FileParserException extends ProgramException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an instance using the details provided.
	 *
	 * @param inner The cause.
	 * @param fmt A formatting string, passed to String.format.
	 * @param parameters Parameters passed to the formatting string.
	 */
	public FileParserException(Exception inner, String fmt, Object... parameters) {
		super(inner, fmt, parameters);
	}

	/**
	 * Constructs an instance using the details provided.
	 *
	 * @param fmt A formatting string, passed to String.format.
	 * @param parameters Parameters passed to the formatting string.
	 */
	public FileParserException(String fmt, Object... parameters) {
		super(null, fmt, parameters);
	}

}
