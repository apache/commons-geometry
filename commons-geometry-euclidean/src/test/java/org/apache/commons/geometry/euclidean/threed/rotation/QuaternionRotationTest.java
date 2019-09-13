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
package org.apache.commons.geometry.euclidean.threed.rotation;

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.exception.IllegalNormException;
import org.apache.commons.geometry.core.internal.SimpleTupleFormat;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.apache.commons.numbers.core.Precision;
import org.apache.commons.numbers.quaternion.Quaternion;
import org.apache.commons.numbers.quaternion.Slerp;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.Assert;
import org.junit.Test;

public class QuaternionRotationTest {

    private static final double EPS = 1e-12;

    // use non-normalized axes to ensure that the axis is normalized
    private static final Vector3D PLUS_X_DIR = Vector3D.of(2, 0, 0);
    private static final Vector3D MINUS_X_DIR = Vector3D.of(-2, 0, 0);

    private static final Vector3D PLUS_Y_DIR = Vector3D.of(0, 3, 0);
    private static final Vector3D MINUS_Y_DIR = Vector3D.of(0, -3, 0);

    private static final Vector3D PLUS_Z_DIR = Vector3D.of(0, 0, 4);
    private static final Vector3D MINUS_Z_DIR = Vector3D.of(0, 0, -4);

    private static final Vector3D PLUS_DIAGONAL = Vector3D.of(1, 1, 1);
    private static final Vector3D MINUS_DIAGONAL = Vector3D.of(-1, -1, -1);

    private static final double TWO_THIRDS_PI = 2.0 * Geometry.PI / 3.0;
    private static final double MINUS_TWO_THIRDS_PI = -TWO_THIRDS_PI;

    @Test
    public void testOf_quaternion() {
        // act/assert
        checkQuaternion(QuaternionRotation.of(Quaternion.of(1, 0, 0, 0)), 1, 0, 0, 0);
        checkQuaternion(QuaternionRotation.of(Quaternion.of(-1, 0, 0, 0)), 1, 0, 0, 0);
        checkQuaternion(QuaternionRotation.of(Quaternion.of(0, 1, 0, 0)), 0, 1, 0, 0);
        checkQuaternion(QuaternionRotation.of(Quaternion.of(0, 0, 1, 0)), 0, 0, 1, 0);
        checkQuaternion(QuaternionRotation.of(Quaternion.of(0, 0, 0, 1)), 0, 0, 0, 1);

        checkQuaternion(QuaternionRotation.of(Quaternion.of(1, 1, 1, 1)), 0.5, 0.5, 0.5, 0.5);
        checkQuaternion(QuaternionRotation.of(Quaternion.of(-1, -1, -1, -1)), 0.5, 0.5, 0.5, 0.5);
    }

    @Test
    public void testOf_quaternion_illegalNorm() {
        // act/assert
        GeometryTestUtils.assertThrows(() ->
            QuaternionRotation.of(Quaternion.of(0, 0, 0, 0)), IllegalStateException.class);
        GeometryTestUtils.assertThrows(() ->
            QuaternionRotation.of(Quaternion.of(1, 1, 1, Double.NaN)), IllegalStateException.class);
        GeometryTestUtils.assertThrows(() ->
            QuaternionRotation.of(Quaternion.of(1, 1, Double.POSITIVE_INFINITY, 1)), IllegalStateException.class);
        GeometryTestUtils.assertThrows(() ->
            QuaternionRotation.of(Quaternion.of(1, Double.NEGATIVE_INFINITY, 1, 1)), IllegalStateException.class);
        GeometryTestUtils.assertThrows(() ->
            QuaternionRotation.of(Quaternion.of(Double.NaN, 1, 1, 1)), IllegalStateException.class);
    }

    @Test
    public void testOf_components() {
        // act/assert
        checkQuaternion(QuaternionRotation.of(1, 0, 0, 0), 1, 0, 0, 0);
        checkQuaternion(QuaternionRotation.of(-1, 0, 0, 0), 1, 0, 0, 0);
        checkQuaternion(QuaternionRotation.of(0, 1, 0, 0), 0, 1, 0, 0);
        checkQuaternion(QuaternionRotation.of(0, 0, 1, 0), 0, 0, 1, 0);
        checkQuaternion(QuaternionRotation.of(0, 0, 0, 1), 0, 0, 0, 1);

        checkQuaternion(QuaternionRotation.of(1, 1, 1, 1), 0.5, 0.5, 0.5, 0.5);
        checkQuaternion(QuaternionRotation.of(-1, -1, -1, -1), 0.5, 0.5, 0.5, 0.5);
    }

