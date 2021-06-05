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
package org.apache.commons.geometry.spherical.twod;


import java.util.Comparator;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class Point2STest {

    private static final double TEST_EPS = 1e-10;

    @Test
    void testProperties() {
        for (int k = -2; k < 3; ++k) {
            // arrange
            final Point2S p = Point2S.of(1.0 + k * Angle.TWO_PI, 1.4);

            // act/assert
            Assertions.assertEquals(1.0, p.getAzimuth(), TEST_EPS);
            Assertions.assertEquals(1.4, p.getPolar(), TEST_EPS);

            Assertions.assertEquals(Math.cos(1.0) * Math.sin(1.4), p.getVector().getX(), TEST_EPS);
            Assertions.assertEquals(Math.sin(1.0) * Math.sin(1.4), p.getVector().getY(), TEST_EPS);
            Assertions.assertEquals(Math.cos(1.4), p.getVector().getZ(), TEST_EPS);

            Assertions.assertFalse(p.isNaN());
        }
    }

    @Test
    void testAzimuthPolarComparator() {
        // arrange
        final Comparator<Point2S> comp = Point2S.POLAR_AZIMUTH_ASCENDING_ORDER;

        // act/assert
        Assertions.assertEquals(0, comp.compare(Point2S.of(1, 2), Point2S.of(1, 2)));
        Assertions.assertEquals(1, comp.compare(Point2S.of(1, 2), Point2S.of(2, 1)));
        Assertions.assertEquals(-1, comp.compare(Point2S.of(2, 1), Point2S.of(1, 2)));

        Assertions.assertEquals(-1, comp.compare(Point2S.of(1, 2), Point2S.of(1, 3)));
        Assertions.assertEquals(1, comp.compare(Point2S.of(1, 3), Point2S.of(1, 2)));

        Assertions.assertEquals(1, comp.compare(null, Point2S.of(1, 2)));
        Assertions.assertEquals(-1, comp.compare(Point2S.of(1, 2), null));
        Assertions.assertEquals(0, comp.compare(null, null));
    }

    @Test
    void testFrom_vector() {
        // arrange
        final double quarterPi = 0.25 * Math.PI;

        // act/assert
        checkPoint(Point2S.from(Vector3D.of(1, 1, 0)), quarterPi, Angle.PI_OVER_TWO);
        checkPoint(Point2S.from(Vector3D.of(1, 0, 1)), 0, quarterPi);
        checkPoint(Point2S.from(Vector3D.of(0, 1, 1)), Angle.PI_OVER_TWO, quarterPi);

        checkPoint(Point2S.from(Vector3D.of(1, -1, 0)), Angle.TWO_PI - quarterPi, Angle.PI_OVER_TWO);
        checkPoint(Point2S.from(Vector3D.of(-1, 0, -1)), Math.PI, Math.PI - quarterPi);
        checkPoint(Point2S.from(Vector3D.of(0, -1, -1)), Angle.TWO_PI - Angle.PI_OVER_TWO, Math.PI - quarterPi);
    }

    @Test
    void testNaN() {
        // act/assert
        Assertions.assertTrue(Point2S.NaN.isNaN());
        Assertions.assertEquals(Point2S.NaN, Point2S.of(Double.NaN, 1.0));
        Assertions.assertNotEquals(Point2S.of(1.0, 1.3), Point2S.NaN);
        Assertions.assertNull(Point2S.NaN.getVector());

        Assertions.assertEquals(Point2S.NaN.hashCode(), Point2S.of(Double.NaN, Double.NaN).hashCode());
    }

    @Test
    void testInfinite() {
        // act/assert
        Assertions.assertTrue(Point2S.of(0, Double.POSITIVE_INFINITY).isInfinite());
        Assertions.assertTrue(Point2S.of(Double.POSITIVE_INFINITY, 0).isInfinite());

        Assertions.assertTrue(Point2S.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY).isInfinite());

        Assertions.assertFalse(Point2S.of(0, 0).isInfinite());
        Assertions.assertFalse(Point2S.of(1, 1).isInfinite());
        Assertions.assertFalse(Point2S.NaN.isInfinite());
    }

    @Test
    void testFinite() {
        // act/assert
        Assertions.assertTrue(Point2S.of(0, 0).isFinite());
        Assertions.assertTrue(Point2S.of(1, 1).isFinite());

        Assertions.assertFalse(Point2S.of(0, Double.POSITIVE_INFINITY).isFinite());
        Assertions.assertFalse(Point2S.of(Double.POSITIVE_INFINITY, 0).isFinite());
        Assertions.assertFalse(Point2S.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY).isFinite());

        Assertions.assertFalse(Point2S.NaN.isFinite());
    }

    @Test
    void testDistance() {
        // arrange
        final Point2S a = Point2S.of(1.0, 0.5 * Math.PI);
        final Point2S b = Point2S.of(a.getAzimuth() + 0.5 * Math.PI, a.getPolar());

        // act/assert
        Assertions.assertEquals(0.5 * Math.PI, a.distance(b), 1.0e-10);
        Assertions.assertEquals(Math.PI, a.distance(a.antipodal()), 1.0e-10);
        Assertions.assertEquals(0.5 * Math.PI, Point2S.MINUS_I.distance(Point2S.MINUS_K), 1.0e-10);
        Assertions.assertEquals(0.0, Point2S.of(1.0, 0).distance(Point2S.of(2.0, 0)), 1.0e-10);
    }

    @Test
    void testSlerp_alongEquator() {
        // arrange
        final Point2S p1 = Point2S.PLUS_I;
        final Point2S p2 = Point2S.PLUS_J;

        // act/assert
        SphericalTestUtils.assertPointsEq(p1, p1.slerp(p2, 0), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(0.25 * Angle.PI_OVER_TWO, Angle.PI_OVER_TWO), p1.slerp(p2, 0.25), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(0.5 * Angle.PI_OVER_TWO, Angle.PI_OVER_TWO), p1.slerp(p2, 0.5), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(0.75 * Angle.PI_OVER_TWO, Angle.PI_OVER_TWO), p1.slerp(p2, 0.75), TEST_EPS);
        SphericalTestUtils.assertPointsEq(p2, p1.slerp(p2, 1), TEST_EPS);

        SphericalTestUtils.assertPointsEq(p2, p2.slerp(p1, 0), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(0.75 * Angle.PI_OVER_TWO, Angle.PI_OVER_TWO), p2.slerp(p1, 0.25), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(0.5 * Angle.PI_OVER_TWO, Angle.PI_OVER_TWO), p2.slerp(p1, 0.5), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(0.25 * Angle.PI_OVER_TWO, Angle.PI_OVER_TWO), p2.slerp(p1, 0.75), TEST_EPS);
        SphericalTestUtils.assertPointsEq(p1, p2.slerp(p1, 1), TEST_EPS);

        SphericalTestUtils.assertPointsEq(Point2S.MINUS_I, p1.slerp(p2, 2), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.MINUS_J, p1.slerp(p2, -1), TEST_EPS);
    }

    @Test
    void testSlerp_alongMeridian() {
        // arrange
        final Point2S p1 = Point2S.PLUS_J;
        final Point2S p2 = Point2S.PLUS_K;

        // act/assert
        SphericalTestUtils.assertPointsEq(p1, p1.slerp(p2, 0), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(Angle.PI_OVER_TWO, 0.75 * Angle.PI_OVER_TWO), p1.slerp(p2, 0.25), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(Angle.PI_OVER_TWO, 0.5 * Angle.PI_OVER_TWO), p1.slerp(p2, 0.5), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(Angle.PI_OVER_TWO, 0.25 * Angle.PI_OVER_TWO), p1.slerp(p2, 0.75), TEST_EPS);
        SphericalTestUtils.assertPointsEq(p2, p1.slerp(p2, 1), TEST_EPS);

        SphericalTestUtils.assertPointsEq(p2, p2.slerp(p1, 0), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(Angle.PI_OVER_TWO, 0.25 * Angle.PI_OVER_TWO), p2.slerp(p1, 0.25), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(Angle.PI_OVER_TWO, 0.5 * Angle.PI_OVER_TWO), p2.slerp(p1, 0.5), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(Angle.PI_OVER_TWO, 0.75 * Angle.PI_OVER_TWO), p2.slerp(p1, 0.75), TEST_EPS);
        SphericalTestUtils.assertPointsEq(p1, p2.slerp(p1, 1), TEST_EPS);

        SphericalTestUtils.assertPointsEq(Point2S.MINUS_J, p1.slerp(p2, 2), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.MINUS_K, p1.slerp(p2, -1), TEST_EPS);
    }

    @Test
    void testSlerp_samePoint() {
        // arrange
        final Point2S p1 = Point2S.PLUS_I;

        // act/assert
        SphericalTestUtils.assertPointsEq(p1, p1.slerp(p1, 0), TEST_EPS);
        SphericalTestUtils.assertPointsEq(p1, p1.slerp(p1, 0.5), TEST_EPS);
        SphericalTestUtils.assertPointsEq(p1, p1.slerp(p1, 1), TEST_EPS);
    }

    @Test
    void testSlerp_antipodal() {
        // arrange
        final Point2S p1 = Point2S.PLUS_I;
        final Point2S p2 = Point2S.MINUS_I;

        // act/assert
        SphericalTestUtils.assertPointsEq(p1, p1.slerp(p1, 0), TEST_EPS);
        SphericalTestUtils.assertPointsEq(p1, p1.slerp(p1, 1), TEST_EPS);

        final Point2S pt = p1.slerp(p2, 0.5);
        Assertions.assertEquals(p1.distance(pt), p2.distance(pt), TEST_EPS);
    }

    @Test
    void testAntipodal() {
        for (double az = -6 * Math.PI; az <= 6 * Math.PI; az += 0.1) {
            for (double p = 0; p <= Math.PI; p += 0.1) {
                // arrange
                final Point2S pt = Point2S.of(az, p);

                // act
                final Point2S result = pt.antipodal();

                // assert
                Assertions.assertEquals(Math.PI, pt.distance(result), TEST_EPS);

                // check that the azimuth and polar components of the point are correct by creating a
                // new point and checking the distance
                Assertions.assertEquals(Math.PI,
                        Point2S.of(result.getAzimuth(), result.getPolar()).distance(pt), TEST_EPS);

                // check that the vectors point in opposite directions
                Assertions.assertEquals(-1, pt.getVector().dot(result.getVector()), TEST_EPS);
            }
        }
    }

    @Test
    void testAntipodal_numericalStability() {
        // arrange
        final double eps = 1e-16;
        final Point2S pt = Point2S.of(1, 2);

        // act
        final Point2S result = pt.antipodal().antipodal();

        // assert
        Assertions.assertEquals(1.0, result.getAzimuth(), eps);
        Assertions.assertEquals(2.0, result.getPolar(), eps);
    }

    @Test
    void testDimension() {
        // arrange
        final Point2S pt = Point2S.of(1, 2);

        // act/assert
        Assertions.assertEquals(2, pt.getDimension());
    }

    @Test
    void testEq() {
        // arrange
        final Precision.DoubleEquivalence smallEps = Precision.doubleEquivalenceOfEpsilon(1e-5);
        final Precision.DoubleEquivalence largeEps = Precision.doubleEquivalenceOfEpsilon(5e-1);

        final Point2S a = Point2S.of(1.0, 2.0);
        final Point2S b = Point2S.of(1.0, 2.01);
        final Point2S c = Point2S.of(1.01, 2.0);
        final Point2S d = Point2S.of(1.0, 2.0);
        final Point2S e = Point2S.of(3.0, 2.0);

        // act/assert
        Assertions.assertTrue(a.eq(a, smallEps));
        Assertions.assertFalse(a.eq(b, smallEps));
        Assertions.assertFalse(a.eq(c, smallEps));
        Assertions.assertTrue(a.eq(d, smallEps));
        Assertions.assertFalse(a.eq(e, smallEps));

        Assertions.assertTrue(a.eq(a, largeEps));
        Assertions.assertTrue(a.eq(b, largeEps));
        Assertions.assertTrue(a.eq(c, largeEps));
        Assertions.assertTrue(a.eq(d, largeEps));
        Assertions.assertFalse(a.eq(e, largeEps));
    }

    @Test
    void testHashCode() {
        // arrange
        final Point2S a = Point2S.of(1.0, 2.0);
        final Point2S b = Point2S.of(1.0, 3.0);
        final Point2S c = Point2S.of(4.0, 2.0);
        final Point2S d = Point2S.of(1.0, 2.0);

        // act
        final int hash = a.hashCode();

        // assert
        Assertions.assertEquals(hash, a.hashCode());

        Assertions.assertNotEquals(hash, b.hashCode());
        Assertions.assertNotEquals(hash, c.hashCode());

        Assertions.assertEquals(hash, d.hashCode());
    }

    @Test
    void testEquals() {
        // arrange
        final Point2S a = Point2S.of(1.0, 2.0);
        final Point2S b = Point2S.of(1.0, 3.0);
        final Point2S c = Point2S.of(4.0, 2.0);
        final Point2S d = Point2S.of(1.0, 2.0);

        // act/assert
        GeometryTestUtils.assertSimpleEqualsCases(a);

        Assertions.assertNotEquals(a, b);
        Assertions.assertNotEquals(a, c);

        Assertions.assertEquals(a, d);
        Assertions.assertEquals(d, a);
    }

    @Test
    void testEquals_poles() {
        // arrange
        final Point2S a = Point2S.of(1.0, 0.0);
        final Point2S b = Point2S.of(0.0, 0.0);
        final Point2S c = Point2S.of(1.0, 0.0);

        final Point2S d = Point2S.of(-1.0, Math.PI);
        final Point2S e = Point2S.of(0.0, Math.PI);
        final Point2S f = Point2S.of(-1.0, Math.PI);

        // act/assert
        Assertions.assertEquals(a, a);
        Assertions.assertNotEquals(a, b);
        Assertions.assertEquals(a, c);

        Assertions.assertEquals(d, d);
        Assertions.assertNotEquals(d, e);
        Assertions.assertEquals(d, f);
    }

    @Test
    void testToString() {
        // act/assert
        Assertions.assertEquals("(0.0, 0.0)", Point2S.of(0.0, 0.0).toString());
        Assertions.assertEquals("(1.0, 2.0)", Point2S.of(1.0, 2.0).toString());
    }

    @Test
    void testParse() {
        // act/assert
        checkPoint(Point2S.parse("(0,0)"), 0.0, 0.0);
        checkPoint(Point2S.parse("(1,2)"), 1.0, 2.0);
    }

    @Test
    void testParse_failure() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Point2S.parse("abc"));
    }

    private static void checkPoint(final Point2S p, final double az, final double polar) {
        final String msg = "Expected (" + az + "," + polar + ") but was " + p;

        Assertions.assertEquals(az, p.getAzimuth(), TEST_EPS, msg);
        Assertions.assertEquals(polar, p.getPolar(), TEST_EPS, msg);
    }
}
