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

package org.apache.commons.geometry.euclidean.oned;

import java.util.regex.Pattern;

import org.apache.commons.numbers.core.Precision;
import org.junit.Assert;
import org.junit.Test;

public class Point1DTest {

    private static final double TEST_TOLERANCE = 1e-15;

    @Test
    public void testConstants() {
        // act/assert
        checkPoint(Point1D.ZERO, 0.0);
        checkPoint(Point1D.ONE, 1.0);
        checkPoint(Point1D.NaN, Double.NaN);
        checkPoint(Point1D.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        checkPoint(Point1D.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testAsVector() {
        // act/assert
        checkVector(Point1D.of(0).asVector(), 0.0);
        checkVector(Point1D.of(1).asVector(), 1.0);
        checkVector(Point1D.of(-1).asVector(), -1.0);
        checkVector(Point1D.NaN.asVector(), Double.NaN);
        checkVector(Point1D.POSITIVE_INFINITY.asVector(), Double.POSITIVE_INFINITY);
        checkVector(Point1D.NEGATIVE_INFINITY.asVector(), Double.NEGATIVE_INFINITY);
    }

    @Test
    public void testDistance() {
        // arrange
        Point1D p1 = Point1D.of(1);
        Point1D p2 = Point1D.of(-4);
        Point1D p3 = Point1D.of(10);

        // act/assert
        Assert.assertEquals(0.0, p1.distance(p1), TEST_TOLERANCE);
        Assert.assertEquals(0.0, p2.distance(p2), TEST_TOLERANCE);
        Assert.assertEquals(0.0, p3.distance(p3), TEST_TOLERANCE);

        Assert.assertEquals(5.0, p1.distance(p2), TEST_TOLERANCE);
        Assert.assertEquals(5.0, p2.distance(p1), TEST_TOLERANCE);

        Assert.assertEquals(9.0, p1.distance(p3), TEST_TOLERANCE);
        Assert.assertEquals(9.0, p3.distance(p1), TEST_TOLERANCE);

        Assert.assertEquals(14.0, p2.distance(p3), TEST_TOLERANCE);
        Assert.assertEquals(14.0, p3.distance(p2), TEST_TOLERANCE);

        Assert.assertEquals(0.0, Point1D.of(-1).distance(Point1D.of(-1)), TEST_TOLERANCE);
    }

    @Test
    public void testSubtract() {
        // arrange
        Point1D p1 = Point1D.of(1);
        Point1D p2 = Point1D.of(-4);
        Point1D p3 = Point1D.of(10);

        // act/assert
        checkVector(p1.subtract(p1), 0.0);
        checkVector(p2.subtract(p2), 0.0);
        checkVector(p3.subtract(p3), 0.0);

        checkVector(p1.subtract(p2), 5.0);
        checkVector(p2.subtract(p1), -5.0);

        checkVector(p1.subtract(p3), -9.0);
        checkVector(p3.subtract(p1), 9.0);

        checkVector(p2.subtract(p3), -14.0);
        checkVector(p3.subtract(p2), 14.0);
    }

    @Test
    public void testVectorTo() {
        // arrange
        Point1D p1 = Point1D.of(1);
        Point1D p2 = Point1D.of(-4);
        Point1D p3 = Point1D.of(10);

        // act/assert
        checkVector(p1.vectorTo(p1), 0.0);
        checkVector(p2.vectorTo(p2), 0.0);
        checkVector(p3.vectorTo(p3), 0.0);

        checkVector(p1.vectorTo(p2), -5.0);
        checkVector(p2.vectorTo(p1), 5.0);

        checkVector(p1.vectorTo(p3), 9.0);
        checkVector(p3.vectorTo(p1), -9.0);

        checkVector(p2.vectorTo(p3), 14.0);
        checkVector(p3.vectorTo(p2), -14.0);
    }

    @Test
    public void testAdd() {
        // arrange
        Point1D p1 = Point1D.of(2.0);
        Point1D p2 = Point1D.of(-2.0);

        // act/assert
        checkPoint(p1.add(Vector1D.ZERO), 2.0);
        checkPoint(p1.add(Vector1D.of(1)), 3.0);
        checkPoint(p1.add(Vector1D.of(-1)), 1.0);

        checkPoint(p2.add(Vector1D.ZERO), -2.0);
        checkPoint(p2.add(Vector1D.of(1)), -1.0);
        checkPoint(p2.add(Vector1D.of(-1)), -3.0);
    }

    @Test
    public void testHashCode() {
        // arrange
        Point1D u = Point1D.of(1);
        Point1D v = Point1D.of(1 + 10 * Precision.EPSILON);
        Point1D w = Point1D.of(1);

        // act/assert
        Assert.assertTrue(u.hashCode() != v.hashCode());
        Assert.assertEquals(u.hashCode(), w.hashCode());

        Assert.assertEquals(Point1D.of(Double.NaN).hashCode(), Point1D.NaN.hashCode());
        Assert.assertEquals(Point1D.of(Double.NaN).hashCode(), Point1D.of(Double.NaN).hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        Point1D u1 = Point1D.of(1);
        Point1D u2 = Point1D.of(1);

        // act/assert
        Assert.assertFalse(u1.equals(null));
        Assert.assertFalse(u1.equals(new Object()));

        Assert.assertTrue(u1.equals(u1));
        Assert.assertTrue(u1.equals(u2));

        Assert.assertFalse(u1.equals(Point1D.of(-1)));
        Assert.assertFalse(u1.equals(Point1D.of(1 + 10 * Precision.EPSILON)));

        Assert.assertTrue(Point1D.of(Double.NaN).equals(Point1D.of(Double.NaN)));
        Assert.assertTrue(Point1D.of(Double.POSITIVE_INFINITY).equals(Point1D.of(Double.POSITIVE_INFINITY)));
        Assert.assertTrue(Point1D.of(Double.NEGATIVE_INFINITY).equals(Point1D.of(Double.NEGATIVE_INFINITY)));
    }

    @Test
    public void testToString() {
        // arrange
        Point1D p = Point1D.of(3);
        Pattern pattern = Pattern.compile("\\(3.{0,2}\\)");

        // act
        String str = p.toString();

        // assert
        Assert.assertTrue("Expected string " + str + " to match regex " + pattern,
                    pattern.matcher(str).matches());
    }

    @Test
    public void testOf() {
        // act/assert
        checkPoint(Point1D.of(0), 0.0);
        checkPoint(Point1D.of(-1), -1.0);
        checkPoint(Point1D.of(1), 1.0);
        checkPoint(Point1D.of(Math.PI), Math.PI);
        checkPoint(Point1D.of(Double.NaN), Double.NaN);
        checkPoint(Point1D.of(Double.NEGATIVE_INFINITY), Double.NEGATIVE_INFINITY);
        checkPoint(Point1D.of(Double.POSITIVE_INFINITY), Double.POSITIVE_INFINITY);
    }

    @Test
    public void testOf_coordinateArg() {
        // act/assert
        checkPoint(Point1D.of(Vector1D.of(0)), 0.0);
        checkPoint(Point1D.of(Vector1D.of(-1)), -1.0);
        checkPoint(Point1D.of(Vector1D.of(1)), 1.0);
        checkPoint(Point1D.of(Vector1D.of(Math.PI)), Math.PI);
        checkPoint(Point1D.of(Vector1D.of(Double.NaN)), Double.NaN);
        checkPoint(Point1D.of(Vector1D.of(Double.NEGATIVE_INFINITY)), Double.NEGATIVE_INFINITY);
        checkPoint(Point1D.of(Vector1D.of(Double.POSITIVE_INFINITY)), Double.POSITIVE_INFINITY);
    }

    @Test
    public void testVectorCombination() {
        // act/assert
        checkPoint(Point1D.vectorCombination(2, Point1D.of(3)), 6);
        checkPoint(Point1D.vectorCombination(-2, Point1D.of(3)), -6);
    }

    @Test
    public void testVectorCombination2() {
        // act/assert
        checkPoint(Point1D.vectorCombination(
                2, Point1D.of(3),
                5, Point1D.of(7)), 41);
        checkPoint(Point1D.vectorCombination(
                2, Point1D.of(3),
                -5, Point1D.of(7)),-29);
    }

    @Test
    public void testVectorCombination3() {
        // act/assert
        checkPoint(Point1D.vectorCombination(
                2, Point1D.of(3),
                5, Point1D.of(7),
                11, Point1D.of(13)), 184);
        checkPoint(Point1D.vectorCombination(
                2, Point1D.of(3),
                5, Point1D.of(7),
                -11, Point1D.of(13)), -102);
    }

    @Test
    public void testVectorCombination4() {
        // act/assert
        checkPoint(Point1D.vectorCombination(
                2, Point1D.of(3),
                5, Point1D.of(7),
                11, Point1D.of(13),
                17, Point1D.of(19)), 507);
        checkPoint(Point1D.vectorCombination(
                2, Point1D.of(3),
                5, Point1D.of(7),
                11, Point1D.of(13),
                -17, Point1D.of(19)), -139);
    }

    private void checkPoint(Point1D p, double x) {
        Assert.assertEquals(x, p.getX(), TEST_TOLERANCE);
    }

    private void checkVector(Vector1D v, double x) {
        Assert.assertEquals(x, v.getX(), TEST_TOLERANCE);
    }
}
