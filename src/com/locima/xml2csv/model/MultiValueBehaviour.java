package com.locima.xml2csv.model;

import com.locima.xml2csv.StringUtil;

/**
 * Defines how the mapper should behaviour when multiple values are encountered for a single mapping.
 */
public enum MultiValueBehaviour {
	/**
	 * Use default behaviour. Default behaviour depends whether this is used on a {@link MappingConfiguration}, {@link MappingList} or {@link Mapping}
	 * .
	 */
	DEFAULT,
	/**
	 * All values other than the first are discarded.
	 */
	DISCARD,
	/**
	 * An error will be issued on the console (and in the log) if multiple values for a field are encountered in the input data and processing will
	 * stop.
	 */
	ERROR,
	/**
	 * Multiple values for a field are expected and will be inlined in a single record.
	 */
	INLINE,
	/**
	 * Multiple values for a field are expected and will cause multiple records to be generated for each value.
	 */
	MULTI_RECORD,
	/**
	 * As per {@link MultiValueBehaviour#DISCARD}, except that a warning will be issued on the console (and in the log) if multiple values for a field
	 * are encountered in the input data. All values other than the first are discarded.
	 */
	WARN;

	public static MultiValueBehaviour parse(String multiValueBehaviourAsString) {
		if (StringUtil.isNullOrEmpty(multiValueBehaviourAsString)) {
			return DEFAULT;
		}
		String uc = multiValueBehaviourAsString.toUpperCase();
		return MultiValueBehaviour.valueOf(uc);
	}
}