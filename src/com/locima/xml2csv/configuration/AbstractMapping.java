package com.locima.xml2csv.configuration;

import com.locima.xml2csv.extractor.GroupState;
import com.locima.xml2csv.util.EqualsUtil;

public abstract class AbstractMapping implements IMapping {

	/**
	 * To understand how groups work, see {@link GroupState}.
	 */
	private int groupNumber;

	private int highestFoundValueCount;

	/**
	 * Tracks the number of instances found at once by this mapping. This is needed when doing inline mappings.
	 */
	private int maxInstanceCount;

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
	 * @param baseName the outputName of the field, must a string of length > 0.
	 * @param valueXPath a compiled XPath expression that will extract the values required for this field.
	 * @param format the format to be used for the {@link Mapping} instance that this method creates.
	 * @param groupNumber the group number for this field definition.
	 * @param multiValueBehaviour defines what should happen when multiple values are found for a single evaluation for this mapping.
	 */
	public AbstractMapping(IMappingContainer parent, NameFormat format, int groupNumber, MultiValueBehaviour multiValueBehaviour,
					XPathValue valueXPath) {
		this.parent = parent;
		this.groupNumber = groupNumber;
		this.multiValueBehaviour = multiValueBehaviour;
		this.nameFormat = format == null ? NameFormat.NO_COUNTS : format;
		this.valueXPath = valueXPath;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof AbstractMapping) {
			AbstractMapping that = (AbstractMapping) obj;
			return EqualsUtil.areEqual(this.nameFormat, that.nameFormat) && EqualsUtil.areEqual(this.groupNumber, that.groupNumber)
							&& EqualsUtil.areEqual(this.multiValueBehaviour, that.multiValueBehaviour)
							&& EqualsUtil.areEqual(this.valueXPath, that.valueXPath);
		} else {
			return false;
		}
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

	public XPathValue getValueXPath() {
		return this.valueXPath;
	}

	@Override
	public abstract int hashCode();

	@Override
	public void setHighestFoundValueCount(int valueFound) {
		this.highestFoundValueCount = Math.max(valueFound, this.highestFoundValueCount);
	}

	@Override
	public abstract String toString();
	
}
