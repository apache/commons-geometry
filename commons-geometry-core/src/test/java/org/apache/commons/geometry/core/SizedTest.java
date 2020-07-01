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
package org.apache.commons.geometry.core;

import org.junit.Assert;
import org.junit.Test;

public class SizedTest {

    @Test
    public void testProperties() {
        // arrange
        final Sized finite = new StubSized(1);
        final Sized infinite = new StubSized(Double.POSITIVE_INFINITY);
        final Sized nan = new StubSized(Double.NaN);

        // act/assert
        Assert.assertTrue(finite.isFinite());
        Assert.assertFalse(finite.isInfinite());

        Assert.assertFalse(infinite.isFinite());
        Assert.assertTrue(infinite.isInfinite());

        Assert.assertFalse(nan.isFinite());
        Assert.assertFalse(nan.isInfinite());
    }

    private static class StubSized implements Sized {

        private final double size;

        StubSized(final double size) {
            this.size = size;
        }

        @Override
        public double getSize() {
            return size;
        }
    }
}
