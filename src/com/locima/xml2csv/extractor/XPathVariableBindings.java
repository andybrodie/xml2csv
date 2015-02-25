package com.locima.xml2csv.extractor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.util.StringUtil;

/**
 * Keeps track of variables that are passed to {@link IExtractionContext} values for use in XPath expressions executed by the underlying
 * {@link IMapping} implementations.
 * <p>
 * Variables are multi-valued.
 */
public class XPathVariableBindings {

	private static final Logger LOG = LoggerFactory.getLogger(XPathVariableBindings.class);

	private Map<QName, XdmValue> bindings;

	/**
	 * Create a new empty set of bindings.
	 */
	public XPathVariableBindings() {
		this.bindings = new HashMap<QName, XdmValue>();
	}

	/**
	 * Add a new value to the variable with the given name. Variables are multi-valued, so if the variable name already exists, then the new value
	 * will be appended (similar to array behaviour).
	 *
	 * @param qName the name of the variable. Must not be null.
	 * @param xdmValue the value to associate with the variable name, or appended to an existing value. May be null.
	 */
	public void addVariable(QName qName, XdmValue xdmValue) {
		if (this.bindings.containsKey(qName)) {
			XdmValue boundValue = this.bindings.get(qName);
			boundValue.append(xdmValue);
		} else {
			this.bindings.put(qName, xdmValue);
		}

	}

	/**
	 * Add an empty value to the variable with the given name. Variables are multi-valued, so if the variable name already exists, then the new value
	 * will be appended (similar to array behaviour).
	 *
	 * @param name the name of the variable. Will be converted to a {@link QName}. Must not be null.
	 */
	public void addVariable(String name) {
		addVariable(new QName(name), new XdmAtomicValue(StringUtil.EMPTY_STRING));
	}

	/**
	 * Add a new value to the variable with the given name. Variables are multi-valued, so if the variable name already exists, then the new value
	 * will be appended (similar to array behaviour).
	 *
	 * @param name the name of the variable. Will be converted to a {@link QName}. Must not be null.
	 * @param value the value to associate with the variable name, or appended to an existing value. Will be converted to an {@link XdmAtomicValue}.
	 *            May be null.
	 */
	public void addVariable(String name, String value) {
		if (value == null) {
			addVariable(name);
		} else {
			addVariable(new QName(name), new XdmAtomicValue(value));
		}
	}

	/**
	 * Add a new value to the variable with the given name. Variables are multi-valued, so if the variable name already exists, then the new value
	 * will be appended (similar to array behaviour).
	 *
	 * @param name the name of the variable. Will be converted to a {@link QName}. Must not be null.
	 * @param xdmValue the value to associate with the variable name, or appended to an existing value. May be null.
	 */
	public void addVariable(String name, XdmValue xdmValue) {
		addVariable(new QName(name), xdmValue);
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

	/**
	 * Creates a string version of the all the variable bindings.
	 * 
	 * @return a string version of all the variable bindings (<code>name = value</code> pairs).
	 */
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
