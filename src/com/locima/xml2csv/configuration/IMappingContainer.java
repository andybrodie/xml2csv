package com.locima.xml2csv.configuration;

import java.util.List;

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
	String getContainerName();

	XPathValue getMappingRoot();

	/**
	 * Returns the number of child mappings contained within this container.
	 * 
	 * @return the number of child mappings contained within this container.
	 */
	int size();

}