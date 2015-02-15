/**
 * Contains the classes that extract data from XML documents, according to a configuration, and load it in to
 * {@link com.locima.xml2csv.output.IExtractionResults} instances.
 * <p>
 * For each type of mapping, there is a corresponding {@link com.locima.xml2csv.extractor.IExtractionContext} that stores the results of extracting a
 * <em>single</em> document, as follows:
 * <table>
 * <thead>
 * <tr>
 * <th>{@link com.locima.xml2csv.configuration.IMapping} instance</th>
 * <th>Corresponding {@link com.locima.xml2csv.output.IExtractionResults} implementation</th>
 * </tr>
 * </thead><tbody>
 * <tr>
 * <td>{@link com.locima.xml2csv.configuration.Mapping}</td>
 * <td>{@link com.locima.xml2csv.extractor.MappingExtractionContext}</td>
 * </tr>
 * <tr>
 * <td>{@link com.locima.xml2csv.configuration.MappingList}</td>
 * <td>{@link com.locima.xml2csv.extractor.ContainerExtractionContext}</>td>
 * </tr>
 * <tr>
 * <td>{@link com.locima.xml2csv.configuration.PivotMapping}</td>
 * <td>{@link com.locima.xml2csv.extractor.PivotExtractionContext}</td>
 * </tr>
 * </tbody>
 * </table>
 */
package com.locima.xml2csv.extractor;