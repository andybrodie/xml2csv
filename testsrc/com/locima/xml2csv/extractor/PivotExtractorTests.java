package com.locima.xml2csv.extractor;

import static com.locima.xml2csv.TestHelpers.assertCsvEquals;
import static com.locima.xml2csv.extractor.ConfigBuilders.createMappingList;

import java.io.File;
import java.io.IOException;

import net.sf.saxon.s9api.XdmNode;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.locima.xml2csv.TestHelpers;
import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.configuration.Mapping;
import com.locima.xml2csv.configuration.MappingConfiguration;
import com.locima.xml2csv.configuration.MappingList;
import com.locima.xml2csv.configuration.MultiValueBehaviour;
import com.locima.xml2csv.configuration.NameFormat;
import com.locima.xml2csv.configuration.PivotMapping;
import com.locima.xml2csv.output.IOutputManager;
import com.locima.xml2csv.output.OutputManager;
import com.locima.xml2csv.output.OutputManagerException;
import com.locima.xml2csv.util.XmlUtil;

public class PivotExtractorTests {

	private XdmNode testDoc;

	@Before
	public void setup() throws Exception {
		String xmlText =
						"<family>"
										+ "<record><field name=\"name\" value=\"Tom\" /><field name=\"age\" value=\"32\" /><field name=\"address\" value=\"Home\" /></record>"
										+ "<record><field name=\"title\" value=\"Dr\"/><field name=\"address\" value=\"Home\" /><field name=\"age\" value=\"20\" /><field name=\"name\" value=\"Dick\" /></record>"
										+ "<record><field name=\"name\" value=\"Harry\" /><field name=\"age\" value=\"44\" /><field name=\"address\" value=\"Away\" /><field name=\"title\" value=\"Mr\" /></record>"
										+ "<record><field name=\"age\" value=\"10\" /><field name=\"suffix\" value=\"Jr\" /><field name=\"name\" value=\"John\" /></record>"
										+ "</family>";
		this.testDoc = TestHelpers.createDocument(xmlText);
	}

	@Test
	public void testSimplePivotMapping() throws Exception {
		PivotMapping pivot =
						ConfigBuilders.createPivotMapping(null, "pivot", "family/record", "field", "@name", "@value", NameFormat.NO_COUNTS, 1,
										MultiValueBehaviour.LAZY);

		MappingConfiguration config = new MappingConfiguration();
		config.addMappings(pivot);

		File outputDir = getTemporaryOutputFolder();
		IOutputManager om = new OutputManager();
		om.initialise(outputDir, config, false);

		XmlDataExtractor extractor = new XmlDataExtractor();
		extractor.setMappingConfiguration(config);

		extractor.extractTo(this.testDoc, om);

		om.close();
		assertCsvEquals("SimplePivotOutput.csv", outputDir, "pivot.csv");
	}

	@Test
	public void testSimpleContainerMapping() throws Exception {
		MappingList mappings = createMappingList("family/record", 0, MultiValueBehaviour.LAZY);
		mappings.setOutputName("pivot");
		addMapping(mappings, "name", "name");
		addMapping(mappings, "age", "age");
		addMapping(mappings, "address", "address");
		addMapping(mappings, "title", "title");
		addMapping(mappings, "suffix", "suffix");

		String xmlText =
						"<family><record><name>Tom</name><age>32</age><address>Home</address></record><record><title>Dr</title><address>Home</address><age>20</age><name>Dick</name></record><record><name>Harry</name><age>44</age><address>Away</address><title>Mr</title></record><record><age>10</age><suffix>Jr</suffix><name>John></name></record></family>";
		this.testDoc = TestHelpers.createDocument(xmlText);

		MappingConfiguration config = new MappingConfiguration();
		config.addMappings(mappings);

		File outputDir = getTemporaryOutputFolder();
		IOutputManager om = new OutputManager();
		om.initialise(outputDir, config, false);
		XmlDataExtractor extractor = new XmlDataExtractor();
		extractor.setMappingConfiguration(config);

		extractor.extractTo(this.testDoc, om);
		om.close();
		assertCsvEquals("SimplePivotOutput.csv", outputDir, "pivot.csv");

	}

	private File getTemporaryOutputFolder() throws IOException {
		TemporaryFolder outputFolder = new TemporaryFolder();
		outputFolder.create();
		return outputFolder.getRoot();
	}

	private Mapping addMapping(MappingList parent, String name, String xPath) throws XMLException {
		Mapping m = new Mapping(parent, name, NameFormat.NO_COUNTS, 1, MultiValueBehaviour.LAZY, XmlUtil.createXPathValue(null, xPath), 0, 0);
		parent.add(m);
		return m;

	}

}
