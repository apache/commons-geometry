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

import java.util.function.Function;

import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class Transform2DTest {

    private static final double TEST_EPS = 1e-15;

    @Test
    public void testIdentity() {
        // arrange
        Vector2D p0 = Vector2D.of(0, 0);
        Vector2D p1 = Vector2D.of(1, 1);
        Vector2D p2 = Vector2D.of(-1, -1);

        // act
        Transform2D t = Transform2D.identity();

        // assert
        Assert.assertTrue(t.preservesOrientation());

        EuclideanTestUtils.assertCoordinatesEqual(p0, t.apply(p0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, t.apply(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, t.apply(p2), TEST_EPS);
    }

    @Test
    public void testFrom_identity() {
        // arrange
        Vector2D p0 = Vector2D.of(0, 0);
        Vector2D p1 = Vector2D.of(1, 1);
        Vector2D p2 = Vector2D.of(-1, -1);

        // act
        Transform2D t = Transform2D.from(Function.identity());

        // assert
        Assert.assertTrue(t.preservesOrientation());

        EuclideanTestUtils.assertCoordinatesEqual(p0, t.apply(p0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, t.apply(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, t.apply(p2), TEST_EPS);
    }

    @Test
    public void testFrom_scaleAndTranslate() {
        // arrange
        Vector2D p0 = Vector2D.of(0, 0);
        Vector2D p1 = Vector2D.of(1, 2);
        Vector2D p2 = Vector2D.of(-1, -2);

        // act
        Transform2D t = Transform2D.from(v -> v.multiply(2).add(Vector2D.of(1, -1)));

        // assert
        Assert.assertTrue(t.preservesOrientation());

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, -1), t.apply(p0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 3), t.apply(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, -5), t.apply(p2), TEST_EPS);
    }

    @Test
    public void testFrom_reflection_singleAxis() {
        // arrange
        Vector2D p0 = Vector2D.of(0, 0);
        Vector2D p1 = Vector2D.of(1, 2);
        Vector2D p2 = Vector2D.of(-1, -2);

        // act
        Transform2D t = Transform2D.from(v -> Vector2D.of(-v.getX(), v.getY()));

        // assert
        Assert.assertFalse(t.preservesOrientation());

        EuclideanTestUtils.assertCoordinatesEqual(p0, t.apply(p0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 2), t.apply(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, -2), t.apply(p2), TEST_EPS);
    }

    @Test
    public void testFrom_reflection_bothAxes() {
        // arrange
        Vector2D p0 = Vector2D.of(0, 0);
        Vector2D p1 = Vector2D.of(1, 2);
        Vector2D p2 = Vector2D.of(-1, -2);

        // act
        Transform2D t = Transform2D.from(Vector2D::negate);

        // assert
        Assert.assertTrue(t.preservesOrientation());

        EuclideanTestUtils.assertCoordinatesEqual(p0, t.apply(p0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, -2), t.apply(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2), t.apply(p2), TEST_EPS);
    }
}
