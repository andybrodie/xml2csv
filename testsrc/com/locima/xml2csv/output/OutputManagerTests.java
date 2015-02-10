package com.locima.xml2csv.output;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.configuration.Mapping;
import com.locima.xml2csv.configuration.MappingConfiguration;
import com.locima.xml2csv.configuration.MappingList;
import com.locima.xml2csv.configuration.MultiValueBehaviour;
import com.locima.xml2csv.configuration.NameFormat;
import com.locima.xml2csv.util.StringUtil;
import com.locima.xml2csv.util.XmlUtil;

public class OutputManagerTests {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Rule
	public TemporaryFolder testOutputDir = new TemporaryFolder();

	private Map<String, List<String>> addOMConfig(Map<String, List<String>> map, String mappingName, String... values) {
		Map<String, List<String>> mapToAddTo = map == null ? new HashMap<String, List<String>>() : map;
		List<String> s = Arrays.asList(values);
		mapToAddTo.put(mappingName, s);
		return mapToAddTo;
	}

	private MappingConfiguration createConfig(Map<String, List<String>> config) throws XMLException {
		MappingConfiguration mappingConfig = new MappingConfiguration();
		for (Map.Entry<String, List<String>> entry : config.entrySet()) {
			String outputName = entry.getKey();
			List<String> fieldNames = entry.getValue();
			IMappingContainer container = createMappingList(outputName, fieldNames.toArray(new String[0]));
			mappingConfig.addMappings(container);
		}
		return mappingConfig;
	}

	private IMappingContainer createMappingList(String containerName, String... fieldNames) throws XMLException {
		MappingList container = new MappingList();
		container.setName(containerName);
		container.setMultiValueBehaviour(MultiValueBehaviour.GREEDY);
		container.setMinValueCount(fieldNames.length);
		container.setMaxValueCount(fieldNames.length);
		for (String fieldName : fieldNames) {
			Mapping mapping = new Mapping();
			mapping.setParent(container);
			mapping.setName(fieldName);
			mapping.setNameFormat(NameFormat.NO_COUNTS);
			mapping.setGroupNumber(0);
			mapping.setMultiValueBehaviour(MultiValueBehaviour.LAZY);
			mapping.setValueXPath(XmlUtil.createXPathValue("."));
			mapping.setMinValueCount(0);
			mapping.setMaxValueCount(0);
			container.add(mapping);
		}
		return container;
	}

	// private Iterable<List<ExtractedField>> createRecords(MappingList mapping, String... values) {
	// List<List<ExtractedField>> records = new ArrayList<List<ExtractedField>>();
	// if (mapping.getMultiValueBehaviour()==MultiValueBehaviour.LAZY) {
	// for (String value : values) {
	// List<ExtractedField> record = new ArrayList<ExtractedField>(1);
	// record.add(new ExtractedField("1", value));
	// records.add(record);
	// }} else {
	// List<ExtractedField> record = new ArrayList<ExtractedField>(values.length);
	// for (int i=0; i<values.length; i++) {
	// record.add(new ExtractedField(new Integer(i).toString(), values[i]));
	// }
	// records.add(record);
	// }
	// return records;
	// }

	private IOutputManager createTempOutputManager(File outputDir, MappingConfiguration mappingConfiguration, boolean appendToFiles)
					throws IOException, OutputManagerException {
		IOutputManager om = new OutputManager();
		om.initialise(outputDir, mappingConfiguration, appendToFiles);
		return om;
	}

	private IOutputManager createTempOutputManager(MappingConfiguration mappingConfiguration, boolean appendToFiles) throws IOException,
	OutputManagerException {
		File newTempFolder = this.testOutputDir.newFolder();
		return createTempOutputManager(newTempFolder, mappingConfiguration, appendToFiles);
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testClose() throws IOException, OutputManagerException, XMLException {
		Map<String, List<String>> outputConfiguration = new LinkedHashMap<String, List<String>>();
		IOutputManager om = createTempOutputManager(createConfig(outputConfiguration), false);
		om.close();
	}

	@Test
	public void testCreateFiles() throws IOException, OutputManagerException, XMLException {
		Map<String, List<String>> config = addOMConfig(null, "test", "col1", "col2", "col3");
		addOMConfig(config, "test2", "colA", "colB", "colC");
		IOutputManager om = createTempOutputManager(createConfig(config), false);
		om.close();
	}

	@Test
	public void testEscape() {
		String[] input = new String[] { null, "a", "", "\"", "a\"b", "," };
		String[] expected = new String[] { null, "a", "", "\"\"\"\"", "\"a\"\"b\"", "\",\"" };
		for (int i = 0; i < input.length; i++) {
			assertEquals(expected[i], StringUtil.escapeForCsv(input[i]));
		}
	}

}
