package com.locima.xml2csv.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.ArgumentException;
import com.locima.xml2csv.ArgumentNullException;
import com.locima.xml2csv.StringUtil;
import com.locima.xml2csv.extractor.DataExtractorException;
import com.locima.xml2csv.inputparser.FileParserException;
import com.locima.xml2csv.model.filter.FilterContainer;
import com.locima.xml2csv.model.filter.IInputFilter;

/**
 * Abstracts a list of mappings between XPath statements and Column Names with methods only relevant to this application.
 */
public class MappingConfiguration implements Iterable<IMappingContainer> {

	private static final Logger LOG = LoggerFactory.getLogger(MappingConfiguration.class);

	/**
	 * The default inline behaviour (when multiple values for fields are found) for all mappings (unless overridden).
	 */
	private MultiValueBehaviour defaultInlineBehaviour;

	/**
	 * Contains all the input filters that have been configured for this set of mappings.
	 */
	private FilterContainer filterContainer = new FilterContainer();

	/**
	 * The list of mappings maintained by this object.
	 */
	private List<IMappingContainer> mappings = new ArrayList<IMappingContainer>();

	/**
	 * The map of XML namespace prefix to namespace URIs used by any {@link MappingList} or {@link Mapping} contained within this configuration.
	 */
	private Map<String, String> namespaceMappings = new HashMap<String, String>();

	/**
	 * Add a new input filter to the list of filters that will be executed for all files processed by this mapping configuration.
	 * 
	 * @param filter the filter to add, must not be null.
	 */
	public void addInputFilter(IInputFilter filter) {
		if (filter == null) {
			throw new ArgumentNullException("filter");
		}
		LOG.debug("Adding filter {} to mapping configuration filters", filter);
		this.filterContainer.addNestedFilter(filter);
	}

	/**
	 * Adds a child set of mappings to this mappings set.
	 *
	 * @param maps a set of mappings, must not be null and must have a unique {@link MappingList#getOutputName()} value.
	 */
	public void addMappings(IMappingContainer maps) {
		if (maps == null) {
			throw new ArgumentNullException("maps");
		}
		// Ensure that the mapping set name is unique
		String mappingSetName = maps.getOutputName();
		if (mappingSetName == null) {
			throw new ArgumentException("maps", "contains a null name.");
		}
		if (getMappingsByName(maps.getOutputName()) != null) {
			throw new ArgumentException("maps", "must contain a unique name");
		}
		this.mappings.add(maps);
	}

	/**
	 * Adds a namespace prefix to URI mapping that can be used in any descendant mapping.
	 *
	 * @param prefix The prefix that may be used within a descendant mapping. Null indicates default namespace.
	 * @param uri The URI that it maps to. Must not be null.
	 * @throws FileParserException If an attempt is made to reassign an existing prefix/URI mapping to a new URI.
	 */
	public void addNamespaceMapping(String prefix, String uri) throws FileParserException {
		if (StringUtil.isNullOrEmpty(uri)) {
			throw new ArgumentNullException("uri");
		}
		String existingUri = this.namespaceMappings.get(prefix);
		if (existingUri != null) {
			if (uri.equals(existingUri)) {
				LOG.debug("Ignoring duplicate namespace prefix declaration {} -> {}", prefix, uri);
			} else {

				throw new FileParserException(
								"Cannot tolerate the same namespace prefix used for different URIs in mapping config (%s maps to %s and %s", prefix,
								existingUri, uri);
			}
		} else {
			this.namespaceMappings.put(prefix, uri);
		}
	}

