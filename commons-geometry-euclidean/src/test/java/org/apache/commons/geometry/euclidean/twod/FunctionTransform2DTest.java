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

import java.util.function.UnaryOperator;

import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class FunctionTransform2DTest {

    private static final double TEST_EPS = 1e-15;

    @Test
    public void testIdentity() {
        // arrange
        Vector2D p0 = Vector2D.of(0, 0);
        Vector2D p1 = Vector2D.of(1, 1);
        Vector2D p2 = Vector2D.of(-1, -1);

        // act
        FunctionTransform2D t = FunctionTransform2D.identity();

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
        FunctionTransform2D t = FunctionTransform2D.from(UnaryOperator.identity());

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
        FunctionTransform2D t = FunctionTransform2D.from(v -> v.multiply(2).add(Vector2D.of(1, -1)));

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
        FunctionTransform2D t = FunctionTransform2D.from(v -> Vector2D.of(-v.getX(), v.getY()));

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
        FunctionTransform2D t = FunctionTransform2D.from(Vector2D::negate);

        // assert
        Assert.assertTrue(t.preservesOrientation());

        EuclideanTestUtils.assertCoordinatesEqual(p0, t.apply(p0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, -2), t.apply(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2), t.apply(p2), TEST_EPS);
    }

    @Test
    public void testApplyVector() {
        // arrange
        Transform2D t = FunctionTransform2D.from(v -> {
            return v.multiply(-2).add(Vector2D.of(4, 5));
        });

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, t.applyVector(Vector2D.ZERO), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2, 0), t.applyVector(Vector2D.Unit.PLUS_X), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-4, -4), t.applyVector(Vector2D.of(2, 2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 0), t.applyVector(Vector2D.of(-1, 0)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(4, 6), t.applyVector(Vector2D.of(-2, -3)), TEST_EPS);
    }

    @Test
    public void testToMatrix() {
        // act/assert
        Assert.assertArrayEquals(new double[] {
                    1, 0, 0,
                    0, 1, 0
                },
                FunctionTransform2D.identity().toMatrix().toArray(), TEST_EPS);
        Assert.assertArrayEquals(new double[] {
                    1, 0, 2,
                    0, 1, 3
                },
                FunctionTransform2D.from(v -> v.add(Vector2D.of(2, 3))).toMatrix().toArray(), TEST_EPS);
        Assert.assertArrayEquals(new double[] {
                    3, 0, 0,
                    0, 3, 0
                },
                FunctionTransform2D.from(v -> v.multiply(3)).toMatrix().toArray(), TEST_EPS);
        Assert.assertArrayEquals(new double[] {
                    3, 0, 6,
                    0, 3, 9
                },
                FunctionTransform2D.from(v -> v.add(Vector2D.of(2, 3)).multiply(3)).toMatrix().toArray(), TEST_EPS);
    }

    @Test
    public void testTransformRoundTrip() {
        // arrange
        double eps = 1e-8;
        double delta = 0.11;

        Vector2D p1 = Vector2D.of(1.1, -3);
        Vector2D p2 = Vector2D.of(-5, 0.2);
        Vector2D vec = p1.vectorTo(p2);

        EuclideanTestUtils.permuteSkipZero(-2, 2, delta, (translate, scale) -> {

            FunctionTransform2D t = FunctionTransform2D.from(v -> {
                return v.multiply(scale * 0.5)
                    .add(Vector2D.of(translate, 0.5 * translate))
                    .multiply(scale * 1.5);
            });

            // act
            Vector2D t1 = t.apply(p1);
            Vector2D t2 = t.apply(p2);
            Vector2D tvec = t.applyVector(vec);

            Transform2D inverse = t.toMatrix().inverse();

            // assert
            EuclideanTestUtils.assertCoordinatesEqual(tvec, t1.vectorTo(t2), eps);
            EuclideanTestUtils.assertCoordinatesEqual(p1, inverse.apply(t1), eps);
            EuclideanTestUtils.assertCoordinatesEqual(p2, inverse.apply(t2), eps);
        });
    }
}
