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
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class Transform1STest {

    private static final double TEST_EPS = 1e-10;

    private static final Point1S ZERO = Point1S.ZERO;

    private static final Point1S HALF_PI = Point1S.of(PlaneAngleRadians.PI_OVER_TWO);

    private static final Point1S PI = Point1S.of(PlaneAngleRadians.PI);

    private static final Point1S MINUS_HALF_PI = Point1S.of(-PlaneAngleRadians.PI_OVER_TWO);

    @Test
    public void testIdentity() {
        // act
        Transform1S t = Transform1S.identity();

        // assert
        Assert.assertTrue(t.preservesOrientation());
        Assert.assertFalse(t.isNegation());
        Assert.assertEquals(0, t.getRotation(), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(ZERO, t.apply(ZERO), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(HALF_PI, t.apply(HALF_PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(PI, t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(MINUS_HALF_PI, t.apply(MINUS_HALF_PI), TEST_EPS);

        checkInverse(t);
    }

    @Test
    public void testRotate_positive() {
        // arrange
        Transform1S t = Transform1S.createRotation(PlaneAngleRadians.PI_OVER_TWO);

        // act/assert
        Assert.assertTrue(t.preservesOrientation());
        Assert.assertFalse(t.isNegation());
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, t.getRotation(), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(HALF_PI, t.apply(ZERO), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(PI, t.apply(HALF_PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(1.5 * PlaneAngleRadians.PI), t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.ZERO, t.apply(MINUS_HALF_PI), TEST_EPS);

        checkInverse(t);
    }

    @Test
    public void testRotate_negative() {
        // arrange
        Transform1S t = Transform1S.createRotation(-PlaneAngleRadians.PI_OVER_TWO);

        // act/assert
        Assert.assertTrue(t.preservesOrientation());
        Assert.assertFalse(t.isNegation());
        Assert.assertEquals(-PlaneAngleRadians.PI_OVER_TWO, t.getRotation(), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(MINUS_HALF_PI, t.apply(ZERO), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(ZERO, t.apply(HALF_PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(HALF_PI, t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(-PlaneAngleRadians.PI), t.apply(MINUS_HALF_PI), TEST_EPS);

        checkInverse(t);
    }

    @Test
    public void testNegate() {
        // arrange
        Transform1S t = Transform1S.createNegation();

        // act/assert
        Assert.assertFalse(t.preservesOrientation());
        Assert.assertTrue(t.isNegation());
        Assert.assertEquals(0, t.getRotation(), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(ZERO, t.apply(ZERO), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(MINUS_HALF_PI, t.apply(HALF_PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(-PlaneAngleRadians.PI), t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(HALF_PI, t.apply(MINUS_HALF_PI), TEST_EPS);

        checkInverse(t);
    }

    @Test
    public void testNegateThenRotate() {
        // arrange
        Transform1S t = Transform1S.createNegation().rotate(PlaneAngleRadians.PI_OVER_TWO);

        // act/assert
        Assert.assertFalse(t.preservesOrientation());
        Assert.assertTrue(t.isNegation());
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, t.getRotation(), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(HALF_PI, t.apply(ZERO), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(ZERO, t.apply(HALF_PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(MINUS_HALF_PI, t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(PI, t.apply(MINUS_HALF_PI), TEST_EPS);

        checkInverse(t);
    }

    @Test
    public void testRotateThenNegate() {
        // arrange
        Transform1S t = Transform1S.createRotation(PlaneAngleRadians.PI_OVER_TWO).negate();

        // act/assert
        Assert.assertFalse(t.preservesOrientation());
        Assert.assertTrue(t.isNegation());
        Assert.assertEquals(-PlaneAngleRadians.PI_OVER_TWO, t.getRotation(), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(MINUS_HALF_PI, t.apply(ZERO), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(-PlaneAngleRadians.PI), t.apply(HALF_PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(-1.5 * PlaneAngleRadians.PI), t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(ZERO, t.apply(MINUS_HALF_PI), TEST_EPS);

        checkInverse(t);
    }

    @Test
    public void testMultiply() {
        // arrange
        Transform1S neg = Transform1S.identity().negate();
        Transform1S rot = Transform1S.identity().rotate(PlaneAngleRadians.PI_OVER_TWO);

        // act
        Transform1S t = rot.multiply(neg);

        // assert
        Assert.assertFalse(t.preservesOrientation());
        Assert.assertTrue(t.isNegation());
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, t.getRotation(), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(HALF_PI, t.apply(ZERO), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(ZERO, t.apply(HALF_PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(MINUS_HALF_PI, t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(PI, t.apply(MINUS_HALF_PI), TEST_EPS);

        checkInverse(t);
    }

    @Test
    public void testPreultiply() {
        // arrange
        Transform1S neg = Transform1S.identity().negate();
        Transform1S rot = Transform1S.identity().rotate(PlaneAngleRadians.PI_OVER_TWO);

        // act
        Transform1S t = neg.premultiply(rot);

        // assert
        Assert.assertFalse(t.preservesOrientation());
        Assert.assertTrue(t.isNegation());
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, t.getRotation(), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(HALF_PI, t.apply(ZERO), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(ZERO, t.apply(HALF_PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(MINUS_HALF_PI, t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(PI, t.apply(MINUS_HALF_PI), TEST_EPS);

        checkInverse(t);
    }

    @Test
    public void testHashCode() {
        // arrange
        Transform1S a = Transform1S.identity().negate().rotate(PlaneAngleRadians.PI_OVER_TWO);
        Transform1S b = Transform1S.identity().rotate(PlaneAngleRadians.PI_OVER_TWO);
        Transform1S c = Transform1S.identity().negate().rotate(-PlaneAngleRadians.PI_OVER_TWO);
        Transform1S d = Transform1S.identity().negate().rotate(PlaneAngleRadians.PI_OVER_TWO);

        // act/assert
        Assert.assertEquals(a, a);

        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        Assert.assertNotEquals(a, b);
        Assert.assertNotEquals(a, c);

        Assert.assertEquals(a, d);
        Assert.assertEquals(d, a);
    }

    @Test
    public void testEquals() {
        // arrange
        Transform1S a = Transform1S.identity().negate().rotate(PlaneAngleRadians.PI_OVER_TWO);
        Transform1S b = Transform1S.identity().rotate(PlaneAngleRadians.PI_OVER_TWO);
        Transform1S c = Transform1S.identity().negate().rotate(-PlaneAngleRadians.PI_OVER_TWO);
        Transform1S d = Transform1S.identity().negate().rotate(PlaneAngleRadians.PI_OVER_TWO);

        // act
        int hash = a.hashCode();

        // assert
        Assert.assertEquals(hash, a.hashCode());

        Assert.assertNotEquals(hash, b.hashCode());
        Assert.assertNotEquals(hash, c.hashCode());

        Assert.assertEquals(hash, d.hashCode());
    }

    @Test
    public void testToString() {
        // arrange
        Transform1S t = Transform1S.identity().negate().rotate(1);

        // act
        String str = t.toString();

        // assert
        GeometryTestUtils.assertContains("Transform1S", str);
        GeometryTestUtils.assertContains("negate= true", str);
        GeometryTestUtils.assertContains("rotate= 1", str);
    }

    private static void checkInverse(Transform1S t) {
        Transform1S inv = t.inverse();

        for (double x = -PlaneAngleRadians.TWO_PI; x <= 2 * PlaneAngleRadians.TWO_PI; x += 0.2) {
            Point1S pt = Point1S.of(x);

            SphericalTestUtils.assertPointsEqual(pt, inv.apply(t.apply(pt)), TEST_EPS);
            SphericalTestUtils.assertPointsEqual(pt, t.apply(inv.apply(pt)), TEST_EPS);
        }
    }
}