	/**
	 * Returns true if this mapping configuration has encountered any mapping lists or mappings with multiple values.
	 *
	 * @return true if this mapping configuration has encountered any mapping lists or mappings with multiple values.
	 */
	public boolean containsInline() {
		for (IMappingContainer container : this) {
			if (containsInline(container)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if this {@link IMapping} instance passed contains any mapping lists or mappings with multiple values.
	 *
	 * @param mapping the mapping to search for multiple values.
	 * @return true if this {@link IMapping} instance passed contains any mapping lists or mappings with multiple values.
	 */
	private boolean containsInline(IMapping mapping) {
		if (mapping.getMaxInstanceCount() > 1) {
			return true;
		}
		boolean foundOne = false;
		if (mapping instanceof IMappingContainer) {
			for (IMapping childMapping : ((IMappingContainer) mapping)) {
				if (containsInline(childMapping)) {
					foundOne = true;
					break;
				}
			}
		}
		return foundOne;
	}

	/**
	 * Gets the default inline behaviour for this configuration.
	 *
	 * @return never returns null.
	 */
	public MultiValueBehaviour getDefaultInlineBehaviour() {
		return this.defaultInlineBehaviour;
	}

	/**
	 * Get a specific set of mappings by name. Generally used for unit testing but might be handy one day.
	 *
	 * @param name the name of the mapping set to return
	 * @return null if a mapping set with that name could not be found.
	 */
	public IMappingContainer getMappingsByName(String name) {
		for (IMappingContainer mapping : this.mappings) {
			if (mapping.getOutputName().equals(name)) {
				return mapping;
			}
		}
		return null;
	}

	/**
	 * Retrieves all "headers" associated with all the mappings.
	 * <p>
	 * The headings are all the output names mapped to the column names that they have. This is useful for initialising all the output files using
	 * {@link com.locima.xml2csv.output.OutputManager#createFiles(Map)}
	 *
	 * @return a map, possibly empty, but never null, or output name to the list of column names.
	 */
	public Map<String, List<String>> getMappingsHeaders() {
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		for (IMappingContainer mapping : this.mappings) {
			List<String> colNames = new ArrayList<String>();
			int colCount = mapping.getColumnNames(colNames);
			LOG.info("Found {} columns in mapping configuration for {}", colCount, this);
			headers.put(mapping.getOutputName(), colNames);
		}
		return headers;
	}

	/**
	 * Retrieve the namespace prefix to URI map that's associated with this configuration.
	 * <p>
	 * These are applied to all the XPath statements in mappings and mapping roots.
	 *
	 * @return a possibly empty map which mappings a namespace prefix to a URI.
	 */
	public Map<String, String> getNamespaceMap() {
		return this.namespaceMappings;
	}

	@Override
	public Iterator<IMappingContainer> iterator() {
		return this.mappings.iterator();
	}

	/**
	 * Gets all the mappings contained within this set as an array.
	 *
	 * @return an array of mappings, possibly empty but never null.
	 */
	public MappingList[] mappingsToArray() {
		return this.mappings.toArray(new MappingList[0]);
	}

	/**
	 * Sets the default inline behaviour for all child mappings of this configuration. If {@link MultiValueBehaviour#INHERIT} is specified then it
	 * will be substitued for {@link MultiValueBehaviour#IGNORE} as there is nowhere to inherit from.
	 *
	 * @param defaultInlineBehaviour the default inline behaviour for child mappings.
	 */
	public void setDefaultInlineBehaviour(MultiValueBehaviour defaultInlineBehaviour) {
		this.defaultInlineBehaviour = (defaultInlineBehaviour == MultiValueBehaviour.INHERIT) ? MultiValueBehaviour.IGNORE : defaultInlineBehaviour;
	}

	/**
	 * Returns the number of mappings contained in the configuration.
	 *
	 * @return a natural number.
	 */
	public int size() {
		return this.mappings.size();
	}

	/**
	 * Returns true if this mapping configuration is interested in processing the passed XML file.
	 * 
	 * @param xmlFile the XML file to test. Must not be null.
	 * @return true if the file should be processed, false otherwise.
	 */
	public boolean include(File xmlFile) {
		return this.filterContainer.include(xmlFile);
	}

	/**
	 * Returns true if this mapping configuration is interested in processing the passed XML document.
	 * 
	 * @param xmlDoc the XML document to test. Must not be null.
	 * @return true if the document should be processed, false otherwise.
	 * @throws DataExtractorException if there was a problem executing the filter.
	 */
	public boolean include(XdmNode xmlDoc) throws DataExtractorException {
		return this.filterContainer.include(xmlDoc);
	}
}