    @Test
    public void testOf_components_illegalNorm() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> QuaternionRotation.of(0, 0, 0, 0), IllegalStateException.class);
        GeometryTestUtils.assertThrows(() -> QuaternionRotation.of(1, 1, 1, Double.NaN), IllegalStateException.class);
        GeometryTestUtils.assertThrows(() -> QuaternionRotation.of(1, 1, Double.POSITIVE_INFINITY, 1), IllegalStateException.class);
        GeometryTestUtils.assertThrows(() -> QuaternionRotation.of(1, Double.NEGATIVE_INFINITY, 1, 1), IllegalStateException.class);
        GeometryTestUtils.assertThrows(() -> QuaternionRotation.of(Double.NaN, 1, 1, 1), IllegalStateException.class);
    }

    @Test
    public void testIdentity() {
        // act
        QuaternionRotation q = QuaternionRotation.identity();

        // assert
        assertRotationEquals(StandardRotations.IDENTITY, q);
    }

    @Test
    public void testIdentity_axis() {
        // arrange
        QuaternionRotation q = QuaternionRotation.identity();

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.PLUS_X, q.getAxis(), EPS);
    }

    @Test
    public void testGetAxis()
    {
        // act/assert
        checkVector(QuaternionRotation.of(0, 1, 0, 0).getAxis(), 1, 0, 0);
        checkVector(QuaternionRotation.of(0, -1, 0, 0).getAxis(), -1, 0, 0);

        checkVector(QuaternionRotation.of(0, 0, 1, 0).getAxis(), 0, 1, 0);
        checkVector(QuaternionRotation.of(0, 0, -1, 0).getAxis(), 0, -1, 0);

        checkVector(QuaternionRotation.of(0, 0, 0, 1).getAxis(), 0, 0, 1);
        checkVector(QuaternionRotation.of(0, 0, 0, -1).getAxis(), 0, 0, -1);
    }

    @Test
    public void testGetAxis_noAxis() {
        // arrange
        QuaternionRotation rot = QuaternionRotation.of(1, 0, 0, 0);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.PLUS_X, rot.getAxis(), EPS);
    }

    @Test
    public void testGetAxis_matchesAxisAngleConstruction() {
        EuclideanTestUtils.permuteSkipZero(-5, 5, 1, (x, y, z) -> {
            // arrange
            Vector3D vec = Vector3D.of(x, y, z);
            Vector3D norm = vec.normalize();

            // act/assert

            // positive angle results in the axis being the normalized input axis
            EuclideanTestUtils.assertCoordinatesEqual(norm,
                    QuaternionRotation.fromAxisAngle(vec, Geometry.HALF_PI).getAxis(), EPS);

            // negative angle results in the axis being the negated normalized input axis
            EuclideanTestUtils.assertCoordinatesEqual(norm,
                    QuaternionRotation.fromAxisAngle(vec.negate(), Geometry.MINUS_HALF_PI).getAxis(), EPS);
        });
    }

    @Test
    public void testGetAngle() {
        // act/assert
        Assert.assertEquals(Geometry.ZERO_PI, QuaternionRotation.of(1, 0, 0, 0).getAngle(), EPS);
        Assert.assertEquals(Geometry.ZERO_PI, QuaternionRotation.of(-1, 0, 0, 0).getAngle(), EPS);

        Assert.assertEquals(Geometry.HALF_PI, QuaternionRotation.of(1, 0, 0, 1).getAngle(), EPS);
        Assert.assertEquals(Geometry.HALF_PI, QuaternionRotation.of(-1, 0, 0, -1).getAngle(), EPS);

        Assert.assertEquals(Geometry.PI  * 2.0 / 3.0, QuaternionRotation.of(1, 1, 1, 1).getAngle(), EPS);

        Assert.assertEquals(Geometry.PI, QuaternionRotation.of(0, 0, 0, 1).getAngle(), EPS);
    }

    @Test
    public void testGetAngle_matchesAxisAngleConstruction() {
        for (double theta = -2 * Geometry.PI; theta <= 2 * Geometry.PI; theta += 0.1) {
            // arrange
            QuaternionRotation rot = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, theta);

            // act
            double angle = rot.getAngle();

            // assert
            // make sure that we're in the [0, pi] range
            Assert.assertTrue(angle >= Geometry.ZERO_PI);
            Assert.assertTrue(angle <= Geometry.PI);

            double expected = PlaneAngleRadians.normalizeBetweenMinusPiAndPi(theta);
            if (PLUS_DIAGONAL.dot(rot.getAxis()) < 0) {
                // if the axis ended up being flipped, then negate the expected angle
                expected *= -1;
            }

            Assert.assertEquals(expected, angle, EPS);
        }
    }

    @Test
    public void testFromAxisAngle_apply() {
        // act/assert

        // --- x axes
        assertRotationEquals(StandardRotations.IDENTITY, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, Geometry.ZERO_PI));

        assertRotationEquals(StandardRotations.PLUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, Geometry.HALF_PI));
        assertRotationEquals(StandardRotations.PLUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_X_DIR, Geometry.MINUS_HALF_PI));

        assertRotationEquals(StandardRotations.MINUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_X_DIR, Geometry.HALF_PI));
        assertRotationEquals(StandardRotations.MINUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, Geometry.MINUS_HALF_PI));

        assertRotationEquals(StandardRotations.X_PI, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, Geometry.PI));
        assertRotationEquals(StandardRotations.X_PI, QuaternionRotation.fromAxisAngle(MINUS_X_DIR, Geometry.PI));

        // --- y axes
        assertRotationEquals(StandardRotations.IDENTITY, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, Geometry.ZERO_PI));

        assertRotationEquals(StandardRotations.PLUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, Geometry.HALF_PI));
        assertRotationEquals(StandardRotations.PLUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Y_DIR, Geometry.MINUS_HALF_PI));

        assertRotationEquals(StandardRotations.MINUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Y_DIR, Geometry.HALF_PI));
        assertRotationEquals(StandardRotations.MINUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, Geometry.MINUS_HALF_PI));

        assertRotationEquals(StandardRotations.Y_PI, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, Geometry.PI));
        assertRotationEquals(StandardRotations.Y_PI, QuaternionRotation.fromAxisAngle(MINUS_Y_DIR, Geometry.PI));

        // --- z axes
        assertRotationEquals(StandardRotations.IDENTITY, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, Geometry.ZERO_PI));

        assertRotationEquals(StandardRotations.PLUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, Geometry.HALF_PI));
        assertRotationEquals(StandardRotations.PLUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Z_DIR, Geometry.MINUS_HALF_PI));

        assertRotationEquals(StandardRotations.MINUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Z_DIR, Geometry.HALF_PI));
        assertRotationEquals(StandardRotations.MINUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, Geometry.MINUS_HALF_PI));

        assertRotationEquals(StandardRotations.Z_PI, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, Geometry.PI));
        assertRotationEquals(StandardRotations.Z_PI, QuaternionRotation.fromAxisAngle(MINUS_Z_DIR, Geometry.PI));

        // --- diagonal
        assertRotationEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, TWO_THIRDS_PI));
        assertRotationEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(MINUS_DIAGONAL, MINUS_TWO_THIRDS_PI));

        assertRotationEquals(StandardRotations.MINUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(MINUS_DIAGONAL, TWO_THIRDS_PI));
        assertRotationEquals(StandardRotations.MINUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, MINUS_TWO_THIRDS_PI));
    }

    @Test
    public void testFromAxisAngle_invalidAxisNorm() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> QuaternionRotation.fromAxisAngle(Vector3D.ZERO, Geometry.HALF_PI), IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> QuaternionRotation.fromAxisAngle(Vector3D.NaN, Geometry.HALF_PI), IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> QuaternionRotation.fromAxisAngle(Vector3D.POSITIVE_INFINITY, Geometry.HALF_PI), IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> QuaternionRotation.fromAxisAngle(Vector3D.NEGATIVE_INFINITY, Geometry.HALF_PI), IllegalNormException.class);
    }

    @Test
    public void testFromAxisAngle_invalidAngle() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> QuaternionRotation.fromAxisAngle(Vector3D.PLUS_X, Double.NaN), IllegalArgumentException.class,
                "Invalid angle: NaN");
        GeometryTestUtils.assertThrows(() -> QuaternionRotation.fromAxisAngle(Vector3D.PLUS_X, Double.POSITIVE_INFINITY), IllegalArgumentException.class,
                "Invalid angle: Infinity");
        GeometryTestUtils.assertThrows(() -> QuaternionRotation.fromAxisAngle(Vector3D.PLUS_X, Double.NEGATIVE_INFINITY), IllegalArgumentException.class,
                "Invalid angle: -Infinity");
    }

    @Test
    public void testInverse() {
        // arrange
        QuaternionRotation rot = QuaternionRotation.of(0.5, 0.5, 0.5, 0.5);

        // act
        QuaternionRotation neg = rot.inverse();

        // assert
        Assert.assertEquals(-0.5, neg.getQuaternion().getX(), EPS);
        Assert.assertEquals(-0.5, neg.getQuaternion().getY(), EPS);
        Assert.assertEquals(-0.5, neg.getQuaternion().getZ(), EPS);
        Assert.assertEquals(0.5, neg.getQuaternion().getW(), EPS);
    }

    @Test
    public void testInverse_apply() {
        // act/assert

        // --- x axes
        assertRotationEquals(StandardRotations.IDENTITY, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, Geometry.ZERO_PI).inverse());

        assertRotationEquals(StandardRotations.PLUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, Geometry.MINUS_HALF_PI).inverse());
        assertRotationEquals(StandardRotations.PLUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_X_DIR, Geometry.HALF_PI).inverse());

        assertRotationEquals(StandardRotations.MINUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_X_DIR, Geometry.MINUS_HALF_PI).inverse());
        assertRotationEquals(StandardRotations.MINUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, Geometry.HALF_PI).inverse());

        assertRotationEquals(StandardRotations.X_PI, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, Geometry.PI).inverse());
        assertRotationEquals(StandardRotations.X_PI, QuaternionRotation.fromAxisAngle(MINUS_X_DIR, Geometry.PI).inverse());

        // --- y axes
        assertRotationEquals(StandardRotations.IDENTITY, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, Geometry.ZERO_PI).inverse());

        assertRotationEquals(StandardRotations.PLUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, Geometry.MINUS_HALF_PI).inverse());
        assertRotationEquals(StandardRotations.PLUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Y_DIR, Geometry.HALF_PI).inverse());

        assertRotationEquals(StandardRotations.MINUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Y_DIR, Geometry.MINUS_HALF_PI).inverse());
        assertRotationEquals(StandardRotations.MINUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, Geometry.HALF_PI).inverse());

        assertRotationEquals(StandardRotations.Y_PI, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, Geometry.PI).inverse());
        assertRotationEquals(StandardRotations.Y_PI, QuaternionRotation.fromAxisAngle(MINUS_Y_DIR, Geometry.PI).inverse());

        // --- z axes
        assertRotationEquals(StandardRotations.IDENTITY, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, Geometry.ZERO_PI).inverse());

        assertRotationEquals(StandardRotations.PLUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, Geometry.MINUS_HALF_PI).inverse());
        assertRotationEquals(StandardRotations.PLUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Z_DIR, Geometry.HALF_PI).inverse());

        assertRotationEquals(StandardRotations.MINUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Z_DIR, Geometry.MINUS_HALF_PI).inverse());
        assertRotationEquals(StandardRotations.MINUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, Geometry.HALF_PI).inverse());

        assertRotationEquals(StandardRotations.Z_PI, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, Geometry.PI).inverse());
        assertRotationEquals(StandardRotations.Z_PI, QuaternionRotation.fromAxisAngle(MINUS_Z_DIR, Geometry.PI).inverse());

        // --- diagonal
        assertRotationEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, MINUS_TWO_THIRDS_PI).inverse());
        assertRotationEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(MINUS_DIAGONAL, TWO_THIRDS_PI).inverse());

        assertRotationEquals(StandardRotations.MINUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(MINUS_DIAGONAL, MINUS_TWO_THIRDS_PI).inverse());
        assertRotationEquals(StandardRotations.MINUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, TWO_THIRDS_PI).inverse());
    }

    @Test
    public void testInverse_undoesOriginalRotation() {
        EuclideanTestUtils.permuteSkipZero(-5, 5, 1, (x, y, z) -> {
            // arrange
            Vector3D vec = Vector3D.of(x, y, z);

            QuaternionRotation rot = QuaternionRotation.fromAxisAngle(vec, 0.75 * Geometry.PI);
            QuaternionRotation neg = rot.inverse();

            // act/assert
            EuclideanTestUtils.assertCoordinatesEqual(PLUS_DIAGONAL, neg.apply(rot.apply(PLUS_DIAGONAL)), EPS);
            EuclideanTestUtils.assertCoordinatesEqual(PLUS_DIAGONAL, rot.apply(neg.apply(PLUS_DIAGONAL)), EPS);
        });
    }

    @Test
    public void testMultiply_sameAxis_simple() {
        // arrange
        QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_X, 0.1 * Geometry.PI);
        QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_X, 0.4 * Geometry.PI);

        // act
        QuaternionRotation result = q1.multiply(q2);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.PLUS_X, result.getAxis(), EPS);
        Assert.assertEquals(Geometry.HALF_PI, result.getAngle(), EPS);

        assertRotationEquals(StandardRotations.PLUS_X_HALF_PI, result);
    }

    @Test
    public void testMultiply_sameAxis_multiple() {
        // arrange
        double oneThird = 1.0 / 3.0;
        QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, 0.1 * Geometry.PI);
        QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, oneThird * Geometry.PI);
        QuaternionRotation q3 = QuaternionRotation.fromAxisAngle(MINUS_DIAGONAL, 0.4 * Geometry.PI);
        QuaternionRotation q4 = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, 0.3 * Geometry.PI);
        QuaternionRotation q5 = QuaternionRotation.fromAxisAngle(MINUS_DIAGONAL, - oneThird * Geometry.PI);

        // act
        QuaternionRotation result = q1.multiply(q2).multiply(q3).multiply(q4).multiply(q5);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(PLUS_DIAGONAL.normalize(), result.getAxis(), EPS);
        Assert.assertEquals(2.0 * Geometry.PI / 3.0, result.getAngle(), EPS);

        assertRotationEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, result);
    }

    @Test
    public void testMultiply_differentAxes() {
        // arrange
        QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_X, Geometry.HALF_PI);
        QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Y, Geometry.HALF_PI);

        // act
        QuaternionRotation result = q1.multiply(q2);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(PLUS_DIAGONAL.normalize(), result.getAxis(), EPS);
        Assert.assertEquals(2.0 * Geometry.PI / 3.0, result.getAngle(), EPS);

        assertRotationEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, result);

        assertRotationEquals((v) -> {
            Vector3D temp = StandardRotations.PLUS_Y_HALF_PI.apply(v);
            return StandardRotations.PLUS_X_HALF_PI.apply(temp);
        }, result);
    }

    @Test
    public void testMultiply_orderOfOperations() {
        // arrange
        QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_X, Geometry.HALF_PI);
        QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Y, Geometry.PI);
        QuaternionRotation q3 = QuaternionRotation.fromAxisAngle(Vector3D.MINUS_Z, Geometry.HALF_PI);

        // act
        QuaternionRotation result = q3.multiply(q2).multiply(q1);

        // assert
        assertRotationEquals((v) -> {
            Vector3D temp = StandardRotations.PLUS_X_HALF_PI.apply(v);
            temp = StandardRotations.Y_PI.apply(temp);
            return StandardRotations.MINUS_Z_HALF_PI.apply(temp);
        }, result);
    }

    @Test
    public void testMultiply_numericalStability() {
        // arrange
        int slices = 1024;
        double delta = (8.0 * Geometry.PI / 3.0) / slices;

        QuaternionRotation q = QuaternionRotation.identity();

        UniformRandomProvider rand = RandomSource.create(RandomSource.JDK, 2L);

        // act
        for (int i=0; i<slices; ++i) {
            double angle = rand.nextDouble();
            QuaternionRotation forward = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, angle);
            QuaternionRotation backward = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, delta - angle);

            q = q.multiply(forward).multiply(backward);
        }

        // assert
        Assert.assertTrue(q.getQuaternion().getW() > 0);
        Assert.assertEquals(1.0, q.getQuaternion().norm(), EPS);

        assertRotationEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, q);
    }

    @Test
    public void testPremultiply_sameAxis_simple() {
        // arrange
        QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_X, 0.1 * Geometry.PI);
        QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_X, 0.4 * Geometry.PI);

        // act
        QuaternionRotation result = q1.premultiply(q2);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.PLUS_X, result.getAxis(), EPS);
        Assert.assertEquals(Geometry.HALF_PI, result.getAngle(), EPS);

        assertRotationEquals(StandardRotations.PLUS_X_HALF_PI, result);
    }

    @Test
    public void testPremultiply_sameAxis_multiple() {
        // arrange
        double oneThird = 1.0 / 3.0;
        QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, 0.1 * Geometry.PI);
        QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, oneThird * Geometry.PI);
        QuaternionRotation q3 = QuaternionRotation.fromAxisAngle(MINUS_DIAGONAL, 0.4 * Geometry.PI);
        QuaternionRotation q4 = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, 0.3 * Geometry.PI);
        QuaternionRotation q5 = QuaternionRotation.fromAxisAngle(MINUS_DIAGONAL, - oneThird * Geometry.PI);

        // act
        QuaternionRotation result = q1.premultiply(q2).premultiply(q3).premultiply(q4).premultiply(q5);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(PLUS_DIAGONAL.normalize(), result.getAxis(), EPS);
        Assert.assertEquals(2.0 * Geometry.PI / 3.0, result.getAngle(), EPS);

        assertRotationEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, result);
    }

    @Test
    public void testPremultiply_differentAxes() {
        // arrange
        QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_X, Geometry.HALF_PI);
        QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Y, Geometry.HALF_PI);

        // act
        QuaternionRotation result = q2.premultiply(q1);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(PLUS_DIAGONAL.normalize(), result.getAxis(), EPS);
        Assert.assertEquals(2.0 * Geometry.PI / 3.0, result.getAngle(), EPS);

        assertRotationEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, result);

        assertRotationEquals((v) -> {
            Vector3D temp = StandardRotations.PLUS_Y_HALF_PI.apply(v);
            return StandardRotations.PLUS_X_HALF_PI.apply(temp);
        }, result);
    }

    @Test
    public void testPremultiply_orderOfOperations() {
        // arrange
        QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_X, Geometry.HALF_PI);
        QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Y, Geometry.PI);
        QuaternionRotation q3 = QuaternionRotation.fromAxisAngle(Vector3D.MINUS_Z, Geometry.HALF_PI);

        // act
        QuaternionRotation result = q1.premultiply(q2).premultiply(q3);

        // assert
        assertRotationEquals((v) -> {
            Vector3D temp = StandardRotations.PLUS_X_HALF_PI.apply(v);
            temp = StandardRotations.Y_PI.apply(temp);
            return StandardRotations.MINUS_Z_HALF_PI.apply(temp);
        }, result);
    }

    @Test
    public void testSlerp_simple() {
        // arrange
        QuaternionRotation q0 = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Z, Geometry.ZERO_PI);
        QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Z, Geometry.PI);
        final Slerp transform = q0.slerp(q1);
        Vector3D v = Vector3D.of(2, 0, 1);

        double sqrt2 = Math.sqrt(2);

        // act
        checkVector(QuaternionRotation.of(transform.apply(0)).apply(v), 2, 0, 1);
        checkVector(QuaternionRotation.of(transform.apply(0.25)).apply(v), sqrt2, sqrt2, 1);
        checkVector(QuaternionRotation.of(transform.apply(0.5)).apply(v), 0, 2, 1);
        checkVector(QuaternionRotation.of(transform.apply(0.75)).apply(v), -sqrt2, sqrt2, 1);
        checkVector(QuaternionRotation.of(transform.apply(1)).apply(v), -2, 0, 1);
    }

    @Test
    public void testSlerp_multipleCombinations() {
        // arrange
        QuaternionRotation[] rotations = {
                QuaternionRotation.fromAxisAngle(Vector3D.PLUS_X, Geometry.ZERO_PI),
                QuaternionRotation.fromAxisAngle(Vector3D.PLUS_X, Geometry.HALF_PI),
                QuaternionRotation.fromAxisAngle(Vector3D.PLUS_X, Geometry.PI),

                QuaternionRotation.fromAxisAngle(Vector3D.MINUS_X, Geometry.ZERO_PI),
                QuaternionRotation.fromAxisAngle(Vector3D.MINUS_X, Geometry.HALF_PI),
                QuaternionRotation.fromAxisAngle(Vector3D.MINUS_X, Geometry.PI),

                QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Y, Geometry.ZERO_PI),
                QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Y, Geometry.HALF_PI),
                QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Y, Geometry.PI),

                QuaternionRotation.fromAxisAngle(Vector3D.MINUS_Y, Geometry.ZERO_PI),
                QuaternionRotation.fromAxisAngle(Vector3D.MINUS_Y, Geometry.HALF_PI),
                QuaternionRotation.fromAxisAngle(Vector3D.MINUS_Y, Geometry.PI),

                QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Z, Geometry.ZERO_PI),
                QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Z, Geometry.HALF_PI),
                QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Z, Geometry.PI),

                QuaternionRotation.fromAxisAngle(Vector3D.MINUS_Z, Geometry.ZERO_PI),
                QuaternionRotation.fromAxisAngle(Vector3D.MINUS_Z, Geometry.HALF_PI),
                QuaternionRotation.fromAxisAngle(Vector3D.MINUS_Z, Geometry.PI),
        };

        // act/assert
        // test each rotation against all of the others (including itself)
        for (int i=0; i<rotations.length; ++i) {
            for (int j=0; j<rotations.length; ++j) {
                checkSlerpCombination(rotations[i], rotations[j]);
            }
        }
    }

    private void checkSlerpCombination(QuaternionRotation start, QuaternionRotation end) {
        final Slerp slerp = start.slerp(end);
        Vector3D vec = Vector3D.of(1, 1, 1).normalize();

        Vector3D startVec = start.apply(vec);
        Vector3D endVec = end.apply(vec);

        // check start and end values
        EuclideanTestUtils.assertCoordinatesEqual(startVec, QuaternionRotation.of(slerp.apply(0)).apply(vec), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(endVec, QuaternionRotation.of(slerp.apply(1)).apply(vec), EPS);

        // check intermediate values
        double prevAngle = -1;
        final int numSteps = 100;
        final double delta = 1d / numSteps;
        for (int step = 0; step <= numSteps; step++) {
            final double t = step * delta;
            QuaternionRotation result = QuaternionRotation.of(slerp.apply(t));

            Vector3D slerpVec = result.apply(vec);
            Assert.assertEquals(1, slerpVec.norm(), EPS);

            // make sure that we're steadily progressing to the end angle
            double angle = slerpVec.angle(startVec);
            Assert.assertTrue("Expected slerp angle to continuously increase; previous angle was " +
                              prevAngle + " and new angle is " + angle,
                              Precision.compareTo(angle, prevAngle, EPS) >= 0);

            prevAngle = angle;
        }
    }

    @Test
    public void testSlerp_followsShortestPath() {
        // arrange
        QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Z, 0.75 * Geometry.PI);
        QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Z, -0.75 * Geometry.PI);

        // act
        QuaternionRotation result = QuaternionRotation.of(q1.slerp(q2).apply(0.5));

        // assert
        // the slerp should have followed the path around the pi coordinate of the circle rather than
        // the one through the zero coordinate
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.MINUS_X, result.apply(Vector3D.PLUS_X), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.PLUS_Z, result.getAxis(), EPS);
        Assert.assertEquals(Geometry.PI, result.getAngle(), EPS);
    }

    @Test
    public void testSlerp_inputQuaternionsHaveMinusOneDotProduct() {
        // arrange
        QuaternionRotation q1 = QuaternionRotation.of(1, 0, 0, 1); // pi/2 around +z
        QuaternionRotation q2 = QuaternionRotation.of(-1, 0, 0, -1); // 3pi/2 around -z

        // act
        QuaternionRotation result = QuaternionRotation.of(q1.slerp(q2).apply(0.5));

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.PLUS_Y, result.apply(Vector3D.PLUS_X), EPS);

        Assert.assertEquals(Geometry.HALF_PI, result.getAngle(), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.PLUS_Z, result.getAxis(), EPS);
    }

    @Test
    public void testSlerp_outputQuaternionIsNormalizedForAllT() {
        // arrange
        QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Z, 0.25 * Geometry.PI);
        QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Z, 0.75 * Geometry.PI);

        final int numSteps = 200;
        final double delta = 1d / numSteps;
        for (int step = 0; step <= numSteps; step++) {
            final double t = -10 + step * delta;

            // act
            QuaternionRotation result = QuaternionRotation.of(q1.slerp(q2).apply(t));

            // assert
            Assert.assertEquals(1.0, result.getQuaternion().norm(), EPS);
        }
    }

    @Test
    public void testSlerp_tOutsideOfZeroToOne_apply() {
        // arrange
        Vector3D vec = Vector3D.PLUS_X;

        QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Z, 0.25 * Geometry.PI);
        QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Z, 0.75 * Geometry.PI);

        // act/assert
        final Slerp slerp12 = q1.slerp(q2);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.PLUS_X, QuaternionRotation.of(slerp12.apply(-4.5)).apply(vec), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.PLUS_X, QuaternionRotation.of(slerp12.apply(-0.5)).apply(vec), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.MINUS_X, QuaternionRotation.of(slerp12.apply(1.5)).apply(vec), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.MINUS_X, QuaternionRotation.of(slerp12.apply(5.5)).apply(vec), EPS);

        final Slerp slerp21 = q2.slerp(q1);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.MINUS_X, QuaternionRotation.of(slerp21.apply(-4.5)).apply(vec), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.MINUS_X, QuaternionRotation.of(slerp21.apply(-0.5)).apply(vec), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.PLUS_X, QuaternionRotation.of(slerp21.apply(1.5)).apply(vec), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.PLUS_X, QuaternionRotation.of(slerp21.apply(5.5)).apply(vec), EPS);
    }

    @Test
    public void testToTransformMatrix() {
        // act/assert
        // --- x axes
        assertTransformEquals(StandardRotations.IDENTITY, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, Geometry.ZERO_PI).toTransformMatrix());

        assertTransformEquals(StandardRotations.PLUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, Geometry.HALF_PI).toTransformMatrix());
        assertTransformEquals(StandardRotations.PLUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_X_DIR, Geometry.MINUS_HALF_PI).toTransformMatrix());

        assertTransformEquals(StandardRotations.MINUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_X_DIR, Geometry.HALF_PI).toTransformMatrix());
        assertTransformEquals(StandardRotations.MINUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, Geometry.MINUS_HALF_PI).toTransformMatrix());

        assertTransformEquals(StandardRotations.X_PI, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, Geometry.PI).toTransformMatrix());
        assertTransformEquals(StandardRotations.X_PI, QuaternionRotation.fromAxisAngle(MINUS_X_DIR, Geometry.PI).toTransformMatrix());

        // --- y axes
        assertTransformEquals(StandardRotations.IDENTITY, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, Geometry.ZERO_PI).toTransformMatrix());

        assertTransformEquals(StandardRotations.PLUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, Geometry.HALF_PI).toTransformMatrix());
        assertTransformEquals(StandardRotations.PLUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Y_DIR, Geometry.MINUS_HALF_PI).toTransformMatrix());

        assertTransformEquals(StandardRotations.MINUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Y_DIR, Geometry.HALF_PI).toTransformMatrix());
        assertTransformEquals(StandardRotations.MINUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, Geometry.MINUS_HALF_PI).toTransformMatrix());

        assertTransformEquals(StandardRotations.Y_PI, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, Geometry.PI).toTransformMatrix());
        assertTransformEquals(StandardRotations.Y_PI, QuaternionRotation.fromAxisAngle(MINUS_Y_DIR, Geometry.PI).toTransformMatrix());

        // --- z axes
        assertTransformEquals(StandardRotations.IDENTITY, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, Geometry.ZERO_PI).toTransformMatrix());

        assertTransformEquals(StandardRotations.PLUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, Geometry.HALF_PI).toTransformMatrix());
        assertTransformEquals(StandardRotations.PLUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Z_DIR, Geometry.MINUS_HALF_PI).toTransformMatrix());

        assertTransformEquals(StandardRotations.MINUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Z_DIR, Geometry.HALF_PI).toTransformMatrix());
        assertTransformEquals(StandardRotations.MINUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, Geometry.MINUS_HALF_PI).toTransformMatrix());

        assertTransformEquals(StandardRotations.Z_PI, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, Geometry.PI).toTransformMatrix());
        assertTransformEquals(StandardRotations.Z_PI, QuaternionRotation.fromAxisAngle(MINUS_Z_DIR, Geometry.PI).toTransformMatrix());

        // --- diagonal
        assertTransformEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, TWO_THIRDS_PI).toTransformMatrix());
        assertTransformEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(MINUS_DIAGONAL, MINUS_TWO_THIRDS_PI).toTransformMatrix());

        assertTransformEquals(StandardRotations.MINUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(MINUS_DIAGONAL, TWO_THIRDS_PI).toTransformMatrix());
        assertTransformEquals(StandardRotations.MINUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, MINUS_TWO_THIRDS_PI).toTransformMatrix());
    }

    @Test
    public void testAxisAngleSequenceConversion_relative() {
        for (AxisSequence axes : AxisSequence.values()) {
            checkAxisAngleSequenceToQuaternionRoundtrip(AxisReferenceFrame.RELATIVE, axes);
            checkQuaternionToAxisAngleSequenceRoundtrip(AxisReferenceFrame.RELATIVE, axes);
        }
    }

    @Test
    public void testAxisAngleSequenceConversion_absolute() {
        for (AxisSequence axes : AxisSequence.values()) {
            checkAxisAngleSequenceToQuaternionRoundtrip(AxisReferenceFrame.ABSOLUTE, axes);
            checkQuaternionToAxisAngleSequenceRoundtrip(AxisReferenceFrame.ABSOLUTE, axes);
        }
    }

    private void checkAxisAngleSequenceToQuaternionRoundtrip(AxisReferenceFrame frame, AxisSequence axes) {
        double step = 0.3;
        double angle2Start = axes.getType() == AxisSequenceType.EULER ? Geometry.ZERO_PI + 0.1 : Geometry.MINUS_HALF_PI + 0.1;
        double angle2Stop = angle2Start + Geometry.PI;

        for (double angle1 = Geometry.ZERO_PI; angle1 <= Geometry.TWO_PI; angle1 += step) {
            for (double angle2 = angle2Start; angle2 < angle2Stop; angle2 += step) {
                for (double angle3 = Geometry.ZERO_PI; angle3 <= Geometry.TWO_PI; angle3 += 0.3) {
                    // arrange
                    AxisAngleSequence angles = new AxisAngleSequence(frame, axes, angle1, angle2, angle3);

                    // act
                    QuaternionRotation q = QuaternionRotation.fromAxisAngleSequence(angles);
                    AxisAngleSequence result = q.toAxisAngleSequence(frame, axes);

                    // assert
                    Assert.assertEquals(frame, result.getReferenceFrame());
                    Assert.assertEquals(axes, result.getAxisSequence());

                    assertRadiansEquals(angle1, result.getAngle1());
                    assertRadiansEquals(angle2, result.getAngle2());
                    assertRadiansEquals(angle3, result.getAngle3());
                }
            }
        }
    }

    private void checkQuaternionToAxisAngleSequenceRoundtrip(AxisReferenceFrame frame, AxisSequence axes) {
        final double step = 0.1;

        EuclideanTestUtils.permuteSkipZero(-1, 1, 0.5, (x, y, z) -> {
            Vector3D axis = Vector3D.of(x, y, z);

            for (double angle = -Geometry.TWO_PI; angle <= Geometry.TWO_PI; angle += step) {
                // arrange
                QuaternionRotation q = QuaternionRotation.fromAxisAngle(axis, angle);

                // act
                AxisAngleSequence seq = q.toAxisAngleSequence(frame, axes);
                QuaternionRotation result = QuaternionRotation.fromAxisAngleSequence(seq);

                // assert
                checkQuaternion(result, q.getQuaternion().getW(), q.getQuaternion().getX(), q.getQuaternion().getY(), q.getQuaternion().getZ());
            }
        });
    }

    @Test
    public void testAxisAngleSequenceConversion_relative_eulerSingularities() {
        // arrange
        double[] eulerSingularities = {
                Geometry.ZERO_PI,
                Geometry.PI
        };

        double angle1 = 0.1;
        double angle2 = 0.3;

        AxisReferenceFrame frame = AxisReferenceFrame.RELATIVE;

        for (AxisSequence axes : getAxes(AxisSequenceType.EULER)) {
            for (int i=0; i<eulerSingularities.length; ++i) {

                double singularityAngle = eulerSingularities[i];

                AxisAngleSequence inputSeq = new AxisAngleSequence(frame, axes, angle1, singularityAngle, angle2);
                QuaternionRotation inputQuat = QuaternionRotation.fromAxisAngleSequence(inputSeq);

                // act
                AxisAngleSequence resultSeq = inputQuat.toAxisAngleSequence(frame, axes);
                QuaternionRotation resultQuat = QuaternionRotation.fromAxisAngleSequence(resultSeq);

                // assert
                Assert.assertEquals(frame, resultSeq.getReferenceFrame());
                Assert.assertEquals(axes, resultSeq.getAxisSequence());

                assertRadiansEquals(singularityAngle, resultSeq.getAngle2());
                assertRadiansEquals(0.0, resultSeq.getAngle3());

                checkQuaternion(resultQuat, inputQuat.getQuaternion().getW(), inputQuat.getQuaternion().getX(), inputQuat.getQuaternion().getY(), inputQuat.getQuaternion().getZ());
            }
        }
    }

    @Test
    public void testAxisAngleSequenceConversion_absolute_eulerSingularities() {
        // arrange
        double[] eulerSingularities = {
                Geometry.ZERO_PI,
                Geometry.PI
        };

        double angle1 = 0.1;
        double angle2 = 0.3;

        AxisReferenceFrame frame = AxisReferenceFrame.ABSOLUTE;

        for (AxisSequence axes : getAxes(AxisSequenceType.EULER)) {
            for (int i=0; i<eulerSingularities.length; ++i) {

                double singularityAngle = eulerSingularities[i];

                AxisAngleSequence inputSeq = new AxisAngleSequence(frame, axes, angle1, singularityAngle, angle2);
                QuaternionRotation inputQuat = QuaternionRotation.fromAxisAngleSequence(inputSeq);

                // act
                AxisAngleSequence resultSeq = inputQuat.toAxisAngleSequence(frame, axes);
                QuaternionRotation resultQuat = QuaternionRotation.fromAxisAngleSequence(resultSeq);

                // assert
                Assert.assertEquals(frame, resultSeq.getReferenceFrame());
                Assert.assertEquals(axes, resultSeq.getAxisSequence());

                assertRadiansEquals(0.0, resultSeq.getAngle1());
                assertRadiansEquals(singularityAngle, resultSeq.getAngle2());

                checkQuaternion(resultQuat, inputQuat.getQuaternion().getW(), inputQuat.getQuaternion().getX(), inputQuat.getQuaternion().getY(), inputQuat.getQuaternion().getZ());
            }
        }
    }

    @Test
    public void testAxisAngleSequenceConversion_relative_taitBryanSingularities() {
        // arrange
        double[] taitBryanSingularities = {
                Geometry.MINUS_HALF_PI,
                Geometry.HALF_PI
        };

        double angle1 = 0.1;
        double angle2 = 0.3;

        AxisReferenceFrame frame = AxisReferenceFrame.RELATIVE;

        for (AxisSequence axes : getAxes(AxisSequenceType.TAIT_BRYAN)) {
            for (int i=0; i<taitBryanSingularities.length; ++i) {

                double singularityAngle = taitBryanSingularities[i];

                AxisAngleSequence inputSeq = new AxisAngleSequence(frame, axes, angle1, singularityAngle, angle2);
                QuaternionRotation inputQuat = QuaternionRotation.fromAxisAngleSequence(inputSeq);

                // act
                AxisAngleSequence resultSeq = inputQuat.toAxisAngleSequence(frame, axes);
                QuaternionRotation resultQuat = QuaternionRotation.fromAxisAngleSequence(resultSeq);

                // assert
                Assert.assertEquals(frame, resultSeq.getReferenceFrame());
                Assert.assertEquals(axes, resultSeq.getAxisSequence());

                assertRadiansEquals(singularityAngle, resultSeq.getAngle2());
                assertRadiansEquals(0.0, resultSeq.getAngle3());

                checkQuaternion(resultQuat, inputQuat.getQuaternion().getW(), inputQuat.getQuaternion().getX(), inputQuat.getQuaternion().getY(), inputQuat.getQuaternion().getZ());
            }
        }
    }

    @Test
    public void testAxisAngleSequenceConversion_absolute_taitBryanSingularities() {
        // arrange
        double[] taitBryanSingularities = {
                Geometry.MINUS_HALF_PI,
                Geometry.HALF_PI
        };

        double angle1 = 0.1;
        double angle2 = 0.3;

        AxisReferenceFrame frame = AxisReferenceFrame.ABSOLUTE;

        for (AxisSequence axes : getAxes(AxisSequenceType.TAIT_BRYAN)) {
            for (int i=0; i<taitBryanSingularities.length; ++i) {

                double singularityAngle = taitBryanSingularities[i];

                AxisAngleSequence inputSeq = new AxisAngleSequence(frame, axes, angle1, singularityAngle, angle2);
                QuaternionRotation inputQuat = QuaternionRotation.fromAxisAngleSequence(inputSeq);

                // act
                AxisAngleSequence resultSeq = inputQuat.toAxisAngleSequence(frame, axes);
                QuaternionRotation resultQuat = QuaternionRotation.fromAxisAngleSequence(resultSeq);

                // assert
                Assert.assertEquals(frame, resultSeq.getReferenceFrame());
                Assert.assertEquals(axes, resultSeq.getAxisSequence());

                assertRadiansEquals(0.0, resultSeq.getAngle1());
                assertRadiansEquals(singularityAngle, resultSeq.getAngle2());

                checkQuaternion(resultQuat, inputQuat.getQuaternion().getW(), inputQuat.getQuaternion().getX(), inputQuat.getQuaternion().getY(), inputQuat.getQuaternion().getZ());
            }
        }
    }

    private List<AxisSequence> getAxes(final AxisSequenceType type) {
        return Arrays.asList(AxisSequence.values()).stream()
                .filter(a -> type.equals(a.getType()))
                .collect(Collectors.toList());
    }

    @Test
    public void testToAxisAngleSequence_invalidArgs() {
        // arrange
        QuaternionRotation q = QuaternionRotation.identity();

        // act/assert
        GeometryTestUtils.assertThrows(() -> q.toAxisAngleSequence(null, AxisSequence.XYZ), IllegalArgumentException.class);
        GeometryTestUtils.assertThrows(() -> q.toAxisAngleSequence(AxisReferenceFrame.ABSOLUTE, null), IllegalArgumentException.class);
    }

    @Test
    public void testToRelativeAxisAngleSequence() {
        // arrange
        QuaternionRotation q = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, TWO_THIRDS_PI);

        // act
        AxisAngleSequence seq = q.toRelativeAxisAngleSequence(AxisSequence.YZX);

        // assert
        Assert.assertEquals(AxisReferenceFrame.RELATIVE, seq.getReferenceFrame());
        Assert.assertEquals(AxisSequence.YZX, seq.getAxisSequence());
        Assert.assertEquals(Geometry.HALF_PI, seq.getAngle1(), EPS);
        Assert.assertEquals(Geometry.HALF_PI, seq.getAngle2(), EPS);
        Assert.assertEquals(0, seq.getAngle3(), EPS);
    }

    @Test
    public void testToAbsoluteAxisAngleSequence() {
        // arrange
        QuaternionRotation q = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, TWO_THIRDS_PI);

        // act
        AxisAngleSequence seq = q.toAbsoluteAxisAngleSequence(AxisSequence.YZX);

        // assert
        Assert.assertEquals(AxisReferenceFrame.ABSOLUTE, seq.getReferenceFrame());
        Assert.assertEquals(AxisSequence.YZX, seq.getAxisSequence());
        Assert.assertEquals(Geometry.HALF_PI, seq.getAngle1(), EPS);
        Assert.assertEquals(0, seq.getAngle2(), EPS);
        Assert.assertEquals(Geometry.HALF_PI, seq.getAngle3(), EPS);
    }

    @Test
    public void testHashCode() {
        // arrange
        double delta = 100 * Precision.EPSILON;
        QuaternionRotation q1 = QuaternionRotation.of(1, 2, 3, 4);
        QuaternionRotation q2 = QuaternionRotation.of(1, 2, 3, 4);

        // act/assert
        Assert.assertEquals(q1.hashCode(), q2.hashCode());

        Assert.assertNotEquals(q1.hashCode(), QuaternionRotation.of(1 + delta, 2, 3, 4).hashCode());
        Assert.assertNotEquals(q1.hashCode(), QuaternionRotation.of(1, 2 + delta, 3, 4).hashCode());
        Assert.assertNotEquals(q1.hashCode(), QuaternionRotation.of(1, 2, 3 + delta, 4).hashCode());
        Assert.assertNotEquals(q1.hashCode(), QuaternionRotation.of(1, 2, 3, 4 + delta).hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        double delta = 100 * Precision.EPSILON;
        QuaternionRotation q1 = QuaternionRotation.of(1, 2, 3, 4);
        QuaternionRotation q2 = QuaternionRotation.of(1, 2, 3, 4);

        // act/assert
        Assert.assertFalse(q1.equals(null));
        Assert.assertFalse(q1.equals(new Object()));

        Assert.assertTrue(q1.equals(q1));
        Assert.assertTrue(q1.equals(q2));

        Assert.assertFalse(q1.equals(QuaternionRotation.of(-1, -2, -3, 4)));
        Assert.assertFalse(q1.equals(QuaternionRotation.of(1, 2, 3, -4)));

        Assert.assertFalse(q1.equals(QuaternionRotation.of(1 + delta, 2, 3, 4)));
        Assert.assertFalse(q1.equals(QuaternionRotation.of(1, 2 + delta, 3, 4)));
        Assert.assertFalse(q1.equals(QuaternionRotation.of(1, 2, 3 + delta, 4)));
        Assert.assertFalse(q1.equals(QuaternionRotation.of(1, 2, 3, 4 + delta)));
    }

    @Test
    public void testToString() {
        // arrange
        QuaternionRotation q = QuaternionRotation.of(1, 2, 3, 4);
        Quaternion qField = q.getQuaternion();

        // assert
        Assert.assertEquals(qField.toString(), q.toString());
    }

    @Test
    public void testCreateVectorRotation_simple() {
        // arrange
        Vector3D u1 = Vector3D.PLUS_X;
        Vector3D u2 = Vector3D.PLUS_Y;

        // act
        QuaternionRotation q = QuaternionRotation.createVectorRotation(u1, u2);

        // assert
        double val = Math.sqrt(2) * 0.5;

        checkQuaternion(q, val, 0, 0, val);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.PLUS_Z, q.getAxis(), EPS);
        Assert.assertEquals(Geometry.HALF_PI, q.getAngle(), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(u2, q.apply(u1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(u1, q.inverse().apply(u2), EPS);
    }

    @Test
    public void testCreateVectorRotation_identity() {
        // arrange
        Vector3D u1 = Vector3D.of(0, 2, 0);
        Vector3D u2 = u1;

        // act
        QuaternionRotation q = QuaternionRotation.createVectorRotation(u1, u2);

        // assert
        checkQuaternion(q, 1, 0, 0, 0);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.PLUS_X, q.getAxis(), EPS);
        Assert.assertEquals(Geometry.ZERO_PI, q.getAngle(), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 2, 0), q.apply(u1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 2, 0), q.inverse().apply(u2), EPS);
    }

    @Test
    public void testCreateVectorRotation_parallel() {
        // arrange
        Vector3D u1 = Vector3D.of(0, 2, 0);
        Vector3D u2 = Vector3D.of(0, 3, 0);

        // act
        QuaternionRotation q = QuaternionRotation.createVectorRotation(u1, u2);

        // assert
        checkQuaternion(q, 1, 0, 0, 0);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.PLUS_X, q.getAxis(), EPS);
        Assert.assertEquals(Geometry.ZERO_PI, q.getAngle(), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 2, 0), q.apply(u1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 3, 0), q.inverse().apply(u2), EPS);
    }

    @Test
    public void testCreateVectorRotation_antiparallel() {
        // arrange
        Vector3D u1 = Vector3D.of(0, 2, 0);
        Vector3D u2 = Vector3D.of(0, -3, 0);

        // act
        QuaternionRotation q = QuaternionRotation.createVectorRotation(u1, u2);

        // assert
        Vector3D axis = q.getAxis();
        Assert.assertEquals(0.0, axis.dot(u1), EPS);
        Assert.assertEquals(0.0, axis.dot(u2), EPS);
        Assert.assertEquals(Geometry.PI, q.getAngle(), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, -2, 0), q.apply(u1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 3, 0), q.inverse().apply(u2), EPS);
    }

    @Test
    public void testCreateVectorRotation_permute() {
        EuclideanTestUtils.permuteSkipZero(-5, 5, 0.1, (x, y, z) -> {
            // arrange
            Vector3D u1 = Vector3D.of(x, y, z);
            Vector3D u2 = PLUS_DIAGONAL;

            // act
            QuaternionRotation q = QuaternionRotation.createVectorRotation(u1, u2);

            // assert
            Assert.assertEquals(0.0, q.apply(u1).angle(u2), EPS);
            Assert.assertEquals(0.0, q.inverse().apply(u2).angle(u1), EPS);

            double angle = q.getAngle();
            Assert.assertTrue(angle >= Geometry.ZERO_PI);
            Assert.assertTrue(angle <= Geometry.PI);
        });
    }

    @Test
    public void testCreateVectorRotation_invalidArgs() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> QuaternionRotation.createVectorRotation(Vector3D.ZERO, Vector3D.PLUS_X),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> QuaternionRotation.createVectorRotation(Vector3D.PLUS_X, Vector3D.ZERO),
                IllegalNormException.class);

        GeometryTestUtils.assertThrows(() -> QuaternionRotation.createVectorRotation(Vector3D.NaN, Vector3D.PLUS_X),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> QuaternionRotation.createVectorRotation(Vector3D.PLUS_X, Vector3D.POSITIVE_INFINITY),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> QuaternionRotation.createVectorRotation(Vector3D.PLUS_X, Vector3D.NEGATIVE_INFINITY),
                IllegalNormException.class);
    }

    @Test
    public void testCreateBasisRotation_simple() {
        // arrange
        Vector3D u1 = Vector3D.PLUS_X;
        Vector3D u2 = Vector3D.PLUS_Y;

        Vector3D v1 = Vector3D.PLUS_Y;
        Vector3D v2 = Vector3D.MINUS_X;

        // act
        QuaternionRotation q = QuaternionRotation.createBasisRotation(u1, u2, v1, v2);

        // assert
        QuaternionRotation qInv = q.inverse();

        EuclideanTestUtils.assertCoordinatesEqual(v1, q.apply(u1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(v2, q.apply(u2), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(u1, qInv.apply(v1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(u2, qInv.apply(v2), EPS);

        assertRotationEquals(StandardRotations.PLUS_Z_HALF_PI, q);
    }

    @Test
    public void testCreateBasisRotation_diagonalAxis() {
        // arrange
        Vector3D u1 = Vector3D.PLUS_X;
        Vector3D u2 = Vector3D.PLUS_Y;

        Vector3D v1 = Vector3D.PLUS_Y;
        Vector3D v2 = Vector3D.PLUS_Z;

        // act
        QuaternionRotation q = QuaternionRotation.createBasisRotation(u1, u2, v1, v2);

        // assert
        QuaternionRotation qInv = q.inverse();

        EuclideanTestUtils.assertCoordinatesEqual(v1, q.apply(u1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(v2, q.apply(u2), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(u1, qInv.apply(v1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(u2, qInv.apply(v2), EPS);

        assertRotationEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, q);
        assertRotationEquals(StandardRotations.MINUS_DIAGONAL_TWO_THIRDS_PI, q.inverse());
    }

    @Test
    public void testCreateBasisRotation_identity() {
        // arrange
        Vector3D u1 = Vector3D.PLUS_X;
        Vector3D u2 = Vector3D.PLUS_Y;

        Vector3D v1 = u1;
        Vector3D v2 = u2;

        // act
        QuaternionRotation q = QuaternionRotation.createBasisRotation(u1, u2, v1, v2);

        // assert
        QuaternionRotation qInv = q.inverse();

        EuclideanTestUtils.assertCoordinatesEqual(v1, q.apply(u1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(v2, q.apply(u2), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(u1, qInv.apply(v1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(u2, qInv.apply(v2), EPS);

        assertRotationEquals(StandardRotations.IDENTITY, q);
    }

    @Test
    public void testCreateBasisRotation_equivalentBases() {
        // arrange
        Vector3D u1 = Vector3D.of(2, 0, 0);
        Vector3D u2 = Vector3D.of(0, 3, 0);

        Vector3D v1 = Vector3D.of(4, 0, 0);
        Vector3D v2 = Vector3D.of(0, 5, 0);

        // act
        QuaternionRotation q = QuaternionRotation.createBasisRotation(u1, u2, v1, v2);

        // assert
        QuaternionRotation qInv = q.inverse();

        EuclideanTestUtils.assertCoordinatesEqual(u1, q.apply(u1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(u2, q.apply(u2), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(v1, qInv.apply(v1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(v2, qInv.apply(v2), EPS);

        assertRotationEquals(StandardRotations.IDENTITY, q);
    }

    @Test
    public void testCreateBasisRotation_nonOrthogonalVectors() {
        // arrange
        Vector3D u1 = Vector3D.of(2, 0, 0);
        Vector3D u2 = Vector3D.of(1, 0.5, 0);

        Vector3D v1 = Vector3D.of(0, 1.5, 0);
        Vector3D v2 = Vector3D.of(-1, 1.5, 0);

        // act
        QuaternionRotation q = QuaternionRotation.createBasisRotation(u1, u2, v1, v2);

        // assert
        QuaternionRotation qInv = q.inverse();

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 2, 0), q.apply(u1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-0.5, 1, 0), q.apply(u2), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1.5, 0, 0), qInv.apply(v1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1.5, 1, 0), qInv.apply(v2), EPS);

        assertRotationEquals(StandardRotations.PLUS_Z_HALF_PI, q);
    }

    @Test
    public void testCreateBasisRotation_permute() {
        // arrange
        Vector3D u1 = Vector3D.of(1, 2, 3);
        Vector3D u2 = Vector3D.of(0, 4, 0);

        Vector3D u1Dir = u1.normalize();
        Vector3D u2Dir = u1Dir.orthogonal(u2);

        EuclideanTestUtils.permuteSkipZero(-5, 5, 0.2, (x, y, z) -> {
            Vector3D v1 = Vector3D.of(x, y, z);
            Vector3D v2 = v1.orthogonal();

            Vector3D v1Dir = v1.normalize();
            Vector3D v2Dir = v2.normalize();

            // act
            QuaternionRotation q = QuaternionRotation.createBasisRotation(u1, u2, v1, v2);
            QuaternionRotation qInv = q.inverse();

            // assert
            EuclideanTestUtils.assertCoordinatesEqual(v1Dir, q.apply(u1Dir), EPS);
            EuclideanTestUtils.assertCoordinatesEqual(v2Dir, q.apply(u2Dir), EPS);

            EuclideanTestUtils.assertCoordinatesEqual(u1Dir, qInv.apply(v1Dir), EPS);
            EuclideanTestUtils.assertCoordinatesEqual(u2Dir, qInv.apply(v2Dir), EPS);

            double angle = q.getAngle();
            Assert.assertTrue(angle >= Geometry.ZERO_PI);
            Assert.assertTrue(angle <= Geometry.PI);

            Vector3D transformedX = q.apply(Vector3D.PLUS_X);
            Vector3D transformedY = q.apply(Vector3D.PLUS_Y);
            Vector3D transformedZ = q.apply(Vector3D.PLUS_Z);

            Assert.assertEquals(1.0, transformedX.norm(), EPS);
            Assert.assertEquals(1.0, transformedY.norm(), EPS);
            Assert.assertEquals(1.0, transformedZ.norm(), EPS);

            Assert.assertEquals(0.0, transformedX.dot(transformedY), EPS);
            Assert.assertEquals(0.0, transformedX.dot(transformedZ), EPS);
            Assert.assertEquals(0.0, transformedY.dot(transformedZ), EPS);

            EuclideanTestUtils.assertCoordinatesEqual(transformedZ.normalize(),
                    transformedX.normalize().cross(transformedY.normalize()), EPS);

            Assert.assertEquals(1.0, q.getQuaternion().norm(), EPS);
        });
    }

    @Test
    public void testCreateBasisRotation_invalidArgs() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> QuaternionRotation.createBasisRotation(
                Vector3D.ZERO, Vector3D.PLUS_Y, Vector3D.PLUS_Y, Vector3D.MINUS_X),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> QuaternionRotation.createBasisRotation(
                Vector3D.PLUS_X, Vector3D.NaN, Vector3D.PLUS_Y, Vector3D.MINUS_X),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> QuaternionRotation.createBasisRotation(
                Vector3D.PLUS_X, Vector3D.PLUS_Y, Vector3D.POSITIVE_INFINITY, Vector3D.MINUS_X),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> QuaternionRotation.createBasisRotation(
                Vector3D.PLUS_X, Vector3D.PLUS_Y, Vector3D.PLUS_Y, Vector3D.NEGATIVE_INFINITY),
                IllegalNormException.class);

        GeometryTestUtils.assertThrows(() -> QuaternionRotation.createBasisRotation(
                Vector3D.PLUS_X, Vector3D.PLUS_X, Vector3D.PLUS_Y, Vector3D.MINUS_X),
                IllegalNormException.class);

        GeometryTestUtils.assertThrows(() -> QuaternionRotation.createBasisRotation(
                Vector3D.PLUS_X, Vector3D.PLUS_Y, Vector3D.PLUS_Y, Vector3D.MINUS_Y),
                IllegalNormException.class);
    }

    @Test
    public void testFromEulerAngles_identity() {
        for (AxisSequence axes : AxisSequence.values()) {

            // act/assert
            assertRotationEquals(StandardRotations.IDENTITY,
                    QuaternionRotation.fromAxisAngleSequence(AxisAngleSequence.createRelative(axes, 0, 0, 0)));
            assertRotationEquals(StandardRotations.IDENTITY,
                    QuaternionRotation.fromAxisAngleSequence(AxisAngleSequence.createRelative(axes, Geometry.TWO_PI, Geometry.TWO_PI, Geometry.TWO_PI)));

            assertRotationEquals(StandardRotations.IDENTITY,
                    QuaternionRotation.fromAxisAngleSequence(AxisAngleSequence.createAbsolute(axes, 0, 0, 0)));
            assertRotationEquals(StandardRotations.IDENTITY,
                    QuaternionRotation.fromAxisAngleSequence(AxisAngleSequence.createAbsolute(axes, Geometry.TWO_PI, Geometry.TWO_PI, Geometry.TWO_PI)));
        }
    }

    @Test
    public void testFromEulerAngles_relative() {

        // --- act/assert

        // XYZ
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.XYZ, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.XYZ, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.XYZ, 0, 0, Geometry.HALF_PI);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.XYZ, Geometry.HALF_PI, Geometry.HALF_PI, 0);

        // XZY
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.XZY, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.XZY, 0, 0, Geometry.HALF_PI);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.XZY, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.XZY, Geometry.HALF_PI, 0, Geometry.HALF_PI);

        // YXZ
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.YXZ, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.YXZ, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.YXZ, 0, 0, Geometry.HALF_PI);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.YXZ, Geometry.HALF_PI, 0, Geometry.HALF_PI);

        // YZX
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.YZX, 0, 0, Geometry.HALF_PI);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.YZX, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.YZX, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.YZX, Geometry.HALF_PI, Geometry.HALF_PI, 0);

        // ZXY
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.YZX, 0, 0, Geometry.HALF_PI);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.YZX, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.YZX, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.YZX, Geometry.HALF_PI, Geometry.HALF_PI, 0);

        // ZYX
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.ZYX, 0, 0, Geometry.HALF_PI);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.ZYX, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.ZYX, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.ZYX, Geometry.HALF_PI, 0, Geometry.HALF_PI);

        // XYX
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.XYX, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.XYX, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.XYX, Geometry.HALF_PI, Geometry.HALF_PI, Geometry.MINUS_HALF_PI);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.XYX, Geometry.HALF_PI, Geometry.HALF_PI, 0);

        // XZX
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.XZX, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.XZX, Geometry.MINUS_HALF_PI, Geometry.HALF_PI, Geometry.HALF_PI);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.XZX, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.XZX, 0, Geometry.HALF_PI, Geometry.HALF_PI);

        // YXY
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.YXY, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.YXY, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.YXY, Geometry.MINUS_HALF_PI, Geometry.HALF_PI, Geometry.HALF_PI);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.YXY, 0, Geometry.HALF_PI, Geometry.HALF_PI);

        // YZY
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.YZY, Geometry.MINUS_HALF_PI, Geometry.MINUS_HALF_PI, Geometry.HALF_PI);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.YZY, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.YZY, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.YZY, Geometry.HALF_PI, Geometry.HALF_PI, 0);

        // ZXZ
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.ZXZ, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.ZXZ, Geometry.HALF_PI, Geometry.HALF_PI, Geometry.MINUS_HALF_PI);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.ZXZ, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.ZXZ, Geometry.HALF_PI, Geometry.HALF_PI, 0);

        // ZYZ
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.ZYZ, Geometry.HALF_PI, Geometry.MINUS_HALF_PI, Geometry.MINUS_HALF_PI);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.ZYZ, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.ZYZ, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.ZYZ, 0, Geometry.HALF_PI, Geometry.HALF_PI);
    }

    /** Helper method for verifying that a {@link RelativeEulerAngles} instance constructed with the given arguments
     * is correctly converted to a QuaternionRotation that matches the given operator.
     * @param rotation
     * @param axes
     * @param angle1
     * @param angle2
     * @param angle3
     */
    private void checkFromAxisAngleSequenceRelative(UnaryOperator<Vector3D> rotation, AxisSequence axes, double angle1, double angle2, double angle3) {
        AxisAngleSequence angles = AxisAngleSequence.createRelative(axes, angle1, angle2, angle3);

        assertRotationEquals(rotation, QuaternionRotation.fromAxisAngleSequence(angles));
    }

    @Test
    public void testFromEulerAngles_absolute() {

        // --- act/assert

        // XYZ
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.XYZ, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.XYZ, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.XYZ, 0, 0, Geometry.HALF_PI);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.XYZ, Geometry.HALF_PI, 0, Geometry.HALF_PI);

        // XZY
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.XZY, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.XZY, 0, 0, Geometry.HALF_PI);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.XZY, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.XZY, Geometry.HALF_PI, Geometry.HALF_PI, 0);

        // YXZ
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.YXZ, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.YXZ, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.YXZ, 0, 0, Geometry.HALF_PI);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.YXZ, Geometry.HALF_PI, Geometry.HALF_PI, 0);

        // YZX
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.YZX, 0, 0, Geometry.HALF_PI);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.YZX, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.YZX, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.YZX, Geometry.HALF_PI, 0, Geometry.HALF_PI);

        // ZXY
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.YZX, 0, 0, Geometry.HALF_PI);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.YZX, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.YZX, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.YZX, Geometry.HALF_PI, 0, Geometry.HALF_PI);

        // ZYX
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.ZYX, 0, 0, Geometry.HALF_PI);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.ZYX, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.ZYX, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.ZYX, Geometry.HALF_PI, Geometry.HALF_PI, 0);

        // XYX
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.XYX, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.XYX, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.XYX, Geometry.HALF_PI, Geometry.MINUS_HALF_PI, Geometry.MINUS_HALF_PI);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.XYX, 0, Geometry.HALF_PI, Geometry.HALF_PI);

        // XZX
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.XZX, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.XZX, Geometry.MINUS_HALF_PI, Geometry.MINUS_HALF_PI, Geometry.HALF_PI);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.XZX, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.XZX, Geometry.HALF_PI, Geometry.HALF_PI, 0);

        // YXY
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.YXY, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.YXY, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.YXY, Geometry.MINUS_HALF_PI, Geometry.MINUS_HALF_PI, Geometry.HALF_PI);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.YXY, Geometry.HALF_PI, Geometry.HALF_PI, 0);

        // YZY
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.YZY, Geometry.MINUS_HALF_PI, Geometry.HALF_PI, Geometry.HALF_PI);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.YZY, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.YZY, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.YZY, 0, Geometry.HALF_PI, Geometry.HALF_PI);

        // ZXZ
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.ZXZ, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.ZXZ, Geometry.MINUS_HALF_PI, Geometry.HALF_PI, Geometry.HALF_PI);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.ZXZ, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.ZXZ, 0, Geometry.HALF_PI, Geometry.HALF_PI);

        // ZYZ
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.ZYZ, Geometry.HALF_PI, Geometry.HALF_PI, Geometry.MINUS_HALF_PI);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.ZYZ, 0, Geometry.HALF_PI, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.ZYZ, Geometry.HALF_PI, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.ZYZ, Geometry.HALF_PI, Geometry.HALF_PI, 0);
    }

    /** Helper method for verifying that an {@link AbsoluteEulerAngles} instance constructed with the given arguments
     * is correctly converted to a QuaternionRotation that matches the given operator.
     * @param rotation
     * @param axes
     * @param angle1
     * @param angle2
     * @param angle3
     */
    private void checkFromAxisAngleSequenceAbsolute(UnaryOperator<Vector3D> rotation, AxisSequence axes, double angle1, double angle2, double angle3) {
        AxisAngleSequence angles = AxisAngleSequence.createAbsolute(axes, angle1, angle2, angle3);

        assertRotationEquals(rotation, QuaternionRotation.fromAxisAngleSequence(angles));
    }

    private static void checkQuaternion(QuaternionRotation qrot, double w, double x, double y, double z) {
        String msg = "Expected"
                + " quaternion to equal " + SimpleTupleFormat.getDefault().format(w, x, y, z) + " but was " + qrot;

        Assert.assertEquals(msg, w, qrot.getQuaternion().getW(), EPS);
        Assert.assertEquals(msg, x, qrot.getQuaternion().getX(), EPS);
        Assert.assertEquals(msg, y, qrot.getQuaternion().getY(), EPS);
        Assert.assertEquals(msg, z, qrot.getQuaternion().getZ(), EPS);

        Quaternion q = qrot.getQuaternion();
        Assert.assertEquals(msg, w, q.getW(), EPS);
        Assert.assertEquals(msg, x, q.getX(), EPS);
        Assert.assertEquals(msg, y, q.getY(), EPS);
        Assert.assertEquals(msg, z, q.getZ(), EPS);

        Assert.assertTrue(qrot.preservesOrientation());
    }

    private static void checkVector(Vector3D v, double x, double y, double z) {
        String msg = "Expected vector to equal " + SimpleTupleFormat.getDefault().format(x, y, z) + " but was " + v;

        Assert.assertEquals(msg, x, v.getX(), EPS);
        Assert.assertEquals(msg, y, v.getY(), EPS);
        Assert.assertEquals(msg, z, v.getZ(), EPS);
    }

    /** Assert that the two given radian values are equivalent.
     * @param expected
     * @param actual
     */
    private static void assertRadiansEquals(double expected, double actual) {
        double diff = PlaneAngleRadians.normalizeBetweenMinusPiAndPi(expected - actual);
        String msg = "Expected " + actual + " radians to be equivalent to " + expected + " radians; difference is " + diff;

        Assert.assertTrue(msg, Math.abs(diff) < 1e-6);
    }

    /**
     * Assert that {@code rotation} returns the same outputs as {@code expected} for a range of vector inputs.
     * @param expected
     * @param rotation
     */
    private static void assertRotationEquals(UnaryOperator<Vector3D> expected, QuaternionRotation rotation) {
        assertFnEquals(expected, rotation::apply);
    }

    /**
     * Assert that {@code transform} returns the same outputs as {@code expected} for a range of vector inputs.
     * @param expected
     * @param transform
     */
    private static void assertTransformEquals(UnaryOperator<Vector3D> expected, AffineTransformMatrix3D transform) {
        assertFnEquals(expected, transform::apply);
    }

    /**
     * Assert that {@code actual} returns the same output as {@code expected} for a range of inputs.
     * @param expectedFn
     * @param actualFn
     */
    private static void assertFnEquals(final UnaryOperator<Vector3D> expectedFn, final UnaryOperator<Vector3D> actualFn) {
        EuclideanTestUtils.permute(-2, 2, 0.25, (x, y, z) -> {
            Vector3D input = Vector3D.of(x, y, z);

            Vector3D expected = expectedFn.apply(input);
            Vector3D actual = actualFn.apply(input);

            String msg = "Expected vector " + input + " to be transformed to " + expected + " but was " + actual;

            Assert.assertEquals(msg, expected.getX(), actual.getX(), EPS);
            Assert.assertEquals(msg, expected.getY(), actual.getY(), EPS);
            Assert.assertEquals(msg, expected.getZ(), actual.getZ(), EPS);
        });
    }
}
