package com.locima.xml2csv.extractor;

import net.sf.saxon.s9api.XdmNode;

public abstract class ExtractionContext {

	private ContainerExtractionContext parent;

	protected ExtractionContext(ContainerExtractionContext parent) {
		this.parent = parent;
	}

	public abstract ExtractedRecordList evaluate(XdmNode rootNode) throws DataExtractorException;

	public ContainerExtractionContext getParent() {
		return this.parent;
	}

}
