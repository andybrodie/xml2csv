package com.locima.xml2csv.extractor;

import com.locima.xml2csv.ArgumentNullException;
import com.locima.xml2csv.BugException;
import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.configuration.IValueMapping;

public class ExtractionContextManager {

	public ExtractionContextManager() {
		
	}
	
	public ExtractionContext create(ContainerExtractionContext parent, IMapping mapping, int index) {
		ExtractionContext ctx;
		if (mapping==null) throw new ArgumentNullException("mapping");
		if (mapping instanceof IValueMapping) {
			ctx = new MappingExtractionContext(parent, ((IValueMapping) mapping));
		} else if (mapping instanceof IMappingContainer) {
			ctx = new ContainerExtractionContext(parent, (IMappingContainer) mapping, index);
		} else {
			throw new BugException("Passed mapping that is not a value mapping or mapping container: %s", mapping);
		}
		return ctx;
	}

}
