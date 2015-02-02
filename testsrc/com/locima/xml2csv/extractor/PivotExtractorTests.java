package com.locima.xml2csv.extractor;

import static com.locima.xml2csv.extractor.ConfigBuilders.createMappingList;
import net.sf.saxon.s9api.XdmNode;

import org.junit.Before;
import org.junit.Test;

import com.locima.xml2csv.TestHelpers;
import com.locima.xml2csv.configuration.MappingConfiguration;
import com.locima.xml2csv.configuration.MappingList;
import com.locima.xml2csv.configuration.MultiValueBehaviour;
import com.locima.xml2csv.configuration.NameFormat;
import com.locima.xml2csv.configuration.PivotMapping;
import com.locima.xml2csv.output.IOutputManager;
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
		MappingList mappings = createMappingList("family/record", 0, MultiValueBehaviour.LAZY);
		PivotMapping pivot =
						ConfigBuilders.createPivotMapping(mappings, "pivot", "field", "@name", "@value", NameFormat.NO_COUNTS, 1,
										MultiValueBehaviour.LAZY);
		mappings.add(pivot);

		IOutputManager lom = new LoggingOutputManager();

		MappingConfiguration config = new MappingConfiguration();
		config.addMappings(mappings);
		lom.initialise(null, config, false);

		XmlDataExtractor extractor = new XmlDataExtractor();
		extractor.setMappingConfiguration(config);

		extractor.extractTo(this.testDoc, lom);
		lom.close();
	}

}
