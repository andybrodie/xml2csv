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

	public boolean containsColumn(String colName) {
		return containsKey(colName);
	}

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

	public String getName() {
		return this.name;
	}

	public void put(String colName, String defaultNamespace, String xPathExpression) throws XMLException {
		XPathExecutable xPath = createXPathExecutable(defaultNamespace, xPathExpression);
		this.put(colName, new XPathValue(xPathExpression, xPath));
	}

	public void setMappingRoot(String defaultNamespace, String mappingRootXPathExpr) throws XMLException {
		this.mappingRoot = createXPathExecutable(defaultNamespace, mappingRootXPathExpr);
	}

	public void setName(String newName) {
		this.name = newName;
	}

}
