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
package org.apache.commons.geometry.euclidean.threed;

import java.util.regex.Pattern;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.numbers.core.Precision;
import org.junit.Assert;
import org.junit.Test;

public class Point3DTest {

    private static final double EPS = 1e-15;

    @Test
    public void testConstants() {
        // act/assert
        checkPoint(Point3D.ZERO, 0, 0, 0);
        checkPoint(Point3D.NaN, Double.NaN, Double.NaN, Double.NaN);
        checkPoint(Point3D.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        checkPoint(Point3D.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testAsVector() {
        // act/assert
        checkVector(Point3D.of(1, 2, 3).asVector(), 1, 2, 3);
        checkVector(Point3D.of(-1, -2, -3).asVector(), -1, -2, -3);
        checkVector(Point3D.of(Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY).asVector(),
                Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testDistance() {
        // act/assert
        Point3D p1 = Point3D.of(1, 2, 3);
        Point3D p2 = Point3D.of(4, 5, 6);
        Point3D p3 = Point3D.of(-7, -8, -9);

        // act/assert
        Assert.assertEquals(0,  p1.distance(p1), EPS);
        Assert.assertEquals(0,  p2.distance(p2), EPS);
        Assert.assertEquals(0,  p3.distance(p3), EPS);

        Assert.assertEquals(Math.sqrt(27), p1.distance(p2), EPS);
        Assert.assertEquals(Math.sqrt(27), p2.distance(p1), EPS);

        Assert.assertEquals(Math.sqrt(308), p1.distance(p3), EPS);
        Assert.assertEquals(Math.sqrt(308), p3.distance(p1), EPS);
    }

    @Test
    public void testSubtract() {
        // act/assert
        Point3D p1 = Point3D.of(1, 2, 3);
        Point3D p2 = Point3D.of(4, 5, 6);
        Point3D p3 = Point3D.of(-7, -8, -9);

        // act/assert
        checkVector(p1.subtract(p1), 0, 0, 0);
        checkVector(p2.subtract(p2), 0, 0, 0);
        checkVector(p3.subtract(p3), 0, 0, 0);

        checkVector(p1.subtract(p2), -3, -3, -3);
        checkVector(p2.subtract(p1), 3, 3, 3);

        checkVector(p1.subtract(p3), 8, 10, 12);
        checkVector(p3.subtract(p1), -8, -10,-12);
    }

    @Test
    public void testVectorTo() {
        // act/assert
        Point3D p1 = Point3D.of(1, 2, 3);
        Point3D p2 = Point3D.of(4, 5, 6);
        Point3D p3 = Point3D.of(-7, -8, -9);

        // act/assert
        checkVector(p1.vectorTo(p1), 0, 0, 0);
        checkVector(p2.vectorTo(p2), 0, 0, 0);
        checkVector(p3.vectorTo(p3), 0, 0, 0);

        checkVector(p1.vectorTo(p2), 3, 3, 3);
        checkVector(p2.vectorTo(p1), -3, -3, -3);

        checkVector(p1.vectorTo(p3), -8, -10, -12);
        checkVector(p3.vectorTo(p1), 8, 10, 12);
    }

    @Test
    public void testLerp() {
        // arrange
        Point3D p1 = Point3D.of(1, -5, 2);
        Point3D p2 = Point3D.of(-4, 0, 2);
        Point3D p3 = Point3D.of(10, -4, 0);

        // act/assert
        checkPoint(p1.lerp(p1, 0), 1, -5, 2);
        checkPoint(p1.lerp(p1, 1), 1, -5, 2);

        checkPoint(p1.lerp(p2, -0.25), 2.25, -6.25, 2);
        checkPoint(p1.lerp(p2, 0), 1, -5, 2);
        checkPoint(p1.lerp(p2, 0.25), -0.25, -3.75, 2);
        checkPoint(p1.lerp(p2, 0.5), -1.5, -2.5, 2);
        checkPoint(p1.lerp(p2, 0.75), -2.75, -1.25, 2);
        checkPoint(p1.lerp(p2, 1), -4, 0, 2);
        checkPoint(p1.lerp(p2, 1.25), -5.25, 1.25, 2);

        checkPoint(p1.lerp(p3, 0), 1, -5, 2);
        checkPoint(p1.lerp(p3, 0.25), 3.25, -4.75, 1.5);
        checkPoint(p1.lerp(p3, 0.5), 5.5, -4.5, 1);
        checkPoint(p1.lerp(p3, 0.75), 7.75, -4.25, 0.5);
        checkPoint(p1.lerp(p3, 1), 10, -4, 0);
    }

    @Test
    public void testLerp_static() {
        // arrange
        Point3D p1 = Point3D.of(1, -5, 2);
        Point3D p2 = Point3D.of(-4, 0, 2);
        Point3D p3 = Point3D.of(10, -4, 0);

        // act/assert
        checkPoint(Point3D.lerp(p1, p1, 0), 1, -5, 2);
        checkPoint(Point3D.lerp(p1, p1, 1), 1, -5, 2);

        checkPoint(Point3D.lerp(p1, p2, -0.25), 2.25, -6.25, 2);
        checkPoint(Point3D.lerp(p1, p2, 0), 1, -5, 2);
        checkPoint(Point3D.lerp(p1, p2, 0.25), -0.25, -3.75, 2);
        checkPoint(Point3D.lerp(p1, p2, 0.5), -1.5, -2.5, 2);
        checkPoint(Point3D.lerp(p1, p2, 0.75), -2.75, -1.25, 2);
        checkPoint(Point3D.lerp(p1, p2, 1), -4, 0, 2);
        checkPoint(Point3D.lerp(p1, p2, 1.25), -5.25, 1.25, 2);

        checkPoint(Point3D.lerp(p1, p3, 0), 1, -5, 2);
        checkPoint(Point3D.lerp(p1, p3, 0.25), 3.25, -4.75, 1.5);
        checkPoint(Point3D.lerp(p1, p3, 0.5), 5.5, -4.5, 1);
        checkPoint(Point3D.lerp(p1, p3, 0.75), 7.75, -4.25, 0.5);
        checkPoint(Point3D.lerp(p1, p3, 1), 10, -4, 0);
    }

    @Test
    public void testAdd() {
        // act/assert
        Point3D p1 = Point3D.of(1, 2, 3);
        Point3D p2 = Point3D.of(-4, -5, -6);

        // act/assert
        checkPoint(p1.add(Vector3D.ZERO), 1, 2, 3);
        checkPoint(p1.add(Vector3D.of(4, 5, 6)), 5, 7, 9);
        checkPoint(p1.add(Vector3D.of(-4, -5, -6)), -3, -3, -3);

        checkPoint(p2.add(Vector3D.ZERO), -4, -5, -6);
        checkPoint(p2.add(Vector3D.of(1, 0, 0)), -3, -5, -6);
        checkPoint(p2.add(Vector3D.of(0, -1, 0)), -4, -6, -6);
        checkPoint(p2.add(Vector3D.of(0, 0, 1)), -4, -5, -5);
    }

    @Test
    public void testHashCode() {
        // arrange
        double delta = 10 * Precision.EPSILON;

        Point3D u = Point3D.of(1, 1, 1);
        Point3D v = Point3D.of(1 + delta, 1 + delta, 1 + delta);
        Point3D w = Point3D.of(1, 1, 1);

        // act/assert
        Assert.assertTrue(u.hashCode() != v.hashCode());
        Assert.assertEquals(u.hashCode(), w.hashCode());

        Assert.assertEquals(Point3D.of(0, 0, Double.NaN).hashCode(), Point3D.NaN.hashCode());
        Assert.assertEquals(Point3D.of(0, Double.NaN, 0).hashCode(), Point3D.NaN.hashCode());
        Assert.assertEquals(Point3D.of(Double.NaN, 0, 0).hashCode(), Point3D.NaN.hashCode());
        Assert.assertEquals(Point3D.of(0, Double.NaN, 0).hashCode(), Point3D.of(Double.NaN, 0, 0).hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        double delta = 10 * Precision.EPSILON;

        Point3D u1 = Point3D.of(1, 2, 3);
        Point3D u2 = Point3D.of(1, 2, 3);

        // act/assert
        Assert.assertFalse(u1.equals(null));
        Assert.assertFalse(u1.equals(new Object()));

        Assert.assertTrue(u1.equals(u1));
        Assert.assertTrue(u1.equals(u2));

        Assert.assertFalse(u1.equals(Point3D.of(-1, -2, -3)));
        Assert.assertFalse(u1.equals(Point3D.of(1 + delta, 2, 3)));
        Assert.assertFalse(u1.equals(Point3D.of(1, 2 + delta, 3)));
        Assert.assertFalse(u1.equals(Point3D.of(1, 2, 3 + delta)));

        Assert.assertTrue(Point3D.of(Double.NaN, 0, 0).equals(Point3D.of(0, Double.NaN, 0)));
        Assert.assertTrue(Point3D.of(0, 0, Double.NaN).equals(Point3D.of(Double.NaN, 0, 0)));

        Assert.assertTrue(Point3D.of(0, 0, Double.NEGATIVE_INFINITY).equals(Point3D.of(0, 0, Double.NEGATIVE_INFINITY)));
        Assert.assertFalse(Point3D.of(0, 0, Double.NEGATIVE_INFINITY).equals(Point3D.of(0, Double.NEGATIVE_INFINITY, 0)));
        Assert.assertFalse(Point3D.of(0, 0, Double.NEGATIVE_INFINITY).equals(Point3D.of(Double.NEGATIVE_INFINITY, 0, 0)));

        Assert.assertTrue(Point3D.of(0, 0, Double.POSITIVE_INFINITY).equals(Point3D.of(0, 0, Double.POSITIVE_INFINITY)));
        Assert.assertFalse(Point3D.of(0, 0, Double.POSITIVE_INFINITY).equals(Point3D.of(0, Double.POSITIVE_INFINITY, 0)));
        Assert.assertFalse(Point3D.of(0, 0, Double.POSITIVE_INFINITY).equals(Point3D.of(Double.POSITIVE_INFINITY, 0, 0)));
    }

    @Test
    public void testToString() {
        // arrange
        Point3D p = Point3D.of(1, 2, 3);
        Pattern pattern = Pattern.compile("\\(1.{0,2}, 2.{0,2}, 3.{0,2}\\)");

        // act
        String str = p.toString();

        // assert
        Assert.assertTrue("Expected string " + str + " to match regex " + pattern,
                    pattern.matcher(str).matches());
    }

    @Test
    public void testParse() {
        // act/assert
        checkPoint(Point3D.parse("(1, 2, 0)"), 1, 2, 0);
        checkPoint(Point3D.parse("(-1, -2, 0)"), -1, -2, 0);

        checkPoint(Point3D.parse("(0.01, -1e-3, 1e3)"), 1e-2, -1e-3, 1e3);

        checkPoint(Point3D.parse("(NaN, -Infinity, Infinity)"), Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        checkPoint(Point3D.parse(Point3D.ZERO.toString()), 0, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_failure() {
        // act/assert
        Point3D.parse("abc");
    }

    @Test
    public void testOf() {
        // act/assert
        checkPoint(Point3D.of(1, 2, 3), 1, 2, 3);
        checkPoint(Point3D.of(-1, -2, -3), -1, -2, -3);
        checkPoint(Point3D.of(Math.PI, Double.NaN, Double.POSITIVE_INFINITY),
                Math.PI, Double.NaN, Double.POSITIVE_INFINITY);
        checkPoint(Point3D.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Math.E),
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Math.E);
    }

    @Test
    public void testOf_coordinateArg() {
        // act/assert
        checkPoint(Point3D.of(Vector3D.of(1, 2, 3)), 1, 2, 3);
        checkPoint(Point3D.of(Vector3D.of(-1, -2, -3)), -1, -2, -3);
        checkPoint(Point3D.of(Vector3D.of(Math.PI, Double.NaN, Double.POSITIVE_INFINITY)),
                Math.PI, Double.NaN, Double.POSITIVE_INFINITY);
        checkPoint(Point3D.of(Vector3D.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Math.E)),
                   Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Math.E);
    }

    @Test
    public void testOf_arrayArg() {
        // act/assert
        checkPoint(Point3D.of(new double[] { 1, 2, 3 }), 1, 2, 3);
        checkPoint(Point3D.of(new double[] { -1, -2, -3 }), -1, -2, -3);
        checkPoint(Point3D.of(new double[] { Math.PI, Double.NaN, Double.POSITIVE_INFINITY }),
                Math.PI, Double.NaN, Double.POSITIVE_INFINITY);
        checkPoint(Point3D.of(new double[] { Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Math.E}),
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Math.E);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOf_arrayArg_invalidDimensions() {
        // act/assert
        Point3D.of(new double[] { 0.0, 0.0 });
    }

    @Test
    public void testOfSpherical() {
        // arrange
        double sqrt3 = Math.sqrt(3);

        // act/assert
        checkPoint(Point3D.ofSpherical(0, 0, 0), 0, 0, 0);

        checkPoint(Point3D.ofSpherical(1, 0, Geometry.HALF_PI), 1, 0, 0);
        checkPoint(Point3D.ofSpherical(1, Geometry.PI, Geometry.HALF_PI), -1, 0, 0);

        checkPoint(Point3D.ofSpherical(2, Geometry.HALF_PI, Geometry.HALF_PI), 0, 2, 0);
        checkPoint(Point3D.ofSpherical(2, Geometry.MINUS_HALF_PI, Geometry.HALF_PI), 0, -2, 0);

        checkPoint(Point3D.ofSpherical(3, 0, 0), 0, 0, 3);
        checkPoint(Point3D.ofSpherical(3, 0, Geometry.PI), 0, 0, -3);

        checkPoint(Point3D.ofSpherical(sqrt3, 0.25 * Geometry.PI, Math.acos(1 / sqrt3)), 1, 1, 1);
        checkPoint(Point3D.ofSpherical(sqrt3, -0.75 * Geometry.PI, Math.acos(-1 / sqrt3)), -1, -1, -1);
    }

    @Test
    public void testVectorCombination1() {
        // arrange
        Point3D p1 = Point3D.of(1, 2, 3);

        // act/assert
        checkPoint(Point3D.vectorCombination(0, p1), 0, 0, 0);

        checkPoint(Point3D.vectorCombination(1, p1), 1, 2, 3);
        checkPoint(Point3D.vectorCombination(-1, p1), -1, -2, -3);

        checkPoint(Point3D.vectorCombination(0.5, p1), 0.5, 1, 1.5);
        checkPoint(Point3D.vectorCombination(-0.5, p1), -0.5, -1, -1.5);
    }

    @Test
    public void testVectorCombination2() {
        // arrange
        Point3D p1 = Point3D.of(1, 2, 3);
        Point3D p2 = Point3D.of(-3, -4, -5);

        // act/assert
        checkPoint(Point3D.vectorCombination(2, p1, -3, p2), 11, 16, 21);
        checkPoint(Point3D.vectorCombination(-3, p1, 2, p2), -9, -14, -19);
    }

    @Test
    public void testVectorCombination3() {
        // arrange
        Point3D p1 = Point3D.of(1, 2, 3);
        Point3D p2 = Point3D.of(-3, -4, -5);
        Point3D p3 = Point3D.of(5, 6, 7);

        // act/assert
        checkPoint(Point3D.vectorCombination(2, p1, -3, p2, 4, p3), 31, 40, 49);
        checkPoint(Point3D.vectorCombination(-3, p1, 2, p2, -4, p3), -29, -38, -47);
    }

    @Test
    public void testVectorCombination4() {
        // arrange
        Point3D p1 = Point3D.of(1, 2, 3);
        Point3D p2 = Point3D.of(-3, -4, -5);
        Point3D p3 = Point3D.of(5, 6, 7);
        Point3D p4 = Point3D.of(-7, -8, 9);

        // act/assert
        checkPoint(Point3D.vectorCombination(2, p1, -3, p2, 4, p3, -5, p4), 66, 80, 4);
        checkPoint(Point3D.vectorCombination(-3, p1, 2, p2, -4, p3, 5, p4), -64, -78, -2);
    }

    private void checkVector(Vector3D v, double x, double y, double z) {
        Assert.assertEquals(x, v.getX(), EPS);
        Assert.assertEquals(y, v.getY(), EPS);
        Assert.assertEquals(z, v.getZ(), EPS);
    }

    private void checkPoint(Point3D p, double x, double y, double z) {
        Assert.assertEquals(x, p.getX(), EPS);
        Assert.assertEquals(y, p.getY(), EPS);
        Assert.assertEquals(z, p.getZ(), EPS);
    }
}
