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

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils.PermuteCallback3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.threed.rotation.StandardRotations;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class AffineTransformMatrix3DTest {

    private static final double EPS = 1e-12;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(EPS);

    @Test
    public void testOf() {
        // arrange
        final double[] arr = {
            1, 2, 3, 4,
            5, 6, 7, 8,
            9, 10, 11, 12
        };

        // act
        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.of(arr);

        // assert
        final double[] result = transform.toArray();
        Assert.assertNotSame(arr, result);
        Assert.assertArrayEquals(arr, result, 0.0);
    }

    @Test
    public void testOf_invalidDimensions() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> AffineTransformMatrix3D.of(1, 2),
                IllegalArgumentException.class, "Dimension mismatch: 2 != 12");
    }

    @Test
    public void testFromColumnVectors_threeVectors() {
        // arrange
        final Vector3D u = Vector3D.of(1, 2, 3);
        final Vector3D v = Vector3D.of(4, 5, 6);
        final Vector3D w = Vector3D.of(7, 8, 9);

        // act
        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.fromColumnVectors(u, v, w);

        // assert
        Assert.assertArrayEquals(new double[] {
            1, 4, 7, 0,
            2, 5, 8, 0,
            3, 6, 9, 0
        }, transform.toArray(), 0.0);
    }

    @Test
    public void testFromColumnVectors_fourVectors() {
        // arrange
        final Vector3D u = Vector3D.of(1, 2, 3);
        final Vector3D v = Vector3D.of(4, 5, 6);
        final Vector3D w = Vector3D.of(7, 8, 9);
        final Vector3D t = Vector3D.of(10, 11, 12);

        // act
        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.fromColumnVectors(u, v, w, t);

        // assert
        Assert.assertArrayEquals(new double[] {
            1, 4, 7, 10,
            2, 5, 8, 11,
            3, 6, 9, 12
        }, transform.toArray(), 0.0);
    }

    @Test
    public void testFrom() {
        // act/assert
        Assert.assertArrayEquals(new double[] {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0
        }, AffineTransformMatrix3D.from(UnaryOperator.identity()).toArray(), EPS);
        Assert.assertArrayEquals(new double[] {
            1, 0, 0, 2,
            0, 1, 0, 3,
            0, 0, 1, -4
        }, AffineTransformMatrix3D.from(v -> v.add(Vector3D.of(2, 3, -4))).toArray(), EPS);
        Assert.assertArrayEquals(new double[] {
            3, 0, 0, 0,
            0, 3, 0, 0,
            0, 0, 3, 0
        }, AffineTransformMatrix3D.from(v -> v.multiply(3)).toArray(), EPS);
        Assert.assertArrayEquals(new double[] {
            3, 0, 0, 6,
            0, 3, 0, 9,
            0, 0, 3, 12
        }, AffineTransformMatrix3D.from(v -> v.add(Vector3D.of(2, 3, 4)).multiply(3)).toArray(), EPS);
    }

    @Test
    public void testFrom_invalidFunction() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix3D.from(v -> v.multiply(0));
        }, IllegalArgumentException.class);
    }

    @Test
    public void testIdentity() {
        // act
        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity();

        // assert
        final double[] expected = {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0
        };
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testCreateTranslation_xyz() {
        // act
        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.createTranslation(2, 3, 4);

        // assert
        final double[] expected = {
            1, 0, 0, 2,
            0, 1, 0, 3,
            0, 0, 1, 4
        };
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testCreateTranslation_vector() {
        // act
        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.createTranslation(Vector3D.of(5, 6, 7));

        // assert
        final double[] expected = {
            1, 0, 0, 5,
            0, 1, 0, 6,
            0, 0, 1, 7
        };
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testCreateScale_xyz() {
        // act
        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.createScale(2, 3, 4);

        // assert
        final double[] expected = {
            2, 0, 0, 0,
            0, 3, 0, 0,
            0, 0, 4, 0
        };
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testTranslate_xyz() {
        // arrange
        final AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    2, 0, 0, 10,
                    0, 3, 0, 11,
                    0, 0, 4, 12
                );

        // act
        final AffineTransformMatrix3D result = a.translate(4, 5, 6);

        // assert
        final double[] expected = {
            2, 0, 0, 14,
            0, 3, 0, 16,
            0, 0, 4, 18
        };
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testTranslate_vector() {
        // arrange
        final AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    2, 0, 0, 10,
                    0, 3, 0, 11,
                    0, 0, 4, 12
                );

        // act
        final AffineTransformMatrix3D result = a.translate(Vector3D.of(7, 8, 9));

        // assert
        final double[] expected = {
            2, 0, 0, 17,
            0, 3, 0, 19,
            0, 0, 4, 21
        };
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testCreateScale_vector() {
        // act
        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.createScale(Vector3D.of(4, 5, 6));

        // assert
        final double[] expected = {
            4, 0, 0, 0,
            0, 5, 0, 0,
            0, 0, 6, 0
        };
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testCreateScale_singleValue() {
        // act
        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.createScale(7);

        // assert
        final double[] expected = {
            7, 0, 0, 0,
            0, 7, 0, 0,
            0, 0, 7, 0
        };
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testScale_xyz() {
        // arrange
        final AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    2, 0, 0, 10,
                    0, 3, 0, 11,
                    0, 0, 4, 12
                );

        // act
        final AffineTransformMatrix3D result = a.scale(4, 5, 6);

        // assert
        final double[] expected = {
            8, 0, 0, 40,
            0, 15, 0, 55,
            0, 0, 24, 72
        };
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testScale_vector() {
        // arrange
        final AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    2, 0, 0, 10,
                    0, 3, 0, 11,
                    0, 0, 4, 12
                );

        // act
        final AffineTransformMatrix3D result = a.scale(Vector3D.of(7, 8, 9));

        // assert
        final double[] expected = {
            14, 0, 0, 70,
            0, 24, 0, 88,
            0, 0, 36, 108
        };
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testScale_singleValue() {
        // arrange
        final AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    2, 0, 0, 10,
                    0, 3, 0, 11,
                    0, 0, 4, 12
                );

        // act
        final AffineTransformMatrix3D result = a.scale(10);

        // assert
        final double[] expected = {
            20, 0, 0, 100,
            0, 30, 0, 110,
            0, 0, 40, 120
        };
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testCreateRotation() {
        // arrange
        final Vector3D center = Vector3D.of(1, 2, 3);
        final QuaternionRotation rotation = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, PlaneAngleRadians.PI_OVER_TWO);

        // act
        final AffineTransformMatrix3D result = AffineTransformMatrix3D.createRotation(center, rotation);

        // assert
        final double[] expected = {
            0, -1, 0, 3,
            1, 0, 0, 1,
            0, 0, 1, 0
        };
        Assert.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testRotate() {
        // arrange
        final AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    1, 2, 3, 4,
                    5, 6, 7, 8,
                    9, 10, 11, 12
                );

        final QuaternionRotation rotation = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, PlaneAngleRadians.PI_OVER_TWO);

        // act
        final AffineTransformMatrix3D result = a.rotate(rotation);

        // assert
        final double[] expected = {
            -5, -6, -7, -8,
            1, 2, 3, 4,
            9, 10, 11, 12
        };
        Assert.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testRotate_aroundCenter() {
        // arrange
        final AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    1, 2, 3, 4,
                    5, 6, 7, 8,
                    9, 10, 11, 12
                );

        final Vector3D center = Vector3D.of(1, 2, 3);
        final QuaternionRotation rotation = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, PlaneAngleRadians.PI_OVER_TWO);

        // act
        final AffineTransformMatrix3D result = a.rotate(center, rotation);

        // assert
        final double[] expected = {
            -5, -6, -7, -5,
            1, 2, 3, 5,
            9, 10, 11, 12
        };
        Assert.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testApply_identity() {
        // arrange
        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity();

        // act/assert
        runWithCoordinates((x, y, z) -> {
            final Vector3D v = Vector3D.of(x, y, z);

            EuclideanTestUtils.assertCoordinatesEqual(v, transform.apply(v), EPS);
        });
    }

    @Test
    public void testApply_translate() {
        // arrange
        final Vector3D translation = Vector3D.of(1.1, -PlaneAngleRadians.PI, 5.5);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .translate(translation);

        // act/assert
        runWithCoordinates((x, y, z) -> {
            final Vector3D vec = Vector3D.of(x, y, z);

            final Vector3D expectedVec = vec.add(translation);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_scale() {
        // arrange
        final Vector3D factors = Vector3D.of(2.0, -3.0, 4.0);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .scale(factors);

        // act/assert
        runWithCoordinates((x, y, z) -> {
            final Vector3D vec = Vector3D.of(x, y, z);

            final Vector3D expectedVec = Vector3D.of(factors.getX() * x, factors.getY() * y, factors.getZ() * z);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_translateThenScale() {
        // arrange
        final Vector3D translation = Vector3D.of(-2.0, -3.0, -4.0);
        final Vector3D scale = Vector3D.of(5.0, 6.0, 7.0);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .translate(translation)
                .scale(scale);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-5, -12, -21), transform.apply(Vector3D.of(1, 1, 1)), EPS);

        runWithCoordinates((x, y, z) -> {
            final Vector3D vec = Vector3D.of(x, y, z);

            final Vector3D expectedVec = Vector3D.of(
                        (x + translation.getX()) * scale.getX(),
                        (y + translation.getY()) * scale.getY(),
                        (z + translation.getZ()) * scale.getZ()
                    );

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_scaleThenTranslate() {
        // arrange
        final Vector3D scale = Vector3D.of(5.0, 6.0, 7.0);
        final Vector3D translation = Vector3D.of(-2.0, -3.0, -4.0);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .scale(scale)
                .translate(translation);

        // act/assert
        runWithCoordinates((x, y, z) -> {
            final Vector3D vec = Vector3D.of(x, y, z);

            final Vector3D expectedVec = Vector3D.of(
                        (x * scale.getX()) + translation.getX(),
                        (y * scale.getY()) + translation.getY(),
                        (z * scale.getZ()) + translation.getZ()
                    );

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_rotate() {
        // arrange
        final QuaternionRotation rotation = QuaternionRotation.fromAxisAngle(Vector3D.of(1, 1, 1), 2.0 * PlaneAngleRadians.PI / 3.0);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity().rotate(rotation);

        // act/assert
        runWithCoordinates((x, y, z) -> {
            final Vector3D vec = Vector3D.of(x, y, z);

            final Vector3D expectedVec = StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI.apply(vec);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_rotate_aroundCenter() {
        // arrange
        final double scaleFactor = 2;
        final Vector3D center = Vector3D.of(3, -4, 5);
        final QuaternionRotation rotation = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, PlaneAngleRadians.PI_OVER_TWO);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .scale(scaleFactor)
                .rotate(center, rotation);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, -3, 2), transform.apply(Vector3D.of(2, -2, 1)), EPS);

        runWithCoordinates((x, y, z) -> {
            final Vector3D vec = Vector3D.of(x, y, z);

            final Vector3D expectedVec = StandardRotations.PLUS_Z_HALF_PI.apply(vec.multiply(scaleFactor).subtract(center)).add(center);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApplyVector_identity() {
        // arrange
        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity();

        // act/assert
        runWithCoordinates((x, y, z) -> {
            final Vector3D v = Vector3D.of(x, y, z);

            EuclideanTestUtils.assertCoordinatesEqual(v, transform.applyVector(v), EPS);
        });
    }

    @Test
    public void testApplyVector_translate() {
        // arrange
        final Vector3D translation = Vector3D.of(1.1, -PlaneAngleRadians.PI, 5.5);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .translate(translation);

        // act/assert
        runWithCoordinates((x, y, z) -> {
            final Vector3D vec = Vector3D.of(x, y, z);

            EuclideanTestUtils.assertCoordinatesEqual(vec, transform.applyVector(vec), EPS);
        });
    }

    @Test
    public void testApplyVector_scale() {
        // arrange
        final Vector3D factors = Vector3D.of(2.0, -3.0, 4.0);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .scale(factors);

        // act/assert
        runWithCoordinates((x, y, z) -> {
            final Vector3D vec = Vector3D.of(x, y, z);

            final Vector3D expectedVec = Vector3D.of(factors.getX() * x, factors.getY() * y, factors.getZ() * z);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.applyVector(vec), EPS);
        });
    }

    @Test
    public void testApplyVector_representsDisplacement() {
        // arrange
        final Vector3D p1 = Vector3D.of(1, 2, 3);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .scale(1.5)
                .translate(4, 6, 5)
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, PlaneAngleRadians.PI_OVER_TWO));

        // act/assert
        runWithCoordinates((x, y, z) -> {
            final Vector3D p2 = Vector3D.of(x, y, z);
            final Vector3D input = p1.subtract(p2);

            final Vector3D expected = transform.apply(p1).subtract(transform.apply(p2));

            EuclideanTestUtils.assertCoordinatesEqual(expected, transform.applyVector(input), EPS);
        });
    }

    @Test
    public void testApplyDirection_identity() {
        // arrange
        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity();

        // act/assert
        EuclideanTestUtils.permuteSkipZero(-5, 5, 0.5, (x, y, z) -> {
            final Vector3D v = Vector3D.of(x, y, z);

            EuclideanTestUtils.assertCoordinatesEqual(v.normalize(), transform.applyDirection(v), EPS);
        });
    }

    @Test
    public void testApplyDirection_translate() {
        // arrange
        final Vector3D translation = Vector3D.of(1.1, -PlaneAngleRadians.PI, 5.5);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .translate(translation);

        // act/assert
        EuclideanTestUtils.permuteSkipZero(-5, 5, 0.5, (x, y, z) -> {
            final Vector3D vec = Vector3D.of(x, y, z);

            EuclideanTestUtils.assertCoordinatesEqual(vec.normalize(), transform.applyDirection(vec), EPS);
        });
    }

    @Test
    public void testApplyDirection_scale() {
        // arrange
        final Vector3D factors = Vector3D.of(2.0, -3.0, 4.0);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .scale(factors);

        // act/assert
        EuclideanTestUtils.permuteSkipZero(-5, 5, 0.5, (x, y, z) -> {
            final Vector3D vec = Vector3D.of(x, y, z);

            final Vector3D expectedVec = Vector3D.of(factors.getX() * x, factors.getY() * y, factors.getZ() * z).normalize();

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.applyDirection(vec), EPS);
        });
    }

    @Test
    public void testApplyDirection_representsNormalizedDisplacement() {
        // arrange
        final Vector3D p1 = Vector3D.of(1, 2, 3);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .scale(1.5)
                .translate(4, 6, 5)
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, PlaneAngleRadians.PI_OVER_TWO));

        // act/assert
        runWithCoordinates((x, y, z) -> {
            final Vector3D p2 = Vector3D.of(x, y, z);
            final Vector3D input = p1.subtract(p2);

            final Vector3D expected = transform.apply(p1).subtract(transform.apply(p2)).normalize();

            EuclideanTestUtils.assertCoordinatesEqual(expected, transform.applyDirection(input), EPS);
        });
    }

    @Test
    public void testApplyDirection_illegalNorm() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> AffineTransformMatrix3D.createScale(1, 0, 1).applyDirection(Vector3D.Unit.PLUS_Y),
                IllegalArgumentException.class);
        GeometryTestUtils.assertThrows(() -> AffineTransformMatrix3D.createScale(2).applyDirection(Vector3D.ZERO),
                IllegalArgumentException.class);
    }

    @Test
    public void testMultiply() {
        // arrange
        final AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    1, 2, 3, 4,
                    5, 6, 7, 8,
                    9, 10, 11, 12
                );
        final AffineTransformMatrix3D b = AffineTransformMatrix3D.of(
                    13, 14, 15, 16,
                    17, 18, 19, 20,
                    21, 22, 23, 24
                );

        // act
        final AffineTransformMatrix3D result = a.multiply(b);

        // assert
        final double[] arr = result.toArray();
        Assert.assertArrayEquals(new double[] {
            110, 116, 122, 132,
            314, 332, 350, 376,
            518, 548, 578, 620
        }, arr, EPS);
    }

    @Test
    public void testDeterminant() {
        // act/assert
        Assert.assertEquals(1.0, AffineTransformMatrix3D.identity().determinant(), EPS);
        Assert.assertEquals(1.0, AffineTransformMatrix3D.of(
                1, 0, 0, 10,
                0, 1, 0, 11,
                0, 0, 1, 12
            ).determinant(), EPS);
        Assert.assertEquals(-1.0, AffineTransformMatrix3D.of(
                -1, 0, 0, 10,
                0, 1, 0, 11,
                0, 0, 1, 12
            ).determinant(), EPS);
        Assert.assertEquals(1.0, AffineTransformMatrix3D.of(
                -1, 0, 0, 10,
                0, -1, 0, 11,
                0, 0, 1, 12
            ).determinant(), EPS);
        Assert.assertEquals(-1.0, AffineTransformMatrix3D.of(
                -1, 0, 0, 10,
                0, -1, 0, 11,
                0, 0, -1, 12
            ).determinant(), EPS);
        Assert.assertEquals(49.0, AffineTransformMatrix3D.of(
                2, -3, 1, 10,
                2, 0, -1, 11,
                1, 4, 5, -12
            ).determinant(), EPS);
        Assert.assertEquals(0.0, AffineTransformMatrix3D.of(
                1, 2, 3, 0,
                4, 5, 6, 0,
                7, 8, 9, 0
            ).determinant(), EPS);
    }

    @Test
    public void testPreservesOrientation() {
        // act/assert
        Assert.assertTrue(AffineTransformMatrix3D.identity().preservesOrientation());
        Assert.assertTrue(AffineTransformMatrix3D.of(
                1, 0, 0, 10,
                0, 1, 0, 11,
                0, 0, 1, 12
            ).preservesOrientation());
        Assert.assertTrue(AffineTransformMatrix3D.of(
                2, -3, 1, 10,
                2, 0, -1, 11,
                1, 4, 5, -12
            ).preservesOrientation());

        Assert.assertFalse(AffineTransformMatrix3D.of(
                -1, 0, 0, 10,
                0, 1, 0, 11,
                0, 0, 1, 12
            ).preservesOrientation());

        Assert.assertTrue(AffineTransformMatrix3D.of(
                -1, 0, 0, 10,
                0, -1, 0, 11,
                0, 0, 1, 12
            ).preservesOrientation());

        Assert.assertFalse(AffineTransformMatrix3D.of(
                -1, 0, 0, 10,
                0, -1, 0, 11,
                0, 0, -1, 12
            ).preservesOrientation());
        Assert.assertFalse(AffineTransformMatrix3D.of(
                1, 2, 3, 0,
                4, 5, 6, 0,
                7, 8, 9, 0
            ).preservesOrientation());
    }

    @Test
    public void testMultiply_combinesTransformOperations() {
        // arrange
        final Vector3D translation1 = Vector3D.of(1, 2, 3);
        final double scale = 2.0;
        final Vector3D translation2 = Vector3D.of(4, 5, 6);

        final AffineTransformMatrix3D a = AffineTransformMatrix3D.createTranslation(translation1);
        final AffineTransformMatrix3D b = AffineTransformMatrix3D.createScale(scale);
        final AffineTransformMatrix3D c = AffineTransformMatrix3D.identity();
        final AffineTransformMatrix3D d = AffineTransformMatrix3D.createTranslation(translation2);

        // act
        final AffineTransformMatrix3D transform = d.multiply(c).multiply(b).multiply(a);

        // assert
        runWithCoordinates((x, y, z) -> {
            final Vector3D vec = Vector3D.of(x, y, z);

            final Vector3D expectedVec = vec
                    .add(translation1)
                    .multiply(scale)
                    .add(translation2);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testPremultiply() {
        // arrange
        final AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    1, 2, 3, 4,
                    5, 6, 7, 8,
                    9, 10, 11, 12
                );
        final AffineTransformMatrix3D b = AffineTransformMatrix3D.of(
                    13, 14, 15, 16,
                    17, 18, 19, 20,
                    21, 22, 23, 24
                );

        // act
        final AffineTransformMatrix3D result = b.premultiply(a);

        // assert
        final double[] arr = result.toArray();
        Assert.assertArrayEquals(new double[] {
            110, 116, 122, 132,
            314, 332, 350, 376,
            518, 548, 578, 620
        }, arr, EPS);
    }

    @Test
    public void testPremultiply_combinesTransformOperations() {
        // arrange
        final Vector3D translation1 = Vector3D.of(1, 2, 3);
        final double scale = 2.0;
        final Vector3D translation2 = Vector3D.of(4, 5, 6);

        final AffineTransformMatrix3D a = AffineTransformMatrix3D.createTranslation(translation1);
        final AffineTransformMatrix3D b = AffineTransformMatrix3D.createScale(scale);
        final AffineTransformMatrix3D c = AffineTransformMatrix3D.identity();
        final AffineTransformMatrix3D d = AffineTransformMatrix3D.createTranslation(translation2);

        // act
        final AffineTransformMatrix3D transform = a.premultiply(b).premultiply(c).premultiply(d);

        // assert
        runWithCoordinates((x, y, z) -> {
            final Vector3D vec = Vector3D.of(x, y, z);

            final Vector3D expectedVec = vec
                    .add(translation1)
                    .multiply(scale)
                    .add(translation2);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testInverse_identity() {
        // act
        final AffineTransformMatrix3D inverse = AffineTransformMatrix3D.identity().inverse();

        // assert
        final double[] expected = {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0
        };
        Assert.assertArrayEquals(expected, inverse.toArray(), 0.0);
    }

    @Test
    public void testInverse_multiplyByInverse_producesIdentity() {
        // arrange
        final AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    1, 3, 7, 8,
                    2, 4, 9, 12,
                    5, 6, 10, 11
                );

        final AffineTransformMatrix3D inv = a.inverse();

        // act
        final AffineTransformMatrix3D result = inv.multiply(a);

        // assert
        final double[] expected = {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0
        };
        Assert.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testInverse_translate() {
        // arrange
        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.createTranslation(1, -2, 4);

        // act
        final AffineTransformMatrix3D inverse = transform.inverse();

        // assert
        final double[] expected = {
            1, 0, 0, -1,
            0, 1, 0, 2,
            0, 0, 1, -4
        };
        Assert.assertArrayEquals(expected, inverse.toArray(), 0.0);
    }

    @Test
    public void testInverse_scale() {
        // arrange
        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.createScale(10, -2, 4);

        // act
        final AffineTransformMatrix3D inverse = transform.inverse();

        // assert
        final double[] expected = {
            0.1, 0, 0, 0,
            0, -0.5, 0, 0,
            0, 0, 0.25, 0
        };
        Assert.assertArrayEquals(expected, inverse.toArray(), 0.0);
    }

    @Test
    public void testInverse_rotate() {
        // arrange
        final Vector3D center = Vector3D.of(1, 2, 3);
        final QuaternionRotation rotation = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, PlaneAngleRadians.PI_OVER_TWO);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.createRotation(center, rotation);

        // act
        final AffineTransformMatrix3D inverse = transform.inverse();

        // assert
        final double[] expected = {
            0, 1, 0, -1,
            -1, 0, 0, 3,
            0, 0, 1, 0
        };
        Assert.assertArrayEquals(expected, inverse.toArray(), EPS);
    }

    @Test
    public void testInverse_undoesOriginalTransform() {
        // arrange
        final Vector3D v1 = Vector3D.ZERO;
        final Vector3D v2 = Vector3D.Unit.PLUS_X;
        final Vector3D v3 = Vector3D.of(1, 1, 1);
        final Vector3D v4 = Vector3D.of(-2, 3, 4);

        final Vector3D center = Vector3D.of(1, 2, 3);
        final QuaternionRotation rotation = QuaternionRotation.fromAxisAngle(Vector3D.of(1, 2, 3), 0.25);

        // act/assert
        runWithCoordinates((x, y, z) -> {
            final AffineTransformMatrix3D transform = AffineTransformMatrix3D
                        .createTranslation(x, y, z)
                        .scale(2, 3, 4)
                        .rotate(center, rotation)
                        .translate(x / 3, y / 3, z / 3);

            final AffineTransformMatrix3D inverse = transform.inverse();

            EuclideanTestUtils.assertCoordinatesEqual(v1, inverse.apply(transform.apply(v1)), EPS);
            EuclideanTestUtils.assertCoordinatesEqual(v2, inverse.apply(transform.apply(v2)), EPS);
            EuclideanTestUtils.assertCoordinatesEqual(v3, inverse.apply(transform.apply(v3)), EPS);
            EuclideanTestUtils.assertCoordinatesEqual(v4, inverse.apply(transform.apply(v4)), EPS);
        });
    }

    @Test
    public void testInverse_nonInvertible() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix3D.of(
                    0, 0, 0, 0,
                    0, 0, 0, 0,
                    0, 0, 0, 0).inverse();
        }, IllegalStateException.class, "Matrix is not invertible; matrix determinant is 0.0");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix3D.of(
                    1, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, Double.NaN, 0).inverse();
        }, IllegalStateException.class, "Matrix is not invertible; matrix determinant is NaN");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix3D.of(
                    1, 0, 0, 0,
                    0, Double.NEGATIVE_INFINITY, 0, 0,
                    0, 0, 1, 0).inverse();
        }, IllegalStateException.class, "Matrix is not invertible; matrix determinant is NaN");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix3D.of(
                    Double.POSITIVE_INFINITY, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, 1, 0).inverse();
        }, IllegalStateException.class, "Matrix is not invertible; matrix determinant is NaN");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix3D.of(
                    1, 0, 0, Double.NaN,
                    0, 1, 0, 0,
                    0, 0, 1, 0).inverse();
        }, IllegalStateException.class, "Matrix is not invertible; invalid matrix element: NaN");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix3D.of(
                    1, 0, 0, 0,
                    0, 1, 0, Double.POSITIVE_INFINITY,
                    0, 0, 1, 0).inverse();
        }, IllegalStateException.class, "Matrix is not invertible; invalid matrix element: Infinity");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix3D.of(
                    1, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, 1, Double.NEGATIVE_INFINITY).inverse();
        }, IllegalStateException.class, "Matrix is not invertible; invalid matrix element: -Infinity");
    }

    @Test
    public void testLinear() {
        // arrange
        final AffineTransformMatrix3D mat = AffineTransformMatrix3D.of(
                2, 3, 4, 5,
                6, 7, 8, 9,
                10, 11, 12, 13);

        // act
        final AffineTransformMatrix3D result = mat.linear();

        // assert
        final double[] expected = {
            2, 3, 4, 0,
            6, 7, 8, 0,
            10, 11, 12, 0
        };
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testLinearTranspose() {
        // arrange
        final AffineTransformMatrix3D mat = AffineTransformMatrix3D.of(
                2, 3, 4, 5,
                6, 7, 8, 9,
                10, 11, 12, 13);

        // act
        final AffineTransformMatrix3D result = mat.linearTranspose();

        // assert
        final double[] expected = {
            2, 6, 10, 0,
            3, 7, 11, 0,
            4, 8, 12, 0
        };
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testNormalTransform() {
        // act/assert
        checkNormalTransform(AffineTransformMatrix3D.identity());

        checkNormalTransform(AffineTransformMatrix3D.createTranslation(2, 3, 4));
        checkNormalTransform(AffineTransformMatrix3D.createTranslation(-3, -4, -5));

        checkNormalTransform(AffineTransformMatrix3D.createScale(2, 5, 0.5));
        checkNormalTransform(AffineTransformMatrix3D.createScale(-3, 4, 2));
        checkNormalTransform(AffineTransformMatrix3D.createScale(-0.1, -0.5, 0.8));
        checkNormalTransform(AffineTransformMatrix3D.createScale(-2, -5, -8));

        final QuaternionRotation rotA = QuaternionRotation.fromAxisAngle(Vector3D.of(2, 3, 4), 0.75 * Math.PI);
        final QuaternionRotation rotB = QuaternionRotation.fromAxisAngle(Vector3D.of(-1, 1, -1), 1.75 * Math.PI);

        checkNormalTransform(AffineTransformMatrix3D.createRotation(Vector3D.of(1, 1, 1), rotA));
        checkNormalTransform(AffineTransformMatrix3D.createRotation(Vector3D.of(-1, -1, -1), rotB));

        checkNormalTransform(AffineTransformMatrix3D.createTranslation(2, 3, 4)
                .scale(7, 5, 4)
                .rotate(rotA));
        checkNormalTransform(AffineTransformMatrix3D.createRotation(Vector3D.ZERO, rotB)
                .translate(7, 5, 4)
                .rotate(rotA)
                .scale(2, 3, 0.5));
    }

    private void checkNormalTransform(final AffineTransformMatrix3D transform) {
        final AffineTransformMatrix3D normalTransform = transform.normalTransform();

        final Vector3D p1 = Vector3D.of(-0.25, 0.75, 0.5);
        final Vector3D p2 = Vector3D.of(0.5, -0.75, 0.25);

        final Vector3D t1 = transform.apply(p1);
        final Vector3D t2 = transform.apply(p2);

        EuclideanTestUtils.permute(-10, 10, 1, (x, y, z) -> {
            final Vector3D p3 = Vector3D.of(x, y, z);
            final Vector3D n = Planes.fromPoints(p1, p2, p3, TEST_PRECISION).getNormal();

            final Vector3D t3 = transform.apply(p3);

            final Plane tPlane = transform.preservesOrientation() ?
                    Planes.fromPoints(t1, t2, t3, TEST_PRECISION) :
                    Planes.fromPoints(t1, t3, t2, TEST_PRECISION);
            final Vector3D expected = tPlane.getNormal();

            final Vector3D actual = normalTransform.apply(n).normalize();

            EuclideanTestUtils.assertCoordinatesEqual(expected, actual, EPS);
        });
    }

    @Test
    public void testNormalTransform_nonInvertible() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix3D.createScale(0).normalTransform();
        }, IllegalStateException.class);
    }

    @Test
    public void testHashCode() {
        // arrange
        final double[] values = {
            1, 2, 3, 4,
            5, 6, 7, 8,
            9, 10, 11, 12
        };

        // act/assert
        final int orig = AffineTransformMatrix3D.of(values).hashCode();
        final int same = AffineTransformMatrix3D.of(values).hashCode();

        Assert.assertEquals(orig, same);

        double[] temp;
        for (int i = 0; i < values.length; ++i) {
            temp = values.clone();
            temp[i] = 0;

            final int modified = AffineTransformMatrix3D.of(temp).hashCode();

            Assert.assertNotEquals(orig, modified);
        }
    }

    @Test
    public void testEquals() {
        // arrange
        final double[] values = {
            1, 2, 3, 4,
            5, 6, 7, 8,
            9, 10, 11, 12
        };

        final AffineTransformMatrix3D a = AffineTransformMatrix3D.of(values);

        // act/assert
        Assert.assertEquals(a, a);

        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        double[] temp;
        for (int i = 0; i < values.length; ++i) {
            temp = values.clone();
            temp[i] = 0;

            final AffineTransformMatrix3D modified = AffineTransformMatrix3D.of(temp);

            Assert.assertNotEquals(a, modified);
        }
    }

    @Test
    public void testToString() {
        // arrange
        final AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    1, 2, 3, 4,
                    5, 6, 7, 8,
                    9, 10, 11, 12
                );

        // act
        final String result = a.toString();

        // assert
        Assert.assertEquals(
                "[ 1.0, 2.0, 3.0, 4.0; " +
                "5.0, 6.0, 7.0, 8.0; " +
                "9.0, 10.0, 11.0, 12.0 ]", result);
    }

    /**
     * Run the given test callback with a wide range of (x, y, z) inputs.
     * @param test
     */
    private static void runWithCoordinates(final PermuteCallback3D test) {
        EuclideanTestUtils.permute(-1e-2, 1e-2, 5e-3, test);
        EuclideanTestUtils.permute(-1e2, 1e2, 5, test);
    }
}
