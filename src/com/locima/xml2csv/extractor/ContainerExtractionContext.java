package com.locima.xml2csv.extractor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.BugException;
import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.configuration.XPathValue;
import com.locima.xml2csv.util.StringUtil;

public class ContainerExtractionContext extends ExtractionContext implements Iterable<List<ExtractedField>> {

	private static final Logger LOG = LoggerFactory.getLogger(ContainerExtractionContext.class);

	private List<ExtractionContext> children;

	private int index;

	private IMappingContainer mapping;

	public ContainerExtractionContext(ContainerExtractionContext parent, IMappingContainer mapping, int index) {
		super(parent);
		this.mapping = mapping;
		this.children = new ArrayList<ExtractionContext>();
		this.index = index;
	}

	public ContainerExtractionContext(IMappingContainer mapping, int index) {
		this(null, mapping, index);
	}

	@Override
	public void clearResults() {
		throw new BugException("Not implemented");
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
		if (mappingRoot != null) {
			LOG.debug("Executing mappingRoot {} for {}", mappingRoot, this.mapping);
			XPathSelector rootIterator = mappingRoot.evaluate(rootNode);
			for (XdmItem item : rootIterator) {
				if (item instanceof XdmNode) {
					// All evaluations have to be done in terms of nodes, so if the XPath returns something like a value then warn and move on.
					evaluateChildren((XdmNode) item);
				} else {
					LOG.warn("Expected to find only elements after executing XPath on mapping list, got {}", item.getClass().getName());
				}
			}
		} else {
			// If there is no root specified by the contextual context, then use "." , or current node passed as rootNode parameter.
			LOG.debug("No mapping root specified for {}, so executing against passed context node", mappingRoot, this.mapping);
			evaluateChildren(rootNode);
		}

		if (LOG.isTraceEnabled()) {
			LOG.trace("START RESULTS OUTPUT after completed mapping container {} against document", this);
			logResults(this, 0, 0);
			LOG.trace("END RESULTS OUTPUT");
		}
	}

	/**
	 * Debugging method to log all the results when an {@link #evaluate(XdmNode)} call has completed.
	 */
	private void logResults(ExtractionContext ctx, int offset, int indentCount) {
		StringBuilder indentSb = new StringBuilder();
		for (int i = 0; i < indentCount; i++) {
			indentSb.append("  ");
		}
		String indent = indentSb.toString();
		if (ctx instanceof ContainerExtractionContext) {
			LOG.trace("{}{}:{}", indent, offset, this);
			int childCount = 0;
			for (ExtractionContext child : ((ContainerExtractionContext) ctx).getChildren()) {
				logResults(child, childCount++, indentCount+1);
			}
		} else {
			MappingExtractionContext mCtx = (MappingExtractionContext) ctx;
			LOG.trace("{}{}:{}({})", indent, offset, mCtx, StringUtil.collectionToString(mCtx.getAllValues(),",",null));
		}
	}

	/**
	 * Evaluates a nested mapping, appending the results to the output line passed.
	 *
	 * @param node the node from which all mappings will be based on.
	 * @param trimWhitespace if true, then leading and trailing whitespace will be removed from all data values.
	 * @throws DataExtractorException if an error occurred whilst extracting data (typically this would be caused by bad XPath, or XPath invalid from
	 *             the <code>mappingRoot</code> specified).
	 */
	private void evaluateChildren(XdmNode node) throws DataExtractorException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Executing {} child mappings of {}", this.mapping.size(), this.mapping);
		}
		int i = 0;
		for (IMapping mapping : this.mapping) {
			ExtractionContext childCtx = ExtractionContext.create(this, mapping, i);
			childCtx.evaluate(node);
			this.children.add(childCtx);
			i++;
		}
		incrementContext();
	}

	public ExtractionContext getChildAt(int valueIndex) {
		if (this.children.size() > valueIndex) {
			return this.children.get(valueIndex);
		} else {
			return null;
		}
	}

	public List<ExtractionContext> getChildren() {
		return this.children;
	}

	public int getIndex() {
		return this.index;
	}

	@Override
	public IMappingContainer getMapping() {
		return this.mapping;
	}

	@Override
	public Iterator<List<ExtractedField>> iterator() {
		return new RecordSetCsvIterator(this);
	}

	/**
	 * Resets our current context and the contexts of all the child contexts of this one.
	 */
	@Override
	public void resetContext() {
		setIndex(0);
		for (ExtractionContext child : this.children) {
			child.resetContext();
		}
	}

	@Override
	public int size() {
		return this.children.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("CEC(");
		sb.append(this.mapping);
		sb.append(", ");
		sb.append(this.index);
		sb.append(")");
		return sb.toString();
	}
}
