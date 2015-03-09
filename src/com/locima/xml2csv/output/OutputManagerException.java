package com.locima.xml2csv.output;

import com.locima.xml2csv.ProgramException;

/**
 * Thrown in any unmanageable conditions arise whilst writing output files.
 */
public class OutputManagerException extends ProgramException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an instance using the details provided.
	 *
	 * @param inner The cause.
	 * @param fmt A formatting string, passed to String.format.
	 * @param parameters Parameters passed to the formatting string.
	 */
	public OutputManagerException(Exception inner, String fmt, Object... parameters) {
		super(inner, fmt, parameters);
	}

	/**
	 * Constructs an instance using the details provided.
	 *
	 * @param fmt A formatting string, passed to String.format.
	 * @param parameters Parameters passed to the formatting string.
	 */
	public OutputManagerException(String fmt, Object... parameters) {
		super(null, fmt, parameters);
	}
}
