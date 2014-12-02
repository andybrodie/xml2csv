package com.locima.xml2csv.configuration;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.util.EqualsUtil;

/**
 * Represents a single column to XPath mapping.
 */
public class Mapping extends AbstractMapping implements IValueMapping {

	private static final Logger LOG = LoggerFactory.getLogger(Mapping.class);

	/**
	 * The baseName of the field that will be created by this mapping.
	 */
	private String baseName;

	private int maxResultsFound;

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
	 * Creates a new immutable Field Definition.
	 *
	 * @param parent the parent mapping container.
	 * @param baseName the outputName of the field, must a string of length > 0.
	 * @param valueXPath a compiled XPath expression that will extract the values required for this field.
	 * @param format the format to be used for the {@link Mapping} instance that this method creates.
	 * @param groupNumber the group number for this field definition.
	 * @param multiValueBehaviour defines what should happen when multiple values are found for a single evaluation for this mapping.
	 */
	public Mapping(IMappingContainer parent, String baseName, NameFormat format, int groupNumber, MultiValueBehaviour multiValueBehaviour,
					XPathValue valueXPath, int minValueCount, int maxValueCount) {
		super(parent, format, groupNumber, multiValueBehaviour, valueXPath);
		this.baseName = baseName;
		this.minValueCount = minValueCount;
		this.maxValueCount = maxValueCount;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof Mapping) {
			Mapping that = (Mapping) obj;
			return EqualsUtil.areEqual(this.baseName, that.baseName) && EqualsUtil.areEqual(this.minValueCount, that.minValueCount)
							&& EqualsUtil.areEqual(this.maxValueCount, that.minValueCount) && super.equals(that);
		} else {
			return false;
		}
	}

	@Override
	public String getBaseName() {
		return this.baseName;
	}

	@Override
	public int getFieldNames(List<String> fieldNames, String parentName, int parentIterationNumber) {
		/*
		 * The number of fields output is the maximum number of values found in a single execution of this mapping, constrained by this.minValueCount
		 * and this.maxValueCount.
		 */
		int numNames = Math.max(this.maxResultsFound, this.minValueCount);
		if (this.maxValueCount > 0) {
			numNames = Math.min(this.maxValueCount, numNames);
		}
		int fieldCount = 0;
		switch (getMultiValueBehaviour()) {
			case LAZY:
				fieldNames.add(getNameFormat().format(this.baseName, 0, parentName, parentIterationNumber));
				fieldCount++;
				break;
			case GREEDY:
				for (; fieldCount < numNames; fieldCount++) {
					fieldNames.add(getNameFormat().format(this.baseName, fieldCount, parentName, parentIterationNumber));
				}
				break;
			default:
				throw new IllegalStateException("Unexpected MultiValueBehaviour: " + getMultiValueBehaviour());
		}
		return fieldCount;
	}

	@Override
	public int getMaxValueCount() {
		return this.maxValueCount;
	}

	@Override
	public int getMinValueCount() {
		return this.minValueCount;
	}

	/**
	 * If multi-record then this mapping will always return one value, to return <code>true</code>. If inline then output cardinality is only fixed if
	 * {@link Mapping#minValueCount} equals {@link Mapping#maxValueCount} and they're both greater than zero (zero indicates unbounded).
	 */
	@Override
	public boolean hasFixedOutputCardinality() {
		boolean isFixed =
						(getMultiValueBehaviour() == MultiValueBehaviour.LAZY)
										|| ((this.maxValueCount == this.minValueCount) && (this.minValueCount > 0));
		LOG.info("Mapping {} hasFixedOutputCardinality = {}", this, isFixed);
		return isFixed;
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
	public boolean requiresTrimWhitespace() {
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Mapping(");
		sb.append(this.baseName);
		sb.append(',');
		sb.append(getNameFormat());
		sb.append(',');
		sb.append(getGroupNumber());
		sb.append(',');
		sb.append(getMultiValueBehaviour());
		sb.append(',');
		sb.append(getValueXPath().getSource());
		sb.append(',');
		sb.append(this.minValueCount);
		sb.append(',');
		sb.append(this.maxValueCount);
		sb.append(')');
		return sb.toString();
	}

}
