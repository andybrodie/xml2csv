package com.locima.xml2csv.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.output.GroupState;
import com.locima.xml2csv.util.EqualsUtil;

/**
 * Common attributes and behaviours for mapping implementations.
 */
public abstract class AbstractMapping implements IValueMapping {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractMapping.class);

	/**
	 * The baseName of the field that will be created by this mapping.
	 */
	private String baseName;

	/**
	 * To understand how groups work, see {@link GroupState}.
	 */
	private int groupNumber;

	private int highestFoundValueCount;

	/**
	 * Specifies the largest number of values that this mapping should return for a single mapping. If an execution of this mapping yields a number of
	 * results more than this value then values will be discarded.
	 */
	private int maxValueCount;

	/**
	 * Specifies the smallest number of values that this mapping should return for a single mapping. If an execution of this mapping yields a number
	 * of results fewer than this value, empty results will be appended to make up this value.
	 */
	private int minValueCount;

	/**
	 * The behaviour for this mapping to use when encountering multiple values for a single execution.
	 */
	private MultiValueBehaviour multiValueBehaviour;

	/**
	 * How to format the name of the mapping when outputting.
	 */
	private NameFormat nameFormat;

	/**
	 * The parent mapping container that this mapping is contained within.
	 */
	private IMappingContainer parent;

	/**
	 * The XPath to execute against an input document to find values for this mapping.
	 */
	private XPathValue valueXPath;

	/**
	 * Creates a new immutable Field Definition.
	 *
	 * @param parent the parent of this mapping. May be null if this is a top level mapping container.
	 * @param valueXPath a compiled XPath expression that will extract the values required for this field.
	 * @param format the format to be used for the {@link Mapping} instance that this method creates.
	 * @param groupNumber the group number for this field definition.
	 * @param multiValueBehaviour defines what should happen when multiple values are found for a single evaluation for this mapping.
	 */
	public AbstractMapping(IMappingContainer parent, String baseName, NameFormat format, int groupNumber, MultiValueBehaviour multiValueBehaviour,
					XPathValue valueXPath, int minValueCount, int maxValueCount) {
		this.parent = parent;
		this.baseName = baseName;
		this.groupNumber = groupNumber;
		this.multiValueBehaviour = multiValueBehaviour;
		this.nameFormat = format == null ? NameFormat.NO_COUNTS : format;
		this.valueXPath = valueXPath;
		this.minValueCount = minValueCount;
		this.maxValueCount = maxValueCount;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof AbstractMapping) {
			// CHECKSTYLE:OFF Can't think of another way to provide a base class implementation of equals
			AbstractMapping that = (AbstractMapping) obj;
			// CHECKSTYLE:ON
			return EqualsUtil.areEqual(this.nameFormat, that.nameFormat) && EqualsUtil.areEqual(this.groupNumber, that.groupNumber)
							&& EqualsUtil.areEqual(this.multiValueBehaviour, that.multiValueBehaviour)
							&& EqualsUtil.areEqual(this.valueXPath, that.valueXPath) && EqualsUtil.areEqual(this.baseName, that.baseName)
							&& EqualsUtil.areEqual(this.minValueCount, that.minValueCount)
							&& EqualsUtil.areEqual(this.maxValueCount, that.maxValueCount);
		} else {
			return false;
		}
	}
	

	/**
	 * Returns a hash code solely based on the name of the field, as this is the only thing that really makes a difference between storing and
	 * indexing.
	 *
	 * @return the hash code of the base name of this definition.
	 */
	@Override
	public int hashCode() {
		return this.baseName.hashCode();
	}

	@Override
	public String getBaseName() {
		return this.baseName;
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
	public int getMaxValueCount() {
		return this.maxValueCount;
	}

	@Override
	public int getMinValueCount() {
		return this.minValueCount;
	}

	@Override
	public MultiValueBehaviour getMultiValueBehaviour() {
		if (this.multiValueBehaviour == MultiValueBehaviour.DEFAULT) {
			// TODO Implement inheritence from parent (currently a mapping has no concept of parent container!)
			return MultiValueBehaviour.LAZY;
		} else {
			return this.multiValueBehaviour;
		}
	}

	@Override
	public NameFormat getNameFormat() {
		return this.nameFormat;
	}

	@Override
	public IMappingContainer getParent() {
		return this.parent;
	}

	/**
	 * Retrieve the XPath statement that will execute this mapping.
	 *
	 * @return the XPath statement that will execute this mapping.
	 */
	public XPathValue getValueXPath() {
		return this.valueXPath;
	}

	/**
	 * If this mapping is lazy then it will only ever return field per record, so return <code>true</code>. If greedy then output cardinality is only
	 * fixed if {@link Mapping#minValueCount} equals {@link Mapping#maxValueCount} and they're both greater than zero (as zero indicates unbounded).
	 *
	 * @return true if this mapping always outputs the same number of fields per record regardless of input document.
	 */
	@Override
	public boolean hasFixedOutputCardinality() {
		boolean isFixed =
						(getMultiValueBehaviour() == MultiValueBehaviour.LAZY)
										|| ((getMaxValueCount() == getMinValueCount()) && (getMinValueCount() > 0));
		LOG.info("Mapping {} hasFixedOutputCardinality = {}", this, isFixed);
		return isFixed;
	}

	public boolean requiresTrimWhitespace() {
		return true;
	}

	@Override
	public void setHighestFoundValueCount(int valueFound) {
		this.highestFoundValueCount = Math.max(valueFound, this.highestFoundValueCount);
	}

	@Override
	public abstract String toString();

}
