package com.locima.xml2csv.inputparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.ArgumentException;
import com.locima.xml2csv.ArgumentNullException;
import com.locima.xml2csv.SaxonProcessorManager;
import com.locima.xml2csv.StringUtil;
import com.locima.xml2csv.Tuple;
import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.extractor.DataExtractorException;

/**
 * Models an ordered list of mappings of column outputName to XPath expression.
 */
public class MappingList extends ArrayList<IMapping> implements IMappingContainer {

	private static final Logger LOG = LoggerFactory.getLogger(MappingList.class);

	private static final long serialVersionUID = -3781997484476001198L;

	private Map<String, String> defaultNamespaceMappings;

	private XPathExecutable mappingRoot;

	private String outputName;

	private Processor saxonProcessor;

	/**
	 * Calls {@link MappingList#NameToXPathMappings(Map)} with an empty map.
	 */
	public MappingList() {
		this(new HashMap<String, String>());
	}

	/**
	 * Initialises a Saxon processor, using the supplied map of namespace prefix to URI mappings.
	 *
	 * @param defaultPrefixUriMap a (possibly empty, but must not be null) map of prefix to URI mappings
	 */
	public MappingList(Map<String, String> defaultPrefixUriMap) {
		if (defaultPrefixUriMap == null) {
			throw new IllegalArgumentException("defaultPrefixUriMap");
		}
		this.saxonProcessor = SaxonProcessorManager.getProcessor();
		this.defaultNamespaceMappings = defaultPrefixUriMap;
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

	@Override
	public List<String> evaluate(XdmNode rootNode, boolean trimWhitespace) throws DataExtractorException {
		List<String> values = new ArrayList<String>();
		for (IMapping mapping : this) {
			values.addAll(mapping.evaluate(rootNode, trimWhitespace));
		}
		return values;
	}

	@Override
	public List<String> getColumnNames() {
		List<String> colNames = new ArrayList<String>();

		for (IMapping mapping : this) {
			colNames.addAll(mapping.getColumnNames());
		}
		return colNames;
	}

	public XPathExecutable getMappingRoots() {
		return this.mappingRoot;
	}

	@Override
	public Tuple<String, List<String>> getMappingsHeaders() {
		return new Tuple<String, List<String>>(this.outputName, getColumnNames());
	}

	/**
	 * Retrieves the output outputName of this set of mappings.
	 *
	 * @return the outputName of this set of mappings. Will never be null or the empty string.
	 */
	@Override
	public String getOutputName() {
		return this.outputName;
	}

	/**
	 * Stores a new column definition in this set of mappings.
	 *
	 * @param colName the outputName of the column, must a string of length > 0.
	 * @param defaultNamespace the default namespace URI.
	 * @param xPathExpression the XPath expression to compile. Must not be null.
	 * @throws XMLException If there was problem compiling the expression (for example, if the XPath is invalid).
	 */
	public void put(String colName, String defaultNamespace, String xPathExpression) throws XMLException {
		if (StringUtil.isNullOrEmpty(colName)) {
			throw new ArgumentException("colName", StringUtil.NULL_OR_EMPTY_MESSAGE);
		}
		if (StringUtil.isNullOrEmpty(xPathExpression)) {
			throw new ArgumentException("xPathExpression", StringUtil.NULL_OR_EMPTY_MESSAGE);
		}
		XPathExecutable xPath = createXPathExecutable(defaultNamespace, xPathExpression);
		this.add(new Mapping(colName, new XPathValue(xPathExpression, xPath)));
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
	 * Sets the output outputName of this mapping.
	 *
	 * @param newName the new outputName of the mapping. Must not be null or the empty string.
	 */
	public void setName(String newName) {
		if (newName == null) {
			throw new ArgumentNullException("newName");
		}
		if (newName.length() == 0) {
			throw new ArgumentException("newName", "must have a length >0");
		}
		this.outputName = newName;
	}

}
