package com.locima.xml2csv.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.StringUtil;

/**
 * Defines how the mapper should behaviour when multiple values are encountered for a single mapping.
 */
public enum MultiValueBehaviour {

	/**
	 * Use default behaviour. Default behaviour depends whether this is used on a {@link MappingConfiguration}, {@link MappingList} or {@link Mapping}
	 * .
	 * <p>
	 * {@link MappingList}s are greedy by default. {@link Mapping}s are lazy by default.
	 */
	DEFAULT,
	/**
	 * A greedy mapping tries to output all of its results at once.
	 * <p>
	 * This is typically used for "inlining" multiple values; i.e. making multiple values for a single mapping appear in a single record.
	 */
	GREEDY,
	/**
	 * A lazy mapping outputs a single field then moves on.
	 */
	LAZY;

	private static final Logger LOG = LoggerFactory.getLogger(MultiValueBehaviour.class);

	public static MultiValueBehaviour parse(String multiValueBehaviourAsString) {
		if (StringUtil.isNullOrEmpty(multiValueBehaviourAsString)) {
			LOG.debug("Parsing {} to {}", multiValueBehaviourAsString == null ? "(null)" : multiValueBehaviourAsString, MultiValueBehaviour.DEFAULT);
			return DEFAULT;
		}
		String uc = multiValueBehaviourAsString.toUpperCase();
		MultiValueBehaviour retVal = ("GREEDY".equals(uc) || "INLINE".equals(uc)) ? GREEDY : LAZY;
		LOG.debug("Parsing {} to {}", multiValueBehaviourAsString, retVal);
		return retVal;
	}

}