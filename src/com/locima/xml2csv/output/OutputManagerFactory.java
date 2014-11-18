package com.locima.xml2csv.output;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.model.IMapping;
import com.locima.xml2csv.model.IMappingContainer;
import com.locima.xml2csv.model.MappingConfiguration;
import com.locima.xml2csv.model.MultiValueBehaviour;

// CHECKSTYLE:OFF An abstract factor here would be nonsensical.
/**
 * Used to create {@link IOutputManager} instances, using a concrete implementation that is suitable for the mapping configuration passed.
 */
public class OutputManagerFactory {
	// CHECKSTYLE:ON
	private static final Logger LOG = LoggerFactory.getLogger(OutputManagerFactory.class);

	/**
	 * Creates an appropriate {#link {@link IOutputManager} based on the mapping configuration provided. The decision logic for which implementation
	 * to use is based on whether a mapping configuration contains any unbounded inline mappings. These produce a variable number of field values in
	 * any given record. If one is found then it means that we can't directly stream out a CSV file using {@link DirectCsvWriter} (because we wouldn't
	 * know how many fields to include in any recrd), so we have to use {@link InlineCsvWriter} instead.
	 * 
	 * @param config the mapping configuration that we are going to output the results of.
	 * @return never returns null.
	 */
	public static IOutputManager create(MappingConfiguration config) {
		IOutputManager outputManager;
		if (includesUnboundedInline(config)) {
			LOG.info("Unbounded inline detected, therefore using the InlineCsvWriter");
			outputManager = new InlineCsvWriter();
		} else {
			LOG.info("No unbounded potential detected, therefore using the DirectCsvWriter");
			outputManager = new DirectCsvWriter();
		}
		return outputManager;

	}

	/**
	 * This is the logic to determine which {@link IOutputManager} is appropriate.
	 * 
	 * @param container the mapping container to search for unbounded inline mappings within.
	 * @return true if an bounded inline mapping was found, false otherwise.
	 */
	private static boolean includesUnboundedInline(IMappingContainer container) {
		LOG.debug("Checking {} for inline multi-value behaviour", container);
		for (IMapping mapping : container) {
			if (mapping.getMultiValueBehaviour() == MultiValueBehaviour.INLINE) {
				LOG.debug("MappingConfiguraton contains a inline configuration for {}, therefore returning true", mapping);
				return true;
			}
			if (mapping instanceof IMappingContainer) {
				return includesUnboundedInline((IMappingContainer) mapping);
			}
		}
		return false;
	}

	private static boolean includesUnboundedInline(MappingConfiguration config) {
		for (IMappingContainer container : config) {
			if (includesUnboundedInline(container)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Prevents instantiation.
	 */
	private OutputManagerFactory() {
	}
}
