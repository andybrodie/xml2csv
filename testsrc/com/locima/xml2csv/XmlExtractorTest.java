package com.locima.xml2csv;

import static com.locima.xml2csv.TestHelpers.assertMappingInstanceCountsCorrect;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.security.CodeSource;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.extractor.XmlDataExtractor;
import com.locima.xml2csv.inputparser.MappingConfiguration;
import com.locima.xml2csv.inputparser.MappingList;

public class XmlExtractorTest {

	private static final Logger LOG = LoggerFactory.getLogger(XmlExtractorTest.class);

	private static String getJaxpImplementationInfo(String componentName, Class<?> componentClass) {
		CodeSource source = componentClass.getProtectionDomain().getCodeSource();
		return MessageFormat.format("{0} implementation: {1} loaded from: {2}", componentName, componentClass.getName(),
						source == null ? "Java Runtime" : source.getLocation());
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	private Processor saxonProcessor;

	private XdmNode createFromString(String xmlAsString) {
		net.sf.saxon.s9api.DocumentBuilder docBuilder = this.saxonProcessor.newDocumentBuilder();
		XdmNode document = null;
		try {
			document = docBuilder.build(new StreamSource(new StringReader(xmlAsString)));
		} catch (SaxonApiException e) {
			e.printStackTrace();
			fail();
		}
		return document;
	}

	@Before
	public void setUp() throws Exception {
		this.saxonProcessor = SaxonProcessorManager.getProcessor();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBasicMappingsWithRoot() throws Exception {
		MappingList parents = new MappingList();
		parents.setName("Parents");
		parents.setMappingRoot("/root/parent");
		parents.put("data", "data");

		MappingList children = new MappingList();
		children.setName("Children");
		children.setMappingRoot("/root/parent/child");
		children.put("data", "data");

		MappingConfiguration config = new MappingConfiguration();
		config.addMappings(parents);
		config.addMappings(children);

		XmlDataExtractor extractor = new XmlDataExtractor();
		extractor.setMappingConfiguration(config);

		MockOutputManager om = new MockOutputManager();
		om.addExpectedResult("Parents", new String[] { "ParentData1" });
		om.addExpectedResult("Parents", new String[] { "ParentData2" });
		om.addExpectedResult("Children", new String[] { "ParentData1Child1" });
		om.addExpectedResult("Children", new String[] { "ParentData1Child2" });
		om.addExpectedResult("Children", new String[] { "ParentData2Child1" });
		om.addExpectedResult("Children", new String[] { "ParentData2Child2" });

		XdmNode testDoc =
						createFromString("<root><parent><data>ParentData1</data>" + "<child><data>ParentData1Child1</data></child>"
										+ "<child><data>ParentData1Child2</data></child></parent>" + "<parent><data>ParentData2</data>"
										+ "<child><data>ParentData2Child1</data></child>"
										+ "<child><data>ParentData2Child2</data></child></parent></root>");

		extractor.convert(testDoc, om);
		/*
		 * We expected 2 and 4 for the top level mappings because there's a total of 2 parents and 4 children. However, each parent and child only
		 * ever has 1 data item. Hence: 2, 1, 4, 1
		 */
		assertMappingInstanceCountsCorrect(config, 2, 1, 4, 1);

		om.close();
	}

	@Test
	public void testMultipleMappingsWithRoot() throws Exception {
		MappingList families = new MappingList();
		families.setName("Families");
		families.setMappingRoot("/families/family");
		families.put("Name", "name");

		MappingList familyMembers = new MappingList();
		familyMembers.put("Name", "name");
		familyMembers.put("Age", "age");
		familyMembers.put("Address", "address");
		familyMembers.setName("FamilyMembers");
		familyMembers.setMappingRoot("/families/family/member");

		MappingConfiguration set = new MappingConfiguration();
		set.addMappings(families);
		set.addMappings(familyMembers);

		XmlDataExtractor extractor = new XmlDataExtractor();
		extractor.setMappingConfiguration(set);

		MockOutputManager om = new MockOutputManager();
		om.addExpectedResult("Families", new String[] { "Brodie" });
		om.addExpectedResult("Families", new String[] { "Test" });
		om.addExpectedResult("FamilyMembers", new String[] { "Andy", "21", "Home" });
		om.addExpectedResult("FamilyMembers", new String[] { "Emma", "20", "Away" });
		om.addExpectedResult("FamilyMembers", new String[] { "Bob", "30", "Home" });
		om.addExpectedResult("FamilyMembers", new String[] { "Zig", "31", "Away" });

		XdmNode testDoc =
						createFromString("<families><family>" + "<name>Brodie</name>"
										+ "<member><name>Andy</name><age>21</age><address>Home</address></member>"
										+ "<member><name>Emma</name><age>20</age><address>Away</address></member>" + "</family><family>"
										+ "<name>Test</name>" + "<member><name>Bob</name><age>30</age><address>Home</address></member>"
										+ "<member><name>Zig</name><age>31</age><address>Away</address></member>" + "</family></families>");

		extractor.convert(testDoc, om);
		om.close();
	}

	@Test
	public void testRealXmlFile() throws Exception {
		// TODO Need to find some XML!!!
	}

	@Test
	public void testSimpleMappings() throws Exception {
		MappingList mappings = new MappingList();
		mappings.put("Name", "/person/name");
		mappings.put("Age", "/person/age");
		mappings.put("Address", "/person/address");
		mappings.setName("Test");

		MappingConfiguration s = new MappingConfiguration();
		s.addMappings(mappings);

		XmlDataExtractor x = new XmlDataExtractor();
		x.setMappingConfiguration(s);

		MockOutputManager om = new MockOutputManager();
		om.addExpectedResult("Test", new String[] { "Andy", "21", "Home" });

		XdmNode testDoc = createFromString("<person><name>Andy</name><age>21</age><address>Home</address></person>");

		x.convert(testDoc, om);
	}

	@Test
	public void testSimpleMappingsWithNamespaces() throws Exception {
		Map<String, String> prefixUriMap = new HashMap<String, String>();

		prefixUriMap.put("a", "http://example.com/a");
		prefixUriMap.put("b", "http://example.com/b");

		MappingList mappings = new MappingList(prefixUriMap);
		mappings.put("Name", "/a:person/b:name");
		mappings.put("Age", "/a:person/b:age");
		mappings.put("Address", "/a:person/b:address");
		mappings.setName("Test");

		MappingConfiguration s = new MappingConfiguration();
		s.addMappings(mappings);

		XmlDataExtractor x = new XmlDataExtractor();
		x.setMappingConfiguration(s);

		MockOutputManager om = new MockOutputManager();
		om.addExpectedResult("Test", new String[] { "Andy", "21", "Home" });

		XdmNode testDoc =
						createFromString("<a:person xmlns:a=\"http://example.com/a\" xmlns:b=\"http://example.com/b\">"
										+ "<b:name>Andy</b:name><b:age>21</b:age><b:address>Home</b:address>" + "</a:person>");

		x.convert(testDoc, om);
	}

	@Test
	public void testSimpleMappingsWithRoot() throws Exception {
		MappingList mappings = new MappingList();
		mappings.put("Name", "name");
		mappings.put("Age", "age");
		mappings.put("Address", "address");
		mappings.setName("Test");
		mappings.setMappingRoot("/personcollection/person");

		MappingConfiguration s = new MappingConfiguration();
		s.addMappings(mappings);

		XmlDataExtractor x = new XmlDataExtractor();
		x.setMappingConfiguration(s);

		MockOutputManager om = new MockOutputManager();
		om.addExpectedResult("Test", new String[] { "Andy", "21", "Home" });
		om.addExpectedResult("Test", new String[] { "Emma", "20", "Away" });

		XdmNode testDoc =
						createFromString("<personcollection><person><name>Andy</name><age>21</age><address>Home</address></person>"
										+ "<person><name>Emma</name><age>20</age><address>Away</address></person></personcollection>");

		x.convert(testDoc, om);
		om.close();
	}

	@Test
	public void textSaxonXPathApi() throws Exception {

		String xml =
						"<a:person xmlns:a=\"http://example.com/a\" xmlns:b=\"http://example.com/b\">"
										+ "<b:name>Andy</b:name><b:age>21</b:age><b:address>Home</b:address></a:person>";

		String saxonObjectModel = NamespaceConstant.OBJECT_MODEL_SAXON;
		System.setProperty("javax.xml.xpath.XPathFactory:" + saxonObjectModel, "net.sf.saxon.xpath.XPathFactoryImpl");
		XPathFactory xPathFactory = XPathFactory.newInstance(saxonObjectModel);

		LOG.info(getJaxpImplementationInfo("DocumentBuilderFactory", DocumentBuilderFactory.newInstance().getClass()));
		LOG.info(getJaxpImplementationInfo("XPathFactory", xPathFactory.getClass()));

		Processor processor = SaxonProcessorManager.getProcessor();
		XPathCompiler xPathCompiler = processor.newXPathCompiler();
		xPathCompiler.declareNamespace("", "http://example.com/a");
		xPathCompiler.declareNamespace("b", "http://example.com/b");
		XPathExecutable xPathExpr = xPathCompiler.compile("/person/b:name/text()");
		XPathSelector xpathSelector = xPathExpr.load();
		StreamSource s = new StreamSource(new StringReader(xml));
		net.sf.saxon.s9api.DocumentBuilder docBuilder = processor.newDocumentBuilder();
		XdmNode document = docBuilder.build(s);
		xpathSelector.setContextItem(document);

		for (XdmItem item : xpathSelector) {
			LOG.info(item.toString());
		}

	}

}
