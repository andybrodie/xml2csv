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
import com.locima.xml2csv.configuration.XPathValue;
import com.locima.xml2csv.output.IExtractionResultsContainer;
import com.locima.xml2csv.output.IExtractionResultsValues;
import com.locima.xml2csv.output.inline.CsiInputStream;
import com.locima.xml2csv.util.StringUtil;

/**
 * Used to store the context of a set of extracted values for 0..n executions of a {@link IValueMapping} against multiple input documents.
 */
public class MappingExtractionContext extends AbstractExtractionContext implements IExtractionResultsValues {

	private static final Logger LOG = LoggerFactory.getLogger(MappingExtractionContext.class);

	/**
	 * Default value (1)L.
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

	/**
	 * Create a new instance.
	 *
	 * @param parent the parent container's evaluation. Must never be null.
	 * @param mapping the mapping that this MEC is going to be evaluating.
	 * @param positionRelativeToOtherRootNodes the index of the new context, with respect to its siblings (first child of the parent has index 0,
	 *            second has index 1, etc.).
	 * @param positionRelativeToIMappingSiblings The position of this extraction context with respect to its sibling {@link IMapping} instances
	 *            beneath the parent.
	 */
	public MappingExtractionContext(IExtractionResultsContainer parent, IValueMapping mapping, int positionRelativeToOtherRootNodes,
					int positionRelativeToIMappingSiblings) {
		super(parent, positionRelativeToOtherRootNodes, positionRelativeToIMappingSiblings);
		if (mapping == null) {
			throw new ArgumentNullException("mapping");
		}
		this.mapping = mapping;
	}

	/**
	 * Evaluates this mapping, using the passed XML node as a root for all XPath statements.
	 *
	 * @param mappingRoot the XML node from which to execute all XPath statements contained within mappings. Must not be null.
	 * @throws DataExtractorException if an error occurred whilst extracting data (typically this would be caused by bad XPath, or XPath invalid from
	 *             the <code>mappingRoot</code> specified).
	 */
	// CHECKSTYLE:OFF Cyclomatic complexity limit up to 11, would be less but I need logging here and splitting would make more complex.
	@Override
	// CHECKSTYLE:ON
	public void evaluate(XdmNode mappingRoot) throws DataExtractorException {
		if (mappingRoot == null) {
			throw new ArgumentNullException("mappingRoot");
		}

		IValueMapping thisMapping = getMapping();
		String fieldName = thisMapping.getBaseName();
		XPathValue xPath = thisMapping.getValueXPath();

		if (LOG.isTraceEnabled()) {
			LOG.trace("Extracting value for \"{}\" using XPath \"{}\"", fieldName, xPath.getSource());
		}

		// Typically there is only one result, so use that as the normal case
		// CHECKSTYLE:OFF I want to use trimToSize later, so need to refer to ArrayList
		ArrayList<String> values = new ArrayList<String>(1);
		// CHECKSTYLE:ON
		int maxValueCount = thisMapping.getMaxValueCount();

		XPathSelector selector = xPath.evaluate(mappingRoot);
		Iterator<XdmItem> resultIter = selector.iterator();
		while (resultIter.hasNext()) {
			// Add the next result to the list of values found, trimming whitespace if configured to do so.
			String value = resultIter.next().getStringValue();
			if ((value != null) && thisMapping.requiresTrimWhitespace()) {
				value = value.trim();
			}
			values.add(value);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Field \"{}\" found value({}) \"{}\" found after executing XPath \"{}\" (max: {})", fieldName, values.size(), value,
								xPath.getSource(), maxValueCount);
			}

			// If maxValueCount applies, then break out of the while loop once we've found the most number of values permitted.
			if ((maxValueCount > 0) && ((values.size()) == maxValueCount)) {
				if (LOG.isInfoEnabled()) {
					if (resultIter.hasNext()) {
						LOG.info("Discarded at least 1 value from mapping {} as maxValueCount reached limit of {}", this, maxValueCount);
					}
				}
				break;
			}

		}

		// Keep track of the most number of results we've found for a single invocation
		thisMapping.setHighestFoundValueCount(values.size());

		if (LOG.isTraceEnabled()) {
			LOG.trace("Adding values to {}: {}", thisMapping, StringUtil.collectionToString(values, ",", "\""));
		}
		values.trimToSize();
		this.results = values;
	}

	@Override
	public IValueMapping getMapping() {
		return this.mapping;
	}

	@Override
	public String getName() {
		return getMapping().getBaseName();
	}

	/**
	 * Gets all the values found by this evaluation of the mapping (i.e. against a single root node).
	 *
	 * @return an ordered list of values extracted from this mapping.
	 */
	@Override
	public List<String> getResults() {
		return this.results;
	}

	@Override
	public String getValueAt(int index) {
		String value = (this.results.size() > index) ? this.results.get(index) : null;
		return value;
	}

	@Override
	public IValueMapping getValueMapping() {
		return this.mapping;
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
		if (this.mapping == null) {
			throw new IOException("Unable to deserialize MEC because serialized IValueMapping link not found: " + mappingName);
		}
	}

	@Override
	public int size() {
		return this.results.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("MEC(");
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
		String mappingName = getMapping().getBaseName();
		int size = this.results.size();
		if (LOG.isInfoEnabled()) {
			LOG.info("Writing {} results to the CSI file for {}", size, mappingName);
		}
		stream.writeObject(this.results);
		stream.writeObject(mappingName);
	}

}
