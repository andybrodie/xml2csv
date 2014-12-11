package com.locima.xml2csv.configuration;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;

import com.locima.xml2csv.extractor.DataExtractorException;
import com.locima.xml2csv.util.EqualsUtil;

/**
 * A tuple structure storing an XPath expression in String form as well as its compiled version.
 * <p>
 * I keep the former for easy logging and debugging and the latter for performance.
 */
public class XPathValue {

	private XPathExecutable compiledXPath;
	private String xPathExpr;

	/**
	 * Constructs a new instance with the source XPath and compiled XPath passed.
	 *
	 * @param xPathExpr the source XPath (string) expression. Used for debug and trace.
	 * @param xPath the compiled (Saxon) XPath object.
	 */
	public XPathValue(String xPathExpr, XPathExecutable xPath) {
		this.xPathExpr = xPathExpr;
		this.compiledXPath = xPath;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof XPathValue) {
			XPathValue that = (XPathValue) obj;
			return EqualsUtil.areEqual(this.xPathExpr, that.xPathExpr);
		} else {
			return false;
		}
	}

	/**
	 * Generates an XPathSelector (for evaluation) based on this instance, within the context of the passed element (must not be null).
	 *
	 * @param element the current node. Must not be null.
	 * @return an XPathSelector which can be evaluated.
	 * @throws DataExtractorException if an error occurs executing the XPath or creating the XPathSelector.
	 */
	public XPathSelector evaluate(XdmNode element) throws DataExtractorException {
		XPathSelector selector = this.compiledXPath.load();
		try {
			selector.setContextItem(element);
		} catch (SaxonApiException e) {
			throw new DataExtractorException(e, "Error evaluating XPath %s", this.xPathExpr);
		}
		return selector;
	}

	/**
	 * Gets the XPath source for this instance.
	 *
	 * @return The XPath source for this instance.
	 */
	public String getSource() {
		return this.xPathExpr;
	}

	@Override
	public int hashCode() {
		return this.xPathExpr.hashCode();
	}
	
	@Override
	public String toString() {
		return "XPathValue(\"" + this.xPathExpr + "\")";
	}

}
