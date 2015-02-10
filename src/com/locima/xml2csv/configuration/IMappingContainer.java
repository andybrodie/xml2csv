package com.locima.xml2csv.configuration;

/**
 * Used for objects that contain ordered collections of mappings of field name to XPath.
 */
public interface IMappingContainer extends IMapping, Iterable<IMapping> {

	/**
	 * Returns an output name associated with this mapping container.
	 *
	 * @return a string, or null if this mapping container is anonymous. Note that top-level mapping containers (i.e. those stored beneath
	 *         {@link MappingConfiguration} cannot be anonymous and must have a valid non-zero length string.
	 */
	String getName();

	/**
	 * Retrieve the mapping root for this container. The mapping root is an XPath expression that will be executed, relative to a parent node (or
	 * document node, if this is a top-level mapping), will be used as a the basis for executing child mappings.
	 *
	 * @return an XPath amount. If null then the node provided by a parent container, or document node (if none) should be used.
	 */
	XPathValue getMappingRoot();

	/**
	 * Returns the number of child mappings contained within this container.
	 *
	 * @return the number of child mappings contained within this container.
	 */
	int size();

}