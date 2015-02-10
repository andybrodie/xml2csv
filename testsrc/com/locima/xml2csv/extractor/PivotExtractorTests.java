package com.locima.xml2csv.extractor;

import static com.locima.xml2csv.TestHelpers.assertCsvEquals;

import java.io.File;
import java.io.IOException;

import net.sf.saxon.s9api.XdmNode;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.locima.xml2csv.TestHelpers;
import com.locima.xml2csv.configuration.MappingConfiguration;
import com.locima.xml2csv.configuration.MultiValueBehaviour;
import com.locima.xml2csv.configuration.NameFormat;
import com.locima.xml2csv.configuration.PivotMapping;
import com.locima.xml2csv.output.IOutputManager;
import com.locima.xml2csv.output.OutputManager;

public class PivotExtractorTests {

	private XdmNode testDoc;

	private File getTemporaryOutputFolder() throws IOException {
		TemporaryFolder outputFolder = new TemporaryFolder();
		outputFolder.create();
		return outputFolder.getRoot();
	}

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

}
