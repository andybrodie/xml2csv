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

	/**
	 * Returns the singleton instance.
	 * 
	 * @return instance of the Saxon processor manager, never returns null.
	 */
	public static Processor getProcessor() {
		return processor;
	}

	/**
	 * Prevents instances as all methods are static.
	 */
	private SaxonProcessorManager() {
	}
}
