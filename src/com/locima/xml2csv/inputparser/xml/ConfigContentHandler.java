package com.locima.xml2csv.inputparser.xml;

import java.util.Stack;
import java.util.regex.PatternSyntaxException;

import javax.xml.XMLConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.locima.xml2csv.ArgumentNullException;
import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.configuration.Mapping;
import com.locima.xml2csv.configuration.MappingConfiguration;
import com.locima.xml2csv.configuration.MappingList;
import com.locima.xml2csv.configuration.MultiValueBehaviour;
import com.locima.xml2csv.configuration.NameFormat;
import com.locima.xml2csv.configuration.XPathValue;
import com.locima.xml2csv.configuration.filter.FileNameInputFilter;
import com.locima.xml2csv.configuration.filter.IInputFilter;
import com.locima.xml2csv.configuration.filter.XPathInputFilter;
import com.locima.xml2csv.inputparser.FileParserException;
import com.locima.xml2csv.util.StringUtil;
import com.locima.xml2csv.util.XmlUtil;

/**
 * The SAX Content Handler for input XML files.
 */
public class ConfigContentHandler extends DefaultHandler {

	/**
	 * All of the valid element names that will be processed. This is used to make the {@link #startElement(String, String, String, Attributes)} code
	 * a bit more elegant using a switch statement.
	 */
	private static enum ElementNames {
		FileNameInputFilter, Filters, Mapping, MappingConfiguration, MappingList, PivotMapping, XPathInputFilter
	}

	private static final String GROUP_NUMBER_ATTR = "group";

	private static final Logger LOG = LoggerFactory.getLogger(ConfigContentHandler.class);
	private static final String MAPPING_NAMESPACE = "http://locima.com/xml2csv/MappingConfiguration";
	private static final String MAPPING_ROOT_ATTR = "mappingRoot";
	private static final String MAX_VALUES_ATTR = "minOccurs";
	private static final String MIN_VALUES_ATTR = "maxOccurs";
	private static final String MULTI_VALUE_BEHAVIOUR_ATTR = "behaviour";
	private static final String NAME_ATTR = "name";
	private static final String NAME_FORMAT_ATTR = "nameFormat";
	private static final String XPATH_ATTR = "xPath";

	private int currentGroupNumber;

	private Locator documentLocator;

	private Stack<IInputFilter> inputFilterStack;

	private MappingConfiguration mappingConfiguration;

	private Stack<MappingList> mappingListStack;

	/**
	 * Adds a filter to either the mapping configuration (if a top level filter) or the current parent filter (from {@link #inputFilterStack}.
	 *
	 * @param filter the filter to add, must not be null.
	 */
	private void addFilter(IInputFilter filter) {
		if (filter == null) {
			throw new ArgumentNullException("filter");
		}
		if (this.inputFilterStack.isEmpty()) {
			this.mappingConfiguration.addInputFilter(filter);
		} else {
			this.inputFilterStack.peek().addNestedFilter(filter);
		}
		this.inputFilterStack.push(filter);
	}

