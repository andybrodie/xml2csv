package com.locima.xml2csv.inputparser.xml;

import java.util.Stack;

import javax.xml.XMLConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.locima.xml2csv.StringUtil;
import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.inputparser.FileParserException;
import com.locima.xml2csv.inputparser.IMappingContainer;
import com.locima.xml2csv.inputparser.InlineFormat;
import com.locima.xml2csv.inputparser.MappingConfiguration;
import com.locima.xml2csv.inputparser.MappingList;

/**
 * The SAX Content Handler for input XML files.
 */
public class ConfigContentHandler extends DefaultHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigContentHandler.class);

	private static final String MAPPING_CONFIGURATION_NAME = "MappingConfiguration";

	private static final String MAPPING_LIST_NAME = "MappingList";

	private static final String MAPPING_NAME = "Mapping";

	private static final String MAPPING_NAMESPACE = "http://locima.com/xml2csv/MappingConfiguration";

	private Locator documentLocator;
	private MappingConfiguration mappingConfiguration;
	private Stack<MappingList> mappingListStack;

	/**
	 * Adds a column mapping to the current MappingList instance being defined.
	 *
	 * @param name the name of the column.
	 * @param xPath the XPath that should be executed to get the value of the column.
	 * @param inlineStyleName the name of one of the built-in styles (see {@link InlineFormat} public members.
	 * @param inlineStyleFormat a bespoke style to use for this mapping.
	 * @throws SAXException if an error occurs while parsing the XPath expression found (will wrap {@link XMLException}.
	 */
	private void addMapping(String name, String xPath, String inlineStyleName, String inlineStyleFormat) throws SAXException {
		MappingList current = this.mappingListStack.peek();
		try {
			InlineFormat format = getFormat(inlineStyleName, inlineStyleFormat);
			current.put(name, xPath, format);
		} catch (XMLException e) {
			throw getException(e, "Unable to add field");
		}
	}

	/**
	 * Checks to ensure that the {@link #mappingListStack} is empty.
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
		if (MAPPING_NAMESPACE.equals(uri) && MAPPING_LIST_NAME.equals(localName)) {
			IMappingContainer current = this.mappingListStack.pop();
			if (this.mappingListStack.size() > 0) {
				this.mappingListStack.peek().add(current);
			} else {
				this.mappingConfiguration.addMappings(current);
			}
		}
	}

	// /**
	// * Returns the string value specified for an XSD boolean type as a Java boolean.
	// *
	// * @param value the value found in an XML attribute.
	// * @return <code>true</code> if the values <code>true</code> or <code>1</code> are passed, false otherwise.
	// */
	// private boolean getBoolean(String value) {
	// return ("true".equals(value) || "1".equals(value));
	// }

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
	 * Parse the inline style name or specified format in to an {@link InlineFormat} instance.
	 *
	 * @param inlineStyleName the name of a specific style.
	 * @param inlineStyleFormat if none of the built-in styles (specified by name) are suitable, this allows a custom style to be defined.
	 * @return an inline format, or null if one could not be determined.
	 */
	private InlineFormat getFormat(String inlineStyleName, String inlineStyleFormat) {
		InlineFormat format;
		if (inlineStyleFormat != null) {
			format = new InlineFormat(inlineStyleFormat);
		} else if (inlineStyleName != null) {
			if ("NoCounts".equals(inlineStyleName)) {
				format = InlineFormat.NO_COUNTS;
			} else if ("WithCount".equals(inlineStyleName)) {
				format = InlineFormat.WITH_COUNT;
			} else if ("WithParentCount".equals(inlineStyleName)) {
				format = InlineFormat.WITH_PARENT_COUNT;
			} else if ("WithCountAndParentCount".equals(inlineStyleName)) {
				format = InlineFormat.WITH_COUNT_AND_PARENT_COUNT;
			} else if ("Custom".equals(inlineStyleName)) {
				format = new InlineFormat(inlineStyleFormat);
			} else {
				throw new IllegalStateException(
								"Unknown format found, this means that the XSD is wrong as it's permitted a value that isn't supported.");
			}
		} else {
			format = null;
		}
		return format;
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
		this.mappingListStack = new Stack<MappingList>();
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

		if (MAPPING_NAMESPACE.equals(uri) && MAPPING_NAME.equals(localName)) {
			addMapping(atts.getValue("name"), atts.getValue("xPath"), atts.getValue("inlineStyle"), atts.getValue("inlineFormat"));
		} else if (MAPPING_NAMESPACE.equals(uri) && MAPPING_LIST_NAME.equals(localName)) {
			startMappingList(atts.getValue("mappingRoot"), atts.getValue("name"));
		} else if (MAPPING_NAMESPACE.equals(uri) && MAPPING_CONFIGURATION_NAME.equals(localName)) {
			LOG.info("Found start of MappingConfiguration");
		} else {
			LOG.warn("Ignoring element ({}):{} as I wasn't expecting it or wasn't using it.", uri, localName);
		}
	}

	/**
	 * Initialises a new MappingList object based on a Mapping element.
	 *
	 * @param mappingRoot The XPath expression that identifies the "root" elements for the mapping.
	 * @param outputName The name of the output that this set of mappings should be written to.
	 * @throws SAXException If any problems occur with the XPath in the mappingRoot attribute.
	 */
	private void startMappingList(String mappingRoot, String outputName) throws SAXException {
		IMappingContainer parent = (this.mappingListStack.size() > 0) ? this.mappingListStack.peek() : null;
		MappingList newMapping = new MappingList(parent, this.mappingConfiguration.getNamespaceMap());
		try {
			newMapping.setMappingRoot(mappingRoot);
		} catch (XMLException e) {
			throw getException(e, "Invalid XPath \"%s\" found in mapping root", mappingRoot);
		}
		newMapping.setOutputName(outputName);
		this.mappingListStack.push(newMapping);
	}

	/**
	 * Track all namespace declarations that aren't related to the XSD language or the mapping schema. These are required and may be used in the XPath
	 * mappings.
	 *
	 * @param prefix The prefix that will be used within XPath statements in the configuration.
	 * @param uri The URI that this namespace maps on to.
	 * @throws SAXException if a duplicate namespace prefix was found within the configuration file. (Will contain a nested
	 * {@link FileParserException}.)
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

}