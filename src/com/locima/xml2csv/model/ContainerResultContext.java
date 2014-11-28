package com.locima.xml2csv.model;

/**
 * <ol>
 * <li>When a container is iterated multiple times for different mapping roots,</li>
 * <ii>create a new ContainerResultContext, which links the mapping or container (IMapping) to the Root (integer index)</li>
 * <li>each Container/Index tuple maps to a list of child tree context (for Containers), or a set of values (for Mappings)</li> 
 * </ol>
 * 
 */
public class ContainerResultContext {
	
	private IMapping generator;
	private int index;
	
	public ContainerResultContext(IMappingContainer mapping, int index) {
		
	}
	
}
