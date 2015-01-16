package com.locima.xml2csv.output.inline;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.output.OutputManagerException;

/**
 * Extends an {@link ObjectInputStream} with a single method to get the next intermediate format record from a CSI file.
 * <p>
 * The logic in here relies on a null being written to the end of the CSI file when it's created.
 */
public class CsiInputStream extends ObjectInputStream {

	private static final Logger LOG = LoggerFactory.getLogger(CsiInputStream.class);

	private int objectCount;

	public CsiInputStream(InputStream in) throws IOException {
		super(in);
	}

	/**
	 * Reads the next record from the CSI file, or null if we've run out of records.
	 *
	 * @return an array of {@link ExtractedField} objects, representing a single record, or null if there are no more records to read.
	 * @throws OutputManagerException
	 */
	public ExtractedField[] getNextRecord() throws OutputManagerException {
		Object objectFromCsi;
		try {
			objectFromCsi = readObject();
			if (objectFromCsi instanceof ExtractedField[]) {
				this.objectCount++;
				return (ExtractedField[]) objectFromCsi;
			}
			throw new OutputManagerException("Unexpected object type found in CSI input stream: %s.", objectFromCsi.getClass());
		} catch (EOFException e) {
			LOG.debug("Found EOF after {} ExtractedField[] objects read.", this.objectCount);
			return null;
		} catch (IOException e) {
			throw new OutputManagerException(e, "Unexpected IOException when reading from CSI file.");
		} catch (ClassNotFoundException e) {
			throw new OutputManagerException(e, "Unexpected object type (of non-available type) found in CSI input stream.");
		}
	}

}
