package com.locima.xml2csv;

public class NotImplementedException extends IllegalStateException {

	private static final long serialVersionUID = 5978057748889910190L;

	public NotImplementedException() {
	}

	/**
	 * Constructs an instance using the details provided.
	 *
	 * @param inner The cause.
	 * @param fmt A formatting string, passed to String.format.
	 * @param parameters Parameters passed to the formatting string.
	 */
	public NotImplementedException(Exception inner, String fmt, Object... parameters) {
		super(String.format(fmt, parameters), inner);
	}

	/**
	 * Constructs an instance using the details provided.
	 *
	 * @param fmt A formatting string, passed to String.format.
	 * @param parameters Parameters passed to the formatting string.
	 */
	public NotImplementedException(String fmt, Object... parameters) {
		this(null, fmt, parameters);
	}
}
