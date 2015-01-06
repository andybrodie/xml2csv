package com.locima.xml2csv.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.XMLException;

public class PivotMapping extends AbstractMapping implements IMapping {

	private static final Logger LOG = LoggerFactory.getLogger(PivotMapping.class);

	private XPathValue baseNameXPath;

	/**
	 * Creates a new instance of a Pivot Mapping object.
	 *
	 * @param namespaceMappings A mapping of namespace prefix to URI mappings. May be null if there are no namespaces involved.
	 * @param baseNameXPath the XPath expression that, when executed relative to the mapping root of the parent, will yield the base name of the
	 *            fields that this pivot mapping will return. Must not be null.
	 * @param valueXPath the XPath expression that, when executed relative to the node yielding the key, will yield the value for that key. Must not
	 *            be null.
	 * @param nameFormat the format to be used for the {@link Mapping} instance that this method creates.
	 * @param multiValueBehaviour defines what should happen when multiple values are found for a single evaluation of a single field wtihin this
	 *            mapping.
	 * @return a new Mapping instance configured with the parameters passed.
	 * @throws XMLException If there was problem compiling the expression (for example, if the XPath is invalid).
	 */
	public PivotMapping(IMappingContainer parent, XPathValue baseNameXPath, XPathValue valueXPath, NameFormat nameFormat, int groupNumber,
					MultiValueBehaviour multiValueBehaviour) throws XMLException {
		super(parent, nameFormat, groupNumber, multiValueBehaviour, valueXPath);
		this.baseNameXPath = baseNameXPath;
	}

	// /**
	// * Evaluate this pivot mapping but executing the value extracting XPath for every key found by executing the base name XPath.
	// *
	// * @param rootNode the context node from which to execute the key-finding XPath expression.
	// * @return a (possibly empty) records set.
	// * @throws DataExtractorException if anything goes wrong finding the field definitions.
	// */
	// public ExtractedRecordList evaluate(XdmNode rootNode, ContainerExtractionContext ctx, boolean trimWhitespace) throws DataExtractorException {
	// XPathSelector keysIterator = this.baseNameXPath.evaluate(rootNode);
	// ExtractedRecordList rs = new ExtractedRecordList();
	// int keyCount = 0;
	// for (XdmItem item : keysIterator) {
	// keyCount++;
	// String baseName = item.getStringValue();
	// if (baseName != null) {
	// baseName = baseName.trim();
	// }
	//
	// // TODO Think about whether pivot mappings should permit min and max values
	// // TODO THink about whether PivotMappings are mappings or mapping containers.
	// // Mapping mapping = new Mapping(getParent(), baseName, getNameFormat(), getGroupNumber(), getMultiValueBehaviour(), getValueXPath(), 0
	// // ,0);
	// // ExtractedRecordList pivotEntryResults = mapping.evaluate((XdmNode) item, ctx, true);
	// // rs.addAll(pivotEntryResults);
	// }
	// if (LOG.isDebugEnabled()) {
	// LOG.debug("{} found {} field definition(s) after executing XPath {}", this, keyCount, this.baseNameXPath.getSource(),
	// this.baseNameXPath.getSource());
	// }
	// return rs;
	// }

	@Override
	public int getMaxValueCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMinValueCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * By their nature, pivot mappings cannot predict how many fields they will return, always returns <code>false</code>
	 */
	@Override
	public boolean hasFixedOutputCardinality() {
		LOG.info("PivotMapping {} hasFixedOutputCardinality = false", this);
		return false;
	}

	@Override
	public int hashCode() {
		return this.baseNameXPath.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("PivotMapping(");
		sb.append(this.baseNameXPath.getSource());
		sb.append(',');
		sb.append(getNameFormat());
		sb.append(',');
		sb.append(getGroupNumber());
		sb.append(',');
		sb.append(getMultiValueBehaviour());
		sb.append(',');
		sb.append(getValueXPath().getSource());
		sb.append(')');
		return sb.toString();
	}

	@Override
	public int getFieldCountForSingleRecord() {
		// TODO Auto-generated method stub
		return 0;
	}

}
