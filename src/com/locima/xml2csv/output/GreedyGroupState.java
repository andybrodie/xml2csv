package com.locima.xml2csv.output;

// CHECKSTYLE:OFF Checkstyle bug, this is used in javadoc.
import com.locima.xml2csv.configuration.IMapping;

// CHECKSTYLE:ON

/**
 * Specialisation of {@link GroupState} used specifically for greedy mappings.
 * <p>
 * Greedy mappings always output all of the available values, so their behaviour is slightly different : they are always exhausted after outputting
 * once, so after the first call to {@link #increment()}, they always return <code>false</code> for a call to {@link #hasNext()}.
 */
public class GreedyGroupState extends GroupState {

	private boolean exhausted;

	/**
	 * Create a new instance.
	 *
	 * @param groupNumber the greedy group number. This is typically forced to -1 by {@link IMapping#getGroupNumber()}, but we don't enforce it here.
	 */
	public GreedyGroupState(int groupNumber) {
		super(groupNumber);
	}

	/**
	 * Greedy groups only need to output once before they're exhausted. Only lazy groups can output in a record and not be exhausted (because they're
	 * outputting one value per record).
	 *
	 * @return <code>true</code> once this mapping has been executed at least once.
	 */
	@Override
	public boolean hasNext() {
		return this.exhausted ? (this.next != null ? this.next.hasNext() : false) : true;
	}

	/**
	 * If a greedy state is incremented, then as it's always exhausted before incrementing, then pass this on to the next (lazy) group state.
	 */
	@Override
	public void increment() {
		this.exhausted = true;
		if (this.next != null) {
			this.next.increment();
		}
	}

}
