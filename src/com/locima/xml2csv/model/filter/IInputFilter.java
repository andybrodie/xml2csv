package com.locima.xml2csv.model.filter;

import java.io.File;

import net.sf.saxon.s9api.XdmNode;

import com.locima.xml2csv.extractor.DataExtractorException;

/**
 * Implements a filter that decides whether to process an input document.
 * <p>
 * Each filter is invoked twice: {@link #include(String)} is invoked before the XML file is loaded; {@link #include(XdmNode)} afterwards.
 */
public interface IInputFilter {
	/**
	 * Adds a nested filter (which must also return true).
	 *
	 * @param nestedFilter a filter which must also return true for this filter to return true. Must not be null.
	 */
	void addNestedFilter(IInputFilter nestedFilter);

	/**
	 * Returns whether this filter must always be executed, or whether it only needs to be executed if all previous filters have agreed to process
	 * this file.
	 *
	 * @return if set then this filter must always be called for all input files. If false (default) then this filter will only be invoked if all
	 *         previous filters have agreed to process this file.
	 */
	boolean getAlwaysExecute();

	/**
	 * Return true if the file should be processed, false if it should be ignored.
	 *
	 * @param inputXmlFile the file that xml2csv is about to process. Will never be null.
	 * @return true if the file should be processed, false if it should be ignored.
	 */
	boolean include(File inputXmlFile);

	/**
	 * Return true if the document passed should be processed, false if it should be ignored.
	 *
	 * @param inputXmlFileDocumentNode the document that xml2csv is about to process. Will never be null.
	 * @return true if the document should be processed, false if it should be ignored.
	 * @throws DataExtractorException if an error occurs whilst extracting data from the passed document for filtering.
	 */
	boolean include(XdmNode inputXmlFileDocumentNode) throws DataExtractorException;

	/**
	 * Configures whether this filter must always be executed, or whether it only needs to be executed if all previous filters have agreed to process
	 * this file.
	 *
	 * @param alwaysExecute if set then this filter must always be called for all input files. If false (default) then this filter will only be
	 *            invoked if all previous filters have agreed to process this file.
	 */
	void setAlwaysExecute(boolean alwaysExecute);
}
