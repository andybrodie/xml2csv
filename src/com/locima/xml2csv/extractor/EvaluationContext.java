package com.locima.xml2csv.extractor;

public class EvaluationContext {
	
	private XPathVariableBindings bindings;
	
	public EvaluationContext() {
		this.bindings = new XPathVariableBindings();
	}
	
	public XPathVariableBindings getVariableBindings() {
		return bindings;
	}
	
	public void setVariableBindings(XPathVariableBindings bindings) {
		this.bindings = bindings;
	}
}
