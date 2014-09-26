package com.locima.xml2csv.inputparser;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmEmptySequence;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import com.locima.xml2csv.extractor.DataExtractorException;

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

	/**
	 * Evaluates this instance using the passed element as the current node.
	 * 
	 * @param element the current node. Must not be null.
	 * @return the result of evaluating the XPath modelled by this object.
	 * @throws DataExtractorException if an error occurs executing the XPath.
	 */
	private XdmValue evaluate(XdmNode element) throws DataExtractorException {
		XPathSelector selector = this.compiledXPath.load();
		try {
			selector.setContextItem(element);
			return selector.evaluate();
		} catch (SaxonApiException e) {
			throw new DataExtractorException(e, "Error evaluating XPath %s", this.xPathExpr);
		}
	}

	/**
	 * Evaluates this instance using the passed element as the current node, casting the result to an {@link XdmNode}, or null if the result wasn't a
	 * node.
	 * 
	 * @param element the current node. Must not be null.
	 * @return the result of evaluating the XPath modelled by this object, or null if the result wasn't compatible with {@link XdmNode}.
	 * @throws DataExtractorException if an error occurs executing the XPath.
	 */
	public XdmNode evaluateAsNode(XdmNode element) throws DataExtractorException {
		XdmValue evalResult = evaluate(element);
		if (evalResult instanceof XdmEmptySequence) {
			return null;
		} else {
			return (XdmNode) evalResult;
		}
	}

	/**
	 * Gets the XPath source for this instance.
	 *
	 * @return The XPath source for this instance.
	 */
	public String getSource() {
		return this.xPathExpr;
	}

}
