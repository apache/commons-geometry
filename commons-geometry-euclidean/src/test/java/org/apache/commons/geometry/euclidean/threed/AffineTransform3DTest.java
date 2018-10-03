package org.apache.commons.geometry.euclidean.threed;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.exception.NonInvertibleTransformException;
import org.junit.Assert;
import org.junit.Test;

public class AffineTransform3DTest {

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
        AffineTransform3D transform = AffineTransform3D.of(arr);

        // assert
        double[] result = transform.toArray();
        Assert.assertNotSame(arr, result);
        Assert.assertArrayEquals(arr, result, 0.0);
    }

    @Test
    public void testOf_invalidDimensions() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> AffineTransform3D.of(1, 2),
                IllegalArgumentException.class, "Dimension mismatch: 2 != 12");
    }

    @Test
    public void testIdentity() {
        // act
        AffineTransform3D transform = AffineTransform3D.identity();

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
        AffineTransform3D transform = AffineTransform3D.createTranslation(2, 3, 4);

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
        AffineTransform3D transform = AffineTransform3D.createTranslation(Vector3D.of(5, 6, 7));

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
        AffineTransform3D transform = AffineTransform3D.createScale(2, 3, 4);

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
        AffineTransform3D a = AffineTransform3D.of(
                    2, 0, 0, 10,
                    0, 3, 0, 11,
                    0, 0, 4, 12
                );

        // act
        AffineTransform3D result = a.translate(4, 5, 6);

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
        AffineTransform3D a = AffineTransform3D.of(
                    2, 0, 0, 10,
                    0, 3, 0, 11,
                    0, 0, 4, 12
                );

        // act
        AffineTransform3D result = a.translate(Vector3D.of(7, 8, 9));

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
        AffineTransform3D transform = AffineTransform3D.createScale(Vector3D.of(4, 5, 6));

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
        AffineTransform3D transform = AffineTransform3D.createScale(7);

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
        AffineTransform3D a = AffineTransform3D.of(
                    2, 0, 0, 10,
                    0, 3, 0, 11,
                    0, 0, 4, 12
                );

        // act
        AffineTransform3D result = a.scale(4, 5, 6);

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
        AffineTransform3D a = AffineTransform3D.of(
                    2, 0, 0, 10,
                    0, 3, 0, 11,
                    0, 0, 4, 12
                );

        // act
        AffineTransform3D result = a.scale(Vector3D.of(7, 8, 9));

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
        AffineTransform3D a = AffineTransform3D.of(
                    2, 0, 0, 10,
                    0, 3, 0, 11,
                    0, 0, 4, 12
                );

        // act
        AffineTransform3D result = a.scale(10);

        // assert
        double[] expected = {
                20, 0, 0, 100,
                0, 30, 0, 110,
                0, 0, 40, 120
        };
        Assert.assertArrayEquals(expected, result.toArray(), 0.0);
    }

    @Test
    public void testApplyTo_identity() {
        // arrange
        AffineTransform3D transform = AffineTransform3D.identity();

        // act/assert
        runWithCoordinates((x, y, z) -> {
            Vector3D v = Vector3D.of(x, y, z);
            Point3D p = Point3D.of(x, y, z);

            EuclideanTestUtils.assertCoordinatesEqual(v, transform.applyTo(v), EPS);
            EuclideanTestUtils.assertCoordinatesEqual(p, transform.applyTo(p), EPS);
        });
    }

    @Test
    public void testApplyTo_translate() {
        // arrange
        Vector3D translation = Vector3D.of(1.1, -Geometry.PI, 5.5);

        AffineTransform3D transform = AffineTransform3D.identity()
                .translate(translation);

        // act/assert
        runWithCoordinates((x, y, z) -> {
            Vector3D vec = Vector3D.of(x, y, z);
            Point3D pt = vec.asPoint();

            Vector3D expectedVec = vec.add(translation);
            Point3D expectedPt = pt.add(translation);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.applyTo(vec), EPS);
            EuclideanTestUtils.assertCoordinatesEqual(expectedPt, transform.applyTo(pt), EPS);
        });
    }

    @Test
    public void testApplyTo_scale() {
        // arrange
        Vector3D factors = Vector3D.of(2.0, -3.0, 4.0);

        AffineTransform3D transform = AffineTransform3D.identity()
                .scale(factors);

        // act/assert
        runWithCoordinates((x, y, z) -> {
            Vector3D vec = Vector3D.of(x, y, z);
            Point3D pt = vec.asPoint();

            Vector3D expectedVec = Vector3D.of(factors.getX() * x, factors.getY() * y, factors.getZ() * z);
            Point3D expectedPt = expectedVec.asPoint();

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.applyTo(vec), EPS);
            EuclideanTestUtils.assertCoordinatesEqual(expectedPt, transform.applyTo(pt), EPS);
        });
    }

    @Test
    public void testApplyTo_translateThenScale() {
        // arrange
        Vector3D translation = Vector3D.of(-2.0, -3.0, -4.0);
        Vector3D scale = Vector3D.of(5.0, 6.0, 7.0);

        AffineTransform3D transform = AffineTransform3D.identity()
                .translate(translation)
                .scale(scale);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Point3D.of(-5, -12, -21), transform.applyTo(Point3D.of(1, 1, 1)), EPS);

        runWithCoordinates((x, y, z) -> {
            Vector3D vec = Vector3D.of(x, y, z);
            Point3D pt = vec.asPoint();

            Vector3D expectedVec = Vector3D.of(
                        (x + translation.getX()) * scale.getX(),
                        (y + translation.getY()) * scale.getY(),
                        (z + translation.getZ()) * scale.getZ()
                    );
            Point3D expectedPt = expectedVec.asPoint();

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.applyTo(vec), EPS);
            EuclideanTestUtils.assertCoordinatesEqual(expectedPt, transform.applyTo(pt), EPS);
        });
    }

    @Test
    public void testApplyTo_scaleThenTranslate() {
        // arrange
        Vector3D scale = Vector3D.of(5.0, 6.0, 7.0);
        Vector3D translation = Vector3D.of(-2.0, -3.0, -4.0);

        AffineTransform3D transform = AffineTransform3D.identity()
                .scale(scale)
                .translate(translation);

        // act/assert
        runWithCoordinates((x, y, z) -> {
            Vector3D vec = Vector3D.of(x, y, z);
            Point3D pt = vec.asPoint();

            Vector3D expectedVec = Vector3D.of(
                        (x * scale.getX()) + translation.getX(),
                        (y * scale.getY()) + translation.getY(),
                        (z * scale.getZ()) + translation.getZ()
                    );
            Point3D expectedPt = expectedVec.asPoint();

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.applyTo(vec), EPS);
            EuclideanTestUtils.assertCoordinatesEqual(expectedPt, transform.applyTo(pt), EPS);
        });
    }

    @Test
    public void testMultiply() {
        // arrange
        AffineTransform3D a = AffineTransform3D.of(
                    1, 2, 3, 4,
                    5, 6, 7, 8,
                    9, 10, 11, 12
                );
        AffineTransform3D b = AffineTransform3D.of(
                    13, 14, 15, 16,
                    17, 18, 19, 20,
                    21, 22, 23, 24
                );

        // act
        AffineTransform3D result = a.multiply(b);

        // assert
        double[] arr = result.toArray();
        Assert.assertArrayEquals(new double[] {
                110, 116, 122, 132,
                314, 332, 350, 376,
                518, 548, 578, 620
        }, arr, EPS);
    }

    @Test
    public void testMultiply_composeTransformOperations() {
        // arrange
        Vector3D translation1 = Vector3D.of(1, 2, 3);
        double scale = 2.0;
        Vector3D translation2 = Vector3D.of(4, 5, 6);

        AffineTransform3D a = AffineTransform3D.createTranslation(translation1);
        AffineTransform3D b = AffineTransform3D.createScale(scale);
        AffineTransform3D c = AffineTransform3D.identity();
        AffineTransform3D d = AffineTransform3D.createTranslation(translation2);

        // act
        AffineTransform3D transform = d.multiply(c).multiply(b).multiply(a);

        // assert
        runWithCoordinates((x, y, z) -> {
            Vector3D vec = Vector3D.of(x, y, z);

            Vector3D expectedVec = vec
                    .add(translation1)
                    .scalarMultiply(scale)
                    .add(translation2);

            EuclideanTestUtils.assertCoordinatesEqual(expectedVec, transform.applyTo(vec), EPS);
        });
    }

    @Test
    public void testGetInverse_identity() {
        // act
        AffineTransform3D inverse = AffineTransform3D.identity().getInverse();

        // assert
        double[] expected = {
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0
        };
        Assert.assertArrayEquals(expected, inverse.toArray(), 0.0);
    }

    @Test
    public void testGetInverse_multiplyByInverse_producesIdentity() {
        // arrange
        AffineTransform3D a = AffineTransform3D.of(
                    1, 3, 7, 8,
                    2, 4, 9, 12,
                    5, 6, 10, 11
                );

        AffineTransform3D inv = a.getInverse();

        // act
        AffineTransform3D result = inv.multiply(a);

        // assert
        double[] expected = {
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0
        };
        Assert.assertArrayEquals(expected, result.toArray(), EPS);
    }

    @Test
    public void testGetInverse_translate() {
        // arrange
        AffineTransform3D transform = AffineTransform3D.createTranslation(1, -2, 4);

        // act
        AffineTransform3D inverse = transform.getInverse();

        // assert
        double[] expected = {
                1, 0, 0, -1,
                0, 1, 0, 2,
                0, 0, 1, -4
        };
        Assert.assertArrayEquals(expected, inverse.toArray(), 0.0);
    }

    @Test
    public void testGetInverse_scale() {
        // arrange
        AffineTransform3D transform = AffineTransform3D.createScale(10, -2, 4);

        // act
        AffineTransform3D inverse = transform.getInverse();

        // assert
        double[] expected = {
                0.1, 0, 0, 0,
                0, -0.5, 0, 0,
                0, 0, 0.25, 0
        };
        Assert.assertArrayEquals(expected, inverse.toArray(), 0.0);
    }

    @Test
    public void testGetInverse_undoesOriginalTransform_translationAndScale() {
        // arrange
        Vector3D v1 = Vector3D.ZERO;
        Vector3D v2 = Vector3D.PLUS_X;
        Vector3D v3 = Vector3D.of(1, 1, 1);
        Vector3D v4 = Vector3D.of(-2, 3, 4);

        // act/assert
        runWithCoordinates((x, y, z) -> {
            AffineTransform3D transform = AffineTransform3D
                        .createTranslation(x, y, z)
                        .scale(2, 3, 4)
                        .translate(x / 3, y / 3, z / 3);

            AffineTransform3D inverse = transform.getInverse();

            EuclideanTestUtils.assertCoordinatesEqual(v1, inverse.applyTo(transform.applyTo(v1)), EPS);
            EuclideanTestUtils.assertCoordinatesEqual(v2, inverse.applyTo(transform.applyTo(v2)), EPS);
            EuclideanTestUtils.assertCoordinatesEqual(v3, inverse.applyTo(transform.applyTo(v3)), EPS);
            EuclideanTestUtils.assertCoordinatesEqual(v4, inverse.applyTo(transform.applyTo(v4)), EPS);
        });
    }

    @Test
    public void testGetInverse_nonInvertible() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            AffineTransform3D.of(
                    0, 0, 0, 0,
                    0, 0, 0, 0,
                    0, 0, 0, 0).getInverse();
        }, NonInvertibleTransformException.class, "Transform is not invertible; matrix determinant is 0.0");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransform3D.of(
                    1, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, Double.NaN, 0).getInverse();
        }, NonInvertibleTransformException.class, "Transform is not invertible; matrix determinant is NaN");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransform3D.of(
                    1, 0, 0, 0,
                    0, Double.NEGATIVE_INFINITY, 0, 0,
                    0, 0, 1, 0).getInverse();
        }, NonInvertibleTransformException.class, "Transform is not invertible; matrix determinant is NaN");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransform3D.of(
                    Double.POSITIVE_INFINITY, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, 1, 0).getInverse();
        }, NonInvertibleTransformException.class, "Transform is not invertible; matrix determinant is NaN");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransform3D.of(
                    1, 0, 0, Double.NaN,
                    0, 1, 0, 0,
                    0, 0, 1, 0).getInverse();
        }, NonInvertibleTransformException.class, "Transform is not invertible; invalid matrix element: NaN");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransform3D.of(
                    1, 0, 0, 0,
                    0, 1, 0, Double.POSITIVE_INFINITY,
                    0, 0, 1, 0).getInverse();
        }, NonInvertibleTransformException.class, "Transform is not invertible; invalid matrix element: Infinity");

        GeometryTestUtils.assertThrows(() -> {
            AffineTransform3D.of(
                    1, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, 1, Double.NEGATIVE_INFINITY).getInverse();
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
        int orig = AffineTransform3D.of(values).hashCode();
        int same = AffineTransform3D.of(values).hashCode();

        Assert.assertEquals(orig, same);

        double[] temp;
        for (int i=0; i<values.length; ++i) {
           temp = values.clone();
           temp[i] = 0;

           int modified = AffineTransform3D.of(temp).hashCode();

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

        AffineTransform3D a = AffineTransform3D.of(values);

        // act/assert
        Assert.assertTrue(a.equals(a));

        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        double[] temp;
        for (int i=0; i<values.length; ++i) {
           temp = values.clone();
           temp[i] = 0;

           AffineTransform3D modified = AffineTransform3D.of(temp);

           Assert.assertFalse(a.equals(modified));
        }
    }

    @Test
    public void testToString() {
        // arrange
        AffineTransform3D a = AffineTransform3D.of(
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

    @FunctionalInterface
    private static interface Coordinate3DTest {

        void run(double x, double y, double z);
    }

    private static void runWithCoordinates(Coordinate3DTest test) {
        runWithCoordinates(test, -1e-2, 1e-2, 5e-3);
        runWithCoordinates(test, -1e2, 1e2, 5);
    }

    private static void runWithCoordinates(Coordinate3DTest test, double min, double max, double step)
    {
        for (double x = min; x <= max; x += step) {
            for (double y = min; y <= max; y += step) {
                for (double z = min; z <= max; z += step) {
                    test.run(x, y, z);
                }
            }
        }
    }
}
