package com.locima.xml2csv.inputparser.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
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

	/**
	 * Create a SAX parser instance that is configured to validate against the schemas used within this application.
	 *
	 * @return A SAX Parser instance, never returns null (exceptions thrown for all failures).
	 * @throws SAXException If unable to create the parser.
	 * @throws ParserConfigurationException If unable to create the parser.
	 */
	private SAXParser getParser() throws SAXException, ParserConfigurationException {

		// Where the XSD file is within my application resources, just one so far, but others will follow.
		final String[] schemaResourceNames = new String[] {"com/locima/xml2csv/inputparser/xml/MappingSet.xsd" };

		// So far, so good.
		SAXParserFactory factory = SAXParserFactory.newInstance();

		// To enable schema validation, ensure you set validating to false. Yes, really.
		factory.setValidating(false);

		// Apparently, namespaces are a bit complicated, so override the default to ignore them.
		factory.setNamespaceAware(true);

		// Now tell it what language (using a magic string), as the parser can't work it out for itself,
		// as if XML files could declare what they are...
		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

		// Pass a set of schemas (schemata if you're feeling pedantic) to a method called setSchema <-- singular.
		factory.setSchema(schemaFactory.newSchema(getSchemasFromResourceNames(schemaResourceNames)));

		SAXParser parser = factory.newSAXParser();

		return parser;
	}

	/**
	 * Given an array of resource names for XSD files, this retrieves {@link Source} versions of all of them, by opening all the resources in turn.
	 * 
	 * @param schemaResourceNames a list of resource names, retrieves from the current class loader, of XSD files.
	 * @return an array of {@link Source} objects, suitable to use with {@link SAXParserFactory#setSchema(javax.xml.validation.Schema)}.
	 */
	private Source[] getSchemasFromResourceNames(String[] schemaResourceNames) {
		List<Source> schemas = new ArrayList<Source>(schemaResourceNames.length);
		ClassLoader cl = getClass().getClassLoader();
		for (String schemaResourceName : schemaResourceNames) {
			Source schemaSAXSource = new StreamSource(cl.getResourceAsStream(schemaResourceName));
			schemas.add(schemaSAXSource);
		}
		return schemas.toArray(new Source[0]);
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
		} catch (ParserConfigurationException | IOException ex) {
			throw new XMLException(ex, "XML parser failed while parsing input configuration file.");
		} catch (SAXException se) {
			if (se.getCause() instanceof XMLException) {
				throw (XMLException) se.getCause();
			} else {
				throw new XMLException(se, "XML parser failed while parsing input configuration file.");
			}
		}
		this.mappings = handler.getMappings();
	}

}
