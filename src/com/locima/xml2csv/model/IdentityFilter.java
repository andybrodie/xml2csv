package com.locima.xml2csv.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.XdmNode;

import com.locima.xml2csv.extractor.DataExtractorException;

/**
 * A base implementation of an input filter that always returns true.
 * <p>
 * This is useful as a base class for other filters as it manages the <code>alwaysExecute</code> flag as well as nested filters.
 */
public class IdentityFilter implements IInputFilter {

	private boolean alwaysExecute;
	private List<IInputFilter> nestedFilters;

	@Override
	public void addNestedFilter(IInputFilter nestedFilter) {
		if (this.nestedFilters == null) {
			this.nestedFilters = new ArrayList<IInputFilter>();
		}
		this.nestedFilters.add(nestedFilter);
	}

	/**
	 * Executes all the nested filters.
	 * 
	 * @param inputXmlFile the file to check with the filters.
	 * @return true if all the filters want to process the filter or if there are no nested filters; false otherwise.
	 */
	protected boolean executeNestedFilters(File inputXmlFile) {
		if (this.nestedFilters != null) {
			for (IInputFilter filter : this.nestedFilters) {
				if (!filter.include(inputXmlFile)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Executes all the nested filters.
	 * 
	 * @param inputXmlFileDocumentNode the file to check with the filters.
	 * @return true if all the filters want to process the filter or if there are no nested filters; false otherwise.
	 * @throws DataExtractorException if an error occurs whilst extracting data from the passed document for filtering.
	 */
	protected boolean executeNestedFilters(XdmNode inputXmlFileDocumentNode) throws DataExtractorException {
		if (this.nestedFilters != null) {
			for (IInputFilter filter : this.nestedFilters) {
				if (!filter.include(inputXmlFileDocumentNode)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean getAlwaysExecute() {
		return this.alwaysExecute;
	}

	@Override
	public boolean include(File inputXmlFile) {
		return true;
	}

	@Override
	public boolean include(XdmNode inputXmlFileDocumentNode) throws DataExtractorException {
		return executeNestedFilters(inputXmlFileDocumentNode);
	}

	@Override
	public void setAlwaysExecute(boolean alwaysExecute) {
		this.alwaysExecute = alwaysExecute;
	}

}
