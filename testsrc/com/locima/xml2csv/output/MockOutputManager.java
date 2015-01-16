package com.locima.xml2csv.output;

import static com.locima.xml2csv.TestHelpers.toFlatString;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.configuration.MappingConfiguration;
import com.locima.xml2csv.util.StringUtil;
import com.locima.xml2csv.util.Tuple;

public class MockOutputManager implements IOutputManager {

	private static final Logger LOG = LoggerFactory.getLogger(MockOutputManager.class);

	private Queue<Tuple<String, String[]>> _expectedResults = new LinkedList<Tuple<String, String[]>>();

	@Override
	public void abort() {
		// No-op
	}

	public void addExpectedResult(String writerName, String... values) {
		this._expectedResults.add(new Tuple<String, String[]>(writerName, values));
	}

	@Override
	public void close() {
		if (this._expectedResults.size() > 0) {
			Assert.fail(String.format("Close called with %1$s expected results expected", this._expectedResults.size()));
		}
	}

	@Override
	public void initialise(File outputDirectory, MappingConfiguration config, boolean appendOutput) throws OutputManagerException {
		// No-op
	}

	private String[] toValuesOnlyArray(List<IExtractionResults> values) {
		throw new UnsupportedOperationException("Need to make this a DIRECT mock as opposed to INLINE mock");
		// if (values == null) {
		// return new String[0];
		// }
		// String[] array = new String[values.size()];
		// for (int i = 0; i < array.length; i++) {
		// // array[i] = values.get(i) == null ? "<NULL ENTRY>" : values.get(i).getFieldValue();
		// }
		// return array;
	}

	@Override
	public void writeRecords(String writerName, IExtractionResultsContainer records) throws OutputManagerException {
		for (List<IExtractionResults> values : records.getChildren()) {
			Tuple<String, String[]> s = this._expectedResults.poll();
			Assert.assertEquals(s.getFirst(), writerName);
			LOG.debug("Expected: {}", StringUtil.toString(s.getSecond()));
			String[] actual = toValuesOnlyArray(values);
			LOG.debug("Actual: {}", StringUtil.toString(actual));
			Assert.assertArrayEquals(s.getSecond(), actual);
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
