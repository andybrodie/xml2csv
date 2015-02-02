package com.locima.xml2csv.extractor;

import static com.locima.xml2csv.extractor.ConfigBuilders.createLazyMapping;
import static com.locima.xml2csv.extractor.ConfigBuilders.createMappingList;
import net.sf.saxon.s9api.XdmNode;

import org.junit.Before;
import org.junit.Test;

import com.locima.xml2csv.TestHelpers;
import com.locima.xml2csv.configuration.MappingConfiguration;
import com.locima.xml2csv.configuration.MappingList;
import com.locima.xml2csv.configuration.MultiValueBehaviour;
import com.locima.xml2csv.output.IOutputManager;

public class SimpleXmlExtractorTests {

	private XdmNode testDoc;

	@Before
	public void setUp() throws Exception {
		this.testDoc =
						TestHelpers.createDocument("<group><subgroup sgName=\"s1\"><a1>a</a1><a2>b</a2></subgroup>"
										+ "<subgroup sgName=\"s2\"><a1>m</a1><a2>n</a2></subgroup></group>");
	}

	@Test
	public void testGreedyContainerWithGreedyChilren() throws Exception {
		MappingConfiguration config = new MappingConfiguration();
		MappingList mappings = createMappingList("group", 0, MultiValueBehaviour.GREEDY);
		createLazyMapping(mappings, "subgroup/a1", 1);
		createLazyMapping(mappings, "subgroup/a2", 2);
		config.addMappings(mappings);

		IOutputManager lom = new LoggingOutputManager();
		lom.initialise(null, config, false);

		XmlDataExtractor extractor = new XmlDataExtractor();
		extractor.setMappingConfiguration(config);
		extractor.extractTo(this.testDoc, lom);
		lom.close();
	}

	@Test
	public void testGreedyContainerWithMultipleChildHits() throws Exception {
		MappingConfiguration config = new MappingConfiguration();
		MappingList mappings = createMappingList("group", 0, MultiValueBehaviour.GREEDY);
		createLazyMapping(mappings, "subgroup/@sgName", 1);
		createLazyMapping(mappings, "subgroup/a1", 1);
		createLazyMapping(mappings, "subgroup/a2", 1);
		config.addMappings(mappings);

		IOutputManager lom = new LoggingOutputManager();
		lom.initialise(null, config, false);

		XmlDataExtractor extractor = new XmlDataExtractor();
		extractor.setMappingConfiguration(config);
		extractor.extractTo(this.testDoc, lom);
		lom.close();
	}

	@Test
	public void testSimpleGreedyMappingsWithRoot() throws Exception {
		MappingConfiguration config = new MappingConfiguration();
		MappingList mappings = createMappingList("group/subgroup", 0, MultiValueBehaviour.LAZY);
		createLazyMapping(mappings, "@sgName", 1);
		createLazyMapping(mappings, "a1", 1);
		createLazyMapping(mappings, "a2", 1);
		config.addMappings(mappings);

		IOutputManager lom = new LoggingOutputManager();
		lom.initialise(null, config, false);

		XmlDataExtractor extractor = new XmlDataExtractor();
		extractor.setMappingConfiguration(config);
		extractor.extractTo(this.testDoc, lom);
		lom.close();
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
		lom.initialise(null, config, false);

		XmlDataExtractor extractor = new XmlDataExtractor();
		extractor.setMappingConfiguration(config);
		extractor.extractTo(this.testDoc, lom);
		lom.close();
	}

}
