package com.locima.xml2csv.extractor;

import java.io.File;
import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.locima.xml2csv.ArgumentNullException;
import com.locima.xml2csv.SaxonProcessorManager;
import com.locima.xml2csv.inputparser.IMappingContainer;
import com.locima.xml2csv.inputparser.MappingConfiguration;
import com.locima.xml2csv.inputparser.MappingList;
import com.locima.xml2csv.output.IOutputManager;
import com.locima.xml2csv.output.OutputManagerException;

/**
 * Extracts data from XML documents provided according a @see MappingConfiguration provided.
 * <p>
 * This makes use of Saxon for parsing XPath, because Saxon was the only XPath 2.0 compliant parser I could find. JAXP does NOT support default
 * namespace prefixes (part of XPath 2.0), so had to resort to the native Saxon APIs. All suggestions seem to be "just rewrite the XPath statement to
 * include a NS declaration. This Apple-style suggestion only works when I have control over the input files. For some use-cases this is not the case.
 */
public class XmlDataExtractor {

	/**
	 * Provides a convenience wrapper that allows us to iterate over {@link NodeList} instances.
	 */
	static final class NodeListWrapper extends AbstractList<Node> implements RandomAccess {
		private final NodeList list;

		/**
		 * Initialises an instance to be wrapped around the passed node list.
		 *
		 * @param nodeList the node list to wrap around. Must not be null.
		 */
		NodeListWrapper(NodeList nodeList) {
			if (nodeList == null) {
				throw new ArgumentNullException("nodeList");
			}
			this.list = nodeList;
		}

		/**
		 * Retrieves the item at the given index.
		 *
		 * @param index the index of the item to return. Index starts at 0.
		 * @return The item at the given index, or throws {@link IndexOutOfBoundsException}
		 */
		@Override
		public Node get(int index) {
			return this.list.item(index);
		}

		/**
		 * Returns the number of items in the node list.
		 *
		 * @return the number of items in the node list this object is wrapping.
		 */
		@Override
		public int size() {
			return this.list.getLength();
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(XmlDataExtractor.class);

	/**
	 * Returns a {@link NodeList} an iterable list.
	 *
	 * @param n the node list to wrap.
	 * @return a wrapped node list that can be iterated over more easily.
	 */
	public static List<Node> asList(NodeList n) {
		return n.getLength() == 0 ? Collections.<Node>emptyList() : new NodeListWrapper(n);
	}

	private MappingConfiguration mappings;

	private Processor saxonProcessor;

	/**
	 * Is set, then all whitespace will be trimmed from the beginning and end of element and attribute values.
	 */
	private boolean trimWhitespace;

	/**
	 * Initialise the internal Saxon Processor.
	 */
	public XmlDataExtractor() {
		this.saxonProcessor = SaxonProcessorManager.getProcessor();
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

	private void executeMappingOnDoc(XdmNode xmlDoc, IOutputManager om, MappingList mapping) throws DataExtractorException,
					OutputManagerException {
		/**
		 * Execute this mapping for the passed XML document by: 1. Getting the mapping root(s) of the mapping. 2. If there isn't a mapping root, use
		 * the document element (one root). 3. Execute this mapping for each of the roots. 4. Each execution results in a single cal to om (one CSV
		 * line).
		 */
		XPathExecutable rootXPath = mapping.getMappingRoots();
		if (rootXPath == null) {
			List<String> outputLine = mapping.evaluate(xmlDoc, this.trimWhitespace);
			om.writeRecords(mapping.getOutputName(), outputLine);
		} else {
			try {
				XPathSelector rootIterator = rootXPath.load();
				rootIterator.setContextItem(xmlDoc);
				for (XdmItem item : rootIterator) {
					if (item instanceof XdmNode) {
						List<String> outputLine = mapping.evaluate((XdmNode) item, this.trimWhitespace);
						om.writeRecords(mapping.getOutputName(), outputLine);
					} else {
						LOG.warn("Expected XdmNode, got {}", item.getClass().getName());
					}
				}
			} catch (SaxonApiException e) {
				throw new DataExtractorException(e, "Error evaluating XPath %s", rootXPath);
			}
		}

		LOG.trace("Completed all mappings against documents");

	}

	/**
	 * Iterate over all the mappings and apply each set to the passed XML document and pass the results to the output manager. This method delegates
	 * to {@link #extractTo(Element, NameToXpathMappings, IOutputManager)}, by either finding the mapping root within {@link NameToXpathMappings} or
	 * using the document root element.
	 *
	 * @param xmlDoc The XML document to extract information from
	 * @param mapping The set of mappings that define the data to be extracted
	 * @param om The output manager to which the data should be sent
	 * @throws DataExtractorException If anything unrecoverable during data extraction happens
	 * @throws OutputManagerException if anything unrecoverable during writing output happens
	 */
	public void extractDocTo(XdmNode xmlDoc, IMappingContainer mapping, IOutputManager om) throws DataExtractorException, OutputManagerException {
		om.writeRecords(mapping.getOutputName(), mapping.evaluate(xmlDoc, this.trimWhitespace));
	}

	/**
	 * Executes the mappings set by {@link #setMappings(MappingConfiguration)} against a document <code>xmlDoc</code> and passes the results to
	 * <code>om</code>.
	 *
	 * @param xmlDoc The XML document to extract information from.
	 * @param om The output manager to send the extracted data to.
	 * @throws DataExtractorException If an error occurred extracting data from the XML document.
	 * @throws OutputManagerException If an error occurred writing data to the output manager.
	 */
	public void extractDocTo(XdmNode xmlDoc, IOutputManager om) throws DataExtractorException, OutputManagerException {
		LOG.trace("Executing {} sets of mappings.", this.mappings.size());
		for (MappingList mapping : this.mappings.mappingsToArray()) {
			executeMappingOnDoc(xmlDoc, om, mapping);
		}
	}

	/**
	 * Configure this extractor with the set of mappings specified.
	 *
	 * @param newMappings the mappings that define how to extract data from the XML when {@link #extractTo} is called.
	 */
	public void setMappings(MappingConfiguration newMappings) {
		this.mappings = newMappings;
	}

	/**
	 * If set to true, then all whitespace will be trimmed from the beginning and end of values extracted from XML. If false, they will be left alone.
	 * <p>
	 * The typically required behaviour is to set this to true.
	 *
	 * @param trimWhitespace whether to trim whitespace from the beginning and end of values extracted from XML.
	 */
	public void setTrimWhitespace(boolean trimWhitespace) {
		this.trimWhitespace = trimWhitespace;
	}
}
