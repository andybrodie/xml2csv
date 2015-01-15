package com.locima.xml2csv.output;

import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.configuration.IValueMapping;
import com.locima.xml2csv.configuration.MultiValueBehaviour;
import com.locima.xml2csv.output.inline.ExtractedField;

public interface IExtractionResults {
	int getGroupNumber();

	/**
	 * Needed for {@link IMappingContainer} only, for generating an {@link ExtractedField} field name.
	 *
	 * @return
	 */
	int getIndex();

	/**
	 * Needed for {@link GroupState#createGroupStateList(IExtractionResults)}.
	 *
	 * @return
	 */
	int getMinCount();

	MultiValueBehaviour getMultiValueBehaviour();

	/**
	 * Needed for {@link IValueMapping} only, for generating an {@link ExtractedField} field name.
	 *
	 * @return
	 */
	IExtractionResults getParent();

	/**
	 * Needed for {@link GroupState#createGroupStateList(IExtractionResults)}.
	 *
	 * @return
	 */
	int size();

}
