package com.locima.xml2csv.output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.configuration.MultiValueBehaviour;
import com.locima.xml2csv.output.direct.DirectOutputRecordIterator;

/**
 * Used by {@link DirectOutputRecordIterator} to keep track of where a specific evaluation group (see {@link IMapping#getGroupNumber()} for a mapping
 * has got to in outputting its results across multiple records. This is only required when dealing with {@link MultiValueBehaviour#LAZY} mappings, as
 * {@link MultiValueBehaviour#GREEDY} mappings always output all their values for every record.
 * <p>
 */
public class GroupState {

	private static final Logger LOG = LoggerFactory.getLogger(GroupState.class);

	/**
	 * Sets in the {@link #prev} and {@link #next} attributes of an ordered list of {@link GroupState} instances appropriately.
	 *
	 * @param groupStates an ordered collection of {@link GroupState}.
	 * @return the first {@link GroupState} in the linked list (i.e. {@link #prev}==<code>null</code>).
	 */
	private static GroupState buildLinkedList(Collection<GroupState> groupStates) {
		GroupState[] gsArray = groupStates.toArray(new GroupState[0]);
		for (int i = 0; i < gsArray.length; i++) {
			if (i > 0) {
				gsArray[i].prev = gsArray[i - 1];
			}
			if (i < (gsArray.length - 1)) {
				gsArray[i].next = gsArray[i + 1];
			}
		}
		if (LOG.isTraceEnabled()) {
			logStates(gsArray[0]);
		}
		return gsArray[0];
	}

	/**
	 * Factory method to create a linked list of {@link GroupState} instances based on a root mapping.
	 * <p>
	 * As a root {@link IMappingContainer} must output to a distinct CSV file there is no need to deal with multiple root contexts.
	 *
	 * @param rootContext the object responsible for evaluating a root {@link IMappingContainer}
	 * @return the head ofa linked list of {@link GroupState} objects.
	 */
	public static GroupState createGroupStateList(IExtractionResults rootContext) {
		if (LOG.isInfoEnabled()) {
			LOG.info("Creating group state list from {}", rootContext);
		}

		/*
		 * Iterate over all the contexts (traverse the entire tree) mapping each context to its group. Using a stack to allow us to traverse the tree
		 * without a recursive method. After processing each node, push all the children to the "to do" stack. This is safe because this is a strict
		 * tree, not a graph.
		 */
		Stack<IExtractionResults> remainingContexts = new Stack<IExtractionResults>();
		remainingContexts.push(rootContext);

		SortedMap<Integer, GroupState> groupStates = new TreeMap<Integer, GroupState>();

		while (remainingContexts.size() > 0) {
			IExtractionResults current = remainingContexts.pop();
			int groupNumber = current.getGroupNumber();
			GroupState state = groupStates.get(groupNumber);
			if (state == null) {
				if (current.getMultiValueBehaviour() == MultiValueBehaviour.GREEDY) {
					state = new GreedyGroupState(groupNumber);
				} else {
					state = new GroupState(groupNumber);
				}
				groupStates.put(groupNumber, state);
			}
			state.addContext(current);

			// Now push all the children to our "to do" stack to ensure that we deal with every results container.
			if (current instanceof IExtractionResultsContainer) {
				IExtractionResultsContainer cec = (IExtractionResultsContainer) current;
				for (List<IExtractionResults> child : cec.getChildren()) {
					remainingContexts.addAll(child);
				}
			}
		}

		return buildLinkedList(groupStates.values());
	}

	/**
	 * Internal debugging method that logs the current state of this group state and all it's successors.
	 *
	 * @param firstState the first state to log.
	 */
	private static void logStates(GroupState firstState) {
		GroupState state = firstState;
		int count = 0;
		LOG.trace("GroupStates START");
		while (state != null) {
			LOG.trace("{}: {}", count, state);
			state = state.next;
		}
		LOG.trace("GroupStates END");
	}

