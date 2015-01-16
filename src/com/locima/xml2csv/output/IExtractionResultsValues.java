package com.locima.xml2csv.output;

import java.util.List;

import com.locima.xml2csv.configuration.IValueMapping;

public interface IExtractionResultsValues extends IExtractionResults {

	/**
	 * Needed for {@link IValueMapping} only.
	 *
	 * @return
	 */
	List<String> getAllValues();

	IValueMapping getMapping();

	/**
	 * Needed for {@link IValueMapping} only.
	 *
	 * @param index
	 * @return
	 */
	String getValueAt(int index);

}
