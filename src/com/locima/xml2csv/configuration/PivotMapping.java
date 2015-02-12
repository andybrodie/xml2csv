package com.locima.xml2csv.configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pivot mappings are pseudo-containers that dynamically create mappings based on data found within the document.
 */
public class PivotMapping extends AbstractMappingContainer implements IMappingContainer {

	private static final Logger LOG = LoggerFactory.getLogger(PivotMapping.class);

	private List<Mapping> children;

	private XPathValue keyXPath;

	private XPathValue kvPairRoot;

	private XPathValue valueXPath;

	/**
	 * Initialises the child collection.
	 */
	public PivotMapping() {
		this.children = new ArrayList<Mapping>();
	}

	/**
	 * Finds a named child {@link PivotKeyMapping} based on the <code>keyName</code> passed.
	 *
	 * @param keyName the name mapping to find.
	 * @return either a {@link PivotKeyMapping} instance, or null if one could not be found with the passed <code>keyName</code>.
	 */
	private Mapping findChild(String keyName) {
		for (Mapping pkm : this.children) {
			if (pkm.getName().equals(keyName)) {
				return pkm;
			}
		}
		return null;
	}

	/**
	 * Return the XPath expression that will return a set of key values when executed.
	 *
	 * @return the XPath expression that will return a set of key values when executed. Will never be null.
	 */
	public XPathValue getKeyXPath() {
		return this.keyXPath;
	}

	/**
	 * Return the XPath expression that will return a set of root nodes from which to execute {@link #getKeyXPath()} and {@link #getValueXPath()}
	 * expressions. May be null.
	 *
	 * @return the XPath expression that will return a set of root nodes from which to execute {@link #getKeyXPath()} and {@link #getValueXPath()}
	 *         expressions. May be null.
	 */
	public XPathValue getKVPairRoot() {
		return this.kvPairRoot;
	}

	/**
	 * Either finds an existing child {@link PivotKeyMapping} or creates a new one if one does not already exist.
	 *
	 * @param keyName the name of the {@link PivotKeyMapping} that we need to retrieve or create.
	 * @return a {@link Mapping} instance, never returns null.
	 * @see #findChild(String)
	 */
	public Mapping getPivotKeyMapping(String keyName) {
		Mapping pkm = findChild(keyName);
		if (pkm != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Returning existing PVM: {}", pkm);
			}
		} else {
			// TODO I'm probably going to want separately configurable values for MVB, minValueCount and maxValueCount later, but ok for now.
			pkm = new Mapping();
			pkm.setParent(this);
			pkm.setName(keyName);
			pkm.setNameFormat(getNameFormat());
			pkm.setGroupNumber(getGroupNumber() + 1);
			pkm.setMultiValueBehaviour(getMultiValueBehaviour());
			pkm.setValueXPath(getValueXPath());
			pkm.setMinValueCount(getMinValueCount());
			pkm.setMaxValueCount(getMaxValueCount());

			if (LOG.isDebugEnabled()) {
				LOG.debug("Created new PVM: {}", pkm);
			}
			this.children.add(pkm);
		}
		return pkm;
	}

	/**
	 * Get the XPath expression that, when executed relative to the results of {@link #getKVPairRoot()} will return 0 or more values.
	 *
	 * @return the XPath expression that, when executed relative to the results of {@link #getKVPairRoot()} will return 0 or more values.
	 */
	public XPathValue getValueXPath() {
		return this.valueXPath;
	}

	/**
	 * Because pivot mapping fields depend on the documents passed to them, pivot mappings cannot predict how many fields they will return, so always
	 * returns <code>false</code>.
	 *
	 * @return false.
	 */
	@Override
	public boolean hasFixedOutputCardinality() {
		LOG.info("PivotMapping {} hasFixedOutputCardinality = false", this);
		return false;
	}

	/**
	 * Returns a hash code solely based on the name of the field, as this is the only thing that really makes a difference between storing and
	 * indexing.
	 *
	 * @return the hash code of the base name of this definition.
	 */
	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	@Override
	public Iterator<IMapping> iterator() {
		// TODO It can't be this hard, surely?
		List<IMapping> iterableIMappingInstance = new ArrayList<IMapping>(this.children.size());
		iterableIMappingInstance.addAll(this.children);
		return iterableIMappingInstance.iterator();
	}

	/**
	 * Sets the XPath expression that, when executed relative to the mapping root of the parent, will yield the base name of the fields that this
	 * pivot mapping will return. Must not be null.
	 *
	 * @param keyXPath the XPath expression that, when executed relative to the mapping root of the parent, will yield the base name of the fields
	 *            that this pivot mapping will return. Must not be null.
	 */
	public void setKeyXPath(XPathValue keyXPath) {
		this.keyXPath = keyXPath;
	}

	/**
	 * Sets the XPath expression that will return a set of root nodes from which to execute {@link #getKeyXPath()} and {@link #getValueXPath()}
	 * expressions. May be null.
	 *
	 * @param newKVPairRoot the XPath expression that will return a set of root nodes from which to execute {@link #getKeyXPath()} and
	 *            {@link #getValueXPath()} expressions. May be null.
	 */
	public void setKVPairRoot(XPathValue newKVPairRoot) {
		this.kvPairRoot = newKVPairRoot;
	}

	/**
	 * Set the name given to this pivot mapping, if top-level will be used to generate the output file name.
	 *
	 * @param valueXPath the name given to this pivot mapping, if top-level will be used to generate the output file name.
	 */
	public void setValueXPath(XPathValue valueXPath) {
		this.valueXPath = valueXPath;
	}

	@Override
	public int size() {
		return this.children.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("PivotMapping(");
		sb.append(getName());
		final String separator = ", ";
		sb.append(separator);
		sb.append(getNameFormat());
		sb.append(separator);
		sb.append(getGroupNumber());
		sb.append(separator);
		sb.append(getMultiValueBehaviour());
		sb.append(separator);
		sb.append("Root(");
		sb.append(getMappingRoot());
		sb.append("), KVPairRoot(");
		sb.append(this.kvPairRoot);
		sb.append("), Key(");
		sb.append(this.keyXPath);
		sb.append("), Value(");
		sb.append(this.valueXPath);
		sb.append("))");
		return sb.toString();
	}

}
