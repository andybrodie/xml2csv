package com.locima.xml2csv.output;

import com.locima.xml2csv.ProgramException;

/**
 * Thrown in any unmanageable conditions arise whilst writing output files.
 */
public class OutputManagerException extends ProgramException {
   
	private static final long serialVersionUID = 901737490561622597L;

	public OutputManagerException(Exception inner, String fmt, Object... parameters) {
        super(inner, fmt, parameters);
    }
	
	public OutputManagerException(String fmt, Object... parameters) {
		super(null, fmt, parameters);
	}
}
