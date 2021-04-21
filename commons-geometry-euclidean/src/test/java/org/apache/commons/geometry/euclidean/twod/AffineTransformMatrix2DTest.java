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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AffineTransformMatrix2DTest {

    private static final double EPS = 1e-12;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(EPS);

    @Test
    public void testOf() {
        // arrange
        final double[] arr = {
            1, 2, 3,
            4, 5, 6
        };

        // act
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.of(arr);

        // assert
        final double[] result = transform.toArray();
        Assertions.assertNotSame(arr, result);
        Assertions.assertArrayEquals(arr, result, 0.0);
    }

    @Test
    public void testOf_invalidDimensions() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> AffineTransformMatrix2D.of(1, 2),
                IllegalArgumentException.class, "Dimension mismatch: 2 != 6");
    }

    @Test
    public void testFromColumnVectors_twoVector() {
        // arrange
        final Vector2D u = Vector2D.of(1, 2);
        final Vector2D v = Vector2D.of(3, 4);

        // act
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.fromColumnVectors(u, v);

        // assert
        Assertions.assertArrayEquals(new double[] {
            1, 3, 0,
            2, 4, 0
        }, transform.toArray(), 0.0);
    }

    @Test
    public void testFromColumnVectors_threeVectors() {
        // arrange
        final Vector2D u = Vector2D.of(1, 2);
        final Vector2D v = Vector2D.of(3, 4);
        final Vector2D t = Vector2D.of(5, 6);

        // act
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.fromColumnVectors(u, v, t);

        // assert
        Assertions.assertArrayEquals(new double[] {
            1, 3, 5,
            2, 4, 6
        }, transform.toArray(), 0.0);
    }

    @Test
    public void testIdentity() {
        // act
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity();

        // assert
        final double[] expected = {
            1, 0, 0,
            0, 1, 0
        };
        Assertions.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testFrom() {
        // act/assert
        Assertions.assertArrayEquals(new double[] {
            1, 0, 0,
            0, 1, 0
        }, AffineTransformMatrix2D.from(UnaryOperator.identity()).toArray(), EPS);
        Assertions.assertArrayEquals(new double[] {
            1, 0, 2,
            0, 1, 3
        }, AffineTransformMatrix2D.from(v -> v.add(Vector2D.of(2, 3))).toArray(), EPS);
        Assertions.assertArrayEquals(new double[] {
            3, 0, 0,
            0, 3, 0
        }, AffineTransformMatrix2D.from(v -> v.multiply(3)).toArray(), EPS);
        Assertions.assertArrayEquals(new double[] {
            3, 0, 6,
            0, 3, 9
        }, AffineTransformMatrix2D.from(v -> v.add(Vector2D.of(2, 3)).multiply(3)).toArray(), EPS);
    }

    @Test
    public void testFrom_invalidFunction() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> AffineTransformMatrix2D.from(v -> v.multiply(0)));
    }

    @Test
    public void testCreateTranslation_xy() {
        // act
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.createTranslation(2, 3);

        // assert
        final double[] expected = {
            1, 0, 2,
            0, 1, 3
        };
        Assertions.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testCreateTranslation_vector() {
        // act
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.createTranslation(Vector2D.of(5, 6));

        // assert
        final double[] expected = {
            1, 0, 5,
            0, 1, 6
        };
        Assertions.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testCreateScale_xy() {
        // act
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.createScale(2, 3);

        // assert
        final double[] expected = {
            2, 0, 0,
            0, 3, 0
        };
        Assertions.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testTranslate_xy() {
        // arrange
        final AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    2, 0, 10,
                    0, 3, 11
                );

        // act
        final AffineTransformMatrix2D result = a.translate(4, 5);

        // assert
        final double[] expected = {
            2, 0, 14,
            0, 3, 16
        };
        Assertions.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testTranslate_vector() {
        // arrange
        final AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    2, 0, 10,
                    0, 3, 11
                );

        // act
        final AffineTransformMatrix2D result = a.translate(Vector2D.of(7, 8));

        // assert
        final double[] expected = {
            2, 0, 17,
            0, 3, 19
        };
        Assertions.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testCreateScale_vector() {
        // act
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.createScale(Vector2D.of(4, 5));

        // assert
        final double[] expected = {
            4, 0, 0,
            0, 5, 0
        };
        Assertions.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testCreateScale_singleValue() {
        // act
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.createScale(7);

        // assert
        final double[] expected = {
            7, 0, 0,
            0, 7, 0
        };
        Assertions.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testScale_xy() {
        // arrange
        final AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    2, 0, 10,
                    0, 3, 11
                );

        // act
        final AffineTransformMatrix2D result = a.scale(4, 5);

        // assert
        final double[] expected = {
            8, 0, 40,
            0, 15, 55
        };
        Assertions.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testScale_vector() {
        // arrange
        final AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    2, 0, 10,
                    0, 3, 11
                );

        // act
        final AffineTransformMatrix2D result = a.scale(Vector2D.of(7, 8));

        // assert
        final double[] expected = {
            14, 0, 70,
            0, 24, 88
        };
        Assertions.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testScale_singleValue() {
        // arrange
        final AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    2, 0, 10,
                    0, 3, 11
                );

        // act
        final AffineTransformMatrix2D result = a.scale(10);

        // assert
        final double[] expected = {
            20, 0, 100,
            0, 30, 110
        };
        Assertions.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testCreateRotation() {
        // act
        final double angle = PlaneAngleRadians.PI * 2.0 / 3.0;
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.createRotation(angle);

        // assert
        final double sin = Math.sin(angle);
        final double cos = Math.cos(angle);

        final double[] expected = {
            cos, -sin, 0,
            sin, cos, 0
        };
        Assertions.assertArrayEquals(expected, transform.toArray(), EPS);
    }

    @Test
    public void testCreateRotation_aroundCenter_rawAngle() {
        // act
        final Vector2D center = Vector2D.of(1, 2);
        final double angle = PlaneAngleRadians.PI * 2.0 / 3.0;
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.createRotation(center, angle);

        // assert
        final double sin = Math.sin(angle);
        final double cos = Math.cos(angle);

        final double[] expected = {
            cos, -sin, -cos + (2 * sin) + 1,
            sin, cos, -sin - (2 * cos) + 2
        };
        Assertions.assertArrayEquals(expected, transform.toArray(), EPS);
    }

    @Test
    public void testCreateRotation_aroundCenter_rotationInstance() {
        // act
        final Vector2D center = Vector2D.of(1, 2);
        final double angle = PlaneAngleRadians.PI * 4.0 / 3.0;
        final Rotation2D rotation = Rotation2D.of(angle);
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.createRotation(center, rotation);

        // assert
        final double sin = Math.sin(angle);
        final double cos = Math.cos(angle);

        final double[] expected = {
            cos, -sin, -cos + (2 * sin) + 1,
            sin, cos, -sin - (2 * cos) + 2
        };
        Assertions.assertArrayEquals(expected, transform.toArray(), EPS);
    }

    @Test
    public void testRotate_rawAngle() {
        // arrange
        final AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    1, 2, 3,
                    4, 5, 6
                );

        // act
        final AffineTransformMatrix2D result = a.rotate(PlaneAngleRadians.PI_OVER_TWO);

        // assert
        final double[] expected = {
            -4, -5, -6,
            1, 2, 3
        };
        Assertions.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testRotate_rotationInstance() {
        // arrange
        final AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    1, 2, 3,
                    4, 5, 6
                );

        // act
        final AffineTransformMatrix2D result = a.rotate(Rotation2D.of(PlaneAngleRadians.PI_OVER_TWO));

        // assert
        final double[] expected = {
            -4, -5, -6,
            1, 2, 3
        };
        Assertions.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testRotate_aroundCenter_rawAngle() {
        // arrange
        final Vector2D center = Vector2D.of(1, 2);

        final AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    1, 2, 3,
                    4, 5, 6
                );

        // act
        final AffineTransformMatrix2D result = a.rotate(center, PlaneAngleRadians.PI_OVER_TWO);

        // assert
        final double[] expected = {
            -4, -5, -3,
            1, 2, 4
        };
        Assertions.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testRotate_aroundCenter_rotationInstance() {
        // arrange
        final Vector2D center = Vector2D.of(1, 2);

        final AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    1, 2, 3,
                    4, 5, 6
                );

        // act
        final AffineTransformMatrix2D result = a.rotate(center, Rotation2D.of(PlaneAngleRadians.PI_OVER_TWO));

        // assert
        final double[] expected = {
            -4, -5, -3,
            1, 2, 4
        };
        Assertions.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testCreateShear() {
        // act
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.createShear(2, 3);

        // assert
        final double[] expected = {
            1, 2, 0,
            3, 1, 0
        };
        Assertions.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testShear() {
        // arrange
        final AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                1, 2, 3,
                4, 5, 6
            );

        // act
        final AffineTransformMatrix2D result = a.shear(-2, 3);

        // assert
        final double[] expected = {
            -7, -8, -9,
            7, 11, 15
        };
        Assertions.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testShear_noShear() {
        // arrange
        final AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                1, 2, 3,
                4, 5, 6
            );

        // act
        final AffineTransformMatrix2D result = a.shear(0, 0);

        // assert
        final double[] expected = {
            1, 2, 3,
            4, 5, 6
        };
        Assertions.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testApply_identity() {
        // arrange
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity();

        // act/assert
        runWithCoordinates((x, y) -> {
            final Vector2D v = Vector2D.of(x, y);

            EuclideanTestUtils.assertCoordinatesEqual(v, transform.apply(v), EPS);
        });
    }

    @Test
    public void testApply_translate() {
        // arrange
        final Vector2D translation = Vector2D.of(1.1, -PlaneAngleRadians.PI);

        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .translate(translation);

        // act/assert
        runWithCoordinates((x, y) -> {
            final Vector2D vec = Vector2D.of(x, y);

            final Vector2D expectedVec = vec.add(translation);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_scale() {
        // arrange
        final Vector2D factors = Vector2D.of(2.0, -3.0);

        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .scale(factors);

        // act/assert
        runWithCoordinates((x, y) -> {
            final Vector2D vec = Vector2D.of(x, y);

            final Vector2D expectedVec = Vector2D.of(factors.getX() * x, factors.getY() * y);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_rotate() {
        // arrange
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .rotate(-PlaneAngleRadians.PI_OVER_TWO);

        // act/assert
        runWithCoordinates((x, y) -> {
            final Vector2D vec = Vector2D.of(x, y);

            final Vector2D expectedVec = Vector2D.of(y, -x);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_rotate_aroundCenter_minusHalfPi() {
        // arrange
        final Vector2D center = Vector2D.of(1, 2);
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .rotate(center, -PlaneAngleRadians.PI_OVER_TWO);

        // act/assert
        runWithCoordinates((x, y) -> {
            final Vector2D vec = Vector2D.of(x, y);

            final Vector2D centered = vec.subtract(center);
            final Vector2D expectedVec = Vector2D.of(centered.getY(), -centered.getX()).add(center);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_rotate_aroundCenter_pi() {
        // arrange
        final Vector2D center = Vector2D.of(1, 2);
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .rotate(center, PlaneAngleRadians.PI);

        // act/assert
        runWithCoordinates((x, y) -> {
            final Vector2D vec = Vector2D.of(x, y);

            final Vector2D centered = vec.subtract(center);
            final Vector2D expectedVec = Vector2D.of(-centered.getX(), -centered.getY()).add(center);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_shearAlongX() {
        // arrange
        final double shearFactor = -2;
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .shear(shearFactor, 0);

        // act/assert
        runWithCoordinates((x, y) -> {
            final Vector2D vec = Vector2D.of(x, y);

            final Vector2D expectedVec = Vector2D.of(x + (shearFactor * y), y);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_shearAlongY() {
        // arrange
        final double shearFactor = 2;
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .shear(0, shearFactor);

        // act/assert
        runWithCoordinates((x, y) -> {
            final Vector2D vec = Vector2D.of(x, y);

            final Vector2D expectedVec = Vector2D.of(x, y + (shearFactor * x));

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_shearAlongXAndY() {
        // arrange
        final double shearX = 2;
        final double shearY = -3;
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .shear(shearX, shearY);

        // act/assert
        runWithCoordinates((x, y) -> {
            final Vector2D vec = Vector2D.of(x, y);

            final Vector2D expectedVec = Vector2D.of(x + (shearX * y), y + (shearY * x));

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_translateShear() {
        // arrange
        final Vector2D translation = Vector2D.of(7, 8);
        final double shearX = -4;
        final double shearY = 5;
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .translate(translation)
                .shear(shearX, shearY);

        // act/assert
        runWithCoordinates((x, y) -> {
            final Vector2D vec = Vector2D.of(x, y);

            final double tx = x + translation.getX();
            final double ty = y + translation.getY();

            final Vector2D expectedVec = Vector2D.of(tx + (shearX * ty), ty + (shearY * tx));

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }


    @Test
    public void testApply_translateScaleRotate() {
        // arrange
        final Vector2D translation = Vector2D.of(-2.0, -3.0);
        final Vector2D scale = Vector2D.of(5.0, 6.0);

        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .translate(translation)
                .scale(scale)
                .rotate(PlaneAngleRadians.PI_OVER_TWO);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(12, -5), transform.apply(Vector2D.of(1, 1)), EPS);

        runWithCoordinates((x, y) -> {
            final Vector2D vec = Vector2D.of(x, y);

            final Vector2D temp = Vector2D.of(
                        (x + translation.getX()) * scale.getX(),
                        (y + translation.getY()) * scale.getY()
                    );
            final Vector2D expectedVec = Vector2D.of(-temp.getY(), temp.getX());

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_scaleTranslateRotate() {
        // arrange
        final Vector2D scale = Vector2D.of(5.0, 6.0);
        final Vector2D translation = Vector2D.of(-2.0, -3.0);

        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .scale(scale)
                .translate(translation)
                .rotate(-PlaneAngleRadians.PI_OVER_TWO);

        // act/assert
        runWithCoordinates((x, y) -> {
            final Vector2D vec = Vector2D.of(x, y);

            final Vector2D temp = Vector2D.of(
                        (x * scale.getX()) + translation.getX(),
                        (y * scale.getY()) + translation.getY()
                    );
            final Vector2D expectedVec = Vector2D.of(temp.getY(), -temp.getX());

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApplyXY() {
        // arrange
        final Vector2D scale = Vector2D.of(5.0, 6.0);
        final Vector2D translation = Vector2D.of(-2.0, -3.0);
        final Vector2D shear = Vector2D.of(7, 8);

        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .scale(scale)
                .translate(translation)
                .rotate(-PlaneAngleRadians.PI_OVER_TWO)
                .shear(shear.getX(), shear.getY());

        // act/assert
        runWithCoordinates((x, y) -> {
            final Vector2D scaledAndTranslated = Vector2D.of(
                        (x * scale.getX()) + translation.getX(),
                        (y * scale.getY()) + translation.getY()
                    );
            final Vector2D rotated = Vector2D.of(scaledAndTranslated.getY(), -scaledAndTranslated.getX());
            final Vector2D expected = Vector2D.of(
                        rotated.getX() + (rotated.getY() * shear.getX()),
                        rotated.getY() + (rotated.getX() * shear.getY())
                    );

            Assertions.assertEquals(expected.getX(), transform.applyX(x, y), EPS);
            Assertions.assertEquals(expected.getY(), transform.applyY(x, y), EPS);
        });
    }

    @Test
    public void testApplyVector_identity() {
        // arrange
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity();

        // act/assert
        runWithCoordinates((x, y) -> {
            final Vector2D v = Vector2D.of(x, y);

            EuclideanTestUtils.assertCoordinatesEqual(v, transform.applyVector(v), EPS);
        });
    }

    @Test
    public void testApplyVector_translate() {
        // arrange
        final Vector2D translation = Vector2D.of(1.1, -PlaneAngleRadians.PI);

        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .translate(translation);

        // act/assert
        runWithCoordinates((x, y) -> {
            final Vector2D vec = Vector2D.of(x, y);

            EuclideanTestUtils.assertCoordinatesEqual(vec, transform.applyVector(vec), EPS);
        });
    }

    @Test
    public void testApplyVector_scale() {
        // arrange
        final Vector2D factors = Vector2D.of(2.0, -3.0);

        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .scale(factors);

        // act/assert
        runWithCoordinates((x, y) -> {
            final Vector2D vec = Vector2D.of(x, y);

            final Vector2D expectedVec = Vector2D.of(factors.getX() * x, factors.getY() * y);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.applyVector(vec), EPS);
        });
    }

    @Test
    public void testApplyVector_representsDisplacement() {
        // arrange
        final Vector2D p1 = Vector2D.of(2, 3);

        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .scale(1.5)
                .translate(4, 6)
                .rotate(PlaneAngleRadians.PI_OVER_TWO);

        // act/assert
        runWithCoordinates((x, y) -> {
            final Vector2D p2 = Vector2D.of(x, y);
            final Vector2D input = p1.subtract(p2);

            final Vector2D expected = transform.apply(p1).subtract(transform.apply(p2));

            EuclideanTestUtils.assertCoordinatesEqual(expected, transform.applyVector(input), EPS);
        });
    }

    @Test
    public void testApplyVectorXY() {
        // arrange
        final Vector2D p1 = Vector2D.of(2, 3);

        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .scale(1.5)
                .translate(4, 6)
                .rotate(0.3 * Math.PI);

        // act/assert
        runWithCoordinates((x, y) -> {
            final Vector2D p2 = p1.add(Vector2D.of(x, y));

            final Vector2D expected = transform.apply(p1).vectorTo(transform.apply(p2));

            Assertions.assertEquals(expected.getX(), transform.applyVectorX(x, y), EPS);
            Assertions.assertEquals(expected.getY(), transform.applyVectorY(x, y), EPS);
        });
    }

    @Test
    public void testApplyDirection_identity() {
        // arrange
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity();

        // act/assert
        EuclideanTestUtils.permuteSkipZero(-5, 5, 0.5, (x, y) -> {
            final Vector2D v = Vector2D.of(x, y);

            EuclideanTestUtils.assertCoordinatesEqual(v.normalize(), transform.applyDirection(v), EPS);
        });
    }

    @Test
    public void testApplyDirection_translate() {
        // arrange
        final Vector2D translation = Vector2D.of(1.1, -PlaneAngleRadians.PI);

        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .translate(translation);

        // act/assert
        EuclideanTestUtils.permuteSkipZero(-5, 5, 0.5, (x, y) -> {
            final Vector2D vec = Vector2D.of(x, y);

            EuclideanTestUtils.assertCoordinatesEqual(vec.normalize(), transform.applyDirection(vec), EPS);
        });
    }

    @Test
    public void testApplyDirection_scale() {
        // arrange
        final Vector2D factors = Vector2D.of(2.0, -3.0);

        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .scale(factors);

        // act/assert
        EuclideanTestUtils.permuteSkipZero(-5, 5, 0.5, (x, y) -> {
            final Vector2D vec = Vector2D.of(x, y);

            final Vector2D expectedVec = Vector2D.of(factors.getX() * x, factors.getY() * y).normalize();

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.applyDirection(vec), EPS);
        });
    }

    @Test
    public void testApplyDirection_representsNormalizedDisplacement() {
        // arrange
        final Vector2D p1 = Vector2D.of(2.1, 3.2);

        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .scale(1.5)
                .translate(4, 6)
                .rotate(PlaneAngleRadians.PI_OVER_TWO);

        // act/assert
        EuclideanTestUtils.permute(-5, 5, 0.5, (x, y) -> {
            final Vector2D p2 = Vector2D.of(x, y);
            final Vector2D input = p1.subtract(p2);

            final Vector2D expected = transform.apply(p1).subtract(transform.apply(p2)).normalize();

            EuclideanTestUtils.assertCoordinatesEqual(expected, transform.applyDirection(input), EPS);
        });
    }

    @Test
    public void testApplyDirection_illegalNorm() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> AffineTransformMatrix2D.createScale(1, 0).applyDirection(Vector2D.Unit.PLUS_Y));
        Assertions.assertThrows(IllegalArgumentException.class, () -> AffineTransformMatrix2D.createScale(2).applyDirection(Vector2D.ZERO));
    }

    @Test
    public void testDeterminant() {
        // act/assert
        Assertions.assertEquals(1.0, AffineTransformMatrix2D.identity().determinant(), EPS);
        Assertions.assertEquals(6.0, AffineTransformMatrix2D.of(
                2, 0, 4,
                0, 3, 5
            ).determinant(), EPS);
        Assertions.assertEquals(-6.0, AffineTransformMatrix2D.of(
                2, 0, 4,
                0, -3, 5
            ).determinant(), EPS);
        Assertions.assertEquals(-5.0, AffineTransformMatrix2D.of(
                1, 3, 0,
                2, 1, 0
            ).determinant(), EPS);
        Assertions.assertEquals(-0.0, AffineTransformMatrix2D.of(
                0, 0, 1,
                0, 0, 2
            ).determinant(), EPS);
    }

    @Test
    public void testPreservesOrientation() {
        // act/assert
        Assertions.assertTrue(AffineTransformMatrix2D.identity().preservesOrientation());
        Assertions.assertTrue(AffineTransformMatrix2D.of(
                2, 0, 4,
                0, 3, 5
            ).preservesOrientation());

        Assertions.assertFalse(AffineTransformMatrix2D.of(
                2, 0, 4,
                0, -3, 5
            ).preservesOrientation());
        Assertions.assertFalse(AffineTransformMatrix2D.of(
                1, 3, 0,
                2, 1, 0
            ).preservesOrientation());
        Assertions.assertFalse(AffineTransformMatrix2D.of(
                0, 0, 1,
                0, 0, 2
            ).preservesOrientation());
    }

    @Test
    public void testMultiply() {
        // arrange
        final AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    1, 2, 3,
                    5, 6, 7
                );
        final AffineTransformMatrix2D b = AffineTransformMatrix2D.of(
                    13, 14, 15,
                    17, 18, 19
                );

        // act
        final AffineTransformMatrix2D result = a.multiply(b);

        // assert
        final double[] arr = result.toArray();
        Assertions.assertArrayEquals(new double[] {
            47, 50, 56,
            167, 178, 196
        }, arr, EPS);
    }

    @Test
    public void testMultiply_combinesTransformOperations() {
        // arrange
        final Vector2D translation1 = Vector2D.of(1, 2);
        final double scale = 2.0;
        final Vector2D translation2 = Vector2D.of(4, 5);

        final AffineTransformMatrix2D a = AffineTransformMatrix2D.createTranslation(translation1);
        final AffineTransformMatrix2D b = AffineTransformMatrix2D.createScale(scale);
        final AffineTransformMatrix2D c = AffineTransformMatrix2D.identity();
        final AffineTransformMatrix2D d = AffineTransformMatrix2D.createTranslation(translation2);

        // act
        final AffineTransformMatrix2D transform = d.multiply(c).multiply(b).multiply(a);

        // assert
        runWithCoordinates((x, y) -> {
            final Vector2D vec = Vector2D.of(x, y);

            final Vector2D expectedVec = vec
                    .add(translation1)
                    .multiply(scale)
                    .add(translation2);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testPremultiply() {
        // arrange
        final AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    1, 2, 3,
                    5, 6, 7
                );
        final AffineTransformMatrix2D b = AffineTransformMatrix2D.of(
                    13, 14, 15,
                    17, 18, 19
                );

        // act
        final AffineTransformMatrix2D result = b.premultiply(a);

        // assert
        final double[] arr = result.toArray();
        Assertions.assertArrayEquals(new double[] {
            47, 50, 56,
            167, 178, 196
        }, arr, EPS);
    }

    @Test
    public void testPremultiply_combinesTransformOperations() {
        // arrange
        final Vector2D translation1 = Vector2D.of(1, 2);
        final double scale = 2.0;
        final Vector2D translation2 = Vector2D.of(4, 5);

        final AffineTransformMatrix2D a = AffineTransformMatrix2D.createTranslation(translation1);
        final AffineTransformMatrix2D b = AffineTransformMatrix2D.createScale(scale);
        final AffineTransformMatrix2D c = AffineTransformMatrix2D.identity();
        final AffineTransformMatrix2D d = AffineTransformMatrix2D.createTranslation(translation2);

        // act
        final AffineTransformMatrix2D transform = a.premultiply(b).premultiply(c).premultiply(d);

        // assert
        runWithCoordinates((x, y) -> {
            final Vector2D vec = Vector2D.of(x, y);

            final Vector2D expectedVec = vec
                    .add(translation1)
                    .multiply(scale)
                    .add(translation2);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testInverse_identity() {
        // act
        final AffineTransformMatrix2D inverse = AffineTransformMatrix2D.identity().inverse();

        // assert
        final double[] expected = {
            1, 0, 0,
            0, 1, 0
        };
        Assertions.assertArrayEquals(expected, inverse.toArray(), 0.0);
    }

    @Test
    public void testInverse_multiplyByInverse_producesIdentity() {
        // arrange
        final AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    1, 3, 7,
                    2, 4, 9
                );

        final AffineTransformMatrix2D inv = a.inverse();

        // act
        final AffineTransformMatrix2D result = inv.multiply(a);

        // assert
        final double[] expected = {
            1, 0, 0,
            0, 1, 0
        };
        Assertions.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testInverse_translate() {
        // arrange
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.createTranslation(1, -2);

        // act
        final AffineTransformMatrix2D inverse = transform.inverse();

        // assert
        final double[] expected = {
            1, 0, -1,
            0, 1, 2
        };
        Assertions.assertArrayEquals(expected, inverse.toArray(), 0.0);
    }

    @Test
    public void testInverse_scale() {
        // arrange
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.createScale(10, -2);

        // act
        final AffineTransformMatrix2D inverse = transform.inverse();

        // assert
        final double[] expected = {
            0.1, 0, 0,
            0, -0.5, 0
        };
        Assertions.assertArrayEquals(expected, inverse.toArray(), 0.0);
    }

    @Test
    public void testInverse_rotate() {
        // arrange
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.createRotation(PlaneAngleRadians.PI_OVER_TWO);

        // act
        final AffineTransformMatrix2D inverse = transform.inverse();

        // assert
        final double[] expected = {
            0, 1, 0,
            -1, 0, 0
        };
        Assertions.assertArrayEquals(expected, inverse.toArray(), EPS);
    }

    @Test
    public void testInverse_rotate_aroundCenter() {
        // arrange
        final Vector2D center = Vector2D.of(1, 2);
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.createRotation(center, PlaneAngleRadians.PI_OVER_TWO);

        // act
        final AffineTransformMatrix2D inverse = transform.inverse();

        // assert
        final double[] expected = {
            0, 1, -1,
            -1, 0, 3
        };
        Assertions.assertArrayEquals(expected, inverse.toArray(), EPS);
    }

    @Test
    public void testInverse_undoesOriginalTransform() {
        // arrange
        final Vector2D v1 = Vector2D.ZERO;
        final Vector2D v2 = Vector2D.Unit.PLUS_X;
        final Vector2D v3 = Vector2D.of(1, 1);
        final Vector2D v4 = Vector2D.of(-2, 3);

        final Vector2D center = Vector2D.of(-0.5, 2);

        // act/assert
        runWithCoordinates((x, y) -> {
            final AffineTransformMatrix2D transform = AffineTransformMatrix2D
                        .createTranslation(x, y)
                        .scale(2, 3)
                        .translate(x / 3, y / 3)
                        .rotate(x / 4)
                        .rotate(center, y / 2);

            final AffineTransformMatrix2D inverse = transform.inverse();

            EuclideanTestUtils.assertCoordinatesEqual(v1, inverse.apply(transform.apply(v1)), EPS);
            EuclideanTestUtils.assertCoordinatesEqual(v2, inverse.apply(transform.apply(v2)), EPS);
            EuclideanTestUtils.assertCoordinatesEqual(v3, inverse.apply(transform.apply(v3)), EPS);
            EuclideanTestUtils.assertCoordinatesEqual(v4, inverse.apply(transform.apply(v4)), EPS);
        });
    }

    @Test
    public void testInverse_nonInvertible() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> AffineTransformMatrix2D.of(
                0, 0, 0,
                0, 0, 0).inverse(), IllegalStateException.class, "Matrix is not invertible; matrix determinant is 0.0");

        GeometryTestUtils.assertThrowsWithMessage(() -> AffineTransformMatrix2D.of(
                1, 0, 0,
                0, Double.NaN, 0).inverse(), IllegalStateException.class, "Matrix is not invertible; matrix determinant is NaN");

        GeometryTestUtils.assertThrowsWithMessage(() -> AffineTransformMatrix2D.of(
                1, 0, 0,
                0, Double.NEGATIVE_INFINITY, 0).inverse(), IllegalStateException.class, "Matrix is not invertible; matrix determinant is -Infinity");

        GeometryTestUtils.assertThrowsWithMessage(() -> AffineTransformMatrix2D.of(
                Double.POSITIVE_INFINITY, 0, 0,
                0, 1, 0).inverse(), IllegalStateException.class, "Matrix is not invertible; matrix determinant is Infinity");

        GeometryTestUtils.assertThrowsWithMessage(() -> AffineTransformMatrix2D.of(
                1, 0, Double.NaN,
                0, 1, 0).inverse(), IllegalStateException.class, "Matrix is not invertible; invalid matrix element: NaN");

        GeometryTestUtils.assertThrowsWithMessage(() -> AffineTransformMatrix2D.of(
                1, 0, Double.POSITIVE_INFINITY,
                0, 1, 0).inverse(), IllegalStateException.class, "Matrix is not invertible; invalid matrix element: Infinity");

        GeometryTestUtils.assertThrowsWithMessage(() -> AffineTransformMatrix2D.of(
                1, 0, Double.NEGATIVE_INFINITY,
                0, 1, 0).inverse(), IllegalStateException.class, "Matrix is not invertible; invalid matrix element: -Infinity");
    }

    @Test
    public void testLinear() {
        // arrange
        final AffineTransformMatrix2D mat = AffineTransformMatrix2D.of(
                2, 3, 4,
                5, 6, 7);

        // act
        final AffineTransformMatrix2D result = mat.linear();

        // assert
        final double[] expected = {
            2, 3, 0,
            5, 6, 0
        };
        Assertions.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testLinearTranspose() {
        // arrange
        final AffineTransformMatrix2D mat = AffineTransformMatrix2D.of(
                2, 3, 4,
                5, 6, 7);

        // act
        final AffineTransformMatrix2D result = mat.linearTranspose();

        // assert
        final double[] expected = {
            2, 5, 0,
            3, 6, 0
        };
        Assertions.assertArrayEquals(expected, result.toArray(), 0.0);
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

    private void checkNormalTransform(final AffineTransformMatrix2D transform) {
        final AffineTransformMatrix2D normalTransform = transform.normalTransform();

        final Vector2D p1 = Vector2D.of(-0.25, 0.75);
        final Vector2D t1 = transform.apply(p1);

        EuclideanTestUtils.permute(-10, 10, 1, (x, y) -> {
            final Vector2D p2 = Vector2D.of(x, y);
            final Vector2D n = Lines.fromPoints(p1, p2, TEST_PRECISION).getOffsetDirection();

            final Vector2D t2 = transform.apply(p2);

            final Line tLine = transform.preservesOrientation() ?
                    Lines.fromPoints(t1, t2, TEST_PRECISION) :
                    Lines.fromPoints(t2, t1, TEST_PRECISION);
            final Vector2D expected = tLine.getOffsetDirection();

            final Vector2D actual = normalTransform.apply(n).normalize();

            EuclideanTestUtils.assertCoordinatesEqual(expected, actual, EPS);
        });
    }

    @Test
    public void testNormalTransform_nonInvertible() {
        // act/assert
        Assertions.assertThrows(IllegalStateException.class, () -> AffineTransformMatrix2D.createScale(0).normalTransform());
    }

    @Test
    public void testHashCode() {
        // arrange
        final double[] values = {
            1, 2, 3,
            5, 6, 7
        };

        // act/assert
        final int orig = AffineTransformMatrix2D.of(values).hashCode();
        final int same = AffineTransformMatrix2D.of(values).hashCode();

        Assertions.assertEquals(orig, same);

        double[] temp;
        for (int i = 0; i < values.length; ++i) {
            temp = values.clone();
            temp[i] = 0;

            final int modified = AffineTransformMatrix2D.of(temp).hashCode();

            Assertions.assertNotEquals(orig, modified);
        }
    }

    @Test
    public void testEquals() {
        // arrange
        final double[] values = {
            1, 2, 3,
            5, 6, 7
        };

        final AffineTransformMatrix2D a = AffineTransformMatrix2D.of(values);

        // act/assert
        GeometryTestUtils.assertSimpleEqualsCases(a);

        double[] temp;
        for (int i = 0; i < values.length; ++i) {
            temp = values.clone();
            temp[i] = 0;

            final AffineTransformMatrix2D modified = AffineTransformMatrix2D.of(temp);

            Assertions.assertNotEquals(a, modified);
        }
    }

    @Test
    public void testEqualsAndHashCode_signedZeroConsistency() {
        // arrange
        final double[] arrWithPosZero = {
            1.0, 0.0, 0.0,
            0.0, 1.0, 0.0
        };
        final double[] arrWithNegZero = {
            1.0, 0.0, 0.0,
            0.0, 1.0, -0.0
        };
        final AffineTransformMatrix2D a = AffineTransformMatrix2D.of(arrWithPosZero);
        final AffineTransformMatrix2D b = AffineTransformMatrix2D.of(arrWithNegZero);
        final AffineTransformMatrix2D c = AffineTransformMatrix2D.of(arrWithPosZero);
        final AffineTransformMatrix2D d = AffineTransformMatrix2D.of(arrWithNegZero);

        // act/assert
        Assertions.assertFalse(a.equals(b));
        Assertions.assertNotEquals(a.hashCode(), b.hashCode());

        Assertions.assertTrue(a.equals(c));
        Assertions.assertEquals(a.hashCode(), c.hashCode());

        Assertions.assertTrue(b.equals(d));
        Assertions.assertEquals(b.hashCode(), d.hashCode());
    }

    @Test
    public void testToString() {
        // arrange
        final AffineTransformMatrix2D a = AffineTransformMatrix2D.of(
                    1, 2, 3,
                    5, 6, 7
                );

        // act
        final String result = a.toString();

        // assert
        Assertions.assertEquals(
                "[ 1.0, 2.0, 3.0; " +
                "5.0, 6.0, 7.0 ]", result);
    }

    @FunctionalInterface
    private interface Coordinate2DTest {

        void run(double x, double y);
    }

    private static void runWithCoordinates(final Coordinate2DTest test) {
        runWithCoordinates(test, -1e-2, 1e-2, 5e-3);
        runWithCoordinates(test, -1e2, 1e2, 5);
    }

    private static void runWithCoordinates(final Coordinate2DTest test, final double min, final double max, final double step) {
        for (double x = min; x <= max; x += step) {
            for (double y = min; y <= max; y += step) {
                test.run(x, y);
            }
        }
    }
}
