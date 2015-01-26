package com.locima.xml2csv.util;

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

	/**
	 * Used with {@link #getDirectory(String, int, boolean)} to indicate that a directory must be executable.
	 */
	public static final int CAN_EXECUTE = 4;

	/**
	 * Used with {@link #getDirectory(String, int, boolean)} to indicate that a directory must be readable.
	 */
	public static final int CAN_READ = 1;

	/**
	 * Used with {@link #getDirectory(String, int, boolean)} to indicate that a directory must be writeable.
	 */
	public static final int CAN_WRITE = 2;
	private static final Logger LOG = LoggerFactory.getLogger(FileUtility.class);

	private static final int MAX_POSIX_LEN = 14;

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
	 * Creates a POSIX-compliant file name bsased on the parameter passed. The returned string will be:
	 * <p>
	 * Note we only permit characters that are fully POSIX compliant, this means:
	 * <ol>
	 * <li>14 characters or fewer.</li>
	 * <li>Made up only of the following characters: A–Z a–z 0–9 . (period) _ (underscore) - (minus/hyphen).</li>
	 * <li>The first character may not be a minus/hyphen.</li>
	 * </ol>
	 *
	 * @param baseName a string that needs to be converted to a file name.
	 * @param extension an extension that should always be present in the returned file name.
	 * @param ignoreLength if true then the length restriction of 14 characters will be ignored.
	 * @return a string that only contains POSIX-compliant filename characters and length (depending on <code>ignoreLength</code>). Returns
	 *         <code>null</code> if <code>null</code> is passed in <code>baseName</code>.
	 */
	// CHECKSTYLE:OFF Cyclomatic complexity caused by that big "if" statement
	public static String convertToPOSIXCompliantFileName(String baseName, String extension, boolean ignoreLength) {
		// CHECKSTYLE:ON
		if (baseName == null) {
			return null;
		}
		int maxPermittedLength = ignoreLength ? Integer.MAX_VALUE : MAX_POSIX_LEN;
		if (extension != null) {
			maxPermittedLength -= extension.length();
		}
		StringBuilder sb = new StringBuilder();
		int baseNameLen = baseName.length();
		int addedCharCount = 0;
		for (int i = 0; (i < baseNameLen) && (addedCharCount < maxPermittedLength); i++) {
			char ch = baseName.charAt(i);
			// CHECKSTYLE:OFF Can't think of a better way to do this "if" statement.
			if (((ch >= 'A') && (ch <= 'Z')) || ((ch >= 'a') && (ch <= 'z')) || ((ch >= '0') && (ch <= '9')) || (ch == '.') || (ch == '_')
							|| ((ch == '-') && (addedCharCount > 0))) {
				// CHECKSTYLE:ON
				sb.append(ch);
				addedCharCount++;
			}
		}
		if (extension != null) {
			sb.append(extension);
		}
		String fileName = sb.toString();
		LOG.info("Created file name {} from {} and {}", fileName, baseName, extension);
		return fileName;
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
				throw new FileNotFoundException("Directory " + absoluteDirName + " does not exist.");
			}
		}
		checkFlags(dir, flags);
		LOG.info("Confirmed {} exists and has appropriate permissions", absoluteDirName);
		return dir;
	}

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
	 * Given a file name or directory, either return just that file in a list or all the files within a directory.
	 *
	 * @param fileOrDirectory a file the evaluate, or the directory to find files within.
	 * @param recurseDirectories if true then directories will be recursively searched for files.
	 * @return A (possibly empty) list of files.
	 */
	public static List<File> getFiles(File fileOrDirectory, boolean recurseDirectories) {
		LOG.debug("Searching for files in {}", fileOrDirectory.getAbsolutePath());
		List<File> files = new ArrayList<File>();
		if (fileOrDirectory.isFile()) {
			files.add(fileOrDirectory);
		} else {
			getFiles(files, fileOrDirectory, recurseDirectories);
		}
		LOG.info("Found {} files", files.size());
		return files;
	}

	/**
	 * Recursively add all files wtihin <code>directory</code> to <code>files</code>.
	 * <p>
	 * <code>directory</code> MUST be a directory and non-null.
	 *
	 * @param files the list of files to add to.
	 * @param directory the directory to search.
	 * @param recurseDirectories if true then directories will be recursively searched for files.
	 */
	private static void getFiles(List<File> files, File directory, boolean recurseDirectories) {
		File[] listOfFiles = directory.listFiles();
		for (File fileOrDirectory : listOfFiles) {
			if (fileOrDirectory.isFile()) {
				files.add(fileOrDirectory);
			} else {
				if (recurseDirectories) {
					getFiles(files, fileOrDirectory, recurseDirectories);
				}
			}
		}
	}

	/**
	 * Prevents instances being created.
	 */
	private FileUtility() {
	}

}