	/**
	 * Search for a group, starting at the state passed, looking for a group with the {@link #groupNumber} specified by <code>searchGroup</code>.
	 *
	 * @param state the state to start searching from.
	 * @param searchGroup the group to search for.
	 * @return the group state that tracks the group number specified by <code>searchGroup</code>, or null if one does not exist.
	 */
	// CHECKSTYLE:OFF Return count is high, but efficient.
	private static GroupState searchByGroup(GroupState state, int searchGroup) {
		// CHECKSTYLE:ON
		if (state == null) {
			return null;
		}
		if (state.groupNumber == searchGroup) {
			return state;
		}

		GroupState current = state;

		if (searchGroup > state.groupNumber) {
			current = current.next;
			while (current != null) {
				if (current.groupNumber == searchGroup) {
					return current;
				}
				if (searchGroup < current.groupNumber) {
					return null;
				}
				current = current.next;
			}
		} else {
			// searchGroup < state.groupNumber
			current = current.prev;
			while (current != null) {
				if (current.groupNumber == searchGroup) {
					return current;
				}
				if (searchGroup > current.groupNumber) {
					return null;
				}
				current = current.prev;
			}
		}

		return null;
	}

	private int currentIndex;
	private int groupNumber;
	private int groupSize;
	protected GroupState next;
	protected GroupState prev;

	private List<IExtractionResults> records;

	/**
	 * Creates a new instance with a specific group number and initial associated set of results.
	 *
	 * @param groupNumber the group number that this instance will track. Must be unique.
	 */
	public GroupState(int groupNumber) {
		this.groupNumber = groupNumber;
		this.records = new ArrayList<IExtractionResults>(1);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Creating new group state {}", groupNumber);
		}

	}

	/**
	 * Associates the <code>record</code> passed with this group.
	 *
	 * @param record the set of results to associated with this group.
	 */
	private void addContext(IExtractionResults record) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Adding {} ({} elements) to existing group state {}", record, record.size(), this);
		}
		this.records.add(record);
		this.groupSize = Math.max(this.groupSize, Math.max(record.size(), record.getMinCount()));
	}

	/**
	 * Finds the group with the index <code>searchIndex</code>. If one does not exist then <code>null</code> is returned.
	 *
	 * @param searchIndex the index to search for.
	 * @return the group with the index <code>searchIndex</code>. If one does not exist then <code>null</code> is returned.
	 */
	public GroupState findGroup(int searchIndex) {
		return GroupState.searchByGroup(this, searchIndex);
	}

	/**
	 * Return the current index of this {@link GroupState}, i.e. how mnay results have been returned from this.
	 *
	 * @return the current index of this {@link GroupState}, i.e. how mnay results have been returned from this.
	 */
	public int getCurrentIndex() {
		return this.currentIndex;
	}

	/**
	 * Return the group number assigned to this group.
	 *
	 * @return the group number assigned to this group.
	 */
	public int getGroupNumber() {
		return this.groupNumber;
	}

	/**
	 * Return true if there are more results to yield from this object, false if it is exhausted.
	 *
	 * @return true if there are more results to yield from this object, false if it is exhausted.
	 */
	public boolean hasNext() {
		boolean hasNext;
		if (!isExhausted()) {
			hasNext = true;
		} else {
			hasNext = (this.next != null) && this.next.hasNext();
		}
		return hasNext;
	}

	/**
	 * Increments this group to the next index (see {@link #getCurrentIndex()}. If this group is exhausted then it will increment the next group (if
	 * there is one).
	 */
	public void increment() {
		this.currentIndex++;
		if (this.currentIndex < (this.groupSize)) {
			LOG.debug("Incremented group {} to {} out of {}", this.groupNumber, this.currentIndex, this.groupSize);
			GroupState prevState = this.prev;
			while (prevState != null) {
				LOG.debug("Resetting group {} from {} as higher group {} has just incremented.", prevState.groupNumber, prevState.currentIndex,
								this.groupNumber);
				prevState.currentIndex = 0;
				prevState = prevState.prev;
			}
		} else {
			LOG.debug("Cannot increment group {} as currentIndex={} and groupSize={}", this.groupNumber, this.currentIndex, this.groupSize);
			if (this.next != null) {
				this.next.increment();
			}
		}
	}

	/**
	 * Determines whether this group has any more values to yield.
	 *
	 * @return true if any of the {@link ExtractedRecord}s that use this group have another value that can be returned.
	 */
	private boolean isExhausted() {
		boolean exhausted = this.currentIndex >= this.groupSize;
		if (LOG.isTraceEnabled()) {
			LOG.trace("{} is {}exhausted as current index is {} out of {}", this, exhausted ? "" : "not ", this.currentIndex, this.groupSize);
		}
		return exhausted;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("GroupState(GN=");
		sb.append(this.groupNumber);
		sb.append(", GS=");
		sb.append(this.groupSize);
		sb.append(", CI=");
		sb.append(this.currentIndex);
		sb.append(')');
		return sb.toString();
	}
}
