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

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class Transform1STest {

    private static final double TEST_EPS = 1e-10;

    private static final Point1S ZERO = Point1S.ZERO;

    private static final Point1S HALF_PI = Point1S.of(Geometry.HALF_PI);

    private static final Point1S PI = Point1S.of(Geometry.PI);

    private static final Point1S MINUS_HALF_PI = Point1S.of(Geometry.MINUS_HALF_PI);

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
        Transform1S t = Transform1S.createRotation(Geometry.HALF_PI);

        // act/assert
        Assert.assertTrue(t.preservesOrientation());
        Assert.assertFalse(t.isNegation());
        Assert.assertEquals(Geometry.HALF_PI, t.getRotation(), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(HALF_PI, t.apply(ZERO), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(PI, t.apply(HALF_PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(1.5 * Geometry.PI), t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.ZERO, t.apply(MINUS_HALF_PI), TEST_EPS);

        checkInverse(t);
    }

    @Test
    public void testRotate_negative() {
        // arrange
        Transform1S t = Transform1S.createRotation(-Geometry.HALF_PI);

        // act/assert
        Assert.assertTrue(t.preservesOrientation());
        Assert.assertFalse(t.isNegation());
        Assert.assertEquals(-Geometry.HALF_PI, t.getRotation(), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(MINUS_HALF_PI, t.apply(ZERO), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(ZERO, t.apply(HALF_PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(HALF_PI, t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(-Geometry.PI), t.apply(MINUS_HALF_PI), TEST_EPS);

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
        SphericalTestUtils.assertPointsEqual(Point1S.of(-Geometry.PI), t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(HALF_PI, t.apply(MINUS_HALF_PI), TEST_EPS);

        checkInverse(t);
    }

    @Test
    public void testNegateThenRotate() {
        // arrange
        Transform1S t = Transform1S.createNegation().rotate(Geometry.HALF_PI);

        // act/assert
        Assert.assertFalse(t.preservesOrientation());
        Assert.assertTrue(t.isNegation());
        Assert.assertEquals(Geometry.HALF_PI, t.getRotation(), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(HALF_PI, t.apply(ZERO), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(ZERO, t.apply(HALF_PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(MINUS_HALF_PI, t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(PI, t.apply(MINUS_HALF_PI), TEST_EPS);

        checkInverse(t);
    }

    @Test
    public void testRotateThenNegate() {
        // arrange
        Transform1S t = Transform1S.createRotation(Geometry.HALF_PI).negate();

        // act/assert
        Assert.assertFalse(t.preservesOrientation());
        Assert.assertTrue(t.isNegation());
        Assert.assertEquals(-Geometry.HALF_PI, t.getRotation(), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(MINUS_HALF_PI, t.apply(ZERO), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(-Geometry.PI), t.apply(HALF_PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(-1.5 * Geometry.PI), t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(ZERO, t.apply(MINUS_HALF_PI), TEST_EPS);

        checkInverse(t);
    }

    @Test
    public void testMultiply() {
        // arrange
        Transform1S neg = Transform1S.identity().negate();
        Transform1S rot = Transform1S.identity().rotate(Geometry.HALF_PI);

        // act
        Transform1S t = rot.multiply(neg);

        // assert
        Assert.assertFalse(t.preservesOrientation());
        Assert.assertTrue(t.isNegation());
        Assert.assertEquals(Geometry.HALF_PI, t.getRotation(), TEST_EPS);

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
        Transform1S rot = Transform1S.identity().rotate(Geometry.HALF_PI);

        // act
        Transform1S t = neg.premultiply(rot);

        // assert
        Assert.assertFalse(t.preservesOrientation());
        Assert.assertTrue(t.isNegation());
        Assert.assertEquals(Geometry.HALF_PI, t.getRotation(), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(HALF_PI, t.apply(ZERO), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(ZERO, t.apply(HALF_PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(MINUS_HALF_PI, t.apply(PI), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(PI, t.apply(MINUS_HALF_PI), TEST_EPS);

        checkInverse(t);
    }

    @Test
    public void testHashCode() {
        // arrange
        Transform1S a = Transform1S.identity().negate().rotate(Geometry.HALF_PI);
        Transform1S b = Transform1S.identity().rotate(Geometry.HALF_PI);
        Transform1S c = Transform1S.identity().negate().rotate(-Geometry.HALF_PI);
        Transform1S d = Transform1S.identity().negate().rotate(Geometry.HALF_PI);

        // act/assert
        Assert.assertTrue(a.equals(a));

        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(a.equals(c));

        Assert.assertTrue(a.equals(d));
        Assert.assertTrue(d.equals(a));
    }

    @Test
    public void testEquals() {
        // arrange
        Transform1S a = Transform1S.identity().negate().rotate(Geometry.HALF_PI);
        Transform1S b = Transform1S.identity().rotate(Geometry.HALF_PI);
        Transform1S c = Transform1S.identity().negate().rotate(-Geometry.HALF_PI);
        Transform1S d = Transform1S.identity().negate().rotate(Geometry.HALF_PI);

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

        for (double x = -Geometry.TWO_PI; x <= 2 * Geometry.TWO_PI; x += 0.2) {
            Point1S pt = Point1S.of(x);

            SphericalTestUtils.assertPointsEqual(pt, inv.apply(t.apply(pt)), TEST_EPS);
            SphericalTestUtils.assertPointsEqual(pt, t.apply(inv.apply(pt)), TEST_EPS);
        }
    }
}