	/**
	 * Adds a column mapping to the current MappingList instance being defined.
	 *
	 * @param name the name of the column.
	 * @param xPath the XPath that should be executed to get the value of the column.
	 * @param predefinedNameFormat the name of one of the built-in styles (see {@link NameFormat} public members.
	 * @param groupNumber the group number that applies to this mapping.
	 * @param bespokeNameFormatFormat a bespoke style to use for this mapping.
	 * @param multiValueBehaviour defines what should happen when multiple values are found for a single evaluation on an element.
	 * @param minValueCount the minimum number of values that this mapping should output for a single evaluation on an element.
	 * @param minValueCount the maximum number of values that this mapping should output for a single evaluation on an element.
	 * @throws SAXException if an error occurs while parsing the XPath expression found (will wrap {@link XMLException}.
	 */
	private void addMapping(String name, String xPath, String predefinedNameFormat, String bespokeNameFormatFormat, int groupNumber,
					String multiValueBehaviour, int minValueCount, int maxValueCount) throws SAXException {
		MappingList current = this.mappingListStack.peek();
		NameFormat nameFormat = NameFormat.parse(predefinedNameFormat, bespokeNameFormatFormat, NameFormat.NO_COUNTS);
		String fieldName;
		if (StringUtil.isNullOrEmpty(name)) {
			LOG.debug("No name was specified for mapping, so XPath value is used instead {}", xPath);
			fieldName = xPath.replace('/', '_');
		} else {
			fieldName = name;
		}
		XPathValue compiledXPath;
		try {
			compiledXPath = XmlUtil.createXPathValue(current.getNamespaceMappings(), xPath);
		} catch (XMLException e) {
			throw getException(e, "Unable to add field %s as there was a problem with the XPath value \"%s\"", name, xPath);
		}

		int finalGroupNumber = groupNumber < 0 ? this.currentGroupNumber : groupNumber;
		Mapping mapping =
						new Mapping(current, fieldName, nameFormat, finalGroupNumber, MultiValueBehaviour.parse(multiValueBehaviour), compiledXPath,
										minValueCount, maxValueCount);
		current.add(mapping);
	}

	/**
	 * Configures the inline behaviour (the instance of {@link MappingConfiguration} is already initialised on {@link #startDocument()}.
	 *
	 * @param multiValueBehaviour the inline behaviour to observe, by default, for all child mappings.
	 */
	private void addMappingConfiguration(String predefinedNameFormat, String multiValueBehaviour) {
		this.mappingConfiguration.setDefaultMultiValueBehaviour(MultiValueBehaviour.parse(multiValueBehaviour));
		this.mappingConfiguration.setDefaultNameFormat(NameFormat.parse(predefinedNameFormat, null, NameFormat.NO_COUNTS));
		this.mappingListStack = new Stack<MappingList>();
	}

	/**
	 * Initialises a new MappingList object based on a Mapping element.
	 *
	 * @param mappingRoot The XPath expression that identifies the "root" elements for the mapping.
	 * @param outputName The name of the output that this set of mappings should be written to.
	 * @throws SAXException If any problems occur with the XPath in the mappingRoot attribute.
	 * @param predefinedNameFormat the name of one of the built-in styles (see {@link NameFormat} public members.
	 * @param multiValueBehaviour defines what should happen when multiple values are found for a single evaluation for this mapping.
	 * @param minValueCount the minimum number of values that this mapping should output for a single evaluation on an element.
	 * @param minValueCount the maximum number of values that this mapping should output for a single evaluation on an element.
	 * @throws SAXException if an error occurs while parsing the XPath expression found (will wrap {@link XMLException}.
	 */
	private void addMappingList(String mappingRoot, String outputName, String predefinedNameFormat, String multiValueBehaviour, int minValueCount,
					int maxValueCount) throws SAXException {
		// IMappingContainer parent = (this.mappingListStack.size() > 0) ? this.mappingListStack.peek() : null;
		MappingList newMapping = new MappingList(this.mappingConfiguration.getNamespaceMap());
		try {
			newMapping.setMappingRoot(mappingRoot);
		} catch (XMLException e) {
			throw getException(e, "Invalid XPath \"%s\" found in mapping root for mapping list", mappingRoot);
		}
		newMapping.setOutputName(outputName);
		newMapping.setMultiValueBehaviour(MultiValueBehaviour.parse(multiValueBehaviour));
		newMapping.setMinValueCount(minValueCount);
		newMapping.setMaxValueCount(maxValueCount);
		this.mappingListStack.push(newMapping);

		/*
		 * Increment the current group number so that all the children of this container have a default group that doesn't match any other child of
		 * another conatiner.
		 */
		this.currentGroupNumber++;
	}

