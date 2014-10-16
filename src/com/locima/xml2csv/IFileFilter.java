package com.locima.xml2csv;

import java.io.File;

/**
 * Simple delegate interface (as we haven't made this require Java 8, otherwise a lambda would do, for filtering files. 
 */
public interface IFileFilter {

	/**
	 * Returns true if this file should be included, false otherwise. Included in <em>what</em> depends on the context. See
	 * {@link FileUtility#getFilesInDirectory(File, IFileFilter)}, for example.
	 * 
	 * @param file the file to test. Will never be null.
	 * @return true if the file should be included, false otherwise.
	 */
	boolean include(File file);

}
