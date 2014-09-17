package com.locima.xml2csv;

import net.sf.saxon.s9api.Processor;

/**
 * Managed a singleton Saxon Processor instance.
 * <p>
 * This is required because if you don't you'll get the following exception when attempting to evaluate XPath expressions:
 * <code>Caused by: net.sf.saxon.s9api.SaxonApiException: Supplied node must be built using the same or a compatible Configuration
 * at net.sf.saxon.s9api.XPathSelector.setContextItem(XPathSelector.java:62</code>.
 */
public class SaxonProcessorManager {

	private static Processor processor = new Processor(false);

	public static Processor getProcessor() {
		return processor;
	}

	private SaxonProcessorManager() {
	}
}
