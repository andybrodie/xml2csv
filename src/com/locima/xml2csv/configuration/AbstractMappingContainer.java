package com.locima.xml2csv.configuration;

import java.util.List;

public abstract class AbstractMappingContainer extends AbstractMapping implements IMappingContainer {
	
	public AbstractMappingContainer(IMappingContainer parent, String baseName, NameFormat format, int groupNumber,
					MultiValueBehaviour multiValueBehaviour, int minValueCount, int maxValueCount) {
		super(parent, baseName, format, groupNumber, multiValueBehaviour, minValueCount, maxValueCount);
	}
	
	public AbstractMappingContainer() {
		
	}


	private XPathValue mappingRoot;

	/**
	 * Gets the XPath expression that returns the root nodes from which {@link #keyXPath} and {@link #valueXPath} will be evaluated.
	 *
	 * @return the XPath expression that returns the root nodes from which {@link #keyXPath} and {@link #valueXPath} will be evaluated.
	 */
	@Override
	public XPathValue getMappingRoot() {
		return this.mappingRoot;
	}


	/**
	 * Sets the XPath expression that returns the root nodes from which {@link #keyXPath} and {@link #valueXPath} will be evaluated.
	 *
	 * @param mappingRoot the XPath expression that returns the root nodes from which {@link #keyXPath} and {@link #valueXPath} will be evaluated.
	 */
	public void setMappingRoot(XPathValue mappingRoot) {
		this.mappingRoot = mappingRoot;
	}

}
