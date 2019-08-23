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
import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.junit.Assert;
import org.junit.Test;

public class OrientedPoint1STest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromAzimuthAndDirection() {
        // act/assert
        checkPoint(OrientedPoint1S.fromAzimuthAndDirection(Geometry.ZERO_PI, true, TEST_PRECISION),
                Geometry.ZERO_PI, true);
        checkPoint(OrientedPoint1S.fromAzimuthAndDirection(Geometry.PI, true, TEST_PRECISION),
                Geometry.PI, true);
        checkPoint(OrientedPoint1S.fromAzimuthAndDirection(Geometry.MINUS_HALF_PI, true, TEST_PRECISION),
                Geometry.MINUS_HALF_PI, true);

        checkPoint(OrientedPoint1S.fromAzimuthAndDirection(Geometry.ZERO_PI, false, TEST_PRECISION),
                Geometry.ZERO_PI, false);
        checkPoint(OrientedPoint1S.fromAzimuthAndDirection(Geometry.PI, false, TEST_PRECISION),
                Geometry.PI, false);
        checkPoint(OrientedPoint1S.fromAzimuthAndDirection(Geometry.MINUS_HALF_PI, false, TEST_PRECISION),
                Geometry.MINUS_HALF_PI, false);
    }

    @Test
    public void testFromPointAndDirection() {
        // arrange
        Point1S pt = Point1S.of(Geometry.MINUS_HALF_PI);

        // act/assert
        checkPoint(OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, true, TEST_PRECISION),
                Geometry.ZERO_PI, true);
        checkPoint(OrientedPoint1S.fromPointAndDirection(Point1S.PI, true, TEST_PRECISION),
                Geometry.PI, true);
        checkPoint(OrientedPoint1S.fromPointAndDirection(pt, true, TEST_PRECISION),
                Geometry.MINUS_HALF_PI, true);

        checkPoint(OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, false, TEST_PRECISION),
                Geometry.ZERO_PI, false);
        checkPoint(OrientedPoint1S.fromPointAndDirection(Point1S.PI, false, TEST_PRECISION),
                Geometry.PI, false);
        checkPoint(OrientedPoint1S.fromPointAndDirection(pt, false, TEST_PRECISION),
                Geometry.MINUS_HALF_PI, false);
    }

    @Test
    public void testCreatePositiveFacing() {
        // act/assert
        checkPoint(OrientedPoint1S.createPositiveFacing(Point1S.ZERO_PI, TEST_PRECISION),
                Geometry.ZERO_PI, true);
        checkPoint(OrientedPoint1S.createPositiveFacing(Point1S.PI, TEST_PRECISION),
                Geometry.PI, true);
        checkPoint(OrientedPoint1S.createPositiveFacing(Geometry.MINUS_HALF_PI, TEST_PRECISION),
                Geometry.MINUS_HALF_PI, true);
    }

    @Test
    public void testCreateNegativeFacing() {
        // act/assert
        checkPoint(OrientedPoint1S.createNegativeFacing(Point1S.ZERO_PI, TEST_PRECISION),
                Geometry.ZERO_PI, false);
        checkPoint(OrientedPoint1S.createNegativeFacing(Point1S.PI, TEST_PRECISION),
                Geometry.PI, false);
        checkPoint(OrientedPoint1S.createNegativeFacing(Geometry.MINUS_HALF_PI, TEST_PRECISION),
                Geometry.MINUS_HALF_PI, false);
    }

    @Test
    public void testOffset() {
        // arrange
        OrientedPoint1S zeroPos = OrientedPoint1S.createPositiveFacing(Geometry.ZERO_PI, TEST_PRECISION);
        OrientedPoint1S zeroNeg = OrientedPoint1S.createNegativeFacing(Geometry.ZERO_PI, TEST_PRECISION);
        OrientedPoint1S negPiPos = OrientedPoint1S.createPositiveFacing(-Geometry.PI, TEST_PRECISION);

        OrientedPoint1S piNeg = OrientedPoint1S.createNegativeFacing(Geometry.PI, TEST_PRECISION);
        OrientedPoint1S twoAndAHalfPiPos = OrientedPoint1S.createPositiveFacing(2.5 * Geometry.PI, TEST_PRECISION);

        // act/assert
        checkOffset(zeroPos, 0, 0);
        checkOffset(zeroPos, Geometry.TWO_PI, 0);
        checkOffset(zeroPos, 2.5 * Geometry.PI, Geometry.HALF_PI);
        checkOffset(zeroPos, Geometry.PI, -Geometry.PI);
        checkOffset(zeroPos, 3.5 * Geometry.PI, Geometry.MINUS_HALF_PI);

        checkOffset(zeroNeg, 0, 0);
        checkOffset(zeroNeg, Geometry.TWO_PI, 0);
        checkOffset(zeroNeg, 2.5 * Geometry.PI, Geometry.MINUS_HALF_PI);
        checkOffset(zeroNeg, Geometry.PI, -Geometry.PI);
        checkOffset(zeroNeg, 3.5 * Geometry.PI, Geometry.HALF_PI);

        checkOffset(negPiPos, 0, -Geometry.PI);
        checkOffset(negPiPos, Geometry.TWO_PI, -Geometry.PI);
        checkOffset(negPiPos, 2.5 * Geometry.PI, Geometry.MINUS_HALF_PI);
        checkOffset(negPiPos, Geometry.PI, 0);
        checkOffset(negPiPos, 3.5 * Geometry.PI, Geometry.HALF_PI);

        checkOffset(piNeg, 0, -Geometry.PI);
        checkOffset(piNeg, Geometry.TWO_PI, -Geometry.PI);
        checkOffset(piNeg, 2.5 * Geometry.PI, Geometry.HALF_PI);
        checkOffset(piNeg, Geometry.PI, 0);
        checkOffset(piNeg, 3.5 * Geometry.PI, Geometry.MINUS_HALF_PI);

        checkOffset(twoAndAHalfPiPos, 0, Geometry.MINUS_HALF_PI);
        checkOffset(twoAndAHalfPiPos, Geometry.TWO_PI, Geometry.MINUS_HALF_PI);
        checkOffset(twoAndAHalfPiPos, 2.5 * Geometry.PI, 0);
        checkOffset(twoAndAHalfPiPos, Geometry.PI, Geometry.HALF_PI);
        checkOffset(twoAndAHalfPiPos, 3.5 * Geometry.PI, -Geometry.PI);
    }

    @Test
    public void testClassify() {
        // arrange
        OrientedPoint1S zeroPos = OrientedPoint1S.createPositiveFacing(Geometry.ZERO_PI, TEST_PRECISION);
        OrientedPoint1S zeroNeg = OrientedPoint1S.createNegativeFacing(Geometry.ZERO_PI, TEST_PRECISION);
        OrientedPoint1S negPiPos = OrientedPoint1S.createPositiveFacing(-Geometry.PI, TEST_PRECISION);

        // act/assert
        checkClassify(zeroPos, HyperplaneLocation.ON, 0, 1e-16, -1e-16);
        checkClassify(zeroPos, HyperplaneLocation.MINUS, -0.5, Geometry.MINUS_HALF_PI, Geometry.PI);
        checkClassify(zeroPos, HyperplaneLocation.PLUS, 0.5, 2.5 * Geometry.PI);

        checkClassify(zeroNeg, HyperplaneLocation.ON, 0, 1e-16, -1e-16);
        checkClassify(zeroNeg, HyperplaneLocation.PLUS, -0.5, Geometry.MINUS_HALF_PI);
        checkClassify(zeroNeg, HyperplaneLocation.MINUS, 0.5, 2.5 * Geometry.PI, Geometry.PI);

        checkClassify(negPiPos, HyperplaneLocation.ON, Geometry.PI, Geometry.PI + 1e-11);
        checkClassify(negPiPos, HyperplaneLocation.MINUS, 0.5, 0, 2.5 * Geometry.PI);
        checkClassify(negPiPos, HyperplaneLocation.PLUS, -0.5, Geometry.MINUS_HALF_PI);
    }

    @Test
    public void testPlusMinuOnPoint() {
        // arrange
        DoublePrecisionContext low = new EpsilonDoublePrecisionContext(1.1);
        DoublePrecisionContext high = new EpsilonDoublePrecisionContext(1e-10);

        OrientedPoint1S a = OrientedPoint1S.createNegativeFacing(Geometry.HALF_PI, low);
        OrientedPoint1S b = OrientedPoint1S.createNegativeFacing(Geometry.HALF_PI, high);

        // act/assert
        checkClassify(a, HyperplaneLocation.ON, a.onPoint());
        checkClassify(b, HyperplaneLocation.ON, b.onPoint());

        checkClassify(a, HyperplaneLocation.PLUS, a.plusPoint());
        checkClassify(b, HyperplaneLocation.PLUS, b.plusPoint());

        checkClassify(a, HyperplaneLocation.MINUS, a.minusPoint());
        checkClassify(b, HyperplaneLocation.MINUS, b.minusPoint());
    }

    @Test
    public void testContains() {
        // arrange
        OrientedPoint1S pt = OrientedPoint1S.createNegativeFacing(Geometry.HALF_PI, TEST_PRECISION);

        // act/assert
        Assert.assertFalse(pt.contains(Point1S.ZERO_PI));
        Assert.assertFalse(pt.contains(Point1S.of(Geometry.PI)));
        Assert.assertFalse(pt.contains(Point1S.of(Geometry.TWO_PI)));

        Assert.assertTrue(pt.contains(Point1S.of(Geometry.HALF_PI)));
        Assert.assertTrue(pt.contains(Point1S.of(Geometry.HALF_PI + 1e-11)));
        Assert.assertTrue(pt.contains(Point1S.of(2.5 * Geometry.PI)));
        Assert.assertTrue(pt.contains(Point1S.of(-3.5 * Geometry.PI)));
    }

    @Test
    public void testReverse() {
        // arrange
        OrientedPoint1S pt = OrientedPoint1S.createNegativeFacing(Geometry.HALF_PI, TEST_PRECISION);

        // act
        OrientedPoint1S result = pt.reverse();

        // assert
        checkPoint(result, Geometry.HALF_PI, true);
        Assert.assertSame(TEST_PRECISION, result.getPrecision());

        checkPoint(result.reverse(), Geometry.HALF_PI, false);
    }

    @Test
    public void testProject() {
        // arrange
        OrientedPoint1S pt = OrientedPoint1S.createNegativeFacing(Geometry.HALF_PI, TEST_PRECISION);

        // act/assert
        for (double az = -Geometry.TWO_PI; az <= Geometry.TWO_PI; az += 0.2) {
            Assert.assertEquals(Geometry.HALF_PI, pt.project(Point1S.of(az)).getAzimuth(), TEST_EPS);
        }
    }

    @Test
    public void testSimilarOrientation() {
        // arrange
        OrientedPoint1S a = OrientedPoint1S.createPositiveFacing(Geometry.ZERO_PI, TEST_PRECISION);
        OrientedPoint1S b = OrientedPoint1S.createNegativeFacing(Geometry.ZERO_PI, TEST_PRECISION);
        OrientedPoint1S c = OrientedPoint1S.createPositiveFacing(Geometry.MINUS_HALF_PI, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(a.similarOrientation(a));
        Assert.assertFalse(a.similarOrientation(b));
        Assert.assertTrue(a.similarOrientation(c));
    }

    @Test
    public void testToString() {
        // arrange
        OrientedPoint1S pt = OrientedPoint1S.createPositiveFacing(Geometry.ZERO_PI, TEST_PRECISION);

        // act
        String str = pt.toString();

        // assert
        Assert.assertTrue(str.startsWith("OrientedPoint1S["));
        Assert.assertTrue(str.contains("point= ") && str.contains("positiveFacing= "));
    }

    private static void checkPoint(OrientedPoint1S pt, double az, boolean positiveFacing) {
        checkPoint(pt, az, positiveFacing, TEST_PRECISION);
    }

    private static void checkPoint(OrientedPoint1S pt, double az, boolean positiveFacing, DoublePrecisionContext precision) {
        Assert.assertEquals(az, pt.getAzimuth(), TEST_EPS);
        Assert.assertEquals(az, pt.getPoint().getAzimuth(), TEST_EPS);
        Assert.assertEquals(positiveFacing, pt.isPositiveFacing());

        Assert.assertSame(precision, pt.getPrecision());
    }

    private static void checkOffset(OrientedPoint1S pt, double az, double offset) {
        Assert.assertEquals(offset, pt.offset(Point1S.of(az)), TEST_EPS);
    }

    private static void checkClassify(OrientedPoint1S pt, HyperplaneLocation loc, double ... azimuths) {
        for (double az : azimuths) {
            Assert.assertEquals("Unexpected location for azimuth " + az, loc, pt.classify(Point1S.of(az)));
        }
    }

    private static void checkClassify(OrientedPoint1S orientedPt, HyperplaneLocation loc, Point1S ... pts) {
        for (Point1S pt : pts) {
            Assert.assertEquals("Unexpected location for point " + pt, loc, orientedPt.classify(pt));
        }
    }
}
