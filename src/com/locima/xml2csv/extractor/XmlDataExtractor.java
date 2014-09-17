package com.locima.xml2csv.extractor;

import java.io.File;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.RandomAccess;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.locima.xml2csv.SaxonProcessorManager;
import com.locima.xml2csv.inputparser.MappingsSet;
import com.locima.xml2csv.output.IOutputManager;
import com.locima.xml2csv.output.OutputManagerException;

/**
 * Extracts data from XML documents provided according a @see MappingsSet provided.
 */
public class XmlDataExtractor {

	/**
	 * Provides a convenience wrapper that allows us to iterate over {@link NodeList} instances.
	 */
	static final class NodeListWrapper extends AbstractList<Node> implements RandomAccess {
		private final NodeList list;

		NodeListWrapper(NodeList nodeList) {
			this.list = nodeList;
		}

		@Override
		public Node get(int index) {
			return this.list.item(index);
		}

		@Override
		public int size() {
			return this.list.getLength();
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(XmlDataExtractor.class);

	public static List<Node> asList(NodeList n) {
		return n.getLength() == 0 ? Collections.<Node>emptyList() : new NodeListWrapper(n);
	}

	private MappingsSet mappings;

	private Processor saxonProcessor;

	public XmlDataExtractor() {
		this.saxonProcessor = SaxonProcessorManager.getProcessor();
	}

	/**
	 * Executes the mappings set by {@link #setMappings(MappingsSet)} against a document <code>xmlDoc</code> and passes the results to <code>om</code>
	 * .
	 * 
	 * @param xmlDoc
	 * @param om
	 * @throws DataExtractorException
	 * @throws OutputManagerException
	 */
	public void extractDocTo(XdmNode xmlDoc, IOutputManager om) throws DataExtractorException, OutputManagerException {
		LOG.trace("Executing {} sets of mappings", this.mappings.size());
		for (NameToXPathMappings mapping : this.mappings.getAll()) {
			extractDocTo(xmlDoc, mapping, om);
		}
		LOG.trace("Completed all mappings against documents");
	}

	/**
	 * Iterate over all the mappings and apply each set to the passed XML document and pass the results to the output manager. This method delegates
	 * to {@link #extractTo(Element, NameToXpathMappings, OutputManager)}, by either finding the mapping root within {@link NameToXpathMappings} or
	 * using the document root element.
	 * 
	 * @param xmlDoc The XML document to extract information from
	 * @param mapping The set of mappings that define the data to be extracted
	 * @param om The output manager to which the data should be sent
	 * @throws DataExtractorException If anything unrecoverable during data extraction happens
	 * @throws OutputManagerException if anything unrecoverable during writing output happens
	 */
	public void extractDocTo(XdmNode xmlDoc, NameToXPathMappings mapping, IOutputManager om) throws DataExtractorException, OutputManagerException {
		XPathSelector mappingRoot = mapping.getMappingRoot();
		if (mappingRoot == null) {
			LOG.info("Executing mappings against document root element");
			extractTo(xmlDoc, mapping, om);
		} else {
			XdmValue mappingRootElements;
			try {
				LOG.info("Executing mappings against specified mapping root");
				mappingRoot.setContextItem(xmlDoc);
				mappingRootElements = mappingRoot.evaluate();
			} catch (SaxonApiException e) {
				throw new DataExtractorException(e, "Invalid Mapping Root specified in %1$s", mapping.getName());
			}
			LOG.info("Found {} mapping root elements", mappingRootElements.size());
			for (XdmItem node : mappingRootElements) {
				extractTo((XdmNode) node, mapping, om);
			}
		}
	}

	/**
	 * Extracts data from the <code>xmlFile</code> passed and pushes that data to the <code>om</code>.
	 * 
	 * @param xmlFile The XML file to read data from, must be a valid file.
	 * @param om The output manager to write data to, must be a valid instance.
	 * @throws DataExtractorException If an error occurs during extraction of data from the XML.
	 * @throws OutputManagerException If an error occurs writing the data out.
	 */
	public void convert(File xmlFile, IOutputManager om) throws DataExtractorException, OutputManagerException {
		try {
			DocumentBuilder db = this.saxonProcessor.newDocumentBuilder();
			LOG.info("Loading and parsing XML file {}", xmlFile.getName());
			XdmNode document = db.build(xmlFile);
			LOG.debug("XML file loaded succesfully");
			extractDocTo(document, om);
		} catch (SaxonApiException e) {
			throw new DataExtractorException(e, "Unable to read XML file %s", xmlFile);
		}
	}

	/**
	 * Iterate over all the mappings and apply each set to the passed mappingRoot (XML element) and pass the results to the output manager.
	 * 
	 * @param xmlmappingRoot The element from which all mappings should be applied
	 * @param mapping The set of mappings that define the data to be extracted
	 * @param om The output manager to which the data should be sent
	 * @throws DataExtractorException If anything unrecoverable during data extraction happens
	 * @throws com.locima.xml2csv.output.OutputManagerException if anything unrecoverable during writing output happens
	 */
	public void extractTo(XdmNode mappingRoot, NameToXPathMappings mapping, IOutputManager om) throws DataExtractorException, OutputManagerException {
		List<String> values = new ArrayList<String>();
		for (Entry<String, XPathValue> colToXPathMapping : mapping.entrySet()) {
			String colName = colToXPathMapping.getKey();
			XPathValue xPathExpr = colToXPathMapping.getValue();
			LOG.trace("Extracting value for {} using {}", colName, xPathExpr.getSource());
			XdmNode node = xPathExpr.evaluateAsNode(mappingRoot);
			if (node != null) {
				String nodeValue = node.getStringValue();
				values.add(nodeValue);
				LOG.debug("Column {} value {} found after executing XPath {}", colName, nodeValue, xPathExpr.getSource());
			} else {
				LOG.trace("No value found for {} in {}", colName, xPathExpr.getSource());
			}
		}
		om.writeRecords(mapping.getName(), values);
	}

	/**
	 * Configure this extractor with the set of mappings specified.
	 * 
	 * @param newMappings the mappings that define how to extract data from the XML when {@link #extractTo} is called.
	 * @param map XML namespace definitions
	 */
	public void setMappings(MappingsSet newMappings) {
		this.mappings = newMappings;
	}
}
