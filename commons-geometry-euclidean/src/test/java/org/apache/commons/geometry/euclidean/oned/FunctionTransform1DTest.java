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

import java.util.function.UnaryOperator;

import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class FunctionTransform1DTest {

    private static final double TEST_EPS = 1e-15;

    @Test
    public void testIdentity() {
        // arrange
        Vector1D p0 = Vector1D.of(0);
        Vector1D p1 = Vector1D.of(1);
        Vector1D p2 = Vector1D.of(-1);

        // act
        Transform1D t = Transform1D.identity();

        // assert
        Assert.assertTrue(t.preservesOrientation());

        EuclideanTestUtils.assertCoordinatesEqual(p0, t.apply(p0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, t.apply(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, t.apply(p2), TEST_EPS);
    }

    @Test
    public void testFrom_identity() {
        // arrange
        Vector1D p0 = Vector1D.of(0);
        Vector1D p1 = Vector1D.of(1);
        Vector1D p2 = Vector1D.of(-1);

        // act
        Transform1D t = Transform1D.from(UnaryOperator.identity());

        // assert
        Assert.assertTrue(t.preservesOrientation());

        EuclideanTestUtils.assertCoordinatesEqual(p0, t.apply(p0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, t.apply(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, t.apply(p2), TEST_EPS);
    }

    @Test
    public void testFrom_scaleAndTranslate() {
        // arrange
        Vector1D p0 = Vector1D.of(0);
        Vector1D p1 = Vector1D.of(1);
        Vector1D p2 = Vector1D.of(-1);

        // act
        Transform1D t = Transform1D.from(v -> Vector1D.of((v.getX() + 2) * 3));

        // assert
        Assert.assertTrue(t.preservesOrientation());

        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(6), t.apply(p0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(9), t.apply(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(3), t.apply(p2), TEST_EPS);
    }

    @Test
    public void testFrom_reflection() {
        // arrange
        Vector1D p0 = Vector1D.of(0);
        Vector1D p1 = Vector1D.of(1);
        Vector1D p2 = Vector1D.of(-1);

        // act
        Transform1D t = Transform1D.from(Vector1D::negate);

        // assert
        Assert.assertFalse(t.preservesOrientation());

        EuclideanTestUtils.assertCoordinatesEqual(p0, t.apply(p0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, t.apply(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, t.apply(p2), TEST_EPS);
    }

    @Test
    public void testApply() {
        // arrange
        Transform1D t = Transform1D.from(v -> {
            double x = v.getX();
            return Vector1D.of((-2 * x) + 1);
        });

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(1), t.apply(Vector1D.ZERO), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(-1), t.apply(Vector1D.Unit.PLUS), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(-3), t.apply(Vector1D.of(2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(3), t.apply(Vector1D.of(-1)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(5), t.apply(Vector1D.of(-2)), TEST_EPS);
    }

    @Test
    public void testApplyVector() {
        // arrange
        Transform1D t = Transform1D.from(v -> {
            double x = v.getX();
            return Vector1D.of((-2 * x) + 1);
        });

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.ZERO, t.applyVector(Vector1D.ZERO), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(-2), t.applyVector(Vector1D.Unit.PLUS), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(-4), t.applyVector(Vector1D.of(2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(2), t.applyVector(Vector1D.of(-1)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(4), t.applyVector(Vector1D.of(-2)), TEST_EPS);
    }

    @Test
    public void testToMatrix() {
        // act/assert
        Assert.assertArrayEquals(new double[] {1, 0},
                Transform1D.identity().toMatrix().toArray(), TEST_EPS);
        Assert.assertArrayEquals(new double[] {1, 2},
                Transform1D.from(v -> v.add(Vector1D.of(2))).toMatrix().toArray(), TEST_EPS);
        Assert.assertArrayEquals(new double[] {3, 0},
                Transform1D.from(v -> v.multiply(3)).toMatrix().toArray(), TEST_EPS);
        Assert.assertArrayEquals(new double[] {3, 6},
                Transform1D.from(v -> v.add(Vector1D.of(2)).multiply(3)).toMatrix().toArray(), TEST_EPS);
    }

    @Test
    public void testTransformRoundTrip() {
        // arrange
        double eps = 1e-8;
        double delta = 0.11;

        Vector1D p1 = Vector1D.of(1.1);
        Vector1D p2 = Vector1D.of(-5);
        Vector1D vec = p1.vectorTo(p2);

        EuclideanTestUtils.permuteSkipZero(-2, 2, delta, (translate, scale) -> {

            Transform1D t = Transform1D.from(v -> {
                return v.multiply(scale * 0.5)
                    .add(Vector1D.of(translate))
                    .multiply(scale * 1.5);
            });

            // act
            Vector1D t1 = t.apply(p1);
            Vector1D t2 = t.apply(p2);
            Vector1D tvec = t.applyVector(vec);

            Transform1D inverse = t.toMatrix().inverse();

            // assert
            EuclideanTestUtils.assertCoordinatesEqual(tvec, t1.vectorTo(t2), eps);
            EuclideanTestUtils.assertCoordinatesEqual(p1, inverse.apply(t1), eps);
            EuclideanTestUtils.assertCoordinatesEqual(p2, inverse.apply(t2), eps);
        });
    }
}
