package com.locima.xml2csv;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FileUtilityTests {

	@Test(expected=IOException.class)
	public void testGetDirectoryMissing() throws Exception {
		TemporaryFolder temp = new TemporaryFolder();
		temp.create();
		String missingFolder = new File(temp.getRoot(), "MISSING").getAbsolutePath(); 
		FileUtility.getDirectory(missingFolder, 0, false);
	}

	@Test
	public void testGetDirectoryOk() throws Exception {
		TemporaryFolder temp = new TemporaryFolder();
		temp.create();
		String existingFolder = temp.getRoot().getAbsolutePath(); 
		FileUtility.getDirectory(existingFolder, 0, false);
	}

	@Test
	public void testGetDirectoryCreate() throws Exception {
		TemporaryFolder temp = new TemporaryFolder();
		temp.create();
		String missingFolder = new File(temp.getRoot(), "CREATED").getAbsolutePath(); 
		FileUtility.getDirectory(missingFolder, 0, true);
		assertTrue(new File(missingFolder).exists());
	}

}
