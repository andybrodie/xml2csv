package com.locima.xml2csv.inputparser.xml;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.inputparser.FileParserException;
import com.locima.xml2csv.inputparser.IConfigParser;
import com.locima.xml2csv.inputparser.MappingsSet;

/**
 * Reads configuration from an XML input file.
 */
public class XmlFileParser implements IConfigParser {

	/**
	 * Converts a filename to a URL, suitable for use with the built-in SAX Parser.
	 *
	 * @param filename the filename of a file. Must not be null.
	 * @return A URL.
	 */
	private static String convertToFileURL(String filename) {
		String path = new File(filename).getAbsolutePath();
		if (File.separatorChar != '/') {
			path = path.replace(File.separatorChar, '/');
		}

		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return "file:" + path;
	}

	private MappingsSet mappings;

	@Override
	public MappingsSet getMappings() {
		return this.mappings;
	}

	private SAXParser getParser() throws SAXException, ParserConfigurationException {
		final String schemaUri = "http://locima.com/xml2csv";
		final String schemaName = "com/locima/xml2csv/inputparser/xml/MappingSet.xsd";

		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		// note that if your XML already declares the XSD to which it has to conform, then there's no need to create a validator from a Schema object
		Source schemaFile = new StreamSource(getClass().getClassLoader().getResourceAsStream(schemaName));
		Schema schema = factory.newSchema(schemaFile);

		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		spf.setSchema(schema);
		return spf.newSAXParser();
	}

	@Override
	public void load(List<File> inputConfigFiles) throws FileParserException, XMLException {

		ConfigContentHandler handler = new ConfigContentHandler();
		try {
			SAXParser saxParser = getParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setErrorHandler(new BasicErrorHandler());
			xmlReader.setContentHandler(handler);
			for (File f : inputConfigFiles) {
				String fileUrl = convertToFileURL(f.getAbsolutePath());
				xmlReader.parse(fileUrl);
			}

		} catch (SAXException | ParserConfigurationException | IOException ex) {
			throw new XMLException(ex, "XML parser failed while parsing input configuration file.");
		}
		this.mappings = handler.getMappings();
	}

}
