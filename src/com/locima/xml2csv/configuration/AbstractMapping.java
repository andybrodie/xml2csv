package com.locima.xml2csv.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.output.GroupState;

public class AbstractMapping implements IMapping {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractMapping.class);

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
	 * The name of the field that will be created by this mapping.
	 */
	private String name;

	/**
	 * How to format the name of the mapping when outputting.
	 */
	private NameFormat nameFormat;

	/**
	 * The parent mapping container that this mapping is contained within.
	 */
	private IMappingContainer parent;

	public AbstractMapping() {
	}

	/**
	 * Creates a new immutable Field Definition.
	 *
	 * @param parent the parent of this mapping. May be null if this is a top level mapping container.
	 * @param baseName the name of this mapping, must be unique within the configuration.
	 * @param minValueCount the fewest number of values, or sets of values, that may be returned by this mapping.
	 * @param maxValueCount the most number of values, or sets of values, that may be returned by this mapping.
	 * @param nameFormat the format to be used for the {@link Mapping} instance that this method creates.
	 * @param groupNumber the group number for this field definition.
	 * @param multiValueBehaviour defines what should happen when multiple values are found for a single evaluation for this mapping.
	 */
	public AbstractMapping(IMappingContainer parent, String baseName, NameFormat nameFormat, int groupNumber, MultiValueBehaviour multiValueBehaviour,
					int minValueCount, int maxValueCount) {
		this.parent = parent;
		this.name = baseName;
		this.nameFormat = nameFormat == null ? NameFormat.NO_COUNTS : nameFormat;
		this.groupNumber = groupNumber;
		this.multiValueBehaviour = multiValueBehaviour;
		this.minValueCount = minValueCount;
		this.maxValueCount = maxValueCount;
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
		return this.multiValueBehaviour;
	}

	@Override
	public String getName() {
		return this.name;
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
		if (LOG.isInfoEnabled()) {
			LOG.info("Mapping {} hasFixedOutputCardinality = {}", this, isFixed);
		}
		return isFixed;
	}

	/**
	 * Sets the logical group number of this mapping container.
	 *
	 * @param groupNumber the logical group number of this mapping container.
	 */
	public void setGroupNumber(int groupNumber) {
		this.groupNumber = groupNumber;
	}

	@Override
	public void setHighestFoundValueCount(int valueCount) {
		this.highestFoundValueCount = Math.max(valueCount, this.highestFoundValueCount);
	}

	/**
	 * Sets the name given to this pivot mapping, if top-level will be used to generate the output file name.
	 *
	 * @param mappingName the name given to this pivot mapping, if top-level will be used to generate the output file name.
	 */
	public void setMappingName(String mappingName) {
		this.name = mappingName;
	}

	/**
	 * Sets what should happen when multiple values are found for a single evaluation of a single field wtihin this mapping.
	 *
	 * @param multiValueBehaviour defines what should happen when multiple values are found for a single evaluation of a single field wtihin this
	 *            mapping.
	 */
	public void setMultiValueBehaviour(MultiValueBehaviour multiValueBehaviour) {
		this.multiValueBehaviour = multiValueBehaviour;
	}

	/**
	 * Sets the format to be used for the {@link Mapping} instance that this method creates.
	 *
	 * @param nameFormat the format to be used for the {@link Mapping} instance that this method creates.
	 */
	public void setNameFormat(NameFormat nameFormat) {
		this.nameFormat = nameFormat;
	}

	/**
	 * Sets the logical group number of this mapping container.
	 *
	 * @param parent the logical group number of this mapping container.
	 */
	public void setParent(IMappingContainer parent) {
		this.parent = parent;
	}

}
