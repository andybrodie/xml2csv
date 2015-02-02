package com.locima.xml2csv;

import static com.locima.xml2csv.TestHelpers.assertCsvEquals;
import static com.locima.xml2csv.TestHelpers.processFiles;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PivotEndToEndTests {

	@Test
	public void testSimplePivot() throws Exception {
		TemporaryFolder outputFolder = processFiles("SimplePivotConfig.xml", "SimplePivotInput.xml");
		assertCsvEquals("SimplePivotOutput.csv", outputFolder.getRoot(), "SimplePivotOutput.csv");
	}
	
}