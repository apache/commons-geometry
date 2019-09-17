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

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.exception.IllegalNormException;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils.PermuteCallback3D;
import org.apache.commons.geometry.euclidean.exception.NonInvertibleTransformException;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.threed.rotation.StandardRotations;
import org.junit.Assert;
import org.junit.Test;

public class AffineTransformMatrix3DTest {

    private static final double EPS = 1e-12;

    @Test
    public void testOf() {
        // arrange
        double[] arr = {
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12
        };

        // act
        AffineTransformMatrix3D transform = AffineTransformMatrix3D.of(arr);

        // assert
        double[] result = transform.toArray();
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
        Vector3D u = Vector3D.of(1, 2, 3);
        Vector3D v = Vector3D.of(4, 5, 6);
        Vector3D w = Vector3D.of(7, 8, 9);

        // act
        AffineTransformMatrix3D transform = AffineTransformMatrix3D.fromColumnVectors(u, v, w);

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
        Vector3D u = Vector3D.of(1, 2, 3);
        Vector3D v = Vector3D.of(4, 5, 6);
        Vector3D w = Vector3D.of(7, 8, 9);
        Vector3D t = Vector3D.of(10, 11, 12);

        // act
        AffineTransformMatrix3D transform = AffineTransformMatrix3D.fromColumnVectors(u, v, w, t);

        // assert
        Assert.assertArrayEquals(new double[] {
                1, 4, 7, 10,
                2, 5, 8, 11,
                3, 6, 9, 12
        }, transform.toArray(), 0.0);
    }

