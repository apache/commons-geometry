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
package org.apache.commons.geometry.euclidean.twod.rotation;

import java.util.function.BiFunction;
import java.util.function.DoubleFunction;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.EuclideanTransform;
import org.apache.commons.geometry.euclidean.twod.AffineTransformMatrix2D;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Rotation2DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private static final double THREE_PI_OVER_TWO = 3 * Math.PI / 2;

    @Test
    void testIdentity() {
        // act
        final Rotation2D r = Rotation2D.identity();

        // assert
        Assertions.assertEquals(0.0, r.getAngle(), 0.0);
        Assertions.assertTrue(r.preservesOrientation());
    }

    @Test
    void testProperties() {
        // act
        final Rotation2D r = Rotation2D.of(100.0);

        // assert
        Assertions.assertEquals(100.0, r.getAngle(), 0.0);
        Assertions.assertTrue(r.preservesOrientation());
    }

    @Test
    void testApply() {
        // act/assert
        checkApply(1.0, Vector2D.ZERO, Vector2D.ZERO);

        checkApply(0.0, Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_X);
        checkApply(Angle.PI_OVER_TWO, Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_Y);
        checkApply(Math.PI, Vector2D.Unit.PLUS_X, Vector2D.Unit.MINUS_X);
        checkApply(THREE_PI_OVER_TWO, Vector2D.Unit.PLUS_X, Vector2D.Unit.MINUS_Y);
        checkApply(Angle.TWO_PI, Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_X);

        checkRotate(Rotation2D::of, Rotation2D::apply);
    }

    @Test
    void testApplyVector() {
        // act/assert
        checkApplyVector(1.0, Vector2D.ZERO, Vector2D.ZERO);

        checkApplyVector(0.0, Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_X);
        checkApplyVector(Angle.PI_OVER_TWO, Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_Y);
        checkApplyVector(Math.PI, Vector2D.Unit.PLUS_X, Vector2D.Unit.MINUS_X);
        checkApplyVector(THREE_PI_OVER_TWO, Vector2D.Unit.PLUS_X, Vector2D.Unit.MINUS_Y);
        checkApplyVector(Angle.TWO_PI, Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_X);

        checkRotate(Rotation2D::of, Rotation2D::applyVector);
    }

    @Test
    void testInverse_properties() {
        // arrange
        final Rotation2D orig = Rotation2D.of(100.0);

        // act
        final Rotation2D r = orig.inverse();

        // assert
        Assertions.assertEquals(-100.0, r.getAngle(), 0.0);
        Assertions.assertTrue(r.preservesOrientation());
    }

    @Test
    void testInverse_apply() {
        // arrange
        final Rotation2D orig = Rotation2D.of(100.0);
        final Rotation2D inv = orig.inverse();

        final Vector2D v1 = Vector2D.of(1, 2);
        final Vector2D v2 = Vector2D.of(-3, 4);
        final Vector2D v3 = Vector2D.of(-5, -6);
        final Vector2D v4 = Vector2D.of(7, -8);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(v1, orig.apply(inv.apply(v1)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(v2, inv.apply(orig.apply(v2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(v3, orig.apply(inv.apply(v3)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(v4, inv.apply(orig.apply(v4)), TEST_EPS);
    }

    @Test
    void testToMatrix() {
        // arrange
        final double angle = 0.1 * Math.PI;

        // act
        final AffineTransformMatrix2D m = Rotation2D.of(angle).toMatrix();

        // assert
        final double sin = Math.sin(angle);
        final double cos = Math.cos(angle);

        final double[] expected = {
            cos, -sin, 0,
            sin, cos, 0
        };
        Assertions.assertArrayEquals(expected, m.toArray(), 0.0);
    }

    @Test
    void testToMatrix_apply() {
        // act/assert
        checkRotate(angle -> Rotation2D.of(angle).toMatrix(), AffineTransformMatrix2D::apply);
    }

    @Test
    void testCreateRotationVector() {
        // arrange
        final double min = -8;
        final double max = 8;
        final double step = 1;

        EuclideanTestUtils.permuteSkipZero(min, max, step, (ux, uy) -> {
            EuclideanTestUtils.permuteSkipZero(min, max, step, (vx, vy) -> {

                final Vector2D u = Vector2D.of(ux, uy);
                final Vector2D v = Vector2D.of(vx, vy);

                // act
                final Rotation2D r = Rotation2D.createVectorRotation(u, v);

                // assert
                EuclideanTestUtils.assertCoordinatesEqual(v.normalize(), r.apply(u).normalize(), TEST_EPS); // u -> v
                Assertions.assertEquals(0.0, v.dot(r.apply(u.orthogonal())), TEST_EPS); // preserves orthogonality
            });
        });
    }

    @Test
    void testCreateRotationVector_invalidVectors() {
        // arrange
        final Vector2D vec = Vector2D.of(1, 1);

        final Vector2D zero = Vector2D.ZERO;
        final Vector2D nan = Vector2D.NaN;
        final Vector2D posInf = Vector2D.POSITIVE_INFINITY;
        final Vector2D negInf = Vector2D.POSITIVE_INFINITY;

        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Rotation2D.createVectorRotation(zero, vec));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Rotation2D.createVectorRotation(vec, zero));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Rotation2D.createVectorRotation(nan, vec));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Rotation2D.createVectorRotation(vec, nan));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Rotation2D.createVectorRotation(posInf, vec));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Rotation2D.createVectorRotation(vec, negInf));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Rotation2D.createVectorRotation(zero, nan));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Rotation2D.createVectorRotation(negInf, posInf));
    }

    @Test
    void testHashCode() {
        // arrange
        final Rotation2D a = Rotation2D.of(1.0);
        final Rotation2D b = Rotation2D.of(0.0);
        final Rotation2D c = Rotation2D.of(-1.0);
        final Rotation2D d = Rotation2D.of(1.0);

        final int hash = a.hashCode();

        // act/assert
        Assertions.assertEquals(hash, a.hashCode());

        Assertions.assertNotEquals(hash, b.hashCode());
        Assertions.assertNotEquals(hash, c.hashCode());

        Assertions.assertEquals(hash, d.hashCode());
    }

    @Test
    void testEquals() {
        // arrange
        final Rotation2D a = Rotation2D.of(1.0);
        final Rotation2D b = Rotation2D.of(0.0);
        final Rotation2D c = Rotation2D.of(-1.0);
        final Rotation2D d = Rotation2D.of(1.0);

        // act/assert
        GeometryTestUtils.assertSimpleEqualsCases(a);

        Assertions.assertNotEquals(a, b);
        Assertions.assertNotEquals(a, c);

        Assertions.assertEquals(a, d);
        Assertions.assertEquals(d, a);
    }

    @Test
    void testToString() {
        // arrange
        final Rotation2D r = Rotation2D.of(1.0);

        // act
        final String str = r.toString();

        // assert
        Assertions.assertEquals("Rotation2D[angle=1.0]", str);
    }

    private static void checkApply(final double angle, final Vector2D pt, final Vector2D expectedPt) {
        final Rotation2D r = Rotation2D.of(angle);
        EuclideanTestUtils.assertCoordinatesEqual(expectedPt, r.apply(pt), TEST_EPS);
    }

    private static void checkApplyVector(final double angle, final Vector2D pt, final Vector2D expectedPt) {
        final Rotation2D r = Rotation2D.of(angle);
        EuclideanTestUtils.assertCoordinatesEqual(expectedPt, r.applyVector(pt), TEST_EPS);
    }

    /** Check a rotation transform for consistency against a variety of points and rotation angles.
     * @param factory function used to create a rotation transform from an input angle
     * @param transformFn function that accepts the transform and a point and returns
     *      the transformed point
     */
    private static <T extends EuclideanTransform<Vector2D>> void checkRotate(
            final DoubleFunction<T> factory, final BiFunction<T, Vector2D, Vector2D> transformFn) {

        // check zero
        final T transform = factory.apply(0);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, transformFn.apply(transform, Vector2D.ZERO), TEST_EPS);

        // check a variety of non-zero points
        EuclideanTestUtils.permuteSkipZero(-2, -2, 1, (x, y) -> {
            checkRotatePoint(Vector2D.of(x, y), factory, transformFn);
        });
    }

    /** Check a rotation transform for consistency when transforming a single point against a
     * variety of rotation angles.
     * @param pt point to transform
     * @param factory function used to create a rotation transform from an input angle
     * @param transformFn function that accepts the transform and a point and returns
     *      the transformed point
     */
    private static <T extends EuclideanTransform<Vector2D>> void checkRotatePoint(
            final Vector2D pt, final DoubleFunction<T> factory, final BiFunction<T, ? super Vector2D, ? extends Vector2D> transformFn) {

        // arrange
        final double limit = 4 * Math.PI;
        final double inc = 0.25;

        final Line line = Lines.fromPointAndDirection(Vector2D.ZERO, pt, TEST_PRECISION);

        T transform;
        Vector2D resultPt;
        Line resultLine;
        for (double angle = -limit; angle < limit; angle += inc) {
            transform = factory.apply(angle);

            // act
            resultPt = transformFn.apply(transform, pt);

            // assert
            // check that the norm is unchanged
            Assertions.assertEquals(pt.norm(), resultPt.norm(), TEST_EPS);

            resultLine = Lines.fromPointAndDirection(Vector2D.ZERO, resultPt, TEST_PRECISION);
            final double lineAngle = line.angle(resultLine);

            // check that the angle is what we expect
            Assertions.assertEquals(Angle.Rad.WITHIN_MINUS_PI_AND_PI.applyAsDouble(angle), lineAngle, TEST_EPS);
        }
    }
}
