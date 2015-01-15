package com.locima.xml2csv.output;

import java.util.List;

import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.configuration.IValueMapping;
import com.locima.xml2csv.output.inline.ExtractedField;

public interface IExtractionResultsContainer extends IExtractionResults {

	List<List<IExtractionResults>> getChildren();


	IMappingContainer getMapping();

	/**
	 * Needed for {@link IMappingContainer} only.
	 *
	 * @param index
	 * @return
	 */
	List<IExtractionResults> getResultsSetAt(int index);


	List<String> getEmptyFieldNames(int containerIterationCount);

}
