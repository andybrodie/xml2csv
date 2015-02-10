package com.locima.xml2csv.configuration;


/**
 * Provides common functionality for mapping containers ({@link MappingList} and {@link PivotMapping}).
 */
public abstract class AbstractMappingContainer extends AbstractMapping implements IMappingContainer {

	private XPathValue mappingRoot;

	/**
	 * No-op constructor.
	 */
	public AbstractMappingContainer() {
	}

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
