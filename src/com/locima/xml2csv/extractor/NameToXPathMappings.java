package com.locima.xml2csv.extractor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.XMLConstants;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.ArgumentException;
import com.locima.xml2csv.ArgumentNullException;
import com.locima.xml2csv.SaxonProcessorManager;
import com.locima.xml2csv.XMLException;

/**
 * Models an ordered list of mappings of column name to XPath expression.
 */
public class NameToXPathMappings extends LinkedHashMap<String, XPathValue> {

	private static final Logger LOG = LoggerFactory.getLogger(NameToXPathMappings.class);

	private static final long serialVersionUID = -3781997484476001198L;

	private Map<String, String> defaultNamespaceMappings;

	private XPathExecutable mappingRoot;

	private String name;

	private Processor saxonProcessor;

	/**
	 * Calls {@link NameToXPathMappings#NameToXPathMappings(Map)} with an empty map.
	 */
	public NameToXPathMappings() {
		this(new HashMap<String, String>());
	}

	/**
	 * Initialises a Saxon processor, using the supplied map of namespace prefix to URI mappings.
	 *
	 * @param defaultPrefixUriMap a (possibly empty, but must not be null) map of prefix to URI mappings
	 */
	public NameToXPathMappings(Map<String, String> defaultPrefixUriMap) {
		if (defaultPrefixUriMap == null) {
			throw new IllegalArgumentException("defaultPrefixUriMap");
		}
		this.saxonProcessor = SaxonProcessorManager.getProcessor();
		this.defaultNamespaceMappings = defaultPrefixUriMap;
	}

	/**
	 * Returns true if the mapping contains a column with a specified name.
	 *
	 * @param colName the name to search for.
	 * @return true if the name could be found, false otherwise.
	 */
	public boolean containsColumn(String colName) {
		return containsKey(colName);
	}

	/**
	 * Given a default namespace URI and an XPath expression (that uses only the default namespace), create a compiled version of the XPath
	 * expression.
	 * 
	 * @param defaultNamespace the default namespace URI.
	 * @param xPathExpression the XPath expression to compile
	 * @return A compiled XPath expression.
	 * @throws XMLException If there was problem compiling the expression (for example, if the XPath is invalid).
	 */
	private XPathExecutable createXPathExecutable(String defaultNamespace, String xPathExpression) throws XMLException {
		// Need to construct a new compiler because the set of namespaces is (potentially) unique to the expression.
		// We could cache a set of compilers, but I doubt it's worth it.
		XPathCompiler xPathCompiler = this.saxonProcessor.newXPathCompiler();
		for (Map.Entry<String, String> entry : this.defaultNamespaceMappings.entrySet()) {
			String prefix = entry.getKey();
			String uri = entry.getValue();
			xPathCompiler.declareNamespace(prefix, uri);
			if (prefix.equals(defaultNamespace)) {
				LOG.trace("Allocating default namespace prefix {} to URI {}", prefix, uri);
				xPathCompiler.declareNamespace(XMLConstants.DEFAULT_NS_PREFIX, uri);
			}
		}

		try {
			XPathExecutable xPath = xPathCompiler.compile(xPathExpression);
			return xPath;
		} catch (SaxonApiException e) {
			throw new XMLException(e, "Unable to compile invalid XPath: %s", xPathExpression);
		}
	}

	/**
	 * Gets the XPath that when executed specifies the root element(s) from which all data should be extracted from within a document.
	 *
	 * @return an XPath selector based on the mapping root specified by {@link #setMappingRoot(String, String)} or null if one hasn't been specified
	 */
	public XPathSelector getMappingRoot() {
		return this.mappingRoot == null ? null : this.mappingRoot.load();
	}

	/**
	 * Retrieves the output name of this set of mappings.
	 * 
	 * @return the name of this set of mapings. Will never be null or the empty string.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Stores a new column definition in this set of mappings.
	 * 
	 * @param colName the name of the column, may be null or empty, but must be unique.
	 * @param defaultNamespace the default namespace URI.
	 * @param xPathExpression the XPath expression to compile
	 * @throws XMLException If there was problem compiling the expression (for example, if the XPath is invalid).
	 */
	public void put(String colName, String defaultNamespace, String xPathExpression) throws XMLException {
		XPathExecutable xPath = createXPathExecutable(defaultNamespace, xPathExpression);
		this.put(colName, new XPathValue(xPathExpression, xPath));
	}

	/**
	 * Sets the query that returns the XML node(s) from which all the mappings will be based.
	 * 
	 * @param defaultNamespace the default namespace URI.
	 * @param mappingRootXPathExpression the XPath expression that will return one or more nodes. All other XPath expressions within this mapping will
	 *            be executed from the context of the returned node(s). Multiple nodes means multiple lines of output.
	 * @throws XMLException If there was problem compiling the expression (for example, if the XPath is invalid).
	 */
	public void setMappingRoot(String defaultNamespace, String mappingRootXPathExpression) throws XMLException {
		this.mappingRoot = createXPathExecutable(defaultNamespace, mappingRootXPathExpression);
	}

	/**
	 * Sets the output name of this mapping.
	 * 
	 * @param newName the new name of the mapping. Must not be null or the empty string.
	 */
	public void setName(String newName) {
		if (newName == null) {
			throw new ArgumentNullException("newName");
		}
		if (newName.length() == 0) {
			throw new ArgumentException("newName", "must have a length >0");
		}
		this.name = newName;
	}

}
