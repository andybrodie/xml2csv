package com.locima.xml2csv.configuration.filter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.extractor.DataExtractorException;

/**
 * Represents a filter that has no include logic, but does handle all other methods with sane implementations.
 */
public abstract class AbstractFilter implements IInputFilter {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractFilter.class);

	private boolean alwaysExecute;
	private List<IInputFilter> nestedFilters;

	@Override
	public void addNestedFilter(IInputFilter nestedFilter) {
		if (this.nestedFilters == null) {
			this.nestedFilters = new ArrayList<IInputFilter>();
		}
		LOG.debug("Adding nested filter {} to {}", nestedFilter, this);
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
			LOG.trace("Executed nested filters of {}", this);
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
	public void setAlwaysExecute(boolean alwaysExecute) {
		this.alwaysExecute = alwaysExecute;
	}

}
