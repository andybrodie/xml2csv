package com.locima.xml2csv;

/**
 * Runtime exception that is used like an assert mechanism for runtime. If one of these is thrown to the user the it means that they've discovered a
 * bug in the xml2csv code.
 */
public class BugException extends RuntimeException {

	/**
	 * The header message printed before any error information.
	 */
	private static final String HEADER = "We're sorry, but an unexpected condition has occurred that indicates a bug in xml2csv.  "
					+ "Please send the details below to crashreports@locima.com to report this issue.";

	/**
	 * Platform-specific line separator chatacter as we always output an apology before reporting error details.
	 */
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/**
	 *
	 */
	private static final long serialVersionUID = -1665563733287516597L;

	/**
	 * Creates a new instance with the message specified by <code>fmt</code> and <code>parameters</code> (see {@link String#format(String, Object...)}
	 * ) for details.
	 * 
	 * @param cause the exception that caused this exception to be thrown. May be null.
	 * @param fmt a formatting string.
	 * @param parameters parameters that will be used in the formatting string.
	 */
	public BugException(Exception cause, String fmt, Object... parameters) {
		super(String.format("%s%s%s", HEADER, LINE_SEPARATOR, String.format(fmt, parameters)), cause);
	}

	/**
	 * Creates a new instance with the message specified by <code>fmt</code> and <code>parameters</code> (see {@link String#format(String, Object...)}
	 * ) for details.
	 * 
	 * @param fmt a formatting string.
	 * @param parameters parameters that will be used in the formatting string.
	 */
	public BugException(String fmt, Object... parameters) {
		this(null, fmt, parameters);
	}

}
