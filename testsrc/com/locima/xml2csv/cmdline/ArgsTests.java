package com.locima.xml2csv.cmdline;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.junit.Before;
import org.junit.Test;

public class ArgsTests {

	private Options options;
	private Parser parser;

	private String createPName(String optionName) {
		return "-" + optionName;
	}

	@Before
	public void initialise() {
		this.options = Program.MAIN_OPTIONS;
		this.parser = new BasicParser();
	}

	private CommandLine parse(String... args) throws ParseException {
		return this.parser.parse(this.options, args);
	}

	@Test
	public void testSwitches() throws ParseException {
		assertEquals(true,
						parse(createPName(Program.OPT_CONFIG_FILE), "x", createPName(Program.OPT_APPEND_OUTPUT)).hasOption(Program.OPT_APPEND_OUTPUT));
		assertEquals(false,
						parse(createPName(Program.OPT_CONFIG_FILE), "x", createPName(Program.OPT_APPEND_OUTPUT)).hasOption(
										Program.OPT_TRIM_WHITESPACE));
		assertEquals(true,
						parse(createPName(Program.OPT_CONFIG_FILE), "x", createPName(Program.OPT_TRIM_WHITESPACE)).hasOption(
										Program.OPT_TRIM_WHITESPACE));
	}

	@Test
	public void testTypicalArguments() throws ParseException {
		CommandLine cmdLine =
						parse(createPName(Program.OPT_CONFIG_FILE), "cValue", createPName(Program.OPT_APPEND_OUTPUT),
										createPName(Program.OPT_OUT_DIR), "outputValue", "input1", "input2", "input3");

		assertEquals("cValue", cmdLine.getOptionValue(Program.OPT_CONFIG_FILE));
		assertArrayEquals(new String[] { "input1", "input2", "input3" }, cmdLine.getArgs());
		assertEquals("outputValue", cmdLine.getOptionValue(Program.OPT_OUT_DIR));
		assertEquals(true, cmdLine.hasOption(Program.OPT_APPEND_OUTPUT));
		assertEquals(false, cmdLine.hasOption(Program.OPT_TRIM_WHITESPACE));
	}

}
