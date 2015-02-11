package com.locima.xml2csv.extractor;

import static com.locima.xml2csv.TestHelpers.addMapping;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.security.CodeSource;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.configuration.MappingConfiguration;
import com.locima.xml2csv.configuration.MappingList;
import com.locima.xml2csv.configuration.MultiValueBehaviour;
import com.locima.xml2csv.output.IExtractionResults;
import com.locima.xml2csv.util.XmlUtil;

public class ExtractionContextTests {

	private static final Logger LOG = LoggerFactory.getLogger(ExtractionContextTests.class);

	private static String getJaxpImplementationInfo(String componentName, Class<?> componentClass) {
		CodeSource source = componentClass.getProtectionDomain().getCodeSource();
		return MessageFormat.format("{0} implementation: {1} loaded from: {2}", componentName, componentClass.getName(),
						source == null ? "Java Runtime" : source.getLocation());
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	private Processor saxonProcessor;

	/**
	 * Verifies values in a {@link ContainerExtractionContext} tree.
	 *
	 * @param expectedValue the string value expected to be found at the <code>expectedIndex</code> values.
	 * @param expectedIndex an array of integers which identify the indices of search for within a tree of {@link ContainerExtractionContext}.
	 *            <p>
	 *            This is complicated, but makes the code to verify values short. There must always be an odd number of elements in the array and
	 *            there must always be 3 or more values (e.g. 3, 5, 7, 9, etc.) because we only pass a {@link ContainerExtractionContext} and never a
	 *            {@link MappingExtractionContext}. The <em>last</em> element in the array is the value contained in a leaf node extraction context (a
	 *            {@link MappingExtractionContext}). The rest of the array holds pairs of values. The first index in the pair (<code>i</code>) is the
	 *            value passed to the index accessor {@link ContainerExtractionContext#getChildren()}; this allows to search results with multiple
	 *            root nodes. The second index in the pair (<code>i+1</code>) is the index of the extraction context child beneath that root node.
	 * @param actual the {@link ContainerExtractionContext} to search.
	 */
	private void assertMappingValues(String expectedValue, int[] expectedIndex, ContainerExtractionContext actual) {
		IExtractionContext current = actual;

		int i = 0;
		int len = expectedIndex.length;
		while (i < len) {
			if (i == (len - 1)) {
				if (!(current instanceof MappingExtractionContext)) {
					Assert.fail(String.format("Expected MEC but found CEC when expectedIndex=%d out of %d.  MEC: %s", i, len, current));
				}
				MappingExtractionContext mec = (MappingExtractionContext) current;
				Assert.assertEquals(expectedValue, mec.getValueAt(expectedIndex[i]));
			} else {
				ContainerExtractionContext cec = (ContainerExtractionContext) current;
				List<List<IExtractionResults>> children = cec.getChildren();
				if (children.size() <= expectedIndex[i]) {
					Assert.fail(String.format("expectedIndex[%d]=%d but current CEC only has %d children (root nodes): %s.", i, expectedIndex[i],
									children.size(), cec));
				}
				List<IExtractionResults> list = children.get(expectedIndex[i]);
				if (list.size() <= expectedIndex[i + 1]) {
					Assert.fail(String.format("expectedIndex[%d]=%d but current CEC root result (%d) only has %d extraction contexts: %s.", i + 1,
									expectedIndex[i + 1], i, list.size(), cec));
				}
				current = (IExtractionContext) list.get(expectedIndex[i + 1]);
			}
			i += 2;
		}
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

	private ContainerExtractionContext evaluate(MappingList mappings, XdmNode testDoc) throws DataExtractorException {
		ContainerExtractionContext ctx = new ContainerExtractionContext(mappings, 0, 0);
		ctx.evaluate(testDoc);
		return ctx;
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
		MappingList parents = new MappingList();
		parents.setName("Parents");
		parents.setMappingRoot(XmlUtil.createXPathValue("/root/parent"));
		parents.setMultiValueBehaviour(MultiValueBehaviour.LAZY);
		addMapping(parents, "data", 1, "data");

		XdmNode testDoc = createFromString("<root><parent><data>ParentData1</data></parent><parent><data>ParentData2</data></parent></root>");

		ContainerExtractionContext ctx = evaluate(parents, testDoc);

		assertMappingValues("ParentData1", new int[] { 0, 0, 0 }, ctx);
		assertMappingValues("ParentData2", new int[] { 1, 0, 0 }, ctx);
	}

	@Test
	public void testMaxValueMapping() throws Exception {
		MappingList mappings = new MappingList();
		mappings.setMultiValueBehaviour(MultiValueBehaviour.LAZY);
		mappings.setName("Test");

		addMapping(mappings, "Name", 0, MultiValueBehaviour.GREEDY, "person/name", 0, 0);
		addMapping(mappings, "Age", 0, MultiValueBehaviour.GREEDY, "person/age", 0, 1);
		addMapping(mappings, "Address", 0, MultiValueBehaviour.GREEDY, "person/address", 0, 2);

		XdmNode testDoc =
						createFromString("<person><name>Andy</name><name>Andy2</name><name>Andy3</name>"
										+ "<age>21</age><age>22</age><age>23</age><address>Home</address><address>Away</address><address>Both</address></person>");

		ContainerExtractionContext ctx = evaluate(mappings, testDoc);
		assertMappingValues("Andy", new int[] { 0, 0, 0 }, ctx);
		assertMappingValues("Andy2", new int[] { 0, 0, 1 }, ctx);
		assertMappingValues("Andy3", new int[] { 0, 0, 2 }, ctx);
		assertMappingValues("21", new int[] { 0, 1, 0 }, ctx);
		assertMappingValues("Home", new int[] { 0, 2, 0 }, ctx);
		assertMappingValues("Away", new int[] { 0, 2, 1 }, ctx);

		// om.addExpectedResult("Test", new String[] { "Andy", "Andy2", "Andy3", "21", "Home", "Away" });
	}

	@Test
	public void testMultipleBasicMappingsWithRoot() throws Exception {

		MappingList parents = new MappingList();
		parents.setName("Parents");
		parents.setMappingRoot(XmlUtil.createXPathValue("/root/parent"));
		parents.setMultiValueBehaviour(MultiValueBehaviour.LAZY);
		addMapping(parents, "data", 1, "data");

		MappingList children = new MappingList();
		children.setName("Children");
		children.setMappingRoot(XmlUtil.createXPathValue("/root/parent/child"));
		children.setMultiValueBehaviour(MultiValueBehaviour.LAZY);
		addMapping(children, "data", 1, "data");

		XdmNode testDoc =
						createFromString("<root><parent><data>ParentData1</data>" + "<child><data>ParentData1Child1</data></child>"
										+ "<child><data>ParentData1Child2</data></child></parent>" + "<parent><data>ParentData2</data>"
										+ "<child><data>ParentData2Child1</data></child>"
										+ "<child><data>ParentData2Child2</data></child></parent></root>");

		ContainerExtractionContext ctx = evaluate(parents, testDoc);
		assertMappingValues("ParentData1", new int[] { 0, 0, 0 }, ctx);
		assertMappingValues("ParentData2", new int[] { 1, 0, 0 }, ctx);

		ctx = evaluate(children, testDoc);
		assertMappingValues("ParentData1Child1", new int[] { 0, 0, 0 }, ctx);
		assertMappingValues("ParentData1Child2", new int[] { 1, 0, 0 }, ctx);
		assertMappingValues("ParentData2Child1", new int[] { 2, 0, 0 }, ctx);
		assertMappingValues("ParentData2Child2", new int[] { 3, 0, 0 }, ctx);
	}

	@Test
	public void testMultipleMappingsWithRoot() throws Exception {
		MappingList families = new MappingList();
		families.setMultiValueBehaviour(MultiValueBehaviour.LAZY);
		families.setName("Families");
		families.setMappingRoot(XmlUtil.createXPathValue("/families/family"));
		addMapping(families, "Name", 1, "name");

		MappingList familyMembers = new MappingList();
		familyMembers.setMultiValueBehaviour(MultiValueBehaviour.LAZY);
		addMapping(familyMembers, "Name", 2, "name");
		addMapping(familyMembers, "Age", 2, "age");
		addMapping(familyMembers, "Address", 2, "address");
		familyMembers.setName("FamilyMembers");
		familyMembers.setMappingRoot(XmlUtil.createXPathValue("/families/family/member"));

		MappingConfiguration config = new MappingConfiguration();
		config.addContainer(families);
		config.addContainer(familyMembers);

		XdmNode testDoc =
						createFromString("<families><family>" + "<name>Brodie</name>"
										+ "<member><name>Andy</name><age>21</age><address>Home</address></member>"
										+ "<member><name>Emma</name><age>20</age><address>Away</address></member>" + "</family><family>"
										+ "<name>Test</name>" + "<member><name>Bob</name><age>30</age><address>Home</address></member>"
										+ "<member><name>Zig</name><age>31</age><address>Away</address></member>" + "</family></families>");

		ContainerExtractionContext ctx = evaluate(families, testDoc);

		// om.addExpectedResult("Families", new String[] { "Brodie" });
		// om.addExpectedResult("Families", new String[] { "Test" });
		assertMappingValues("Brodie", new int[] { 0, 0, 0 }, ctx);
		assertMappingValues("Test", new int[] { 1, 0, 0 }, ctx);

		ctx = evaluate(familyMembers, testDoc);

		assertMappingValues("Andy", new int[] { 0, 0, 0 }, ctx);
		assertMappingValues("21", new int[] { 0, 1, 0 }, ctx);
		assertMappingValues("Home", new int[] { 0, 2, 0 }, ctx);

		assertMappingValues("Emma", new int[] { 1, 0, 0 }, ctx);
		assertMappingValues("20", new int[] { 1, 1, 0 }, ctx);
		assertMappingValues("Away", new int[] { 1, 2, 0 }, ctx);

		assertMappingValues("Bob", new int[] { 2, 0, 0 }, ctx);
		assertMappingValues("30", new int[] { 2, 1, 0 }, ctx);
		assertMappingValues("Home", new int[] { 2, 2, 0 }, ctx);

		assertMappingValues("Zig", new int[] { 3, 0, 0 }, ctx);
		assertMappingValues("31", new int[] { 3, 1, 0 }, ctx);
		assertMappingValues("Away", new int[] { 3, 2, 0 }, ctx);

		// om.addExpectedResult("FamilyMembers", new String[] { "Andy", "21", "Home" });
		// om.addExpectedResult("FamilyMembers", new String[] { "Emma", "20", "Away" });
		// om.addExpectedResult("FamilyMembers", new String[] { "Bob", "30", "Home" });
		// om.addExpectedResult("FamilyMembers", new String[] { "Zig", "31", "Away" });
	}

	@Test
	public void testMultiRecordLazyOutput() throws Exception {
		MappingList mappings = new MappingList();
		mappings.setName("Test");
		mappings.setMultiValueBehaviour(MultiValueBehaviour.LAZY);
		mappings.setMappingRoot(XmlUtil.createXPathValue("/root/c"));
		mappings.setMultiValueBehaviour(MultiValueBehaviour.DEFAULT);
		addMapping(mappings, "parent", 1, MultiValueBehaviour.LAZY, "../name", 0, 0);
		addMapping(mappings, "a1", 1, MultiValueBehaviour.LAZY, "a1", 0, 0);
		addMapping(mappings, "a2", 1, MultiValueBehaviour.LAZY, "a2", 0, 0);

		XdmNode testDoc = createFromString("<root><name>parent</name><c><a1>c1a1</a1><a2>c1a2</a2></c><c><a1>c2a1</a1><a2>c2a2</a2></c></root>");

		ContainerExtractionContext ctx = evaluate(mappings, testDoc);
		assertMappingValues("parent", new int[] { 0, 0, 0 }, ctx);
		assertMappingValues("c1a1", new int[] { 0, 1, 0 }, ctx);
		assertMappingValues("c1a2", new int[] { 0, 2, 0 }, ctx);
		assertMappingValues("parent", new int[] { 1, 0, 0 }, ctx);
		assertMappingValues("c2a1", new int[] { 1, 1, 0 }, ctx);
		assertMappingValues("c2a2", new int[] { 1, 2, 0 }, ctx);
		// om.addExpectedResult("Test", new String[] { "parent", "c1a1", "c1a2" });
		// om.addExpectedResult("Test", new String[] { "parent", "c2a1", "c2a2" });

	}

	@Test
	public void testMultiValueMec() throws Exception {
		MappingList mappings = new MappingList();
		mappings.setName("Test");
		mappings.setMultiValueBehaviour(MultiValueBehaviour.LAZY);

		addMapping(mappings, "Name", 0, MultiValueBehaviour.GREEDY, "person/name", 3, 0);
		addMapping(mappings, "Age", 0, MultiValueBehaviour.GREEDY, "person/age", 1, 0);
		addMapping(mappings, "Address", 0, MultiValueBehaviour.GREEDY, "person/address", 0, 0);

		XdmNode testDoc = createFromString("<person><name>Andy</name><age>21</age><age>22</age><address>Home</address></person>");

		ContainerExtractionContext ctx = evaluate(mappings, testDoc);

		assertMappingValues("Andy", new int[] { 0, 0, 0 }, ctx);
		assertMappingValues("21", new int[] { 0, 1, 0 }, ctx);
		assertMappingValues("22", new int[] { 0, 1, 1 }, ctx);
		assertMappingValues("Home", new int[] { 0, 2, 0 }, ctx);
	}

	@Test
	public void testSimpleMappings() throws Exception {
		MappingList mappings = new MappingList();
		mappings.setMultiValueBehaviour(MultiValueBehaviour.LAZY);
		addMapping(mappings, "Name", 1, "/person/name");
		addMapping(mappings, "Age", 1, "/person/age");
		addMapping(mappings, "Address", 1, "/person/address");
		mappings.setName("Test");

		XdmNode testDoc = createFromString("<person><name>Andy</name><age>21</age><address>Home</address></person>");
		ContainerExtractionContext ctx = evaluate(mappings, testDoc);
		assertMappingValues("Andy", new int[] { 0, 0, 0 }, ctx);
		assertMappingValues("21", new int[] { 0, 1, 0 }, ctx);
		assertMappingValues("Home", new int[] { 0, 2, 0 }, ctx);
		// om.addExpectedResult("Test", new String[] { "Andy", "21", "Home" });

	}

	@Test
	public void testSimpleMappingsWithNamespaces() throws Exception {
		Map<String, String> prefixUriMap = new HashMap<String, String>();

		prefixUriMap.put("a", "http://example.com/a");
		prefixUriMap.put("b", "http://example.com/b");

		MappingList mappings = new MappingList();
		mappings.setMultiValueBehaviour(MultiValueBehaviour.LAZY);
		addMapping(mappings, "Name", 1, XmlUtil.createXPathValue(prefixUriMap, "/a:person/b:name"));
		addMapping(mappings, "Age", 1, XmlUtil.createXPathValue(prefixUriMap, "/a:person/b:age"));
		addMapping(mappings, "Address", 1, XmlUtil.createXPathValue(prefixUriMap, "/a:person/b:address"));
		mappings.setName("Test");

		XdmNode testDoc =
						createFromString("<a:person xmlns:a=\"http://example.com/a\" xmlns:b=\"http://example.com/b\">"
										+ "<b:name>Andy</b:name><b:age>21</b:age><b:address>Home</b:address>" + "</a:person>");

		ContainerExtractionContext ctx = evaluate(mappings, testDoc);
		assertMappingValues("Andy", new int[] { 0, 0, 0 }, ctx);
		assertMappingValues("21", new int[] { 0, 1, 0 }, ctx);
		assertMappingValues("Home", new int[] { 0, 2, 0 }, ctx);

	}

	@Test
	public void testSimpleMappingsWithRoot() throws Exception {
		MappingList mappings = new MappingList();
		mappings.setName("Test");
		mappings.setMappingRoot(XmlUtil.createXPathValue("/personcollection/person"));
		mappings.setMultiValueBehaviour(MultiValueBehaviour.LAZY);
		addMapping(mappings, "Name", 1, MultiValueBehaviour.GREEDY, "name", 0, 0);
		addMapping(mappings, "Age", 1, MultiValueBehaviour.GREEDY, "age", 0, 0);
		addMapping(mappings, "Address", 1, MultiValueBehaviour.GREEDY, "address", 0, 0);

		XdmNode testDoc =
						createFromString("<personcollection><person><name>Andy</name><age>21</age><address>Home</address></person>"
										+ "<person><name>Emma</name><age>20</age><address>Away</address></person></personcollection>");

		ContainerExtractionContext ctx = evaluate(mappings, testDoc);
		assertMappingValues("Andy", new int[] { 0, 0, 0 }, ctx);
		assertMappingValues("21", new int[] { 0, 1, 0 }, ctx);
		assertMappingValues("Home", new int[] { 0, 2, 0 }, ctx);
		assertMappingValues("Emma", new int[] { 1, 0, 0 }, ctx);
		assertMappingValues("20", new int[] { 1, 1, 0 }, ctx);
		assertMappingValues("Away", new int[] { 1, 2, 0 }, ctx);

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

}
