package com.locima.xml2csv.output;

import static com.locima.xml2csv.TestHelpers.assertCsvEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

import com.locima.xml2csv.StringUtil;
import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.XmlUtil;
import com.locima.xml2csv.model.ExtractedField;
import com.locima.xml2csv.model.IMappingContainer;
import com.locima.xml2csv.model.Mapping;
import com.locima.xml2csv.model.MappingConfiguration;
import com.locima.xml2csv.model.MappingList;
import com.locima.xml2csv.model.MultiValueBehaviour;
import com.locima.xml2csv.model.NameFormat;
import com.locima.xml2csv.model.RecordSet;

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
		container.setOutputName(containerName);
		for (String fieldName : fieldNames) {
			Mapping mapping =
							new Mapping(container, fieldName, NameFormat.NO_COUNTS, 0, MultiValueBehaviour.LAZY, XmlUtil.createXPathValue(
											null, "."), 0, 0);
			container.add(mapping);
		}
		return container;
	}

	private RecordSet createRs(MappingList mapping, String... values) {
		RecordSet rs = new RecordSet();
		for (int i = 0; i < values.length; i++) {
			List<ExtractedField> valueList = new ArrayList<ExtractedField>(1);
			valueList.add(new ExtractedField("1", values[i]));
			rs.addResults((Mapping) mapping.get(i), valueList);
		}
		return rs;
	}

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
	public void testAppendOutput() throws Exception {
		File testOutputDir = this.testOutputDir.newFolder();
		MappingConfiguration mappingConfig = new MappingConfiguration();
		MappingList test = (MappingList) mappingConfig.addMappings(createMappingList("test", "col1", "col2", "col3"));
		MappingList test2 = (MappingList) mappingConfig.addMappings(createMappingList("test2", "colA", "colB", "colC"));
		File tempFolder = this.testOutputDir.newFolder();

		IOutputManager om = new OutputManager();
		om.initialise(tempFolder, mappingConfig, false);

		om.writeRecords("test", createRs(test, "1", "2", "3"));
		om.writeRecords("test2", createRs(test2, "A", "B", "C"));
		om.close();

		om = new OutputManager();
		om.initialise(tempFolder, mappingConfig, true);
		om.writeRecords("test", createRs(test, "1", "2", "3"));
		om.writeRecords("test2", createRs(test2, "A", "B", "C"));
		om.close();

		assertCsvEquals("OutputManagerAppendTest1.csv", tempFolder, "test.csv");
		assertCsvEquals("OutputManagerAppendTest2.csv", tempFolder, "test2.csv");
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

	@Test
	public void testWriteRecords() throws Exception {
		MappingConfiguration mappingConfig = new MappingConfiguration();
		MappingList test = (MappingList) mappingConfig.addMappings(createMappingList("test", "col1", "col2", "col3"));
		MappingList test2 = (MappingList) mappingConfig.addMappings(createMappingList("test2", "colA", "colB", "colC"));
		File tempFolder = this.testOutputDir.newFolder();
		IOutputManager om = createTempOutputManager(tempFolder, mappingConfig, false);
		om.writeRecords("test", createRs(test, "1", "2", "3"));
		om.writeRecords("test2", createRs(test2, "A", "B", "C"));
		om.close();

		assertCsvEquals("OutputManagerTest1.csv", tempFolder, "test.csv");
		assertCsvEquals("OutputManagerTest2.csv", tempFolder, "test2.csv");
	}

}
