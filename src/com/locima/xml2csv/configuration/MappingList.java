package com.locima.xml2csv.configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.ArgumentNullException;

/**
 * Models an ordered list of mappings of column containerName to XPath expression.
 */
public class MappingList extends AbstractMappingContainer implements IMappingContainer {

	private static final Logger LOG = LoggerFactory.getLogger(MappingList.class);

	private List<IMapping> children;

	/**
	 * Initialises the internal list of children.
	 */
	public MappingList() {
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

	@Override
	public int getFieldCountForSingleRecord() {
		return getMultiValueBehaviour() == MultiValueBehaviour.LAZY ? 1 : Math.max(getMinValueCount(), getHighestFoundValueCount());
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

	@Override
	public int size() {
		return this.children.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("MappingList(");
		sb.append(getName());
		final String separator = ", ";
		sb.append(separator);
		sb.append(getMultiValueBehaviour());
		sb.append(separator);
		sb.append(getGroupNumber());
		sb.append(separator);
		sb.append(getMinValueCount());
		sb.append(separator);
		sb.append(getMaxValueCount());
		sb.append(separator);
		sb.append(getHighestFoundValueCount());
		sb.append(")[");
		sb.append(size());
		sb.append(" children]");
		return sb.toString();
	}

}
