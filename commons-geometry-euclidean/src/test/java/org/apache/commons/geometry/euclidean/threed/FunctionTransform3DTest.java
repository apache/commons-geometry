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

import java.util.function.UnaryOperator;

import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class FunctionTransform3DTest {

    private static final double TEST_EPS = 1e-15;

    @Test
    public void testIdentity() {
        // arrange
        Vector3D p0 = Vector3D.of(0, 0, 0);
        Vector3D p1 = Vector3D.of(1, 1, 1);
        Vector3D p2 = Vector3D.of(-1, -1, -1);

        // act
        Transform3D t = Transform3D.identity();

        // assert
        Assert.assertTrue(t.preservesOrientation());

        EuclideanTestUtils.assertCoordinatesEqual(p0, t.apply(p0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, t.apply(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, t.apply(p2), TEST_EPS);
    }

    @Test
    public void testFrom_identity() {
        // arrange
        Vector3D p0 = Vector3D.of(0, 0, 0);
        Vector3D p1 = Vector3D.of(1, 1, 1);
        Vector3D p2 = Vector3D.of(-1, -1, -1);

        // act
        Transform3D t = Transform3D.from(UnaryOperator.identity());

        // assert
        Assert.assertTrue(t.preservesOrientation());

        EuclideanTestUtils.assertCoordinatesEqual(p0, t.apply(p0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, t.apply(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, t.apply(p2), TEST_EPS);
    }

    @Test
    public void testFrom_scaleAndTranslate() {
        // arrange
        Vector3D p0 = Vector3D.of(0, 0, 0);
        Vector3D p1 = Vector3D.of(1, 2, 3);
        Vector3D p2 = Vector3D.of(-1, -2, -3);

        // act
        Transform3D t = Transform3D.from(v -> v.multiply(2).add(Vector3D.of(1, -1, 2)));

        // assert
        Assert.assertTrue(t.preservesOrientation());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, -1, 2), t.apply(p0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 3, 8), t.apply(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, -5, -4), t.apply(p2), TEST_EPS);
    }

    @Test
    public void testFrom_reflection_singleAxis() {
        // arrange
        Vector3D p0 = Vector3D.of(0, 0, 0);
        Vector3D p1 = Vector3D.of(1, 2, 3);
        Vector3D p2 = Vector3D.of(-1, -2, -3);

        // act
        Transform3D t = Transform3D.from(v -> Vector3D.of(-v.getX(), v.getY(), v.getZ()));

        // assert
        Assert.assertFalse(t.preservesOrientation());

        EuclideanTestUtils.assertCoordinatesEqual(p0, t.apply(p0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 2, 3), t.apply(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, -2, -3), t.apply(p2), TEST_EPS);
    }

    @Test
    public void testFrom_reflection_twoAxes() {
        // arrange
        Vector3D p0 = Vector3D.of(0, 0, 0);
        Vector3D p1 = Vector3D.of(1, 2, 3);
        Vector3D p2 = Vector3D.of(-1, -2, -3);

        // act
        Transform3D t = Transform3D.from(v -> Vector3D.of(-v.getX(), -v.getY(), v.getZ()));

        // assert
        Assert.assertTrue(t.preservesOrientation());

        EuclideanTestUtils.assertCoordinatesEqual(p0, t.apply(p0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, -2, 3), t.apply(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, -3), t.apply(p2), TEST_EPS);
    }

    @Test
    public void testFrom_reflection_allAxes() {
        // arrange
        Vector3D p0 = Vector3D.of(0, 0, 0);
        Vector3D p1 = Vector3D.of(1, 2, 3);
        Vector3D p2 = Vector3D.of(-1, -2, -3);

        // act
        Transform3D t = Transform3D.from(Vector3D::negate);

        // assert
        Assert.assertFalse(t.preservesOrientation());

        EuclideanTestUtils.assertCoordinatesEqual(p0, t.apply(p0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, -2, -3), t.apply(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 3), t.apply(p2), TEST_EPS);
    }

    @Test
    public void testToMatrix() {
        // act/assert
        Assert.assertArrayEquals(new double[] {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0
        }, Transform3D.identity().toMatrix().toArray(), TEST_EPS);
        Assert.assertArrayEquals(new double[] {
            1, 0, 0, 2,
            0, 1, 0, 3,
            0, 0, 1, -4
        }, Transform3D.from(v -> v.add(Vector3D.of(2, 3, -4))).toMatrix().toArray(), TEST_EPS);
        Assert.assertArrayEquals(new double[] {
            3, 0, 0, 0,
            0, 3, 0, 0,
            0, 0, 3, 0
        }, Transform3D.from(v -> v.multiply(3)).toMatrix().toArray(), TEST_EPS);
        Assert.assertArrayEquals(new double[] {
            3, 0, 0, 6,
            0, 3, 0, 9,
            0, 0, 3, 12
        }, Transform3D.from(v -> v.add(Vector3D.of(2, 3, 4)).multiply(3)).toMatrix().toArray(), TEST_EPS);
    }

    @Test
    public void testTransformRoundTrip() {
        // arrange
        double eps = 1e-8;
        double delta = 0.11;

        Vector3D p1 = Vector3D.of(1.1, -3, 0);
        Vector3D p2 = Vector3D.of(-5, 0.2, 2);
        Vector3D vec = p1.vectorTo(p2);

        EuclideanTestUtils.permuteSkipZero(-2, 2, delta, (translate, scale) -> {

            Transform3D t = Transform3D.from(v -> {
                return v.multiply(scale * 0.5)
                    .add(Vector3D.of(translate, 0.5 * translate, 0.25 * translate))
                    .multiply(scale * 1.5);
            });

            // act
            Vector3D t1 = t.apply(p1);
            Vector3D t2 = t.apply(p2);
            Vector3D tvec = t.applyVector(vec);

            Transform3D inverse = t.toMatrix().inverse();

            // assert
            EuclideanTestUtils.assertCoordinatesEqual(tvec, t1.vectorTo(t2), eps);
            EuclideanTestUtils.assertCoordinatesEqual(p1, inverse.apply(t1), eps);
            EuclideanTestUtils.assertCoordinatesEqual(p2, inverse.apply(t2), eps);
        });
    }
}
