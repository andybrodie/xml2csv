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
	 * Gets the XPath expression that returns the root nodes for this mapping.
	 *
	 * @return the XPath expression that returns the root nodes for this mapping.
	 */
	@Override
	public XPathValue getMappingRoot() {
		return this.mappingRoot;
	}

	/**
	 * Sets the XPath expression that returns the root nodes for this mapping.
	 *
	 * @param mappingRoot sets the XPath expression that returns the root nodes for this mapping.
	 */
	public void setMappingRoot(XPathValue mappingRoot) {
		this.mappingRoot = mappingRoot;
	}

}
