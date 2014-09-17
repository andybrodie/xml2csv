package com.locima.xml2csv.extractor;

import com.locima.xml2csv.ProgramException;

/**
 * Thrown when unmanageable errors occur with @see XmlDataExtractor.
 */
public class DataExtractorException extends ProgramException {

	private static final long serialVersionUID = -3134742744533001471L;

	public DataExtractorException(Exception inner, String fmt, Object... parameters) {
        super(inner, fmt, parameters);
    }
}
