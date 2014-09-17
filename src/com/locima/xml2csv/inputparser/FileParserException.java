package com.locima.xml2csv.inputparser;

import com.locima.xml2csv.ProgramException;

/**
 * Throws if any unmanageable conditions occur whilst parsing configuration files.
 */
public class FileParserException extends ProgramException {

	private static final long serialVersionUID = 3146273025929566845L;

	public FileParserException(Exception inner, String fmt, Object... parameters) {
        super(inner, fmt, parameters);
    }

    public FileParserException(String fmt, Object... parameters) {
        super(null, fmt, parameters);
    }

}
