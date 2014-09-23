package com.locima.xml2csv.extractor;

import com.locima.xml2csv.ProgramException;

/**
 * Thrown when unmanageable errors occur with @see XmlDataExtractor.
 */
public class DataExtractorException extends ProgramException {

	private static final long serialVersionUID = -3134742744533001471L;

	/**
	 * Constructs an instance using the details provided.
	 * @param inner The cause.
	 * @param fmt A formatting string, passed to String.format.
	 * @param parameters Parameters passed to the formatting string.
	 */
	public DataExtractorException(Exception inner, String fmt, Object... parameters) {
        super(inner, fmt, parameters);
    }

	/**
	 * Constructs an instance using the details provided.
	 * @param fmt A formatting string, passed to String.format.
	 * @param parameters Parameters passed to the formatting string.
	 */
	public DataExtractorException(String fmt, Object... parameters) {
        super(null, fmt, parameters);
    }

}
