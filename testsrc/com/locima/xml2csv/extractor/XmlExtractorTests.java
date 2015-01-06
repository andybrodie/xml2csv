package com.locima.xml2csv.extractor;

import static org.junit.Assert.fail;

import java.io.StringReader;
import java.security.CodeSource;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

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

import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.configuration.Mapping;
import com.locima.xml2csv.configuration.MappingConfiguration;
import com.locima.xml2csv.configuration.MappingList;
import com.locima.xml2csv.configuration.MultiValueBehaviour;
import com.locima.xml2csv.configuration.NameFormat;
import com.locima.xml2csv.configuration.XPathValue;
import com.locima.xml2csv.extractor.XmlDataExtractor;
import com.locima.xml2csv.output.MockOutputManager;
import com.locima.xml2csv.util.XmlUtil;

public class XmlExtractorTests {

	private static final Logger LOG = LoggerFactory.getLogger(XmlExtractorTests.class);

	private static String getJaxpImplementationInfo(String componentName, Class<?> componentClass) {
		CodeSource source = componentClass.getProtectionDomain().getCodeSource();
		return MessageFormat.format("{0} implementation: {1} loaded from: {2}", componentName, componentClass.getName(),
						source == null ? "Java Runtime" : source.getLocation());
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	private Processor saxonProcessor;

	private void addMapping(MappingList mappings, Map<String, String> prefixUriMap, String baseName, int groupNumber, String valueXPathExpression)
					throws XMLException {
		XPathValue valueXPath = XmlUtil.createXPathValue(prefixUriMap, valueXPathExpression);
		Mapping m = new Mapping(mappings, baseName, NameFormat.NO_COUNTS, groupNumber, MultiValueBehaviour.LAZY, valueXPath, 0, 0);
		mappings.add(m);
	}

	private void addMapping(MappingList mappings, Map<String, String> prefixUriMap, String baseName, int groupNumber, MultiValueBehaviour mvb,
					String valueXPathExpression) throws XMLException {
		XPathValue valueXPath = XmlUtil.createXPathValue(prefixUriMap, valueXPathExpression);
		Mapping m = new Mapping(mappings, baseName, NameFormat.NO_COUNTS, groupNumber, mvb, valueXPath, 0, 0);
		mappings.add(m);
	}

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
		this.saxonProcessor = XmlUtil.getProcessor();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBasicMappingsWithRoot() throws Exception {
		MappingConfiguration config = new MappingConfiguration();

		MappingList parents = new MappingList();
		parents.setOutputName("Parents");
		parents.setMappingRoot("/root/parent");
		parents.setMultiValueBehaviour(MultiValueBehaviour.LAZY);
		addMapping(parents, null, "data", 1, "data");
		config.addMappings(parents);

		XmlDataExtractor extractor = new XmlDataExtractor();
		extractor.setMappingConfiguration(config);

		MockOutputManager om = new MockOutputManager();
		om.addExpectedResult("Parents", "ParentData1");
		om.addExpectedResult("Parents", "ParentData2");

		XdmNode testDoc = createFromString("<root><parent><data>ParentData1</data></parent><parent><data>ParentData2</data></parent></root>");

		extractor.extractTo(testDoc, om);
		om.close();
	}

	@Test
	public void testMultipleBasicMappingsWithRoot() throws Exception {
		MappingConfiguration config = new MappingConfiguration();

		MappingList parents = new MappingList();
		parents.setOutputName("Parents");
		parents.setMappingRoot("/root/parent");
		parents.setMultiValueBehaviour(MultiValueBehaviour.LAZY);
		addMapping(parents, null, "data", 1, "data");
		config.addMappings(parents);

		MappingList children = new MappingList();
		children.setOutputName("Children");
		children.setMappingRoot("/root/parent/child");
		children.setMultiValueBehaviour(MultiValueBehaviour.LAZY);
		addMapping(children, null, "data", 1, "data");
		config.addMappings(children);

		XmlDataExtractor extractor = new XmlDataExtractor();
		extractor.setMappingConfiguration(config);

		MockOutputManager om = new MockOutputManager();
		om.addExpectedResult("Parents", "ParentData1");
		om.addExpectedResult("Parents", "ParentData2");
		om.addExpectedResult("Children", "ParentData1Child1");
		om.addExpectedResult("Children", "ParentData1Child2");
		om.addExpectedResult("Children", "ParentData2Child1");
		om.addExpectedResult("Children", "ParentData2Child2");

		XdmNode testDoc =
						createFromString("<root><parent><data>ParentData1</data>" + "<child><data>ParentData1Child1</data></child>"
										+ "<child><data>ParentData1Child2</data></child></parent>" + "<parent><data>ParentData2</data>"
										+ "<child><data>ParentData2Child1</data></child>"
										+ "<child><data>ParentData2Child2</data></child></parent></root>");

		extractor.extractTo(testDoc, om);
		/*
		 * We expected 2 and 4 for the top level mappings because there's a total of 2 parents and 4 children. However, each parent and child only
		 * ever has 1 data item. Hence: 2, 1, 4, 1
		 */
		// assertMappingInstanceCountsCorrect(config, 2, 1, 4, 1);

		om.close();
	}

	@Test
	public void testMaxValueMapping() throws Exception {
		MappingList mappings = new MappingList();
		mappings.setMultiValueBehaviour(MultiValueBehaviour.LAZY);
		mappings.setOutputName("Test");

		Mapping m =
						new Mapping(mappings, "Name", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.GREEDY, XmlUtil.createXPathValue(null,
										"person/name"), 0, 0);
		mappings.add(m);
		m = new Mapping(mappings, "Age", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.GREEDY, XmlUtil.createXPathValue(null, "person/age"), 0, 1);
		mappings.add(m);
		m =
						new Mapping(mappings, "Address", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.GREEDY, XmlUtil.createXPathValue(null,
										"person/address"), 0, 2);
		mappings.add(m);

		MappingConfiguration s = new MappingConfiguration();
		s.addMappings(mappings);

		XmlDataExtractor x = new XmlDataExtractor();
		x.setMappingConfiguration(s);

		MockOutputManager om = new MockOutputManager();
		om.addExpectedResult("Test", new String[] { "Andy", "Andy2", "Andy3", "21", "Home", "Away" });

		XdmNode testDoc =
						createFromString("<person><name>Andy</name><name>Andy2</name><name>Andy3</name>"
										+ "<age>21</age><age>22</age><age>23</age><address>Home</address><address>Away</address><address>Both</address></person>");

		x.extractTo(testDoc, om);
	}

	@Test
	public void testMinValueMapping() throws Exception {
		MappingList mappings = new MappingList();
		mappings.setOutputName("Test");
		mappings.setMultiValueBehaviour(MultiValueBehaviour.LAZY);

		Mapping m =
						new Mapping(mappings, "Name", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.GREEDY, XmlUtil.createXPathValue(null,
										"person/name"), 3, 0);
		mappings.add(m);
		m = new Mapping(mappings, "Age", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.GREEDY, XmlUtil.createXPathValue(null, "person/age"), 1, 0);
		mappings.add(m);
		m =
						new Mapping(mappings, "Address", NameFormat.NO_COUNTS, 0, MultiValueBehaviour.GREEDY, XmlUtil.createXPathValue(null,
										"person/address"), 0, 0);
		mappings.add(m);

		MappingConfiguration s = new MappingConfiguration();
		s.addMappings(mappings);

		XmlDataExtractor x = new XmlDataExtractor();
		x.setMappingConfiguration(s);

		MockOutputManager om = new MockOutputManager();
		om.addExpectedResult("Test", new String[] { "Andy", null, null, "21", "22", "Home" });

		XdmNode testDoc = createFromString("<person><name>Andy</name><age>21</age><age>22</age><address>Home</address></person>");

		x.extractTo(testDoc, om);
	}

	@Test
	public void testMultipleMappingsWithRoot() throws Exception {
		MappingList families = new MappingList();
		families.setMultiValueBehaviour(MultiValueBehaviour.LAZY);
		families.setOutputName("Families");
		families.setMappingRoot("/families/family");
		addMapping(families, null, "Name", 1, "name");

		MappingList familyMembers = new MappingList();
		familyMembers.setMultiValueBehaviour(MultiValueBehaviour.LAZY);
		addMapping(familyMembers, null, "Name", 2, "name");
		addMapping(familyMembers, null, "Age", 2, "age");
		addMapping(familyMembers, null, "Address", 2, "address");
		familyMembers.setOutputName("FamilyMembers");
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

		extractor.extractTo(testDoc, om);
		om.close();
	}

	@Test
	public void testSimpleMappings() throws Exception {
		MappingList mappings = new MappingList();
		mappings.setMultiValueBehaviour(MultiValueBehaviour.LAZY);
		addMapping(mappings, null, "Name", 1, "/person/name");
		addMapping(mappings, null, "Age", 1, "/person/age");
		addMapping(mappings, null, "Address", 1, "/person/address");
		mappings.setOutputName("Test");

		MappingConfiguration s = new MappingConfiguration();
		s.addMappings(mappings);

		XmlDataExtractor x = new XmlDataExtractor();
		x.setMappingConfiguration(s);

		MockOutputManager om = new MockOutputManager();
		om.addExpectedResult("Test", new String[] { "Andy", "21", "Home" });

		XdmNode testDoc = createFromString("<person><name>Andy</name><age>21</age><address>Home</address></person>");

		x.extractTo(testDoc, om);
	}

	@Test
	public void testSimpleMappingsWithNamespaces() throws Exception {
		Map<String, String> prefixUriMap = new HashMap<String, String>();

		prefixUriMap.put("a", "http://example.com/a");
		prefixUriMap.put("b", "http://example.com/b");

		MappingList mappings = new MappingList(prefixUriMap);
		mappings.setMultiValueBehaviour(MultiValueBehaviour.LAZY);
		addMapping(mappings, prefixUriMap, "Name", 1, "/a:person/b:name");
		addMapping(mappings, prefixUriMap, "Age", 1, "/a:person/b:age");
		addMapping(mappings, prefixUriMap, "Address", 1, "/a:person/b:address");
		mappings.setOutputName("Test");

		MappingConfiguration s = new MappingConfiguration();
		s.addMappings(mappings);

		XmlDataExtractor x = new XmlDataExtractor();
		x.setMappingConfiguration(s);

		MockOutputManager om = new MockOutputManager();
		om.addExpectedResult("Test", new String[] { "Andy", "21", "Home" });

		XdmNode testDoc =
						createFromString("<a:person xmlns:a=\"http://example.com/a\" xmlns:b=\"http://example.com/b\">"
										+ "<b:name>Andy</b:name><b:age>21</b:age><b:address>Home</b:address>" + "</a:person>");

		x.extractTo(testDoc, om);
	}

	@Test
	public void testSimpleMappingsWithRoot() throws Exception {
		MappingList mappings = new MappingList();
		mappings.setOutputName("Test");
		mappings.setMappingRoot("/personcollection/person");
		mappings.setMultiValueBehaviour(MultiValueBehaviour.LAZY);
		addMapping(mappings, null, "Name", 1, MultiValueBehaviour.GREEDY, "name");
		addMapping(mappings, null, "Age", 1, MultiValueBehaviour.GREEDY, "age");
		addMapping(mappings, null, "Address", 1, MultiValueBehaviour.GREEDY, "address");

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

		x.extractTo(testDoc, om);
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

		Processor processor = XmlUtil.getProcessor();
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
	
	@Test
	public void testMultiRecordLazyOutput() throws Exception {
		MappingList mappings = new MappingList();
		mappings.setOutputName("Test");
		mappings.setMultiValueBehaviour(MultiValueBehaviour.LAZY);
		mappings.setMappingRoot("/root/c");
		mappings.setMultiValueBehaviour(MultiValueBehaviour.DEFAULT);
		addMapping(mappings, null, "parent", 1, MultiValueBehaviour.LAZY, "../name");
		addMapping(mappings, null, "a1", 1, MultiValueBehaviour.LAZY, "a1");
		addMapping(mappings, null, "a2", 1, MultiValueBehaviour.LAZY, "a2");

		MappingConfiguration s = new MappingConfiguration();
		s.addMappings(mappings);

		XmlDataExtractor x = new XmlDataExtractor();
		x.setMappingConfiguration(s);

		MockOutputManager om = new MockOutputManager();
		om.addExpectedResult("Test", new String[] { "parent","c1a1","c1a2" });
		om.addExpectedResult("Test", new String[] { "parent","c2a1","c2a2" });

		XdmNode testDoc =
						createFromString("<root><name>parent</name><c><a1>c1a1</a1><a2>c1a2</a2></c><c><a1>c2a1</a1><a2>c2a2</a2></c></root>");

		x.extractTo(testDoc, om);
		om.close();
	}

}
