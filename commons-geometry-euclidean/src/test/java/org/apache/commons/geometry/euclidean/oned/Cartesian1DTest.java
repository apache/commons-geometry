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
package org.apache.commons.geometry.euclidean.oned;

import java.util.regex.Pattern;

import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class Cartesian1DTest {

    private static final double TEST_TOLERANCE = 1e-15;

    @Test
    public void testCoordinates() {
        // act/assert
        Assert.assertEquals(0.0, new StubCartesian1D(0.0).getX(), TEST_TOLERANCE);
        Assert.assertEquals(-1.0, new StubCartesian1D(-1.0).getX(), TEST_TOLERANCE);
        Assert.assertEquals(1.0, new StubCartesian1D(1.0).getX(), TEST_TOLERANCE);

        Assert.assertEquals(Double.NaN, new StubCartesian1D(Double.NaN).getX(), TEST_TOLERANCE);
        EuclideanTestUtils.assertNegativeInfinity(new StubCartesian1D(Double.NEGATIVE_INFINITY).getX());
        EuclideanTestUtils.assertPositiveInfinity(new StubCartesian1D(Double.POSITIVE_INFINITY).getX());
    }

    @Test
    public void testDimension() {
        // arrange
        Cartesian1D c = new StubCartesian1D(0.0);

        // act/assert
        Assert.assertEquals(1, c.getDimension());
    }

    @Test
    public void testNaN() {
        // act/assert
        Assert.assertTrue(new StubCartesian1D(Double.NaN).isNaN());

        Assert.assertFalse(new StubCartesian1D(1).isNaN());
        Assert.assertFalse(new StubCartesian1D(Double.NEGATIVE_INFINITY).isNaN());
    }

    @Test
    public void testInfinite() {
        // act/assert
        Assert.assertTrue(new StubCartesian1D(Double.NEGATIVE_INFINITY).isInfinite());
        Assert.assertTrue(new StubCartesian1D(Double.POSITIVE_INFINITY).isInfinite());

        Assert.assertFalse(new StubCartesian1D(1).isInfinite());
        Assert.assertFalse(new StubCartesian1D(Double.NaN).isInfinite());
    }

    @Test
    public void testToString() {
        // arrange
        StubCartesian1D c = new StubCartesian1D(1);
        Pattern pattern = Pattern.compile("\\(1.{0,2}\\)");

        // act
        String str = c.toString();

        // assert
        Assert.assertTrue("Expected string " + str + " to match regex " + pattern,
                    pattern.matcher(str).matches());
    }

    private static class StubCartesian1D extends Cartesian1D {
        private static final long serialVersionUID = 1L;

        public StubCartesian1D(double x) {
            super(x);
        }
    }
}
