package com.locima.xml2csv.extractor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import com.locima.xml2csv.configuration.Mapping;
import com.locima.xml2csv.configuration.PivotMapping;
import com.locima.xml2csv.configuration.XPathValue;
import com.locima.xml2csv.output.IExtractionResults;
import com.locima.xml2csv.output.IExtractionResultsContainer;
import com.locima.xml2csv.output.inline.CsiInputStream;
import com.locima.xml2csv.util.StringUtil;

/**
 * Used to manage the evaluation and storage of results of an {@link PivotMapping} instance.
 */
public class PivotExtractionContext extends AbstractExtractionContext implements IExtractionResultsContainer {

	private static final Logger LOG = LoggerFactory.getLogger(PivotExtractionContext.class);

	private static final long serialVersionUID = 1L;
	private List<List<IExtractionResults>> children;
	private transient PivotMapping mapping;

	/**
	 * Constructs a new instance to manage the evaluation of the <code>mapping</code> passed.
	 *
	 * @param pivotMapping the mapping configuration that this context is responsible for evaluating.
	 * @param parent the parent for this context (should be null if this is a top-level mapping on the configuration).
	 * @param positionRelativeToOtherRootNodes the index of the new context, with respect to its siblings (first child of the parent has index 0,
	 *            second has index 1, etc.).
	 * @param positionRelativeToIMappingSiblings The position of this extraction context with respect to its sibling {@link IMapping} instances
	 *            beneath the parent.
	 */
	public PivotExtractionContext(IExtractionResultsContainer parent, PivotMapping pivotMapping, int positionRelativeToOtherRootNodes,
					int positionRelativeToIMappingSiblings) {
		super(parent, positionRelativeToOtherRootNodes, positionRelativeToIMappingSiblings);
		this.mapping = pivotMapping;
	}

	/**
	 * Constructs a new instance to manage the evaluation of the <code>mapping</code> passed with no parent.
	 *
	 * @param pivotMapping the mapping configuration that this context is responsible for evaluating.
	 * @param positionRelativeToOtherRootNodes the index of the new context, with respect to its siblings (first child of the parent has index 0,
	 *            second has index 1, etc.).
	 * @param positionRelativeToIMappingSiblings The position of this extraction context with respect to its sibling {@link IMapping} instances
	 *            beneath the parent.
	 */
	public PivotExtractionContext(PivotMapping pivotMapping, int positionRelativeToOtherRootNodes, int positionRelativeToIMappingSiblings) {
		this(null, pivotMapping, positionRelativeToOtherRootNodes, positionRelativeToIMappingSiblings);
	}

