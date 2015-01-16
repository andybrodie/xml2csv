package com.locima.xml2csv.extractor;

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.configuration.XPathValue;
import com.locima.xml2csv.output.IExtractionResults;
import com.locima.xml2csv.output.IExtractionResultsContainer;
import com.locima.xml2csv.util.StringUtil;

/**
 * Used to manage the evaluation and storage of results of an {@link IMappingContainer} instance.
 */
public class ContainerExtractionContext extends ExtractionContext implements IExtractionResultsContainer {

	private static final Logger LOG = LoggerFactory.getLogger(ContainerExtractionContext.class);

	/**
	 * A list of all the child contexts (also a list) found as a result of evaluating the {@link ContainerExtractionContext#mapping}'s
	 * {@link IMappingContainer#getMappingRoot()} query.
	 */
	private List<List<IExtractionResults>> children;

	/**
	 * The mapping that this extraction context is representing the evaluation of.
	 */
	private IMappingContainer mapping;

	public ContainerExtractionContext(ContainerExtractionContext parent, IMappingContainer mapping, int positionRelativeToOtherRootNodes,
					int positionRelativeToIMappingSiblings) {
		super(parent, positionRelativeToOtherRootNodes, positionRelativeToIMappingSiblings);
		this.mapping = mapping;
		this.children = new ArrayList<List<IExtractionResults>>();
	}

	public ContainerExtractionContext(IMappingContainer mapping, int positionRelativeToOtherRootNodes, int positionRelativeToIMappingSiblings) {
		this(null, mapping, positionRelativeToOtherRootNodes, positionRelativeToIMappingSiblings);
	}

	/**
	 * Execute this mapping for the passed XML document by:
	 * <ol>
	 * <li>Getting the mapping root(s) of the mapping, relative to the rootNode passed.</li>
	 * <li>If there isn't a mapping root, use the root node passed.</li>
	 * <li>Execute this mapping for each of the root(s).</li>
	 * <li>Each execution results in a single call to om (one CSV line).</li>
	 * </ol>
	 */
	@Override
	public void evaluate(XdmNode rootNode) throws DataExtractorException {
		XPathValue mappingRoot = this.mapping.getMappingRoot();
		// If there's no mapping root expression, use the passed node as a single root
		int rootCount = 0;
		if (mappingRoot != null) {
			LOG.debug("Executing mappingRoot {} for {}", mappingRoot, this.mapping);
			XPathSelector rootIterator = mappingRoot.evaluate(rootNode);
			for (XdmItem item : rootIterator) {
				if (item instanceof XdmNode) {
					// All evaluations have to be done in terms of nodes, so if the XPath returns something like a value then warn and move on.
					evaluateChildren((XdmNode) item, rootCount);
				} else {
					LOG.warn("Expected to find only elements after executing XPath on mapping list, got {}", item.getClass().getName());
				}
				rootCount++;
			}
		} else {
			// If there is no root specified by the contextual context, then use "." , or current node passed as rootNode parameter.
			if (LOG.isDebugEnabled()) {
				LOG.debug("No mapping root specified for {}, so executing against passed context node", mappingRoot, this.mapping);
			}
			evaluateChildren(rootNode, rootCount);
			rootCount = 1;
		}

		// Keep track of the most number of results we've found for a single invocation of the mapping root.
		this.mapping.setHighestFoundValueCount(rootCount);

		if (LOG.isTraceEnabled()) {
			LOG.trace("START RESULTS OUTPUT after completed mapping container {} against document", this);
			logResults(this, 0, 0);
			LOG.trace("END RESULTS OUTPUT");
		}
	}

	/**
	 * Evaluates a nested mapping, appending the results to the output line passed.
	 *
	 * @param node the node from which all mappings will be based on.
	 * @param positionRelativeToOtherRootNodes the position of this set of children relative to all the other roots found by this container's
	 *            evaluation of the mapping root.
	 * @param trimWhitespace if true, then leading and trailing whitespace will be removed from all data values.
	 * @throws DataExtractorException if an error occurred whilst extracting data (typically this would be caused by bad XPath, or XPath invalid from
	 *             the <code>mappingRoot</code> specified).
	 */
	private void evaluateChildren(XdmNode node, int positionRelativeToOtherRootNodes) throws DataExtractorException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Executing {} child mappings of {}", this.mapping.size(), this.mapping);
		}
		int positionRelativeToIMappingSiblings = 0;
		List<IExtractionResults> iterationECs = new ArrayList<IExtractionResults>(size());
		for (IMapping mapping : this.mapping) {
			ExtractionContext childCtx =
							ExtractionContext.create(this, mapping, positionRelativeToOtherRootNodes, positionRelativeToIMappingSiblings);
			childCtx.evaluate(node);
			iterationECs.add(childCtx);
			positionRelativeToIMappingSiblings++;
		}
		this.children.add(iterationECs);
	}

	@Override
	public List<List<IExtractionResults>> getChildren() {
		return this.children;
	}

	@Override
	public List<String> getEmptyFieldNames(int containerIterationCount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMappingContainer getMapping() {
		return this.mapping;
	}

	@Override
	public String getName() {
		return this.mapping.getContainerName();
	}

	@Override
	public List<IExtractionResults> getResultsSetAt(int valueIndex) {
		if (this.children.size() > valueIndex) {
			return this.children.get(valueIndex);
		} else {
			return null;
		}
	}

	/**
	 * Debugging method to log all the results when an {@link #evaluate(XdmNode)} call has completed.
	 */
	public void logResults(IExtractionResults ctx, int offset, int indentCount) {
		StringBuilder indentSb = new StringBuilder();
		for (int i = 0; i < indentCount; i++) {
			indentSb.append("  ");
		}
		String indent = indentSb.toString();
		if (ctx instanceof ContainerExtractionContext) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("{}{}:{}", indent, offset, this);
			}
			int childResultsSetCount = 0;
			int childCount = 0;
			for (List<IExtractionResults> children : ((IExtractionResultsContainer) ctx).getChildren()) {
				LOG.trace("{}  {}", indent, childResultsSetCount++);
				for (IExtractionResults child : children) {
					logResults(child, childCount++, indentCount + 2);
				}
				childCount = 0;
			}
		} else {
			MappingExtractionContext mCtx = (MappingExtractionContext) ctx;
			if (LOG.isTraceEnabled()) {
				LOG.trace("{}{}:{}({})", indent, offset, mCtx, StringUtil.collectionToString(mCtx.getAllValues(), ",", null));
			}
		}
	}

	/**
	 * Returns the number of mapping roots found for this object to evaluate against.
	 */
	@Override
	public int size() {
		return this.children.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("CEC(");
		sb.append(this.mapping);
		sb.append(", ");
		sb.append(getPositionRelativeToIMappingSiblings());
		sb.append(", ");
		sb.append(getPositionRelativeToOtherRootNodes());
		sb.append(")");
		return sb.toString();
	}
}
