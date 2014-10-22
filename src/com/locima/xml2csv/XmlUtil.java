package com.locima.xml2csv;

import java.util.Map;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;

public class XmlUtil {

	public static XPathExecutable createXPathExecutable(Map<String, String> namespaceMappings, String xPathExpression) throws XMLException {
		// Need to construct a new compiler because the set of namespaces is (potentially) unique to the expression.
		// We could cache a set of compilers, but I doubt it's worth it.
		XPathCompiler xPathCompiler = SaxonProcessorManager.getProcessor().newXPathCompiler();
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
	 * Prevents instantiation.
	 */
	private XmlUtil() {
	}
}
