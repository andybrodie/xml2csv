package com.locima.xml2csv.extractor;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.security.CodeSource;
import java.text.MessageFormat;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmEmptySequence;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.util.XmlUtil;

public class VariablesTests {

	private static final Logger LOG = LoggerFactory.getLogger(VariablesTests.class);

	private static String getJaxpImplementationInfo(String componentName, Class<?> componentClass) {
		CodeSource source = componentClass.getProtectionDomain().getCodeSource();
		return MessageFormat.format("{0} implementation: {1} loaded from: {2}", componentName, componentClass.getName(),
						source == null ? "Java Runtime" : source.getLocation());
	}

	private Processor saxonProcessor;

	private XdmNode createFromString(String xmlAsString) {
		DocumentBuilder docBuilder = this.saxonProcessor.newDocumentBuilder();
		XdmNode document = null;
		try {
			document = docBuilder.build(new StreamSource(new StringReader(xmlAsString)));
		} catch (SaxonApiException e) {
			e.printStackTrace();
			fail();
		}
		return document;
	}

	@Before
	public void setUpSaxon() throws XPathFactoryConfigurationException {
		LOG.info("Initialising Saxon Processor");
		String saxonObjectModel = NamespaceConstant.OBJECT_MODEL_SAXON;
		System.setProperty("javax.xml.xpath.XPathFactory:" + saxonObjectModel, "net.sf.saxon.xpath.XPathFactoryImpl");
		XPathFactory xPathFactory = XPathFactory.newInstance(saxonObjectModel);

		LOG.debug(getJaxpImplementationInfo("DocumentBuilderFactory", DocumentBuilderFactory.newInstance().getClass()));
		LOG.debug(getJaxpImplementationInfo("XPathFactory", xPathFactory.getClass()));

		Processor processor = XmlUtil.getProcessor();

		this.saxonProcessor = processor;
		LOG.info("Saxon Processor initialised successfully");
	}

	/**
	 * Used for validate Saxon behaviour, not a real unit test of xml2csv.
	 */
	@Test
	public void testSimpleVariables() throws Exception {

		String xml = "<root><child attr=\"attr1\">value1</child><child attr=\"attr2\">value2</child></root>";
		Processor processor = this.saxonProcessor;
		XdmNode document = createFromString(xml);
		
		XPathCompiler xPathCompiler = processor.newXPathCompiler();
		xPathCompiler.declareVariable(new QName("var1"), ItemType.STRING, OccurrenceIndicator.ZERO_OR_MORE);
		XPathExecutable xPathExpr = xPathCompiler.compile("root/child[@attr=$var1]/text()");
		XPathSelector xpathSelector = xPathExpr.load();
		
		XdmValue varValues = new XdmValue(new XdmAtomicValue("attr1"));
		varValues = varValues.append(new XdmAtomicValue("attr2"));
		
		xpathSelector.setContextItem(document);

		xpathSelector.setVariable(new QName("var1"), varValues);
		XdmValue result = xpathSelector.evaluate();
		assertEquals("value1", result.itemAt(0).getStringValue());
		assertEquals("value2", result.itemAt(1).getStringValue());
	}
	
	/**
	 * Used for validate Saxon behaviour, not a real unit test of xml2csv.
	 */
	@Test
	public void testUndeclaredVariables() throws Exception {

		String xml = "<root><child attr=\"attr1\">value1</child><child attr=\"attr2\">value2</child></root>";
		Processor processor = this.saxonProcessor;
		XdmNode document = createFromString(xml);
		
		XPathCompiler xPathCompiler = processor.newXPathCompiler();
		xPathCompiler.setAllowUndeclaredVariables(true);
//		xPathCompiler.declareVariable(new QName("var1"), ItemType.STRING, OccurrenceIndicator.ZERO_OR_MORE);
		XPathExecutable xPathExpr = xPathCompiler.compile("root/child[@attr=$var1]/text()");
		XPathSelector xpathSelector = xPathExpr.load();
		
		XdmValue varValues = new XdmValue(new XdmAtomicValue("attr1"));
		varValues = varValues.append(new XdmAtomicValue("attr2"));
		
		xpathSelector.setContextItem(document);
		xpathSelector.setVariable(new QName("var1"), varValues);
		XdmValue result = xpathSelector.evaluate();
		assertEquals("value1", result.itemAt(0).getStringValue());
		assertEquals("value2", result.itemAt(1).getStringValue());
	}
	
}
