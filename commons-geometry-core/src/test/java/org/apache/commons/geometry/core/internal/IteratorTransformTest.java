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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

public class IteratorTransformTest {

    @Test
    public void testIteration() {
        // arrange
        List<Integer> input = Arrays.asList(1, 2, 3, 4, 12, 13);

        // act
        List<String> result = toList(new EvenCharIterator(input.iterator()));

        // assert
        Assert.assertEquals(Arrays.asList("2", "4", "1", "2"), result);
    }

    @Test(expected = NoSuchElementException.class)
    public void testThrowsNoSuchElement() {
        // arrange
        List<Integer> input = Arrays.asList();
        EvenCharIterator it = new EvenCharIterator(input.iterator());

        // act/assert
        Assert.assertFalse(it.hasNext());
        it.next();
    }

    private static <T> List<T> toList(Iterator<T> it) {
        List<T> result = new ArrayList<>();
        while (it.hasNext()) {
            result.add(it.next());
        }

        return result;
    }

    private static class EvenCharIterator extends IteratorTransform<Integer, String>{

        public EvenCharIterator(final Iterator<Integer> inputIterator) {
            super(inputIterator);
        }

        /** {@inheritDoc} */
        @Override
        protected void acceptInput(Integer input) {
            // filter out odd integers
            int value = input.intValue();
            if (value % 2 == 0) {
                char[] chars = (value + "").toCharArray();

                if (chars.length > 1) {
                    List<String> strs = new ArrayList<>();
                    for (char ch : chars) {
                        strs.add(ch + "");
                    }

                    addAllOutput(strs);
                }
                else if (chars.length == 1) {
                    addOutput(chars[0] + "");
                }
            }
        }
    }
}
