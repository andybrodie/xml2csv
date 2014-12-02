package com.locima.xml2csv.extractor;

import java.util.HashMap;
import java.util.Map;

import com.locima.xml2csv.BugException;
import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.configuration.IValueMapping;

public class ExtractionContextManager {

	private static ExtractionContextManager INSTANCE;

	static {
		INSTANCE = new ExtractionContextManager();
	}

	public static ExtractionContext get(ContainerExtractionContext parent, IMapping mapping) {
		if (INSTANCE.existingContexts.containsKey(mapping)) {
			return INSTANCE.existingContexts.get(mapping);
		} else {
			return INSTANCE.createNewExtractionContext(parent, mapping);
		}
	}

	Map<IMapping, ExtractionContext> existingContexts;

	private ExtractionContextManager() {
		this.existingContexts = new HashMap<IMapping, ExtractionContext>();
	}

	private ExtractionContext createNewExtractionContext(ContainerExtractionContext parent, IMapping mapping) {
		ExtractionContext ctx;
		if (mapping instanceof IValueMapping) {
			ctx = new MappingExtractionContext(parent, ((IValueMapping) mapping));
		} else if (mapping instanceof IMappingContainer) {
			ctx = new ContainerExtractionContext(parent, (IMappingContainer) mapping);
		} else {
			throw new BugException("Passed mapping that is not a value mapping or mapping container: %s", mapping);
		}
		this.existingContexts.put(mapping, ctx);
		return ctx;
	}
}
