package com.locima.xml2csv.extractor;

import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.configuration.Mapping;
import com.locima.xml2csv.configuration.MappingConfiguration;
import com.locima.xml2csv.configuration.MappingList;
import com.locima.xml2csv.output.IOutputManager;
import com.locima.xml2csv.output.OutputManagerException;

/**
 * Extracts data from XML documents provided according a @see MappingConfiguration provided.
 * <p>
 * This makes use of Saxon for parsing XPath, because Saxon was the only XPath 2.0 compliant parser I could find. JAXP does NOT support default
 * namespace prefixes (part of XPath 2.0), so had to resort to the native Saxon APIs. All suggested workaround seem to be "just rewrite the XPath
 * statement to include a NS declaration", however I have no control over the input files and XPath, so this is not viable.
 * <p>
 * This is a surprisingly thin class, because most of the actual heavy lifting is done in {@link MappingList} and {@link Mapping}.
 */
public class XmlDataExtractor {

	private static final Logger LOG = LoggerFactory.getLogger(XmlDataExtractor.class);

	/**
	 * Stores the mapping configuration to be used by this extractor.
	 */
	private MappingConfiguration mappingConfiguration;

	/**
	 * Executes the mappingConfiguration set by {@link #setMappingConfiguration(MappingConfiguration)} against a document <code>xmlDoc</code> and
	 * passes the results to <code>outputManager</code>.
	 * <p>
	 * Note that all filtering (application of {@link MappingConfiguration#include(java.io.File)} and {@link MappingConfiguration#include(XdmNode)}
	 * should be done by the caller before executing this method.
	 *
	 * @param xmlDoc The XML document to extract information from.
	 * @param outputManager The output manager to send the extracted data to. May be null if no output is required.
	 * @throws DataExtractorException If an error occurred extracting data from the XML document.
	 * @throws OutputManagerException If an error occurred writing data to the output manager.
	 */
	public void extractTo(XdmNode xmlDoc, IOutputManager outputManager) throws DataExtractorException, OutputManagerException {
		extractTo(xmlDoc, outputManager, 0);
	}

	/**
	 * Executes the mappingConfiguration set by {@link #setMappingConfiguration(MappingConfiguration)} against a document <code>xmlDoc</code> and
	 * passes the results to <code>outputManager</code>.
	 * <p>
	 * Note that all filtering (application of {@link MappingConfiguration#include(java.io.File)} and {@link MappingConfiguration#include(XdmNode)}
	 * should be done by the caller before executing this method.
	 *
	 * @param xmlDoc The XML document to extract information from.
	 * @param outputManager The output manager to send the extracted data to. May be null if no output is required.
	 * @throws DataExtractorException If an error occurred extracting data from the XML document.
	 * @throws OutputManagerException If an error occurred writing data to the output manager.
	 */
	public void extractTo(XdmNode xmlDoc, IOutputManager outputManager, int positionRelativeToOtherRootNodes) throws DataExtractorException,
	OutputManagerException {
		LOG.info("Executing {} sets of mappingConfiguration", this.mappingConfiguration.size());
		this.mappingConfiguration.log();
		int mappingSiblingIndex = 0;
		for (IMappingContainer mapping : this.mappingConfiguration) {
			ContainerExtractionContext ctx = new ContainerExtractionContext(mapping, positionRelativeToOtherRootNodes, mappingSiblingIndex);
			ctx.evaluate(xmlDoc);
			
			if (LOG.isTraceEnabled()) {
				LOG.trace("START RESULTS OUTPUT after completed mapping container {} against document", this);
				ContainerExtractionContext.logResults(ctx, 0, 0);
				LOG.trace("END RESULTS OUTPUT");
			}
			
			outputManager.writeRecords(mapping.getContainerName(), ctx);
			mappingSiblingIndex++;
		}
	}

	/**
	 * Configure this extractor with the mappingConfiguration specified.
	 *
	 * @param mappingConfiguration the mappingConfiguration that define how to extract data from the XML when {@link #extractTo} is called.
	 */
	public void setMappingConfiguration(MappingConfiguration mappingConfiguration) {
		this.mappingConfiguration = mappingConfiguration;
	}
}
