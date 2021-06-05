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

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.apache.commons.numbers.angle.Angle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Transform1STest {

    private static final double TEST_EPS = 1e-10;

    private static final Point1S ZERO = Point1S.ZERO;

    private static final Point1S HALF_PI = Point1S.of(Angle.PI_OVER_TWO);

    private static final Point1S PI = Point1S.of(Math.PI);

    private static final Point1S MINUS_HALF_PI = Point1S.of(-Angle.PI_OVER_TWO);

    @Test
    public void testIdentity() {
        // act
        final Transform1S t = Transform1S.identity();

        // assert
        Assertions.assertTrue(t.preservesOrientation());
        Assertions.assertFalse(t.isNegation());
        Assertions.assertEquals(0, t.getRotation(), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(ZERO, t.apply(ZERO), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(HALF_PI, t.apply(HALF_PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(PI, t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(MINUS_HALF_PI, t.apply(MINUS_HALF_PI), TEST_EPS);

        checkInverse(t);
    }

    @Test
    public void testRotate_positive() {
        // arrange
        final Transform1S t = Transform1S.createRotation(Angle.PI_OVER_TWO);

        // act/assert
        Assertions.assertTrue(t.preservesOrientation());
        Assertions.assertFalse(t.isNegation());
        Assertions.assertEquals(Angle.PI_OVER_TWO, t.getRotation(), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(HALF_PI, t.apply(ZERO), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(PI, t.apply(HALF_PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(1.5 * Math.PI), t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.ZERO, t.apply(MINUS_HALF_PI), TEST_EPS);

        checkInverse(t);
    }

    @Test
    public void testRotate_negative() {
        // arrange
        final Transform1S t = Transform1S.createRotation(-Angle.PI_OVER_TWO);

        // act/assert
        Assertions.assertTrue(t.preservesOrientation());
        Assertions.assertFalse(t.isNegation());
        Assertions.assertEquals(-Angle.PI_OVER_TWO, t.getRotation(), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(MINUS_HALF_PI, t.apply(ZERO), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(ZERO, t.apply(HALF_PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(HALF_PI, t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(-Math.PI), t.apply(MINUS_HALF_PI), TEST_EPS);

        checkInverse(t);
    }

    @Test
    public void testNegate() {
        // arrange
        final Transform1S t = Transform1S.createNegation();

        // act/assert
        Assertions.assertFalse(t.preservesOrientation());
        Assertions.assertTrue(t.isNegation());
        Assertions.assertEquals(0, t.getRotation(), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(ZERO, t.apply(ZERO), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(MINUS_HALF_PI, t.apply(HALF_PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(-Math.PI), t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(HALF_PI, t.apply(MINUS_HALF_PI), TEST_EPS);

        checkInverse(t);
    }

    @Test
    public void testNegateThenRotate() {
        // arrange
        final Transform1S t = Transform1S.createNegation().rotate(Angle.PI_OVER_TWO);

        // act/assert
        Assertions.assertFalse(t.preservesOrientation());
        Assertions.assertTrue(t.isNegation());
        Assertions.assertEquals(Angle.PI_OVER_TWO, t.getRotation(), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(HALF_PI, t.apply(ZERO), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(ZERO, t.apply(HALF_PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(MINUS_HALF_PI, t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(PI, t.apply(MINUS_HALF_PI), TEST_EPS);

        checkInverse(t);
    }

    @Test
    public void testRotateThenNegate() {
        // arrange
        final Transform1S t = Transform1S.createRotation(Angle.PI_OVER_TWO).negate();

        // act/assert
        Assertions.assertFalse(t.preservesOrientation());
        Assertions.assertTrue(t.isNegation());
        Assertions.assertEquals(-Angle.PI_OVER_TWO, t.getRotation(), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(MINUS_HALF_PI, t.apply(ZERO), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(-Math.PI), t.apply(HALF_PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(-1.5 * Math.PI), t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(ZERO, t.apply(MINUS_HALF_PI), TEST_EPS);

        checkInverse(t);
    }

    @Test
    public void testMultiply() {
        // arrange
        final Transform1S neg = Transform1S.identity().negate();
        final Transform1S rot = Transform1S.identity().rotate(Angle.PI_OVER_TWO);

        // act
        final Transform1S t = rot.multiply(neg);

        // assert
        Assertions.assertFalse(t.preservesOrientation());
        Assertions.assertTrue(t.isNegation());
        Assertions.assertEquals(Angle.PI_OVER_TWO, t.getRotation(), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(HALF_PI, t.apply(ZERO), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(ZERO, t.apply(HALF_PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(MINUS_HALF_PI, t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(PI, t.apply(MINUS_HALF_PI), TEST_EPS);

        checkInverse(t);
    }

    @Test
    public void testPreultiply() {
        // arrange
        final Transform1S neg = Transform1S.identity().negate();
        final Transform1S rot = Transform1S.identity().rotate(Angle.PI_OVER_TWO);

        // act
        final Transform1S t = neg.premultiply(rot);

        // assert
        Assertions.assertFalse(t.preservesOrientation());
        Assertions.assertTrue(t.isNegation());
        Assertions.assertEquals(Angle.PI_OVER_TWO, t.getRotation(), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(HALF_PI, t.apply(ZERO), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(ZERO, t.apply(HALF_PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(MINUS_HALF_PI, t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(PI, t.apply(MINUS_HALF_PI), TEST_EPS);

        checkInverse(t);
    }

    @Test
    public void testHashCode() {
        // arrange
        final Transform1S a = Transform1S.identity().negate().rotate(Angle.PI_OVER_TWO);
        final Transform1S b = Transform1S.identity().rotate(Angle.PI_OVER_TWO);
        final Transform1S c = Transform1S.identity().negate().rotate(-Angle.PI_OVER_TWO);
        final Transform1S d = Transform1S.identity().negate().rotate(Angle.PI_OVER_TWO);

        // act
        final int hash = a.hashCode();

        // assert
        Assertions.assertEquals(hash, a.hashCode());

        Assertions.assertNotEquals(hash, b.hashCode());
        Assertions.assertNotEquals(hash, c.hashCode());

        Assertions.assertEquals(hash, d.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        final Transform1S a = Transform1S.identity().negate().rotate(Angle.PI_OVER_TWO);
        final Transform1S b = Transform1S.identity().rotate(Angle.PI_OVER_TWO);
        final Transform1S c = Transform1S.identity().negate().rotate(-Angle.PI_OVER_TWO);
        final Transform1S d = Transform1S.identity().negate().rotate(Angle.PI_OVER_TWO);

        // act/assert
        GeometryTestUtils.assertSimpleEqualsCases(a);

        Assertions.assertNotEquals(a, b);
        Assertions.assertNotEquals(a, c);

        Assertions.assertEquals(a, d);
        Assertions.assertEquals(d, a);
    }

    @Test
    public void testToString() {
        // arrange
        final Transform1S t = Transform1S.identity().negate().rotate(1);

        // act
        final String str = t.toString();

        // assert
        GeometryTestUtils.assertContains("Transform1S", str);
        GeometryTestUtils.assertContains("negate= true", str);
        GeometryTestUtils.assertContains("rotate= 1", str);
    }

    private static void checkInverse(final Transform1S t) {
        final Transform1S inv = t.inverse();

        for (double x = -Angle.TWO_PI; x <= 2 * Angle.TWO_PI; x += 0.2) {
            final Point1S pt = Point1S.of(x);

            SphericalTestUtils.assertPointsEqual(pt, inv.apply(t.apply(pt)), TEST_EPS);
            SphericalTestUtils.assertPointsEqual(pt, t.apply(inv.apply(pt)), TEST_EPS);
        }
    }
}
