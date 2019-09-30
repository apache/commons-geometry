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

import java.util.function.Function;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class FunctionTransform2STest {

    private static final double TEST_EPS = 1e-15;

    @Test
    public void testIdentity() {
        // arrange
        Point2S p0 = Point2S.of(0, 0);
        Point2S p1 = Point2S.of(1, 1);
        Point2S p2 = Point2S.of(-1, 2);

        // act
        FunctionTransform2S t = FunctionTransform2S.identity();

        // assert
        Assert.assertTrue(t.preservesOrientation());

        SphericalTestUtils.assertPointsEqual(p0, t.apply(p0), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(p1, t.apply(p1), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(p2, t.apply(p2), TEST_EPS);
    }

    @Test
    public void testFrom_identity() {
        // arrange
        Point2S p0 = Point2S.of(0, 0);
        Point2S p1 = Point2S.of(1, 1);
        Point2S p2 = Point2S.of(-1, 2);

        // act
        FunctionTransform2S t = FunctionTransform2S.from(Function.identity());

        // assert
        Assert.assertTrue(t.preservesOrientation());

        SphericalTestUtils.assertPointsEqual(p0, t.apply(p0), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(p1, t.apply(p1), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(p2, t.apply(p2), TEST_EPS);
    }

    @Test
    public void testFrom_scaleAndTranslate() {
        // arrange
        Point2S p0 = Point2S.of(0, 0);
        Point2S p1 = Point2S.of(1, 1);
        Point2S p2 = Point2S.of(-1, 2);

        // act
        FunctionTransform2S t = FunctionTransform2S.from(p -> {
            double az = (p.getAzimuth() * 2) + 1;
            double polar = p.getPolar() + 1;
            return Point2S.of(az, polar);
        });

        // assert
        Assert.assertTrue(t.preservesOrientation());

        SphericalTestUtils.assertPointsEqual(Point2S.of(1, 1), t.apply(p0), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(3, 2), t.apply(p1), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(-1, 3), t.apply(p2), TEST_EPS);
    }

    @Test
    public void testFrom_negate_azimuth() {
        // arrange
        Point2S p0 = Point2S.of(0, 0);
        Point2S p1 = Point2S.of(1, 1);
        Point2S p2 = Point2S.of(-1, 2);

        // act
        FunctionTransform2S t = FunctionTransform2S.from(p -> {
            double az = -p.getAzimuth();
            double polar = p.getPolar();
            return Point2S.of(az, polar);
        });

        // assert
        Assert.assertFalse(t.preservesOrientation());

        SphericalTestUtils.assertPointsEqual(Point2S.of(0, 0), t.apply(p0), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(-1, 1), t.apply(p1), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(1, 2), t.apply(p2), TEST_EPS);
    }

    @Test
    public void testFrom_negate_polar() {
     // arrange
        Point2S p0 = Point2S.of(0, 0);
        Point2S p1 = Point2S.of(1, 1);
        Point2S p2 = Point2S.of(-1, 2);

        // act
        FunctionTransform2S t = FunctionTransform2S.from(p -> {
            double az = p.getAzimuth();
            double polar = -p.getPolar();
            return Point2S.of(az, polar);
        });

        // assert
        // the polar coordinate is not sensitive to sign, so negating it does not affect the
        // orientation-preservation of the transform
        Assert.assertTrue(t.preservesOrientation());

        SphericalTestUtils.assertPointsEqual(Point2S.of(0, 0), t.apply(p0), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(1, 1), t.apply(p1), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(-1, 2), t.apply(p2), TEST_EPS);
    }

    @Test
    public void testFrom_invertPolar() {
        // arrange
        Point2S p0 = Point2S.of(0, 0);
        Point2S p1 = Point2S.of(1, 1);
        Point2S p2 = Point2S.of(-1, 2);

        // act
        FunctionTransform2S t = FunctionTransform2S.from(p -> {
            double az = p.getAzimuth();
            double polar = Geometry.PI - p.getPolar();
            return Point2S.of(az, polar);
        });

        // assert
        Assert.assertFalse(t.preservesOrientation());

        SphericalTestUtils.assertPointsEqual(Point2S.of(0, Geometry.PI), t.apply(p0), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(1, Geometry.PI - 1), t.apply(p1), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(-1, Geometry.PI - 2), t.apply(p2), TEST_EPS);
    }

    @Test
    public void testFrom_negate_azimuthAndPolar() {
        // arrange
        Point2S p0 = Point2S.of(0, 0);
        Point2S p1 = Point2S.of(1, 1);
        Point2S p2 = Point2S.of(-1, 2);

        // act
        FunctionTransform2S t = FunctionTransform2S.from(p -> {
            double az = -p.getAzimuth();
            double polar = -p.getPolar();
            return Point2S.of(az, polar);
        });

        // assert
        Assert.assertFalse(t.preservesOrientation());

        SphericalTestUtils.assertPointsEqual(Point2S.of(0, 0), t.apply(p0), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(-1, 1), t.apply(p1), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(1, 2), t.apply(p2), TEST_EPS);
    }
}
