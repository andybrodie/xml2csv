package com.locima.xml2csv.inputparser.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A simple implementation of an SAX ContentHandler error manager.
 */
public class BasicErrorHandler implements ErrorHandler {

	private static final Logger LOG = LoggerFactory.getLogger(BasicErrorHandler.class);

	@Override
	public void error(SAXParseException spe) throws SAXException {
		String message = getParseExceptionInfo("Error", spe);
		throw new SAXException(message);
	}

	@Override
	public void fatalError(SAXParseException spe) throws SAXException {
		String message = getParseExceptionInfo("Fatal Error", spe);
		throw new SAXException(message);
	}

	/**
	 * Creates a nice, human readable error for a parser exception.
	 *
	 * @param errorType type of error that has occurred (warn, fatal, error).
	 * @param spe the exception that was thrown for the error.
	 * @return a nice, human readable error for a parser exception.
	 */
	private String getParseExceptionInfo(String errorType, SAXParseException spe) {
		String systemId = spe.getSystemId();

		if (systemId == null) {
			systemId = "null";
		}

		String info = String.format("%1$s: URI=%2$s Line=%3$d : %4$s", errorType, systemId, spe.getLineNumber(), spe.getMessage());

		return info;
	}

	@Override
	public void warning(SAXParseException spe) throws SAXException {
		LOG.warn(getParseExceptionInfo("Warning", spe));
	}
}
