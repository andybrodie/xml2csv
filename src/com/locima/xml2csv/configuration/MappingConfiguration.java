package com.locima.xml2csv.configuration;

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
import com.locima.xml2csv.configuration.filter.FilterContainer;
import com.locima.xml2csv.configuration.filter.IInputFilter;
import com.locima.xml2csv.extractor.DataExtractorException;
import com.locima.xml2csv.inputparser.FileParserException;
import com.locima.xml2csv.util.StringUtil;

/**
 * Abstracts a list of mappings between XPath statements and Column Names with methods only relevant to this application.
 */
public class MappingConfiguration implements Iterable<IMappingContainer> {

	private static final Logger LOG = LoggerFactory.getLogger(MappingConfiguration.class);

	/**
	 * The default inline behaviour (when multiple values for fields are found) for all mappings (unless overridden).
	 */
	private MultiValueBehaviour defaultMultiValueBehaviour;

	private NameFormat defaultNameFormat;

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
	public IMappingContainer addMappings(IMappingContainer maps) {
		if (maps == null) {
			throw new ArgumentNullException("maps");
		}
		// Ensure that the mapping set name is unique
		String containerName = maps.getContainerName();
		if (containerName == null) {
			throw new ArgumentException("maps", "contains a null name.");
		}
		if (containsContainer(containerName)) {
			throw new ArgumentException("maps", "must contain a unique name");
		}
		this.mappings.add(maps);
		return maps;
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
	 * Determines whether this object already contains a mapping contains with the same name (this isn't allowed).
	 *
	 * @param name the name of the mapping set to return
	 * @return null if a mapping set with that name could not be found.
	 */
	public boolean containsContainer(String name) {
		for (IMappingContainer mapping : this.mappings) {
			if (mapping.getContainerName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retrieve a top level mapping container ({@link MappingList by name, or null if it doesn't exist.
	 * @param containerName the name of the mapping container (needs to match {@link IMappingContainer#getContainerName()}).
	 * @return a mapping container instance with the matching name, or null if one doesn't exist.
	 */
	public IMappingContainer getContainerByName(String containerName) {
		for (IMappingContainer container : this.mappings) {
			if (container.getContainerName().equals(containerName)) {
				return container;
			}
		}
		return null;
	}
	
	public IMapping findMappingByName(String mappingName) {
		for (IMappingContainer container : this.mappings) {
			if (container.getContainerName().equals(mappingName)) {
				return container;
			}
			IMapping mapping = container.findMapping(mappingName);
			if (mapping!=null) {
				return mapping;
			}
		}
		return null;
	}


	public MultiValueBehaviour getDefaultMultiValueBehaviour() {
		return this.defaultMultiValueBehaviour;
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

	@Override
	public Iterator<IMappingContainer> iterator() {
		return this.mappings.iterator();
	}

	public void log() {
		for (IMappingContainer mappingList : this.mappings) {
			LOG.debug(mappingList.toString());
			for (IMapping child : mappingList) {
				log(child, 1);
			}
		}
	}

	private void log(IMapping mapping, int index) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < index; i++) {
			sb.append('\t');
		}
		sb.append(mapping.toString());
		LOG.debug(sb.toString());
		if (mapping instanceof IMappingContainer) {
			for (IMapping child : (IMappingContainer) mapping) {
				if (child instanceof IMappingContainer) {
					log(child, 1);
				} else {
					log(child, 1);
				}
			}
		}
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
	 * Sets the default inline behaviour for all child mappings of this configuration. If {@link MultiValueTolerance#INHERIT} is specified then it
	 * will be substitued for {@link MultiValueTolerance#IGNORE} as there is nowhere to inherit from.
	 *
	 * @param defaultMultiValueBehaviour the default inline behaviour for child mappings.
	 */
	public void setDefaultMultiValueBehaviour(MultiValueBehaviour defaultMultiValueBehaviour) {
		this.defaultMultiValueBehaviour =
						(defaultMultiValueBehaviour == MultiValueBehaviour.DEFAULT) ? MultiValueBehaviour.LAZY : defaultMultiValueBehaviour;
	}

	public void setDefaultNameFormat(NameFormat format) {
		this.defaultNameFormat = format;
	}

	/**
	 * Returns the number of mappings contained in the configuration.
	 *
	 * @return a natural number.
	 */
	public int size() {
		return this.mappings.size();
	}

}
