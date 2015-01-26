package com.locima.xml2csv.extractor;

import java.io.Serializable;

import net.sf.saxon.s9api.XdmNode;

import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.configuration.IValueMapping;
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
public interface IExtractionContext extends IExtractionResults, Serializable {

	/**
	 * Evaluates this context against the passed XML node to generate results.
	 *
	 * @param rootNode the node to execute the mapping against.
	 * @throws DataExtractorException if any errors occur during data extraction.
	 */
	void evaluate(XdmNode rootNode) throws DataExtractorException;

	/**
	 * Retrieve the mapping that this context is going to execute.
	 *
	 * @return never returns null.
	 */
	IMapping getMapping();

	/**
	 * Retrieves the name of the mapping (either {@link IMappingContainer#getContainerName()} or {@link IValueMapping#getBaseName()}.
	 *
	 * @return the name of the mapping, never null or a zero length string.
	 */
	String getName();

	/**
	 * Retrieves the position of this extraction context with respect to its sibling {@link IMapping} instances beneath the parent as set on the
	 * constructor.
	 *
	 * @return the position of this extraction context with respect to its sibling {@link IMapping} instances beneath the parent as set on the
	 *         constructor.
	 */
	int getPositionRelativeToIMappingSiblings();

	/**
	 * Retrieves the position of this extraction context with respect to the other root nodes found when evaluating the parent as set on the
	 * constructor.
	 *
	 * @return the position of this extraction context with respect to the other root nodes found when evaluating the parent.
	 */
	int getPositionRelativeToOtherRootNodes();

	/**
	 * Returns the number of results found by this context.
	 *
	 * @return a natural number.
	 */
	@Override
	int size();

}