/**
 * The classes that manage converting extracted data (from instances of {@link com.locima.xml2csv.output.IExtractionResults}) in to CSV files.
 * <p>
 * In order to provide some separation between the output and extraction, we don't rely on {@link com.locima.xml2csv.extractor.IExtractionContext} and
 * instead use {@link com.locima.xml2csv.output.IExtractionResults}.
 * <p>
 * Most of the actual logic that drives the creation of CSV records is in the sub-package class
 * {@link com.locima.xml2csv.output.direct.DirectOutputRecordIterator}. {@link com.locima.xml2csv.output.GroupState} and
 * {@link com.locima.xml2csv.output.GreedyGroupState} provide supporting logic.  The other classes in here are either interfaces or
 * file management.
 */
package com.locima.xml2csv.output;