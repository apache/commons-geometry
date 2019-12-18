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

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class AffineTransformMatrix1DTest {

    private static final double EPS = 1e-12;

    @Test
    public void testOf() {
        // act
        AffineTransformMatrix1D transform = AffineTransformMatrix1D.of(1, 2);

        // assert
        Assert.assertTrue(transform.preservesOrientation());

        double[] result = transform.toArray();
        Assert.assertArrayEquals(new double[] {1, 2}, result, 0.0);
    }


    @Test
    public void testOf_invalidDimensions() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> AffineTransformMatrix1D.of(1),
                IllegalArgumentException.class, "Dimension mismatch: 1 != 2");
    }

    @Test
    public void testIdentity() {
        // act
        AffineTransformMatrix1D transform = AffineTransformMatrix1D.identity();

        // assert
        Assert.assertTrue(transform.preservesOrientation());

        double[] expected = {1, 0};
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testCreateTranslation_value() {
        // act
        AffineTransformMatrix1D transform = AffineTransformMatrix1D.createTranslation(2);

        // assert
        Assert.assertTrue(transform.preservesOrientation());

        double[] expected = {1, 2};
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testCreateTranslation_vector() {
        // act
        AffineTransformMatrix1D transform = AffineTransformMatrix1D.createTranslation(Vector1D.of(5));

        // assert
        Assert.assertTrue(transform.preservesOrientation());

        double[] expected = {1, 5};
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testTranslate_value() {
        // arrange
        AffineTransformMatrix1D a = AffineTransformMatrix1D.of(2, 10);

        // act
        AffineTransformMatrix1D result = a.translate(4);

        // assert
        Assert.assertTrue(result.preservesOrientation());

        double[] expected = {2, 14};
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testTranslate_vector() {
        // arrange
        AffineTransformMatrix1D a = AffineTransformMatrix1D.of(2, 10);

        // act
        AffineTransformMatrix1D result = a.translate(Vector1D.of(7));

        // assert
        Assert.assertTrue(result.preservesOrientation());

        double[] expected = {2, 17};
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testCreateScale_vector() {
        // act
        AffineTransformMatrix1D transform = AffineTransformMatrix1D.createScale(Vector1D.of(4));

        // assert
        Assert.assertTrue(transform.preservesOrientation());

        double[] expected = {4, 0};
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testCreateScale_value() {
        // act
        AffineTransformMatrix1D transform = AffineTransformMatrix1D.createScale(7);

        // assert
        Assert.assertTrue(transform.preservesOrientation());

        double[] expected = {7, 0};
        Assert.assertArrayEquals(expected, transform.toArray(), 0.0);
    }

    @Test
    public void testScale_value() {
        // arrange
        AffineTransformMatrix1D a = AffineTransformMatrix1D.of(2, 10);

        // act
        AffineTransformMatrix1D result = a.scale(4);

        // assert
        Assert.assertTrue(result.preservesOrientation());

        double[] expected = {8, 40};
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testScale_vector() {
        // arrange
        AffineTransformMatrix1D a = AffineTransformMatrix1D.of(2, 10);

        // act
        AffineTransformMatrix1D result = a.scale(Vector1D.of(7));

        // assert
        Assert.assertTrue(result.preservesOrientation());

        double[] expected = {14, 70};
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testApply_identity() {
        // arrange
        AffineTransformMatrix1D transform = AffineTransformMatrix1D.identity();

        // act/assert
        runWithCoordinates(x -> {
            Vector1D v = Vector1D.of(x);

            EuclideanTestUtils.assertCoordinatesEqual(v, transform.apply(v), EPS);
        });
    }

    @Test
    public void testApply_translate() {
        // arrange
        Vector1D translation = Vector1D.of(-PlaneAngleRadians.PI);

        AffineTransformMatrix1D transform = AffineTransformMatrix1D.identity()
                .translate(translation);

        // act/assert
        runWithCoordinates(x -> {
            Vector1D vec = Vector1D.of(x);

            Vector1D expectedVec = vec.add(translation);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_scale() {
        // arrange
        Vector1D factor = Vector1D.of(2.0);

        AffineTransformMatrix1D transform = AffineTransformMatrix1D.identity()
                .scale(factor);

        // act/assert
        runWithCoordinates(x -> {
            Vector1D vec = Vector1D.of(x);

            Vector1D expectedVec = Vector1D.of(factor.getX() * x);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_translateThenScale() {
        // arrange
        Vector1D translation = Vector1D.of(-2.0);
        Vector1D scale = Vector1D.of(5.0);

        AffineTransformMatrix1D transform = AffineTransformMatrix1D.identity()
                .translate(translation)
                .scale(scale);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(-5), transform.apply(Vector1D.of(1)), EPS);

        runWithCoordinates(x -> {
            Vector1D vec = Vector1D.of(x);

            Vector1D expectedVec = Vector1D.of(
                        (x + translation.getX()) * scale.getX()
                    );

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApply_scaleThenTranslate() {
        // arrange
        Vector1D scale = Vector1D.of(5.0);
        Vector1D translation = Vector1D.of(-2.0);

        AffineTransformMatrix1D transform = AffineTransformMatrix1D.identity()
                .scale(scale)
                .translate(translation);

        // act/assert
        runWithCoordinates(x -> {
            Vector1D vec = Vector1D.of(x);

            Vector1D expectedVec = Vector1D.of(
                        (x * scale.getX()) + translation.getX()
                    );

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testApplyVector_identity() {
        // arrange
        AffineTransformMatrix1D transform = AffineTransformMatrix1D.identity();

        // act/assert
        runWithCoordinates(x -> {
            Vector1D v = Vector1D.of(x);

            EuclideanTestUtils.assertCoordinatesEqual(v, transform.applyVector(v), EPS);
        });
    }

    @Test
    public void testApplyVector_translate() {
        // arrange
        Vector1D translation = Vector1D.of(-PlaneAngleRadians.PI);

        AffineTransformMatrix1D transform = AffineTransformMatrix1D.identity()
                .translate(translation);

        // act/assert
        runWithCoordinates(x -> {
            Vector1D vec = Vector1D.of(x);

            EuclideanTestUtils.assertCoordinatesEqual(vec, transform.applyVector(vec), EPS);
        });
    }

    @Test
    public void testApplyVector_scale() {
        // arrange
        Vector1D factor = Vector1D.of(2.0);

        AffineTransformMatrix1D transform = AffineTransformMatrix1D.identity()
                .scale(factor);

        // act/assert
        runWithCoordinates(x -> {
            Vector1D vec = Vector1D.of(x);

            Vector1D expectedVec = Vector1D.of(factor.getX() * x);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.applyVector(vec), EPS);
        });
    }

    @Test
    public void testApplyVector_representsDisplacement() {
        // arrange
        Vector1D p1 = Vector1D.of(PlaneAngleRadians.PI);

        Vector1D translation = Vector1D.of(-2.0);
        Vector1D scale = Vector1D.of(5.0);

        AffineTransformMatrix1D transform = AffineTransformMatrix1D.identity()
                .translate(translation)
                .scale(scale);

        // act/assert
        runWithCoordinates(x -> {
            Vector1D p2 = Vector1D.of(x);
            Vector1D input = p1.subtract(p2);

            Vector1D expectedVec = transform.apply(p1).subtract(transform.apply(p2));

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.applyVector(input), EPS);
        });
    }

    @Test
    public void testApplyDirection_identity() {
        // arrange
        AffineTransformMatrix1D transform = AffineTransformMatrix1D.identity();

        // act/assert
        runWithCoordinates(x -> {
            Vector1D v = Vector1D.of(x);

            EuclideanTestUtils.assertCoordinatesEqual(v.normalize(), transform.applyDirection(v), EPS);
        }, true);
    }

    @Test
    public void testApplyDirection_translate() {
        // arrange
        Vector1D translation = Vector1D.of(-PlaneAngleRadians.PI);

        AffineTransformMatrix1D transform = AffineTransformMatrix1D.identity()
                .translate(translation);

        // act/assert
        runWithCoordinates(x -> {
            Vector1D vec = Vector1D.of(x);

            EuclideanTestUtils.assertCoordinatesEqual(vec.normalize(), transform.applyDirection(vec), EPS);
        }, true);
    }

    @Test
    public void testApplyDirection_scale() {
        // arrange
        Vector1D factor = Vector1D.of(2.0);

        AffineTransformMatrix1D transform = AffineTransformMatrix1D.identity()
                .scale(factor);

        // act/assert
        runWithCoordinates(x -> {
            Vector1D vec = Vector1D.of(x);

            Vector1D expectedVec = Vector1D.of(factor.getX() * x).normalize();

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.applyDirection(vec), EPS);
        }, true);
    }

    @Test
    public void testApplyDirection_representsNormalizedDisplacement() {
        // arrange
        Vector1D p1 = Vector1D.of(PlaneAngleRadians.PI);

        Vector1D translation = Vector1D.of(-2.0);
        Vector1D scale = Vector1D.of(5.0);

        AffineTransformMatrix1D transform = AffineTransformMatrix1D.identity()
                .translate(translation)
                .scale(scale);

        // act/assert
        runWithCoordinates(x -> {
            Vector1D p2 = Vector1D.of(x);
            Vector1D input = p1.subtract(p2);

            Vector1D expectedVec = transform.apply(p1).subtract(transform.apply(p2)).normalize();

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.applyDirection(input), EPS);
        });
    }

    @Test
    public void testApplyDirection_illegalNorm() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> AffineTransformMatrix1D.createScale(0).applyDirection(Vector1D.Unit.PLUS),
                IllegalArgumentException.class);
        GeometryTestUtils.assertThrows(() -> AffineTransformMatrix1D.createScale(2).applyDirection(Vector1D.ZERO),
                IllegalArgumentException.class);
    }

    @Test
    public void testDeterminant() {
        // act/assert
        Assert.assertEquals(0.0, AffineTransformMatrix1D.of(0, 1).determinant(), EPS);
        Assert.assertEquals(1.0, AffineTransformMatrix1D.of(1, 0).determinant(), EPS);
        Assert.assertEquals(-1.0, AffineTransformMatrix1D.of(-1, 2).determinant(), EPS);
    }

    @Test
    public void testPreservesOrientation() {
        // act/assert
        Assert.assertFalse(AffineTransformMatrix1D.of(0, 1).preservesOrientation());
        Assert.assertTrue(AffineTransformMatrix1D.of(1, 0).preservesOrientation());
        Assert.assertFalse(AffineTransformMatrix1D.of(-1, 2).preservesOrientation());
    }

    @Test
    public void testToMatrix() {
        // arrange
        AffineTransformMatrix1D t = AffineTransformMatrix1D.of(1, 1);

        // act/assert
        Assert.assertSame(t, t.toMatrix());
    }

    @Test
    public void testMultiply() {
        // arrange
        AffineTransformMatrix1D a = AffineTransformMatrix1D.of(2, 3);
        AffineTransformMatrix1D b = AffineTransformMatrix1D.of(13, 14);

        // act
        AffineTransformMatrix1D result = a.multiply(b);

        // assert
        double[] arr = result.toArray();
        Assert.assertArrayEquals(new double[] {26, 31}, arr, EPS);
    }

    @Test
    public void testMultiply_combinesTransformOperations() {
        // arrange
        Vector1D translation1 = Vector1D.of(1);
        double scale = 2.0;
        Vector1D translation2 = Vector1D.of(4);

        AffineTransformMatrix1D a = AffineTransformMatrix1D.createTranslation(translation1);
        AffineTransformMatrix1D b = AffineTransformMatrix1D.createScale(scale);
        AffineTransformMatrix1D c = AffineTransformMatrix1D.identity();
        AffineTransformMatrix1D d = AffineTransformMatrix1D.createTranslation(translation2);

        // act
        AffineTransformMatrix1D transform = d.multiply(c).multiply(b).multiply(a);

        // assert
        runWithCoordinates(x -> {
            Vector1D vec = Vector1D.of(x);

            Vector1D expectedVec = vec
                    .add(translation1)
                    .multiply(scale)
                    .add(translation2);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testPremultiply() {
        // arrange
        AffineTransformMatrix1D a = AffineTransformMatrix1D.of(2, 3);
        AffineTransformMatrix1D b = AffineTransformMatrix1D.of(13, 14);

        // act
        AffineTransformMatrix1D result = b.premultiply(a);

        // assert
        double[] arr = result.toArray();
        Assert.assertArrayEquals(new double[] {26, 31}, arr, EPS);
    }

    @Test
    public void testPremultiply_combinesTransformOperations() {
        // arrange
        Vector1D translation1 = Vector1D.of(1);
        double scale = 2.0;
        Vector1D translation2 = Vector1D.of(4);

        AffineTransformMatrix1D a = AffineTransformMatrix1D.createTranslation(translation1);
        AffineTransformMatrix1D b = AffineTransformMatrix1D.createScale(scale);
        AffineTransformMatrix1D c = AffineTransformMatrix1D.identity();
        AffineTransformMatrix1D d = AffineTransformMatrix1D.createTranslation(translation2);

        // act
        AffineTransformMatrix1D transform = a.premultiply(b).premultiply(c).premultiply(d);

        // assert
        runWithCoordinates(x -> {
            Vector1D vec = Vector1D.of(x);

            Vector1D expectedVec = vec
                    .add(translation1)
                    .multiply(scale)
                    .add(translation2);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.apply(vec), EPS);
        });
    }

    @Test
    public void testInverse_identity() {
        // act
        AffineTransformMatrix1D inverse = AffineTransformMatrix1D.identity().inverse();

        // assert
        double[] expected = {1, 0};
        Assert.assertArrayEquals(expected, inverse.toArray(), 0.0);
    }

    @Test
    public void testInverse_multiplyByInverse_producesIdentity() {
        // arrange
        AffineTransformMatrix1D a = AffineTransformMatrix1D.of(1, 3);

        AffineTransformMatrix1D inv = a.inverse();

        // act
        AffineTransformMatrix1D result = inv.multiply(a);

        // assert
        double[] expected = {1, 0};
        Assert.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testInverse_translate() {
        // arrange
        AffineTransformMatrix1D transform = AffineTransformMatrix1D.createTranslation(3);

        // act
        AffineTransformMatrix1D inverse = transform.inverse();

        // assert
        double[] expected = {1, -3};
        Assert.assertArrayEquals(expected, inverse.toArray(), 0.0);
    }

    @Test
    public void testInverse_scale() {
        // arrange
        AffineTransformMatrix1D transform = AffineTransformMatrix1D.createScale(10);

        // act
        AffineTransformMatrix1D inverse = transform.inverse();

        // assert
        double[] expected = {0.1, 0};
        Assert.assertArrayEquals(expected, inverse.toArray(), 0.0);
    }

    @Test
    public void testInverse_undoesOriginalTransform_translationAndScale() {
        // arrange
        Vector1D v1 = Vector1D.ZERO;
        Vector1D v2 = Vector1D.Unit.PLUS;
        Vector1D v3 = Vector1D.of(1.5);
        Vector1D v4 = Vector1D.of(-2);

        // act/assert
        runWithCoordinates(x -> {
            AffineTransformMatrix1D transform = AffineTransformMatrix1D
                        .createTranslation(x)
                        .scale(2)
                        .translate(x / 3);

            AffineTransformMatrix1D inverse = transform.inverse();

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
            AffineTransformMatrix1D.of(0, 0).inverse();
        }, IllegalStateException.class, "Transform is not invertible; matrix determinant is 0.0");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix1D.of(Double.NaN, 0).inverse();
        }, IllegalStateException.class, "Transform is not invertible; matrix determinant is NaN");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix1D.of(Double.NEGATIVE_INFINITY, 0.0).inverse();
        }, IllegalStateException.class, "Transform is not invertible; matrix determinant is -Infinity");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix1D.of(Double.POSITIVE_INFINITY, 0).inverse();
        }, IllegalStateException.class, "Transform is not invertible; matrix determinant is Infinity");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix1D.of(1, Double.NaN).inverse();
        }, IllegalStateException.class, "Transform is not invertible; invalid matrix element: NaN");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix1D.of(1, Double.NEGATIVE_INFINITY).inverse();
        }, IllegalStateException.class, "Transform is not invertible; invalid matrix element: -Infinity");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransformMatrix1D.of(1, Double.POSITIVE_INFINITY).inverse();
        }, IllegalStateException.class, "Transform is not invertible; invalid matrix element: Infinity");
    }

    @Test
    public void testHashCode() {
        // act
        int orig = AffineTransformMatrix1D.of(1, 2).hashCode();
        int same = AffineTransformMatrix1D.of(1, 2).hashCode();

        // assert
        Assert.assertEquals(orig, same);

        Assert.assertNotEquals(orig, AffineTransformMatrix1D.of(0, 2).hashCode());
        Assert.assertNotEquals(orig, AffineTransformMatrix1D.of(1, 0).hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        AffineTransformMatrix1D a = AffineTransformMatrix1D.of(1, 2);

        // act/assert
        Assert.assertTrue(a.equals(a));

        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        Assert.assertFalse(a.equals(AffineTransformMatrix1D.of(0, 2)));
        Assert.assertFalse(a.equals(AffineTransformMatrix1D.of(1, 0)));
    }

    @Test
    public void testToString() {
        // arrange
        AffineTransformMatrix1D a = AffineTransformMatrix1D.of(1, 2);

        // act
        String result = a.toString();

        // assert
        Assert.assertEquals("[ 1.0, 2.0 ]", result);
    }

    @FunctionalInterface
    private interface Coordinate1DTest {

        void run(double x);
    }

    private static void runWithCoordinates(Coordinate1DTest test) {
        runWithCoordinates(test, false);
    }

    private static void runWithCoordinates(Coordinate1DTest test, boolean skipZero) {
        runWithCoordinates(test, -1e-2, 1e-2, 5e-3, skipZero);
        runWithCoordinates(test, -1e2, 1e2, 5, skipZero);
    }

    private static void runWithCoordinates(Coordinate1DTest test, double min, double max, double step, boolean skipZero) {
        for (double x = min; x <= max; x += step) {
            if (!skipZero || x != 0.0) {
                test.run(x);
            }
        }
    }
}
