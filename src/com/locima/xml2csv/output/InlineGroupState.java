package com.locima.xml2csv.output;

import com.locima.xml2csv.extractor.ExtractionContext;

public class InlineGroupState extends GroupState {

	private boolean exhausted;

	public InlineGroupState(ExtractionContext record) {
		super(-1, record);
	}

	/**
	 * Inline groups only need to output once before they're exhausted. Beyond that they're exhausted.
	 */
	@Override
	public boolean hasNext() {
		return this.exhausted ? (this.next != null ? this.next.hasNext() : false) : true;
	}

	@Override
	public void increment() {
		this.exhausted = true;
		if (this.next != null) {
			this.next.increment();
		}
	}

}
