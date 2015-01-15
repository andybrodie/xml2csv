package com.locima.xml2csv.output;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.BugException;
import com.locima.xml2csv.configuration.IMapping;
import com.locima.xml2csv.configuration.IMappingContainer;
import com.locima.xml2csv.configuration.MultiValueBehaviour;
import com.locima.xml2csv.output.direct.DirectOutputRecordIterator;

/**
 * Used by {@link DirectOutputRecordIterator} to keep track of where a specific evaluation group (see {@link IMapping#getGroupNumber()} for a mapping
 * has got to in outputting its results across multiple records. This is only required when dealing with {@link MultiValueBehaviour#LAZY} mappings, as
 * {@link MultiValueBehaviour#GREEDY} mappings always output all their values for every record.
 * <p>
 * TODO Refactor this as a Map, I can't actually see any need for this to be a linked list. It's just more code for virtually no gain.
 */
public class GroupState {

	private static final Logger LOG = LoggerFactory.getLogger(GroupState.class);

	/**
	 * Factory method to create a linked list of {@link GroupState} instances based on a root mapping.
	 * <p>
	 * As a root {@link IMappingContainer} must output to a distinct CSV file there is no need to deal with multiple root contexts.
	 *
	 * @param rootContext the object responsible for evaluating a root {@link IMappingContainer}
	 * @return the head ofa linked list of {@link GroupState} objects.
	 */
	public static GroupState createGroupStateList(IExtractionResults rootContext) {

		LOG.info("Creating group state list from {}", rootContext);

		GroupState inlineGroup = null;
		GroupState initialState = null;

		/*
		 * Iterate over all the contexts (traverse the entire tree) mapping each context to its group. Using a stack to allow us to traverse the tree
		 * without a recursive method. After processing each node, push all the children to the "to do" stack. This is safe because this is a strict
		 * tree, not a graph.
		 */
		Stack<IExtractionResults> remainingContexts = new Stack<IExtractionResults>();
		remainingContexts.push(rootContext);

		while (remainingContexts.size() > 0) {
			IExtractionResults current = remainingContexts.pop();
			switch (current.getMultiValueBehaviour()) {
				case GREEDY:
					if (inlineGroup == null) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("Creating new inline group state for {}", current);
						}
						inlineGroup = new GreedyGroupState(current);
					}
					inlineGroup.addContext(current);
					break;
				case LAZY:
					int groupNum = current.getGroupNumber();

					// Either add to an existing group managing that group number, or create a new one
					if (initialState == null) {
						if (LOG.isInfoEnabled()) {
							LOG.info("Creating initial state for group {} for {}", groupNum, current);
						}
						initialState = new GroupState(groupNum, current);
					} else {
						GroupState existingGroup = GroupState.searchByGroup(initialState, groupNum);

						if (existingGroup == null) {
							if (LOG.isInfoEnabled()) {
								LOG.info("Creating new group state for {} ({})", current, groupNum);
							}
							GroupState newState = new GroupState(groupNum, current);
							initialState.insert(newState);
						} else {
							if (LOG.isDebugEnabled()) {
								LOG.debug("Adding {} to existing group state {}", current, existingGroup);
							}
							existingGroup.addContext(current);
						}
					}
					break;
				case DEFAULT:
				default:
					throw new BugException("Found unexpected MultiValueBehaviour %s in %s", current.getMultiValueBehaviour().toString(), current);

			}
			// Now push all the children to our "to do" stack.
			if (current instanceof IExtractionResultsContainer) {
				IExtractionResultsContainer cec = (IExtractionResultsContainer) current;
				for (List<IExtractionResults> child : cec.getChildren()) {
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

	private List<IExtractionResults> records;

	public GroupState(int groupNum, IExtractionResults record) {
		this.groupNumber = groupNum;
		this.groupSize = Math.max(record.size(), record.getMinCount());
		this.records = new ArrayList<IExtractionResults>(1);
		this.records.add(record);
	}

	private void addContext(IExtractionResults record) {
		this.records.add(record);
		this.groupSize = Math.max(this.groupSize, record.size());
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

		LOG.debug("Attempting  to insert {} at state {}", newState, this);

		if (newGroupNum == this.groupNumber) {
			throw new BugException("Created two group states for the same group number.  New: %s and Existing: %S", this, newState);
		}

		if (newGroupNum > this.groupNumber) {
			if (this.next == null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Adding {} to the end, after {}", newState, this);
				}
				this.next = newState;
				newState.prev = this;
			} else {
				if (this.next.groupNumber > this.groupNumber) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Inserting {} between {} and {}", newState, this, this.next);
					}
					this.next.prev = newState;
					newState.next = this.next;
					newState.prev = this;
					this.next = newState;
				} else {
					this.next.insert(newState);
				}
			}
		} else {
			if (this.prev == null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Adding {} to the beginning, before {}", newState, this);
				}
				this.prev = newState;
				newState.next = this;
			} else {
				if (this.prev.groupNumber < this.groupNumber) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Inserting {} between {} and {}", newState, this, this.prev);
					}
					this.prev.next = newState;
					newState.prev = this.prev;
					newState.next = this;
					this.prev = newState;
				} else {
					this.prev.insert(newState);
				}
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
