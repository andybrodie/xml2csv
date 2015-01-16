package com.locima.xml2csv.output;

import java.util.List;

import com.locima.xml2csv.configuration.IMappingContainer;

public interface IExtractionResultsContainer extends IExtractionResults {

	List<List<IExtractionResults>> getChildren();

	List<String> getEmptyFieldNames(int containerIterationCount);

	IMappingContainer getMapping();

	/**
	 * Needed for {@link IMappingContainer} only.
	 *
	 * @param index
	 * @return
	 */
	List<IExtractionResults> getResultsSetAt(int index);

}
