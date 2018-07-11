package org.apache.commons.geometry.euclidean.twod;

import org.apache.commons.geometry.core.Geometry;
import org.junit.Assert;
import org.junit.Test;


public class Cartesian2DTest {

    private static final double TEST_TOLERANCE = 1e-15;

    @Test
    public void testCoordinates() {
        // arrange
        Cartesian2D c = new StubCartesian2D(1, 2);

        // act/assert
        Assert.assertEquals(1.0, c.getX(), TEST_TOLERANCE);
        Assert.assertEquals(2.0, c.getY(), TEST_TOLERANCE);
    }

    @Test
    public void testToArray() {
        // arrange
        Cartesian2D oneTwo = new StubCartesian2D(1, 2);

        // act
        double[] array = oneTwo.toArray();

        // assert
        Assert.assertEquals(2, array.length);
        Assert.assertEquals(1.0, array[0], TEST_TOLERANCE);
        Assert.assertEquals(2.0, array[1], TEST_TOLERANCE);
    }

    @Test
    public void testDimension() {
        // arrange
        Cartesian2D c = new StubCartesian2D(1, 2);

        // act/assert
        Assert.assertEquals(2, c.getDimension());
    }

    @Test
    public void testNaN() {
        // act/assert
        Assert.assertTrue(new StubCartesian2D(0, Double.NaN).isNaN());
        Assert.assertTrue(new StubCartesian2D(Double.NaN, 0).isNaN());

        Assert.assertFalse(new StubCartesian2D(1, 1).isNaN());
        Assert.assertFalse(new StubCartesian2D(1, Double.NEGATIVE_INFINITY).isNaN());
        Assert.assertFalse(new StubCartesian2D(Double.POSITIVE_INFINITY, 1).isNaN());
    }

    @Test
    public void testInfinite() {
        // act/assert
        Assert.assertTrue(new StubCartesian2D(0, Double.NEGATIVE_INFINITY).isInfinite());
        Assert.assertTrue(new StubCartesian2D(Double.NEGATIVE_INFINITY, 0).isInfinite());
        Assert.assertTrue(new StubCartesian2D(0, Double.POSITIVE_INFINITY).isInfinite());
        Assert.assertTrue(new StubCartesian2D(Double.POSITIVE_INFINITY, 0).isInfinite());

        Assert.assertFalse(new StubCartesian2D(1, 1).isInfinite());
        Assert.assertFalse(new StubCartesian2D(0, Double.NaN).isInfinite());
        Assert.assertFalse(new StubCartesian2D(Double.NEGATIVE_INFINITY, Double.NaN).isInfinite());
        Assert.assertFalse(new StubCartesian2D(Double.NaN, Double.NEGATIVE_INFINITY).isInfinite());
        Assert.assertFalse(new StubCartesian2D(Double.POSITIVE_INFINITY, Double.NaN).isInfinite());
        Assert.assertFalse(new StubCartesian2D(Double.NaN, Double.POSITIVE_INFINITY).isInfinite());
    }

    @Test
    public void testToPolar() {
        // arrange
        double sqrt2 = Math.sqrt(2.0);

        // act/assert
        checkPolar(new StubCartesian2D(0, 0).toPolar(), 0, 0);

        checkPolar(new StubCartesian2D(1, 0).toPolar(), 1, 0);
        checkPolar(new StubCartesian2D(-1, 0).toPolar(), 1, Geometry.PI);

        checkPolar(new StubCartesian2D(0, 2).toPolar(), 2, Geometry.HALF_PI);
        checkPolar(new StubCartesian2D(0, -2).toPolar(), 2, Geometry.THREE_HALVES_PI);

        checkPolar(new StubCartesian2D(sqrt2, sqrt2).toPolar(), 2, 0.25 * Geometry.PI);
        checkPolar(new StubCartesian2D(-sqrt2, sqrt2).toPolar(), 2, 0.75 * Geometry.PI);
        checkPolar(new StubCartesian2D(sqrt2, -sqrt2).toPolar(), 2, 1.75 * Geometry.PI);
        checkPolar(new StubCartesian2D(-sqrt2, -sqrt2).toPolar(), 2, 1.25 * Geometry.PI);
    }

    @Test
    public void testToPolar_NaNAndInfinite() {
        // act/assert
        Assert.assertTrue(new StubCartesian2D(Double.NaN, Double.NaN).toPolar().isNaN());
        Assert.assertTrue(new StubCartesian2D(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).toPolar().isInfinite());
    }

    private void checkPolar(PolarCoordinates polar, double radius, double azimuth) {
        Assert.assertEquals(radius, polar.getRadius(), TEST_TOLERANCE);
        Assert.assertEquals(azimuth, polar.getAzimuth(), TEST_TOLERANCE);
    }

    private static class StubCartesian2D extends Cartesian2D {
        private static final long serialVersionUID = 1L;

        public StubCartesian2D(double x, double y) {
            super(x, y);
        }
    }
}
