package com.locima.xml2csv.extractor;

import java.io.Serializable;

import net.sf.saxon.s9api.XdmNode;

import com.locima.xml2csv.ArgumentNullException;
import com.locima.xml2csv.BugException;
import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.configuration.IValueMapping;
import com.locima.xml2csv.configuration.MultiValueBehaviour;
import com.locima.xml2csv.output.IExtractionResults;
import com.locima.xml2csv.output.direct.DirectOutputRecordIterator;

/**
 * An extraction context provides an object that understands how to evaluate individual mappings or containers of multiple mappings recursively, and
 * store the results of that evaluation such that output records can be generated.
 * <p>
 * There is a 1..n relationship between an {@link IMapping} instance and an {@link AbstractExtractionContext}. Each time an XML root node is found
 * from which the {@link IMapping} instance requires evaulation, a corresponding {@link AbstractExtractionContext} is created to manage the execution
 * and store the results. Therefore for each execution of an {@link IMapping} that this context manages, either:
 * <ol>
 * <li>A new ordered list of values is created, in the case that this context is managing an {@link IValueMapping} (see
 * {@link MappingExtractionContext}), or</li>
 * <li>An ordered list of references to child contexts are created for {@link IMappingContainer} (see {@link ContainerExtractionContext}), each
 * reference containing an index.</li>
 * </ol>
 * How these tree-structured sets of values are then flattened in to a CSV file is performed in {@link DirectOutputRecordIterator} and dependent on
 * the configuration of the {@link IMapping} instance, specifically the {@link IMapping#getMultiValueBehaviour()} value.
 */
public abstract class AbstractExtractionContext implements IExtractionResults, Serializable {

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
	public static AbstractExtractionContext create(ContainerExtractionContext parent, IMapping mapping, int positionRelativeToOtherRootNodes,
					int positionRelativeToIMappingSiblings) {
		AbstractExtractionContext ctx;
		if (mapping == null) {
			throw new ArgumentNullException("mapping");
		}
		if (mapping instanceof IValueMapping) {
			ctx =
							new MappingExtractionContext(parent, ((IValueMapping) mapping), positionRelativeToOtherRootNodes,
											positionRelativeToIMappingSiblings);
		} else if (mapping instanceof IMappingContainer) {
			ctx =
							new ContainerExtractionContext(parent, (IMappingContainer) mapping, positionRelativeToOtherRootNodes,
											positionRelativeToIMappingSiblings);
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
	protected AbstractExtractionContext(ContainerExtractionContext parent, int positionRelativeToOtherRootNodes,
					int positionRelativeToIMappingSiblings) {
		this.positionRelativeToIMappingSiblings = positionRelativeToIMappingSiblings;
		this.positionRelativeToOtherRootNodes = positionRelativeToOtherRootNodes;
	}

	/**
	 * Evaluates this context against the passed XML node to generate results.
	 *
	 * @param rootNode the node to execute the mapping against.
	 * @throws DataExtractorException if any errors occur during data extraction.
	 */
	public abstract void evaluate(XdmNode rootNode) throws DataExtractorException;

	@Override
	public int getGroupNumber() {
		return getMapping().getGroupNumber();
	}

	/**
	 * Retrieve the mapping that this context is going to execute.
	 *
	 * @return never returns null.
	 */
	public abstract IMapping getMapping();

	// @Override
	// public int getFieldsRequiredInRecord() {
	// IMapping mapping = getMapping();
	// return mapping.getFieldCountForSingleRecord() - mapping.getHighestFoundValueCount();
	// }

	@Override
	public int getMinCount() {
		return getMapping().getMinValueCount();
	}

	@Override
	public MultiValueBehaviour getMultiValueBehaviour() {
		return getMapping().getMultiValueBehaviour();
	}

	/**
	 * Retrieves the name of the mapping (either {@link IMappingContainer#getContainerName()} or {@link IValueMapping#getBaseName()}.
	 *
	 * @return the name of the mapping, never null or a zero length string.
	 */
	public abstract String getName();

	/**
	 * Retrieves the position of this extraction context with respect to its sibling {@link IMapping} instances beneath the parent as set on the
	 * constructor.
	 *
	 * @return the position of this extraction context with respect to its sibling {@link IMapping} instances beneath the parent as set on the
	 *         constructor.
	 */
	public int getPositionRelativeToIMappingSiblings() {
		return this.positionRelativeToIMappingSiblings;
	}

	/**
	 * Retrieves the position of this extraction context with respect to the other root nodes found when evaluating the parent as set on the
	 * constructor.
	 *
	 * @return the position of this extraction context with respect to the other root nodes found when evaluating the parent.
	 */
	public int getPositionRelativeToOtherRootNodes() {
		return this.positionRelativeToOtherRootNodes;
	}

	/**
	 * Returns the number of results found by this context.
	 *
	 * @return a natural number.
	 */
	@Override
	public abstract int size();

}
