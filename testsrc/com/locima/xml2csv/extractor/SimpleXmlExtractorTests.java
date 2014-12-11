package com.locima.xml2csv.extractor;

import static org.junit.Assert.fail;

import java.io.StringReader;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.junit.Before;
import org.junit.Test;

import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.configuration.Mapping;
import com.locima.xml2csv.configuration.MappingConfiguration;
import com.locima.xml2csv.configuration.MappingList;
import com.locima.xml2csv.configuration.MultiValueBehaviour;
import com.locima.xml2csv.configuration.NameFormat;
import com.locima.xml2csv.output.IOutputManager;
import com.locima.xml2csv.util.XmlUtil;

public class SimpleXmlExtractorTests {

	private Processor saxonProcessor;
	private XdmNode testDoc;

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

	private Mapping createGreedyMapping(MappingList parent, String xPath) throws XMLException {
		Mapping m =
						new Mapping(parent, xPath, NameFormat.NO_COUNTS, -1, MultiValueBehaviour.GREEDY, XmlUtil.createXPathValue(
										null, xPath), 0, 0);
		parent.add(m);
		return m;
	}

	private Mapping createLazyMapping(MappingList parent, String xPath, int groupNumber) throws XMLException {
		Mapping m =
						new Mapping(parent, xPath, NameFormat.NO_COUNTS, groupNumber, MultiValueBehaviour.LAZY,
										XmlUtil.createXPathValue(null, xPath), 0, 0);
		parent.add(m);
		return m;
	}

	private MappingList createMappingList(String xPath, int groupNumber, MultiValueBehaviour mvb) throws XMLException {
		MappingList ml = new MappingList();
		ml.setOutputName(xPath);
		ml.setGroupNumber(groupNumber);
		ml.setMultiValueBehaviour(mvb);
		ml.setMappingRoot(xPath);
		return ml;
	}

	@Before
	public void setUp() throws Exception {
		this.saxonProcessor = XmlUtil.getProcessor();

		this.testDoc = createFromString("<group><subgroup sgName=\"s1\"><a1>a</a1><a2>b</a2></subgroup>"
						+ "<subgroup sgName=\"s2\"><a1>m</a1><a2>n</a2></subgroup></group>");
	}

	@Test
	public void testSimpleLazyMappingsWithRoot() throws Exception {
		MappingConfiguration config = new MappingConfiguration();
		MappingList mappings = createMappingList("group/subgroup", 0, MultiValueBehaviour.LAZY);
		createLazyMapping(mappings, "@sgName", 1);
		createLazyMapping(mappings, "a1", 1);
		createLazyMapping(mappings, "a2", 1);
		config.addMappings(mappings);

		IOutputManager lom = new LoggingOutputManager();
		lom.initialise(null,config, false);
	
		XmlDataExtractor extractor = new XmlDataExtractor();
		extractor.setMappingConfiguration(config);
		extractor.extractTo(testDoc, lom);
		lom.close();
	}
	
	@Test
	public void testSimpleGreedyMappingsWithRoot() throws Exception {
		MappingConfiguration config = new MappingConfiguration();
		MappingList mappings = createMappingList("group/subgroup", 0, MultiValueBehaviour.LAZY);
		createLazyMapping(mappings, "@sgName",1);
		createLazyMapping(mappings, "a1",1);
		createLazyMapping(mappings, "a2",1);
		config.addMappings(mappings);

		IOutputManager lom = new LoggingOutputManager();
		lom.initialise(null,config, false);
	
		XmlDataExtractor extractor = new XmlDataExtractor();
		extractor.setMappingConfiguration(config);
		extractor.extractTo(testDoc, lom);
		lom.close();
	}

	@Test
	public void testGreedyContainerWithMultipleChildHits() throws Exception {
		MappingConfiguration config = new MappingConfiguration();
		MappingList mappings = createMappingList("group", 0, MultiValueBehaviour.GREEDY);
		createLazyMapping(mappings, "subgroup/@sgName",1);
		createLazyMapping(mappings, "subgroup/a1",1);
		createLazyMapping(mappings, "subgroup/a2",1);
		config.addMappings(mappings);

		IOutputManager lom = new LoggingOutputManager();
		lom.initialise(null,config, false);
	
		XmlDataExtractor extractor = new XmlDataExtractor();
		extractor.setMappingConfiguration(config);
		extractor.extractTo(testDoc, lom);
		lom.close();
	}

	@Test
	public void testGreedyContainerWithGreedyChilren() throws Exception {
		MappingConfiguration config = new MappingConfiguration();
		MappingList mappings = createMappingList("group", 0, MultiValueBehaviour.GREEDY);
		createLazyMapping(mappings, "subgroup/a1",1);
		createLazyMapping(mappings, "subgroup/a2",2);
		config.addMappings(mappings);

		IOutputManager lom = new LoggingOutputManager();
		lom.initialise(null,config, false);
	
		XmlDataExtractor extractor = new XmlDataExtractor();
		extractor.setMappingConfiguration(config);
		extractor.extractTo(testDoc, lom);
		lom.close();
	}

}
