package com.locima.xml2csv;

import static com.locima.xml2csv.TestHelpers.assertCsvEquals;
import static com.locima.xml2csv.TestHelpers.processFiles;
import static com.locima.xml2csv.TestHelpers.processFilesAsCmdLine;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class EndToEndTests {

	@Test
	public void testCmdLineEntryPoint() throws Exception {
		TemporaryFolder outputFolder = processFilesAsCmdLine("FruitBasketConfig.xml", "FruitBasket.xml");
		assertCsvEquals("FruitBasket.csv", outputFolder.getRoot(), "FruitBasket.csv");
	}

	@Test
	public void testEndToEnd() throws Exception {
		TemporaryFolder outputFolder = processFiles("SimpleFamilyConfig.xml", "SimpleFamily1.xml", "SimpleFamily2.xml");
		assertCsvEquals("SimpleFamilyOutput1.csv", outputFolder.getRoot(), "Family.csv");
		assertCsvEquals("SimpleFamilyOutput2.csv", outputFolder.getRoot(), "People.csv");

	}

	@Test
	public void testEndToEndWithInline() throws Exception {
		TemporaryFolder outputFolder = processFiles("SimpleFamilyInlineConfig.xml", "SimpleFamily1.xml", "SimpleFamily2.xml");
		assertCsvEquals("SimpleFamilyInlineOutput.csv", outputFolder.getRoot(), "family.csv");
	}

	@Test
	public void testEndToEndWithInlineDirect() throws Exception {
		TemporaryFolder outputFolder = processFiles("SimpleFamilyInlineDirectConfig.xml", "SimpleFamily1.xml", "SimpleFamily2.xml");
		assertCsvEquals("SimpleFamilyInlineOutput.csv", outputFolder.getRoot(), "family.csv");
	}

	@Test
	public void testFilters() throws Exception {
		TemporaryFolder outputFolder = processFiles("PeopleFilterConfig.xml", "Person1.xml", "Person2.xml", "Person3.xml");
		assertCsvEquals("PeopleFiltered.csv", outputFolder.getRoot(), "PeopleFiltered.csv");
	}

	@Test
	public void testFruitBaskets() throws Exception {
		TemporaryFolder outputFolder = processFiles("FruitBasketConfig.xml", "FruitBasket.xml");
		assertCsvEquals("FruitBasket.csv", outputFolder.getRoot(), "FruitBasket.csv");
	}

	@Test
	public void testGroups() throws Exception {
		TemporaryFolder outputFolder = processFiles("GroupDemoConfig.xml", "GroupDemo.xml");
		assertCsvEquals("GroupDemo1.csv", outputFolder.getRoot(), "GroupDemo1.csv");
		assertCsvEquals("GroupDemo2.csv", outputFolder.getRoot(), "GroupDemo2.csv");
		assertCsvEquals("GroupDemo3.csv", outputFolder.getRoot(), "GroupDemo3.csv");
	}

	@Test
	public void testNamespaces() throws Exception {
		TemporaryFolder outputFolder = processFiles("FamilyConfigWithNamespaces.xml", "FamilyWithNamespaces.xml");
		assertCsvEquals("FamilyWithNamespaces.csv", outputFolder.getRoot(), "FamilyWithNamespaces.csv");
		assertCsvEquals("FamilyMembersWithNamespaces.csv", outputFolder.getRoot(), "FamilyMembersWithNamespaces.csv");
	}

	@Test
	public void testPeople() throws Exception {
		TemporaryFolder outputFolder = processFiles("PeopleConfig.xml", "People.xml");
		assertCsvEquals("People.csv", outputFolder.getRoot(), "People.csv");
	}

	@Test
	public void testPivot() throws Exception {
		TemporaryFolder outputFolder = processFiles("SimplePivotConfig.xml", "SimplePivotInput.xml");
		assertCsvEquals("SimplePivotOutput.csv", outputFolder.getRoot(), "SimplePivotOutput.csv");
	}

	@Test
	public void testPivotInContainer() throws Exception {
		TemporaryFolder outputFolder = processFiles("SimplePivotInContainerConfig.xml", "SimplePivotInput.xml");
		assertCsvEquals("SimplePivotOutput.csv", outputFolder.getRoot(), "SimplePivotOutput.csv");
	}

	@Test
	public void testPivotInputWithNormalMapping() throws Exception {
		TemporaryFolder outputFolder = processFiles("SimplePivotFlatConfig.xml", "SimplePivotInput.xml");
		assertCsvEquals("SimplePivotOutput.csv", outputFolder.getRoot(), "SimplePivotOutput.csv");
	}
}
