package com.locima.xml2csv.configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.ArgumentException;
import com.locima.xml2csv.ArgumentNullException;
import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.util.StringUtil;
import com.locima.xml2csv.util.XmlUtil;

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

	/**
	 * Retrieves the mapping root expression that, when evaluated, will return all the XML node that should be used to extract data from. If null then
	 * this mapping will run from the parent mapping root (the top level mapping list will use the document node).
	 */
	private XPathValue mappingRoot;

	private int maximumResultCount;

	private int minimumResultCount = 1;

	private MultiValueBehaviour multiValueBehaviour;

	private Map<String, String> namespaceMappings;

	private IMappingContainer parent;

	private int groupNumber;

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

	@Override
	public int getGroupNumber() {
		return this.groupNumber;
	}
	
	public void setGroupNumber(int groupNumber) {
		this.groupNumber = groupNumber;
	}

	@Override
	public XPathValue getMappingRoot() {
		return this.mappingRoot;
	}

	private int getMaxResultCount() {
		return Math.max(this.minimumResultCount, this.maximumResultCount);
	}

	@Override
	public NameFormat getNameFormat() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the set of namespace prefix to URI mappings this mapping list is using.
	 *
	 * @return the mapping of namespace prefixes to URIs. May be null or empty.
	 */
	public Map<String, String> getNamespaceMappings() {
		return this.namespaceMappings;
	}

	@Override
	public IMappingContainer getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Look at all our contained mappings, if they're all fixed output then return <code>true</code>, if only one isn't then we can't guarantee how
	 * many fields are output, so return <code>false</code>.
	 */
	@Override
	public boolean hasFixedOutputCardinality() {
		boolean isFixed = true;
		for (IMapping mapping : this) {
			if (!mapping.hasFixedOutputCardinality()) {
				isFixed = false;
				break;
			}
		}
		LOG.info("MappingList {} hasFixedOutputCardinality = {}", this, isFixed);
		return isFixed;
	}

	/**
	 * Sets the query that returns the XML node(s) from which all the mappings will be based.
	 *
	 * @param mappingRootXPathExpression the XPath expression that will return one or more nodes. All other XPath expressions within this mapping will
	 *            be executed from the context of the returned node(s). Multiple nodes means multiple lines of output.
	 * @throws XMLException If there was problem compiling the expression (for example, if the XPath is invalid).
	 */
	public void setMappingRoot(String mappingRootXPathExpression) throws XMLException {
		if (!StringUtil.isNullOrEmpty(mappingRootXPathExpression)) {
			this.mappingRoot = XmlUtil.createXPathValue(this.namespaceMappings, mappingRootXPathExpression);
		}
	}

	public void setMultiValueBehaviour(MultiValueBehaviour multiValueBehaviour) {
		this.multiValueBehaviour = (multiValueBehaviour == MultiValueBehaviour.DEFAULT) ? MultiValueBehaviour.LAZY : multiValueBehaviour;
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

	public void setParent(IMappingContainer parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("MappingList(");
		sb.append(this.containerName);
		sb.append(", ");
		sb.append(this.multiValueBehaviour);
		sb.append(")[");
		Iterator<IMapping> mappings = iterator();
		while (mappings.hasNext()) {
			sb.append(mappings.next().toString());
			if (mappings.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public MultiValueBehaviour getMultiValueBehaviour() {
		return this.multiValueBehaviour;
	}

	@Override
	public int getMaxValueCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMinValueCount() {
		// TODO Auto-generated method stub
		return 0;
	}

}
