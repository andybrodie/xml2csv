/**
 * Contains the logic for creating and writing out to CSI files based on data provided by {@link com.locima.xml2csv.output.IExtractionResults}
 * instances.
 * <p>
 * CSI files are intermediate files, which consist of serialized {@link com.locima.xml2csv.output.IExtractionResults} instances. They are used in the
 * circumstance that the number of fields required in a CSV is a function of the data within the XML file (such as using unbounded greedy mappings or
 * {@link com.locima.xml2csv.configuration.PivotMapping} instances).
 */
package com.locima.xml2csv.output.inline;