	/**
	 * Adds a column mapping to the current MappingList instance being defined.
	 *
	 * @param name the name of the column.
	 * @param xPath the XPath that should be executed to get the value of the column.
	 * @param predefinedNameFormat the name of one of the built-in styles (see {@link NameFormat} public members.
	 * @param groupNumber the group number that applies to this mapping.
	 * @param bespokeNameFormatFormat a bespoke style to use for this mapping.
	 * @param multiValueBehaviour defines what should happen when multiple values are found for a single evaluation for this mapping.
	 * @throws SAXException if an error occurs while parsing the XPath expression found (will wrap {@link XMLException}.
	 */
	private void addPivotMapping(String name, String xPath, String predefinedNameFormat, String bespokeNameFormatFormat, int groupNumber,
					String multiValueBehaviour) throws SAXException {
		MappingList current = this.mappingListStack.peek();
		NameFormat nameFormat = NameFormat.parse(predefinedNameFormat, bespokeNameFormatFormat, NameFormat.NO_COUNTS);
		String fieldName;
		if (StringUtil.isNullOrEmpty(name)) {
			LOG.debug("No name was specified for mapping, so XPath value is used instead {}", xPath);
			fieldName = xPath.replace('/', '_');
		} else {
			fieldName = name;
		}
		XPathValue compiledXPath;
		try {
			compiledXPath = XmlUtil.createXPathValue(current.getNamespaceMappings(), xPath);
		} catch (XMLException e) {
			throw getException(e, "Unable to add field");
		}
		throw new UnsupportedOperationException("Haven't implemented pivot mappings yet!");
	}

