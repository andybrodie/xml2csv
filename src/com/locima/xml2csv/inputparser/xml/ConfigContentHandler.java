package com.locima.xml2csv.inputparser.xml;

import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.extractor.NameToXPathMappings;
import com.locima.xml2csv.inputparser.MappingsSet;

/**
 * The SAX Content Handler for input XML files.
 */
public class ConfigContentHandler extends DefaultHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigContentHandler.class);

	private String defaultSchemaNamespace;

	private MappingsSet mappingSet;

	private Stack<NameToXPathMappings> mappingStack;

	/**
	 * Adds a field definition to the current NameToXPathMappings instance being defined.
	 * @param name
	 * @param xPath
	 * @throws SAXException
	 */
	private void addField(String name, String xPath) throws SAXException {
		NameToXPathMappings current = this.mappingStack.peek();
		try {
			current.put(name, this.defaultSchemaNamespace, xPath);
		} catch (XMLException e) {
			throw new SAXException("Unable to add field", e);
		}
	}

	private void addMappingSet(String schemaNamespace) {
		this.mappingSet = new MappingsSet();
		this.mappingStack = new Stack<NameToXPathMappings>();
		this.defaultSchemaNamespace = schemaNamespace;
	}

	@Override
	public void endDocument() throws SAXException {
		if (!this.mappingStack.empty()) {
			throw new SAXException("Mapping stack should be empty, contains " + this.mappingStack.size() + " elements!");
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (LOG.isTraceEnabled()) {
			LOG.trace("endElement(URI={})(localName={})(qName={})", uri, localName, qName);
		}
		if ("Mapping".equals(qName)) {
			NameToXPathMappings current = this.mappingStack.pop();
			this.mappingSet.add(current);
		}
	}

	public MappingsSet getMappings() {
		return this.mappingSet;
	}

	/**
	 * Delegates to various helper methods that manage the opening tag of the following elements: MappingSet, Mapping or Field.
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

		if ("Field".equals(qName)) {
			addField(atts.getValue("name"), atts.getValue("xPath"));
		} else if ("Mapping".equals(qName)) {
			startMapping(atts.getValue("mappingRoot"), atts.getValue("outputName"));
		} else if ("MappingSet".equals(qName)) {
			addMappingSet(atts.getValue("inputSchema"));
		} else {
			LOG.warn("Ignoring element as I wasn't expecting it or wasn't using it.");
		}
	}

	/**
	 * Initialises a new NameToXPathMappings object based on a Mapping element.
	 * @param mappingRoot The XPath expression that identifies the "root" elements for the mapping.
	 * @param outputName The name of the output that this set of mappings should be written to.
	 * @throws SAXException If any problems occur with the XPath in the mappingRoot attribute.
	 */
	private void startMapping(String mappingRoot, String outputName) throws SAXException {
		NameToXPathMappings newMapping = new NameToXPathMappings();
		try {
			newMapping.setMappingRoot(this.defaultSchemaNamespace, mappingRoot);
		} catch (XMLException e) {
			throw new SAXException("Invalid XPath found in mapping root", e);
		}
		newMapping.setName(outputName);
		this.mappingStack.push(newMapping);
	}

}