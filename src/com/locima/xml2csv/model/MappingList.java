package com.locima.xml2csv.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.ArgumentException;
import com.locima.xml2csv.ArgumentNullException;
import com.locima.xml2csv.StringUtil;
import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.XmlUtil;
import com.locima.xml2csv.extractor.DataExtractorException;

/**
 * Models an ordered list of mappings of column containerName to XPath expression.
 */
public class MappingList extends ArrayList<IMapping> implements IMappingContainer {

	private static final Logger LOG = LoggerFactory.getLogger(MappingList.class);

	private static final long serialVersionUID = -3781997484476001198L;

	// /**
	// * Retrieve the number of columns that will be rendered within this mapping container.
	// * <p>
	// * Recursively called for child mapping lists.
	// * <p>
	// * Used to work out how many empty columns are required in a record when no values are present.
	// *
	// * @param mappings The list of mappings (typically a {@link MappingList} instance) the count the columns within.
	// * @return the number of columns found.
	// */
	// private static int getChildColumnCount(List<IMapping> mappings) {
	// int count = 0;
	// for (IMapping child : mappings) {
	// int childMaxInstanceCount = child.getMaxInstanceCount();
	// if (child instanceof ISingleMapping) {
	// count += childMaxInstanceCount;
	// } else if (child instanceof MappingList) {
	// MappingList childList = (MappingList) child;
	// count += childMaxInstanceCount * getChildColumnCount(childList);
	// } else {
	// throw new IllegalStateException("Unexpected type of IMappingContainer found: " + child.getClass().getName());
	// }
	// }
	// return count;
	// }

	private String containerName;

	private MultiValueBehaviour defaultMultiValueBehaviour;

	/**
	 * Retrieves the mapping root expression that, when evaluated, will return all the XML node that should be used to extract data from. If null then
	 * this mapping will run from the parent mapping root (the top level mapping list will use the document node).
	 */
	private XPathExecutable mappingRoot;

	private int maximumResultCount;

	private int minimumResultCount = 1;

	private Map<String, String> namespaceMappings;

	/**
	 * Calls {@link MappingList#NameToXPathMappings(Map)} with an empty map.
	 */
	public MappingList() {
		this(null);
	}

	/**
	 * Initialises a Saxon processor, using the supplied map of namespace prefix to URI mappings.
	 *
	 * @param namespaceMap a (possibly empty, but must not be null) map of prefix to URI mappings.
	 */
	public MappingList(Map<String, String> namespaceMap) {
		this.namespaceMappings = namespaceMap;
	}

	public void add(FieldDefinition fd) {
	}

	@Override
	public RecordSet evaluate(XdmNode rootNode, boolean trimWhitespace) throws DataExtractorException {
		/**
		 * Execute this mapping for the passed XML document by: 1. Getting the mapping root(s) of the mapping. 2. If there isn't a mapping root, use
		 * the root node passed. 3. Execute this mapping for each of the root(s). 4. Each execution results in a single call to om (one CSV line).
		 */

		int instanceCount = 0;
		RecordSet rs = new RecordSet();
		if (this.mappingRoot != null) {
			XPathSelector rootIterator;
			rootIterator = getNodeIterator(rootNode, this.mappingRoot);
			for (XdmItem item : rootIterator) {
				if (item instanceof XdmNode) {
					instanceCount++;
					evaluate((XdmNode) item, rs, trimWhitespace);
				} else {
					LOG.warn("Expected to find only elements after executing XPath on mapping list, got {}", item.getClass().getName());
				}
			}
		} else {
			evaluate(rootNode, rs, trimWhitespace);
		}

		this.maximumResultCount = Math.max(this.maximumResultCount, instanceCount);

		LOG.trace("Completed all mappings against document");
		return rs;
	}

	/**
	 * Evaluates a nested mapping, appending the results to the output line passed.
	 *
	 * @param node the node from which all mappings will be based on.
	 * @param outputLine the existing output line that will be appended to.
	 * @param trimWhitespace if true, then leading and trailing whitespace will be removed from all data values.
	 * @throws DataExtractorException if an error occurred whilst extracting data (typically this would be caused by bad XPath, or XPath invalid from
	 *             the <code>mappingRoot</code> specified).
	 */
	private void evaluate(XdmNode node, RecordSet outputLine, boolean trimWhitespace) throws DataExtractorException {
		for (IMapping mapping : this) {
			RecordSet records = mapping.evaluate(node, trimWhitespace);
			outputLine.addAll(records);
		}
	}

