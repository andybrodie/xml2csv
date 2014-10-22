package com.locima.xml2csv.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.ArgumentException;
import com.locima.xml2csv.ArgumentNullException;
import com.locima.xml2csv.SaxonProcessorManager;
import com.locima.xml2csv.StringUtil;
import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.XmlUtil;
import com.locima.xml2csv.extractor.DataExtractorException;

/**
 * Models an ordered list of mappings of column outputName to XPath expression.
 */
public class MappingList extends ArrayList<IMapping> implements IMappingContainer {

	private static final Logger LOG = LoggerFactory.getLogger(MappingList.class);

	private static final long serialVersionUID = -3781997484476001198L;

	/**
	 * Retrieve the number of columns that will be rendered within this mapping container.
	 * <p>
	 * Recursively called for child mapping lists.
	 * <p>
	 * Used to work out how many empty columns are required in a record when no values are present.
	 *
	 * @param mappings The list of mappings (typically a {@link MappingList} instance) the count the columns within.
	 * @return the number of columns found.
	 */
	private static int getChildColumnCount(List<IMapping> mappings) {
		int count = 0;
		for (IMapping child : mappings) {
			int childMaxInstanceCount = child.getMaxInstanceCount();
			if (child instanceof ISingleMapping) {
				count += childMaxInstanceCount;
			} else if (child instanceof MappingList) {
				MappingList childList = (MappingList) child;
				count += childMaxInstanceCount * getChildColumnCount(childList);
			} else {
				throw new IllegalStateException("Unexpected type of IMappingContainer found: " + child.getClass().getName());
			}
		}
		return count;
	}

	private XPathExecutable mappingRoot;

	private int maxInstanceCount;

	private int minimumInstanceCount = 1;

	private Map<String, String> namespaceMappings;

	private String outputName;

	private IMappingContainer parent;

	private Processor saxonProcessor;

	/**
	 * Calls {@link MappingList#NameToXPathMappings(Map)} with an empty map and no parent (top-level mapping).
	 */
	public MappingList() {
		this(null, new HashMap<String, String>());
	}

	/**
	 * Initialises a Saxon processor, using the supplied map of namespace prefix to URI mappings.
	 *
	 * @param parent the parent mapping container of this mapping list. If null then this mapping list is assumed to be a top-level mapping (i.e. it's
	 *            parent is the {@link MappingConfiguration} object.
	 * @param namespaceMap a (possibly empty, but must not be null) map of prefix to URI mappings.
	 */
	public MappingList(IMappingContainer parent, Map<String, String> namespaceMap) {
		this.saxonProcessor = SaxonProcessorManager.getProcessor();
		this.parent = parent;
		this.namespaceMappings = namespaceMap;
	}

	/**
	 * Given a default namespace URI and an XPath expression (that uses only the default namespace), create a compiled version of the XPath
	 * expression.
	 *
	 * @param xPathExpression the XPath expression to compile
	 * @return A compiled XPath expression.
	 * @throws XMLException If there was problem compiling the expression (for example, if the XPath is invalid).
	 */
	private XPathExecutable createXPathExecutable(String xPathExpression) throws XMLException {
		return XmlUtil.createXPathExecutable(this.namespaceMappings, xPathExpression);
	}

