package com.locima.xml2csv.model;


/**
 * Specifies the valid range of values for controlling inline behaviour.
 */
public enum MultiValueBehaviour {
	/**
	 * An error will be issued on the console (and in the log) if multiple values for a field are encountered in the input data and processing will
	 * stop.
	 */
	ERROR,
	/**
	 * Multiple values for a field are ignored and considered expected. This is the default.
	 */
	IGNORE,
	/**
	 * A warning will be issued on the console (and in the log) if multiple values for a field are encountered in the input data.
	 */
	WARN,
	/**
	 * Inherit from a parent (if no parent is available then {@link #IGNORE} will be used).
	 */
	INHERIT
	
}
