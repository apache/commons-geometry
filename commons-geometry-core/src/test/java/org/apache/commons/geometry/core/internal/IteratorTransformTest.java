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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IteratorTransformTest {

    @Test
    void testIteration() {
        // arrange
        final List<Integer> input = Arrays.asList(1, 2, 3, 4, 12, 13);

        // act
        final List<String> result = toList(new EvenCharIterator(input.iterator()));

        // assert
        Assertions.assertEquals(Arrays.asList("2", "4", "1", "2"), result);
    }

    @Test
    void testThrowsNoSuchElement() {
        // arrange
        final List<Integer> input = Collections.emptyList();
        final EvenCharIterator it = new EvenCharIterator(input.iterator());

        // act/assert
        Assertions.assertFalse(it.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, it::next);
    }

    private static <T> List<T> toList(final Iterator<T> it) {
        final List<T> result = new ArrayList<>();
        while (it.hasNext()) {
            result.add(it.next());
        }

        return result;
    }

    private static class EvenCharIterator extends IteratorTransform<Integer, String> {

        EvenCharIterator(final Iterator<Integer> inputIterator) {
            super(inputIterator);
        }

        /** {@inheritDoc} */
        @Override
        protected void acceptInput(final Integer input) {
            // filter out odd integers
            final int value = input;
            if (value % 2 == 0) {
                final char[] chars = (Integer.toString(value)).toCharArray();

                if (chars.length > 1) {
                    final List<String> strs = new ArrayList<>();
                    for (final char ch : chars) {
                        strs.add(String.valueOf(ch));
                    }

                    addAllOutput(strs);
                } else if (chars.length == 1) {
                    addOutput(String.valueOf(chars[0]));
                }
            }
        }
    }
}
