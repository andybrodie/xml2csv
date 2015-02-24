package com.locima.xml2csv.configuration;

import java.util.Map;
import java.util.Map.Entry;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;

import com.locima.xml2csv.extractor.DataExtractorException;
import com.locima.xml2csv.util.EqualsUtil;

/**
 * A tuple structure storing an XPath expression in String form as well as its compiled version.
 * <p>
 * I keep the former for easy logging and debugging and the latter for performance.
 */
public class XPathValue {

	/**
	 * A Saxon-compiled XPath statement.
	 */
	private XPathExecutable compiledXPath;

	/**
	 * The XPath statement that was compiled to {@link #compiledXPath}.
	 */
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
	 * Binds a set of variables in to the passed selector.
	 * 
	 * @param selector the selector to bind the variable values to. Must not be null.
	 * @param variableBindings the variables to bind, may be null or empty.
	 * @throws SaxonApiException if any errors occur during binding (for example, attempting to bind an undeclared variable.
	 */
	private void bindVariables(XPathSelector selector, Map<QName, String> variableBindings) throws SaxonApiException {
		if (variableBindings == null) {
			return;
		}
		for (Entry<QName, String> binding : variableBindings.entrySet()) {
			selector.setVariable(binding.getKey(), new XdmAtomicValue(binding.getValue()));
		}

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
	 * Generates an XPathSelector (for evaluation) based on this instance, within the context of the passed element (must not be null), passing no
	 * variable bindings.
	 *
	 * @param element the current node. Must not be null.
	 * @return an XPathSelector which can be evaluated.
	 * @throws DataExtractorException if an error occurs executing the XPath or creating the XPathSelector.
	 */
	public XPathSelector evaluate(XdmNode element) throws DataExtractorException {
		return evaluate(element, null);
	}

	/**
	 * Generates an XPathSelector (for evaluation) based on this instance, within the context of the passed element (must not be null).
	 *
	 * @param element the current node. Must not be null.
	 * @param variableBindings a set of variable bindings to apply to this XPath evaluation. May be null or empty.
	 * @return an XPathSelector which can be evaluated.
	 * @throws DataExtractorException if an error occurs executing the XPath or creating the XPathSelector.
	 */
	public XPathSelector evaluate(XdmNode element, Map<QName, String> variableBindings) throws DataExtractorException {
		XPathSelector selector = this.compiledXPath.load();
		try {
			bindVariables(selector, variableBindings);
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