	@Override
	public List<String> evaluate(XdmNode rootNode, boolean trimWhitespace) throws DataExtractorException {
		List<List<String>> outputLines = evaluateToRecordList(rootNode, trimWhitespace);
		List<String> outputLine = new ArrayList<String>();
		for (List<String> line : outputLines) {
			outputLine.addAll(line);
		}
		return outputLine;
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
	private void evaluate(XdmNode node, List<String> outputLine, boolean trimWhitespace) throws DataExtractorException {
		for (IMapping mapping : this) {
			List<String> records = mapping.evaluate(node, trimWhitespace);
			outputLine.addAll(records);
		}
	}

	@Override
	public List<List<String>> evaluateToRecordList(XdmNode rootNode, boolean trimWhitespace) throws DataExtractorException {
		/**
		 * Execute this mapping for the passed XML document by: 1. Getting the mapping root(s) of the mapping. 2. If there isn't a mapping root, use
		 * the root node passed. 3. Execute this mapping for each of the root(s). 4. Each execution results in a single call to om (one CSV line).
		 */

		List<List<String>> outputLines = new ArrayList<List<String>>();
		XPathExecutable rootXPath = getMappingRoots();
		int instanceCount = 0;
		if (rootXPath != null) {
			XPathSelector rootIterator;
			rootIterator = getNodeIterator(rootNode, rootXPath);
			for (XdmItem item : rootIterator) {
				if (item instanceof XdmNode) {
					List<String> outputLine = new ArrayList<String>();
					evaluate((XdmNode) item, outputLine, trimWhitespace);
					instanceCount++;
					outputLines.add(outputLine);
				} else {
					LOG.warn("Expected to find only elements after executing XPath on mapping list, got {}", item.getClass().getName());
				}
			}
		} else {
			List<String> outputLine = new ArrayList<String>();
			evaluate(rootNode, outputLine, trimWhitespace);
			instanceCount++;
			outputLines.add(outputLine);
		}

		// Add any blanks where maxInstanceCount is more than valuesSize
		int maxInstances = getMaxInstanceCount();
		if ((instanceCount < maxInstances) && (this.parent != null)) {
			int columnCount = getChildColumnCount(this);
			LOG.trace("Adding {} blank iterations of {} columns to make up to {} for {}", maxInstances - instanceCount, columnCount, maxInstances,
							getOutputName());
			for (int i = instanceCount; i < maxInstances; i++) {
				List<String> emptyValues = new ArrayList<String>();
				for (int j = 0; j < columnCount; j++) {
					emptyValues.add(StringUtil.EMPTY_STRING);
				}
				outputLines.add(emptyValues);
			}
		}
		this.maxInstanceCount = Math.max(this.maxInstanceCount, instanceCount);

		LOG.trace("Completed all mappings against documents");
		return outputLines;
	}

	@Override
	public int getColumnNames(List<String> columnNames) {
		int nestedRepeats = getColumnNames(columnNames, null, 0);
		return nestedRepeats;
	}

	/**
	 * Recursive implementation of {@link #getColumnNames(List)}. This ensures that the parent iteration count is available.
	 *
	 * @param columnNames the list of column names that is being built up.
	 * @param parentName the name of the parent mapping list (or <code>null</code> if this {@link MappingList} is a direct child of the
	 *            {@link MappingConfiguration}.
	 * @param parentCount the iteration of the parent mapping list that we're currently within.
	 * @return the number of columns added by this invocation.
	 */
	@Override
	public int getColumnNames(List<String> columnNames, String parentName, int parentCount) {
		int columnCount = 0;
		/*
		 * If this is a non-nested MappingList, i.e. a direct child of MappingConfiguration then the instance count refers to the number of records
		 * output, not the number of fields (as a nested, in-line MappingList would indicate. Therefore, only process as in-line if nested.
		 */
		int repeats = (parentName != null) ? getMaxInstanceCount() : 1;
		String mappingListName = getOutputName();
		for (int mappingListIteration = 0; mappingListIteration < repeats; mappingListIteration++) {
			for (IMapping mapping : this) {
				columnCount += mapping.getColumnNames(columnNames, mappingListName, mappingListIteration);
			}
		}
		return columnCount;
	}

	/**
	 * Retrieves the mapping root expression that, when evaluated, will return all the XML node that should be used to extract data from.
	 *
	 * @return a mapping root expression, or null if these mappings should be executed against whatever the parents root was (in the case of a
	 *         non-nested MappingList, this will be the document element of the XML document).
	 */
	public XPathExecutable getMappingRoots() {
		return this.mappingRoot;
	}

	@Override
	public int getMaxInstanceCount() {
		return Math.max(this.maxInstanceCount, this.minimumInstanceCount);
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
	 * Retrieves the output outputName of this set of mappings.
	 *
	 * @return the outputName of this set of mappings. Will never be null or the empty string.
	 */
	@Override
	public String getOutputName() {
		return this.outputName;
	}

	/**
	 * See {@link #put(String, String, InlineFormat)} with a default value of {@link InlineFormat#NO_COUNTS} for the inline format.
	 *
	 * @param colName the outputName of the column, must a string of length > 0.
	 * @param xPathExpression the XPath expression to compile. Must not be null.
	 * @throws XMLException if any errors occur
	 */
	public void put(String colName, String xPathExpression) throws XMLException {
		put(colName, xPathExpression, InlineFormat.NO_COUNTS, MultiValueBehaviour.INHERIT);
	}

	/**
	 * Stores a new column definition in this set of mappings.
	 *
	 * @param colName the outputName of the column, must a string of length > 0.
	 * @param xPathExpression the XPath expression to compile. Must not be null.
	 * @param format the format to be used for the {@link Mapping} instance that this method creates.
	 * @param multiValueBehaviour defines what should happen when multiple values are found for a single evaluation for this mapping.
	 * @throws XMLException If there was problem compiling the expression (for example, if the XPath is invalid).
	 */
	public void put(String colName, String xPathExpression, InlineFormat format, MultiValueBehaviour multiValueBehaviour) throws XMLException {
		if (StringUtil.isNullOrEmpty(colName)) {
			throw new ArgumentException("colName", StringUtil.NULL_OR_EMPTY_MESSAGE);
		}
		if (StringUtil.isNullOrEmpty(xPathExpression)) {
			throw new ArgumentException("xPathExpression", StringUtil.NULL_OR_EMPTY_MESSAGE);
		}
		XPathExecutable xPath = createXPathExecutable(xPathExpression);
		Mapping newMapping = new Mapping(colName, new XPathValue(xPathExpression, xPath), format, multiValueBehaviour);

		this.add(newMapping);
	}

	/**
	 * Sets the query that returns the XML node(s) from which all the mappings will be based.
	 *
	 * @param mappingRootXPathExpression the XPath expression that will return one or more nodes. All other XPath expressions within this mapping will
	 *            be executed from the context of the returned node(s). Multiple nodes means multiple lines of output.
	 * @throws XMLException If there was problem compiling the expression (for example, if the XPath is invalid).
	 */
	public void setMappingRoot(String mappingRootXPathExpression) throws XMLException {
		this.mappingRoot = createXPathExecutable(mappingRootXPathExpression);
	}

	/**
	 * Sets the output outputName of this mapping.
	 *
	 * @param newName the new outputName of the mapping. Must not be null or the empty string.
	 */
	public void setOutputName(String newName) {
		if (newName == null) {
			throw new ArgumentNullException("newName");
		}
		if (newName.length() == 0) {
			throw new ArgumentException("newName", "must have a length >0");
		}
		this.outputName = newName;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder("MappingList(");
		sb.append(getOutputName());
		sb.append(")[");
		for (IMapping mapping : this) {
			sb.append(mapping.toString());
		}
		sb.append("]");
		return sb.toString();
	}

}
