package com.locima.xml2csv.extractor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmEmptySequence;
import net.sf.saxon.s9api.XdmValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.util.StringUtil;

public class XPathVariableBindings {

	private static final Logger LOG = LoggerFactory.getLogger(XPathVariableBindings.class);

	private Map<QName, XdmValue> bindings;

	public XPathVariableBindings() {
		this.bindings = new HashMap<QName, XdmValue>();
	}

	public void addVariable(String name, String value) {
		addVariable(new QName(name), new XdmAtomicValue(value));
	}
	
	public void addVariable(String name, XdmValue xdmValue) {
		addVariable(new QName(name), xdmValue);
	}
	
	public void addVariable(QName qName, XdmValue xdmValue) {
		if (this.bindings.containsKey(qName)) {
			XdmValue boundValue = this.bindings.get(qName);
			boundValue.append(xdmValue);
		} else {
			this.bindings.put(qName, xdmValue);
		}
		
	}

	public void addVariable(String name) {
		addVariable(new QName(name), new XdmAtomicValue(StringUtil.EMPTY_STRING));
	}

	/**
	 * Binds a set of variables in to the passed selector.
	 *
	 * @param selector the selector to bind the variable values to. Must not be null.
	 * @throws SaxonApiException if any errors occur during binding (for example, attempting to bind an undeclared variable.
	 */
	public void bindTo(XPathSelector selector) throws SaxonApiException {
		if (LOG.isTraceEnabled()) {
			LOG.trace(dumpContents());
		}
		for (Entry<QName, XdmValue> binding : this.bindings.entrySet()) {
			selector.setVariable(binding.getKey(), binding.getValue());
		}
	}

	public String dumpContents() {
		StringBuilder sb = new StringBuilder();
		sb.append("XPath variable bindings as follows:");
		sb.append(StringUtil.LINE_SEPARATOR);
		for (Entry<QName, XdmValue> entry : this.bindings.entrySet()) {
			sb.append(entry.getKey().getLocalName());
			sb.append(" = ");
			sb.append(entry.getValue());
			sb.append(StringUtil.LINE_SEPARATOR);
		}
		return sb.toString();
	}


}
