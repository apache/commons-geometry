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

import java.util.regex.Pattern;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.exception.GeometryException;
import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class GreatCircleTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final Vector3D.Unit X = Vector3D.Unit.PLUS_X;
    private static final Vector3D.Unit Y = Vector3D.Unit.PLUS_Y;
    private static final Vector3D.Unit Z = Vector3D.Unit.PLUS_Z;

    @Test
    public void testFromPole() {
        // act/assert
        checkGreatCircle(GreatCircle.fromPole(X, TEST_PRECISION), X, Z);
        checkGreatCircle(GreatCircle.fromPole(Y, TEST_PRECISION), Y, Z.negate());
        checkGreatCircle(GreatCircle.fromPole(Z, TEST_PRECISION), Z, Y);
    }

    @Test
    public void testFromPoleAndXAxis() {
        // act/assert
        checkGreatCircle(GreatCircle.fromPoleAndXAxis(X, Y, TEST_PRECISION), X, Y);
        checkGreatCircle(GreatCircle.fromPoleAndXAxis(X, Z, TEST_PRECISION), X, Z);
        checkGreatCircle(GreatCircle.fromPoleAndXAxis(Y, Z, TEST_PRECISION), Y, Z);
    }

    @Test
    public void testFromPoints() {
        // act/assert
        checkGreatCircle(GreatCircle.fromPoints(
                    Point2S.of(0, Geometry.HALF_PI),
                    Point2S.of(Geometry.HALF_PI, Geometry.HALF_PI),
                    TEST_PRECISION),
                Z, X);

        checkGreatCircle(GreatCircle.fromPoints(
                Point2S.of(0, Geometry.HALF_PI),
                Point2S.of(-0.1 * Geometry.PI, Geometry.HALF_PI),
                TEST_PRECISION),
            Z.negate(), X);

        checkGreatCircle(GreatCircle.fromPoints(
                Point2S.of(0, Geometry.HALF_PI),
                Point2S.of(1.5 * Geometry.PI, Geometry.HALF_PI),
                TEST_PRECISION),
            Z.negate(), X);

        checkGreatCircle(GreatCircle.fromPoints(
                Point2S.of(0, 0),
                Point2S.of(0, Geometry.HALF_PI),
                TEST_PRECISION),
            Y, Z);
    }

    @Test
    public void testFromPoints_invalidPoints() {
        // arrange
        Point2S p1 = Point2S.of(0, Geometry.HALF_PI);
        Point2S p2 = Point2S.of(Geometry.PI, Geometry.HALF_PI);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            GreatCircle.fromPoints(p1, p1, TEST_PRECISION);
        }, GeometryException.class, Pattern.compile("^.*points are equal$"));

        GeometryTestUtils.assertThrows(() -> {
            GreatCircle.fromPoints(
                    Point2S.fromVector(Vector3D.Unit.PLUS_X),
                    Point2S.fromVector(Vector3D.Unit.MINUS_X),
                    TEST_PRECISION);
        }, GeometryException.class, Pattern.compile("^.*points are antipodal$"));

        GeometryTestUtils.assertThrows(() -> {
            GreatCircle.fromPoints(p1, Point2S.NaN, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            GreatCircle.fromPoints(Point2S.NaN, p2, TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    private static void checkGreatCircle(GreatCircle circle, Vector3D pole, Vector3D x) {
        SphericalTestUtils.assertVectorsEqual(pole, circle.getPole(), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(x, circle.getXAxis(), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(pole.cross(x), circle.getYAxis(), TEST_EPS);

        Point2S plusPolePt = Point2S.fromVector(circle.getPole());
        Point2S minusPolePt = Point2S.fromVector(circle.getPole().negate());
        Point2S origin = Point2S.fromVector(circle.getXAxis());

        Assert.assertFalse(circle.contains(plusPolePt));
        Assert.assertFalse(circle.contains(minusPolePt));
        Assert.assertTrue(circle.contains(origin));

        Assert.assertEquals(HyperplaneLocation.PLUS, circle.classify(plusPolePt));
        Assert.assertEquals(HyperplaneLocation.MINUS, circle.classify(minusPolePt));
        Assert.assertEquals(HyperplaneLocation.ON, circle.classify(origin));
    }
}
