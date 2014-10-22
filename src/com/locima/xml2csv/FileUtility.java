package com.locima.xml2csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.model.filter.IInputFilter;

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
	 * Turns a file name in to a {@link File} instance, ensuring that required permissions are granted to the current user.
	 * 
	 * @param name the name of the file.
	 * @param flags the permission flags. See {@link #CAN_READ}, {@link #CAN_WRITE} and {@link #CAN_EXECUTE}. For no permissions checking, pass 0.
	 * @throws IOException if any errors occur, such as the directory does not exist and could not be created, or the reuqired permissions are not
	 *             available.
	 * @return a file instance for the specified file.
	 */
	public static File getFile(String name, int flags) throws IOException {
		File f = new File(name);
		if (!f.exists()) {
			throw new IOException("File " + f.getAbsolutePath() + " does not exist.");
		}
		checkFlags(f, flags);
		return f;
	}

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
				// CHECKSTYLE:OFF Nested if-else depth is 2 (max allowed is 1).
				if (dir.mkdirs()) {
					// CHECKSTYLE:ON
					LOG.info("Created directory {}", dir.getAbsolutePath());
				} else {
					throw new IOException("Unable to create directory " + absoluteDirName);
				}
			} else {
				throw new FileNotFoundException("Directory " + absoluteDirName + " does not exist and no request was made to create it.");
			}
		}
		checkFlags(dir, flags);
		LOG.info("Confirmed {} exists and has appropriate permissions", absoluteDirName);
		return dir;
	}

	/**
	 * Checks the permissions on a file, throwing an exception if not what the caller wants (specified by <code>flags</code>).
	 * 
	 * @param file the file to check (can also be a directory).
	 * @param flags the permission flags. See {@link #CAN_READ}, {@link #CAN_WRITE} and {@link #CAN_EXECUTE}. For no permissions checking, pass 0.
	 * @throws IOException if any of the required permissions are not granted to the currently executing user.
	 */
	private static void checkFlags(File file, int flags) throws IOException {
		if (((flags | CAN_READ) > 0) && !file.canRead()) {
			throw new IOException("Found directory, but cannot read from it: " + file.getAbsolutePath());
		}
		if (((flags | CAN_WRITE) > 0) && !file.canRead()) {
			throw new IOException("Found directory, but cannot write to it: " + file.getAbsolutePath());
		}
		if (((flags | CAN_EXECUTE) > 0) && !file.canRead()) {
			throw new IOException("Found directory, but cannot execute it: " + file.getAbsolutePath());
		}
	}

	/**
	 * Get all the files within a directory. Sub-directories or any other objects that aren't files are ignored.
	 * 
	 * @param directory the directory to find the files within.
	 * @return A (possibly empty) list of files.
	 */
	public static List<File> getFilesInDirectory(File directory) {
		return getFilesInDirectory(directory, null);
	}

	/**
	 * Get all the files within a directory. Sub-directories or any other objects that aren't files are ignored.
	 * 
	 * @param directory the directory to find the files within.
	 * @param filter A filter that all files must match if they are to be included. If null, no filtering is applied.
	 * @return A (possibly empty) list of files.
	 */
	public static List<File> getFilesInDirectory(File directory, Object filter) {
		LOG.debug("Retrieving all files in {}", directory.getAbsolutePath());
		List<File> files = new ArrayList<File>();

		File[] listOfFiles = directory.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				if (filter == null) {
					LOG.trace("Adding {} to list.  Total size is now {}", file.getName(), files.size());
					files.add(file);
				} else {
					LOG.trace("Excluding {} from list, based on filter", file.getName());
				}
			} else {
				// Ignore any directories, we're not recursively searching
				LOG.warn("Ignoring all non-file types {}", file.getPath());
			}
		}
		LOG.info("Found {} files", files.size());
		return files;
	}

}
