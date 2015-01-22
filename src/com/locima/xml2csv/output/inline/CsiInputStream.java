package com.locima.xml2csv.output.inline;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.configuration.IValueMapping;
import com.locima.xml2csv.configuration.MappingConfiguration;
import com.locima.xml2csv.extractor.ContainerExtractionContext;
import com.locima.xml2csv.output.OutputManagerException;

/**
 * Extends an {@link ObjectInputStream} with a single method to get the next intermediate format record from a CSI file.
 * <p>
 * The logic in here relies on a null being written to the end of the CSI file when it's created.
 */
public class CsiInputStream extends ObjectInputStream {

	private static final Logger LOG = LoggerFactory.getLogger(CsiInputStream.class);

	private Map<String, IMapping> nameToMapping;

	/**
	 * Records how many objects have been read by a call to {@link #getNextRecord()}.
	 */
	private int readCount;

	public CsiInputStream(Map<String, IMapping> iMappingDictionary, InputStream in) throws IOException {
		super(in);
		this.nameToMapping = iMappingDictionary;
	}

	public IValueMapping getIValueMapping(String mappingName) {
		IMapping mapping = this.nameToMapping.get("V_" + mappingName);
		if ((mapping != null) && (mapping instanceof IValueMapping)) {
			LOG.debug("Successfully retrieved MEC for {}", mappingName);
			return (IValueMapping) mapping;
		}
		LOG.debug("Could not find MEC for {}", mappingName);
		return null;
	}

	public IMappingContainer getMappingContainer(String mappingName) {
		IMapping mapping = this.nameToMapping.get("C_" + mappingName);
		if ((mapping != null) && (mapping instanceof IMappingContainer)) {
			LOG.debug("Successfully retrieved CEC for {}", mappingName);
			return (IMappingContainer) mapping;
		}
		LOG.debug("Could not find CEC for {}", mappingName);
		return null;
	}

	public ContainerExtractionContext getNextRecord() throws OutputManagerException {
		Object ctxObject;
		try {
			if (LOG.isInfoEnabled()) {
				LOG.info("Reading CEC {} from CSI", this.readCount);
			}
			ctxObject = readObject();
			this.readCount++;
		} catch (ClassNotFoundException cnfe) {
			throw new OutputManagerException(cnfe, "Unexpected object in CSI");
		} catch (EOFException eofe) {
			return null;
		} catch (IOException e) {
			throw new OutputManagerException(e, "Unable to read next CEC");
		}
		if (ctxObject instanceof ContainerExtractionContext) {
			return (ContainerExtractionContext) ctxObject;
		} else {
			throw new OutputManagerException("Unexpected object in CSI: %s", ctxObject.getClass().getName());
		}
	}

}
