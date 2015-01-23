package com.locima.xml2csv.configuration;

import java.util.Stack;

import com.locima.xml2csv.util.Tuple;

/**
 * A marker interface for tracking the ancestry of of a {@link IMapping} instance when creating its name. Each member of the stack contains the name
 * of the parent mapping list (or <code>null</code> if this {@link MappingList} is a direct child of the {@link MappingConfiguration}, along with the
 * iteration of the parent mapping list that we're currently within.
 * <p>
 * Instaces of this object are used by {@link NameFormat#format(String, int, MappingIndexAncestors)} to create the name of a field.
 */
public class MappingIndexAncestors extends Stack<Tuple<String, Integer>> {

	private static final long serialVersionUID = -6623538363663390090L;

	/**
	 * Creates the parameters array for a {@link String#format(String, Object...)} call with a {@link NameFormat} instance based on the mapping name
	 * and iteration index passed. The first and second values in the array will be the parameters passed, the rest are made up of the current
	 * ancestry stack.
	 *
	 * @param thisMappingName the name of the mapping to make the first parameter.
	 * @param thisMappingIteration the index of the mapping to make the second parameter.
	 * @return
	 */
	public Object[] getFormatArgs(String thisMappingName, int thisMappingIteration) {
		Object[] args = new Object[(size() * 2) + 2];
		int size = size();
		args[0] = thisMappingName;
		args[1] = thisMappingIteration + 1;
		for (int i = 0; i < size; i++) {
			args[(i * 2) + 2] = get(size - 1 - i).getFirst();
			args[(i * 2) + 3] = get(size - 1 - i).getSecond() + 1;
		}
		return args;

	}

	/**
	 * Convenience method to peek the current iteration number at the top of the stack.
	 *
	 * @return the parent iteration number at the top of the stack.
	 */
	public int peekIterationNumber() {
		return peek().getSecond();
	}

	/**
	 * Convenience method to peek the current mapping name at the top of the stack.
	 *
	 * @return the parent mapping name at the top of the stack.
	 */
	public String peekMappingName() {
		return peek().getFirst();
	}

	/**
	 * Convenience method to push a new {@link Tuple} to the top of the stack.
	 *
	 * @param mappingName the name of the mapping, which forms the {@link Tuple#getFirst()} value.
	 * @param iterationNumber the iteration of the mapping, which forms the {@link Tuple#getSecond()} value.
	 */
	public void push(String mappingName, int iterationNumber) {
		this.push(new Tuple<String, Integer>(mappingName, iterationNumber));
	}
}
