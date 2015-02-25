package com.locima.xml2csv.configuration.filter;

import java.io.File;

import net.sf.saxon.s9api.XdmNode;

import com.locima.xml2csv.extractor.DataExtractorException;

/**
 * A base implementation of an input filter that always returns true.
 * <p>
 * This is useful as a base class for other filters as it manages the <code>alwaysExecute</code> flag as well as nested filters.
 */
public class FilterContainer extends AbstractFilter {

	@Override
	public boolean include(File inputXmlFile) {
		return executeNestedFilters(inputXmlFile);
	}

	@Override
	public boolean include(XdmNode inputXmlFileDocumentNode) throws DataExtractorException {
		return executeNestedFilters(inputXmlFileDocumentNode);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("FilterContainer(");
		sb.append(super.toString());
		sb.append(")");
		return sb.toString();
	}


}
