package com.locima.xml2csv.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtractionContext {

	private static final Logger LOG = LoggerFactory.getLogger(ExtractionContext.class);

	private Deque<Integer> indices;

	public ExtractionContext() {
		this.indices = new ArrayDeque<Integer>(3); // Unlikely to ever go deeper than 3 (ML -> ML -> Mapping)
	}

	public void addContext(int x) {
		this.indices.push(x);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Added context {}", toContextString());
		}
	}

	public void increment() {
		String before = null;
		if (LOG.isDebugEnabled()) {
			before = toContextString();
		}
		this.indices.push(this.indices.pop() + 1);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Incremented {} to {}", before, toContextString());
		}

	}

	public void removeContext() {
		this.indices.pop();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Popped context {}", toContextString());
		}
	}

	public String toContextString() {
		StringBuilder sb = new StringBuilder();
		Iterator<Integer> iter = this.indices.descendingIterator();
		while (iter.hasNext()) {
			sb.append(iter.next());
			if (iter.hasNext()) {
				sb.append('_');
			}
		}
		return sb.toString();
	}

}
