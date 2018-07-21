package org.apache.commons.geometry.euclidean.oned;

import java.util.regex.Pattern;

import org.apache.commons.numbers.core.Precision;
import org.junit.Assert;
import org.junit.Test;

public class Vector1DTest {

    private static final double TEST_TOLERANCE = 1e-15;

    @Test
    public void testConstants() {
        // act/assert
        checkVector(Vector1D.ZERO, 0.0);
        checkVector(Vector1D.ONE, 1.0);
        checkVector(Vector1D.NaN, Double.NaN);
        checkVector(Vector1D.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        checkVector(Vector1D.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testAsPoint() {
        // act/assert
        checkPoint(Vector1D.of(0.0).asPoint(), 0.0);
        checkPoint(Vector1D.of(1.0).asPoint(), 1.0);
        checkPoint(Vector1D.of(-1.0).asPoint(), -1.0);
        checkPoint(Vector1D.of(Double.NaN).asPoint(), Double.NaN);
        checkPoint(Vector1D.of(Double.NEGATIVE_INFINITY).asPoint(), Double.NEGATIVE_INFINITY);
        checkPoint(Vector1D.of(Double.POSITIVE_INFINITY).asPoint(), Double.POSITIVE_INFINITY);
    }

    @Test
    public void testZero() {
        // act
        Vector1D zero = Vector1D.of(1).getZero();

        // assert
        checkVector(zero, 0.0);
        checkPoint(Point1D.ONE.add(zero), 1.0);
    }

    @Test
    public void testNorm1() {
        // act/assert
        Assert.assertEquals(0.0, Vector1D.ZERO.getNorm1(), TEST_TOLERANCE);
        Assert.assertEquals(6.0, Vector1D.of(6).getNorm1(), TEST_TOLERANCE);
        Assert.assertEquals(6.0, Vector1D.of(-6).getNorm1(), TEST_TOLERANCE);
    }

    @Test
    public void testNorm() {
        // act/assert
        Assert.assertEquals(0.0, Vector1D.ZERO.getNorm(), TEST_TOLERANCE);
        Assert.assertEquals(3.0, Vector1D.of(3).getNorm(), TEST_TOLERANCE);
        Assert.assertEquals(3.0, Vector1D.of(-3).getNorm(), TEST_TOLERANCE);
    }

    @Test
    public void testNormSq() {
        // act/assert
        Assert.assertEquals(0.0, Vector1D.of(0).getNormSq(), TEST_TOLERANCE);
        Assert.assertEquals(9.0, Vector1D.of(3).getNormSq(), TEST_TOLERANCE);
        Assert.assertEquals(9.0, Vector1D.of(-3).getNormSq(), TEST_TOLERANCE);
    }

    @Test
    public void testNormInf() {
        // act/assert
        Assert.assertEquals(0.0, Vector1D.ZERO.getNormInf(), TEST_TOLERANCE);
        Assert.assertEquals(3.0, Vector1D.of(3).getNormInf(), TEST_TOLERANCE);
        Assert.assertEquals(3.0, Vector1D.of(-3).getNormInf(), TEST_TOLERANCE);
    }

    @Test
    public void testAdd() {
        // arrange
        Vector1D v1 = Vector1D.of(1);
        Vector1D v2 = Vector1D.of(-3);
        Vector1D v3 = Vector1D.of(3);

        // act/assert
        checkVector(v1.add(v1), 2);
        checkVector(v1.add(v2), -2);
        checkVector(v2.add(v1), -2);
        checkVector(v2.add(v3), 0);
    }

    @Test
    public void testAdd_scaled() {
        // arrange
        Vector1D v1 = Vector1D.of(1);
        Vector1D v2 = Vector1D.of(-3);
        Vector1D v3 = Vector1D.of(3);

        // act/assert
        checkVector(v1.add(1, v1), 2);
        checkVector(v1.add(0.5, v1), 1.5);
        checkVector(v1.add(-1, v1), 0);

        checkVector(v1.add(0, v2), 1);
        checkVector(v2.add(3, v1), 0);
        checkVector(v2.add(2, v3), 3);
    }

    @Test
    public void testSubtract() {
        // arrange
        Vector1D v1 = Vector1D.of(1);
        Vector1D v2 = Vector1D.of(-3);
        Vector1D v3 = Vector1D.of(3);

        // act/assert
        checkVector(v1.subtract(v1), 0);
        checkVector(v1.subtract(v2), 4);
        checkVector(v2.subtract(v1), -4);
        checkVector(v2.subtract(v3), -6);
    }

    @Test
    public void testSubtract_scaled() {
        // arrange
        Vector1D v1 = Vector1D.of(1);
        Vector1D v2 = Vector1D.of(-3);
        Vector1D v3 = Vector1D.of(3);

        // act/assert
        checkVector(v1.subtract(1, v1), 0);
        checkVector(v1.subtract(0.5, v1), 0.5);
        checkVector(v1.subtract(-1, v1), 2);

        checkVector(v1.subtract(0, v2), 1);
        checkVector(v2.subtract(3, v1), -6);
        checkVector(v2.subtract(2, v3), -9);
    }

    @Test
    public void testNormalize() {
        // act/assert
        checkVector(Vector1D.of(1).normalize(), 1);
        checkVector(Vector1D.of(-1).normalize(), -1);
        checkVector(Vector1D.of(5).normalize(), 1);
        checkVector(Vector1D.of(-5).normalize(), -1);
    }

    @Test(expected = IllegalStateException.class)
    public void testNormalize_zeroNorm() {
        // act
        Vector1D.ZERO.normalize();
    }

    @Test
    public void testNegate() {
        // act/assert
        checkVector(Vector1D.of(0.1).negate(), -0.1);
        checkVector(Vector1D.of(-0.1).negate(), 0.1);
    }

    @Test
    public void testScalarMultiply() {
        // act/assert
        checkVector(Vector1D.of(1).scalarMultiply(3), 3);
        checkVector(Vector1D.of(1).scalarMultiply(-3), -3);

        checkVector(Vector1D.of(1.5).scalarMultiply(7), 10.5);
        checkVector(Vector1D.of(-1.5).scalarMultiply(7), -10.5);
    }

    @Test
    public void testDistance1() {
        // arrange
        Vector1D v1 = Vector1D.of(1);
        Vector1D v2 = Vector1D.of(-4);

        // act/assert
        Assert.assertEquals(0.0, v1.distance1(v1), TEST_TOLERANCE);

        Assert.assertEquals(5.0, v1.distance1(v2), TEST_TOLERANCE);
        Assert.assertEquals(5.0, v2.distance1(v1), TEST_TOLERANCE);
        Assert.assertEquals(v1.subtract(v2).getNorm1(), v1.distance1(v2), TEST_TOLERANCE);

        Assert.assertEquals(0.0, Vector1D.of(-1).distance1(Vector1D.of(-1)), TEST_TOLERANCE);
    }

    @Test
    public void testDistance() {
        // arrange
        Vector1D v1 = Vector1D.of(1);
        Vector1D v2 = Vector1D.of(-4);

        // act/assert
        Assert.assertEquals(0.0, v1.distance(v1), TEST_TOLERANCE);

        Assert.assertEquals(5.0, v1.distance(v2), TEST_TOLERANCE);
        Assert.assertEquals(5.0, v2.distance(v1), TEST_TOLERANCE);
        Assert.assertEquals(v1.subtract(v2).getNorm(), v1.distance(v2), TEST_TOLERANCE);

        Assert.assertEquals(0.0, Vector1D.of(-1).distance(Vector1D.of(-1)), TEST_TOLERANCE);
    }

    @Test
    public void testDistanceInf() {
        // arrange
        Vector1D v1 = Vector1D.of(1);
        Vector1D v2 = Vector1D.of(-4);

        // act/assert
        Assert.assertEquals(0.0, Vector1D.of(-1).distanceInf(Vector1D.of(-1)), TEST_TOLERANCE);
        Assert.assertEquals(5.0, v1.distanceInf(v2), TEST_TOLERANCE);
        Assert.assertEquals(5.0, v2.distanceInf(v1), TEST_TOLERANCE);

        Assert.assertEquals(v1.subtract(v2).getNormInf(), v1.distanceInf(v2), TEST_TOLERANCE);
    }

    @Test
    public void testDistanceSq() {
        // arrange
        Vector1D v1 = Vector1D.of(1);
        Vector1D v2 = Vector1D.of(-4);

        // act/assert
        Assert.assertEquals(0.0, Vector1D.of(-1).distanceSq(Vector1D.of(-1)), TEST_TOLERANCE);
        Assert.assertEquals(25.0, v1.distanceSq(v2), TEST_TOLERANCE);
        Assert.assertEquals(25.0, v2.distanceSq(v1), TEST_TOLERANCE);
    }

    @Test
    public void testDotProduct() {
        // arrange
        Vector1D v1 = Vector1D.of(2);
        Vector1D v2 = Vector1D.of(-3);
        Vector1D v3 = Vector1D.of(3);

        // act/assert
        Assert.assertEquals(-6.0, v1.dotProduct(v2), TEST_TOLERANCE);
        Assert.assertEquals(-6.0, v2.dotProduct(v1), TEST_TOLERANCE);

        Assert.assertEquals(6.0, v1.dotProduct(v3), TEST_TOLERANCE);
        Assert.assertEquals(6.0, v3.dotProduct(v1), TEST_TOLERANCE);
    }

    @Test
    public void testHashCode() {
        // arrange
        Vector1D u = Vector1D.of(1);
        Vector1D v = Vector1D.of(1 + 10 * Precision.EPSILON);
        Vector1D w = Vector1D.of(1);

        // act/assert
        Assert.assertTrue(u.hashCode() != v.hashCode());
        Assert.assertEquals(u.hashCode(), w.hashCode());

        Assert.assertEquals(Vector1D.of(Double.NaN).hashCode(), Vector1D.NaN.hashCode());
        Assert.assertEquals(Vector1D.of(Double.NaN).hashCode(), Vector1D.of(Double.NaN).hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        Vector1D u1 = Vector1D.of(1);
        Vector1D u2 = Vector1D.of(1);

        // act/assert
        Assert.assertFalse(u1.equals(null));
        Assert.assertFalse(u1.equals(new Object()));

        Assert.assertTrue(u1.equals(u1));
        Assert.assertTrue(u1.equals(u2));

        Assert.assertFalse(u1.equals(Vector1D.of(-1)));
        Assert.assertFalse(u1.equals(Vector1D.of(1 + 10 * Precision.EPSILON)));

        Assert.assertTrue(Vector1D.of(Double.NaN).equals(Vector1D.of(Double.NaN)));
        Assert.assertTrue(Vector1D.of(Double.POSITIVE_INFINITY).equals(Vector1D.of(Double.POSITIVE_INFINITY)));
        Assert.assertTrue(Vector1D.of(Double.NEGATIVE_INFINITY).equals(Vector1D.of(Double.NEGATIVE_INFINITY)));
    }

    @Test
    public void testToString() {
        // arrange
        Vector1D v = Vector1D.of(3);
        Pattern pattern = Pattern.compile("\\(3.{0,2}\\)");

        // act
        String str = v.toString();

        // assert
        Assert.assertTrue("Expected string " + str + " to match regex " + pattern,
                    pattern.matcher(str).matches());
    }

    @Test
    public void testParse() {
        // act/assert
        checkVector(Vector1D.parse("(1)"), 1);
        checkVector(Vector1D.parse("(-1)"), -1);

        checkVector(Vector1D.parse("(0.01)"), 1e-2);
        checkVector(Vector1D.parse("(-1e-3)"), -1e-3);

        checkVector(Vector1D.parse("(NaN)"), Double.NaN);

        checkVector(Vector1D.parse(Vector1D.ZERO.toString()), 0);
        checkVector(Vector1D.parse(Vector1D.ONE.toString()), 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_failure() {
        // act/assert
        Vector1D.parse("abc");
    }

    @Test
    public void testOf() {
        // act/assert
        checkVector(Vector1D.of(0), 0.0);
        checkVector(Vector1D.of(-1), -1.0);
        checkVector(Vector1D.of(1), 1.0);
        checkVector(Vector1D.of(Math.PI), Math.PI);
        checkVector(Vector1D.of(Double.NaN), Double.NaN);
        checkVector(Vector1D.of(Double.NEGATIVE_INFINITY), Double.NEGATIVE_INFINITY);
        checkVector(Vector1D.of(Double.POSITIVE_INFINITY), Double.POSITIVE_INFINITY);
    }

    @Test
    public void testOf_coordinateArg() {
        // act/assert
        checkVector(Vector1D.of(Vector1D.of(0)), 0.0);
        checkVector(Vector1D.of(Vector1D.of(-1)), -1.0);
        checkVector(Vector1D.of(Vector1D.of(1)), 1.0);
        checkVector(Vector1D.of(Vector1D.of(Math.PI)), Math.PI);
        checkVector(Vector1D.of(Vector1D.of(Double.NaN)), Double.NaN);
        checkVector(Vector1D.of(Vector1D.of(Double.NEGATIVE_INFINITY)), Double.NEGATIVE_INFINITY);
        checkVector(Vector1D.of(Vector1D.of(Double.POSITIVE_INFINITY)), Double.POSITIVE_INFINITY);
    }

    @Test
    public void testLinearCombination() {
        // act/assert
        checkVector(Vector1D.linearCombination(2, Vector1D.of(3)), 6);
        checkVector(Vector1D.linearCombination(-2, Vector1D.of(3)), -6);
    }

    @Test
    public void testLinearCombination2() {
        // act/assert
        checkVector(Vector1D.linearCombination(
                2, Vector1D.of(3),
                5, Vector1D.of(7)), 41);
        checkVector(Vector1D.linearCombination(
                2, Vector1D.of(3),
                -5, Vector1D.of(7)),-29);
    }

    @Test
    public void testLinearCombination3() {
        // act/assert
        checkVector(Vector1D.linearCombination(
                2, Vector1D.of(3),
                5, Vector1D.of(7),
                11, Vector1D.of(13)), 184);
        checkVector(Vector1D.linearCombination(
                2, Vector1D.of(3),
                5, Vector1D.of(7),
                -11, Vector1D.of(13)), -102);
    }

    @Test
    public void testLinearCombination4() {
        // act/assert
        checkVector(Vector1D.linearCombination(
                2, Vector1D.of(3),
                5, Vector1D.of(7),
                11, Vector1D.of(13),
                17, Vector1D.of(19)), 507);
        checkVector(Vector1D.linearCombination(
                2, Vector1D.of(3),
                5, Vector1D.of(7),
                11, Vector1D.of(13),
                -17, Vector1D.of(19)), -139);
    }

    private void checkPoint(Point1D p, double x) {
        Assert.assertEquals(x, p.getX(), TEST_TOLERANCE);
    }

    private void checkVector(Vector1D v, double x) {
        Assert.assertEquals(x, v.getX(), TEST_TOLERANCE);
    }
}
