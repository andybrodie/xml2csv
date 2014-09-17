package com.locima.xml2csv;

import java.io.Serializable;

/**
 * Represents a simple tuple.
 * @param <T> the type of the first member of the tuple.
 * @param <U> the type of the second member of the tuple.
 */
public class Tuple<T extends Serializable, U> {

    private final T first;
    private final U second;

    public Tuple(T firstValue, U secondValue) {
        this.first = firstValue;
        this.second = secondValue;
    }

    public T getFirst() {
        return this.first;
    }

    public U getSecond() {
        return this.second;
    }
}
