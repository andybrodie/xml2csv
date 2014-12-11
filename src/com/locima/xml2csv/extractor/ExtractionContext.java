package com.locima.xml2csv.extractor;

import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.ArgumentNullException;
import com.locima.xml2csv.BugException;
import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.configuration.IValueMapping;
import com.locima.xml2csv.output.ICsvWriter;

/**
 * An extraction context provides an object that understands how to evaluate individual mappings or containers of multiple mappings recursively, and
 * store the results of that evaluation such that output records can be generated.
 * <p>
 * There is a 1..1 relationship between an {@link IMapping} instance and an {@link ExtractionContext}. Therefore for each execution of an
 * {@link IMapping} that this context manages, either:
 * <ol>
 * <li>A new ordered list of values is created, in the case that this context is managing an {@link IValueMapping} (see
 * {@link MappingExtractionContext}), or</li>
 * <li>An ordered list of references to child contexts are created for {@link IMappingContainer} (see {@link ContainerExtractionContext}), each
 * references containing an index.</li>
 * </ol>
 * How these tree-structured sets of values are then flattened in to a CSV file is dependant on the configuration of the {@link IMapping} instance,
 * specifically the {@link IMapping#getMultiValueBehaviour()} result (see {@link ICsvWriter}.
 */
public abstract class ExtractionContext {

	private static final Logger LOG = LoggerFactory.getLogger(ExtractionContext.class);

	private int currentIndex;
	private ContainerExtractionContext parent;

	protected ExtractionContext(ContainerExtractionContext parent) {
		this.parent = parent;
	}

	public abstract void clearResults();

	public abstract void evaluate(XdmNode rootNode) throws DataExtractorException;

	public String getContextIndexString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.currentIndex);
		ExtractionContext current = getParent();
		while (current != null) {
			sb.insert(0, "_");
			sb.insert(0, current.currentIndex);
			current = current.getParent();
		}
		return sb.toString();
	}

	public abstract IMapping getMapping();

	public ContainerExtractionContext getParent() {
		return this.parent;
	}

	public void incrementContext() {
		this.currentIndex++;
		LOG.debug("Incrementing context of {} to {}", this, this.currentIndex);
	}

	public abstract void resetContext();

	public void resetForNewDoc() {
		resetContext();
		clearResults();
	}

	protected void setIndex(int newIndex) {
		this.currentIndex = newIndex;
	}

	public abstract int size();

	public static ExtractionContext create(ContainerExtractionContext parent, IMapping mapping, int index) {
		ExtractionContext ctx;
		if (mapping==null) throw new ArgumentNullException("mapping");
		if (mapping instanceof IValueMapping) {
			ctx = new MappingExtractionContext(parent, ((IValueMapping) mapping));
		} else if (mapping instanceof IMappingContainer) {
			ctx = new ContainerExtractionContext(parent, (IMappingContainer) mapping, index);
		} else {
			throw new BugException("Passed mapping that is not a value mapping or mapping container: %s", mapping);
		}
		return ctx;
	}

}
