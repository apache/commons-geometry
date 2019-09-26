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
package org.apache.commons.geometry.spherical.oned;

import static org.junit.Assert.assertEquals;

import java.util.function.Function;

import org.apache.commons.geometry.core.Geometry;
import org.junit.Assert;
import org.junit.Test;

public class FunctionTransform1STest {

    private static final double TEST_EPS = 1e-15;

    @Test
    public void testIdentity() {
        // arrange
        Point1S p0 = Point1S.of(0);
        Point1S p1 = Point1S.of(1);
        Point1S p2 = Point1S.of(-1);

        // act
        Transform1S t = FunctionTransform1S.identity();

        // assert
        Assert.assertTrue(t.preservesOrientation());

        assertAzimuthsEqual(p0, t.apply(p0));
        assertAzimuthsEqual(p1, t.apply(p1));
        assertAzimuthsEqual(p2, t.apply(p2));
    }

    @Test
    public void testFrom_identity() {
        // arrange
        Point1S p0 = Point1S.of(0);
        Point1S p1 = Point1S.of(1);
        Point1S p2 = Point1S.of(-1);

        // act
        Transform1S t = FunctionTransform1S.from(Function.identity());

        // assert
        Assert.assertTrue(t.preservesOrientation());

        assertAzimuthsEqual(p0, t.apply(p0));
        assertAzimuthsEqual(p1, t.apply(p1));
        assertAzimuthsEqual(p2, t.apply(p2));
    }

    @Test
    public void testFrom_preservesOrientation() {
        // arrange
        Point1S p0 = Point1S.of(0);
        Point1S p1 = Point1S.of(Geometry.PI);
        Point1S p2 = Point1S.of(Geometry.MINUS_HALF_PI);

        // act
        Transform1S t = FunctionTransform1S.from(p -> Point1S.of(Geometry.PI + p.getAzimuth()));

        // assert
        Assert.assertTrue(t.preservesOrientation());

        assertAzimuthsEqual(Point1S.of(Geometry.PI), t.apply(p0));
        assertAzimuthsEqual(Point1S.of(Geometry.TWO_PI), t.apply(p1));
        assertAzimuthsEqual(Point1S.of(Geometry.HALF_PI), t.apply(p2));
    }

    @Test
    public void testFrom_doesNotPreserveOrientation() {
        // arrange
        Point1S p0 = Point1S.of(0);
        Point1S p1 = Point1S.of(Geometry.PI);
        Point1S p2 = Point1S.of(Geometry.MINUS_HALF_PI);

        // act
        Transform1S t = FunctionTransform1S.from(p -> Point1S.of(Geometry.PI - p.getAzimuth()));

        // assert
        Assert.assertFalse(t.preservesOrientation());

        assertAzimuthsEqual(Point1S.of(Geometry.PI), t.apply(p0));
        assertAzimuthsEqual(Point1S.ZERO, t.apply(p1));
        assertAzimuthsEqual(Point1S.of(1.5 * Geometry.PI), t.apply(p2));
    }

    private static void assertAzimuthsEqual(final Point1S a, final Point1S b) {
        String str = "Expected point " + a + " to equal " + b;
        assertEquals(str, a.getAzimuth(), b.getAzimuth(), TEST_EPS);
    }
}
