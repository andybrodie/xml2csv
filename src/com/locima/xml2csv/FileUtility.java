package com.locima.xml2csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
	 * Used with {@link #getDirectory(String, int, boolean)} to indicate that a directory must be readable.
	 */
	public static final int CAN_READ = 1;
	/**
	 * Used with {@link #getDirectory(String, int, boolean)} to indicate that a directory must be writeable.
	 */
	public static final int CAN_WRITE = 2;
	/**
	 * Used with {@link #getDirectory(String, int, boolean)} to indicate that a directory must be executable.
	 */
	public static final int CAN_EXECUTE = 4;

	/**
	 * Turns a directory name in to a {@link File} instance, creating the directory if necessary and ensuring that required permissions are granted to
	 * the current user.
	 * 
	 * @param name the name of the directory.
	 * @param flags the permission flags. See {@link #CAN_READ}, {@link #CAN_WRITE} and {@link #CAN_EXECUTE}. For no permissions checking, pass 0.
	 * @param createIfNecessary if true and the directory does not exist, then it will be created.
	 * @return the {@link File} object that represents this directory.
	 * @throws IOException if any errors occur, such as the directory does not exist and could not be created, or the reuqired permissions are not
	 *             available.
	 */
	public static File getDirectory(String name, int flags, boolean createIfNecessary) throws IOException {
		File dir = new File(name);
		String absoluteDirName = dir.getAbsolutePath();
		LOG.debug("getDirectory Resolved {} to {}", name, absoluteDirName);
		if (!dir.exists()) {
			LOG.debug("Directory {} does not exist.", absoluteDirName);
			if (createIfNecessary) {
				if (dir.mkdirs()) {
					LOG.info("Created directory {}", dir.getAbsolutePath());
				} else {
					throw new IOException("Unable to create directory " + absoluteDirName);
				}
			} else {
				throw new FileNotFoundException("Directory " + absoluteDirName + " does not exist and no request was made to create it.");
			}
		}
		if (((flags | CAN_READ) > 0) && !dir.canRead()) {
			throw new IOException("Found directory, but cannot read from it: " + absoluteDirName);
		}
		if (((flags | CAN_WRITE) > 0) && !dir.canRead()) {
			throw new IOException("Found directory, but cannot write to it: " + absoluteDirName);
		}
		if (((flags | CAN_EXECUTE) > 0) && !dir.canRead()) {
			throw new IOException("Found directory, but cannot execute it: " + absoluteDirName);
		}
		LOG.info("Confirmed {} exists and has appropriate permissions", absoluteDirName);
		return dir;
	}

	/**
	 * Get all the files within a directory. Sub-directories or any other objects that aren't files are ignored.
	 * 
	 * @param directory the directory to find the files within.
	 * @return A (possibly empty) list of files.
	 */
	public static List<File> getFilesInDirectory(File directory) {
		LOG.debug("Retrieving all files in {}", directory.getAbsolutePath());
		List<File> files = new ArrayList<File>();

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
