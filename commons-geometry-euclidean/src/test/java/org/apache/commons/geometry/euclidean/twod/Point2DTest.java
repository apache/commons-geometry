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
package org.apache.commons.geometry.euclidean.twod;

import java.util.regex.Pattern;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.util.Coordinates;
import org.apache.commons.numbers.core.Precision;
import org.junit.Assert;
import org.junit.Test;

public class Point2DTest {

    private static final double EPS = Math.ulp(1d);

    @Test
    public void testConstants() {
        // act/assert
        checkPoint(Point2D.ZERO, 0.0, 0.0);
        checkPoint(Point2D.NaN, Double.NaN, Double.NaN);
        checkPoint(Point2D.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        checkPoint(Point2D.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    @Test
    public void testAsVector() {
        // act/assert
        checkVector(Point2D.of(1, 2).asVector(), 1, 2);
        checkVector(Point2D.of(-1, -2).asVector(), -1, -2);
        checkVector(Point2D.of(Double.NaN, Double.POSITIVE_INFINITY).asVector(), Double.NaN, Double.POSITIVE_INFINITY);
        checkVector(Point2D.of(Double.NEGATIVE_INFINITY, Double.NaN).asVector(), Double.NEGATIVE_INFINITY, Double.NaN);
    }

    @Test
    public void testDistance() {
        // arrange
        Point2D p1 = Point2D.of(1, 1);
        Point2D p2 = Point2D.of(4, 5);
        Point2D p3 = Point2D.of(-1, 0);

        // act/assert
        Assert.assertEquals(0, p1.distance(p1), EPS);
        Assert.assertEquals(5, p1.distance(p2), EPS);
        Assert.assertEquals(5, p2.distance(p1), EPS);

        Assert.assertEquals(Math.sqrt(5), p1.distance(p3), EPS);
        Assert.assertEquals(Math.sqrt(5), p3.distance(p1), EPS);
    }

    @Test
    public void testSubtract() {
        // arrange
        Point2D p1 = Point2D.of(1, 1);
        Point2D p2 = Point2D.of(4, 5);
        Point2D p3 = Point2D.of(-1, 0);

        // act/assert
        checkVector(p1.subtract(p1), 0, 0);
        checkVector(p1.subtract(p2), -3, -4);
        checkVector(p2.subtract(p1), 3, 4);

        checkVector(p1.subtract(p3), 2, 1);
        checkVector(p3.subtract(p1), -2, -1);
    }

    @Test
    public void testVectorTo() {
        // arrange
        Point2D p1 = Point2D.of(1, 1);
        Point2D p2 = Point2D.of(4, 5);
        Point2D p3 = Point2D.of(-1, 0);

        // act/assert
        checkVector(p1.vectorTo(p1), 0, 0);
        checkVector(p1.vectorTo(p2), 3, 4);
        checkVector(p2.vectorTo(p1), -3, -4);

        checkVector(p1.vectorTo(p3), -2, -1);
        checkVector(p3.vectorTo(p1), 2, 1);
    }

    @Test
    public void testAdd() {
        // arrange
        Point2D p1 = Point2D.of(1, 1);
        Point2D p2 = Point2D.of(-4, -5);

        // act/assert
        checkPoint(p1.add(Vector2D.ZERO), 1, 1);
        checkPoint(p1.add(Vector2D.of(0, 1)), 1, 2);
        checkPoint(p1.add(Vector2D.of(1, 0)), 2, 1);
        checkPoint(p1.add(Vector2D.of(0, -1)), 1, 0);
        checkPoint(p1.add(Vector2D.of(-1, 0)), 0, 1);

        checkPoint(p2.add(Vector2D.ZERO), -4, -5);
        checkPoint(p2.add(Vector2D.of(1, 1)), -3, -4);
        checkPoint(p2.add(Vector2D.of(-1, -1)), -5, -6);
    }

    @Test
    public void testHashCode() {
        // arrange
        Point2D u = Point2D.of(1, 1);
        Point2D v = Point2D.of(1 + 10 * Precision.EPSILON, 1 + 10 * Precision.EPSILON);
        Point2D w = Point2D.of(1, 1);

        // act/assert
        Assert.assertTrue(u.hashCode() != v.hashCode());
        Assert.assertEquals(u.hashCode(), w.hashCode());

        Assert.assertEquals(Point2D.of(0, Double.NaN).hashCode(), Point2D.NaN.hashCode());
        Assert.assertEquals(Point2D.of(Double.NaN, 0).hashCode(), Point2D.NaN.hashCode());
        Assert.assertEquals(Point2D.of(0, Double.NaN).hashCode(), Point2D.of(Double.NaN, 0).hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        Point2D u1 = Point2D.of(1, 2);
        Point2D u2 = Point2D.of(1, 2);

        // act/assert
        Assert.assertFalse(u1.equals(null));
        Assert.assertFalse(u1.equals(new Object()));

        Assert.assertTrue(u1.equals(u1));
        Assert.assertTrue(u1.equals(u2));

        Assert.assertFalse(u1.equals(Point2D.of(-1, -2)));
        Assert.assertFalse(u1.equals(Point2D.of(1 + 10 * Precision.EPSILON, 2)));
        Assert.assertFalse(u1.equals(Point2D.of(1, 2 + 10 * Precision.EPSILON)));

        Assert.assertTrue(Point2D.of(0, Double.NaN).equals(Point2D.of(Double.NaN, 0)));

        Assert.assertTrue(Point2D.of(0, Double.POSITIVE_INFINITY).equals(Point2D.of(0, Double.POSITIVE_INFINITY)));
        Assert.assertFalse(Point2D.of(Double.POSITIVE_INFINITY, 0).equals(Point2D.of(0, Double.POSITIVE_INFINITY)));

        Assert.assertTrue(Point2D.of(Double.NEGATIVE_INFINITY, 0).equals(Point2D.of(Double.NEGATIVE_INFINITY, 0)));
        Assert.assertFalse(Point2D.of(0, Double.NEGATIVE_INFINITY).equals(Point2D.of(Double.NEGATIVE_INFINITY, 0)));
    }

    @Test
    public void testToString() {
        // arrange
        Point2D p = Point2D.of(1, 2);
        Pattern pattern = Pattern.compile("\\(1.{0,2}, 2.{0,2}\\)");

        // act
        String str = p.toString();

        // assert
        Assert.assertTrue("Expected string " + str + " to match regex " + pattern,
                    pattern.matcher(str).matches());
    }

    @Test
    public void testParse() {
        // act/assert
        checkPoint(Point2D.parse("(1, 2)"), 1, 2);
        checkPoint(Point2D.parse("(-1, -2)"), -1, -2);

        checkPoint(Point2D.parse("(0.01, -1e-3)"), 1e-2, -1e-3);

        checkPoint(Point2D.parse("(NaN, -Infinity)"), Double.NaN, Double.NEGATIVE_INFINITY);

        checkPoint(Point2D.parse(Point2D.ZERO.toString()), 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_failure() {
        // act/assert
        Point2D.parse("abc");
    }

    @Test
    public void testOf() {
        // act/assert
        checkPoint(Point2D.of(0, 1), 0, 1);
        checkPoint(Point2D.of(-1, -2), -1, -2);
        checkPoint(Point2D.of(Math.PI, Double.NaN), Math.PI, Double.NaN);
        checkPoint(Point2D.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    @Test
    public void testOf_coordinateArg() {
        // act/assert
        checkPoint(Point2D.of(Vector2D.of(0, 1)), 0, 1);
        checkPoint(Point2D.of(Vector2D.of(-1, -2)), -1, -2);
        checkPoint(Point2D.of(Vector2D.of(Math.PI, Double.NaN)), Math.PI, Double.NaN);
        checkPoint(Point2D.of(Vector2D.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)), Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    @Test
    public void testOf_arrayArg() {
        // act/assert
        checkPoint(Point2D.of(new double[] { 0, 1 }), 0, 1);
        checkPoint(Point2D.of(new double[] { -1, -2 }), -1, -2);
        checkPoint(Point2D.of(new double[] { Math.PI, Double.NaN }), Math.PI, Double.NaN);
        checkPoint(Point2D.of(new double[] { Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY }), Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOf_arrayArg_invalidDimensions() {
        // act/assert
        Point2D.of(new double[] {0.0 });
    }

    @Test
    public void testOfPolar() {
        // arrange
        double eps = 1e-15;
        double sqrt2 = Math.sqrt(2.0);

        // act/assert
        checkPoint(Point2D.ofPolar(0, 0), 0, 0, eps);
        checkPoint(Point2D.ofPolar(1, 0), 1, 0, eps);

        checkPoint(Point2D.ofPolar(2, Geometry.PI), -2, 0, eps);
        checkPoint(Point2D.ofPolar(-2, Geometry.PI), 2, 0, eps);

        checkPoint(Point2D.ofPolar(2, Geometry.HALF_PI), 0, 2, eps);
        checkPoint(Point2D.ofPolar(-2, Geometry.HALF_PI), 0, -2, eps);

        checkPoint(Point2D.ofPolar(2, 0.25 * Geometry.PI), sqrt2, sqrt2, eps);
        checkPoint(Point2D.ofPolar(2, 0.75 * Geometry.PI), -sqrt2, sqrt2, eps);
        checkPoint(Point2D.ofPolar(2, -0.25 * Geometry.PI), sqrt2, - sqrt2, eps);
        checkPoint(Point2D.ofPolar(2, -0.75 * Geometry.PI), -sqrt2, - sqrt2, eps);
    }

    @Test
    public void testGetFactory() {
        // act
        Coordinates.Factory2D<Point2D> factory = Point2D.getFactory();

        // assert
        checkPoint(factory.create(1, 2), 1, 2);
        checkPoint(factory.create(-1, -2), -1, -2);
    }

    @Test
    public void testVectorCombination1() {
        // arrange
        Point2D p1 = Point2D.of(1, 2);

        // act/assert
        checkPoint(Point2D.vectorCombination(0, p1), 0, 0);

        checkPoint(Point2D.vectorCombination(1, p1), 1, 2);
        checkPoint(Point2D.vectorCombination(-1, p1), -1, -2);

        checkPoint(Point2D.vectorCombination(0.5, p1), 0.5, 1);
        checkPoint(Point2D.vectorCombination(-0.5, p1), -0.5, -1);
    }

    @Test
    public void testVectorCombination2() {
        // arrange
        Point2D p1 = Point2D.of(1, 2);
        Point2D p2 = Point2D.of(-3, -4);

        // act/assert
        checkPoint(Point2D.vectorCombination(2, p1, -3, p2), 11, 16);
        checkPoint(Point2D.vectorCombination(-3, p1, 2, p2), -9, -14);
    }

    @Test
    public void testVectorCombination3() {
        // arrange
        Point2D p1 = Point2D.of(1, 2);
        Point2D p2 = Point2D.of(-3, -4);
        Point2D p3 = Point2D.of(5, 6);

        // act/assert
        checkPoint(Point2D.vectorCombination(2, p1, -3, p2, 4, p3), 31, 40);
        checkPoint(Point2D.vectorCombination(-3, p1, 2, p2, -4, p3), -29, -38);
    }

    @Test
    public void testVectorCombination4() {
        // arrange
        Point2D p1 = Point2D.of(1, 2);
        Point2D p2 = Point2D.of(-3, -4);
        Point2D p3 = Point2D.of(5, 6);
        Point2D p4 = Point2D.of(-7, -8);

        // act/assert
        checkPoint(Point2D.vectorCombination(2, p1, -3, p2, 4, p3, -5, p4), 66, 80);
        checkPoint(Point2D.vectorCombination(-3, p1, 2, p2, -4, p3, 5, p4), -64, -78);
    }

    private void checkVector(Vector2D v, double x, double y) {
        Assert.assertEquals(x, v.getX(), EPS);
        Assert.assertEquals(y, v.getY(), EPS);
    }

    private void checkPoint(Point2D p, double x, double y) {
        checkPoint(p, x, y, EPS);
    }

    private void checkPoint(Point2D p, double x, double y, double eps) {
        Assert.assertEquals(x, p.getX(), eps);
        Assert.assertEquals(y, p.getY(), eps);
    }
}
