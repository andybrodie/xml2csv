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
import com.locima.xml2csv.configuration.IValueMapping;
import com.locima.xml2csv.configuration.XPathValue;

public class ContainerExtractionContext extends ExtractionContext {

	private static final Logger LOG = LoggerFactory.getLogger(ContainerExtractionContext.class);

	private List<ContainerExtractionContext> children;

	private IMappingContainer mapping;

	public ContainerExtractionContext(ContainerExtractionContext parent, IMappingContainer mapping) {
		super(parent);
		this.mapping = mapping;
		this.children = new ArrayList<ContainerExtractionContext>();
	}

	public ContainerExtractionContext(IMappingContainer mapping) {
		this(null, mapping);
	}

	// public void addContext(int x) {
	// this.indices.push(x);
	// if (LOG.isDebugEnabled()) {
	// LOG.debug("Added context {}", toContextString());
	// }
	// }
	//
	// public void increment() {
	// String before = null;
	// if (LOG.isDebugEnabled()) {
	// before = toContextString();
	// }
	// this.indices.push(this.indices.pop() + 1);
	// if (LOG.isDebugEnabled()) {
	// LOG.debug("Incremented {} to {}", before, toContextString());
	// }
	//
	// }
	//
	// public void removeContext() {
	// this.indices.pop();
	// if (LOG.isDebugEnabled()) {
	// LOG.debug("Popped context {}", toContextString());
	// }
	// }
	//
	// public String toContextString() {
	// StringBuilder sb = new StringBuilder();
	// Iterator<Integer> iter = this.indices.descendingIterator();
	// while (iter.hasNext()) {
	// sb.append(iter.next());
	// if (iter.hasNext()) {
	// sb.append('_');
	// }
	// }
	// return sb.toString();
	// }

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
	public ExtractedRecordList evaluate(XdmNode rootNode) throws DataExtractorException {
		ExtractedRecordList rs = new ExtractedRecordList();
		XPathValue mappingRoot = this.mapping.getMappingRoot();
		if (mappingRoot != null) {
			XPathSelector rootIterator = mappingRoot.evaluate(rootNode);
			for (XdmItem item : rootIterator) {
				if (item instanceof XdmNode) {
					// All evaluations have to be done in terms of nodes, so if the XPath returns something like a value then ignore it and warn.
					evaluateChildren((XdmNode) item, rs);
				} else {
					LOG.warn("Expected to find only elements after executing XPath on mapping list, got {}", item.getClass().getName());
				}
			}
		} else {
			// If there is no root specified by the contextual context, then use "." , or current node.
			evaluateChildren(rootNode, rs);
		}

		if (LOG.isTraceEnabled()) {
			LOG.trace("Completed mapping container {} against document", this);
		}
		return rs;
	}

	/**
	 * Evaluates a nested mapping, appending the results to the output line passed.
	 *
	 * @param node the node from which all mappings will be based on.
	 * @param outputLine the existing output line that will be appended to.
	 * @param trimWhitespace if true, then leading and trailing whitespace will be removed from all data values.
	 * @throws DataExtractorException if an error occurred whilst extracting data (typically this would be caused by bad XPath, or XPath invalid from
	 *             the <code>mappingRoot</code> specified).
	 */
	private void evaluateChildren(XdmNode node, ExtractedRecordList outputLine) throws DataExtractorException {
		for (IMapping mapping : this.mapping) {
			ExtractionContext childCtx;
			if (mapping instanceof IMappingContainer) {
				childCtx = new ContainerExtractionContext(this, (IMappingContainer) mapping);
			} else {
				childCtx = new MappingExtractionContext(this, (IValueMapping) mapping);
			}
			ExtractedRecordList records = childCtx.evaluate(node);
			outputLine.addAll(records);
		}
	}

}
