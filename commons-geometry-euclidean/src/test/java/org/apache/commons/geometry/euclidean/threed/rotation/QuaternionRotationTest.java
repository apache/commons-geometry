/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.euclidean.threed.rotation;

import java.util.List;
import java.util.function.DoubleFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.internal.SimpleTupleFormat;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.apache.commons.numbers.quaternion.Quaternion;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QuaternionRotationTest {

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

    private static final double TWO_THIRDS_PI = 2.0 * Math.PI / 3.0;
    private static final double MINUS_TWO_THIRDS_PI = -TWO_THIRDS_PI;

    @Test
    void testOf_quaternion() {
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
    void testOf_quaternion_illegalNorm() {
        // act/assert
        Assertions.assertThrows(IllegalStateException.class, () -> QuaternionRotation.of(Quaternion.of(0, 0, 0, 0)));
        Assertions.assertThrows(IllegalStateException.class, () -> QuaternionRotation.of(Quaternion.of(1, 1, 1, Double.NaN)));
        Assertions.assertThrows(IllegalStateException.class, () -> QuaternionRotation.of(Quaternion.of(1, 1, Double.POSITIVE_INFINITY, 1)));
        Assertions.assertThrows(IllegalStateException.class, () -> QuaternionRotation.of(Quaternion.of(1, Double.NEGATIVE_INFINITY, 1, 1)));
        Assertions.assertThrows(IllegalStateException.class, () -> QuaternionRotation.of(Quaternion.of(Double.NaN, 1, 1, 1)));
    }

    @Test
    void testOf_components() {
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
    void testOf_components_illegalNorm() {
        // act/assert
        Assertions.assertThrows(IllegalStateException.class, () -> QuaternionRotation.of(0, 0, 0, 0));
        Assertions.assertThrows(IllegalStateException.class, () -> QuaternionRotation.of(1, 1, 1, Double.NaN));
        Assertions.assertThrows(IllegalStateException.class, () -> QuaternionRotation.of(1, 1, Double.POSITIVE_INFINITY, 1));
        Assertions.assertThrows(IllegalStateException.class, () -> QuaternionRotation.of(1, Double.NEGATIVE_INFINITY, 1, 1));
        Assertions.assertThrows(IllegalStateException.class, () -> QuaternionRotation.of(Double.NaN, 1, 1, 1));
    }

    @Test
    void testIdentity() {
        // act
        final QuaternionRotation q = QuaternionRotation.identity();

        // assert
        assertRotationEquals(StandardRotations.IDENTITY, q);
    }

    @Test
    void testIdentity_axis() {
        // arrange
        final QuaternionRotation q = QuaternionRotation.identity();

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_X, q.getAxis(), EPS);
    }

    @Test
    void testGetAxis() {
        // act/assert
        checkVector(QuaternionRotation.of(0, 1, 0, 0).getAxis(), 1, 0, 0);
        checkVector(QuaternionRotation.of(0, -1, 0, 0).getAxis(), -1, 0, 0);

        checkVector(QuaternionRotation.of(0, 0, 1, 0).getAxis(), 0, 1, 0);
        checkVector(QuaternionRotation.of(0, 0, -1, 0).getAxis(), 0, -1, 0);

        checkVector(QuaternionRotation.of(0, 0, 0, 1).getAxis(), 0, 0, 1);
        checkVector(QuaternionRotation.of(0, 0, 0, -1).getAxis(), 0, 0, -1);
    }

    @Test
    void testGetAxis_noAxis() {
        // arrange
        final QuaternionRotation rot = QuaternionRotation.of(1, 0, 0, 0);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_X, rot.getAxis(), EPS);
    }

    @Test
    void testGetAxis_matchesAxisAngleConstruction() {
        EuclideanTestUtils.permuteSkipZero(-5, 5, 1, (x, y, z) -> {
            // arrange
            final Vector3D vec = Vector3D.of(x, y, z);
            final Vector3D norm = vec.normalize();

            // act/assert

            // positive angle results in the axis being the normalized input axis
            EuclideanTestUtils.assertCoordinatesEqual(norm,
                    QuaternionRotation.fromAxisAngle(vec, Angle.PI_OVER_TWO).getAxis(), EPS);

            // negative angle results in the axis being the negated normalized input axis
            EuclideanTestUtils.assertCoordinatesEqual(norm,
                    QuaternionRotation.fromAxisAngle(vec.negate(), -Angle.PI_OVER_TWO).getAxis(), EPS);
        });
    }

    @Test
    void testGetAngle() {
        // act/assert
        Assertions.assertEquals(0.0, QuaternionRotation.of(1, 0, 0, 0).getAngle(), EPS);
        Assertions.assertEquals(0.0, QuaternionRotation.of(-1, 0, 0, 0).getAngle(), EPS);

        Assertions.assertEquals(Angle.PI_OVER_TWO, QuaternionRotation.of(1, 0, 0, 1).getAngle(), EPS);
        Assertions.assertEquals(Angle.PI_OVER_TWO, QuaternionRotation.of(-1, 0, 0, -1).getAngle(), EPS);

        Assertions.assertEquals(Math.PI  * 2.0 / 3.0, QuaternionRotation.of(1, 1, 1, 1).getAngle(), EPS);

        Assertions.assertEquals(Math.PI, QuaternionRotation.of(0, 0, 0, 1).getAngle(), EPS);
    }

    @Test
    void testGetAngle_matchesAxisAngleConstruction() {
        for (double theta = -2 * Math.PI; theta <= 2 * Math.PI; theta += 0.1) {
            // arrange
            final QuaternionRotation rot = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, theta);

            // act
            final double angle = rot.getAngle();

            // assert
            // make sure that we're in the [0, pi] range
            Assertions.assertTrue(angle >= 0.0);
            Assertions.assertTrue(angle <= Math.PI);

            double expected = Angle.Rad.WITHIN_MINUS_PI_AND_PI.applyAsDouble(theta);
            if (PLUS_DIAGONAL.dot(rot.getAxis()) < 0) {
                // if the axis ended up being flipped, then negate the expected angle
                expected *= -1;
            }

            Assertions.assertEquals(expected, angle, EPS);
        }
    }

    @Test
    void testFromAxisAngle_apply() {
        // act/assert

        // --- x axes
        assertRotationEquals(StandardRotations.IDENTITY, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, 0.0));

        assertRotationEquals(StandardRotations.PLUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, Angle.PI_OVER_TWO));
        assertRotationEquals(StandardRotations.PLUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_X_DIR, -Angle.PI_OVER_TWO));

        assertRotationEquals(StandardRotations.MINUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_X_DIR, Angle.PI_OVER_TWO));
        assertRotationEquals(StandardRotations.MINUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, -Angle.PI_OVER_TWO));

        assertRotationEquals(StandardRotations.X_PI, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, Math.PI));
        assertRotationEquals(StandardRotations.X_PI, QuaternionRotation.fromAxisAngle(MINUS_X_DIR, Math.PI));

        // --- y axes
        assertRotationEquals(StandardRotations.IDENTITY, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, 0.0));

        assertRotationEquals(StandardRotations.PLUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, Angle.PI_OVER_TWO));
        assertRotationEquals(StandardRotations.PLUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Y_DIR, -Angle.PI_OVER_TWO));

        assertRotationEquals(StandardRotations.MINUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Y_DIR, Angle.PI_OVER_TWO));
        assertRotationEquals(StandardRotations.MINUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, -Angle.PI_OVER_TWO));

        assertRotationEquals(StandardRotations.Y_PI, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, Math.PI));
        assertRotationEquals(StandardRotations.Y_PI, QuaternionRotation.fromAxisAngle(MINUS_Y_DIR, Math.PI));

        // --- z axes
        assertRotationEquals(StandardRotations.IDENTITY, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, 0.0));

        assertRotationEquals(StandardRotations.PLUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, Angle.PI_OVER_TWO));
        assertRotationEquals(StandardRotations.PLUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Z_DIR, -Angle.PI_OVER_TWO));

        assertRotationEquals(StandardRotations.MINUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Z_DIR, Angle.PI_OVER_TWO));
        assertRotationEquals(StandardRotations.MINUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, -Angle.PI_OVER_TWO));

        assertRotationEquals(StandardRotations.Z_PI, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, Math.PI));
        assertRotationEquals(StandardRotations.Z_PI, QuaternionRotation.fromAxisAngle(MINUS_Z_DIR, Math.PI));

        // --- diagonal
        assertRotationEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, TWO_THIRDS_PI));
        assertRotationEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(MINUS_DIAGONAL, MINUS_TWO_THIRDS_PI));

        assertRotationEquals(StandardRotations.MINUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(MINUS_DIAGONAL, TWO_THIRDS_PI));
        assertRotationEquals(StandardRotations.MINUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, MINUS_TWO_THIRDS_PI));
    }

    @Test
    void testFromAxisAngle_invalidAxisNorm() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> QuaternionRotation.fromAxisAngle(Vector3D.ZERO, Angle.PI_OVER_TWO));
        Assertions.assertThrows(IllegalArgumentException.class, () -> QuaternionRotation.fromAxisAngle(Vector3D.NaN, Angle.PI_OVER_TWO));
        Assertions.assertThrows(IllegalArgumentException.class, () -> QuaternionRotation.fromAxisAngle(Vector3D.POSITIVE_INFINITY, Angle.PI_OVER_TWO));
        Assertions.assertThrows(IllegalArgumentException.class, () -> QuaternionRotation.fromAxisAngle(Vector3D.NEGATIVE_INFINITY, Angle.PI_OVER_TWO));
    }

    @Test
    void testFromAxisAngle_invalidAngle() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, Double.NaN),
                IllegalArgumentException.class, "Invalid angle: NaN");
        GeometryTestUtils.assertThrowsWithMessage(() -> QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, Double.POSITIVE_INFINITY),
                IllegalArgumentException.class, "Invalid angle: Infinity");
        GeometryTestUtils.assertThrowsWithMessage(() -> QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, Double.NEGATIVE_INFINITY),
                IllegalArgumentException.class, "Invalid angle: -Infinity");
    }

    @Test
    void testApplyVector() {
        // arrange
        final QuaternionRotation q = QuaternionRotation.fromAxisAngle(Vector3D.of(1, 1, 1), Angle.PI_OVER_TWO);

        EuclideanTestUtils.permute(-2, 2, 0.2, (x, y, z) -> {
            final Vector3D input = Vector3D.of(x, y, z);

            // act
            final Vector3D pt = q.apply(input);
            final Vector3D vec = q.applyVector(input);

            EuclideanTestUtils.assertCoordinatesEqual(pt, vec, EPS);
        });
    }

    @Test
    void testInverse() {
        // arrange
        final QuaternionRotation rot = QuaternionRotation.of(0.5, 0.5, 0.5, 0.5);

        // act
        final QuaternionRotation neg = rot.inverse();

        // assert
        Assertions.assertEquals(-0.5, neg.getQuaternion().getX(), EPS);
        Assertions.assertEquals(-0.5, neg.getQuaternion().getY(), EPS);
        Assertions.assertEquals(-0.5, neg.getQuaternion().getZ(), EPS);
        Assertions.assertEquals(0.5, neg.getQuaternion().getW(), EPS);
    }

    @Test
    void testInverse_apply() {
        // act/assert

        // --- x axes
        assertRotationEquals(StandardRotations.IDENTITY, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, 0.0).inverse());

        assertRotationEquals(StandardRotations.PLUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, -Angle.PI_OVER_TWO).inverse());
        assertRotationEquals(StandardRotations.PLUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_X_DIR, Angle.PI_OVER_TWO).inverse());

        assertRotationEquals(StandardRotations.MINUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_X_DIR, -Angle.PI_OVER_TWO).inverse());
        assertRotationEquals(StandardRotations.MINUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, Angle.PI_OVER_TWO).inverse());

        assertRotationEquals(StandardRotations.X_PI, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, Math.PI).inverse());
        assertRotationEquals(StandardRotations.X_PI, QuaternionRotation.fromAxisAngle(MINUS_X_DIR, Math.PI).inverse());

        // --- y axes
        assertRotationEquals(StandardRotations.IDENTITY, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, 0.0).inverse());

        assertRotationEquals(StandardRotations.PLUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, -Angle.PI_OVER_TWO).inverse());
        assertRotationEquals(StandardRotations.PLUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Y_DIR, Angle.PI_OVER_TWO).inverse());

        assertRotationEquals(StandardRotations.MINUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Y_DIR, -Angle.PI_OVER_TWO).inverse());
        assertRotationEquals(StandardRotations.MINUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, Angle.PI_OVER_TWO).inverse());

        assertRotationEquals(StandardRotations.Y_PI, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, Math.PI).inverse());
        assertRotationEquals(StandardRotations.Y_PI, QuaternionRotation.fromAxisAngle(MINUS_Y_DIR, Math.PI).inverse());

        // --- z axes
        assertRotationEquals(StandardRotations.IDENTITY, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, 0.0).inverse());

        assertRotationEquals(StandardRotations.PLUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, -Angle.PI_OVER_TWO).inverse());
        assertRotationEquals(StandardRotations.PLUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Z_DIR, Angle.PI_OVER_TWO).inverse());

        assertRotationEquals(StandardRotations.MINUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Z_DIR, -Angle.PI_OVER_TWO).inverse());
        assertRotationEquals(StandardRotations.MINUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, Angle.PI_OVER_TWO).inverse());

        assertRotationEquals(StandardRotations.Z_PI, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, Math.PI).inverse());
        assertRotationEquals(StandardRotations.Z_PI, QuaternionRotation.fromAxisAngle(MINUS_Z_DIR, Math.PI).inverse());

        // --- diagonal
        assertRotationEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, MINUS_TWO_THIRDS_PI).inverse());
        assertRotationEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(MINUS_DIAGONAL, TWO_THIRDS_PI).inverse());

        assertRotationEquals(StandardRotations.MINUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(MINUS_DIAGONAL, MINUS_TWO_THIRDS_PI).inverse());
        assertRotationEquals(StandardRotations.MINUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, TWO_THIRDS_PI).inverse());
    }

    @Test
    void testInverse_undoesOriginalRotation() {
        EuclideanTestUtils.permuteSkipZero(-5, 5, 1, (x, y, z) -> {
            // arrange
            final Vector3D vec = Vector3D.of(x, y, z);

            final QuaternionRotation rot = QuaternionRotation.fromAxisAngle(vec, 0.75 * Math.PI);
            final QuaternionRotation neg = rot.inverse();

            // act/assert
            EuclideanTestUtils.assertCoordinatesEqual(PLUS_DIAGONAL, neg.apply(rot.apply(PLUS_DIAGONAL)), EPS);
            EuclideanTestUtils.assertCoordinatesEqual(PLUS_DIAGONAL, rot.apply(neg.apply(PLUS_DIAGONAL)), EPS);
        });
    }

    @Test
    void testMultiply_sameAxis_simple() {
        // arrange
        final QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, 0.1 * Math.PI);
        final QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, 0.4 * Math.PI);

        // act
        final QuaternionRotation result = q1.multiply(q2);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_X, result.getAxis(), EPS);
        Assertions.assertEquals(Angle.PI_OVER_TWO, result.getAngle(), EPS);

        assertRotationEquals(StandardRotations.PLUS_X_HALF_PI, result);
    }

    @Test
    void testMultiply_sameAxis_multiple() {
        // arrange
        final double oneThird = 1.0 / 3.0;
        final QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, 0.1 * Math.PI);
        final QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, oneThird * Math.PI);
        final QuaternionRotation q3 = QuaternionRotation.fromAxisAngle(MINUS_DIAGONAL, 0.4 * Math.PI);
        final QuaternionRotation q4 = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, 0.3 * Math.PI);
        final QuaternionRotation q5 = QuaternionRotation.fromAxisAngle(MINUS_DIAGONAL, -oneThird * Math.PI);

        // act
        final QuaternionRotation result = q1.multiply(q2).multiply(q3).multiply(q4).multiply(q5);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(PLUS_DIAGONAL.normalize(), result.getAxis(), EPS);
        Assertions.assertEquals(2.0 * Math.PI / 3.0, result.getAngle(), EPS);

        assertRotationEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, result);
    }

    @Test
    void testMultiply_differentAxes() {
        // arrange
        final QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, Angle.PI_OVER_TWO);
        final QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, Angle.PI_OVER_TWO);

        // act
        final QuaternionRotation result = q1.multiply(q2);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(PLUS_DIAGONAL.normalize(), result.getAxis(), EPS);
        Assertions.assertEquals(2.0 * Math.PI / 3.0, result.getAngle(), EPS);

        assertRotationEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, result);

        assertRotationEquals(v -> {
            final Vector3D temp = StandardRotations.PLUS_Y_HALF_PI.apply(v);
            return StandardRotations.PLUS_X_HALF_PI.apply(temp);
        }, result);
    }

    @Test
    void testMultiply_orderOfOperations() {
        // arrange
        final QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, Angle.PI_OVER_TWO);
        final QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, Math.PI);
        final QuaternionRotation q3 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.MINUS_Z, Angle.PI_OVER_TWO);

        // act
        final QuaternionRotation result = q3.multiply(q2).multiply(q1);

        // assert
        assertRotationEquals(v -> {
            Vector3D temp = StandardRotations.PLUS_X_HALF_PI.apply(v);
            temp = StandardRotations.Y_PI.apply(temp);
            return StandardRotations.MINUS_Z_HALF_PI.apply(temp);
        }, result);
    }

    @Test
    void testMultiply_numericalStability() {
        // arrange
        final int slices = 1024;
        final double delta = (8.0 * Math.PI / 3.0) / slices;

        QuaternionRotation q = QuaternionRotation.identity();

        final UniformRandomProvider rand = RandomSource.XO_SHI_RO_256_PP.create(0x975e1238facd123L);

        // act
        for (int i = 0; i < slices; ++i) {
            final double angle = rand.nextDouble();
            final QuaternionRotation forward = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, angle);
            final QuaternionRotation backward = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, delta - angle);

            q = q.multiply(forward).multiply(backward);
        }

        // assert
        Assertions.assertTrue(q.getQuaternion().getW() > 0);
        Assertions.assertEquals(1.0, q.getQuaternion().norm(), EPS);

        assertRotationEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, q);
    }

    @Test
    void testPremultiply_sameAxis_simple() {
        // arrange
        final QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, 0.1 * Math.PI);
        final QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, 0.4 * Math.PI);

        // act
        final QuaternionRotation result = q1.premultiply(q2);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_X, result.getAxis(), EPS);
        Assertions.assertEquals(Angle.PI_OVER_TWO, result.getAngle(), EPS);

        assertRotationEquals(StandardRotations.PLUS_X_HALF_PI, result);
    }

    @Test
    void testPremultiply_sameAxis_multiple() {
        // arrange
        final double oneThird = 1.0 / 3.0;
        final QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, 0.1 * Math.PI);
        final QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, oneThird * Math.PI);
        final QuaternionRotation q3 = QuaternionRotation.fromAxisAngle(MINUS_DIAGONAL, 0.4 * Math.PI);
        final QuaternionRotation q4 = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, 0.3 * Math.PI);
        final QuaternionRotation q5 = QuaternionRotation.fromAxisAngle(MINUS_DIAGONAL, -oneThird * Math.PI);

        // act
        final QuaternionRotation result = q1.premultiply(q2).premultiply(q3).premultiply(q4).premultiply(q5);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(PLUS_DIAGONAL.normalize(), result.getAxis(), EPS);
        Assertions.assertEquals(2.0 * Math.PI / 3.0, result.getAngle(), EPS);

        assertRotationEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, result);
    }

    @Test
    void testPremultiply_differentAxes() {
        // arrange
        final QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, Angle.PI_OVER_TWO);
        final QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, Angle.PI_OVER_TWO);

        // act
        final QuaternionRotation result = q2.premultiply(q1);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(PLUS_DIAGONAL.normalize(), result.getAxis(), EPS);
        Assertions.assertEquals(2.0 * Math.PI / 3.0, result.getAngle(), EPS);

        assertRotationEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, result);

        assertRotationEquals(v -> {
            final Vector3D temp = StandardRotations.PLUS_Y_HALF_PI.apply(v);
            return StandardRotations.PLUS_X_HALF_PI.apply(temp);
        }, result);
    }

    @Test
    void testPremultiply_orderOfOperations() {
        // arrange
        final QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, Angle.PI_OVER_TWO);
        final QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, Math.PI);
        final QuaternionRotation q3 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.MINUS_Z, Angle.PI_OVER_TWO);

        // act
        final QuaternionRotation result = q1.premultiply(q2).premultiply(q3);

        // assert
        assertRotationEquals(v -> {
            Vector3D temp = StandardRotations.PLUS_X_HALF_PI.apply(v);
            temp = StandardRotations.Y_PI.apply(temp);
            return StandardRotations.MINUS_Z_HALF_PI.apply(temp);
        }, result);
    }

    @Test
    void testSlerp_simple() {
        // arrange
        final QuaternionRotation q0 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, 0.0);
        final QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, Math.PI);
        final DoubleFunction<QuaternionRotation> fn = q0.slerp(q1);
        final Vector3D v = Vector3D.of(2, 0, 1);

        final double sqrt2 = Math.sqrt(2);

        // act
        checkVector(fn.apply(0).apply(v), 2, 0, 1);
        checkVector(fn.apply(0.25).apply(v), sqrt2, sqrt2, 1);
        checkVector(fn.apply(0.5).apply(v), 0, 2, 1);
        checkVector(fn.apply(0.75).apply(v), -sqrt2, sqrt2, 1);
        checkVector(fn.apply(1).apply(v), -2, 0, 1);
    }

    @Test
    void testSlerp_multipleCombinations() {
        // arrange
        final QuaternionRotation[] rotations = {
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, 0.0),
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, Angle.PI_OVER_TWO),
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, Math.PI),

                QuaternionRotation.fromAxisAngle(Vector3D.Unit.MINUS_X, 0.0),
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.MINUS_X, Angle.PI_OVER_TWO),
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.MINUS_X, Math.PI),

                QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, 0.0),
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, Angle.PI_OVER_TWO),
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, Math.PI),

                QuaternionRotation.fromAxisAngle(Vector3D.Unit.MINUS_Y, 0.0),
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.MINUS_Y, Angle.PI_OVER_TWO),
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.MINUS_Y, Math.PI),

                QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, 0.0),
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, Angle.PI_OVER_TWO),
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, Math.PI),

                QuaternionRotation.fromAxisAngle(Vector3D.Unit.MINUS_Z, 0.0),
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.MINUS_Z, Angle.PI_OVER_TWO),
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.MINUS_Z, Math.PI),
        };

        // act/assert
        // test each rotation against all of the others (including itself)
        for (final QuaternionRotation quaternionRotation : rotations) {
            for (final QuaternionRotation rotation : rotations) {
                checkSlerpCombination(quaternionRotation, rotation);
            }
        }
    }

    private void checkSlerpCombination(final QuaternionRotation start, final QuaternionRotation end) {
        final DoubleFunction<QuaternionRotation> slerp = start.slerp(end);
        final Vector3D vec = Vector3D.of(1, 1, 1).normalize();

        final Vector3D startVec = start.apply(vec);
        final Vector3D endVec = end.apply(vec);

        // check start and end values
        EuclideanTestUtils.assertCoordinatesEqual(startVec, slerp.apply(0).apply(vec), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(endVec, slerp.apply(1).apply(vec), EPS);

        // check intermediate values
        double prevAngle = -1;
        final int numSteps = 100;
        final double delta = 1d / numSteps;
        for (int step = 0; step <= numSteps; step++) {
            final double t = step * delta;
            final QuaternionRotation result = slerp.apply(t);

            final Vector3D slerpVec = result.apply(vec);
            Assertions.assertEquals(1, slerpVec.norm(), EPS);

            // make sure that we're steadily progressing to the end angle
            final double angle = slerpVec.angle(startVec);
            Assertions.assertTrue(Precision.compareTo(angle, prevAngle, EPS) >= 0, "Expected slerp angle to continuously increase; previous angle was " +
                    prevAngle + " and new angle is " + angle);

            prevAngle = angle;
        }
    }

    @Test
    void testSlerp_followsShortestPath() {
        // arrange
        final QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, 0.75 * Math.PI);
        final QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, -0.75 * Math.PI);

        // act
        final QuaternionRotation result = q1.slerp(q2).apply(0.5);

        // assert
        // the slerp should have followed the path around the pi coordinate of the circle rather than
        // the one through the zero coordinate
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_X, result.apply(Vector3D.Unit.PLUS_X), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z, result.getAxis(), EPS);
        Assertions.assertEquals(Math.PI, result.getAngle(), EPS);
    }

    @Test
    void testSlerp_inputQuaternionsHaveMinusOneDotProduct() {
        // arrange
        final QuaternionRotation q1 = QuaternionRotation.of(1, 0, 0, 1); // pi/2 around +z
        final QuaternionRotation q2 = QuaternionRotation.of(-1, 0, 0, -1); // 3pi/2 around -z

        // act
        final QuaternionRotation result = q1.slerp(q2).apply(0.5);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Y, result.apply(Vector3D.Unit.PLUS_X), EPS);

        Assertions.assertEquals(Angle.PI_OVER_TWO, result.getAngle(), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z, result.getAxis(), EPS);
    }

    @Test
    void testSlerp_outputQuaternionIsNormalizedForAllT() {
        // arrange
        final QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, 0.25 * Math.PI);
        final QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, 0.75 * Math.PI);

        final int numSteps = 200;
        final double delta = 1d / numSteps;
        for (int step = 0; step <= numSteps; step++) {
            final double t = -10 + step * delta;

            // act
            final QuaternionRotation result = q1.slerp(q2).apply(t);

            // assert
            Assertions.assertEquals(1.0, result.getQuaternion().norm(), EPS);
        }
    }

    @Test
    void testSlerp_tOutsideOfZeroToOne_apply() {
        // arrange
        final Vector3D vec = Vector3D.Unit.PLUS_X;

        final QuaternionRotation q1 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, 0.25 * Math.PI);
        final QuaternionRotation q2 = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, 0.75 * Math.PI);

        // act/assert
        final DoubleFunction<QuaternionRotation> slerp12 = q1.slerp(q2);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_X, slerp12.apply(-4.5).apply(vec), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_X, slerp12.apply(-0.5).apply(vec), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_X, slerp12.apply(1.5).apply(vec), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_X, slerp12.apply(5.5).apply(vec), EPS);

        final DoubleFunction<QuaternionRotation> slerp21 = q2.slerp(q1);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_X, slerp21.apply(-4.5).apply(vec), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_X, slerp21.apply(-0.5).apply(vec), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_X, slerp21.apply(1.5).apply(vec), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_X, slerp21.apply(5.5).apply(vec), EPS);
    }

    @Test
    void testToMatrix() {
        // act/assert
        // --- x axes
        assertTransformEquals(StandardRotations.IDENTITY, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, 0.0).toMatrix());

        assertTransformEquals(StandardRotations.PLUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, Angle.PI_OVER_TWO).toMatrix());
        assertTransformEquals(StandardRotations.PLUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_X_DIR, -Angle.PI_OVER_TWO).toMatrix());

        assertTransformEquals(StandardRotations.MINUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_X_DIR, Angle.PI_OVER_TWO).toMatrix());
        assertTransformEquals(StandardRotations.MINUS_X_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, -Angle.PI_OVER_TWO).toMatrix());

        assertTransformEquals(StandardRotations.X_PI, QuaternionRotation.fromAxisAngle(PLUS_X_DIR, Math.PI).toMatrix());
        assertTransformEquals(StandardRotations.X_PI, QuaternionRotation.fromAxisAngle(MINUS_X_DIR, Math.PI).toMatrix());

        // --- y axes
        assertTransformEquals(StandardRotations.IDENTITY, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, 0.0).toMatrix());

        assertTransformEquals(StandardRotations.PLUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, Angle.PI_OVER_TWO).toMatrix());
        assertTransformEquals(StandardRotations.PLUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Y_DIR, -Angle.PI_OVER_TWO).toMatrix());

        assertTransformEquals(StandardRotations.MINUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Y_DIR, Angle.PI_OVER_TWO).toMatrix());
        assertTransformEquals(StandardRotations.MINUS_Y_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, -Angle.PI_OVER_TWO).toMatrix());

        assertTransformEquals(StandardRotations.Y_PI, QuaternionRotation.fromAxisAngle(PLUS_Y_DIR, Math.PI).toMatrix());
        assertTransformEquals(StandardRotations.Y_PI, QuaternionRotation.fromAxisAngle(MINUS_Y_DIR, Math.PI).toMatrix());

        // --- z axes
        assertTransformEquals(StandardRotations.IDENTITY, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, 0.0).toMatrix());

        assertTransformEquals(StandardRotations.PLUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, Angle.PI_OVER_TWO).toMatrix());
        assertTransformEquals(StandardRotations.PLUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Z_DIR, -Angle.PI_OVER_TWO).toMatrix());

        assertTransformEquals(StandardRotations.MINUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(MINUS_Z_DIR, Angle.PI_OVER_TWO).toMatrix());
        assertTransformEquals(StandardRotations.MINUS_Z_HALF_PI, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, -Angle.PI_OVER_TWO).toMatrix());

        assertTransformEquals(StandardRotations.Z_PI, QuaternionRotation.fromAxisAngle(PLUS_Z_DIR, Math.PI).toMatrix());
        assertTransformEquals(StandardRotations.Z_PI, QuaternionRotation.fromAxisAngle(MINUS_Z_DIR, Math.PI).toMatrix());

        // --- diagonal
        assertTransformEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, TWO_THIRDS_PI).toMatrix());
        assertTransformEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(MINUS_DIAGONAL, MINUS_TWO_THIRDS_PI).toMatrix());

        assertTransformEquals(StandardRotations.MINUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(MINUS_DIAGONAL, TWO_THIRDS_PI).toMatrix());
        assertTransformEquals(StandardRotations.MINUS_DIAGONAL_TWO_THIRDS_PI, QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, MINUS_TWO_THIRDS_PI).toMatrix());
    }

    @Test
    void testAxisAngleSequenceConversion_relative() {
        for (final AxisSequence axes : AxisSequence.values()) {
            checkAxisAngleSequenceToQuaternionRoundtrip(AxisReferenceFrame.RELATIVE, axes);
            checkQuaternionToAxisAngleSequenceRoundtrip(AxisReferenceFrame.RELATIVE, axes);
        }
    }

    @Test
    void testAxisAngleSequenceConversion_absolute() {
        for (final AxisSequence axes : AxisSequence.values()) {
            checkAxisAngleSequenceToQuaternionRoundtrip(AxisReferenceFrame.ABSOLUTE, axes);
            checkQuaternionToAxisAngleSequenceRoundtrip(AxisReferenceFrame.ABSOLUTE, axes);
        }
    }

    private void checkAxisAngleSequenceToQuaternionRoundtrip(final AxisReferenceFrame frame, final AxisSequence axes) {
        final double step = 0.3;
        final double angle2Start = axes.getType() == AxisSequenceType.EULER ? 0.0 + 0.1 : -Angle.PI_OVER_TWO + 0.1;
        final double angle2Stop = angle2Start + Math.PI;

        for (double angle1 = 0.0; angle1 <= Angle.TWO_PI; angle1 += step) {
            for (double angle2 = angle2Start; angle2 < angle2Stop; angle2 += step) {
                for (double angle3 = 0.0; angle3 <= Angle.TWO_PI; angle3 += 0.3) {
                    // arrange
                    final AxisAngleSequence angles = new AxisAngleSequence(frame, axes, angle1, angle2, angle3);

                    // act
                    final QuaternionRotation q = QuaternionRotation.fromAxisAngleSequence(angles);
                    final AxisAngleSequence result = q.toAxisAngleSequence(frame, axes);

                    // assert
                    Assertions.assertEquals(frame, result.getReferenceFrame());
                    Assertions.assertEquals(axes, result.getAxisSequence());

                    assertRadiansEquals(angle1, result.getAngle1());
                    assertRadiansEquals(angle2, result.getAngle2());
                    assertRadiansEquals(angle3, result.getAngle3());
                }
            }
        }
    }

    private void checkQuaternionToAxisAngleSequenceRoundtrip(final AxisReferenceFrame frame, final AxisSequence axes) {
        final double step = 0.1;

        EuclideanTestUtils.permuteSkipZero(-1, 1, 0.5, (x, y, z) -> {
            final Vector3D axis = Vector3D.of(x, y, z);

            for (double angle = -Angle.TWO_PI; angle <= Angle.TWO_PI; angle += step) {
                // arrange
                final QuaternionRotation q = QuaternionRotation.fromAxisAngle(axis, angle);

                // act
                final AxisAngleSequence seq = q.toAxisAngleSequence(frame, axes);
                final QuaternionRotation result = QuaternionRotation.fromAxisAngleSequence(seq);

                // assert
                checkQuaternion(result, q.getQuaternion().getW(), q.getQuaternion().getX(), q.getQuaternion().getY(), q.getQuaternion().getZ());
            }
        });
    }

    @Test
    void testAxisAngleSequenceConversion_relative_eulerSingularities() {
        // arrange
        final double[] eulerSingularities = {
            0.0,
            Math.PI
        };

        final double angle1 = 0.1;
        final double angle2 = 0.3;

        final AxisReferenceFrame frame = AxisReferenceFrame.RELATIVE;

        for (final AxisSequence axes : getAxes(AxisSequenceType.EULER)) {
            for (final double singularityAngle : eulerSingularities) {

                final AxisAngleSequence inputSeq = new AxisAngleSequence(frame, axes, angle1, singularityAngle, angle2);
                final QuaternionRotation inputQuat = QuaternionRotation.fromAxisAngleSequence(inputSeq);

                // act
                final AxisAngleSequence resultSeq = inputQuat.toAxisAngleSequence(frame, axes);
                final QuaternionRotation resultQuat = QuaternionRotation.fromAxisAngleSequence(resultSeq);

                // assert
                Assertions.assertEquals(frame, resultSeq.getReferenceFrame());
                Assertions.assertEquals(axes, resultSeq.getAxisSequence());

                assertRadiansEquals(singularityAngle, resultSeq.getAngle2());
                assertRadiansEquals(0.0, resultSeq.getAngle3());

                checkQuaternion(resultQuat, inputQuat.getQuaternion().getW(), inputQuat.getQuaternion().getX(), inputQuat.getQuaternion().getY(), inputQuat.getQuaternion().getZ());
            }
        }
    }

    @Test
    void testAxisAngleSequenceConversion_absolute_eulerSingularities() {
        // arrange
        final double[] eulerSingularities = {
            0.0,
            Math.PI
        };

        final double angle1 = 0.1;
        final double angle2 = 0.3;

        final AxisReferenceFrame frame = AxisReferenceFrame.ABSOLUTE;

        for (final AxisSequence axes : getAxes(AxisSequenceType.EULER)) {
            for (final double singularityAngle : eulerSingularities) {

                final AxisAngleSequence inputSeq = new AxisAngleSequence(frame, axes, angle1, singularityAngle, angle2);
                final QuaternionRotation inputQuat = QuaternionRotation.fromAxisAngleSequence(inputSeq);

                // act
                final AxisAngleSequence resultSeq = inputQuat.toAxisAngleSequence(frame, axes);
                final QuaternionRotation resultQuat = QuaternionRotation.fromAxisAngleSequence(resultSeq);

                // assert
                Assertions.assertEquals(frame, resultSeq.getReferenceFrame());
                Assertions.assertEquals(axes, resultSeq.getAxisSequence());

                assertRadiansEquals(0.0, resultSeq.getAngle1());
                assertRadiansEquals(singularityAngle, resultSeq.getAngle2());

                checkQuaternion(resultQuat, inputQuat.getQuaternion().getW(), inputQuat.getQuaternion().getX(), inputQuat.getQuaternion().getY(), inputQuat.getQuaternion().getZ());
            }
        }
    }

    @Test
    void testAxisAngleSequenceConversion_relative_taitBryanSingularities() {
        // arrange
        final double[] taitBryanSingularities = {
            -Angle.PI_OVER_TWO,
            Angle.PI_OVER_TWO
        };

        final double angle1 = 0.1;
        final double angle2 = 0.3;

        final AxisReferenceFrame frame = AxisReferenceFrame.RELATIVE;

        for (final AxisSequence axes : getAxes(AxisSequenceType.TAIT_BRYAN)) {
            for (final double singularityAngle : taitBryanSingularities) {

                final AxisAngleSequence inputSeq = new AxisAngleSequence(frame, axes, angle1, singularityAngle, angle2);
                final QuaternionRotation inputQuat = QuaternionRotation.fromAxisAngleSequence(inputSeq);

                // act
                final AxisAngleSequence resultSeq = inputQuat.toAxisAngleSequence(frame, axes);
                final QuaternionRotation resultQuat = QuaternionRotation.fromAxisAngleSequence(resultSeq);

                // assert
                Assertions.assertEquals(frame, resultSeq.getReferenceFrame());
                Assertions.assertEquals(axes, resultSeq.getAxisSequence());

                assertRadiansEquals(singularityAngle, resultSeq.getAngle2());
                assertRadiansEquals(0.0, resultSeq.getAngle3());

                checkQuaternion(resultQuat, inputQuat.getQuaternion().getW(), inputQuat.getQuaternion().getX(), inputQuat.getQuaternion().getY(), inputQuat.getQuaternion().getZ());
            }
        }
    }

    @Test
    void testAxisAngleSequenceConversion_absolute_taitBryanSingularities() {
        // arrange
        final double[] taitBryanSingularities = {
            -Angle.PI_OVER_TWO,
            Angle.PI_OVER_TWO
        };

        final double angle1 = 0.1;
        final double angle2 = 0.3;

        final AxisReferenceFrame frame = AxisReferenceFrame.ABSOLUTE;

        for (final AxisSequence axes : getAxes(AxisSequenceType.TAIT_BRYAN)) {
            for (final double singularityAngle : taitBryanSingularities) {

                final AxisAngleSequence inputSeq = new AxisAngleSequence(frame, axes, angle1, singularityAngle, angle2);
                final QuaternionRotation inputQuat = QuaternionRotation.fromAxisAngleSequence(inputSeq);

                // act
                final AxisAngleSequence resultSeq = inputQuat.toAxisAngleSequence(frame, axes);
                final QuaternionRotation resultQuat = QuaternionRotation.fromAxisAngleSequence(resultSeq);

                // assert
                Assertions.assertEquals(frame, resultSeq.getReferenceFrame());
                Assertions.assertEquals(axes, resultSeq.getAxisSequence());

                assertRadiansEquals(0.0, resultSeq.getAngle1());
                assertRadiansEquals(singularityAngle, resultSeq.getAngle2());

                checkQuaternion(resultQuat, inputQuat.getQuaternion().getW(), inputQuat.getQuaternion().getX(), inputQuat.getQuaternion().getY(), inputQuat.getQuaternion().getZ());
            }
        }
    }

    private List<AxisSequence> getAxes(final AxisSequenceType type) {
        return Stream.of(AxisSequence.values())
                .filter(a -> type.equals(a.getType()))
                .collect(Collectors.toList());
    }

    @Test
    void testToAxisAngleSequence_invalidArgs() {
        // arrange
        final QuaternionRotation q = QuaternionRotation.identity();

        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> q.toAxisAngleSequence(null, AxisSequence.XYZ));
        Assertions.assertThrows(IllegalArgumentException.class, () -> q.toAxisAngleSequence(AxisReferenceFrame.ABSOLUTE, null));
    }

    @Test
    void testToRelativeAxisAngleSequence() {
        // arrange
        final QuaternionRotation q = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, TWO_THIRDS_PI);

        // act
        final AxisAngleSequence seq = q.toRelativeAxisAngleSequence(AxisSequence.YZX);

        // assert
        Assertions.assertEquals(AxisReferenceFrame.RELATIVE, seq.getReferenceFrame());
        Assertions.assertEquals(AxisSequence.YZX, seq.getAxisSequence());
        Assertions.assertEquals(Angle.PI_OVER_TWO, seq.getAngle1(), EPS);
        Assertions.assertEquals(Angle.PI_OVER_TWO, seq.getAngle2(), EPS);
        Assertions.assertEquals(0, seq.getAngle3(), EPS);
    }

    @Test
    void testToAbsoluteAxisAngleSequence() {
        // arrange
        final QuaternionRotation q = QuaternionRotation.fromAxisAngle(PLUS_DIAGONAL, TWO_THIRDS_PI);

        // act
        final AxisAngleSequence seq = q.toAbsoluteAxisAngleSequence(AxisSequence.YZX);

        // assert
        Assertions.assertEquals(AxisReferenceFrame.ABSOLUTE, seq.getReferenceFrame());
        Assertions.assertEquals(AxisSequence.YZX, seq.getAxisSequence());
        Assertions.assertEquals(Angle.PI_OVER_TWO, seq.getAngle1(), EPS);
        Assertions.assertEquals(0, seq.getAngle2(), EPS);
        Assertions.assertEquals(Angle.PI_OVER_TWO, seq.getAngle3(), EPS);
    }

    @Test
    void testHashCode() {
        // arrange
        final double delta = 100 * Precision.EPSILON;
        final QuaternionRotation q1 = QuaternionRotation.of(1, 2, 3, 4);
        final QuaternionRotation q2 = QuaternionRotation.of(1, 2, 3, 4);

        // act/assert
        Assertions.assertEquals(q1.hashCode(), q2.hashCode());

        Assertions.assertNotEquals(q1.hashCode(), QuaternionRotation.of(1 + delta, 2, 3, 4).hashCode());
        Assertions.assertNotEquals(q1.hashCode(), QuaternionRotation.of(1, 2 + delta, 3, 4).hashCode());
        Assertions.assertNotEquals(q1.hashCode(), QuaternionRotation.of(1, 2, 3 + delta, 4).hashCode());
        Assertions.assertNotEquals(q1.hashCode(), QuaternionRotation.of(1, 2, 3, 4 + delta).hashCode());
    }

    @Test
    void testEquals() {
        // arrange
        final double delta = 100 * Precision.EPSILON;
        final QuaternionRotation q1 = QuaternionRotation.of(1, 2, 3, 4);
        final QuaternionRotation q2 = QuaternionRotation.of(1, 2, 3, 4);

        // act/assert
        GeometryTestUtils.assertSimpleEqualsCases(q1);
        Assertions.assertEquals(q1, q2);

        Assertions.assertNotEquals(q1, QuaternionRotation.of(-1, -2, -3, 4));
        Assertions.assertNotEquals(q1, QuaternionRotation.of(1, 2, 3, -4));

        Assertions.assertNotEquals(q1, QuaternionRotation.of(1 + delta, 2, 3, 4));
        Assertions.assertNotEquals(q1, QuaternionRotation.of(1, 2 + delta, 3, 4));
        Assertions.assertNotEquals(q1, QuaternionRotation.of(1, 2, 3 + delta, 4));
        Assertions.assertNotEquals(q1, QuaternionRotation.of(1, 2, 3, 4 + delta));
    }

    @Test
    void testToString() {
        // arrange
        final QuaternionRotation q = QuaternionRotation.of(1, 2, 3, 4);
        final Quaternion qField = q.getQuaternion();

        // assert
        Assertions.assertEquals(qField.toString(), q.toString());
    }

    @Test
    void testCreateVectorRotation_simple() {
        // arrange
        final Vector3D u1 = Vector3D.Unit.PLUS_X;
        final Vector3D u2 = Vector3D.Unit.PLUS_Y;

        // act
        final QuaternionRotation q = QuaternionRotation.createVectorRotation(u1, u2);

        // assert
        final double val = Math.sqrt(2) * 0.5;

        checkQuaternion(q, val, 0, 0, val);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z, q.getAxis(), EPS);
        Assertions.assertEquals(Angle.PI_OVER_TWO, q.getAngle(), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(u2, q.apply(u1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(u1, q.inverse().apply(u2), EPS);
    }

    @Test
    void testCreateVectorRotation_identity() {
        // arrange
        final Vector3D u1 = Vector3D.of(0, 2, 0);

        // act
        final QuaternionRotation q = QuaternionRotation.createVectorRotation(u1, u1);

        // assert
        checkQuaternion(q, 1, 0, 0, 0);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_X, q.getAxis(), EPS);
        Assertions.assertEquals(0.0, q.getAngle(), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 2, 0), q.apply(u1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 2, 0), q.inverse().apply(u1), EPS);
    }

    @Test
    void testCreateVectorRotation_parallel() {
        // arrange
        final Vector3D u1 = Vector3D.of(0, 2, 0);
        final Vector3D u2 = Vector3D.of(0, 3, 0);

        // act
        final QuaternionRotation q = QuaternionRotation.createVectorRotation(u1, u2);

        // assert
        checkQuaternion(q, 1, 0, 0, 0);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_X, q.getAxis(), EPS);
        Assertions.assertEquals(0.0, q.getAngle(), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 2, 0), q.apply(u1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 3, 0), q.inverse().apply(u2), EPS);
    }

    @Test
    void testCreateVectorRotation_antiparallel() {
        // arrange
        final Vector3D u1 = Vector3D.of(0, 2, 0);
        final Vector3D u2 = Vector3D.of(0, -3, 0);

        // act
        final QuaternionRotation q = QuaternionRotation.createVectorRotation(u1, u2);

        // assert
        final Vector3D axis = q.getAxis();
        Assertions.assertEquals(0.0, axis.dot(u1), EPS);
        Assertions.assertEquals(0.0, axis.dot(u2), EPS);
        Assertions.assertEquals(Math.PI, q.getAngle(), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, -2, 0), q.apply(u1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 3, 0), q.inverse().apply(u2), EPS);
    }

    @Test
    void testCreateVectorRotation_permute() {
        EuclideanTestUtils.permuteSkipZero(-5, 5, 0.1, (x, y, z) -> {
            // arrange
            final Vector3D u1 = Vector3D.of(x, y, z);
            final Vector3D u2 = PLUS_DIAGONAL;

            // act
            final QuaternionRotation q = QuaternionRotation.createVectorRotation(u1, u2);

            // assert
            Assertions.assertEquals(0.0, q.apply(u1).angle(u2), EPS);
            Assertions.assertEquals(0.0, q.inverse().apply(u2).angle(u1), EPS);

            final double angle = q.getAngle();
            Assertions.assertTrue(angle >= 0.0);
            Assertions.assertTrue(angle <= Math.PI);
        });
    }

    @Test
    void testCreateVectorRotation_invalidArgs() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> QuaternionRotation.createVectorRotation(Vector3D.ZERO, Vector3D.Unit.PLUS_X));
        Assertions.assertThrows(IllegalArgumentException.class, () -> QuaternionRotation.createVectorRotation(Vector3D.Unit.PLUS_X, Vector3D.ZERO));
        Assertions.assertThrows(IllegalArgumentException.class, () -> QuaternionRotation.createVectorRotation(Vector3D.NaN, Vector3D.Unit.PLUS_X));
        Assertions.assertThrows(IllegalArgumentException.class, () -> QuaternionRotation.createVectorRotation(Vector3D.Unit.PLUS_X, Vector3D.POSITIVE_INFINITY));
        Assertions.assertThrows(IllegalArgumentException.class, () -> QuaternionRotation.createVectorRotation(Vector3D.Unit.PLUS_X, Vector3D.NEGATIVE_INFINITY));
    }

    @Test
    void testCreateBasisRotation_simple() {
        // arrange
        final Vector3D u1 = Vector3D.Unit.PLUS_X;
        final Vector3D u2 = Vector3D.Unit.PLUS_Y;

        final Vector3D v1 = Vector3D.Unit.PLUS_Y;
        final Vector3D v2 = Vector3D.Unit.MINUS_X;

        // act
        final QuaternionRotation q = QuaternionRotation.createBasisRotation(u1, u2, v1, v2);

        // assert
        final QuaternionRotation qInv = q.inverse();

        EuclideanTestUtils.assertCoordinatesEqual(v1, q.apply(u1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(v2, q.apply(u2), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(u1, qInv.apply(v1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(u2, qInv.apply(v2), EPS);

        assertRotationEquals(StandardRotations.PLUS_Z_HALF_PI, q);
    }

    @Test
    void testCreateBasisRotation_diagonalAxis() {
        // arrange
        final Vector3D u1 = Vector3D.Unit.PLUS_X;
        final Vector3D u2 = Vector3D.Unit.PLUS_Y;

        final Vector3D v1 = Vector3D.Unit.PLUS_Y;
        final Vector3D v2 = Vector3D.Unit.PLUS_Z;

        // act
        final QuaternionRotation q = QuaternionRotation.createBasisRotation(u1, u2, v1, v2);

        // assert
        final QuaternionRotation qInv = q.inverse();

        EuclideanTestUtils.assertCoordinatesEqual(v1, q.apply(u1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(v2, q.apply(u2), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(u1, qInv.apply(v1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(u2, qInv.apply(v2), EPS);

        assertRotationEquals(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, q);
        assertRotationEquals(StandardRotations.MINUS_DIAGONAL_TWO_THIRDS_PI, q.inverse());
    }

    @Test
    void testCreateBasisRotation_identity() {
        // arrange
        final Vector3D u1 = Vector3D.Unit.PLUS_X;
        final Vector3D u2 = Vector3D.Unit.PLUS_Y;

        // act
        final QuaternionRotation q = QuaternionRotation.createBasisRotation(u1, u2, u1, u2);

        // assert
        final QuaternionRotation qInv = q.inverse();

        EuclideanTestUtils.assertCoordinatesEqual(u1, q.apply(u1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(u2, q.apply(u2), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(u1, qInv.apply(u1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(u2, qInv.apply(u2), EPS);

        assertRotationEquals(StandardRotations.IDENTITY, q);
    }

    @Test
    void testCreateBasisRotation_equivalentBases() {
        // arrange
        final Vector3D u1 = Vector3D.of(2, 0, 0);
        final Vector3D u2 = Vector3D.of(0, 3, 0);

        final Vector3D v1 = Vector3D.of(4, 0, 0);
        final Vector3D v2 = Vector3D.of(0, 5, 0);

        // act
        final QuaternionRotation q = QuaternionRotation.createBasisRotation(u1, u2, v1, v2);

        // assert
        final QuaternionRotation qInv = q.inverse();

        EuclideanTestUtils.assertCoordinatesEqual(u1, q.apply(u1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(u2, q.apply(u2), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(v1, qInv.apply(v1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(v2, qInv.apply(v2), EPS);

        assertRotationEquals(StandardRotations.IDENTITY, q);
    }

    @Test
    void testCreateBasisRotation_nonOrthogonalVectors() {
        // arrange
        final Vector3D u1 = Vector3D.of(2, 0, 0);
        final Vector3D u2 = Vector3D.of(1, 0.5, 0);

        final Vector3D v1 = Vector3D.of(0, 1.5, 0);
        final Vector3D v2 = Vector3D.of(-1, 1.5, 0);

        // act
        final QuaternionRotation q = QuaternionRotation.createBasisRotation(u1, u2, v1, v2);

        // assert
        final QuaternionRotation qInv = q.inverse();

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 2, 0), q.apply(u1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-0.5, 1, 0), q.apply(u2), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1.5, 0, 0), qInv.apply(v1), EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1.5, 1, 0), qInv.apply(v2), EPS);

        assertRotationEquals(StandardRotations.PLUS_Z_HALF_PI, q);
    }

    @Test
    void testCreateBasisRotation_permute() {
        // arrange
        final Vector3D u1 = Vector3D.of(1, 2, 3);
        final Vector3D u2 = Vector3D.of(0, 4, 0);

        final Vector3D u1Dir = u1.normalize();
        final Vector3D u2Dir = u1Dir.orthogonal(u2);

        EuclideanTestUtils.permuteSkipZero(-5, 5, 0.2, (x, y, z) -> {
            final Vector3D v1 = Vector3D.of(x, y, z);
            final Vector3D v2 = v1.orthogonal();

            final Vector3D v1Dir = v1.normalize();
            final Vector3D v2Dir = v2.normalize();

            // act
            final QuaternionRotation q = QuaternionRotation.createBasisRotation(u1, u2, v1, v2);
            final QuaternionRotation qInv = q.inverse();

            // assert
            EuclideanTestUtils.assertCoordinatesEqual(v1Dir, q.apply(u1Dir), EPS);
            EuclideanTestUtils.assertCoordinatesEqual(v2Dir, q.apply(u2Dir), EPS);

            EuclideanTestUtils.assertCoordinatesEqual(u1Dir, qInv.apply(v1Dir), EPS);
            EuclideanTestUtils.assertCoordinatesEqual(u2Dir, qInv.apply(v2Dir), EPS);

            final double angle = q.getAngle();
            Assertions.assertTrue(angle >= 0.0);
            Assertions.assertTrue(angle <= Math.PI);

            final Vector3D transformedX = q.apply(Vector3D.Unit.PLUS_X);
            final Vector3D transformedY = q.apply(Vector3D.Unit.PLUS_Y);
            final Vector3D transformedZ = q.apply(Vector3D.Unit.PLUS_Z);

            Assertions.assertEquals(1.0, transformedX.norm(), EPS);
            Assertions.assertEquals(1.0, transformedY.norm(), EPS);
            Assertions.assertEquals(1.0, transformedZ.norm(), EPS);

            Assertions.assertEquals(0.0, transformedX.dot(transformedY), EPS);
            Assertions.assertEquals(0.0, transformedX.dot(transformedZ), EPS);
            Assertions.assertEquals(0.0, transformedY.dot(transformedZ), EPS);

            EuclideanTestUtils.assertCoordinatesEqual(transformedZ.normalize(),
                    transformedX.normalize().cross(transformedY.normalize()), EPS);

            Assertions.assertEquals(1.0, q.getQuaternion().norm(), EPS);
        });
    }

    @Test
    void testCreateBasisRotation_invalidArgs() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> QuaternionRotation.createBasisRotation(
                Vector3D.ZERO, Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_X));
        Assertions.assertThrows(IllegalArgumentException.class, () -> QuaternionRotation.createBasisRotation(
                Vector3D.Unit.PLUS_X, Vector3D.NaN, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_X));
        Assertions.assertThrows(IllegalArgumentException.class, () -> QuaternionRotation.createBasisRotation(
                Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, Vector3D.POSITIVE_INFINITY, Vector3D.Unit.MINUS_X));
        Assertions.assertThrows(IllegalArgumentException.class, () -> QuaternionRotation.createBasisRotation(
                Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_Y, Vector3D.NEGATIVE_INFINITY));
        Assertions.assertThrows(IllegalArgumentException.class, () -> QuaternionRotation.createBasisRotation(
                Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_X));
        Assertions.assertThrows(IllegalArgumentException.class, () -> QuaternionRotation.createBasisRotation(
                Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_Y));
    }

    @Test
    void testFromEulerAngles_identity() {
        for (final AxisSequence axes : AxisSequence.values()) {

            // act/assert
            assertRotationEquals(StandardRotations.IDENTITY,
                    QuaternionRotation.fromAxisAngleSequence(AxisAngleSequence.createRelative(axes, 0, 0, 0)));
            assertRotationEquals(StandardRotations.IDENTITY,
                    QuaternionRotation.fromAxisAngleSequence(AxisAngleSequence.createRelative(axes, Angle.TWO_PI, Angle.TWO_PI, Angle.TWO_PI)));

            assertRotationEquals(StandardRotations.IDENTITY,
                    QuaternionRotation.fromAxisAngleSequence(AxisAngleSequence.createAbsolute(axes, 0, 0, 0)));
            assertRotationEquals(StandardRotations.IDENTITY,
                    QuaternionRotation.fromAxisAngleSequence(AxisAngleSequence.createAbsolute(axes, Angle.TWO_PI, Angle.TWO_PI, Angle.TWO_PI)));
        }
    }

    @Test
    void testFromEulerAngles_relative() {

        // --- act/assert

        // XYZ
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.XYZ, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.XYZ, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.XYZ, 0, 0, Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.XYZ, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, 0);

        // XZY
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.XZY, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.XZY, 0, 0, Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.XZY, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.XZY, Angle.PI_OVER_TWO, 0, Angle.PI_OVER_TWO);

        // YXZ
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.YXZ, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.YXZ, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.YXZ, 0, 0, Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.YXZ, Angle.PI_OVER_TWO, 0, Angle.PI_OVER_TWO);

        // YZX
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.YZX, 0, 0, Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.YZX, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.YZX, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.YZX, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, 0);

        // ZXY
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.YZX, 0, 0, Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.YZX, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.YZX, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.YZX, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, 0);

        // ZYX
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.ZYX, 0, 0, Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.ZYX, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.ZYX, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.ZYX, Angle.PI_OVER_TWO, 0, Angle.PI_OVER_TWO);

        // XYX
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.XYX, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.XYX, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.XYX, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, -Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.XYX, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, 0);

        // XZX
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.XZX, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.XZX, -Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.XZX, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.XZX, 0, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO);

        // YXY
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.YXY, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.YXY, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.YXY, -Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.YXY, 0, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO);

        // YZY
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.YZY, -Angle.PI_OVER_TWO, -Angle.PI_OVER_TWO, Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.YZY, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.YZY, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.YZY, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, 0);

        // ZXZ
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.ZXZ, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.ZXZ, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, -Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.ZXZ, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.ZXZ, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, 0);

        // ZYZ
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_X_HALF_PI, AxisSequence.ZYZ, Angle.PI_OVER_TWO, -Angle.PI_OVER_TWO, -Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.ZYZ, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.ZYZ, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceRelative(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.ZYZ, 0, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO);
    }

    /** Helper method for verifying that a relative euler angles instance constructed with the given arguments
     * is correctly converted to a QuaternionRotation that matches the given operator.
     * @param rotation
     * @param axes
     * @param angle1
     * @param angle2
     * @param angle3
     */
    private void checkFromAxisAngleSequenceRelative(final UnaryOperator<Vector3D> rotation, final AxisSequence axes, final double angle1, final double angle2, final double angle3) {
        final AxisAngleSequence angles = AxisAngleSequence.createRelative(axes, angle1, angle2, angle3);

        assertRotationEquals(rotation, QuaternionRotation.fromAxisAngleSequence(angles));
    }

    @Test
    void testFromEulerAngles_absolute() {

        // --- act/assert

        // XYZ
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.XYZ, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.XYZ, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.XYZ, 0, 0, Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.XYZ, Angle.PI_OVER_TWO, 0, Angle.PI_OVER_TWO);

        // XZY
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.XZY, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.XZY, 0, 0, Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.XZY, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.XZY, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, 0);

        // YXZ
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.YXZ, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.YXZ, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.YXZ, 0, 0, Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.YXZ, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, 0);

        // YZX
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.YZX, 0, 0, Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.YZX, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.YZX, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.YZX, Angle.PI_OVER_TWO, 0, Angle.PI_OVER_TWO);

        // ZXY
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.YZX, 0, 0, Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.YZX, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.YZX, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.YZX, Angle.PI_OVER_TWO, 0, Angle.PI_OVER_TWO);

        // ZYX
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.ZYX, 0, 0, Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.ZYX, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.ZYX, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.ZYX, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, 0);

        // XYX
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.XYX, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.XYX, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.XYX, Angle.PI_OVER_TWO, -Angle.PI_OVER_TWO, -Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.XYX, 0, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO);

        // XZX
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.XZX, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.XZX, -Angle.PI_OVER_TWO, -Angle.PI_OVER_TWO, Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.XZX, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.XZX, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, 0);

        // YXY
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.YXY, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.YXY, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.YXY, -Angle.PI_OVER_TWO, -Angle.PI_OVER_TWO, Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.YXY, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, 0);

        // YZY
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.YZY, -Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.YZY, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.YZY, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.YZY, 0, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO);

        // ZXZ
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.ZXZ, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.ZXZ, -Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.ZXZ, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.ZXZ, 0, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO);

        // ZYZ
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_X_HALF_PI, AxisSequence.ZYZ, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, -Angle.PI_OVER_TWO);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Y_HALF_PI, AxisSequence.ZYZ, 0, Angle.PI_OVER_TWO, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_Z_HALF_PI, AxisSequence.ZYZ, Angle.PI_OVER_TWO, 0, 0);
        checkFromAxisAngleSequenceAbsolute(StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI, AxisSequence.ZYZ, Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, 0);
    }

    /** Helper method for verifying that an absolute euler angles instance constructed with the given arguments
     * is correctly converted to a QuaternionRotation that matches the given operator.
     * @param rotation
     * @param axes
     * @param angle1
     * @param angle2
     * @param angle3
     */
    private void checkFromAxisAngleSequenceAbsolute(final UnaryOperator<Vector3D> rotation, final AxisSequence axes, final double angle1, final double angle2, final double angle3) {
        final AxisAngleSequence angles = AxisAngleSequence.createAbsolute(axes, angle1, angle2, angle3);

        assertRotationEquals(rotation, QuaternionRotation.fromAxisAngleSequence(angles));
    }

    private static void checkQuaternion(final QuaternionRotation qrot, final double w, final double x, final double y, final double z) {
        final String msg = "Expected" +
                " quaternion to equal " + SimpleTupleFormat.getDefault().format(w, x, y, z) + " but was " + qrot;

        Assertions.assertEquals(w, qrot.getQuaternion().getW(), EPS, msg);
        Assertions.assertEquals(x, qrot.getQuaternion().getX(), EPS, msg);
        Assertions.assertEquals(y, qrot.getQuaternion().getY(), EPS, msg);
        Assertions.assertEquals(z, qrot.getQuaternion().getZ(), EPS, msg);

        final Quaternion q = qrot.getQuaternion();
        Assertions.assertEquals(w, q.getW(), EPS, msg);
        Assertions.assertEquals(x, q.getX(), EPS, msg);
        Assertions.assertEquals(y, q.getY(), EPS, msg);
        Assertions.assertEquals(z, q.getZ(), EPS, msg);

        Assertions.assertTrue(qrot.preservesOrientation());
    }

    private static void checkVector(final Vector3D v, final double x, final double y, final double z) {
        final String msg = "Expected vector to equal " + SimpleTupleFormat.getDefault().format(x, y, z) + " but was " + v;

        Assertions.assertEquals(x, v.getX(), EPS, msg);
        Assertions.assertEquals(y, v.getY(), EPS, msg);
        Assertions.assertEquals(z, v.getZ(), EPS, msg);
    }

    /** Assert that the two given radian values are equivalent.
     * @param expected
     * @param actual
     */
    private static void assertRadiansEquals(final double expected, final double actual) {
        final double diff = Angle.Rad.WITHIN_MINUS_PI_AND_PI.applyAsDouble(expected - actual);
        final String msg = "Expected " + actual + " radians to be equivalent to " + expected + " radians; difference is " + diff;

        Assertions.assertTrue(Math.abs(diff) < 1e-6, msg);
    }

    /**
     * Assert that {@code rotation} returns the same outputs as {@code expected} for a range of vector inputs.
     * @param expected
     * @param rotation
     */
    private static void assertRotationEquals(final UnaryOperator<Vector3D> expected, final QuaternionRotation rotation) {
        assertFnEquals(expected, rotation);
    }

    /**
     * Assert that {@code transform} returns the same outputs as {@code expected} for a range of vector inputs.
     * @param expected
     * @param transform
     */
    private static void assertTransformEquals(final UnaryOperator<Vector3D> expected, final AffineTransformMatrix3D transform) {
        assertFnEquals(expected, transform);
    }

    /**
     * Assert that {@code actual} returns the same output as {@code expected} for a range of inputs.
     * @param expectedFn
     * @param actualFn
     */
    private static void assertFnEquals(final UnaryOperator<Vector3D> expectedFn, final UnaryOperator<Vector3D> actualFn) {
        EuclideanTestUtils.permute(-2, 2, 0.25, (x, y, z) -> {
            final Vector3D input = Vector3D.of(x, y, z);

            final Vector3D expected = expectedFn.apply(input);
            final Vector3D actual = actualFn.apply(input);

            final String msg = "Expected vector " + input + " to be transformed to " + expected + " but was " + actual;

            Assertions.assertEquals(expected.getX(), actual.getX(), EPS, msg);
            Assertions.assertEquals(expected.getY(), actual.getY(), EPS, msg);
            Assertions.assertEquals(expected.getZ(), actual.getZ(), EPS, msg);
        });
    }
}
