package com.locima.xml2csv.configuration.filter;

import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filters based on the name of the input file.
 * <p>
 * Performs no matching based on the XML content.
 */
public class FileNameInputFilter extends FilterContainer {

	private static final Logger LOG = LoggerFactory.getLogger(FileNameInputFilter.class);
	private boolean matchLocalFileNameOnly;
	private Pattern pattern;

	/**
	 * Sets the regex that this filter will use to match on {@link #include(File)}.
	 *
	 * @param regex the regular expression that will be used to match. Must not be null.
	 * @param matchLocalFileNameOnly if true, then only the relative filename (i.e. no directory information) will be passed to the filter.
	 * @throws PatternSyntaxException if the regular expression passed by <code>regex</code> is invalid.
	 */
	// CHECKSTYLE:OFF I don't care if PatternSyntaxException is a runtime exception, it's pertinent!
	public FileNameInputFilter(String regex, boolean matchLocalFileNameOnly) throws PatternSyntaxException {
		// CHECKSTYLE:ON
		LOG.debug("Compiling regex {}", regex);
		this.pattern = Pattern.compile(regex);
		this.matchLocalFileNameOnly = matchLocalFileNameOnly;
	}

	/**
	 * Filters out files that do not match the regular expression passed by {@link FileNameInputFilter#FileNameInputFilter(String)}.
	 *
	 * @param xmlInputFile the XML file to match. This method uses the results of {@link File#getAbsolutePath()} to do the maching.
	 * @return true if the file will be processed, false otherwise.
	 */
	@Override
	public boolean include(File xmlInputFile) {
		boolean match;
		String pathToTest = this.matchLocalFileNameOnly ? xmlInputFile.getName() : xmlInputFile.getAbsolutePath();
		if (this.pattern == null) {
			LOG.warn("Regex pattern not specified on FileNameInputFilter, returning true");
			match = false;
		} else {
			match = this.pattern.matcher(pathToTest).find();
			LOG.debug("Input file {} did {}match file name input filter {}", pathToTest, match ? "" : "not ", this.pattern);
			if (match) {
				match = this.executeNestedFilters(xmlInputFile);
				if (!match) {
					LOG.trace("Nested filter of {} rejected {}", this, xmlInputFile);
				}
			}
		}
		return match;
	}

	@Override
	public String toString() {
		return "FileNameInputFilter(\"" + this.pattern + "\")";
	}
}
