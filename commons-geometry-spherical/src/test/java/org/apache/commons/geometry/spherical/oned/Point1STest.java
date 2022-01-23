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
import org.apache.commons.geometry.euclidean.twod.PolarCoordinates;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Point1STest {

    private static final double TEST_EPS = 1e-10;

    @Test
    void testConstants() {
        // act/assert
        Assertions.assertEquals(0.0, Point1S.ZERO.getAzimuth(), TEST_EPS);
        Assertions.assertEquals(Math.PI, Point1S.PI.getAzimuth(), TEST_EPS);
    }

    @Test
    void testNormalizedAzimuthComparator() {
        // arrange
        final Comparator<Point1S> comp = Point1S.NORMALIZED_AZIMUTH_ASCENDING_ORDER;

        // act/assert
        Assertions.assertEquals(0, comp.compare(Point1S.of(1), Point1S.of(1)));
        Assertions.assertEquals(-1, comp.compare(Point1S.of(0), Point1S.of(1)));
        Assertions.assertEquals(1, comp.compare(Point1S.of(1), Point1S.of(0)));
        Assertions.assertEquals(1, comp.compare(Point1S.of(1), Point1S.of(0.1 + Angle.TWO_PI)));

        Assertions.assertEquals(1, comp.compare(null, Point1S.of(0)));
        Assertions.assertEquals(-1, comp.compare(Point1S.of(0), null));
        Assertions.assertEquals(0, comp.compare(null, null));
    }

    @Test
    void testOf() {
        // act/assert
        checkPoint(Point1S.of(0), 0, 0);
        checkPoint(Point1S.of(1), 1, 1);
        checkPoint(Point1S.of(-1), -1, Angle.TWO_PI - 1);

        checkPoint(Point1S.of(Angle.Deg.of(90)), Angle.PI_OVER_TWO, Angle.PI_OVER_TWO);
        checkPoint(Point1S.of(Angle.Turn.of(0.5)), Math.PI, Math.PI);
        checkPoint(Point1S.of(-Angle.PI_OVER_TWO), -Angle.PI_OVER_TWO, 1.5 * Math.PI);

        final double base = Angle.PI_OVER_TWO;
        for (int k = -3; k <= 3; ++k) {
            final double az = base + (k * Angle.TWO_PI);
            checkPoint(Point1S.of(az), az, base);
        }
    }

    @Test
    void testFrom_vector() {
        // act/assert
        checkPoint(Point1S.from(Vector2D.of(2, 0)), 0.0);
        checkPoint(Point1S.from(Vector2D.of(0, 0.1)), Angle.PI_OVER_TWO);
        checkPoint(Point1S.from(Vector2D.of(-0.5, 0)), Math.PI);
        checkPoint(Point1S.from(Vector2D.of(0, -100)), 1.5 * Math.PI);
    }

    @Test
    void testFrom_polar() {
        // act/assert
        checkPoint(Point1S.from(PolarCoordinates.of(100, 0)), 0.0);
        checkPoint(Point1S.from(PolarCoordinates.of(1, Angle.PI_OVER_TWO)), Angle.PI_OVER_TWO);
        checkPoint(Point1S.from(PolarCoordinates.of(0.5, Math.PI)), Math.PI);
        checkPoint(Point1S.from(PolarCoordinates.of(1e-4, -Angle.PI_OVER_TWO)), 1.5 * Math.PI);
    }

    @Test
    void testFrom_polar_invalidAzimuths() {
        // act/assert
        checkPoint(Point1S.from(PolarCoordinates.of(100, Double.POSITIVE_INFINITY)), Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        checkPoint(Point1S.from(PolarCoordinates.of(100, Double.NEGATIVE_INFINITY)), Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        checkPoint(Point1S.from(PolarCoordinates.of(100, Double.NaN)), Double.NaN, Double.NaN);
    }

    @Test
    void testNaN() {
        // act
        final Point1S pt = Point1S.of(Double.NaN);

        // assert
        Assertions.assertTrue(pt.isNaN());
        Assertions.assertTrue(Point1S.NaN.isNaN());

        Assertions.assertTrue(Double.isNaN(pt.getAzimuth()));
        Assertions.assertTrue(Double.isNaN(pt.getNormalizedAzimuth()));
        Assertions.assertNull(pt.getVector());

        Assertions.assertEquals(Point1S.NaN, pt);
        Assertions.assertNotEquals(Point1S.of(1.0), Point1S.NaN);
    }

    @Test
    void testGetDimension() {
        // arrange
        final Point1S p = Point1S.of(0.0);

        // act/assert
        Assertions.assertEquals(1, p.getDimension());
    }

    @Test
    void testInfinite() {
        // act/assert
        Assertions.assertTrue(Point1S.of(Double.POSITIVE_INFINITY).isInfinite());
        Assertions.assertTrue(Point1S.of(Double.NEGATIVE_INFINITY).isInfinite());

        Assertions.assertFalse(Point1S.NaN.isInfinite());
        Assertions.assertFalse(Point1S.of(1).isInfinite());
    }

    @Test
    void testFinite() {
        // act/assert
        Assertions.assertTrue(Point1S.of(0).isFinite());
        Assertions.assertTrue(Point1S.of(1).isFinite());

        Assertions.assertFalse(Point1S.of(Double.POSITIVE_INFINITY).isFinite());
        Assertions.assertFalse(Point1S.of(Double.NEGATIVE_INFINITY).isFinite());
        Assertions.assertFalse(Point1S.NaN.isFinite());
    }

    @Test
    void testAntipodal() {
        for (double az = -6 * Math.PI; az <= 6 * Math.PI; az += 0.1) {
            // arrange
            final Point1S pt = Point1S.of(az);

            // act
            final Point1S result = pt.antipodal();

            // assert
            Assertions.assertTrue(result.getAzimuth() >= 0 && result.getAzimuth() < Angle.TWO_PI);
            Assertions.assertEquals(Math.PI, pt.distance(result), TEST_EPS);
        }
    }

    @Test
    void testHashCode() {
        // act
        final Point1S a = Point1S.of(1.0);
        final Point1S b = Point1S.of(2.0);
        final Point1S c = Point1S.of(1.0);
        final Point1S d = Point1S.of(1.0 + Math.PI);

        final int hash = a.hashCode();

        // assert
        Assertions.assertEquals(hash, a.hashCode());
        Assertions.assertNotEquals(hash, b.hashCode());
        Assertions.assertEquals(hash, c.hashCode());
        Assertions.assertNotEquals(hash, d.hashCode());

        Assertions.assertEquals(Point1S.NaN.hashCode(), Point1S.of(Double.NaN).hashCode());
    }

    @Test
    void testEquals() {
        // act
        final Point1S a = Point1S.of(1.0);
        final Point1S b = Point1S.of(2.0);
        final Point1S c = Point1S.of(1.0 + Math.PI);
        final Point1S d = Point1S.of(1.0);
        final Point1S e = Point1S.of(Double.NaN);

        // assert
        GeometryTestUtils.assertSimpleEqualsCases(a);

        Assertions.assertNotEquals(a, b);
        Assertions.assertNotEquals(b, a);

        Assertions.assertNotEquals(a, c);
        Assertions.assertNotEquals(c, a);

        Assertions.assertEquals(a, d);
        Assertions.assertEquals(d, a);

        Assertions.assertNotEquals(a, e);
        Assertions.assertEquals(Point1S.NaN, e);
    }

    @Test
    void testEqualsAndHashCode_signedZeroConsistency() {
        // arrange
        final Point1S a = Point1S.of(0.0);
        final Point1S b = Point1S.of(-0.0);
        final Point1S c = Point1S.of(0.0);
        final Point1S d = Point1S.of(-0.0);

        // act/assert
        Assertions.assertFalse(a.equals(b));
        Assertions.assertNotEquals(a.hashCode(), b.hashCode());

        Assertions.assertTrue(a.equals(c));
        Assertions.assertEquals(a.hashCode(), c.hashCode());

        Assertions.assertTrue(b.equals(d));
        Assertions.assertEquals(b.hashCode(), d.hashCode());
    }

    @Test
    void testEq() {
        // arrange
        final Precision.DoubleEquivalence highPrecision = Precision.doubleEquivalenceOfEpsilon(1e-10);
        final Precision.DoubleEquivalence lowPrecision = Precision.doubleEquivalenceOfEpsilon(1e-2);

        final Point1S a = Point1S.of(1);
        final Point1S b = Point1S.of(0.9999);
        final Point1S c = Point1S.of(1.00001);
        final Point1S d = Point1S.of(1 + (3 * Angle.TWO_PI));

        // act/assert
        Assertions.assertTrue(a.eq(a, highPrecision));
        Assertions.assertTrue(a.eq(a, lowPrecision));

        Assertions.assertFalse(a.eq(b, highPrecision));
        Assertions.assertTrue(a.eq(b, lowPrecision));

        Assertions.assertFalse(a.eq(c, highPrecision));
        Assertions.assertTrue(a.eq(c, lowPrecision));

        Assertions.assertTrue(a.eq(d, highPrecision));
        Assertions.assertTrue(a.eq(d, lowPrecision));
    }

    @Test
    void testEq_wrapAround() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-2);

        final Point1S a = Point1S.ZERO;
        final Point1S b = Point1S.of(1e-3);
        final Point1S c = Point1S.of(-1e-3);

        // act/assert
        Assertions.assertTrue(a.eq(a, precision));

        Assertions.assertTrue(a.eq(b, precision));
        Assertions.assertTrue(b.eq(a, precision));

        Assertions.assertTrue(a.eq(c, precision));
        Assertions.assertTrue(c.eq(a, precision));
    }

    @Test
    void testEqZero() {
        // arrange
        final double eps = 1e-8;
        final double delta = 1e-3 * eps;
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(eps);

        // act/assert
        checkEqZero(0, precision);
        checkEqZero(Angle.TWO_PI, precision);
        checkEqZero(-Angle.TWO_PI, precision);

        checkNotEqZero(Math.PI, precision);
        checkNotEqZero(-Math.PI, precision);

        checkNotEqZero(Angle.PI_OVER_TWO, precision);
        checkNotEqZero(-Angle.PI_OVER_TWO, precision);

        checkNotEqZero(-eps - delta, precision);
        checkNotEqZero(-eps - delta - Angle.TWO_PI, precision);

        for (double a = -eps + delta; a < eps; a += delta) {
            checkEqZero(a, precision);
            checkEqZero(a - Angle.TWO_PI, precision);
            checkEqZero(a + Angle.TWO_PI, precision);
        }

        checkNotEqZero(eps + delta, precision);
        checkNotEqZero(eps + delta + Angle.TWO_PI, precision);
    }

    private void checkEqZero(final double az, final Precision.DoubleEquivalence precision) {
        final Point1S pt = Point1S.of(az);

        Assertions.assertTrue(pt.eqZero(precision));
        Assertions.assertTrue(Point1S.ZERO.eq(pt, precision));
    }

    private void checkNotEqZero(final double az, final Precision.DoubleEquivalence precision) {
        final Point1S pt = Point1S.of(az);

        Assertions.assertFalse(pt.eqZero(precision));
        Assertions.assertFalse(Point1S.ZERO.eq(pt, precision));
    }

    @Test
    void testDistance() {
        // arrange
        final Point1S a = Point1S.of(0.0);
        final Point1S b = Point1S.of(Math.PI - 0.5);
        final Point1S c = Point1S.of(Math.PI);
        final Point1S d = Point1S.of(Math.PI + 0.5);
        final Point1S e = Point1S.of(4.0);

        // act/assert
        Assertions.assertEquals(0.0, a.distance(a), TEST_EPS);
        Assertions.assertEquals(Math.PI - 0.5, a.distance(b), TEST_EPS);
        Assertions.assertEquals(Math.PI - 0.5, b.distance(a), TEST_EPS);

        Assertions.assertEquals(Math.PI, a.distance(c), TEST_EPS);
        Assertions.assertEquals(Math.PI, c.distance(a), TEST_EPS);

        Assertions.assertEquals(Math.PI - 0.5, a.distance(d), TEST_EPS);
        Assertions.assertEquals(Math.PI - 0.5, d.distance(a), TEST_EPS);

        Assertions.assertEquals(Angle.TWO_PI - 4, a.distance(e), TEST_EPS);
        Assertions.assertEquals(Angle.TWO_PI - 4, e.distance(a), TEST_EPS);
    }

    @Test
    void testSignedDistance() {
        // arrange
        final Point1S a = Point1S.of(0.0);
        final Point1S b = Point1S.of(Math.PI - 0.5);
        final Point1S c = Point1S.of(Math.PI);
        final Point1S d = Point1S.of(Math.PI + 0.5);
        final Point1S e = Point1S.of(4.0);

        // act/assert
        Assertions.assertEquals(0.0, a.signedDistance(a), TEST_EPS);
        Assertions.assertEquals(Math.PI - 0.5, a.signedDistance(b), TEST_EPS);
        Assertions.assertEquals(-Math.PI + 0.5, b.signedDistance(a), TEST_EPS);

        Assertions.assertEquals(-Math.PI, a.signedDistance(c), TEST_EPS);
        Assertions.assertEquals(-Math.PI, c.signedDistance(a), TEST_EPS);

        Assertions.assertEquals(-Math.PI + 0.5, a.signedDistance(d), TEST_EPS);
        Assertions.assertEquals(Math.PI - 0.5, d.signedDistance(a), TEST_EPS);

        Assertions.assertEquals(-Angle.TWO_PI + 4, a.signedDistance(e), TEST_EPS);
        Assertions.assertEquals(Angle.TWO_PI - 4, e.signedDistance(a), TEST_EPS);
    }

    @Test
    void testDistance_inRangeZeroToPi() {
        for (double a = -4 * Math.PI; a < 4 * Math.PI; a += 0.1) {
            for (double b = -4 * Math.PI; b < 4 * Math.PI; b += 0.1) {
                // arrange
                final Point1S p1 = Point1S.of(a);
                final Point1S p2 = Point1S.of(b);

                // act/assert
                final double d1 = p1.distance(p2);
                Assertions.assertTrue(d1 >= 0 && d1 <= Math.PI);

                final double d2 = p2.distance(p1);
                Assertions.assertTrue(d2 >= 0 && d2 <= Math.PI);
            }
        }
    }

    @Test
    void testAbove() {
        // arrange
        final Point1S p1 = Point1S.ZERO;
        final Point1S p2 = Point1S.of(Angle.Deg.of(90));
        final Point1S p3 = Point1S.PI;
        final Point1S p4 = Point1S.of(Angle.Deg.of(-90));
        final Point1S p5 = Point1S.of(Angle.TWO_PI);

        // act/assert
        checkPoint(p1.above(p1), 0);
        checkPoint(p2.above(p1), Angle.PI_OVER_TWO);
        checkPoint(p3.above(p1), Math.PI);
        checkPoint(p4.above(p1), 1.5 * Math.PI);
        checkPoint(p5.above(p1), 0);

        checkPoint(p1.above(p3), Angle.TWO_PI);
        checkPoint(p2.above(p3), 2.5 * Math.PI);
        checkPoint(p3.above(p3), Math.PI);
        checkPoint(p4.above(p3), 1.5 * Math.PI);
        checkPoint(p5.above(p3), Angle.TWO_PI);
    }

    @Test
    void testAbove_nonFinite() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Point1S.of(Double.POSITIVE_INFINITY).above(Point1S.ZERO));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Point1S.of(Double.NEGATIVE_INFINITY).above(Point1S.ZERO));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Point1S.of(Double.NaN).above(Point1S.ZERO));
    }

    @Test
    void testToString() {
        // act/assert
        Assertions.assertEquals("(0.0)", Point1S.of(0.0).toString());
        Assertions.assertEquals("(1.0)", Point1S.of(1.0).toString());
    }

    @Test
    void testParse() {
        // act/assert
        checkPoint(Point1S.parse("(0)"), 0.0);
        checkPoint(Point1S.parse("(1)"), 1.0);
    }

    @Test
    void testParse_failure() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () ->  Point1S.parse("abc"));
    }

    private static void checkPoint(final Point1S pt, final double az) {
        checkPoint(pt, az, Angle.Rad.WITHIN_0_AND_2PI.applyAsDouble(az));
    }

    private static void checkPoint(final Point1S pt, final double az, final double normAz) {
        Assertions.assertEquals(az, pt.getAzimuth(), TEST_EPS);
        Assertions.assertEquals(normAz, pt.getNormalizedAzimuth(), TEST_EPS);

        Assertions.assertEquals(1, pt.getDimension());

        Assertions.assertEquals(Double.isFinite(az), pt.isFinite());
        Assertions.assertEquals(Double.isInfinite(az), pt.isInfinite());

        final Vector2D vec = pt.getVector();
        if (pt.isFinite()) {
            Assertions.assertEquals(1, vec.norm(), TEST_EPS);
            Assertions.assertEquals(normAz, PolarCoordinates.fromCartesian(vec).getAzimuth(), TEST_EPS);
        } else {
            Assertions.assertNull(vec);
        }
    }
}
