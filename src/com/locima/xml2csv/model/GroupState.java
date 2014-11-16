package com.locima.xml2csv.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locima.xml2csv.BugException;

public class GroupState {

	private static final Logger LOG = LoggerFactory.getLogger(GroupState.class);

	public static GroupState createGroupStateList(Collection<? extends MappingRecord> records) {
		if (records.size() == 0) {
			return null;
		}

		GroupState inlineGroup = null;
		GroupState initialState = null;

		for (MappingRecord record : records) {
			if (record.getMultiValueBehaviour() != MultiValueBehaviour.MULTI_RECORD) {
				if (inlineGroup == null) {
					inlineGroup = new InlineGroupState(record);
				}
				inlineGroup.addRecord(record);
				continue;
			}

			int groupNum = record.getMapping().getGroupNumber();

			GroupState existingGroup = initialState == null ? null : GroupState.searchByGroup(initialState, groupNum);

			if (existingGroup == null) {
				GroupState newState = new GroupState(groupNum, record);

				if (initialState == null) {
					initialState = newState;
				} else {
					initialState.insert(newState);
				}
			} else {
				existingGroup.addRecord(record);
			}
		}

		// Return the first state in the linked list
		GroupState firstState = initialState;
		if (firstState != null) {
			while (firstState.prev != null) {
				firstState = firstState.prev;
			}
		}

		if (inlineGroup != null) {
			if (firstState != null)
				firstState.insert(inlineGroup);
			firstState = inlineGroup;
		}

		return firstState;
	}

	private static GroupState searchByGroup(GroupState initialState, int searchGroup) {
		if (initialState == null) {
			return null;
		}
		if (initialState.groupNumber == searchGroup) {
			return initialState;
		} else {
			// Searh forward from initialState
			GroupState current = initialState;
			do {
				if (current.groupNumber == searchGroup) {
					return current;
				}
				current = current.next;
			} while (current != null);
			// Search backwards from initialState
			current = initialState;
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

	private List<MappingRecord> records;

	public GroupState(int groupNum, MappingRecord record) {
		this.groupNumber = groupNum;
		this.groupSize = record.size();
		this.records = new ArrayList<MappingRecord>();
		this.records.add(record);
	}

	private void addRecord(MappingRecord record) {
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
		if (isExhausted() == false) {
			hasNext = true;
		} else {
			hasNext = (this.next != null) && this.next.hasNext();
		}
		return hasNext;
	}

	public void increment() {
		this.currentIndex++;
		if (this.currentIndex < (this.groupSize)) {
			LOG.debug("Incrementing group {} to {} as it has {} more values to yield", this.groupNumber, this.currentIndex - 1, this.groupSize
							- this.currentIndex - 2);
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
	 * @return true if any of the {@link MappingRecord}s that use this group have another value that can be returned.
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
		return "GroupState(" + this.groupNumber + ")";
	}
}
