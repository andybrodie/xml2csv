package com.locima.xml2csv;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains useful File system based utilities.
 */
public class FileUtility {

	private static final Logger LOG = LoggerFactory.getLogger(FileUtility.class);
	
	/**
	 * Prevents instances being created. 
	 */
	private FileUtility() {
	}

	/**
	 * Get all the files within a directory.  Sub-directories or any other objects that aren't files are ignored.
	 * @param directoryName The name of the directory (relative or absolute) to retrieve files from. 
	 * @return A (possibly empty) list of files. 
	 */
	public static List<File> getFilesInDirectory(String directoryName) {
		LOG.debug("Retrieving all files in {}", directoryName);
		List<File> files = new ArrayList<File>();

		File directory = new File(directoryName);
		File[] listOfFiles = directory.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				LOG.trace("Adding {} to list.  Total size is now {}", file.getName(), files.size());
				files.add(file);
			} else {
				// Ignore any directories, we're not recursively searching
				LOG.warn("Ignoring all non-file types {}", file.getPath());
			}
		}
		LOG.info("Found {} files", files.size());
		return files;
	}

}
