package com.locima.xml2csv.model;

import java.util.Map;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmEmptySequence;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.XMLException;
import com.locima.xml2csv.XmlUtil;
import com.locima.xml2csv.extractor.DataExtractorException;

/**
 * An input matcher that only accepts files (i.e. returns <code>true</code> from {@link #include(XdmNode)) if they match the
 * XPath expression passed in the constructor.
 */
public class XPathInputFilter extends IdentityFilter {

	private static final Logger LOG = LoggerFactory.getLogger(XPathInputFilter.class);
	private XPathValue xPath;

	public XPathInputFilter(Map<String, String> namespaceMappings, String xPathExpression) throws XMLException {
		XPathExecutable xPathExecutable = XmlUtil.createXPathExecutable(namespaceMappings, xPathExpression);
		this.xPath = new XPathValue(xPathExpression, xPathExecutable);
	}

	@Override
	public boolean include(XdmNode inputXmlFileDocumentNode) throws DataExtractorException {
		XPathSelector selector = this.xPath.evaluate(inputXmlFileDocumentNode);
		boolean match;
		try {
			XdmValue value = selector.evaluate();
			match = !(value instanceof XdmEmptySequence);
			if (match) {
				LOG.trace("Match succeeded on matching {} against {}", this.xPath);
			} else {
				LOG.trace("Match failed to match {}", this.xPath);
				
			}
		} catch (SaxonApiException sae) {
			throw new DataExtractorException(sae, "Error evaluating filter XPath ({}) against input document", this.xPath.getSource());
		}
		return match;
	}

}
