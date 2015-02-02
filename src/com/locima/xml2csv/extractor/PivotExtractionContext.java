package com.locima.xml2csv.extractor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.BugException;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.configuration.Mapping;
import com.locima.xml2csv.configuration.PivotMapping;
import com.locima.xml2csv.configuration.XPathValue;
import com.locima.xml2csv.output.IExtractionResults;
import com.locima.xml2csv.output.IExtractionResultsContainer;
import com.locima.xml2csv.output.inline.CsiInputStream;
import com.locima.xml2csv.util.StringUtil;

public class PivotExtractionContext extends AbstractExtractionContext implements IExtractionResultsContainer {

	private static final Logger LOG = LoggerFactory.getLogger(PivotExtractionContext.class);

	private static final long serialVersionUID = 1L;
	private List<IExtractionResults> children;
	private transient PivotMapping mapping;

	public PivotExtractionContext(IExtractionResultsContainer parent, PivotMapping pivotMapping, int positionRelativeToOtherRootNodes,
					int positionRelativeToIMappingSiblings) {
		super(parent, positionRelativeToOtherRootNodes, positionRelativeToIMappingSiblings);
		this.mapping = pivotMapping;
	}

	/**
	 * Creates or re-uses an existing {@link MappingExtractionContext} that is dynamically created, along with its child {@link PivotKeyMapping} to
	 * hold the values extracted form the XML.
	 *
	 * @param baseName the name of the key. All values found for keys with the same name are added to the same MEC.
	 * @param keyCount
	 * @return either an existing MEC or a newly created one to manage mappings for the key specifid by <code>baseName</code>.
	 */
	private MappingExtractionContext ensureMec(Map<String, MappingExtractionContext> mecs, String baseName, int keyCount) {
		LOG.info("Creating new MEC for {}", baseName);
		Mapping keyMapping = this.mapping.getPivotKeyMapping(baseName);
		MappingExtractionContext mec = new MappingExtractionContext(this, keyMapping, 0, keyCount);
		this.children.add(mec);
		return mec;
	}

	/**
	 * Evaluate this pivot mapping but executing the value extracting XPath for every key found by executing the base name XPath.
	 *
	 * @param rootNode the context node from which to execute the key-finding XPath expression.
	 * @throws DataExtractorException if anything goes wrong finding the field definitions.
	 */
	@Override
	public void evaluate(XdmNode rootNode) throws DataExtractorException {
		this.children = new ArrayList<IExtractionResults>();
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
			if (rootCount == 0 && LOG.isDebugEnabled()) {
				LOG.debug("No results found for executing mapping root {} on {}", mappingRoot, this);
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
		getMapping().setHighestFoundValueCount(rootCount);
	}

	/**
	 * Evaluates a nested mapping, appending the results to the output line passed.
	 *
	 * @param node the node from which all mappings will be based on.
	 * @param positionRelativeToOtherRootNodes the position of this set of children relative to all the other roots found by this container's
	 *            evaluation of the mapping root.
	 * @throws DataExtractorException if an error occurred whilst extracting data (typically this would be caused by bad XPath, or XPath invalid from
	 *             the <code>mappingRoot</code> specified).
	 */
	private void evaluateChildren(XdmNode node, int positionRelativeToOtherRootNodes) throws DataExtractorException {
		XPathValue keyXPath = this.mapping.getKeyXPath();
		XPathValue rootXPath = this.mapping.getMappingRoot();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Executing keyXPath mapping {} for {}", keyXPath, this.mapping);
		}
		XPathSelector keyIterator = keyXPath.evaluate(node);
		int keyCount = 0;
		for (XdmItem keyItem : keyIterator) {
			String baseName = keyItem.getStringValue();
			if (StringUtil.isNullOrEmpty(baseName)) {
				LOG.debug("Found null key value at index {} for pivot mapping {} after executing {}.  Skipping.", keyCount, this, rootXPath);
				continue;
			}
			baseName = baseName.trim();
			LOG.debug("Found pivot mapping key {}", baseName);
			MappingExtractionContext mec = ensureMec(null, baseName, keyCount);
			mec.evaluate(node);
			keyCount++;
		}
		if (LOG.isInfoEnabled()) {
			if (keyCount == 0) {
				LOG.debug("Found no keys for pivot mapping {} after executing ", this, rootXPath);
			}
		}
	}

	@Override
	public List<List<IExtractionResults>> getChildren() {
		List<List<IExtractionResults>> childrenList = new ArrayList<List<IExtractionResults>>();
		childrenList.add(this.children);
		return childrenList;
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
	public List<IExtractionResults> getResultsSetAt(int index) {
		return index == 0 ? this.children : null;
	}

	/**
	 * Overridden to manage not writing {@link #mapping} to the output stream.
	 *
	 * @param rawInputStream target stream for serialized state.
	 * @throws IOException if any issues occur during writing.
	 * @throws ClassNotFoundException if an unexpected class instance appears in the CSI file, should never, ever happen.
	 */
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream rawInputStream) throws IOException, ClassNotFoundException {
		if (!(rawInputStream instanceof CsiInputStream)) {
			throw new BugException("Bug found when deserializing PEC.  I've been given a %s instead of a CsiInputStream", rawInputStream.getClass());
		}
		CsiInputStream stream = (CsiInputStream) rawInputStream;
		Object readObject = stream.readObject();
		try {
			this.children = (List<IExtractionResults>) readObject;
		} catch (ClassCastException cce) {
			throw new IOException("Unexpected object type found in stream.  Expected List<IExtractionResults> but got "
							+ readObject.getClass().getName());
		}
		readObject = stream.readObject();
		String mappingName;
		try {
			mappingName = (String) readObject;
		} catch (ClassCastException cce) {
			throw new IOException("Unexpected object type found in stream.  Expected String but got " + readObject.getClass().getName());
		}
		this.mapping = (PivotMapping) stream.getMappingContainer(mappingName);
		if (this.mapping == null) {
			throw new IOException("Unable to restore link to IMappingContainer " + mappingName + " as it was not found");
		}
	}

	@Override
	public int size() {
		return this.children.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("PEC(");
		sb.append(this.mapping);
		sb.append(", ");
		sb.append(getPositionRelativeToIMappingSiblings());
		sb.append(", ");
		sb.append(getPositionRelativeToOtherRootNodes());
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Overridden to manage not writing {@link #mapping} to the output stream.
	 *
	 * @param stream target stream for serialized state.
	 * @throws IOException if any issues occur during writing.
	 */
	private void writeObject(ObjectOutputStream stream) throws IOException {
		String mappingName = getMapping().getContainerName();
		int size = this.children.size();
		if (LOG.isInfoEnabled()) {
			LOG.info("Writing {} results to the CSI file for {}", size, mappingName);
		}
		stream.writeObject(this.children);
		stream.writeObject(mappingName);
	}

}
