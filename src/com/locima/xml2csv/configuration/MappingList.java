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
public class MappingList implements IMappingContainer {

	private static final Logger LOG = LoggerFactory.getLogger(MappingList.class);

	private List<IMapping> children;

	private String containerName;

	private int groupNumber;

	private int highestFoundValueCount;

	/**
	 * Retrieves the mapping root expression that, when evaluated, will return all the XML node that should be used to extract data from. If null then
	 * this mapping will run from the parent mapping root (the top level mapping list will use the document node).
	 */
	private XPathValue mappingRoot;

	private int maxValueCount;

	private int minValueCount;

	private MultiValueBehaviour multiValueBehaviour;

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
		this.children = new ArrayList<IMapping>();
	}

	/**
	 * Adds a new child mapping to this instance.
	 *
	 * @param mapping the mapping to add to this container. Must not be null.
	 */
	public void add(IMapping mapping) {
		if (mapping == null) {
			throw new ArgumentNullException("Cannot add null to child of this mapping " + this);
		}
		this.children.add(mapping);
	}

	/**
	 * Retrieve the child mapping at the index specified.
	 * 
	 * @param index the index of the child to retrieve.
	 * @return the child mapping at the index specified. Will never return null.
	 */
	public IMapping get(int index) {
		return this.children.get(index);
	}

	/**
	 * Retrieves the output containerName of this set of mappings.
	 *
	 * @return the containerName of this set of mappings. Will never be null or the empty string.
	 */
	@Override
	public String getName() {
		return this.containerName;
	}

	@Override
	public int getFieldCountForSingleRecord() {
		return getMultiValueBehaviour() == MultiValueBehaviour.LAZY ? 1 : Math.max(getMinValueCount(), getHighestFoundValueCount());
	}

	@Override
	public int getGroupNumber() {
		return this.groupNumber;
	}

	@Override
	public int getHighestFoundValueCount() {
		return this.highestFoundValueCount;
	}

	@Override
	public XPathValue getMappingRoot() {
		return this.mappingRoot;
	}

	@Override
	public int getMaxValueCount() {
		return this.maxValueCount;
	}

	@Override
	public int getMinValueCount() {
		return this.minValueCount;
	}

	@Override
	public MultiValueBehaviour getMultiValueBehaviour() {
		return this.multiValueBehaviour;
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
		throw new UnsupportedOperationException();
		// return this.parent;
	}

	/**
	 * Look at all ourself and all of our contained mappings, if they're all fixed output then return <code>true</code>, if only one isn't then we
	 * can't guarantee how many fields are output, so return <code>false</code>.
	 *
	 * @return <code>true</code> if execution of this container and all its children will always result in a consistent, known, number of output
	 *         fields.
	 */
	@Override
	public boolean hasFixedOutputCardinality() {
		boolean isFixed =
						(getMultiValueBehaviour() == MultiValueBehaviour.LAZY)
										|| ((getMinValueCount() == getMaxValueCount()) && (getMinValueCount() > 0));

		if (isFixed) {
			for (IMapping mapping : this) {
				if (!mapping.hasFixedOutputCardinality()) {
					isFixed = false;
					break;
				}
			}
		}
		LOG.info("MappingList {} hasFixedOutputCardinality = {}", this, isFixed);
		return isFixed;
	}

	@Override
	public Iterator<IMapping> iterator() {
		return this.children.iterator();
	}

	/**
	 * Sets the group number of this mapping container. Containers that use the same group number are incremented together when iterating output
	 * records.
	 *
	 * @param groupNumber the group number to use, must be 0 or greater. (Negative numbers are reserved).
	 */
	public void setGroupNumber(int groupNumber) {
		this.groupNumber = groupNumber;
	}

	@Override
	public void setHighestFoundValueCount(int valueCount) {
		this.highestFoundValueCount = Math.max(valueCount, this.highestFoundValueCount);
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

	/**
	 * Sets the maximum number of results that will be processed when executing the {@link #mappingRoot}. Any results over and above this value will
	 * be discarded.
	 *
	 * @param maxValueCount either 0 for no maximum, or a natural number greater than zero to apply a limit.
	 */

	public void setMaxValueCount(int maxValueCount) {
		this.maxValueCount = maxValueCount;
	}

	/**
	 * Sets the minimum number of results that should be processed when executing the {@link #mappingRoot}. If not enough nodes are found by executing
	 * the {@link #mappingRoot} then blank fields will be inserted in to the output.
	 *
	 * @param minValueCount either 0 for no minimum, or a natural number greater than zero to apply a minimum.
	 */
	public void setMinValueCount(int minValueCount) {
		this.minValueCount = minValueCount;
	}

	/**
	 * Sets the multi-value behaviour of this container. See {@link MultiValueBehaviour}.
	 *
	 * @param multiValueBehaviour the multi-value behaviour to use.
	 */
	public void setMultiValueBehaviour(MultiValueBehaviour multiValueBehaviour) {
		this.multiValueBehaviour = multiValueBehaviour;
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
	public int size() {
		return this.children.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("MappingList(");
		sb.append(this.containerName);
		final String separator = ", ";
		sb.append(separator);
		sb.append(this.multiValueBehaviour);
		sb.append(separator);
		sb.append(this.groupNumber);
		sb.append(separator);
		sb.append(this.minValueCount);
		sb.append(separator);
		sb.append(this.maxValueCount);
		sb.append(separator);
		sb.append(this.highestFoundValueCount);
		sb.append(")[");
		sb.append(size());
		sb.append(" children]");
		/*
		 * sb.append(")["); Iterator<IMapping> mappings = iterator(); while (mappings.hasNext()) { sb.append(mappings.next().toString()); if
		 * (mappings.hasNext()) { sb.append(separator); } } sb.append("]");
		 */
		return sb.toString();
	}

}
