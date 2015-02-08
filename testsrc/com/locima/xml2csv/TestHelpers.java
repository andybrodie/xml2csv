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
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.configuration.MappingConfiguration;
import com.locima.xml2csv.inputparser.FileParserException;
import com.locima.xml2csv.inputparser.xml.XmlFileParser;
import com.locima.xml2csv.util.XmlUtil;

public class TestHelpers {

	private static final Logger LOG = LoggerFactory.getLogger(TestHelpers.class);

	public static final String RES_DIR = "testdata";

	public static void assertCsvEquals(File expectedFile, File actualFile) throws Exception {
		String[] expected = loadFile(expectedFile);
		String[] actual = loadFile(actualFile);

		int lineNo = 1;
		for (String actualLine : actual) {
			LOG.trace("Actual {}: {}", lineNo, actualLine);
			lineNo++;
		}

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
		Program p = new Program();
		List<File> configFiles = new ArrayList<File>();
		configFiles.add(createFile(configurationFile));
		List<File> xmlInputFiles = new ArrayList<File>();
		for (String inputFile : inputFileNames) {
			xmlInputFiles.add(createFile(inputFile));
		}

		TemporaryFolder outputFolder = new TemporaryFolder();
		outputFolder.create();
		File outputDirectory = outputFolder.getRoot();
		p.execute(configFiles, xmlInputFiles, outputDirectory, false, true);

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
