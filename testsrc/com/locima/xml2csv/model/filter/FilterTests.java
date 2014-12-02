package com.locima.xml2csv.model.filter;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;

import net.sf.saxon.s9api.XdmNode;

import org.junit.Test;

import com.locima.xml2csv.TestHelpers;
import com.locima.xml2csv.configuration.filter.FileNameInputFilter;
import com.locima.xml2csv.configuration.filter.IInputFilter;
import com.locima.xml2csv.configuration.filter.XPathInputFilter;
import com.locima.xml2csv.extractor.DataExtractorException;

public class FilterTests {

	private void checkAllInputFileFilterInputs(IInputFilter filter, boolean expectedResult, String... filenames) {
		for (String filename : filenames) {
			File f = new File(filename);
			assertEquals(expectedResult, filter.include(f));
		}
	}

	private void checkAllXmlInputs(IInputFilter filter, boolean expectedResult, XdmNode... docs) throws DataExtractorException {
		for (XdmNode doc : docs) {
			assertEquals(expectedResult, filter.include(doc));
		}
	}

	@Test
	public void fileNameTest() {
		FileNameInputFilter filter = new FileNameInputFilter("\\.xml$");
		checkAllInputFileFilterInputs(filter, true, "wibble.xml", "test.xml", ".xml");
		checkAllInputFileFilterInputs(filter, false, "wibble.xml ", "wibble.xml.bak", "wibble.bak");
	}

	@Test
	public void nestedTest() {
		FileNameInputFilter xmlFilter = new FileNameInputFilter("\\.xml$");
		FileNameInputFilter andyFilter = new FileNameInputFilter("a");
		xmlFilter.addNestedFilter(andyFilter);
		FileNameInputFilter tomFilter = new FileNameInputFilter("b");
		andyFilter.addNestedFilter(tomFilter);

		checkAllInputFileFilterInputs(xmlFilter, false, "a.xml", "b.xml", "c.xml", "ab.bak", "ab.xml2");
		checkAllInputFileFilterInputs(xmlFilter, true, "ba.xml", "ab.xml", "abba.xml", "ab.xml.bak.xml");
	}

	@Test
	public void xPathTest() throws Exception {
		XPathInputFilter filter = new XPathInputFilter(null, "/Test");
		XdmNode doc1 = TestHelpers.createDocument("<Test><Nested attr=\"Value\">Text</Nested></Test>");
		XdmNode doc2 = TestHelpers.createDocument("<Test/>");
		XdmNode doc3 = TestHelpers.createDocument("<Test2/>");
		XdmNode doc4 = TestHelpers.createDocument("<Test2><Test/></Test2>");
		checkAllXmlInputs(filter, true, doc1, doc2);
		checkAllXmlInputs(filter, false, doc3, doc4);
	}

	@Test
	public void xPathWithNamespaceTest() throws Exception {
		Map<String, String> nsMap = new HashMap<String, String>();
		nsMap.put("a", "http://example.com");
		XPathInputFilter filter = new XPathInputFilter(nsMap, "/a:Test");
		XdmNode doc1 = TestHelpers.createDocument("<a:Test xmlns:a=\"http://example.com\"><Nested attr=\"Value\">Text</Nested></a:Test>");
		XdmNode doc2 = TestHelpers.createDocument("<Test xmlns=\"http://example.com\"/>");
		XdmNode doc3 = TestHelpers.createDocument("<Test/>");
		XdmNode doc4 = TestHelpers.createDocument("<Test><Test xmlns=\"http://example.com\"/></Test>");
		checkAllXmlInputs(filter, true, doc1, doc2);
		checkAllXmlInputs(filter, false, doc3, doc4);

		nsMap = new HashMap<String, String>();
		nsMap.put(XMLConstants.DEFAULT_NS_PREFIX, "http://example.com");
		filter = new XPathInputFilter(nsMap, "/Test");
	}
}
