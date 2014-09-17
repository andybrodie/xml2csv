package com.locima.xml2csv.output;

import java.io.File;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import com.locima.xml2csv.Tuple;

/**
 * Provided ONLY for unit testing.
 */
public interface IOutputManager {

	void close();

	void createFiles(Map<String, List<String>> headers) throws OutputManagerException;

	Map<String, Tuple<File, Writer>> getWriterFiles() throws OutputManagerException;

	void setDirectory(String outputDirectoryName) throws OutputManagerException;

	void writeRecords(String writerName, List<String> values) throws OutputManagerException;

}
