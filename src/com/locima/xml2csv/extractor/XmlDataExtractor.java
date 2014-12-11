package com.locima.xml2csv.extractor;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.locima.xml2csv.ArgumentNullException;
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

	public XmlDataExtractor() {
//		this.contextManager = new ExtractionContextManager();
	}
	
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
		LOG.info("Executing {} sets of mappingConfiguration", this.mappingConfiguration.size());
		this.mappingConfiguration.log();
		int index = 0;
		for (IMappingContainer mapping : this.mappingConfiguration) {
			ContainerExtractionContext ctx = new ContainerExtractionContext(mapping, index);
			ctx.evaluate(xmlDoc);
			outputManager.writeRecords(mapping.getContainerName(), ctx);
			index++;
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
