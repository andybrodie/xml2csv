package com.locima.xml2csv.util;

import static com.locima.xml2csv.TestHelpers.assertIterableEquals;
import static com.locima.xml2csv.TestHelpers.assertSameContents;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FileUtilityTests {

	private File createDirectory(File parent, String dirName) {
		File dir = new File(parent, dirName);
		if (!dir.mkdirs()) {
			fail("Unable to create directory " + dir);
		}
		return dir;
	}

	private List<File> createFile(File parent, String... names) throws IOException {
		List<File> files = new ArrayList<File>(names.length);
		for (int i = 0; i < names.length; i++) {
			File file = new File(parent, names[i]);
			if (!file.createNewFile()) {
				fail("Unable to create file " + i + ": " + file);
			}
			files.add(file);
		}
		return files;
	}

	private File createTempDir() throws IOException {
		TemporaryFolder outputFolder = new TemporaryFolder();
		outputFolder.create();

		File root = outputFolder.getRoot();
		return root;

	}

	@Test
	public void getFilesDirectoryTest() throws IOException {
		File root = createTempDir();
		List<File> expected = new ArrayList<File>();

		expected.addAll(createFile(root, "RootChild1.txt"));

		File subDir = createDirectory(root, "dir");
		assertSameContents(expected, FileUtility.getFiles(root, false));
		createFile(subDir, "Child1.txt", "Child2.txt");
		assertSameContents(expected, FileUtility.getFiles(root, false));
		expected.add(new File(subDir, "Child1.txt"));
		expected.add(new File(subDir, "Child2.txt"));
		assertSameContents(expected, FileUtility.getFiles(root, true));
	}

	@Test
	public void getFilesSimpleTest() throws IOException {
		File root = createTempDir();
		List<File> expected = new ArrayList<File>();
		expected.addAll(createFile(root, "Child1.txt"));
		assertSameContents(expected, FileUtility.getFiles(root, false));
		expected.addAll(createFile(root, "Child2.txt"));
		assertSameContents(expected, FileUtility.getFiles(root, false));
		expected.addAll(createFile(root, "Child3.txt"));
		assertSameContents(expected, FileUtility.getFiles(root, false));
	}
}
