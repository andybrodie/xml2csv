package com.locima.xml2csv.configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pivot mappings are pseudo-containers that dynamically create mappings based on data found within the document.
 */
public class PivotMapping implements IMappingContainer {

	private static final Logger LOG = LoggerFactory.getLogger(PivotMapping.class);

	private List<Mapping> children;

	private int groupNumber;

	private int highesFoundValueCount;

	private XPathValue keyXPath;

	private MultiValueBehaviour multiValueBehaviour;

	private NameFormat nameFormat;

	private IMappingContainer parent;

	private String pivotMappingName;

	private XPathValue mappingRoot;

	private XPathValue valueXPath;

	private XPathValue kvPairRoot;

	/**
	 * Creates a new instance of a Pivot Mapping object.
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
			if (pkm.getBaseName().equals(keyName)) {
				return pkm;
			}
		}
		return null;
	}

	@Override
	public String getContainerName() {
		return this.pivotMappingName;
	}

	@Override
	public int getFieldCountForSingleRecord() {
		return getMultiValueBehaviour() == MultiValueBehaviour.LAZY ? 1 : Math.max(getMinValueCount(), getHighestFoundValueCount());
	}

	@Override
	public int getGroupNumber() {
		return this.groupNumber;
	}

	@Override
	public int getHighestFoundValueCount() {
		return this.highesFoundValueCount;
	}

	public XPathValue getKeyXPath() {
		return this.keyXPath;
	}

	public XPathValue getKVPairRoot() {
		return this.kvPairRoot;
	}

	public void setKVPairRoot(XPathValue kvPairRoot) {
		this.kvPairRoot = kvPairRoot;
	}

	/**
	 * Gets the XPath expression that returns the root nodes from which {@link #keyXPath} and {@link #valueXPath} will be evaluated.
	 * 
	 * @return the XPath expression that returns the root nodes from which {@link #keyXPath} and {@link #valueXPath} will be evaluated.
	 */
	@Override
	public XPathValue getMappingRoot() {
		return this.mappingRoot;
	}

	@Override
	public int getMaxValueCount() {
		return 0;
	}

	@Override
	public int getMinValueCount() {
		return 0;
	}

	@Override
	public MultiValueBehaviour getMultiValueBehaviour() {
		return this.multiValueBehaviour;
	}

	@Override
	public NameFormat getNameFormat() {
		return this.nameFormat;
	}

	@Override
	public IMappingContainer getParent() {
		return this.parent;
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
			pkm =
							new Mapping(this, keyName, getNameFormat(), getGroupNumber() + 1, getMultiValueBehaviour(), getValueXPath(),
											getMinValueCount(), getMaxValueCount());
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
	 * Because pivot mapping columns depend on the documents passed to them, pivot mappings cannot predict how many fields they will return, so always
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
		return this.pivotMappingName.hashCode();
	}

	@Override
	public Iterator<IMapping> iterator() {
		// TODO It can't be this hard, surely?
		List<IMapping> iterableIMappingInstance = new ArrayList<IMapping>(this.children.size());
		iterableIMappingInstance.addAll(this.children);
		return iterableIMappingInstance.iterator();
	}

	/**
	 * Sets the logical group number of this mapping container.
	 * 
	 * @param groupNumber the logical group number of this mapping container.
	 */
	public void setGroupNumber(int groupNumber) {
		this.groupNumber = groupNumber;
	}

	@Override
	public void setHighestFoundValueCount(int valueFound) {
		this.highesFoundValueCount = valueFound;
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
	 * Sets the name given to this pivot mapping, if top-level will be used to generate the output file name.
	 * 
	 * @param mappingName the name given to this pivot mapping, if top-level will be used to generate the output file name.
	 */
	public void setMappingName(String mappingName) {
		this.pivotMappingName = mappingName;
	}

	/**
	 * Sets what should happen when multiple values are found for a single evaluation of a single field wtihin this mapping.
	 * 
	 * @param multiValueBehaviour defines what should happen when multiple values are found for a single evaluation of a single field wtihin this
	 *            mapping.
	 */
	public void setMultiValueBehaviour(MultiValueBehaviour multiValueBehaviour) {
		this.multiValueBehaviour = multiValueBehaviour;
	}

	/**
	 * Sets the format to be used for the {@link Mapping} instance that this method creates.
	 * 
	 * @param nameFormat the format to be used for the {@link Mapping} instance that this method creates.
	 */
	public void setNameFormat(NameFormat nameFormat) {
		this.nameFormat = nameFormat;
	}

	/**
	 * Sets the logical group number of this mapping container.
	 * 
	 * @param parent the logical group number of this mapping container.
	 */
	public void setParent(IMappingContainer parent) {
		this.parent = parent;
	}

	/**
	 * Sets the XPath expression that returns the root nodes from which {@link #keyXPath} and {@link #valueXPath} will be evaluated.
	 * 
	 * @param mappingRoot the XPath expression that returns the root nodes from which {@link #keyXPath} and {@link #valueXPath} will be evaluated.
	 */
	public void setMappingRoot(XPathValue mappingRoot) {
		this.mappingRoot = mappingRoot;
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
		sb.append(this.pivotMappingName);
		final String separator = ", ";
		sb.append(separator);
		sb.append(getNameFormat());
		sb.append(separator);
		sb.append(getGroupNumber());
		sb.append(separator);
		sb.append(getMultiValueBehaviour());
		sb.append(separator);
		sb.append(" Root(");
		sb.append(this.mappingRoot);
		sb.append("), Value(");
		sb.append(" KVPairRoot(");
		sb.append(this.kvPairRoot);
		sb.append("), Value(");
		sb.append(" Key(");
		sb.append(this.keyXPath);
		sb.append("), Value(");
		sb.append(this.valueXPath);
		sb.append("))");
		return sb.toString();
	}

}
