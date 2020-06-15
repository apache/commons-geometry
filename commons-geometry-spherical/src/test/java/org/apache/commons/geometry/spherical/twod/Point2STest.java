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

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class Point2STest {

    private static final double TEST_EPS = 1e-10;

    @Test
    public void testProperties() {
        for (int k = -2; k < 3; ++k) {
            // arrange
            Point2S p = Point2S.of(1.0 + k * PlaneAngleRadians.TWO_PI, 1.4);

            // act/assert
            Assert.assertEquals(1.0, p.getAzimuth(), TEST_EPS);
            Assert.assertEquals(1.4, p.getPolar(), TEST_EPS);

            Assert.assertEquals(Math.cos(1.0) * Math.sin(1.4), p.getVector().getX(), TEST_EPS);
            Assert.assertEquals(Math.sin(1.0) * Math.sin(1.4), p.getVector().getY(), TEST_EPS);
            Assert.assertEquals(Math.cos(1.4), p.getVector().getZ(), TEST_EPS);

            Assert.assertFalse(p.isNaN());
        }
    }

    @Test
    public void testAzimuthPolarComparator() {
        // arrange
        Comparator<Point2S> comp = Point2S.POLAR_AZIMUTH_ASCENDING_ORDER;

        // act/assert
        Assert.assertEquals(0, comp.compare(Point2S.of(1, 2), Point2S.of(1, 2)));
        Assert.assertEquals(1, comp.compare(Point2S.of(1, 2), Point2S.of(2, 1)));
        Assert.assertEquals(-1, comp.compare(Point2S.of(2, 1), Point2S.of(1, 2)));

        Assert.assertEquals(-1, comp.compare(Point2S.of(1, 2), Point2S.of(1, 3)));
        Assert.assertEquals(1, comp.compare(Point2S.of(1, 3), Point2S.of(1, 2)));

        Assert.assertEquals(1, comp.compare(null, Point2S.of(1, 2)));
        Assert.assertEquals(-1, comp.compare(Point2S.of(1, 2), null));
        Assert.assertEquals(0, comp.compare(null, null));
    }

    @Test
    public void testFrom_vector() {
        // arrange
        double quarterPi = 0.25 * PlaneAngleRadians.PI;

        // act/assert
        checkPoint(Point2S.from(Vector3D.of(1, 1, 0)), quarterPi, PlaneAngleRadians.PI_OVER_TWO);
        checkPoint(Point2S.from(Vector3D.of(1, 0, 1)), 0, quarterPi);
        checkPoint(Point2S.from(Vector3D.of(0, 1, 1)), PlaneAngleRadians.PI_OVER_TWO, quarterPi);

        checkPoint(Point2S.from(Vector3D.of(1, -1, 0)), PlaneAngleRadians.TWO_PI - quarterPi, PlaneAngleRadians.PI_OVER_TWO);
        checkPoint(Point2S.from(Vector3D.of(-1, 0, -1)), PlaneAngleRadians.PI, PlaneAngleRadians.PI - quarterPi);
        checkPoint(Point2S.from(Vector3D.of(0, -1, -1)), PlaneAngleRadians.TWO_PI - PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI - quarterPi);
    }

    @Test
    public void testNaN() {
        // act/assert
        Assert.assertTrue(Point2S.NaN.isNaN());
        Assert.assertEquals(Point2S.NaN, Point2S.of(Double.NaN, 1.0));
        Assert.assertNotEquals(Point2S.of(1.0, 1.3), Point2S.NaN);
        Assert.assertNull(Point2S.NaN.getVector());

        Assert.assertEquals(Point2S.NaN.hashCode(), Point2S.of(Double.NaN, Double.NaN).hashCode());
    }

    @Test
    public void testInfinite() {
        // act/assert
        Assert.assertTrue(Point2S.of(0, Double.POSITIVE_INFINITY).isInfinite());
        Assert.assertTrue(Point2S.of(Double.POSITIVE_INFINITY, 0).isInfinite());

        Assert.assertTrue(Point2S.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY).isInfinite());

        Assert.assertFalse(Point2S.of(0, 0).isInfinite());
        Assert.assertFalse(Point2S.of(1, 1).isInfinite());
        Assert.assertFalse(Point2S.NaN.isInfinite());
    }

    @Test
    public void testFinite() {
        // act/assert
        Assert.assertTrue(Point2S.of(0, 0).isFinite());
        Assert.assertTrue(Point2S.of(1, 1).isFinite());

        Assert.assertFalse(Point2S.of(0, Double.POSITIVE_INFINITY).isFinite());
        Assert.assertFalse(Point2S.of(Double.POSITIVE_INFINITY, 0).isFinite());
        Assert.assertFalse(Point2S.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY).isFinite());

        Assert.assertFalse(Point2S.NaN.isFinite());
    }

    @Test
    public void testDistance() {
        // arrange
        Point2S a = Point2S.of(1.0, 0.5 * PlaneAngleRadians.PI);
        Point2S b = Point2S.of(a.getAzimuth() + 0.5 * PlaneAngleRadians.PI, a.getPolar());

        // act/assert
        Assert.assertEquals(0.5 * PlaneAngleRadians.PI, a.distance(b), 1.0e-10);
        Assert.assertEquals(PlaneAngleRadians.PI, a.distance(a.antipodal()), 1.0e-10);
        Assert.assertEquals(0.5 * PlaneAngleRadians.PI, Point2S.MINUS_I.distance(Point2S.MINUS_K), 1.0e-10);
        Assert.assertEquals(0.0, Point2S.of(1.0, 0).distance(Point2S.of(2.0, 0)), 1.0e-10);
    }

    @Test
    public void testSlerp_alongEquator() {
        // arrange
        Point2S p1 = Point2S.PLUS_I;
        Point2S p2 = Point2S.PLUS_J;

        // act/assert
        SphericalTestUtils.assertPointsEq(p1, p1.slerp(p2, 0), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(0.25 * PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO), p1.slerp(p2, 0.25), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(0.5 * PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO), p1.slerp(p2, 0.5), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(0.75 * PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO), p1.slerp(p2, 0.75), TEST_EPS);
        SphericalTestUtils.assertPointsEq(p2, p1.slerp(p2, 1), TEST_EPS);

        SphericalTestUtils.assertPointsEq(p2, p2.slerp(p1, 0), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(0.75 * PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO), p2.slerp(p1, 0.25), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(0.5 * PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO), p2.slerp(p1, 0.5), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(0.25 * PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO), p2.slerp(p1, 0.75), TEST_EPS);
        SphericalTestUtils.assertPointsEq(p1, p2.slerp(p1, 1), TEST_EPS);

        SphericalTestUtils.assertPointsEq(Point2S.MINUS_I, p1.slerp(p2, 2), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.MINUS_J, p1.slerp(p2, -1), TEST_EPS);
    }

    @Test
    public void testSlerp_alongMeridian() {
        // arrange
        Point2S p1 = Point2S.PLUS_J;
        Point2S p2 = Point2S.PLUS_K;

        // act/assert
        SphericalTestUtils.assertPointsEq(p1, p1.slerp(p2, 0), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(PlaneAngleRadians.PI_OVER_TWO, 0.75 * PlaneAngleRadians.PI_OVER_TWO), p1.slerp(p2, 0.25), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(PlaneAngleRadians.PI_OVER_TWO, 0.5 * PlaneAngleRadians.PI_OVER_TWO), p1.slerp(p2, 0.5), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(PlaneAngleRadians.PI_OVER_TWO, 0.25 * PlaneAngleRadians.PI_OVER_TWO), p1.slerp(p2, 0.75), TEST_EPS);
        SphericalTestUtils.assertPointsEq(p2, p1.slerp(p2, 1), TEST_EPS);

        SphericalTestUtils.assertPointsEq(p2, p2.slerp(p1, 0), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(PlaneAngleRadians.PI_OVER_TWO, 0.25 * PlaneAngleRadians.PI_OVER_TWO), p2.slerp(p1, 0.25), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(PlaneAngleRadians.PI_OVER_TWO, 0.5 * PlaneAngleRadians.PI_OVER_TWO), p2.slerp(p1, 0.5), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(PlaneAngleRadians.PI_OVER_TWO, 0.75 * PlaneAngleRadians.PI_OVER_TWO), p2.slerp(p1, 0.75), TEST_EPS);
        SphericalTestUtils.assertPointsEq(p1, p2.slerp(p1, 1), TEST_EPS);

        SphericalTestUtils.assertPointsEq(Point2S.MINUS_J, p1.slerp(p2, 2), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.MINUS_K, p1.slerp(p2, -1), TEST_EPS);
    }

    @Test
    public void testSlerp_samePoint() {
        // arrange
        Point2S p1 = Point2S.PLUS_I;

        // act/assert
        SphericalTestUtils.assertPointsEq(p1, p1.slerp(p1, 0), TEST_EPS);
        SphericalTestUtils.assertPointsEq(p1, p1.slerp(p1, 0.5), TEST_EPS);
        SphericalTestUtils.assertPointsEq(p1, p1.slerp(p1, 1), TEST_EPS);
    }

    @Test
    public void testSlerp_antipodal() {
        // arrange
        Point2S p1 = Point2S.PLUS_I;
        Point2S p2 = Point2S.MINUS_I;

        // act/assert
        SphericalTestUtils.assertPointsEq(p1, p1.slerp(p1, 0), TEST_EPS);
        SphericalTestUtils.assertPointsEq(p1, p1.slerp(p1, 1), TEST_EPS);

        Point2S pt = p1.slerp(p2, 0.5);
        Assert.assertEquals(p1.distance(pt), p2.distance(pt), TEST_EPS);
    }

    @Test
    public void testAntipodal() {
        for (double az = -6 * PlaneAngleRadians.PI; az <= 6 * PlaneAngleRadians.PI; az += 0.1) {
            for (double p = 0; p <= PlaneAngleRadians.PI; p += 0.1) {
                // arrange
                Point2S pt = Point2S.of(az, p);

                // act
                Point2S result = pt.antipodal();

                // assert
                Assert.assertEquals(PlaneAngleRadians.PI, pt.distance(result), TEST_EPS);

                // check that the azimuth and polar components of the point are correct by creating a
                // new point and checking the distance
                Assert.assertEquals(PlaneAngleRadians.PI,
                        Point2S.of(result.getAzimuth(), result.getPolar()).distance(pt), TEST_EPS);

                // check that the vectors point in opposite directions
                Assert.assertEquals(-1, pt.getVector().dot(result.getVector()), TEST_EPS);
            }
        }
    }

    @Test
    public void testAntipodal_numericalStability() {
        // arrange
        double eps = 1e-16;
        Point2S pt = Point2S.of(1, 2);

        // act
        Point2S result = pt.antipodal().antipodal();

        // assert
        Assert.assertEquals(1.0, result.getAzimuth(), eps);
        Assert.assertEquals(2.0, result.getPolar(), eps);
    }

    @Test
    public void testDimension() {
        // arrange
        Point2S pt = Point2S.of(1, 2);

        // act/assert
        Assert.assertEquals(2, pt.getDimension());
    }

    @Test
    public void testEq() {
        // arrange
        DoublePrecisionContext smallEps = new EpsilonDoublePrecisionContext(1e-5);
        DoublePrecisionContext largeEps = new EpsilonDoublePrecisionContext(5e-1);

        Point2S a = Point2S.of(1.0, 2.0);
        Point2S b = Point2S.of(1.0, 2.01);
        Point2S c = Point2S.of(1.01, 2.0);
        Point2S d = Point2S.of(1.0, 2.0);
        Point2S e = Point2S.of(3.0, 2.0);

        // act/assert
        Assert.assertTrue(a.eq(a, smallEps));
        Assert.assertFalse(a.eq(b, smallEps));
        Assert.assertFalse(a.eq(c, smallEps));
        Assert.assertTrue(a.eq(d, smallEps));
        Assert.assertFalse(a.eq(e, smallEps));

        Assert.assertTrue(a.eq(a, largeEps));
        Assert.assertTrue(a.eq(b, largeEps));
        Assert.assertTrue(a.eq(c, largeEps));
        Assert.assertTrue(a.eq(d, largeEps));
        Assert.assertFalse(a.eq(e, largeEps));
    }

    @Test
    public void testHashCode() {
        // arrange
        Point2S a = Point2S.of(1.0, 2.0);
        Point2S b = Point2S.of(1.0, 3.0);
        Point2S c = Point2S.of(4.0, 2.0);
        Point2S d = Point2S.of(1.0, 2.0);

        // act
        int hash = a.hashCode();

        // assert
        Assert.assertEquals(hash, a.hashCode());

        Assert.assertNotEquals(hash, b.hashCode());
        Assert.assertNotEquals(hash, c.hashCode());

        Assert.assertEquals(hash, d.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        Point2S a = Point2S.of(1.0, 2.0);
        Point2S b = Point2S.of(1.0, 3.0);
        Point2S c = Point2S.of(4.0, 2.0);
        Point2S d = Point2S.of(1.0, 2.0);

        // act/assert
        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        Assert.assertEquals(a, a);

        Assert.assertNotEquals(a, b);
        Assert.assertNotEquals(a, c);

        Assert.assertEquals(a, d);
        Assert.assertEquals(d, a);
    }

    @Test
    public void testEquals_poles() {
        // arrange
        Point2S a = Point2S.of(1.0, 0.0);
        Point2S b = Point2S.of(0.0, 0.0);
        Point2S c = Point2S.of(1.0, 0.0);

        Point2S d = Point2S.of(-1.0, PlaneAngleRadians.PI);
        Point2S e = Point2S.of(0.0, PlaneAngleRadians.PI);
        Point2S f = Point2S.of(-1.0, PlaneAngleRadians.PI);

        // act/assert
        Assert.assertEquals(a, a);
        Assert.assertNotEquals(a, b);
        Assert.assertEquals(a, c);

        Assert.assertEquals(d, d);
        Assert.assertNotEquals(d, e);
        Assert.assertEquals(d, f);
    }

    @Test
    public void testToString() {
        // act/assert
        Assert.assertEquals("(0.0, 0.0)", Point2S.of(0.0, 0.0).toString());
        Assert.assertEquals("(1.0, 2.0)", Point2S.of(1.0, 2.0).toString());
    }

    @Test
    public void testParse() {
        // act/assert
        checkPoint(Point2S.parse("(0,0)"), 0.0, 0.0);
        checkPoint(Point2S.parse("(1,2)"), 1.0, 2.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_failure() {
        // act/assert
        Point2S.parse("abc");
    }

    private static void checkPoint(Point2S p, double az, double polar) {
        String msg = "Expected (" + az + "," + polar + ") but was " + p;

        Assert.assertEquals(msg, az, p.getAzimuth(), TEST_EPS);
        Assert.assertEquals(msg, polar, p.getPolar(), TEST_EPS);
    }
}
