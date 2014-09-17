package com.locima.xml2csv.extractor;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

/**
 * A tuple structure storing an XPath expression in String form as well as its compiled version.
 * <p>
 * I keep the former for easy logging and debugging and the latter for performance.
 */
public class XPathValue {

	private XPathExecutable compiledXPath;
	private String xPathExpr;

	public XPathValue(String xPathExpr, XPathExecutable xPath) {
		this.xPathExpr = xPathExpr;
		this.compiledXPath = xPath;
	}

	private XdmValue evaluate(XdmNode element) throws DataExtractorException {
		XPathSelector selector = this.compiledXPath.load();
		try {
			selector.setContextItem(element);
			return selector.evaluate();
		} catch (SaxonApiException e) {
			throw new DataExtractorException(e, "Error evaluating XPath %s", this.xPathExpr);
		}
	}

	public XdmNode evaluateAsNode(XdmNode element) throws DataExtractorException {
		return (XdmNode) evaluate(element);
	}

	public String getSource() {
		return this.xPathExpr;
	}

}
