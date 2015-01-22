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

import com.locima.xml2csv.ArgumentNullException;
import com.locima.xml2csv.BugException;
import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IValueMapping;
import com.locima.xml2csv.output.IExtractionResultsValues;
import com.locima.xml2csv.output.inline.CsiInputStream;
import com.locima.xml2csv.util.StringUtil;

/**
 * Used to store the context of a set of extracted values for 0..n executions of a {@link IValueMapping} against multiple input documents.
 */
public class MappingExtractionContext extends ExtractionContext implements IExtractionResultsValues {

	private static final Logger LOG = LoggerFactory.getLogger(MappingExtractionContext.class);

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The mapping that should be used to evaluate input documents against.
	 */
	private transient IValueMapping mapping;

	/**
	 * The set of values extracted as a result of executing this mapping within the context of a single root of the parent. E.g. if the parent found 3
	 * mapping roots then this will create 3 instances of {@link MappingExtractionContext}.
	 */
	private List<String> results;

	/**
	 * Default no-arg constructor required for serialization.
	 */
	public MappingExtractionContext() {

	}

	public MappingExtractionContext(ContainerExtractionContext parent, IValueMapping mapping, int positionRelativeToOtherRootNodes,
					int positionRelativeToIMappingSiblings) {
		super(parent, positionRelativeToOtherRootNodes, positionRelativeToIMappingSiblings);
		this.mapping = mapping;
	}

	/**
	 * Evaluates this mapping, using the passed XML node as a root for all XPath statements.
	 *
	 * @param rootNode the XML node from which to execute all XPath statements contained within mappings. Must not be null.
	 * @param trimWhitespace if true, then leading and trailing whitespace will be removed from all data values.
	 * @return an array of values extracted from the data. May be empty, but never null.
	 * @throws DataExtractorException if an error occurred whilst extracting data (typically this would be caused by bad XPath, or XPath invalid from
	 *             the <code>mappingRoot</code> specified).
	 */
	@Override
	public void evaluate(XdmNode mappingRoot) throws DataExtractorException {
		if (mappingRoot == null) {
			throw new ArgumentNullException("mappingRoot");
		}

		IValueMapping mapping = this.getMapping();

		String fieldName = mapping.getBaseName();

		if (LOG.isTraceEnabled()) {
			LOG.trace("Extracting value for \"{}\" using XPath \"{}\"", fieldName, mapping.getValueXPath().getSource());
		}

		List<String> values = new ArrayList<String>();
		int maxValueCount = mapping.getMaxValueCount();

		XPathSelector selector = mapping.getValueXPath().evaluate(mappingRoot);
		Iterator<XdmItem> resultIter = selector.iterator();
		while (resultIter.hasNext()) {
			String value = resultIter.next().getStringValue();
			if ((value != null) && mapping.requiresTrimWhitespace()) {
				value = value.trim();
			}
			values.add(value);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Field \"{}\" found value({}) \"{}\" found after executing XPath \"{}\" (max: {})", fieldName, values.size(), value,
								mapping.getValueXPath().getSource(), maxValueCount);
			}

			if ((maxValueCount > 0) && ((values.size()) == maxValueCount)) {
				if (LOG.isWarnEnabled()) {
					if (resultIter.hasNext()) {
						LOG.warn("Discarded at least 1 value from mapping {} as maxValueCount reached limit of {}", this, maxValueCount);
					}
				}
				break;
			}

		}

		// Keep track of the most number of results we've found for a single invocation
		mapping.setHighestFoundValueCount(values.size());

		if (LOG.isTraceEnabled()) {
			LOG.trace("Adding values to {}: {}", mapping, StringUtil.collectionToString(values, ",", "\""));
		}
		this.results = values;
	}

	/**
	 * Gets all the values found by this evaluation of the mapping (i.e. against a single root node). This takes in to account the number of fields
	 * required to be output as opposed to just the number found (see {@link IMapping#getFieldCountForSingleRecord()}).
	 *
	 * @param namePrefix the prefix to be applied to each field name.
	 * @return an ordered list of values extracted from this mapping.
	 */
	@Override
	public List<String> getAllValues() {
		int valueCountRequired = this.getMapping().getFieldCountForSingleRecord();
		List<String> fields = new ArrayList<String>(valueCountRequired);
		for (int i = 0; i < valueCountRequired; i++) {
			fields.add(getValueAt(i));
		}
		return fields;
	}

	@Override
	public IValueMapping getMapping() {
		return this.mapping;
	}

	@Override
	public String getName() {
		return this.getMapping().getBaseName();
	}

	/**
	 * Retrieve an extracted field instance for the value at the index given.
	 *
	 * @param valueIndex the index of the value to retrieve within this mapping.
	 * @return an extracted field for the index given. If there is no value for a field with this index, then null is returned (never throws an
	 *         exception for this).
	 */
	@Override
	public String getValueAt(int valueIndex) {
		String value = (this.results.size() > valueIndex) ? this.results.get(valueIndex) : null;
		return value;
	}

	@Override
	public int size() {
		return this.results.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("MEC(");
		sb.append(this.getMapping());
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
		String mappingName = this.getMapping().getBaseName();
		int size = this.results.size();
		if (LOG.isInfoEnabled()) {
			LOG.info("Writing {} results to the CSI file for {}", size, mappingName);
		}
		stream.writeObject(this.results);
		stream.writeObject(mappingName);
	}

	/**
	 * Overridden to manage not writing {@link #mapping} to the output stream.
	 * 
	 * @param stream target stream for serialized state.
	 * @throws IOException if any issues occur during writing.
	 */
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream rawInputStream) throws IOException, ClassNotFoundException {
		if (!(rawInputStream instanceof CsiInputStream)) {
			throw new BugException("Bug found when deserializing MEC.  I've been given an ObjectInputStream instead of a CsiInputStream!");
		}
		CsiInputStream stream = (CsiInputStream) rawInputStream;
		Object readObject = stream.readObject();
		try {
			this.results = (List<String>) readObject;
		} catch (ClassCastException cce) {
			throw new IOException("Unexpected object type found in stream.  Expected List<String> but got " + readObject.getClass().getName());
		}
		readObject = stream.readObject();
		String mappingName;
		try {
			mappingName = (String) readObject;
		} catch (ClassCastException cce) {
			throw new IOException("Unexpected object type found in stream.  Expected String but got " + readObject.getClass().getName());
		}
		this.mapping = stream.getIValueMapping(mappingName);
		if (this.mapping==null) {
			throw new IOException("Unable to deserialize MEC because serialized IValueMapping link not found: " + mappingName);
		}
	}

}
