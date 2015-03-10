package com.locima.xml2csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.cmdline.Program;
import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.Mapping;
import com.locima.xml2csv.configuration.MappingConfiguration;
import com.locima.xml2csv.configuration.MappingList;
import com.locima.xml2csv.configuration.MultiValueBehaviour;
import com.locima.xml2csv.configuration.NameFormat;
import com.locima.xml2csv.configuration.XPathValue;
import com.locima.xml2csv.inputparser.FileParserException;
import com.locima.xml2csv.inputparser.xml.XmlFileParser;
import com.locima.xml2csv.util.StringUtil;
import com.locima.xml2csv.util.XmlUtil;

public class TestHelpers {

	private static final Logger LOG = LoggerFactory.getLogger(TestHelpers.class);

	public static final String RES_DIR = "testdata";

	public static Mapping addMapping(MappingList mappings, String name, int groupNumber, MultiValueBehaviour mvb, String valueXPath,
					int minValueCount, int maxValueCount) throws XMLException {
		List<String> params = new ArrayList<String>();
		if (mappings != null) {
			for (IMapping siblingMapping : mappings) {
				params.add(siblingMapping.getName());
			}

		}
		XPathValue xpv = XmlUtil.createXPathValue(null, valueXPath, params.toArray(new String[0]));
		return addMapping(mappings, name, groupNumber, mvb, xpv, minValueCount, maxValueCount);

	}

	public static Mapping addMapping(MappingList mappings, String name, int groupNumber, MultiValueBehaviour mvb, XPathValue valueXPath,
					int minValueCount, int maxValueCount) throws XMLException {
		Mapping mapping = new Mapping();
		mapping.setParent(mappings);
		mapping.setName(name);
		mapping.setNameFormat(NameFormat.NO_COUNTS);
		mapping.setGroupNumber(groupNumber);
		mapping.setMultiValueBehaviour(mvb);
		mapping.setMinValueCount(minValueCount);
		mapping.setMaxValueCount(maxValueCount);
		mapping.setValueXPath(valueXPath);

		if (mappings != null) {
			mappings.add(mapping);
		}

		return mapping;
	}

	public static Mapping addMapping(MappingList mappings, String name, int groupNumber, String valueXPathExpression) throws XMLException {

		return addMapping(mappings, name, groupNumber, MultiValueBehaviour.LAZY, valueXPathExpression, 0, 0);
	}

	public static Mapping addMapping(MappingList mappings, String name, int groupNumber, XPathValue valueXPath) throws XMLException {
		return addMapping(mappings, name, groupNumber, MultiValueBehaviour.LAZY, valueXPath, 0, 0);
	}

	public static void assertCsvEquals(File expectedFile, File actualFile) throws Exception {
		String[] expected = loadFile(expectedFile);
		String[] actual = loadFile(actualFile);

		int lineNo = 1;
		StringBuilder actualDump = new StringBuilder("Actual output of " + actualFile.getName() + " as follows:\n");
		for (String actualLine : actual) {
			actualDump.append(String.format("%d: %s\n", lineNo, actualLine));
			lineNo++;
		}
		LOG.trace(actualDump.toString());

		for (int i = 0; i < expected.length; i++) {
			if (i >= actual.length) {
				fail(String.format("Unable to compare line %d as actual has run out of lines (expected %d).", i + 1, expected.length));
			}
			assertEquals(String.format("Mismatch at line %d", i + 1), expected[i], actual[i]);
			LOG.debug("Successfully compared line {}: {}", i, actual[i]);
		}
		assertEquals("More lines in actual than expected.", expected.length, actual.length);
	}

	public static void assertCsvEquals(String expectedFileName, File actualRootDirectory, String actualFileName) throws Exception {
		assertCsvEquals(new File(RES_DIR, expectedFileName), new File(actualRootDirectory, actualFileName));
	}

	public static void assertCsvEquals(String expectedFileName, String actualFileName) throws Exception {
		assertCsvEquals(new File(RES_DIR, expectedFileName), new File(actualFileName));
	}

	public static void assertIterableEquals(Iterable<? extends Object> expectedIterable, Iterable<? extends Object> actualIterable) {
		if (expectedIterable==null && actualIterable==null) return;
		if (expectedIterable==null) fail("Actual specifies elements but expected was null");
		if (actualIterable==null) fail("Expected specifies elements but actual was null");
		Iterator<? extends Object> exIter = expectedIterable.iterator();
		Iterator<? extends Object> acIter = actualIterable.iterator();
		int count = 0;
		while (exIter.hasNext() && acIter.hasNext()) {
			Object expected = exIter.next();
			Object actual = acIter.next();
			LOG.debug("Comparing item {}: {} with {}", count, expected, actual);
			assertEquals("Mismatch at position " + count, expected, actual);
			count++;
		}
		if (exIter.hasNext()) {
			fail("Actual has run out of elements at: " + count);
		}
		if (acIter.hasNext()) {
			fail("More objects in actual than expected: " + count);
			
		}
	}
	
