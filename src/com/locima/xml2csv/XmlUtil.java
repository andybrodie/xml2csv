package com.locima.xml2csv;

import java.util.Map;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;

/**
 * Utility methods to make dealing with Saxon easier.
 */
public class XmlUtil {

	/**
	 * Managed a singleton Saxon Processor instance.
	 * <p>
	 * This is required because if you don't you'll get the following exception when attempting to evaluate XPath expressions:
	 * <code>Caused by: net.sf.saxon.s9api.SaxonApiException: Supplied node must be built using the same or a compatible Configuration
	 * at net.sf.saxon.s9api.XPathSelector.setContextItem(XPathSelector.java:62</code>.
	 */
	private static Processor processor = new Processor(false);

	/**
	 * Creates an Saxon executable XPath expression based on the XPath and a set of namespace prefix to URI mappings.
	 * 
	 * @param namespaceMappings A mapping of namespace prefix to URI mappings.
	 * @param xPathExpression An XPath expression to compile. Must be valid XPath.
	 * @return a Saxon executable XPath expression, never null.
	 * @throws XMLException If there are any problems compiling <code>xPathExpression</code>.
	 */
	public static XPathExecutable createXPathExecutable(Map<String, String> namespaceMappings, String xPathExpression) throws XMLException {
		// Need to construct a new compiler because the set of namespaces is (potentially) unique to the expression.
		// We could cache a set of compilers, but I doubt it's worth it.
		XPathCompiler xPathCompiler = XmlUtil.getProcessor().newXPathCompiler();
		for (Map.Entry<String, String> entry : namespaceMappings.entrySet()) {
			String prefix = entry.getKey();
			String uri = entry.getValue();
			xPathCompiler.declareNamespace(prefix, uri);
		}

		try {
			XPathExecutable xPath = xPathCompiler.compile(xPathExpression);
			return xPath;
		} catch (SaxonApiException e) {
			throw new XMLException(e, "Unable to compile invalid XPath: %s", xPathExpression);
		}
	}

	/**
	 * Returns the singleton instance.
	 *
	 * @return instance of the Saxon processor manager, never returns null.
	 */
	public static Processor getProcessor() {
		return processor;
	}

	/**
	 * Prevents instantiation.
	 */
	private XmlUtil() {
	}
}
