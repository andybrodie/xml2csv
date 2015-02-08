package com.locima.xml2csv.output;

import java.util.List;

import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.extractor.ContainerExtractionContext;

/**
 * Specifies the methods required for obtaining results from mapping execution (see {@link ContainerExtractionContext}.
 */
public interface IExtractionResultsContainer extends IExtractionResults {

	/**
	 * Retrieve all the child results.
	 *
	 * @return a list, containing one element for each mapping root node found. Each member of the list is itself a list which contains the results
	 *         for each child {@link IMapping} instance.
	 */
	List<List<IExtractionResults>> getChildren();

	/**
	 * Retrieves the mapping configuration object that the results for this object have been obtained from.
	 *
	 * @return a mapping container configuration instance, never null.
	 */
	IMappingContainer getMappingContainer();

	/**
	 * Needed for {@link IMappingContainer} only.
	 *
	 * @param index the index to retrieve, must be 0 <= <code>index</code> {@link #getChildren()}.size().
	 * @return equivalent to retrieving the element at <code>index</code> from a call to {@link #getChildren()}.
	 */
	List<IExtractionResults> getResultsSetAt(int index);

}
