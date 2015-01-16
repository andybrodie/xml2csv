package com.locima.xml2csv.output;

/**
 * Specialisation of {@link GroupState} used specifically for greedy mappings.
 * <p>
 * Greedy mappings always output all of the available values, so their behaviour is slightly different : they are always exhausted after outputting
 * once, so after the first call to {@link #increment()}, they always return <code>false</code> for a call to {@link #hasNext()}.
 */
public class GreedyGroupState extends GroupState {

	/**
	 * The greedy group state (there is only one) always takes the value -1, so it's the first group state in the list.
	 */
	private static final int GREEDY_GROUP_NUMBER = -1;

	private boolean exhausted;

	/**
	 * Initialises a new instance. No need to specify inline group number as it's always a constant (enforced by this class).
	 *
	 * @param record the extraction context that the greedy group state is responsible for.
	 */
	public GreedyGroupState(IExtractionResults record) {
		super(GREEDY_GROUP_NUMBER, record);
	}

	/**
	 * Greedy groups only need to output once before they're exhausted. Only lazy groups can output in a record and not be exhausted (because they're
	 * outputting one value per record).
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
