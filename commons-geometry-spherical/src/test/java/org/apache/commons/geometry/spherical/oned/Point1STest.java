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

import java.util.Comparator;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.PolarCoordinates;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.angle.PlaneAngle;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class Point1STest {

    private static final double TEST_EPS = 1e-10;

    @Test
    public void testConstants() {
        // act/assert
        Assert.assertEquals(0.0, Point1S.ZERO.getAzimuth(), TEST_EPS);
        Assert.assertEquals(Math.PI, Point1S.PI.getAzimuth(), TEST_EPS);
    }

    @Test
    public void testNormalizedAzimuthComparator() {
        // arrange
        Comparator<Point1S> comp = Point1S.NORMALIZED_AZIMUTH_ASCENDING_ORDER;

        // act/assert
        Assert.assertEquals(0, comp.compare(Point1S.of(1), Point1S.of(1)));
        Assert.assertEquals(-1, comp.compare(Point1S.of(0), Point1S.of(1)));
        Assert.assertEquals(1, comp.compare(Point1S.of(1), Point1S.of(0)));
        Assert.assertEquals(1, comp.compare(Point1S.of(1), Point1S.of(0.1 + PlaneAngleRadians.TWO_PI)));

        Assert.assertEquals(1, comp.compare(null, Point1S.of(0)));
        Assert.assertEquals(-1, comp.compare(Point1S.of(0), null));
        Assert.assertEquals(0, comp.compare(null, null));
    }

    @Test
    public void testOf() {
        // act/assert
        checkPoint(Point1S.of(0), 0, 0);
        checkPoint(Point1S.of(1), 1, 1);
        checkPoint(Point1S.of(-1), -1, PlaneAngleRadians.TWO_PI - 1);

        checkPoint(Point1S.of(PlaneAngle.ofDegrees(90)), PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO);
        checkPoint(Point1S.of(PlaneAngle.ofTurns(0.5)), PlaneAngleRadians.PI, PlaneAngleRadians.PI);
        checkPoint(Point1S.of(-PlaneAngleRadians.PI_OVER_TWO), -PlaneAngleRadians.PI_OVER_TWO, 1.5 * PlaneAngleRadians.PI);

        double base = PlaneAngleRadians.PI_OVER_TWO;
        for (int k = -3; k <= 3; ++k) {
            double az = base + (k * PlaneAngleRadians.TWO_PI);
            checkPoint(Point1S.of(az), az, base);
        }
    }

    @Test
    public void testFrom_vector() {
        // act/assert
        checkPoint(Point1S.from(Vector2D.of(2, 0)), 0.0);
        checkPoint(Point1S.from(Vector2D.of(0, 0.1)), PlaneAngleRadians.PI_OVER_TWO);
        checkPoint(Point1S.from(Vector2D.of(-0.5, 0)), PlaneAngleRadians.PI);
        checkPoint(Point1S.from(Vector2D.of(0, -100)), 1.5 * PlaneAngleRadians.PI);
    }

    @Test
    public void testFrom_polar() {
        // act/assert
        checkPoint(Point1S.from(PolarCoordinates.of(100, 0)), 0.0);
        checkPoint(Point1S.from(PolarCoordinates.of(1, PlaneAngleRadians.PI_OVER_TWO)), PlaneAngleRadians.PI_OVER_TWO);
        checkPoint(Point1S.from(PolarCoordinates.of(0.5, PlaneAngleRadians.PI)), PlaneAngleRadians.PI);
        checkPoint(Point1S.from(PolarCoordinates.of(1e-4, -PlaneAngleRadians.PI_OVER_TWO)), 1.5 * PlaneAngleRadians.PI);
    }

    @Test
    public void testFrom_polar_invalidAzimuths() {
        // act/assert
        checkPoint(Point1S.from(PolarCoordinates.of(100, Double.POSITIVE_INFINITY)), Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        checkPoint(Point1S.from(PolarCoordinates.of(100, Double.NEGATIVE_INFINITY)), Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        checkPoint(Point1S.from(PolarCoordinates.of(100, Double.NaN)), Double.NaN, Double.NaN);
    }

    @Test
    public void testNaN() {
        // act
        Point1S pt = Point1S.of(Double.NaN);

        // assert
        Assert.assertTrue(pt.isNaN());
        Assert.assertTrue(Point1S.NaN.isNaN());

        Assert.assertTrue(Double.isNaN(pt.getAzimuth()));
        Assert.assertTrue(Double.isNaN(pt.getNormalizedAzimuth()));
        Assert.assertNull(pt.getVector());

        Assert.assertTrue(Point1S.NaN.equals(pt));
        Assert.assertFalse(Point1S.of(1.0).equals(Point1S.NaN));
    }

    @Test
    public void testGetDimension() {
        // arrange
        Point1S p = Point1S.of(0.0);

        // act/assert
        Assert.assertEquals(1, p.getDimension());
    }

    @Test
    public void testInfinite() {
        // act/assert
        Assert.assertTrue(Point1S.of(Double.POSITIVE_INFINITY).isInfinite());
        Assert.assertTrue(Point1S.of(Double.NEGATIVE_INFINITY).isInfinite());

        Assert.assertFalse(Point1S.NaN.isInfinite());
        Assert.assertFalse(Point1S.of(1).isInfinite());
    }

    @Test
    public void testFinite() {
        // act/assert
        Assert.assertTrue(Point1S.of(0).isFinite());
        Assert.assertTrue(Point1S.of(1).isFinite());

        Assert.assertFalse(Point1S.of(Double.POSITIVE_INFINITY).isFinite());
        Assert.assertFalse(Point1S.of(Double.NEGATIVE_INFINITY).isFinite());
        Assert.assertFalse(Point1S.NaN.isFinite());
    }

    @Test
    public void testAntipodal() {
        for (double az = -6 * PlaneAngleRadians.PI; az <= 6 * PlaneAngleRadians.PI; az += 0.1) {
            // arrange
            Point1S pt = Point1S.of(az);

            // act
            Point1S result = pt.antipodal();

            // assert
            Assert.assertTrue(result.getAzimuth() >= 0 && result.getAzimuth() < PlaneAngleRadians.TWO_PI);
            Assert.assertEquals(PlaneAngleRadians.PI, pt.distance(result), TEST_EPS);
        }
    }

    @Test
    public void testHashCode() {
        // act
        Point1S a = Point1S.of(1.0);
        Point1S b = Point1S.of(2.0);
        Point1S c = Point1S.of(1.0);
        Point1S d = Point1S.of(1.0 + PlaneAngleRadians.PI);

        int hash = a.hashCode();

        // assert
        Assert.assertEquals(hash, a.hashCode());
        Assert.assertNotEquals(hash, b.hashCode());
        Assert.assertEquals(hash, c.hashCode());
        Assert.assertNotEquals(hash, d.hashCode());

        Assert.assertEquals(Point1S.NaN.hashCode(), Point1S.of(Double.NaN).hashCode());
    }

    @Test
    public void testEquals() {
        // act
        Point1S a = Point1S.of(1.0);
        Point1S b = Point1S.of(2.0);
        Point1S c = Point1S.of(1.0 + PlaneAngleRadians.PI);
        Point1S d = Point1S.of(1.0);
        Point1S e = Point1S.of(Double.NaN);

        // assert
        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        Assert.assertTrue(a.equals(a));

        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(b.equals(a));

        Assert.assertFalse(a.equals(c));
        Assert.assertFalse(c.equals(a));

        Assert.assertTrue(a.equals(d));
        Assert.assertTrue(d.equals(a));

        Assert.assertFalse(a.equals(e));
        Assert.assertTrue(e.equals(Point1S.NaN));
    }

    @Test
    public void testEq() {
        // arrange
        DoublePrecisionContext highPrecision = new EpsilonDoublePrecisionContext(1e-10);
        DoublePrecisionContext lowPrecision = new EpsilonDoublePrecisionContext(1e-2);

        Point1S a = Point1S.of(1);
        Point1S b = Point1S.of(0.9999);
        Point1S c = Point1S.of(1.00001);
        Point1S d = Point1S.of(1 + (3 * PlaneAngleRadians.TWO_PI));

        // act/assert
        Assert.assertTrue(a.eq(a, highPrecision));
        Assert.assertTrue(a.eq(a, lowPrecision));

        Assert.assertFalse(a.eq(b, highPrecision));
        Assert.assertTrue(a.eq(b, lowPrecision));

        Assert.assertFalse(a.eq(c, highPrecision));
        Assert.assertTrue(a.eq(c, lowPrecision));

        Assert.assertTrue(a.eq(d, highPrecision));
        Assert.assertTrue(a.eq(d, lowPrecision));
    }

    @Test
    public void testEq_wrapAround() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        Point1S a = Point1S.ZERO;
        Point1S b = Point1S.of(1e-3);
        Point1S c = Point1S.of(-1e-3);

        // act/assert
        Assert.assertTrue(a.eq(a, precision));

        Assert.assertTrue(a.eq(b, precision));
        Assert.assertTrue(b.eq(a, precision));

        Assert.assertTrue(a.eq(c, precision));
        Assert.assertTrue(c.eq(a, precision));
    }

    @Test
    public void testDistance() {
        // arrange
        Point1S a = Point1S.of(0.0);
        Point1S b = Point1S.of(PlaneAngleRadians.PI - 0.5);
        Point1S c = Point1S.of(PlaneAngleRadians.PI);
        Point1S d = Point1S.of(PlaneAngleRadians.PI + 0.5);
        Point1S e = Point1S.of(4.0);

        // act/assert
        Assert.assertEquals(0.0, a.distance(a), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI - 0.5, a.distance(b), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI - 0.5, b.distance(a), TEST_EPS);

        Assert.assertEquals(PlaneAngleRadians.PI, a.distance(c), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI, c.distance(a), TEST_EPS);

        Assert.assertEquals(PlaneAngleRadians.PI - 0.5, a.distance(d), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI - 0.5, d.distance(a), TEST_EPS);

        Assert.assertEquals(PlaneAngleRadians.TWO_PI - 4, a.distance(e), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.TWO_PI - 4, e.distance(a), TEST_EPS);
    }

    @Test
    public void testSignedDistance() {
        // arrange
        Point1S a = Point1S.of(0.0);
        Point1S b = Point1S.of(PlaneAngleRadians.PI - 0.5);
        Point1S c = Point1S.of(PlaneAngleRadians.PI);
        Point1S d = Point1S.of(PlaneAngleRadians.PI + 0.5);
        Point1S e = Point1S.of(4.0);

        // act/assert
        Assert.assertEquals(0.0, a.signedDistance(a), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI - 0.5, a.signedDistance(b), TEST_EPS);
        Assert.assertEquals(-PlaneAngleRadians.PI + 0.5, b.signedDistance(a), TEST_EPS);

        Assert.assertEquals(-PlaneAngleRadians.PI, a.signedDistance(c), TEST_EPS);
        Assert.assertEquals(-PlaneAngleRadians.PI, c.signedDistance(a), TEST_EPS);

        Assert.assertEquals(-PlaneAngleRadians.PI + 0.5, a.signedDistance(d), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI - 0.5, d.signedDistance(a), TEST_EPS);

        Assert.assertEquals(-PlaneAngleRadians.TWO_PI + 4, a.signedDistance(e), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.TWO_PI - 4, e.signedDistance(a), TEST_EPS);
    }

    @Test
    public void testDistance_inRangeZeroToPi() {
        for (double a = -4 * PlaneAngleRadians.PI; a < 4 * PlaneAngleRadians.PI; a += 0.1) {
            for (double b = -4 * PlaneAngleRadians.PI; b < 4 * PlaneAngleRadians.PI; b += 0.1) {
                // arrange
                Point1S p1 = Point1S.of(a);
                Point1S p2 = Point1S.of(b);

                // act/assert
                double d1 = p1.distance(p2);
                Assert.assertTrue(d1 >= 0 && d1 <= PlaneAngleRadians.PI);

                double d2 = p2.distance(p1);
                Assert.assertTrue(d2 >= 0 && d2 <= PlaneAngleRadians.PI);
            }
        }
    }

    @Test
    public void testNormalize() {
        for (double az = -PlaneAngleRadians.TWO_PI; az < 2 * PlaneAngleRadians.TWO_PI; az += 0.2) {
            // arrange
            Point1S pt = Point1S.of(az);

            double expectedPiNorm = PlaneAngleRadians.normalizeBetweenZeroAndTwoPi(az);
            double expectedZeroNorm = PlaneAngleRadians.normalizeBetweenMinusPiAndPi(az);

            // act
            Point1S piNorm = pt.normalize(Point1S.PI);
            Point1S zeroNorm = pt.normalize(0.0);

            // assert
            Assert.assertEquals(expectedPiNorm, piNorm.getAzimuth(), TEST_EPS);
            Assert.assertEquals(pt.getNormalizedAzimuth(), piNorm.getNormalizedAzimuth(), TEST_EPS);

            Assert.assertEquals(expectedZeroNorm, zeroNorm.getAzimuth(), TEST_EPS);
            Assert.assertEquals(pt.getNormalizedAzimuth(), zeroNorm.getNormalizedAzimuth(), TEST_EPS);
        }
    }

    @Test
    public void testNormalize_nonFinite() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Point1S.of(Double.POSITIVE_INFINITY).normalize(0.0);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Point1S.of(Double.NEGATIVE_INFINITY).normalize(0.0);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Point1S.of(Double.NaN).normalize(Point1S.ZERO);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testAbove() {
        // arrange
        Point1S p1 = Point1S.ZERO;
        Point1S p2 = Point1S.of(PlaneAngle.ofDegrees(90));
        Point1S p3 = Point1S.PI;
        Point1S p4 = Point1S.of(PlaneAngle.ofDegrees(-90));
        Point1S p5 = Point1S.of(PlaneAngleRadians.TWO_PI);

        // act/assert
        checkPoint(p1.above(p1), 0);
        checkPoint(p2.above(p1), PlaneAngleRadians.PI_OVER_TWO);
        checkPoint(p3.above(p1), PlaneAngleRadians.PI);
        checkPoint(p4.above(p1), 1.5 * PlaneAngleRadians.PI);
        checkPoint(p5.above(p1), 0);

        checkPoint(p1.above(p3), PlaneAngleRadians.TWO_PI);
        checkPoint(p2.above(p3), 2.5 * PlaneAngleRadians.PI);
        checkPoint(p3.above(p3), PlaneAngleRadians.PI);
        checkPoint(p4.above(p3), 1.5 * PlaneAngleRadians.PI);
        checkPoint(p5.above(p3), PlaneAngleRadians.TWO_PI);
    }

    @Test
    public void testAbove_nonFinite() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Point1S.of(Double.POSITIVE_INFINITY).above(Point1S.ZERO);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Point1S.of(Double.NEGATIVE_INFINITY).above(Point1S.ZERO);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Point1S.of(Double.NaN).above(Point1S.ZERO);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testBelow() {
        // arrange
        Point1S p1 = Point1S.ZERO;
        Point1S p2 = Point1S.of(PlaneAngle.ofDegrees(90));
        Point1S p3 = Point1S.PI;
        Point1S p4 = Point1S.of(PlaneAngle.ofDegrees(-90));
        Point1S p5 = Point1S.of(PlaneAngleRadians.TWO_PI);

        // act/assert
        checkPoint(p1.below(p1), -PlaneAngleRadians.TWO_PI);
        checkPoint(p2.below(p1), -1.5 * PlaneAngleRadians.PI);
        checkPoint(p3.below(p1), -PlaneAngleRadians.PI);
        checkPoint(p4.below(p1), -PlaneAngleRadians.PI_OVER_TWO);
        checkPoint(p5.below(p1), -PlaneAngleRadians.TWO_PI);

        checkPoint(p1.below(p3), 0.0);
        checkPoint(p2.below(p3), PlaneAngleRadians.PI_OVER_TWO);
        checkPoint(p3.below(p3), -PlaneAngleRadians.PI);
        checkPoint(p4.below(p3), -PlaneAngleRadians.PI_OVER_TWO);
        checkPoint(p5.below(p3), 0.0);
    }

    @Test
    public void testBelow_nonFinite() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Point1S.of(Double.POSITIVE_INFINITY).below(Point1S.ZERO);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Point1S.of(Double.NEGATIVE_INFINITY).below(Point1S.ZERO);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Point1S.of(Double.NaN).below(Point1S.ZERO);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testToString() {
        // act/assert
        Assert.assertEquals("(0.0)", Point1S.of(0.0).toString());
        Assert.assertEquals("(1.0)", Point1S.of(1.0).toString());
    }

    @Test
    public void testParse() {
        // act/assert
        checkPoint(Point1S.parse("(0)"), 0.0);
        checkPoint(Point1S.parse("(1)"), 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_failure() {
        // act/assert
        Point1S.parse("abc");
    }

    private static void checkPoint(Point1S pt, double az) {
        checkPoint(pt, az, PlaneAngleRadians.normalizeBetweenZeroAndTwoPi(az));
    }

    private static void checkPoint(Point1S pt, double az, double normAz) {
        Assert.assertEquals(az, pt.getAzimuth(), TEST_EPS);
        Assert.assertEquals(normAz, pt.getNormalizedAzimuth(), TEST_EPS);

        Assert.assertEquals(1, pt.getDimension());

        Assert.assertEquals(Double.isFinite(az), pt.isFinite());
        Assert.assertEquals(Double.isInfinite(az), pt.isInfinite());

        Vector2D vec = pt.getVector();
        if (pt.isFinite()) {
            Assert.assertEquals(1, vec.norm(), TEST_EPS);
            Assert.assertEquals(normAz, PolarCoordinates.fromCartesian(vec).getAzimuth(), TEST_EPS);
        } else {
            Assert.assertNull(vec);
        }
    }
}
