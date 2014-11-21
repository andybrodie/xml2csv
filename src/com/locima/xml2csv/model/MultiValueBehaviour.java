package com.locima.xml2csv.model;

import com.locima.xml2csv.StringUtil;

/**
 * Defines how the mapper should behaviour when multiple values are encountered for a single mapping.
 */
public enum MultiValueBehaviour {
	/**
	 * Use default behaviour. Default behaviour depends whether this is used on a {@link MappingConfiguration}, {@link MappingList} or {@link Mapping}.
	 */
	DEFAULT,
	/**
	 * Multiple values for a field are expected and will be inlined in a single record.
	 */
	INLINE,
	/**
	 * Multiple values for a field are expected and will cause multiple records to be generated for each value.
	 */
	MULTI_RECORD;

	public static MultiValueBehaviour parse(String multiValueBehaviourAsString) {
		if (StringUtil.isNullOrEmpty(multiValueBehaviourAsString)) {
			return DEFAULT;
		}
		String uc = multiValueBehaviourAsString.toUpperCase();
		return MultiValueBehaviour.valueOf(uc);
	}
}