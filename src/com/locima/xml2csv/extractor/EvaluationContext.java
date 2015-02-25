package com.locima.xml2csv.extractor;

/**
 * Contains contextually useful information associated with the evaluation of a set of mappings. Used within
 * {@link IExtractionContext#evaluate(net.sf.saxon.s9api.XdmNode, EvaluationContext)}.
 */
public class EvaluationContext {

	private XPathVariableBindings bindings;

	/**
	 * Create a new, empty instance.
	 */
	public EvaluationContext() {
		this.bindings = new XPathVariableBindings();
	}

	/**
	 * Retrieve the current set of variable bindings.
	 * @return the current set of variable bindings.
	 */
	public XPathVariableBindings getVariableBindings() {
		return this.bindings;
	}

}
