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

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.twod.rotation.Rotation2D;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class AffineTransformMatrix2DTest {

    private static final double EPS = 1e-12;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(EPS);

    @Test
    public void testOf() {
        // arrange
        double[] arr = {
            1, 2, 3,
            4, 5, 6
        };

        // act
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.of(arr);

        // assert
        double[] result = transform.toArray();
        Assert.assertNotSame(arr, result);
        Assert.assertArrayEquals(arr, result, 0.0);
    }

    @Test
    public void testOf_invalidDimensions() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> AffineTransformMatrix2D.of(1, 2),
                IllegalArgumentException.class, "Dimension mismatch: 2 != 6");
    }

    @Test
    public void testFromColumnVectors_twoVector() {
        // arrange
        Vector2D u = Vector2D.of(1, 2);
        Vector2D v = Vector2D.of(3, 4);

        // act
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.fromColumnVectors(u, v);

        // assert
        Assert.assertArrayEquals(new double[] {
            1, 3, 0,
            2, 4, 0
        }, transform.toArray(), 0.0);
    }

    @Test
    public void testFromColumnVectors_threeVectors() {
        // arrange
        Vector2D u = Vector2D.of(1, 2);
        Vector2D v = Vector2D.of(3, 4);
        Vector2D t = Vector2D.of(5, 6);

        // act
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.fromColumnVectors(u, v, t);

        // assert
        Assert.assertArrayEquals(new double[] {
            1, 3, 5,
            2, 4, 6
        }, transform.toArray(), 0.0);
    }

    @Test
    public void testIdentity() {
        // act
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity();

        // assert
        double[] expected = {
            1, 0, 0,
            0, 1, 0
        };
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testFrom() {
        // act/assert
        Assert.assertArrayEquals(new double[] {
            1, 0, 0,
            0, 1, 0
        }, AffineTransformMatrix2D.from(UnaryOperator.identity()).toArray(), EPS);
        Assert.assertArrayEquals(new double[] {
            1, 0, 2,
            0, 1, 3
        }, AffineTransformMatrix2D.from(v -> v.add(Vector2D.of(2, 3))).toArray(), EPS);
        Assert.assertArrayEquals(new double[] {
            3, 0, 0,
            0, 3, 0
        }, AffineTransformMatrix2D.from(v -> v.multiply(3)).toArray(), EPS);
        Assert.assertArrayEquals(new double[] {
            3, 0, 6,
            0, 3, 9
        }, AffineTransformMatrix2D.from(v -> v.add(Vector2D.of(2, 3)).multiply(3)).toArray(), EPS);
    }

    @Test
    public void testFrom_invalidFunction() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix2D.from(v -> v.multiply(0));
        }, IllegalArgumentException.class);
    }

    @Test
    public void testCreateTranslation_xy() {
        // act
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.createTranslation(2, 3);

        // assert
        double[] expected = {
            1, 0, 2,
            0, 1, 3
        };
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testCreateTranslation_vector() {
        // act
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.createTranslation(Vector2D.of(5, 6));

        // assert
        double[] expected = {
            1, 0, 5,
            0, 1, 6
        };
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testCreateScale_xy() {
        // act
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.createScale(2, 3);

        // assert
        double[] expected = {
            2, 0, 0,
            0, 3, 0
        };
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testTranslate_xy() {
        // arrange
        AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    2, 0, 10,
                    0, 3, 11
                );

        // act
        AffineTransformMatrix2D result = a.translate(4, 5);

        // assert
        double[] expected = {
            2, 0, 14,
            0, 3, 16
        };
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testTranslate_vector() {
        // arrange
        AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    2, 0, 10,
                    0, 3, 11
                );

        // act
        AffineTransformMatrix2D result = a.translate(Vector2D.of(7, 8));

        // assert
        double[] expected = {
            2, 0, 17,
            0, 3, 19
        };
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testCreateScale_vector() {
        // act
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.createScale(Vector2D.of(4, 5));

        // assert
        double[] expected = {
            4, 0, 0,
            0, 5, 0
        };
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testCreateScale_singleValue() {
        // act
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.createScale(7);

        // assert
        double[] expected = {
            7, 0, 0,
            0, 7, 0
        };
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testScale_xy() {
        // arrange
        AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    2, 0, 10,
                    0, 3, 11
                );

        // act
        AffineTransformMatrix2D result = a.scale(4, 5);

        // assert
        double[] expected = {
            8, 0, 40,
            0, 15, 55
        };
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testScale_vector() {
        // arrange
        AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    2, 0, 10,
                    0, 3, 11
                );

        // act
        AffineTransformMatrix2D result = a.scale(Vector2D.of(7, 8));

        // assert
        double[] expected = {
            14, 0, 70,
            0, 24, 88
        };
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testScale_singleValue() {
        // arrange
        AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    2, 0, 10,
                    0, 3, 11
                );

        // act
        AffineTransformMatrix2D result = a.scale(10);

        // assert
        double[] expected = {
            20, 0, 100,
            0, 30, 110
        };
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testCreateRotation() {
        // act
        double angle = PlaneAngleRadians.PI * 2.0 / 3.0;
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.createRotation(angle);

        // assert
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        double[] expected = {
            cos, -sin, 0,
            sin, cos, 0
        };
        Assert.assertArrayEquals(expected, transform.toArray(), EPS);
    }

    @Test
    public void testCreateRotation_aroundCenter_rawAngle() {
        // act
        Vector2D center = Vector2D.of(1, 2);
        double angle = PlaneAngleRadians.PI * 2.0 / 3.0;
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.createRotation(center, angle);

        // assert
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        double[] expected = {
            cos, -sin, -cos + (2 * sin) + 1,
            sin, cos, -sin - (2 * cos) + 2
        };
        Assert.assertArrayEquals(expected, transform.toArray(), EPS);
    }

    @Test
    public void testCreateRotation_aroundCenter_rotationInstance() {
        // act
        Vector2D center = Vector2D.of(1, 2);
        double angle = PlaneAngleRadians.PI * 4.0 / 3.0;
        Rotation2D rotation = Rotation2D.of(angle);
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.createRotation(center, rotation);

        // assert
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        double[] expected = {
            cos, -sin, -cos + (2 * sin) + 1,
            sin, cos, -sin - (2 * cos) + 2
        };
        Assert.assertArrayEquals(expected, transform.toArray(), EPS);
    }

    @Test
    public void testRotate_rawAngle() {
        // arrange
        AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    1, 2, 3,
                    4, 5, 6
                );

        // act
        AffineTransformMatrix2D result = a.rotate(PlaneAngleRadians.PI_OVER_TWO);

        // assert
        double[] expected = {
            -4, -5, -6,
            1, 2, 3
        };
        Assert.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testRotate_rotationInstance() {
        // arrange
        AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    1, 2, 3,
                    4, 5, 6
                );

        // act
        AffineTransformMatrix2D result = a.rotate(Rotation2D.of(PlaneAngleRadians.PI_OVER_TWO));

        // assert
        double[] expected = {
            -4, -5, -6,
            1, 2, 3
        };
        Assert.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testRotate_aroundCenter_rawAngle() {
        // arrange
        Vector2D center = Vector2D.of(1, 2);

        AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    1, 2, 3,
                    4, 5, 6
                );

        // act
        AffineTransformMatrix2D result = a.rotate(center, PlaneAngleRadians.PI_OVER_TWO);

        // assert
        double[] expected = {
            -4, -5, -3,
            1, 2, 4
        };
        Assert.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testRotate_aroundCenter_rotationInstance() {
        // arrange
        Vector2D center = Vector2D.of(1, 2);

        AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    1, 2, 3,
                    4, 5, 6
                );

        // act
        AffineTransformMatrix2D result = a.rotate(center, Rotation2D.of(PlaneAngleRadians.PI_OVER_TWO));

        // assert
        double[] expected = {
            -4, -5, -3,
            1, 2, 4
        };
        Assert.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testApply_identity() {
        // arrange
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity();

        // act/assert
        runWithCoordinates((x, y) -> {
            Vector2D v = Vector2D.of(x, y);

            EuclideanTestUtils.assertCoordinatesEqual(v, transform.apply(v), EPS);
        });
    }

    @Test
    public void testApply_translate() {
        // arrange
        Vector2D translation = Vector2D.of(1.1, -PlaneAngleRadians.PI);

        AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .translate(translation);

        // act/assert
        runWithCoordinates((x, y) -> {
            Vector2D vec = Vector2D.of(x, y);

            Vector2D expectedVec = vec.add(translation);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_scale() {
        // arrange
        Vector2D factors = Vector2D.of(2.0, -3.0);

        AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .scale(factors);

        // act/assert
        runWithCoordinates((x, y) -> {
            Vector2D vec = Vector2D.of(x, y);

            Vector2D expectedVec = Vector2D.of(factors.getX() * x, factors.getY() * y);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_rotate() {
        // arrange
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .rotate(-PlaneAngleRadians.PI_OVER_TWO);

        // act/assert
        runWithCoordinates((x, y) -> {
            Vector2D vec = Vector2D.of(x, y);

            Vector2D expectedVec = Vector2D.of(y, -x);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_rotate_aroundCenter_minusHalfPi() {
        // arrange
        Vector2D center = Vector2D.of(1, 2);
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .rotate(center, -PlaneAngleRadians.PI_OVER_TWO);

        // act/assert
        runWithCoordinates((x, y) -> {
            Vector2D vec = Vector2D.of(x, y);

            Vector2D centered = vec.subtract(center);
            Vector2D expectedVec = Vector2D.of(centered.getY(), -centered.getX()).add(center);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_rotate_aroundCenter_pi() {
        // arrange
        Vector2D center = Vector2D.of(1, 2);
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .rotate(center, PlaneAngleRadians.PI);

        // act/assert
        runWithCoordinates((x, y) -> {
            Vector2D vec = Vector2D.of(x, y);

            Vector2D centered = vec.subtract(center);
            Vector2D expectedVec = Vector2D.of(-centered.getX(), -centered.getY()).add(center);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_translateScaleRotate() {
        // arrange
        Vector2D translation = Vector2D.of(-2.0, -3.0);
        Vector2D scale = Vector2D.of(5.0, 6.0);

        AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .translate(translation)
                .scale(scale)
                .rotate(PlaneAngleRadians.PI_OVER_TWO);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(12, -5), transform.apply(Vector2D.of(1, 1)), EPS);

        runWithCoordinates((x, y) -> {
            Vector2D vec = Vector2D.of(x, y);

            Vector2D temp = Vector2D.of(
                        (x + translation.getX()) * scale.getX(),
                        (y + translation.getY()) * scale.getY()
                    );
            Vector2D expectedVec = Vector2D.of(-temp.getY(), temp.getX());

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_scaleTranslateRotate() {
        // arrange
        Vector2D scale = Vector2D.of(5.0, 6.0);
        Vector2D translation = Vector2D.of(-2.0, -3.0);

        AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .scale(scale)
                .translate(translation)
                .rotate(-PlaneAngleRadians.PI_OVER_TWO);

        // act/assert
        runWithCoordinates((x, y) -> {
            Vector2D vec = Vector2D.of(x, y);

            Vector2D temp = Vector2D.of(
                        (x * scale.getX()) + translation.getX(),
                        (y * scale.getY()) + translation.getY()
                    );
            Vector2D expectedVec = Vector2D.of(temp.getY(), -temp.getX());

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApplyVector_identity() {
        // arrange
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity();

        // act/assert
        runWithCoordinates((x, y) -> {
            Vector2D v = Vector2D.of(x, y);

            EuclideanTestUtils.assertCoordinatesEqual(v, transform.applyVector(v), EPS);
        });
    }

    @Test
    public void testApplyVector_translate() {
        // arrange
        Vector2D translation = Vector2D.of(1.1, -PlaneAngleRadians.PI);

        AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .translate(translation);

        // act/assert
        runWithCoordinates((x, y) -> {
            Vector2D vec = Vector2D.of(x, y);

            EuclideanTestUtils.assertCoordinatesEqual(vec, transform.applyVector(vec), EPS);
        });
    }

    @Test
    public void testApplyVector_scale() {
        // arrange
        Vector2D factors = Vector2D.of(2.0, -3.0);

        AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .scale(factors);

        // act/assert
        runWithCoordinates((x, y) -> {
            Vector2D vec = Vector2D.of(x, y);

            Vector2D expectedVec = Vector2D.of(factors.getX() * x, factors.getY() * y);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.applyVector(vec), EPS);
        });
    }

    @Test
    public void testApplyVector_representsDisplacement() {
        // arrange
        Vector2D p1 = Vector2D.of(2, 3);

        AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .scale(1.5)
                .translate(4, 6)
                .rotate(PlaneAngleRadians.PI_OVER_TWO);

        // act/assert
        runWithCoordinates((x, y) -> {
            Vector2D p2 = Vector2D.of(x, y);
            Vector2D input = p1.subtract(p2);

            Vector2D expected = transform.apply(p1).subtract(transform.apply(p2));

            EuclideanTestUtils.assertCoordinatesEqual(expected, transform.applyVector(input), EPS);
        });
    }

    @Test
    public void testApplyDirection_identity() {
        // arrange
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity();

        // act/assert
        EuclideanTestUtils.permuteSkipZero(-5, 5, 0.5, (x, y) -> {
            Vector2D v = Vector2D.of(x, y);

            EuclideanTestUtils.assertCoordinatesEqual(v.normalize(), transform.applyDirection(v), EPS);
        });
    }

    @Test
    public void testApplyDirection_translate() {
        // arrange
        Vector2D translation = Vector2D.of(1.1, -PlaneAngleRadians.PI);

        AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .translate(translation);

        // act/assert
        EuclideanTestUtils.permuteSkipZero(-5, 5, 0.5, (x, y) -> {
            Vector2D vec = Vector2D.of(x, y);

            EuclideanTestUtils.assertCoordinatesEqual(vec.normalize(), transform.applyDirection(vec), EPS);
        });
    }

    @Test
    public void testApplyDirection_scale() {
        // arrange
        Vector2D factors = Vector2D.of(2.0, -3.0);

        AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .scale(factors);

        // act/assert
        EuclideanTestUtils.permuteSkipZero(-5, 5, 0.5, (x, y) -> {
            Vector2D vec = Vector2D.of(x, y);

            Vector2D expectedVec = Vector2D.of(factors.getX() * x, factors.getY() * y).normalize();

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.applyDirection(vec), EPS);
        });
    }

    @Test
    public void testApplyDirection_representsNormalizedDisplacement() {
        // arrange
        Vector2D p1 = Vector2D.of(2.1, 3.2);

        AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .scale(1.5)
                .translate(4, 6)
                .rotate(PlaneAngleRadians.PI_OVER_TWO);

        // act/assert
        EuclideanTestUtils.permute(-5, 5, 0.5, (x, y) -> {
            Vector2D p2 = Vector2D.of(x, y);
            Vector2D input = p1.subtract(p2);

            Vector2D expected = transform.apply(p1).subtract(transform.apply(p2)).normalize();

            EuclideanTestUtils.assertCoordinatesEqual(expected, transform.applyDirection(input), EPS);
        });
    }

    @Test
    public void testApplyDirection_illegalNorm() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> AffineTransformMatrix2D.createScale(1, 0).applyDirection(Vector2D.Unit.PLUS_Y),
                IllegalArgumentException.class);
        GeometryTestUtils.assertThrows(() -> AffineTransformMatrix2D.createScale(2).applyDirection(Vector2D.ZERO),
                IllegalArgumentException.class);
    }

    @Test
    public void testDeterminant() {
        // act/assert
        Assert.assertEquals(1.0, AffineTransformMatrix2D.identity().determinant(), EPS);
        Assert.assertEquals(6.0, AffineTransformMatrix2D.of(
                2, 0, 4,
                0, 3, 5
            ).determinant(), EPS);
        Assert.assertEquals(-6.0, AffineTransformMatrix2D.of(
                2, 0, 4,
                0, -3, 5
            ).determinant(), EPS);
        Assert.assertEquals(-5.0, AffineTransformMatrix2D.of(
                1, 3, 0,
                2, 1, 0
            ).determinant(), EPS);
        Assert.assertEquals(-0.0, AffineTransformMatrix2D.of(
                0, 0, 1,
                0, 0, 2
            ).determinant(), EPS);
    }

    @Test
    public void testPreservesOrientation() {
        // act/assert
        Assert.assertTrue(AffineTransformMatrix2D.identity().preservesOrientation());
        Assert.assertTrue(AffineTransformMatrix2D.of(
                2, 0, 4,
                0, 3, 5
            ).preservesOrientation());

        Assert.assertFalse(AffineTransformMatrix2D.of(
                2, 0, 4,
                0, -3, 5
            ).preservesOrientation());
        Assert.assertFalse(AffineTransformMatrix2D.of(
                1, 3, 0,
                2, 1, 0
            ).preservesOrientation());
        Assert.assertFalse(AffineTransformMatrix2D.of(
                0, 0, 1,
                0, 0, 2
            ).preservesOrientation());
    }

    @Test
    public void testMultiply() {
        // arrange
        AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    1, 2, 3,
                    5, 6, 7
                );
        AffineTransformMatrix2D b = AffineTransformMatrix2D.of(
                    13, 14, 15,
                    17, 18, 19
                );

        // act
        AffineTransformMatrix2D result = a.multiply(b);

        // assert
        double[] arr = result.toArray();
        Assert.assertArrayEquals(new double[] {
            47, 50, 56,
            167, 178, 196
        }, arr, EPS);
    }

    @Test
    public void testMultiply_combinesTransformOperations() {
        // arrange
        Vector2D translation1 = Vector2D.of(1, 2);
        double scale = 2.0;
        Vector2D translation2 = Vector2D.of(4, 5);

        AffineTransformMatrix2D a = AffineTransformMatrix2D.createTranslation(translation1);
        AffineTransformMatrix2D b = AffineTransformMatrix2D.createScale(scale);
        AffineTransformMatrix2D c = AffineTransformMatrix2D.identity();
        AffineTransformMatrix2D d = AffineTransformMatrix2D.createTranslation(translation2);

        // act
        AffineTransformMatrix2D transform = d.multiply(c).multiply(b).multiply(a);

        // assert
        runWithCoordinates((x, y) -> {
            Vector2D vec = Vector2D.of(x, y);

            Vector2D expectedVec = vec
                    .add(translation1)
                    .multiply(scale)
                    .add(translation2);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testPremultiply() {
        // arrange
        AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    1, 2, 3,
                    5, 6, 7
                );
        AffineTransformMatrix2D b = AffineTransformMatrix2D.of(
                    13, 14, 15,
                    17, 18, 19
                );

        // act
        AffineTransformMatrix2D result = b.premultiply(a);

        // assert
        double[] arr = result.toArray();
        Assert.assertArrayEquals(new double[] {
            47, 50, 56,
            167, 178, 196
        }, arr, EPS);
    }

    @Test
    public void testPremultiply_combinesTransformOperations() {
        // arrange
        Vector2D translation1 = Vector2D.of(1, 2);
        double scale = 2.0;
        Vector2D translation2 = Vector2D.of(4, 5);

        AffineTransformMatrix2D a = AffineTransformMatrix2D.createTranslation(translation1);
        AffineTransformMatrix2D b = AffineTransformMatrix2D.createScale(scale);
        AffineTransformMatrix2D c = AffineTransformMatrix2D.identity();
        AffineTransformMatrix2D d = AffineTransformMatrix2D.createTranslation(translation2);

        // act
        AffineTransformMatrix2D transform = a.premultiply(b).premultiply(c).premultiply(d);

        // assert
        runWithCoordinates((x, y) -> {
            Vector2D vec = Vector2D.of(x, y);

            Vector2D expectedVec = vec
                    .add(translation1)
                    .multiply(scale)
                    .add(translation2);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testInverse_identity() {
        // act
        AffineTransformMatrix2D inverse = AffineTransformMatrix2D.identity().inverse();

        // assert
        double[] expected = {
            1, 0, 0,
            0, 1, 0
        };
        Assert.assertArrayEquals(expected, inverse.toArray(), 0.0);
    }

    @Test
    public void testInverse_multiplyByInverse_producesIdentity() {
        // arrange
        AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    1, 3, 7,
                    2, 4, 9
                );

        AffineTransformMatrix2D inv = a.inverse();

        // act
        AffineTransformMatrix2D result = inv.multiply(a);

        // assert
        double[] expected = {
            1, 0, 0,
            0, 1, 0
        };
        Assert.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testInverse_translate() {
        // arrange
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.createTranslation(1, -2);

        // act
        AffineTransformMatrix2D inverse = transform.inverse();

        // assert
        double[] expected = {
            1, 0, -1,
            0, 1, 2
        };
        Assert.assertArrayEquals(expected, inverse.toArray(), 0.0);
    }

    @Test
    public void testInverse_scale() {
        // arrange
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.createScale(10, -2);

        // act
        AffineTransformMatrix2D inverse = transform.inverse();

        // assert
        double[] expected = {
            0.1, 0, 0,
            0, -0.5, 0
        };
        Assert.assertArrayEquals(expected, inverse.toArray(), 0.0);
    }

    @Test
    public void testInverse_rotate() {
        // arrange
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.createRotation(PlaneAngleRadians.PI_OVER_TWO);

        // act
        AffineTransformMatrix2D inverse = transform.inverse();

        // assert
        double[] expected = {
            0, 1, 0,
            -1, 0, 0
        };
        Assert.assertArrayEquals(expected, inverse.toArray(), EPS);
    }

    @Test
    public void testInverse_rotate_aroundCenter() {
        // arrange
        Vector2D center = Vector2D.of(1, 2);
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.createRotation(center, PlaneAngleRadians.PI_OVER_TWO);

        // act
        AffineTransformMatrix2D inverse = transform.inverse();

        // assert
        double[] expected = {
            0, 1, -1,
            -1, 0, 3
        };
        Assert.assertArrayEquals(expected, inverse.toArray(), EPS);
    }

    @Test
    public void testInverse_undoesOriginalTransform() {
        // arrange
        Vector2D v1 = Vector2D.ZERO;
        Vector2D v2 = Vector2D.Unit.PLUS_X;
        Vector2D v3 = Vector2D.of(1, 1);
        Vector2D v4 = Vector2D.of(-2, 3);

        Vector2D center = Vector2D.of(-0.5, 2);

        // act/assert
        runWithCoordinates((x, y) -> {
            AffineTransformMatrix2D transform = AffineTransformMatrix2D
                        .createTranslation(x, y)
                        .scale(2, 3)
                        .translate(x / 3, y / 3)
                        .rotate(x / 4)
                        .rotate(center, y / 2);

            AffineTransformMatrix2D inverse = transform.inverse();

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
            AffineTransformMatrix2D.of(
                    0, 0, 0,
                    0, 0, 0).inverse();
        }, IllegalStateException.class, "Matrix is not invertible; matrix determinant is 0.0");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix2D.of(
                    1, 0, 0,
                    0, Double.NaN, 0).inverse();
        }, IllegalStateException.class, "Matrix is not invertible; matrix determinant is NaN");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix2D.of(
                    1, 0, 0,
                    0, Double.NEGATIVE_INFINITY, 0).inverse();
        }, IllegalStateException.class, "Matrix is not invertible; matrix determinant is -Infinity");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix2D.of(
                    Double.POSITIVE_INFINITY, 0, 0,
                    0, 1, 0).inverse();
        }, IllegalStateException.class, "Matrix is not invertible; matrix determinant is Infinity");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix2D.of(
                    1, 0, Double.NaN,
                    0, 1, 0).inverse();
        }, IllegalStateException.class, "Matrix is not invertible; invalid matrix element: NaN");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix2D.of(
                    1, 0, Double.POSITIVE_INFINITY,
                    0, 1, 0).inverse();
        }, IllegalStateException.class, "Matrix is not invertible; invalid matrix element: Infinity");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix2D.of(
                    1, 0, Double.NEGATIVE_INFINITY,
                    0, 1, 0).inverse();
        }, IllegalStateException.class, "Matrix is not invertible; invalid matrix element: -Infinity");
    }

    @Test
    public void testLinear() {
        // arrange
        AffineTransformMatrix2D mat = AffineTransformMatrix2D.of(
                2, 3, 4,
                5, 6, 7);

        // act
        AffineTransformMatrix2D result = mat.linear();

        // assert
        double[] expected = {
            2, 3, 0,
            5, 6, 0
        };
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testLinearTranspose() {
        // arrange
        AffineTransformMatrix2D mat = AffineTransformMatrix2D.of(
                2, 3, 4,
                5, 6, 7);

        // act
        AffineTransformMatrix2D result = mat.linearTranspose();

        // assert
        double[] expected = {
            2, 5, 0,
            3, 6, 0
        };
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testNormalTransform() {
        // act/assert
        checkNormalTransform(AffineTransformMatrix2D.identity());

        checkNormalTransform(AffineTransformMatrix2D.createTranslation(2, 3));
        checkNormalTransform(AffineTransformMatrix2D.createTranslation(-3, -4));

        checkNormalTransform(AffineTransformMatrix2D.createScale(2, 5));
        checkNormalTransform(AffineTransformMatrix2D.createScale(-3, 4));
        checkNormalTransform(AffineTransformMatrix2D.createScale(-2, -5));

        checkNormalTransform(AffineTransformMatrix2D.createRotation(PlaneAngleRadians.PI_OVER_TWO));
        checkNormalTransform(AffineTransformMatrix2D.createRotation(PlaneAngleRadians.THREE_PI_OVER_TWO));

        checkNormalTransform(AffineTransformMatrix2D.createRotation(Vector2D.of(3, 4), PlaneAngleRadians.THREE_PI_OVER_TWO)
                .translate(8, 2)
                .scale(-3, -2));
        checkNormalTransform(AffineTransformMatrix2D.createScale(2, -1)
                .translate(-3, -4)
                .rotate(Vector2D.of(-0.5, 0.5), 0.75 * Math.PI));
    }

    private void checkNormalTransform(AffineTransformMatrix2D transform) {
        AffineTransformMatrix2D normalTransform = transform.normalTransform();

        Vector2D p1 = Vector2D.of(-0.25, 0.75);
        Vector2D t1 = transform.apply(p1);

        EuclideanTestUtils.permute(-10, 10, 1, (x, y) -> {
            Vector2D p2 = Vector2D.of(x, y);
            Vector2D n = Lines.fromPoints(p1, p2, TEST_PRECISION).getOffsetDirection();

            Vector2D t2 = transform.apply(p2);

            Line tLine = transform.preservesOrientation() ?
                    Lines.fromPoints(t1, t2, TEST_PRECISION) :
                    Lines.fromPoints(t2, t1, TEST_PRECISION);
            Vector2D expected = tLine.getOffsetDirection();

            Vector2D actual = normalTransform.apply(n).normalize();

            EuclideanTestUtils.assertCoordinatesEqual(expected, actual, EPS);
        });
    }

    @Test
    public void testNormalTransform_nonInvertible() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix2D.createScale(0).normalTransform();
        }, IllegalStateException.class);
    }

    @Test
    public void testHashCode() {
        // arrange
        double[] values = {
            1, 2, 3,
            5, 6, 7
        };

        // act/assert
        int orig = AffineTransformMatrix2D.of(values).hashCode();
        int same = AffineTransformMatrix2D.of(values).hashCode();

        Assert.assertEquals(orig, same);

        double[] temp;
        for (int i = 0; i < values.length; ++i) {
            temp = values.clone();
            temp[i] = 0;

            int modified = AffineTransformMatrix2D.of(temp).hashCode();

            Assert.assertNotEquals(orig, modified);
        }
    }

    @Test
    public void testEquals() {
        // arrange
        double[] values = {
            1, 2, 3,
            5, 6, 7
        };

        AffineTransformMatrix2D a = AffineTransformMatrix2D.of(values);

        // act/assert
        Assert.assertEquals(a, a);

        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        double[] temp;
        for (int i = 0; i < values.length; ++i) {
            temp = values.clone();
            temp[i] = 0;

            AffineTransformMatrix2D modified = AffineTransformMatrix2D.of(temp);

            Assert.assertNotEquals(a, modified);
        }
    }

    @Test
    public void testToString() {
        // arrange
        AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    1, 2, 3,
                    5, 6, 7
                );

        // act
        String result = a.toString();

        // assert
        Assert.assertEquals(
                "[ 1.0, 2.0, 3.0; " +
                "5.0, 6.0, 7.0 ]", result);
    }

    @FunctionalInterface
    private interface Coordinate2DTest {

        void run(double x, double y);
    }

    private static void runWithCoordinates(Coordinate2DTest test) {
        runWithCoordinates(test, -1e-2, 1e-2, 5e-3);
        runWithCoordinates(test, -1e2, 1e2, 5);
    }

    private static void runWithCoordinates(Coordinate2DTest test, double min, double max, double step) {
        for (double x = min; x <= max; x += step) {
            for (double y = min; y <= max; y += step) {
                test.run(x, y);
            }
        }
    }
}
