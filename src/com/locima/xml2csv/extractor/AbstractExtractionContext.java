package com.locima.xml2csv.extractor;

import com.locima.xml2csv.ArgumentNullException;
import com.locima.xml2csv.BugException;
import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.configuration.IValueMapping;
import com.locima.xml2csv.configuration.MappingList;
import com.locima.xml2csv.configuration.MultiValueBehaviour;
import com.locima.xml2csv.configuration.PivotMapping;
import com.locima.xml2csv.output.IExtractionResultsContainer;

/**
 * Common implementation between {@link MappingExtractionContext} and {@link ContainerExtractionContext}.
 */
public abstract class AbstractExtractionContext implements IExtractionContext {

	/**
	 * First version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Factory method to create the right type of {@link AbstractExtractionContext} (either {@link MappingExtractionContext} or
	 * {@link ContainerExtractionContext}) based on the sub-type of the <code>mapping</code> parameter.
	 * <p>
	 * I've done it this way so an {@link AbstractExtractionContext} instance can be easily created using an {@link IMapping} instance (i.e. the type
	 * checking is done here).
	 *
	 * @param parent the parent context (in the same way that an {@link IMapping} has a parent).
	 * @param mapping the mapping that the new context will be managing.
	 * @param positionRelativeToOtherRootNodes the index of the new context, with respect to its siblings (first child of the parent has index 0,
	 *            second has index 1, etc.).
	 * @param positionRelativeToIMappingSiblings The position of this extraction context with respect to its sibling {@link IMapping} instances
	 *            beneath the parent.
	 * @return either a {@link MappingExtractionContext} or {@link ContainerExtractionContext} instance. Never null.
	 */
	public static IExtractionContext create(IExtractionResultsContainer parent, IMapping mapping, int positionRelativeToOtherRootNodes,
					int positionRelativeToIMappingSiblings) {
		IExtractionContext ctx;
		if (mapping == null) {
			throw new ArgumentNullException("mapping");
		}
		if (mapping instanceof IValueMapping) {
			ctx = new MappingExtractionContext(parent, (IValueMapping) mapping, positionRelativeToOtherRootNodes, positionRelativeToIMappingSiblings);
		} else if (mapping instanceof MappingList) {
			ctx =
							new ContainerExtractionContext(parent, (IMappingContainer) mapping, positionRelativeToOtherRootNodes,
											positionRelativeToIMappingSiblings);
		} else if (mapping instanceof PivotMapping) {
			ctx = new PivotExtractionContext(parent, (PivotMapping) mapping, positionRelativeToOtherRootNodes, positionRelativeToIMappingSiblings);
		} else {
			throw new BugException("Passed mapping that is not a value mapping or mapping container: %s", mapping);
		}
		return ctx;
	}

	/**
	 * The position of this extraction context with respect to its sibling {@link IMapping} instances beneath the parent.
	 */
	private int positionRelativeToIMappingSiblings;

	/**
	 * The position of this extraction context with respect to the other root nodes found when evaluating the parent.
	 * {@link IMappingContainer#getMappingRoot()} values.
	 */
	private int positionRelativeToOtherRootNodes;

	/**
	 * Default no-arg constructor required for serialization.
	 */
	public AbstractExtractionContext() {
	}

	/**
	 * Initialises instance variables based on parameters.
	 *
	 * @param parent the parent context (may be null if based the top level {@link IMappingContainer}.
	 * @param positionRelativeToOtherRootNodes the position of this extraction context with respect to the other root nodes found when evaluating the
	 *            parent.
	 * @param positionRelativeToIMappingSiblings the position of this extraction context with respect to its sibling {@link IMapping} instances
	 *            beneath the parent.
	 */
	protected AbstractExtractionContext(IExtractionResultsContainer parent, int positionRelativeToOtherRootNodes,
					int positionRelativeToIMappingSiblings) {
		this.positionRelativeToIMappingSiblings = positionRelativeToIMappingSiblings;
		this.positionRelativeToOtherRootNodes = positionRelativeToOtherRootNodes;
	}

	@Override
	public int getGroupNumber() {
		return getMapping().getGroupNumber();
	}

	@Override
	public int getMinCount() {
		return getMapping().getMinValueCount();
	}

	@Override
	public MultiValueBehaviour getMultiValueBehaviour() {
		return getMapping().getMultiValueBehaviour();
	}

	@Override
	public int getPositionRelativeToIMappingSiblings() {
		return this.positionRelativeToIMappingSiblings;
	}

	@Override
	public int getPositionRelativeToOtherRootNodes() {
		return this.positionRelativeToOtherRootNodes;
	}

}
