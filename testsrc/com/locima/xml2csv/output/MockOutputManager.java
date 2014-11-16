package com.locima.xml2csv.output;

import static com.locima.xml2csv.TestHelpers.toFlatString;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.NotImplementedException;
import com.locima.xml2csv.Tuple;
import com.locima.xml2csv.model.MappingConfiguration;
import com.locima.xml2csv.model.RecordSet;

public class MockOutputManager implements IOutputManager {

	private static final Logger LOG = LoggerFactory.getLogger(MockOutputManager.class);

	private Queue<Tuple<String, String[]>> _expectedResults = new LinkedList<Tuple<String, String[]>>();

	public void addExpectedResult(String writerName, String[] values) {
		this._expectedResults.add(new Tuple<String, String[]>(writerName, values));
	}

	@Override
	public void close() {
		if (this._expectedResults.size() > 0) {
			Assert.fail(String.format("Close called with %1$s expected results expected", this._expectedResults.size()));
		}
	}

	@Override
	public void initialise(MappingConfiguration config, boolean appendOutput) throws OutputManagerException {
	}

	@Override
	public void setDirectory(File outputDirectoryName) throws OutputManagerException {
	}

	@Override
	public void writeRecords(RecordSet records) throws OutputManagerException {
		for (List<String> values : records) {
			Tuple<String, String[]> s = this._expectedResults.poll();
		}
	}

	public void writeRecords(String writerName, String[] values) throws OutputManagerException {
		Tuple<String, String[]> s = this._expectedResults.poll();
		if (s == null) {
			Assert.fail("writeRecords invoked but no expected values in mock");
		}

		Assert.assertEquals(s.getFirst(), writerName);
		if (LOG.isTraceEnabled()) {
			LOG.trace("Expected \"{}\"", toFlatString(s.getSecond()));
			LOG.trace("Actual \"{}\"", toFlatString(values));
		}
		Assert.assertArrayEquals(s.getSecond(), values);
	}

}
