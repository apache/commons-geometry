package org.apache.commons.geometry.euclidean.twod;

import java.util.regex.Pattern;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.numbers.core.Precision;
import org.junit.Assert;
import org.junit.Test;

public class Vector2DTest {

    private static final double EPS = Math.ulp(1d);

    @Test
    public void testConstants() {
        // act/assert
        checkVector(Vector2D.ZERO, 0, 0);
        checkVector(Vector2D.PLUS_X, 1, 0);
        checkVector(Vector2D.MINUS_X, -1, 0);
        checkVector(Vector2D.PLUS_Y, 0, 1);
        checkVector(Vector2D.MINUS_Y, 0, -1);
        checkVector(Vector2D.NaN, Double.NaN, Double.NaN);
        checkVector(Vector2D.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        checkVector(Vector2D.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testToArray() {
        // arrange
        Vector2D v = Vector2D.of(1, 2);

        // act
        double[] arr = v.toArray();

        // assert
        Assert.assertEquals(2, arr.length);
        Assert.assertEquals(1, arr[0], EPS);
        Assert.assertEquals(2, arr[1], EPS);
    }

    @Test
    public void testToPoint() {
        // act/assert
        checkPoint(Vector2D.of(1, 2).asPoint(), 1, 2);
        checkPoint(Vector2D.of(-1, -2).asPoint(), -1, -2);
        checkPoint(Vector2D.of(Double.NaN, Double.POSITIVE_INFINITY).asPoint(), Double.NaN, Double.POSITIVE_INFINITY);
        checkPoint(Vector2D.of(Double.NEGATIVE_INFINITY, Double.NaN).asPoint(), Double.NEGATIVE_INFINITY, Double.NaN);
    }

    @Test
    public void testGetZero() {
        // act/assert
        checkVector(Vector2D.of(1.0, 1.0).getZero(), 0, 0);
    }

    @Test
    public void testNorm1() {
        // act/assert
        Assert.assertEquals(0.0, Vector2D.of(0, 0).getNorm1(), EPS);

        Assert.assertEquals(3.0, Vector2D.of(1, 2).getNorm1(), EPS);
        Assert.assertEquals(3.0, Vector2D.of(1, -2).getNorm1(), EPS);
        Assert.assertEquals(3.0, Vector2D.of(-1, 2).getNorm1(), EPS);
        Assert.assertEquals(3.0, Vector2D.of(-1, -2).getNorm1(), EPS);
    }

    @Test
    public void testNorm() {
        // act/assert
        Assert.assertEquals(0.0, Vector2D.of(0, 0).getNorm(), EPS);

        Assert.assertEquals(5.0, Vector2D.of(3, 4).getNorm(), EPS);
        Assert.assertEquals(5.0, Vector2D.of(3, -4).getNorm(), EPS);
        Assert.assertEquals(5.0, Vector2D.of(-3, 4).getNorm(), EPS);
        Assert.assertEquals(5.0, Vector2D.of(-3, -4).getNorm(), EPS);

        Assert.assertEquals(Math.sqrt(5.0), Vector2D.of(-1, -2).getNorm(), EPS);
    }

    @Test
    public void testNormSq() {
        // act/assert
        Assert.assertEquals(0.0, Vector2D.of(0, 0).getNormSq(), EPS);

        Assert.assertEquals(25.0, Vector2D.of(3, 4).getNormSq(), EPS);
        Assert.assertEquals(25.0, Vector2D.of(3, -4).getNormSq(), EPS);
        Assert.assertEquals(25.0, Vector2D.of(-3, 4).getNormSq(), EPS);
        Assert.assertEquals(25.0, Vector2D.of(-3, -4).getNormSq(), EPS);

        Assert.assertEquals(5.0, Vector2D.of(-1, -2).getNormSq(), EPS);
    }

    @Test
    public void testNormInf() {
        // act/assert
        Assert.assertEquals(0.0, Vector2D.of(0, 0).getNormInf(), EPS);

        Assert.assertEquals(2.0, Vector2D.of(1, 2).getNormInf(), EPS);
        Assert.assertEquals(2.0, Vector2D.of(1, -2).getNormInf(), EPS);
        Assert.assertEquals(2.0, Vector2D.of(-1, 2).getNormInf(), EPS);
        Assert.assertEquals(2.0, Vector2D.of(-1, -2).getNormInf(), EPS);

        Assert.assertEquals(100.0, Vector2D.of(100, -99).getNormInf(), EPS);
    }

    @Test
    public void testAdd() {
        // arrange
        Vector2D v1 = Vector2D.of(-1, 2);
        Vector2D v2 = Vector2D.of(3, -4);
        Vector2D v3 = Vector2D.of(5, 6);

        // act/assert
        checkVector(v1.add(v1), -2, 4);

        checkVector(v1.add(v2), 2, -2);
        checkVector(v2.add(v1), 2, -2);

        checkVector(v1.add(v3), 4, 8);
        checkVector(v3.add(v1), 4, 8);
    }

    @Test
    public void testAdd_scaled() {
        // arrange
        Vector2D v1 = Vector2D.of(-1, 2);
        Vector2D v2 = Vector2D.of(3, -4);
        Vector2D v3 = Vector2D.of(5, 6);

        // act/assert
        checkVector(v1.add(2, v1), -3, 6);

        checkVector(v1.add(0, v2), -1, 2);
        checkVector(v2.add(1, v1), 2, -2);

        checkVector(v1.add(-1, v3), -6, -4);
        checkVector(v3.add(-2, v1), 7, 2);
    }

    @Test
    public void testSubtract() {
        // arrange
        Vector2D v1 = Vector2D.of(-1, 2);
        Vector2D v2 = Vector2D.of(3, -4);
        Vector2D v3 = Vector2D.of(5, 6);

        // act/assert
        checkVector(v1.subtract(v1), 0, 0);

        checkVector(v1.subtract(v2), -4, 6);
        checkVector(v2.subtract(v1), 4, -6);

        checkVector(v1.subtract(v3), -6, -4);
        checkVector(v3.subtract(v1), 6, 4);
    }

    @Test
    public void testSubtract_scaled() {
        // arrange
        Vector2D v1 = Vector2D.of(-1, 2);
        Vector2D v2 = Vector2D.of(3, -4);
        Vector2D v3 = Vector2D.of(5, 6);

        // act/assert
        checkVector(v1.subtract(2, v1), 1, -2);

        checkVector(v1.subtract(0, v2), -1, 2);
        checkVector(v2.subtract(1, v1), 4, -6);

        checkVector(v1.subtract(-1, v3), 4, 8);
        checkVector(v3.subtract(-2, v1), 3, 10);
    }

    @Test
    public void testNormalize() {
        // act/assert
        checkVector(Vector2D.of(100, 0).normalize(), 1, 0);
        checkVector(Vector2D.of(-100, 0).normalize(), -1, 0);
        checkVector(Vector2D.of(0, 100).normalize(), 0, 1);
        checkVector(Vector2D.of(0, -100).normalize(), 0, -1);
        checkVector(Vector2D.of(-1, 2).normalize(), -1.0/Math.sqrt(5), 2.0/Math.sqrt(5));
    }

    @Test(expected = IllegalStateException.class)
    public void testNormalize_zeroNorm() {
        Vector2D.ZERO.normalize();
    }

    @Test
    public void testNegate() {
        // act/assert
        checkVector(Vector2D.of(1, 2).negate(), -1, -2);
        checkVector(Vector2D.of(-3, -4).negate(), 3, 4);
        checkVector(Vector2D.of(5, -6).negate().negate(), 5, -6);
    }

    @Test
    public void testScalarMultiply() {
        // act/assert
        checkVector(Vector2D.of(1, 2).scalarMultiply(0), 0, 0);

        checkVector(Vector2D.of(1, 2).scalarMultiply(3), 3, 6);
        checkVector(Vector2D.of(1, 2).scalarMultiply(-3), -3, -6);

        checkVector(Vector2D.of(2, 3).scalarMultiply(1.5), 3, 4.5);
        checkVector(Vector2D.of(2, 3).scalarMultiply(-1.5), -3, -4.5);
    }

    @Test
    public void testDistance1() {
        // arrange
        Vector2D v1 = Vector2D.of(1, 1);
        Vector2D v2 = Vector2D.of(4, 5);
        Vector2D v3 = Vector2D.of(-1, 0);

        // act/assert
        Assert.assertEquals(0, v1.distance1(v1), EPS);

        Assert.assertEquals(7, v1.distance1(v2), EPS);
        Assert.assertEquals(7, v2.distance1(v1), EPS);

        Assert.assertEquals(3, v1.distance1(v3), EPS);
        Assert.assertEquals(3, v3.distance1(v1), EPS);
    }

    @Test
    public void testDistance() {
        // arrange
        Vector2D v1 = Vector2D.of(1, 1);
        Vector2D v2 = Vector2D.of(4, 5);
        Vector2D v3 = Vector2D.of(-1, 0);

        // act/assert
        Assert.assertEquals(0, v1.distance(v1), EPS);

        Assert.assertEquals(5, v1.distance(v2), EPS);
        Assert.assertEquals(5, v2.distance(v1), EPS);

        Assert.assertEquals(Math.sqrt(5), v1.distance(v3), EPS);
        Assert.assertEquals(Math.sqrt(5), v3.distance(v1), EPS);
    }

    @Test
    public void testDistanceSq() {
        // arrange
        Vector2D v1 = Vector2D.of(1, 1);
        Vector2D v2 = Vector2D.of(4, 5);
        Vector2D v3 = Vector2D.of(-1, 0);

        // act/assert
        Assert.assertEquals(0, v1.distanceSq(v1), EPS);

        Assert.assertEquals(25, v1.distanceSq(v2), EPS);
        Assert.assertEquals(25, v2.distanceSq(v1), EPS);

        Assert.assertEquals(5, v1.distanceSq(v3), EPS);
        Assert.assertEquals(5, v3.distanceSq(v1), EPS);
    }

    @Test
    public void testDistanceInf() {
        // arrange
        Vector2D v1 = Vector2D.of(1, 1);
        Vector2D v2 = Vector2D.of(4, 5);
        Vector2D v3 = Vector2D.of(-1, 0);

        // act/assert
        Assert.assertEquals(0, v1.distanceInf(v1), EPS);

        Assert.assertEquals(4, v1.distanceInf(v2), EPS);
        Assert.assertEquals(4, v2.distanceInf(v1), EPS);

        Assert.assertEquals(2, v1.distanceInf(v3), EPS);
        Assert.assertEquals(2, v3.distanceInf(v1), EPS);
    }

    @Test
    public void testDotProduct() {
        // arrange
        Vector2D v1 = Vector2D.of(1, 1);
        Vector2D v2 = Vector2D.of(4, 5);
        Vector2D v3 = Vector2D.of(-1, 0);

        // act/assert
        Assert.assertEquals(2, v1.dotProduct(v1), EPS);
        Assert.assertEquals(41, v2.dotProduct(v2), EPS);
        Assert.assertEquals(1, v3.dotProduct(v3), EPS);

        Assert.assertEquals(9, v1.dotProduct(v2), EPS);
        Assert.assertEquals(9, v2.dotProduct(v1), EPS);

        Assert.assertEquals(-1, v1.dotProduct(v3), EPS);
        Assert.assertEquals(-1, v3.dotProduct(v1), EPS);

        Assert.assertEquals(1, Vector2D.PLUS_X.dotProduct(Vector2D.PLUS_X), EPS);
        Assert.assertEquals(0, Vector2D.PLUS_X.dotProduct(Vector2D.PLUS_Y), EPS);
        Assert.assertEquals(-1, Vector2D.PLUS_X.dotProduct(Vector2D.MINUS_X), EPS);
        Assert.assertEquals(0, Vector2D.PLUS_X.dotProduct(Vector2D.MINUS_Y), EPS);
    }

    @Test
    public void testDotProduct_static() {
        // arrange
        Vector2D v1 = Vector2D.of(1, 1);
        Vector2D v2 = Vector2D.of(4, 5);
        Vector2D v3 = Vector2D.of(-1, 0);

        // act/assert
        Assert.assertEquals(2, Vector2D.dotProduct(v1, v1), EPS);
        Assert.assertEquals(41, Vector2D.dotProduct(v2, v2), EPS);
        Assert.assertEquals(1, Vector2D.dotProduct(v3, v3), EPS);

        Assert.assertEquals(9, Vector2D.dotProduct(v1, v2), EPS);
        Assert.assertEquals(9, Vector2D.dotProduct(v2, v1), EPS);

        Assert.assertEquals(-1, Vector2D.dotProduct(v1, v3), EPS);
        Assert.assertEquals(-1, Vector2D.dotProduct(v3, v1), EPS);

        Assert.assertEquals(1, Vector2D.dotProduct(Vector2D.PLUS_X, Vector2D.PLUS_X), EPS);
        Assert.assertEquals(0, Vector2D.dotProduct(Vector2D.PLUS_X, Vector2D.PLUS_Y), EPS);
        Assert.assertEquals(-1, Vector2D.dotProduct(Vector2D.PLUS_X, Vector2D.MINUS_X), EPS);
        Assert.assertEquals(0, Vector2D.dotProduct(Vector2D.PLUS_X, Vector2D.MINUS_Y), EPS);
    }

    @Test
    public void testAngle() {
        // act/assert
        Assert.assertEquals(0, Vector2D.PLUS_X.angle(Vector2D.PLUS_X), EPS);

        Assert.assertEquals(Geometry.PI, Vector2D.PLUS_X.angle(Vector2D.MINUS_X), EPS);
        Assert.assertEquals(Geometry.HALF_PI, Vector2D.PLUS_X.angle(Vector2D.PLUS_Y), EPS);
        Assert.assertEquals(Geometry.HALF_PI, Vector2D.PLUS_X.angle(Vector2D.MINUS_Y), EPS);

        Assert.assertEquals(Geometry.PI / 4, Vector2D.of(1, 1).angle(Vector2D.of(1, 0)), EPS);
        Assert.assertEquals(Geometry.PI / 4, Vector2D.of(1, 0).angle(Vector2D.of(1, 1)), EPS);

        Assert.assertEquals(0.004999958333958323, Vector2D.of(20.0, 0.0).angle(Vector2D.of(20.0, 0.1)), EPS);
    }

    @Test
    public void testAngle_static() {
        // act/assert
        Assert.assertEquals(0, Vector2D.angle(Vector2D.PLUS_X, Vector2D.PLUS_X), EPS);

        Assert.assertEquals(Geometry.PI, Vector2D.angle(Vector2D.PLUS_X, Vector2D.MINUS_X), EPS);
        Assert.assertEquals(Geometry.HALF_PI, Vector2D.angle(Vector2D.PLUS_X, Vector2D.PLUS_Y), EPS);
        Assert.assertEquals(Geometry.HALF_PI, Vector2D.angle(Vector2D.PLUS_X, Vector2D.MINUS_Y), EPS);

        Assert.assertEquals(Geometry.PI / 4, Vector2D.angle(Vector2D.of(1, 1), Vector2D.of(1, 0)), EPS);
        Assert.assertEquals(Geometry.PI / 4, Vector2D.angle(Vector2D.of(1, 0), Vector2D.of(1, 1)), EPS);

        Assert.assertEquals(0.004999958333958323, Vector2D.angle(Vector2D.of(20.0, 0.0), Vector2D.of(20.0, 0.1)), EPS);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testAngle_zeroNorm() {
        Vector2D.of(1, 1).angle(Vector2D.ZERO);
    }

    @Test
    public void testCrossProduct() {
        // arrange
        Vector2D p1 = Vector2D.of(1, 1);
        Vector2D p2 = Vector2D.of(2, 2);

        Vector2D p3 = Vector2D.of(3, 3);
        Vector2D p4 = Vector2D.of(1, 2);
        Vector2D p5 = Vector2D.of(2, 1);

        // act/assert
        Assert.assertEquals(0.0, p3.crossProduct(p1, p2), EPS);
        Assert.assertEquals(1.0, p4.crossProduct(p1, p2), EPS);
        Assert.assertEquals(-1.0, p5.crossProduct(p1, p2), EPS);
    }

    @Test
    public void testHashCode() {
        // arrange
        Vector2D u = Vector2D.of(1, 1);
        Vector2D v = Vector2D.of(1 + 10 * Precision.EPSILON, 1 + 10 * Precision.EPSILON);
        Vector2D w = Vector2D.of(1, 1);

        // act/assert
        Assert.assertTrue(u.hashCode() != v.hashCode());
        Assert.assertEquals(u.hashCode(), w.hashCode());

        Assert.assertEquals(Vector2D.of(0, Double.NaN).hashCode(), Vector2D.NaN.hashCode());
        Assert.assertEquals(Vector2D.of(Double.NaN, 0).hashCode(), Vector2D.NaN.hashCode());
        Assert.assertEquals(Vector2D.of(0, Double.NaN).hashCode(), Vector2D.of(Double.NaN, 0).hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        Vector2D u1 = Vector2D.of(1, 2);
        Vector2D u2 = Vector2D.of(1, 2);

        // act/assert
        Assert.assertFalse(u1.equals(null));
        Assert.assertFalse(u1.equals(new Object()));

        Assert.assertTrue(u1.equals(u1));
        Assert.assertTrue(u1.equals(u2));

        Assert.assertFalse(u1.equals(Vector2D.of(-1, -2)));
        Assert.assertFalse(u1.equals(Vector2D.of(1 + 10 * Precision.EPSILON, 2)));
        Assert.assertFalse(u1.equals(Vector2D.of(1, 2 + 10 * Precision.EPSILON)));

        Assert.assertTrue(Vector2D.of(0, Double.NaN).equals(Vector2D.of(Double.NaN, 0)));

        Assert.assertTrue(Vector2D.of(0, Double.POSITIVE_INFINITY).equals(Vector2D.of(0, Double.POSITIVE_INFINITY)));
        Assert.assertFalse(Vector2D.of(Double.POSITIVE_INFINITY, 0).equals(Vector2D.of(0, Double.POSITIVE_INFINITY)));

        Assert.assertTrue(Vector2D.of(Double.NEGATIVE_INFINITY, 0).equals(Vector2D.of(Double.NEGATIVE_INFINITY, 0)));
        Assert.assertFalse(Vector2D.of(0, Double.NEGATIVE_INFINITY).equals(Vector2D.of(Double.NEGATIVE_INFINITY, 0)));
    }

    @Test
    public void testToString() {
        // arrange
        Vector2D v = Vector2D.of(1, 2);
        Pattern pattern = Pattern.compile("\\(1.{0,2}, 2.{0,2}\\)");

        // act
        String str = v.toString();

        // assert
        Assert.assertTrue("Expected string " + str + " to match regex " + pattern,
                    pattern.matcher(str).matches());
    }

    @Test
    public void testParse() {
        // act/assert
        checkVector(Vector2D.parse("(1, 2)"), 1, 2);
        checkVector(Vector2D.parse("(-1, -2)"), -1, -2);

        checkVector(Vector2D.parse("(0.01, -1e-3)"), 1e-2, -1e-3);

        checkVector(Vector2D.parse("(NaN, -Infinity)"), Double.NaN, Double.NEGATIVE_INFINITY);

        checkVector(Vector2D.parse(Vector2D.ZERO.toString()), 0, 0);
        checkVector(Vector2D.parse(Vector2D.MINUS_X.toString()), -1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_failure() {
        // act/assert
        Vector2D.parse("abc");
    }

    @Test
    public void testOf() {
        // act/assert
        checkVector(Vector2D.of(0, 1), 0, 1);
        checkVector(Vector2D.of(-1, -2), -1, -2);
        checkVector(Vector2D.of(Math.PI, Double.NaN), Math.PI, Double.NaN);
        checkVector(Vector2D.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    @Test
    public void testOf_coordinateArg() {
        // act/assert
        checkVector(Vector2D.of(Point2D.of(0, 1)), 0, 1);
        checkVector(Vector2D.of(Point2D.of(-1, -2)), -1, -2);
        checkVector(Vector2D.of(Point2D.of(Math.PI, Double.NaN)), Math.PI, Double.NaN);
        checkVector(Vector2D.of(Point2D.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)), Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    @Test
    public void testOf_arrayArg() {
        // act/assert
        checkVector(Vector2D.of(new double[] { 0, 1 }), 0, 1);
        checkVector(Vector2D.of(new double[] { -1, -2 }), -1, -2);
        checkVector(Vector2D.of(new double[] { Math.PI, Double.NaN }), Math.PI, Double.NaN);
        checkVector(Vector2D.of(new double[] { Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY }), Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOf_arrayArg_invalidDimensions() {
        // act/assert
        Vector2D.of(new double[] {0.0 });
    }

    @Test
    public void testOfPolar() {
        // arrange
        double eps = 1e-15;
        double sqrt2 = Math.sqrt(2.0);

        // act/assert
        checkVector(Vector2D.ofPolar(0, 0), 0, 0, eps);
        checkVector(Vector2D.ofPolar(1, 0), 1, 0, eps);

        checkVector(Vector2D.ofPolar(2, Geometry.PI), -2, 0, eps);
        checkVector(Vector2D.ofPolar(-2, Geometry.PI), 2, 0, eps);

        checkVector(Vector2D.ofPolar(2, Geometry.HALF_PI), 0, 2, eps);
        checkVector(Vector2D.ofPolar(-2, Geometry.HALF_PI), 0, -2, eps);

        checkVector(Vector2D.ofPolar(2, 0.25 * Geometry.PI), sqrt2, sqrt2, eps);
        checkVector(Vector2D.ofPolar(2, 0.75 * Geometry.PI), -sqrt2, sqrt2, eps);
        checkVector(Vector2D.ofPolar(2, -0.25 * Geometry.PI), sqrt2, - sqrt2, eps);
        checkVector(Vector2D.ofPolar(2, -0.75 * Geometry.PI), -sqrt2, - sqrt2, eps);
    }

    @Test
    public void testLinearCombination1() {
        // arrange
        Vector2D p1 = Vector2D.of(1, 2);

        // act/assert
        checkVector(Vector2D.linearCombination(0, p1), 0, 0);

        checkVector(Vector2D.linearCombination(1, p1), 1, 2);
        checkVector(Vector2D.linearCombination(-1, p1), -1, -2);

        checkVector(Vector2D.linearCombination(0.5, p1), 0.5, 1);
        checkVector(Vector2D.linearCombination(-0.5, p1), -0.5, -1);
    }

    @Test
    public void testLinearCombination2() {
        // arrange
        Vector2D p1 = Vector2D.of(1, 2);
        Vector2D p2 = Vector2D.of(-3, -4);

        // act/assert
        checkVector(Vector2D.linearCombination(2, p1, -3, p2), 11, 16);
        checkVector(Vector2D.linearCombination(-3, p1, 2, p2), -9, -14);
    }

    @Test
    public void testLinearCombination3() {
        // arrange
        Vector2D p1 = Vector2D.of(1, 2);
        Vector2D p2 = Vector2D.of(-3, -4);
        Vector2D p3 = Vector2D.of(5, 6);

        // act/assert
        checkVector(Vector2D.linearCombination(2, p1, -3, p2, 4, p3), 31, 40);
        checkVector(Vector2D.linearCombination(-3, p1, 2, p2, -4, p3), -29, -38);
    }

    @Test
    public void testLinearCombination4() {
        // arrange
        Vector2D p1 = Vector2D.of(1, 2);
        Vector2D p2 = Vector2D.of(-3, -4);
        Vector2D p3 = Vector2D.of(5, 6);
        Vector2D p4 = Vector2D.of(-7, -8);

        // act/assert
        checkVector(Vector2D.linearCombination(2, p1, -3, p2, 4, p3, -5, p4), 66, 80);
        checkVector(Vector2D.linearCombination(-3, p1, 2, p2, -4, p3, 5, p4), -64, -78);
    }

    private void checkVector(Vector2D v, double x, double y) {
        checkVector(v, x, y, EPS);
    }

    private void checkVector(Vector2D v, double x, double y, double eps) {
        Assert.assertEquals(x, v.getX(), eps);
        Assert.assertEquals(y, v.getY(), eps);
    }

    private void checkPoint(Point2D p, double x, double y) {
        Assert.assertEquals(x, p.getX(), EPS);
        Assert.assertEquals(y, p.getY(), EPS);
    }
}
