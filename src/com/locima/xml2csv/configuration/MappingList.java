package com.locima.xml2csv.configuration;

import java.util.ArrayList;
import java.util.Iterator;
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

	/**
	 *
	 */
	private static final long serialVersionUID = -5914600946119131908L;

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

	private IMappingContainer parent;

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
		return this.parent;
	}

	/**
	 * Look at all ourself and all of our contained mappings, if they're all fixed output then return <code>true</code>, if only one isn't then we
	 * can't guarantee how many fields are output, so return <code>false</code>.
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

	public void setGroupNumber(int groupNumber) {
		this.groupNumber = groupNumber;
	}

	@Override
	public void setHighestFoundValueCount(int valueCount) {
		this.highestFoundValueCount = valueCount;
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

	public void setMaxValueCount(int maxValueCount) {
		this.maxValueCount = maxValueCount;
	}

	public void setMinValueCount(int minValueCount) {
		this.minValueCount = minValueCount;
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
		sb.append(", ");
		sb.append(this.minValueCount);
		sb.append(", ");
		sb.append(this.maxValueCount);
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

}