    @Test
    public void testIdentity() {
        // act
        AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity();

        // assert
        double[] expected = {
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0
        };
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testCreateTranslation_xyz() {
        // act
        AffineTransformMatrix3D transform = AffineTransformMatrix3D.createTranslation(2, 3, 4);

        // assert
        double[] expected = {
                1, 0, 0, 2,
                0, 1, 0, 3,
                0, 0, 1, 4
        };
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testCreateTranslation_vector() {
        // act
        AffineTransformMatrix3D transform = AffineTransformMatrix3D.createTranslation(Vector3D.of(5, 6, 7));

        // assert
        double[] expected = {
                1, 0, 0, 5,
                0, 1, 0, 6,
                0, 0, 1, 7
        };
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testCreateScale_xyz() {
        // act
        AffineTransformMatrix3D transform = AffineTransformMatrix3D.createScale(2, 3, 4);

        // assert
        double[] expected = {
                2, 0, 0, 0,
                0, 3, 0, 0,
                0, 0, 4, 0
        };
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testTranslate_xyz() {
        // arrange
        AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    2, 0, 0, 10,
                    0, 3, 0, 11,
                    0, 0, 4, 12
                );

        // act
        AffineTransformMatrix3D result = a.translate(4, 5, 6);

        // assert
        double[] expected = {
                2, 0, 0, 14,
                0, 3, 0, 16,
                0, 0, 4, 18
        };
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testTranslate_vector() {
        // arrange
        AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    2, 0, 0, 10,
                    0, 3, 0, 11,
                    0, 0, 4, 12
                );

        // act
        AffineTransformMatrix3D result = a.translate(Vector3D.of(7, 8, 9));

        // assert
        double[] expected = {
                2, 0, 0, 17,
                0, 3, 0, 19,
                0, 0, 4, 21
        };
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testCreateScale_vector() {
        // act
        AffineTransformMatrix3D transform = AffineTransformMatrix3D.createScale(Vector3D.of(4, 5, 6));

        // assert
        double[] expected = {
                4, 0, 0, 0,
                0, 5, 0, 0,
                0, 0, 6, 0
        };
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testCreateScale_singleValue() {
        // act
        AffineTransformMatrix3D transform = AffineTransformMatrix3D.createScale(7);

        // assert
        double[] expected = {
                7, 0, 0, 0,
                0, 7, 0, 0,
                0, 0, 7, 0
        };
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testScale_xyz() {
        // arrange
        AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    2, 0, 0, 10,
                    0, 3, 0, 11,
                    0, 0, 4, 12
                );

        // act
        AffineTransformMatrix3D result = a.scale(4, 5, 6);

        // assert
        double[] expected = {
                8, 0, 0, 40,
                0, 15, 0, 55,
                0, 0, 24, 72
        };
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testScale_vector() {
        // arrange
        AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    2, 0, 0, 10,
                    0, 3, 0, 11,
                    0, 0, 4, 12
                );

        // act
        AffineTransformMatrix3D result = a.scale(Vector3D.of(7, 8, 9));

        // assert
        double[] expected = {
                14, 0, 0, 70,
                0, 24, 0, 88,
                0, 0, 36, 108
        };
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testScale_singleValue() {
        // arrange
        AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    2, 0, 0, 10,
                    0, 3, 0, 11,
                    0, 0, 4, 12
                );

        // act
        AffineTransformMatrix3D result = a.scale(10);

        // assert
        double[] expected = {
                20, 0, 0, 100,
                0, 30, 0, 110,
                0, 0, 40, 120
        };
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testCreateRotation() {
        // arrange
        Vector3D center = Vector3D.of(1, 2, 3);
        QuaternionRotation rotation = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Z, Geometry.HALF_PI);

        // act
        AffineTransformMatrix3D result = AffineTransformMatrix3D.createRotation(center, rotation);

        // assert
        double[] expected = {
                0, -1, 0, 3,
                1, 0, 0, 1,
                0, 0, 1, 0
        };
        Assert.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testRotate() {
        // arrange
        AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    1, 2, 3, 4,
                    5, 6, 7, 8,
                    9, 10, 11, 12
                );

        QuaternionRotation rotation = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Z, Geometry.HALF_PI);

        // act
        AffineTransformMatrix3D result = a.rotate(rotation);

        // assert
        double[] expected = {
                -5, -6, -7, -8,
                1, 2, 3, 4,
                9, 10, 11, 12
        };
        Assert.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testRotate_aroundCenter() {
        // arrange
        AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    1, 2, 3, 4,
                    5, 6, 7, 8,
                    9, 10, 11, 12
                );

        Vector3D center = Vector3D.of(1, 2, 3);
        QuaternionRotation rotation = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Z, Geometry.HALF_PI);

        // act
        AffineTransformMatrix3D result = a.rotate(center, rotation);

        // assert
        double[] expected = {
                -5, -6, -7, -5,
                1, 2, 3, 5,
                9, 10, 11, 12
        };
        Assert.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testApply_identity() {
        // arrange
        AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity();

        // act/assert
        runWithCoordinates((x, y, z) -> {
            Vector3D v = Vector3D.of(x, y, z);

            EuclideanTestUtils.assertCoordinatesEqual(v, transform.apply(v), EPS);
        });
    }

    @Test
    public void testApply_translate() {
        // arrange
        Vector3D translation = Vector3D.of(1.1, -Geometry.PI, 5.5);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .translate(translation);

        // act/assert
        runWithCoordinates((x, y, z) -> {
            Vector3D vec = Vector3D.of(x, y, z);

            Vector3D expectedVec = vec.add(translation);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_scale() {
        // arrange
        Vector3D factors = Vector3D.of(2.0, -3.0, 4.0);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .scale(factors);

        // act/assert
        runWithCoordinates((x, y, z) -> {
            Vector3D vec = Vector3D.of(x, y, z);

            Vector3D expectedVec = Vector3D.of(factors.getX() * x, factors.getY() * y, factors.getZ() * z);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_translateThenScale() {
        // arrange
        Vector3D translation = Vector3D.of(-2.0, -3.0, -4.0);
        Vector3D scale = Vector3D.of(5.0, 6.0, 7.0);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .translate(translation)
                .scale(scale);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-5, -12, -21), transform.apply(Vector3D.of(1, 1, 1)), EPS);

        runWithCoordinates((x, y, z) -> {
            Vector3D vec = Vector3D.of(x, y, z);

            Vector3D expectedVec = Vector3D.of(
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
        Vector3D scale = Vector3D.of(5.0, 6.0, 7.0);
        Vector3D translation = Vector3D.of(-2.0, -3.0, -4.0);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .scale(scale)
                .translate(translation);

        // act/assert
        runWithCoordinates((x, y, z) -> {
            Vector3D vec = Vector3D.of(x, y, z);

            Vector3D expectedVec = Vector3D.of(
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
        QuaternionRotation rotation = QuaternionRotation.fromAxisAngle(Vector3D.of(1, 1, 1), 2.0 * Geometry.PI / 3.0);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity().rotate(rotation);

        // act/assert
        runWithCoordinates((x, y, z) -> {
            Vector3D vec = Vector3D.of(x, y, z);

            Vector3D expectedVec = StandardRotations.PLUS_DIAGONAL_TWO_THIRDS_PI.apply(vec);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_rotate_aroundCenter() {
        // arrange
        double scaleFactor = 2;
        Vector3D center = Vector3D.of(3, -4, 5);
        QuaternionRotation rotation = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Z, Geometry.HALF_PI);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .scale(scaleFactor)
                .rotate(center, rotation);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, -3, 2), transform.apply(Vector3D.of(2, -2, 1)), EPS);

        runWithCoordinates((x, y, z) -> {
            Vector3D vec = Vector3D.of(x, y, z);

            Vector3D expectedVec = StandardRotations.PLUS_Z_HALF_PI.apply(vec.multiply(scaleFactor).subtract(center)).add(center);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApplyVector_identity() {
        // arrange
        AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity();

        // act/assert
        runWithCoordinates((x, y, z) -> {
            Vector3D v = Vector3D.of(x, y, z);

            EuclideanTestUtils.assertCoordinatesEqual(v, transform.applyVector(v), EPS);
        });
    }

    @Test
    public void testApplyVector_translate() {
        // arrange
        Vector3D translation = Vector3D.of(1.1, -Geometry.PI, 5.5);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .translate(translation);

        // act/assert
        runWithCoordinates((x, y, z) -> {
            Vector3D vec = Vector3D.of(x, y, z);

            EuclideanTestUtils.assertCoordinatesEqual(vec, transform.applyVector(vec), EPS);
        });
    }

    @Test
    public void testApplyVector_scale() {
        // arrange
        Vector3D factors = Vector3D.of(2.0, -3.0, 4.0);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .scale(factors);

        // act/assert
        runWithCoordinates((x, y, z) -> {
            Vector3D vec = Vector3D.of(x, y, z);

            Vector3D expectedVec = Vector3D.of(factors.getX() * x, factors.getY() * y, factors.getZ() * z);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.applyVector(vec), EPS);
        });
    }

    @Test
    public void testApplyVector_representsDisplacement() {
        // arrange
        Vector3D p1 = Vector3D.of(1, 2, 3);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .scale(1.5)
                .translate(4, 6, 5)
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Z, Geometry.HALF_PI));

        // act/assert
        runWithCoordinates((x, y, z) -> {
            Vector3D p2 = Vector3D.of(x, y, z);
            Vector3D input = p1.subtract(p2);

            Vector3D expected = transform.apply(p1).subtract(transform.apply(p2));

            EuclideanTestUtils.assertCoordinatesEqual(expected, transform.applyVector(input), EPS);
        });
    }

    @Test
    public void testApplyDirection_identity() {
        // arrange
        AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity();

        // act/assert
        EuclideanTestUtils.permuteSkipZero(-5, 5, 0.5, (x, y, z) -> {
            Vector3D v = Vector3D.of(x, y, z);

            EuclideanTestUtils.assertCoordinatesEqual(v.normalize(), transform.applyDirection(v), EPS);
        });
    }

    @Test
    public void testApplyDirection_translate() {
        // arrange
        Vector3D translation = Vector3D.of(1.1, -Geometry.PI, 5.5);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .translate(translation);

        // act/assert
        EuclideanTestUtils.permuteSkipZero(-5, 5, 0.5, (x, y, z) -> {
            Vector3D vec = Vector3D.of(x, y, z);

            EuclideanTestUtils.assertCoordinatesEqual(vec.normalize(), transform.applyDirection(vec), EPS);
        });
    }

    @Test
    public void testApplyDirection_scale() {
        // arrange
        Vector3D factors = Vector3D.of(2.0, -3.0, 4.0);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .scale(factors);

        // act/assert
        EuclideanTestUtils.permuteSkipZero(-5, 5, 0.5, (x, y, z) -> {
            Vector3D vec = Vector3D.of(x, y, z);

            Vector3D expectedVec = Vector3D.of(factors.getX() * x, factors.getY() * y, factors.getZ() * z).normalize();

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.applyDirection(vec), EPS);
        });
    }

    @Test
    public void testApplyDirection_representsNormalizedDisplacement() {
        // arrange
        Vector3D p1 = Vector3D.of(1, 2, 3);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .scale(1.5)
                .translate(4, 6, 5)
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Z, Geometry.HALF_PI));

        // act/assert
        runWithCoordinates((x, y, z) -> {
            Vector3D p2 = Vector3D.of(x, y, z);
            Vector3D input = p1.subtract(p2);

            Vector3D expected = transform.apply(p1).subtract(transform.apply(p2)).normalize();

            EuclideanTestUtils.assertCoordinatesEqual(expected, transform.applyDirection(input), EPS);
        });
    }

    @Test
    public void testApplyDirection_illegalNorm() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> AffineTransformMatrix3D.createScale(1, 0, 1).applyDirection(Vector3D.PLUS_Y),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> AffineTransformMatrix3D.createScale(2).applyDirection(Vector3D.ZERO),
                IllegalNormException.class);
    }

    @Test
    public void testMultiply() {
        // arrange
        AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    1, 2, 3, 4,
                    5, 6, 7, 8,
                    9, 10, 11, 12
                );
        AffineTransformMatrix3D b = AffineTransformMatrix3D.of(
                    13, 14, 15, 16,
                    17, 18, 19, 20,
                    21, 22, 23, 24
                );

        // act
        AffineTransformMatrix3D result = a.multiply(b);

        // assert
        double[] arr = result.toArray();
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
    public void testToMatrix() {
        // arrange
        AffineTransformMatrix3D m = AffineTransformMatrix3D.createScale(3);

        // act/assert
        Assert.assertSame(m, m.toMatrix());
    }

    @Test
    public void testMultiply_combinesTransformOperations() {
        // arrange
        Vector3D translation1 = Vector3D.of(1, 2, 3);
        double scale = 2.0;
        Vector3D translation2 = Vector3D.of(4, 5, 6);

        AffineTransformMatrix3D a = AffineTransformMatrix3D.createTranslation(translation1);
        AffineTransformMatrix3D b = AffineTransformMatrix3D.createScale(scale);
        AffineTransformMatrix3D c = AffineTransformMatrix3D.identity();
        AffineTransformMatrix3D d = AffineTransformMatrix3D.createTranslation(translation2);

        // act
        AffineTransformMatrix3D transform = d.multiply(c).multiply(b).multiply(a);

        // assert
        runWithCoordinates((x, y, z) -> {
            Vector3D vec = Vector3D.of(x, y, z);

            Vector3D expectedVec = vec
                    .add(translation1)
                    .multiply(scale)
                    .add(translation2);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testPremultiply() {
        // arrange
        AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    1, 2, 3, 4,
                    5, 6, 7, 8,
                    9, 10, 11, 12
                );
        AffineTransformMatrix3D b = AffineTransformMatrix3D.of(
                    13, 14, 15, 16,
                    17, 18, 19, 20,
                    21, 22, 23, 24
                );

        // act
        AffineTransformMatrix3D result = b.premultiply(a);

        // assert
        double[] arr = result.toArray();
        Assert.assertArrayEquals(new double[] {
                110, 116, 122, 132,
                314, 332, 350, 376,
                518, 548, 578, 620
        }, arr, EPS);
    }

    @Test
    public void testPremultiply_combinesTransformOperations() {
        // arrange
        Vector3D translation1 = Vector3D.of(1, 2, 3);
        double scale = 2.0;
        Vector3D translation2 = Vector3D.of(4, 5, 6);

        AffineTransformMatrix3D a = AffineTransformMatrix3D.createTranslation(translation1);
        AffineTransformMatrix3D b = AffineTransformMatrix3D.createScale(scale);
        AffineTransformMatrix3D c = AffineTransformMatrix3D.identity();
        AffineTransformMatrix3D d = AffineTransformMatrix3D.createTranslation(translation2);

        // act
        AffineTransformMatrix3D transform = a.premultiply(b).premultiply(c).premultiply(d);

        // assert
        runWithCoordinates((x, y, z) -> {
            Vector3D vec = Vector3D.of(x, y, z);

            Vector3D expectedVec = vec
                    .add(translation1)
                    .multiply(scale)
                    .add(translation2);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testInverse_identity() {
        // act
        AffineTransformMatrix3D inverse = AffineTransformMatrix3D.identity().inverse();

        // assert
        double[] expected = {
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0
        };
        Assert.assertArrayEquals(expected, inverse.toArray(), 0.0);
    }

    @Test
    public void testInverse_multiplyByInverse_producesIdentity() {
        // arrange
        AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    1, 3, 7, 8,
                    2, 4, 9, 12,
                    5, 6, 10, 11
                );

        AffineTransformMatrix3D inv = a.inverse();

        // act
        AffineTransformMatrix3D result = inv.multiply(a);

        // assert
        double[] expected = {
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0
        };
        Assert.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testInverse_translate() {
        // arrange
        AffineTransformMatrix3D transform = AffineTransformMatrix3D.createTranslation(1, -2, 4);

        // act
        AffineTransformMatrix3D inverse = transform.inverse();

        // assert
        double[] expected = {
                1, 0, 0, -1,
                0, 1, 0, 2,
                0, 0, 1, -4
        };
        Assert.assertArrayEquals(expected, inverse.toArray(), 0.0);
    }

    @Test
    public void testInverse_scale() {
        // arrange
        AffineTransformMatrix3D transform = AffineTransformMatrix3D.createScale(10, -2, 4);

        // act
        AffineTransformMatrix3D inverse = transform.inverse();

        // assert
        double[] expected = {
                0.1, 0, 0, 0,
                0, -0.5, 0, 0,
                0, 0, 0.25, 0
        };
        Assert.assertArrayEquals(expected, inverse.toArray(), 0.0);
    }

    @Test
    public void testInverse_rotate() {
        // arrange
        Vector3D center = Vector3D.of(1, 2, 3);
        QuaternionRotation rotation = QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Z, Geometry.HALF_PI);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.createRotation(center, rotation);

        // act
        AffineTransformMatrix3D inverse = transform.inverse();

        // assert
        double[] expected = {
                0, 1, 0, -1,
                -1, 0, 0, 3,
                0, 0, 1, 0
        };
        Assert.assertArrayEquals(expected, inverse.toArray(), EPS);
    }

    @Test
    public void testInverse_undoesOriginalTransform() {
        // arrange
        Vector3D v1 = Vector3D.ZERO;
        Vector3D v2 = Vector3D.PLUS_X;
        Vector3D v3 = Vector3D.of(1, 1, 1);
        Vector3D v4 = Vector3D.of(-2, 3, 4);

        Vector3D center = Vector3D.of(1, 2, 3);
        QuaternionRotation rotation = QuaternionRotation.fromAxisAngle(Vector3D.of(1, 2, 3), 0.25);

        // act/assert
        runWithCoordinates((x, y, z) -> {
            AffineTransformMatrix3D transform = AffineTransformMatrix3D
                        .createTranslation(x, y, z)
                        .scale(2, 3, 4)
                        .rotate(center, rotation)
                        .translate(x / 3, y / 3, z / 3);

            AffineTransformMatrix3D inverse = transform.inverse();

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
        }, NonInvertibleTransformException.class, "Transform is not invertible; matrix determinant is 0.0");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix3D.of(
                    1, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, Double.NaN, 0).inverse();
        }, NonInvertibleTransformException.class, "Transform is not invertible; matrix determinant is NaN");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix3D.of(
                    1, 0, 0, 0,
                    0, Double.NEGATIVE_INFINITY, 0, 0,
                    0, 0, 1, 0).inverse();
        }, NonInvertibleTransformException.class, "Transform is not invertible; matrix determinant is NaN");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix3D.of(
                    Double.POSITIVE_INFINITY, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, 1, 0).inverse();
        }, NonInvertibleTransformException.class, "Transform is not invertible; matrix determinant is NaN");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix3D.of(
                    1, 0, 0, Double.NaN,
                    0, 1, 0, 0,
                    0, 0, 1, 0).inverse();
        }, NonInvertibleTransformException.class, "Transform is not invertible; invalid matrix element: NaN");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix3D.of(
                    1, 0, 0, 0,
                    0, 1, 0, Double.POSITIVE_INFINITY,
                    0, 0, 1, 0).inverse();
        }, NonInvertibleTransformException.class, "Transform is not invertible; invalid matrix element: Infinity");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix3D.of(
                    1, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, 1, Double.NEGATIVE_INFINITY).inverse();
        }, NonInvertibleTransformException.class, "Transform is not invertible; invalid matrix element: -Infinity");
    }

    @Test
    public void testHashCode() {
        // arrange
        double[] values = new double[] {
            1, 2, 3, 4,
            5, 6, 7, 8,
            9, 10, 11, 12
        };

        // act/assert
        int orig = AffineTransformMatrix3D.of(values).hashCode();
        int same = AffineTransformMatrix3D.of(values).hashCode();

        Assert.assertEquals(orig, same);

        double[] temp;
        for (int i=0; i<values.length; ++i) {
           temp = values.clone();
           temp[i] = 0;

           int modified = AffineTransformMatrix3D.of(temp).hashCode();

           Assert.assertNotEquals(orig, modified);
        }
    }

    @Test
    public void testEquals() {
        // arrange
        double[] values = new double[] {
            1, 2, 3, 4,
            5, 6, 7, 8,
            9, 10, 11, 12
        };

        AffineTransformMatrix3D a = AffineTransformMatrix3D.of(values);

        // act/assert
        Assert.assertTrue(a.equals(a));

        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        double[] temp;
        for (int i=0; i<values.length; ++i) {
           temp = values.clone();
           temp[i] = 0;

           AffineTransformMatrix3D modified = AffineTransformMatrix3D.of(temp);

           Assert.assertFalse(a.equals(modified));
        }
    }

    @Test
    public void testToString() {
        // arrange
        AffineTransformMatrix3D a = AffineTransformMatrix3D.of(
                    1, 2, 3, 4,
                    5, 6, 7, 8,
                    9, 10, 11, 12
                );

        // act
        String result = a.toString();

        // assert
        Assert.assertEquals("[ 1.0, 2.0, 3.0, 4.0; "
                + "5.0, 6.0, 7.0, 8.0; "
                + "9.0, 10.0, 11.0, 12.0 ]", result);
    }

    /**
     * Run the given test callback with a wide range of (x, y, z) inputs.
     * @param test
     */
    private static void runWithCoordinates(PermuteCallback3D test) {
        EuclideanTestUtils.permute(-1e-2, 1e-2, 5e-3, test);
        EuclideanTestUtils.permute(-1e2, 1e2, 5, test);
    }
}
