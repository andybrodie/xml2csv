package com.locima.xml2csv.output;

import java.util.List;

import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IValueMapping;
import com.locima.xml2csv.output.inline.ExtractedField;

public interface IExtractionResultsValues extends IExtractionResults {

	IValueMapping getMapping();

	/**
	 * Needed for {@link IValueMapping} only.
	 * 
	 * @return
	 */
	List<String> getAllValues();

	/**
	 * Needed for {@link IValueMapping} only.
	 * 
	 * @param index
	 * @return
	 */
	String getValueAt(int index);

}