	/**
	 * Retrieves the output containerName of this set of mappings.
	 *
	 * @return the containerName of this set of mappings. Will never be null or the empty string.
	 */
	@Override
	public String getContainerName() {
		return this.containerName;
	}

	/**
	 * Recursive implementation of {@link #getColumnNames(List)}. This ensures that the parent iteration count is available.
	 *
	 * @param fieldNames the list of column names that is being built up.
	 * @param parentName the name of the parent mapping list (or <code>null</code> if this {@link MappingList} is a direct child of the
	 *            {@link MappingConfiguration}.
	 * @param parentCount the iteration of the parent mapping list that we're currently within.
	 * @return the number of columns added by this invocation.
	 */
	@Override
	public int getFieldNames(List<String> fieldNames, String parentName, int parentCount) {
		int columnCount = 0;
		/*
		 * If this is a non-nested MappingList, i.e. a direct child of MappingConfiguration then the instance count refers to the number of records
		 * output, not the number of fields (as a nested, in-line MappingList would indicate. Therefore, only process as in-line if nested.
		 */
		int repeats = parentName != null ? getMaxResultCount() : 1;
		String mappingListName = getContainerName();
		for (int mappingListIteration = 0; mappingListIteration < repeats; mappingListIteration++) {
			for (IMapping mapping : this) {
				columnCount += mapping.getFieldNames(fieldNames, mappingListName, mappingListIteration);
			}
		}
		return columnCount;
	}

	@Override
	public List<String> getFieldNames(String parentName, int parentIterationNumber) {
		List<String> fieldNames = new ArrayList<String>();
		getFieldNames(fieldNames, parentName, parentIterationNumber);
		return fieldNames;
	}

	private int getMaxResultCount() {
		return Math.max(this.minimumResultCount, this.maximumResultCount);
	}

	@Override
	public MultiValueBehaviour getMultiValueBehaviour() {
		return this.defaultMultiValueBehaviour;
	}

	@Override
	public NameFormat getNameFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Returns the set of namespace prefix to URI mappings this mapping list is using.
	 *
	 * @return the mapping of namespace prefixes to URIs. May be null or empty.
	 */
	public Map<String, String> getNamespaceMappings() {
		return this.namespaceMappings;
	}

	/**
	 * Retrieves an iterator over the results of executing the passed xpath against the rootNode specified.
	 *
	 * @param rootNode the root node to execute the mapping on.
	 * @param xPath the XPath to execute against the root node passed.
	 * @return an iterator over the results.
	 * @throws DataExtractorException if an error occurs executing the XPath.
	 */
	private XPathSelector getNodeIterator(XdmNode rootNode, XPathExecutable xPath) throws DataExtractorException {
		XPathSelector nodeIterator = xPath.load();
		try {
			nodeIterator.setContextItem(rootNode);
		} catch (SaxonApiException e) {
			throw new DataExtractorException(e, "Error evaluating XPath %s", xPath);
		}
		return nodeIterator;
	}

	/**
	 * Sets the query that returns the XML node(s) from which all the mappings will be based.
	 *
	 * @param mappingRootXPathExpression the XPath expression that will return one or more nodes. All other XPath expressions within this mapping will
	 *            be executed from the context of the returned node(s). Multiple nodes means multiple lines of output.
	 * @throws XMLException If there was problem compiling the expression (for example, if the XPath is invalid).
	 */
	public void setMappingRoot(String mappingRootXPathExpression) throws XMLException {
		String xPath = StringUtil.isNullOrEmpty(mappingRootXPathExpression) ? "." : mappingRootXPathExpression;
		this.mappingRoot = XmlUtil.createXPathExecutable(this.namespaceMappings, xPath);
	}

	/**
	 * Sets the output containerName of this mapping.
	 *
	 * @param newName the new containerName of the mapping. Must not be null or the empty string.
	 */
	public void setOutputName(String newName) {
		if (newName == null) {
			throw new ArgumentNullException("newName");
		}
		if (newName.length() == 0) {
			throw new ArgumentException("newName", "must have a length >0");
		}
		this.containerName = newName;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder("MappingList(");
		sb.append(this.containerName);
		sb.append(")[");
		for (IMapping mapping : this) {
			sb.append(mapping.toString());
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public int getGroupNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

}
