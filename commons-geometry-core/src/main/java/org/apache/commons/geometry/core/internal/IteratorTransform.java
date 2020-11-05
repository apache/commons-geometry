/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.core.internal;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/** Class that wraps another iterator, converting each input iterator value into
 * one or more output iterator values.
 * @param <I> Input iterator type
 * @param <T> Output iterator type
 */
public abstract class IteratorTransform<I, T> implements Iterator<T> {

    /** Input iterator instance that supplies the input values for this instance. */
    private final Iterator<I> inputIterator;

    /** Output value queue. */
    private final Deque<T> outputQueue = new LinkedList<>();

    /** Create a new instance that uses the given iterator as the input source.
     * @param inputIterator iterator supplying input values
     */
    protected IteratorTransform(final Iterator<I> inputIterator) {
        this.inputIterator = inputIterator;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
        return loadNextOutput();
    }

    /** {@inheritDoc} */
    @Override
    public T next() {
        if (outputQueue.isEmpty()) {
            throw new NoSuchElementException();
        }

        return outputQueue.removeFirst();
    }

    /** Load the next output values into the output queue. Returns true if the output queue
     * contains more entries.
     * @return true if more output values are available
     */
    private boolean loadNextOutput() {
        while (outputQueue.isEmpty() && inputIterator.hasNext()) {
            acceptInput(inputIterator.next());
        }

        return !outputQueue.isEmpty();
    }

    /** Add a value to the output queue.
     * @param value value to add to the output queue
     */
    protected void addOutput(final T value) {
        outputQueue.add(value);
    }

    /** Add multiple values to the output queue.
     * @param values values to add to the output queue
     */
    protected void addAllOutput(final Collection<T> values) {
        outputQueue.addAll(values);
    }

    /** Accept a value from the input iterator. This method should take
     * the input value and add one or more values to the output queue.
     * @param input value from the input iterator
     */
    protected abstract void acceptInput(I input);
}
