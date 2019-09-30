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


import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.junit.Assert;
import org.junit.Test;

public class Point2STest {

    private static final double EPS = 1e-10;

    @Test
    public void testProperties() {
        for (int k = -2; k < 3; ++k) {
            // arrange
            Point2S p = Point2S.of(1.0 + k * Geometry.TWO_PI, 1.4);

            // act/assert
            Assert.assertEquals(1.0, p.getAzimuth(), EPS);
            Assert.assertEquals(1.4, p.getPolar(), EPS);

            Assert.assertEquals(Math.cos(1.0) * Math.sin(1.4), p.getVector().getX(), EPS);
            Assert.assertEquals(Math.sin(1.0) * Math.sin(1.4), p.getVector().getY(), EPS);
            Assert.assertEquals(Math.cos(1.4), p.getVector().getZ(), EPS);

            Assert.assertFalse(p.isNaN());
        }
    }

    @Test
    public void testFrom_vector() {
        // arrange
        double quarterPi = 0.25 * Geometry.PI;

        // act/assert
        checkPoint(Point2S.from(Vector3D.of(1, 1, 0)), quarterPi, Geometry.HALF_PI);
        checkPoint(Point2S.from(Vector3D.of(1, 0, 1)), 0, quarterPi);
        checkPoint(Point2S.from(Vector3D.of(0, 1, 1)), Geometry.HALF_PI, quarterPi);

        checkPoint(Point2S.from(Vector3D.of(1, -1, 0)), Geometry.TWO_PI - quarterPi, Geometry.HALF_PI);
        checkPoint(Point2S.from(Vector3D.of(-1, 0, -1)), Geometry.PI, Geometry.PI - quarterPi);
        checkPoint(Point2S.from(Vector3D.of(0, -1, -1)), Geometry.TWO_PI - Geometry.HALF_PI, Geometry.PI - quarterPi);
    }

    @Test
    public void testNaN() {
        // act/assert
        Assert.assertTrue(Point2S.NaN.isNaN());
        Assert.assertTrue(Point2S.NaN.equals(Point2S.of(Double.NaN, 1.0)));
        Assert.assertFalse(Point2S.of(1.0, 1.3).equals(Point2S.NaN));

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
        Point2S a = Point2S.of(1.0, 0.5 * Math.PI);
        Point2S b = Point2S.of(a.getAzimuth() + 0.5 * Math.PI, a.getPolar());

        // act/assert
        Assert.assertEquals(0.5 * Math.PI, a.distance(b), 1.0e-10);
        Assert.assertEquals(Math.PI, a.distance(a.negate()), 1.0e-10);
        Assert.assertEquals(0.5 * Math.PI, Point2S.MINUS_I.distance(Point2S.MINUS_K), 1.0e-10);
        Assert.assertEquals(0.0, Point2S.of(1.0, 0).distance(Point2S.of(2.0, 0)), 1.0e-10);
    }

    @Test
    public void testDimension() {
        // arrange
        Point2S pt = Point2S.of(1, 2);

        // act/assert
        Assert.assertEquals(2, pt.getDimension());
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

        Assert.assertTrue(a.equals(a));

        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(a.equals(c));

        Assert.assertTrue(a.equals(d));
        Assert.assertTrue(d.equals(a));
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

        Assert.assertEquals(msg, az, p.getAzimuth(), EPS);
        Assert.assertEquals(msg, polar, p.getPolar(), EPS);
    }
}