	/**
	 * Checks to ensure that the {@link #mappingListStack} is empty.
	 *
	 * @throws SAXException if {@link #mappingListStack} is not empty.
	 */
	@Override
	public void endDocument() throws SAXException {
		if (!this.mappingListStack.empty()) {
			throw getException(null, "Mapping stack should be empty, contains %s elements!", this.mappingListStack.size());
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (LOG.isTraceEnabled()) {
			LOG.trace("endElement(URI={})(localName={})(qName={})", uri, localName, qName);
		}

		if (MAPPING_NAMESPACE.equals(uri)) {
			ElementNames elementName = getElementNameEnum(localName);
			switch (elementName) {
				case FileNameInputFilter:
					endInputFilter();
					break;
				case Filters:
					endFilters();
					break;
				case Mapping:
					break;
				case PivotMapping:
					break;
				case MappingConfiguration:
					break;
				case MappingList:
					endMappingList();
					break;
				case XPathInputFilter:
					endInputFilter();
					break;
				default:
					break;
			}
		}
	}

	/**
	 * Clears down the stack for filters.
	 *
	 * @throws SAXException if the input filter stack isn't empty. Indicates a bug in xml2csv.
	 */
	private void endFilters() throws SAXException {
		if (!this.inputFilterStack.empty()) {
			throw getException(null, "Input filter stack is not empty.  Bug in xml2csv.");
		}
		this.inputFilterStack = null;
	}

	/**
	 * Closes off this input filter definition by popping the {@link #inputFilterStack}.
	 */
	private void endInputFilter() {
		this.inputFilterStack.pop();
	}

	/**
	 * Closes off a mapping list, managing the stack of them (to supported nested MappingList occurrences.
	 */
	private void endMappingList() {
		IMappingContainer current = this.mappingListStack.pop();
		if (this.mappingListStack.size() > 0) {
			this.mappingListStack.peek().add(current);
		} else {
			this.mappingConfiguration.addMappings(current);
		}
	}

	private int getAttributeValueAsInt(Attributes atts, String attrName, int defaultValue) throws SAXException {
		String attrValueAsString = atts.getValue(attrName);
		if (StringUtil.isNullOrEmpty(attrValueAsString)) {
			LOG.debug("No value specified for {}, returning default value of {}", attrName, defaultValue);
			return defaultValue;
		}
		try {
			return Integer.parseInt(attrValueAsString);
		} catch (NumberFormatException nfe) {
			throw getException(nfe, "Invalid value for %s found: %s", attrName, attrValueAsString);
		}
	}

	/**
	 * Given an element's local name (namespace URI checking is left to the caller), return an enum value that can be used in switch statements to
	 * branch logic depending on the input.
	 *
	 * @param localName the local name of the element.
	 * @return an enum representation of the element's name
	 * @throws SAXException if the element is not recognised (indicates a bug in xml2csv).
	 */
	private ElementNames getElementNameEnum(String localName) throws SAXException {
		ElementNames elementName;
		try {
			elementName = Enum.valueOf(ElementNames.class, localName);
			return elementName;
		} catch (IllegalArgumentException iae) {
			throw getException(iae, "Unexpected element name found.  This is a bug in xml2csv because the schema validation "
							+ "should have caught this already");
		}
	}

	/**
	 * Creates an exception to be thrown by this content handler, ensuring that formatting is consistent and including locator information.
	 *
	 * @param inner the exception that caused this exception to be created. May be null.
	 * @param message the message formatting string.
	 * @param parameters parameters to the message formatting string.
	 * @return an exception, ready to be thrown.
	 */
	private SAXException getException(Exception inner, String message, Object... parameters) {
		String temp = String.format(message, parameters);
		XMLException de =
						new XMLException(inner, "Error parsing %s(%d,%d) %s", this.documentLocator.getSystemId(),
										this.documentLocator.getLineNumber(), this.documentLocator.getColumnNumber(), temp);
		SAXException se = new SAXException(de);
		return se;
	}

	/**
	 * Get all the mappings that have been found so far by this parser.
	 *
	 * @return a set of mappings, possibly empty and possible null if no files have been parsed.
	 */
	public MappingConfiguration getMappings() {
		return this.mappingConfiguration;
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		this.documentLocator = locator;
	}

	@Override
	public void startDocument() throws SAXException {
		this.mappingConfiguration = new MappingConfiguration();
	}

	/**
	 * Delegates to various helper methods that manage the opening tag of the following elements: MappingSet, Mapping or Field.
	 *
	 * @param uri the Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
	 * @param localName the local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param qName the qualified name (with prefix), or the empty string if qualified names are not available.
	 * @param atts the attributes attached to the element. If there are no attributes, it shall be an empty Attributes object. The value of this
	 *            object after startElement returns is undefined.
	 * @throws SAXException if any errors occur, usually caused by bad XPath expressions defined in the mapping input configuration. Typically wraps
	 *             {@link XMLException}
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (LOG.isTraceEnabled()) {
			LOG.trace("startElement(URI={})(localName={})(qName={})", uri, localName, qName);
			for (int i = 0; i < atts.getLength(); i++) {
				LOG.trace("Attr[{}](LocalName={})(QName={})(Type={})(URI={})(Value={})", i, atts.getLocalName(i), atts.getQName(i), atts.getType(i),
								atts.getURI(i), atts.getValue(i));
			}
		}

		ElementNames elementName = getElementNameEnum(localName);

		if (MAPPING_NAMESPACE.equals(uri)) {
			switch (elementName) {
				case Mapping:
					addMapping(atts.getValue(NAME_ATTR), atts.getValue(XPATH_ATTR), atts.getValue(NAME_FORMAT_ATTR), null,
									getAttributeValueAsInt(atts, GROUP_NUMBER_ATTR, -2), atts.getValue(MULTI_VALUE_BEHAVIOUR_ATTR),
									getAttributeValueAsInt(atts, MIN_VALUES_ATTR, 0), getAttributeValueAsInt(atts, MAX_VALUES_ATTR, 0));
					break;
				case PivotMapping:
					addPivotMapping(atts.getValue(NAME_ATTR), atts.getValue(XPATH_ATTR), atts.getValue(NAME_FORMAT_ATTR), null,
									getAttributeValueAsInt(atts, GROUP_NUMBER_ATTR, 0), atts.getValue(MULTI_VALUE_BEHAVIOUR_ATTR));
					break;
				case MappingList:
					addMappingList(atts.getValue(MAPPING_ROOT_ATTR), atts.getValue(NAME_ATTR), atts.getValue(NAME_FORMAT_ATTR),
									atts.getValue(MULTI_VALUE_BEHAVIOUR_ATTR), getAttributeValueAsInt(atts, MIN_VALUES_ATTR, 0),
									getAttributeValueAsInt(atts, MAX_VALUES_ATTR, 0));
					break;
				case MappingConfiguration:
					addMappingConfiguration(atts.getValue(NAME_FORMAT_ATTR), atts.getValue(MULTI_VALUE_BEHAVIOUR_ATTR));
					break;
				case Filters:
					startFilters();
					break;
				case XPathInputFilter:
					startXPathFilter(atts.getValue("xPath"));
					break;
				case FileNameInputFilter:
					startFileNameFilter(atts.getValue("fileNameRegex"));
					break;
				default:
					LOG.warn("Ignoring element ({}):{} as it isn't supported in this version of xml2csv", uri, localName);
			}
		} else {
			LOG.warn("Ignoring element ({}):{} as it is outside of of the mapping namespace {}", uri, localName, MAPPING_NAMESPACE);
		}
	}

	/**
	 * Adds filters to the mapping configuration.
	 *
	 * @param fileNameRegex a regular expression to match against the filename. May be null.
	 * @throws SAXException If any errors occur whilst adding the filters.
	 */
	private void startFileNameFilter(String fileNameRegex) throws SAXException {
		try {
			IInputFilter filter = new FileNameInputFilter(fileNameRegex);
			addFilter(filter);
		} catch (PatternSyntaxException pse) {
			throw getException(pse, "Invalid Regular Expression {} specified for input filter.", fileNameRegex);
		}
	}

	/**
	 * When a new set of filters are found, ensure that stack is empty.
	 *
	 * @throws SAXException if {@link #inputFilterStack} isn't null.
	 */
	private void startFilters() throws SAXException {
		if (this.inputFilterStack != null) {
			throw getException(null, "New Filter set found, but existing filter set not tidied up.  Bug in xml2csv");
		}
		this.inputFilterStack = new Stack<IInputFilter>();
	}

	/**
	 * Track all namespace declarations that aren't related to the XSD language or the mapping schema. These are required and may be used in the XPath
	 * mappings.
	 *
	 * @param prefix The prefix that will be used within XPath statements in the configuration.
	 * @param uri The URI that this namespace maps on to.
	 * @throws SAXException if a duplicate namespace prefix was found within the configuration file. (Will contain a nested
	 *             {@link FileParserException}.)
	 */
	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		try {
			LOG.info("startPrefixMapping(Prefix={},Uri={})", prefix, uri);
			String finalPrefix = (StringUtil.isNullOrEmpty(uri)) ? XMLConstants.DEFAULT_NS_PREFIX : prefix;
			this.mappingConfiguration.addNamespaceMapping(finalPrefix, uri);
		} catch (FileParserException fpe) {
			throw getException(fpe, "Duplicate namespace mapping found in configuration");
		}
	}

	/**
	 * Adds filters to the mapping configuration.
	 *
	 * @param xPath an XPath value to match within the document. May be null.
	 * @throws SAXException If any errors occur whilst adding the filters.
	 */
	private void startXPathFilter(String xPath) throws SAXException {
		try {
			IInputFilter filter = new XPathInputFilter(this.mappingConfiguration.getNamespaceMap(), xPath);
			addFilter(filter);
		} catch (XMLException e) {
			throw getException(e, "Unable to parse XPath {} specified for input filter.");
		}
	}

}