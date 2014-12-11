package com.locima.xml2csv.output;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.BugException;
import com.locima.xml2csv.configuration.MultiValueBehaviour;
import com.locima.xml2csv.extractor.ContainerExtractionContext;
import com.locima.xml2csv.extractor.ExtractionContext;

public class GroupState {

	private static final Logger LOG = LoggerFactory.getLogger(GroupState.class);

	public static GroupState createGroupStateList(ContainerExtractionContext rootContext) {

		GroupState inlineGroup = null;
		GroupState initialState = null;

		/*
		 * Iterate over all the contexts (traverse the entire tree) mapping each context to its group. Using a stack to allow us to traverse the tree
		 * without a recursive method. After processing each node, push all the children to the "to do" stack. This is safe because this is a strict
		 * tree, not a graph.
		 */
		Stack<ExtractionContext> remainingContexts = new Stack<ExtractionContext>();
		remainingContexts.push(rootContext);

		while (remainingContexts.size() > 0) {
			ExtractionContext current = remainingContexts.pop();
			switch (current.getMapping().getMultiValueBehaviour()) {
				case GREEDY:
					if (inlineGroup == null) {
						inlineGroup = new InlineGroupState(current);
					}
					inlineGroup.addContext(current);
					break;
				case LAZY:
					int groupNum = current.getMapping().getGroupNumber();

					// Either add to an existing group managing that group number, or create a new one
					if (initialState == null) {
						initialState = new GroupState(groupNum, current);
					} else {
						GroupState existingGroup = GroupState.searchByGroup(initialState, groupNum);

						if (existingGroup == null) {
							GroupState newState = new GroupState(groupNum, current);
							initialState.insert(newState);
						} else {
							existingGroup.addContext(current);
						}
					}
					break;
				case DEFAULT:
				default:
					throw new BugException("Found unexpected MultiValueBehaviour %s in %s", current.getMapping().getMultiValueBehaviour().toString(),
									current.getMapping());

			}
			// Now push all the children to our "to do" stack.
			if (current instanceof ContainerExtractionContext) {
				ContainerExtractionContext cec = (ContainerExtractionContext)current;
				for (List<ExtractionContext> child : cec.getChildren()) {
					remainingContexts.addAll(child);
				}
			}
		}

		// Return the first state in the linked list, as the list is ordered by group
		GroupState firstState = initialState;
		if (firstState != null) {
			while (firstState.prev != null) {
				firstState = firstState.prev;
			}
		}

		// If we have any inline mappings, then put that first
		if (inlineGroup != null) {
			if (firstState != null) {
				firstState.insert(inlineGroup);
			}
			firstState = inlineGroup;
		}

		if (LOG.isTraceEnabled()) {
			logStates(firstState);
		}
		return firstState;
	}

	/**
	 * Internal debugging method that logs the current state of this group state and all it's successors.
	 * 
	 * @param firstState
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

	private static GroupState searchByGroup(GroupState state, int searchGroup) {
		if (state == null) {
			return null;
		}
		if (state.groupNumber == searchGroup) {
			return state;
		} else {
			// Search forward from initialState
			GroupState current = state;
			do {
				if (current.groupNumber == searchGroup) {
					return current;
				}
				current = current.next;
			} while (current != null);
			// Search backwards from initialState
			current = state;
			do {
				if (current.groupNumber == searchGroup) {
					return current;
				}
				current = current.prev;
			} while (current != null);
		}
		// Didn't find one at all
		return null;
	}

	private int currentIndex;
	private int groupNumber;
	private int groupSize;
	protected GroupState next;
	protected GroupState prev;

	private List<ExtractionContext> records;

	public GroupState(int groupNum, ExtractionContext record) {
		this.groupNumber = groupNum;
		this.groupSize = record.size();
		this.records = new ArrayList<ExtractionContext>(1);
		this.records.add(record);
	}

	private void addContext(ExtractionContext record) {
		this.records.add(record);
		setSizeIfBigger(record.size());
	}

	public GroupState findByGroup(int searchGroup) {
		return GroupState.searchByGroup(this, searchGroup);
	}

	public int getCurrentIndex() {
		return this.currentIndex;
	}

	public int getGroupNumber() {
		return this.groupNumber;
	}

	public boolean hasNext() {
		boolean hasNext;
		if (!isExhausted()) {
			hasNext = true;
		} else {
			hasNext = (this.next != null) && this.next.hasNext();
		}
		return hasNext;
	}

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
	 * Inserts the new passed <code>newState</code> in to the list that this is a member of, in the right place.
	 *
	 * @param newState the new state to insert.
	 */
	private void insert(GroupState newState) {
		int newGroupNum = newState.groupNumber;

		if (newGroupNum == this.groupNumber) {
			throw new BugException("Created two group states for the same group number");
		}

		if (newGroupNum > this.groupNumber) {
			if (this.next == null) {
				this.next = newState;
				newState.prev = this;
			} else {
				this.next.insert(newState);
			}
		} else {
			if (this.prev == null) {
				this.prev = newState;
				newState.next = this;
			} else {
				this.prev.insert(newState);
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

	private void setSizeIfBigger(int size) {
		this.groupSize = Math.max(this.groupSize, size);
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