	public static void assertSameContents(Collection<? extends Object> expectedCollection, Collection<? extends Object> actualCollection) {
		List<? extends Object> expectedList = new ArrayList<Object>(expectedCollection);
		List<? extends Object> actualList = new ArrayList<Object>(actualCollection);
		int count=0;
		for (Object expected : expectedList) {
			if (actualList.contains(expected)) {
				actualList.remove(expected);
			} else {
				fail("Expected value " + expected + " at index " + count + " was not found in actual");
			}
			count++;
		}
		if (!actualList.isEmpty()) {
			fail("Actual has values not in expected:" + StringUtil.collectionToString(actualList, ", ", null));
		}
		
	}

	public static XdmNode createDocument(String xmlText) throws SaxonApiException {
		DocumentBuilder db = XmlUtil.getProcessor().newDocumentBuilder();
		XdmNode document = db.build(new StreamSource(new StringReader(xmlText)));
		return document;
	}

	public static File createFile(String relativeFilename) {
		return new File(RES_DIR, relativeFilename);
	}

	public static String[] loadFile(File file) throws IOException {
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		List<String> lines = new ArrayList<String>();
		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
			lines.add(line);
		}
		bufferedReader.close();
		return lines.toArray(new String[lines.size()]);
	}

	public static MappingConfiguration loadMappingConfiguration(String configurationFile) throws XMLException, FileParserException, IOException {
		XmlFileParser parser = new XmlFileParser();
		List<File> files = new ArrayList<File>();
		files.add(new File(RES_DIR, configurationFile));
		parser.load(files);
		MappingConfiguration mappingConfig = parser.getMappings();
		return mappingConfig;
	}

	public static TemporaryFolder processFiles(String configurationFile, String... inputFileNames) throws IOException, ProgramException {
		new Program();
		List<File> configFiles = new ArrayList<File>();
		configFiles.add(createFile(configurationFile));
		List<File> xmlInputFiles = new ArrayList<File>();
		for (String inputFile : inputFileNames) {
			xmlInputFiles.add(createFile(inputFile));
		}

		TemporaryFolder outputFolder = new TemporaryFolder();
		outputFolder.create();
		File outputDirectory = outputFolder.getRoot();
		new Xml2Csv().execute(configFiles, xmlInputFiles, outputDirectory, false, true);

		return outputFolder;

	}

	public static TemporaryFolder processFilesAsCmdLine(String configurationFile, String... inputFileNames) throws IOException, ProgramException {
		Program p = new Program();

		List<String> params = new ArrayList<String>();

		params.add("-" + Program.OPT_APPEND_OUTPUT);

		params.add("-" + Program.OPT_CONFIG_FILE);
		params.add(createFile(configurationFile).getAbsolutePath());

		List<File> xmlInputFiles = new ArrayList<File>();
		for (String inputFile : inputFileNames) {
			xmlInputFiles.add(createFile(inputFile));
		}

		TemporaryFolder outputFolder = new TemporaryFolder();
		outputFolder.create();
		File outputDirectory = outputFolder.getRoot();
		params.add("-" + Program.OPT_OUT_DIR);
		params.add(outputDirectory.getAbsolutePath());

		params.add("REPLACE");

		String[] paramsArray = params.toArray(new String[0]);
		int inputParamIndex = paramsArray.length - 1;
		for (String inputFileName : inputFileNames) {
			paramsArray[inputParamIndex] = TestHelpers.RES_DIR + File.separatorChar + inputFileName;
			LOG.info("Executing xml2csv with parameters: {} ", StringUtil.toStringList(paramsArray));
			p.execute(paramsArray);
		}

		return outputFolder;
	}

	public static String toFlatString(Collection<? extends Object> second) {
		StringBuffer buf = new StringBuffer();
		if ((second != null) && (second.size() > 0)) {
			for (Object o : second) {
				String s = o == null ? "<null>" : o.toString();
				buf.append(s);
				buf.append(", ");
			}
			buf = buf.delete(buf.length() - 2, buf.length());
		}
		return buf.toString();
	}

	public static String toFlatString(Object[] second) {
		StringBuffer buf = new StringBuffer();
		if ((second != null) && (second.length > 0)) {
			for (Object o : second) {
				String s = o == null ? "<null>" : o.toString();
				buf.append(s);
				buf.append(", ");
			}
			buf = buf.delete(buf.length() - 2, buf.length());
		}
		return buf.toString();
	}

	public static List<String> toStringList(String... strings) {
		List<String> list = new ArrayList<String>(strings.length);
		for (String string : strings) {
			list.add(string);
		}
		return list;
	}

}
