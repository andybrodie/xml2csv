package com.locima.xml2csv.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.locima.xml2csv.util.Tuple;

/**
 * A marker interface for tracking the ancestry of of a {@link IMapping} instance when creating its name. Each member of the stack contains the name
 * of the parent mapping list (or <code>null</code> if this {@link MappingList} is a direct child of the {@link MappingConfiguration}, along with the
 * iteration of the parent mapping list that we're currently within.
 * <p>
 * Instaces of this object are used by {@link NameFormat#format(String, int, MappingIndexAncestors)} to create the name of a field.
 */
public class MappingIndexAncestors {

	private List<Tuple<String, Integer>> queue;

	/**
	 * Initialise a new instance.
	 */
	public MappingIndexAncestors() {
		this.queue = new ArrayList<Tuple<String, Integer>>();
	}

	/**
	 * Creates the parameters array for a {@link String#format(String, Object...)} call with a {@link NameFormat} instance based on the mapping name
	 * and iteration index passed. The first and second values in the array will be the parameters passed, the rest are made up of the current
	 * ancestry stack.
	 *
	 * @param thisMappingName the name of the mapping to make the first parameter.
	 * @param thisMappingIteration the index of the mapping to make the second parameter.
	 * @return an array suitable for passing to {@link NameFormat#format(String, int, MappingIndexAncestors)}
	 */
	public Object[] getFormatArgs(String thisMappingName, int thisMappingIteration) {
		int size = this.queue.size();
		Object[] args = new Object[(size * 2) + 2];
		args[0] = thisMappingName;
		args[1] = thisMappingIteration + 1;
		for (int i = 0; i < size; i++) {
			args[(i * 2) + 2] = this.queue.get(i).getFirst();
			// CHECKSTYLE:OFF Could refactor this to avoid magic number "3", but not worth the extra complexity
			args[(i * 2) + 3] = this.queue.get(i).getSecond() + 1;
			// CHECKSTYLE:ON
		}
		return args;
	}

	/**
	 * Convenience method to push a new {@link Tuple} to the top of the stack.
	 *
	 * @param mappingName the name of the mapping, which forms the {@link Tuple#getFirst()} value.
	 * @param iterationNumber the iteration of the mapping, which forms the {@link Tuple#getSecond()} value.
	 */
	public void push(String mappingName, int iterationNumber) {
		this.queue.add(0, new Tuple<String, Integer>(mappingName, iterationNumber));
	}

	public void pop() {
		this.queue.remove(0);
	}
}
