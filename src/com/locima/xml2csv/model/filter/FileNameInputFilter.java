package com.locima.xml2csv.model.filter;

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
	private Pattern pattern;

	/**
	 * Sets the regex that this filter will use to match on {@link #include(File)}.
	 *
	 * @param regex the regular expression that will be used to match. Must not be null.
	 * @throws PatternSyntaxException if the regular expression passed by <code>regex</code> is invalid.
	 */
	// CHECKSTYLE:OFF I don't care if PatternSyntaxException is a runtime exception, it's pertinent!
	public FileNameInputFilter(String regex) throws PatternSyntaxException {
		// CHECKSTYLE:ON
		LOG.debug("Compiling regex {}", regex);
		this.pattern = Pattern.compile(regex);
	}

	/**
	 * Filters out files that do not match the regular expression passed by {@link #setMachingRegex}.
	 *
	 * @param xmlInputFile the XML file to match. This method uses the results of {@link File#getAbsolutePath()} to do the maching.
	 * @return true if the file will be processed, false otherwise.
	 */
	@Override
	public boolean include(File xmlInputFile) {
		boolean match;
		String absPath = xmlInputFile.getAbsolutePath();
		if (this.pattern == null) {
			LOG.warn("Regex pattern not specified on FileNameInputFilter, returning true");
			match = false;
		} else {
			match = this.pattern.matcher(absPath).find();
			LOG.debug("Input file {} did {}match file name input filter {}", absPath, match ? "" : "not ", this.pattern);
			if (match) {
				match = this.executeNestedFilters(xmlInputFile);
				LOG.trace("Input file {} excluded by nested filter", absPath);
			}
		}
		return match;
	}

	@Override
	public String toString() {
		return String.format("FileNameInputFilter(%s)", this.pattern);
	}
}