	/**
	 * Creates or re-uses an existing {@link MappingExtractionContext} that is dynamically created, along with its child {@link PivotKeyMapping} to
	 * hold the values extracted form the XML.
	 *
	 * @param baseName the name of the key. All values found for keys with the same name are added to the same MEC.
	 * @param positionRelativeToOtherRootNodes the index of the new context, with respect to its siblings (first child of the parent has index 0,
	 *            second has index 1, etc.).
	 * @param positionRelativeToIMappingSiblings The position of this extraction context with respect to its sibling {@link IMapping} instances
	 *            beneath the parent.
	 * @return either an existing MEC or a newly created one to manage mappings for the key specifid by <code>baseName</code>.
	 */
	private MappingExtractionContext ensureMec(String baseName, int positionRelativeToOtherRootNodes, int positionRelativeToIMappingSiblings) {
		LOG.info("Creating new MEC for {}", baseName);
		Mapping keyMapping = this.mapping.getPivotKeyMapping(baseName);
		MappingExtractionContext mec =
						new MappingExtractionContext(this, keyMapping, positionRelativeToOtherRootNodes, positionRelativeToIMappingSiblings);
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
		this.children = new ArrayList<List<IExtractionResults>>();
		XPathValue mappingRoot = this.mapping.getMappingRoot();
		// If there's no mapping root expression, use the passed node as a single root
		int rootCount = 0;
		if (mappingRoot != null) {
			LOG.debug("Executing mappingRoot {} for {}", mappingRoot, this.mapping);
			XPathSelector rootIterator = mappingRoot.evaluate(rootNode);
			for (XdmItem item : rootIterator) {
				if (item instanceof XdmNode) {
					// All evaluations have to be done in terms of nodes, so if the XPath returns something like a value then warn and move on.
					evaluateKVPairs((XdmNode) item, rootCount);
				} else {
					LOG.warn("Expected to find only elements after executing XPath on mapping list, got {}", item.getClass().getName());
				}
				rootCount++;
			}
			if ((rootCount == 0) && LOG.isDebugEnabled()) {
				LOG.debug("No results found for executing mapping root {} on {}", mappingRoot, this);
			}
		} else {
			// If there is no root specified by the contextual context, then use "." , or current node passed as rootNode parameter.
			if (LOG.isDebugEnabled()) {
				LOG.debug("No mapping root specified for {}, so executing against passed context node", mappingRoot, this.mapping);
			}
			evaluateKVPairs(rootNode, rootCount);
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
	private void evaluateKVPairs(XdmNode node, int positionRelativeToOtherRootNodes) throws DataExtractorException {
		XPathValue kvPairRoot = this.mapping.getKVPairRoot();
		XPathValue keyXPath = this.mapping.getKeyXPath();
		XPathValue rootXPath = this.mapping.getMappingRoot();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Getting Key/Value Pair Roots using {} for {}", keyXPath, this.mapping);
		}
		int positionRelativeToIMappingSiblings = 0;
		List<IExtractionResults> iterationECs = new ArrayList<IExtractionResults>();

		XPathSelector kvIterator = kvPairRoot.evaluate(node);
		for (XdmItem kvItem : kvIterator) {
			if (!(kvItem instanceof XdmNode)) {
				LOG.warn("KVPair Root yielded a {} ({}) instead of XdmNode.  Cannot find keys/values from here!", kvItem, kvItem.getClass());
				continue;
			}

			XdmNode kvNode = (XdmNode) kvItem;
			String keyName = getKey(kvNode, keyXPath);
			if (keyName == null) {
				LOG.info("No key could be found for {} on {}.  Moving on.", keyXPath, this.mapping);
			} else {
				LOG.debug("Found pivot mapping key {}", keyName);
				MappingExtractionContext childCtx = ensureMec(keyName, positionRelativeToOtherRootNodes, positionRelativeToIMappingSiblings);
				childCtx.evaluate(kvNode);
				positionRelativeToIMappingSiblings++;
				iterationECs.add(childCtx);
			}
		}

		if (iterationECs.size() > 0) {
			if (positionRelativeToIMappingSiblings == 0) {
				LOG.debug("Added {} keys for pivot mapping {} after executing ", positionRelativeToIMappingSiblings, rootXPath);
			}
			this.children.add(iterationECs);
		} else {
			if (LOG.isInfoEnabled()) {
				if (positionRelativeToIMappingSiblings == 0) {
					LOG.debug("Found no keys for pivot mapping {} after executing ", this, rootXPath);
				}
			}
		}
	}

	@Override
	public List<List<IExtractionResults>> getChildren() {
		return this.children;
	}

	/**
	 * Retrieves a key name by taking the first result from executing <code>keyXPath</code> against <code>kvNode</code>. If there are multiple results
	 * then all but the first are disregarded. If there are no results, or the first result does not yield a string when passed to
	 * {@link XdmItem#getStringValue()} then null is returned.
	 *
	 * @param kvNode the context node from which to execute <code>keyXPath</code>.
	 * @param keyXPath the XPath statement to execute from <code>kvNode</code>.
	 * @return either a string key name, or null if one could not be found.
	 * @throws DataExtractorException if any errors occur whilst extracting the key value from the XML.
	 */
	private String getKey(XdmNode kvNode, XPathValue keyXPath) throws DataExtractorException {
		String keyName;
		XPathSelector keyIterator = keyXPath.evaluate(kvNode);
		Iterator<XdmItem> items = keyIterator.iterator();
		if (!items.hasNext()) {
			LOG.warn("KVPair key XPath {} yielded no results", keyXPath);
			keyName = null;
		} else {
			XdmItem keyItem = items.next();
			String baseName = keyItem.getStringValue();
			if (StringUtil.isNullOrEmpty(baseName)) {
				LOG.debug("Found XML node for key {} but had null/empty value", keyXPath);
				keyName = null;
			} else {
				keyName = baseName.trim();
				LOG.debug("Found key name {} from {}", keyName, keyXPath);
			}
		}
		return keyName;
	}

	@Override
	public IMappingContainer getMapping() {
		return this.mapping;
	}

	@Override
	public IMappingContainer getMappingContainer() {
		return this.mapping;
	}

	@Override
	public String getName() {
		return this.mapping.getName();
	}

	@Override
	public List<IExtractionResults> getResultsSetAt(int valueIndex) {
		return (this.children.size() > valueIndex) ? this.children.get(valueIndex) : null;
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
			this.children = (List<List<IExtractionResults>>) readObject;
		} catch (ClassCastException cce) {
			throw new IOException("Unexpected object type found in stream.  Expected List<List<IExtractionResults>> but got "
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
		String mappingName = getMapping().getName();
		int size = this.children.size();
		if (LOG.isInfoEnabled()) {
			LOG.info("Writing {} results to the CSI file for {}", size, mappingName);
		}
		stream.writeObject(this.children);
		stream.writeObject(mappingName);
	}

}
