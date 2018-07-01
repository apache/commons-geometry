package org.apache.commons.geometry.euclidean.threed;

import java.util.regex.Pattern;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.util.Coordinates;
import org.apache.commons.geometry.euclidean.twod.PolarCoordinates;
import org.junit.Assert;
import org.junit.Test;


public class SphericalCoordinatesTest {

    private static final double EPS = 1e-10;

    private static final double QUARTER_PI = 0.25 * Geometry.PI;
    private static final double MINUS_QUARTER_PI = -0.25 * Geometry.PI;
    private static final double THREE_QUARTER_PI = 0.75 * Geometry.PI;
    private static final double MINUS_THREE_QUARTER_PI = -0.75 * Geometry.PI;

    @Test
    public void testOf() {
        // act/assert
        checkSpherical(SphericalCoordinates.of(0, 0, 0), 0, 0, 0);
        checkSpherical(SphericalCoordinates.of(0.1, 0.2, 0.3), 0.1, 0.2, 0.3);

        checkSpherical(SphericalCoordinates.of(1, Geometry.HALF_PI, Geometry.PI),
                1, Geometry.HALF_PI, Geometry.PI);
        checkSpherical(SphericalCoordinates.of(1, Geometry.MINUS_HALF_PI, Geometry.HALF_PI),
                1, Geometry.MINUS_HALF_PI, Geometry.HALF_PI);
    }

    @Test
    public void testOf_normalizesAzimuthAngle() {
        // act/assert
        checkSpherical(SphericalCoordinates.of(2, Geometry.TWO_PI, 0), 2, 0, 0);
        checkSpherical(SphericalCoordinates.of(2, Geometry.HALF_PI + Geometry.TWO_PI, 0), 2, Geometry.HALF_PI, 0);
        checkSpherical(SphericalCoordinates.of(2, -Geometry.PI, 0), 2, Geometry.PI, 0);
        checkSpherical(SphericalCoordinates.of(2, Geometry.PI * 1.5, 0), 2, Geometry.MINUS_HALF_PI, 0);
    }

    @Test
    public void testOf_normalizesPolarAngle() {
        // act/assert
        checkSpherical(SphericalCoordinates.of(1, 0, 0), 1, 0, 0);

        checkSpherical(SphericalCoordinates.of(1, 0, QUARTER_PI), 1, 0, QUARTER_PI);
        checkSpherical(SphericalCoordinates.of(1, 0, MINUS_QUARTER_PI), 1, 0, QUARTER_PI);

        checkSpherical(SphericalCoordinates.of(1, 0, Geometry.HALF_PI), 1, 0, Geometry.HALF_PI);
        checkSpherical(SphericalCoordinates.of(1, 0, Geometry.MINUS_HALF_PI), 1, 0, Geometry.HALF_PI);

        checkSpherical(SphericalCoordinates.of(1, 0, THREE_QUARTER_PI), 1, 0, THREE_QUARTER_PI);
        checkSpherical(SphericalCoordinates.of(1, 0, MINUS_THREE_QUARTER_PI), 1, 0, THREE_QUARTER_PI);

        checkSpherical(SphericalCoordinates.of(1, 0, Geometry.TWO_PI), 1, 0, 0);
        checkSpherical(SphericalCoordinates.of(1, 0, Geometry.MINUS_TWO_PI), 1, 0, 0);
    }

    @Test
    public void testOf_angleWrapAround() {
        // act/assert
        checkOfWithAngleWrapAround(1, 0, 0);
        checkOfWithAngleWrapAround(1, QUARTER_PI, QUARTER_PI);
        checkOfWithAngleWrapAround(1, Geometry.HALF_PI, Geometry.HALF_PI);
        checkOfWithAngleWrapAround(1, THREE_QUARTER_PI, THREE_QUARTER_PI);
        checkOfWithAngleWrapAround(1, Geometry.PI, Geometry.PI);
    }

    private void checkOfWithAngleWrapAround(double radius, double azimuth, double polar) {
        for (int i=-4; i<=4; ++i) {
            checkSpherical(
                    SphericalCoordinates.of(radius, azimuth + (i * Geometry.TWO_PI), polar + (-i * Geometry.TWO_PI)),
                    radius, azimuth, polar);
        }
    }

    @Test
    public void testOf_negativeRadius() {
        // act/assert
        checkSpherical(SphericalCoordinates.of(-2, 0, 0), 2, Geometry.PI, Geometry.PI);
        checkSpherical(SphericalCoordinates.of(-2, Geometry.PI, Geometry.PI), 2, 0, 0);

        checkSpherical(SphericalCoordinates.of(-3, Geometry.HALF_PI, QUARTER_PI), 3, Geometry.MINUS_HALF_PI, THREE_QUARTER_PI);
        checkSpherical(SphericalCoordinates.of(-3, Geometry.MINUS_HALF_PI, THREE_QUARTER_PI), 3, Geometry.HALF_PI, QUARTER_PI);

        checkSpherical(SphericalCoordinates.of(-4, QUARTER_PI, Geometry.HALF_PI), 4, MINUS_THREE_QUARTER_PI, Geometry.HALF_PI);
        checkSpherical(SphericalCoordinates.of(-4, MINUS_THREE_QUARTER_PI, Geometry.HALF_PI), 4, QUARTER_PI, Geometry.HALF_PI);
    }

    @Test
    public void testOf_NaNAndInfinite() {
        // act/assert
        checkSpherical(SphericalCoordinates.of(Double.NaN, Double.NaN, Double.NaN),
                Double.NaN, Double.NaN, Double.NaN);
        checkSpherical(SphericalCoordinates.of(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        checkSpherical(SphericalCoordinates.of(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY),
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    @Test
    public void testOfCartesian() {
        // arrange
        double sqrt3 = Math.sqrt(3);

        // act/assert
        checkSpherical(SphericalCoordinates.ofCartesian(0, 0, 0), 0, 0, 0);

        checkSpherical(SphericalCoordinates.ofCartesian(0.1, 0, 0), 0.1, 0, Geometry.HALF_PI);
        checkSpherical(SphericalCoordinates.ofCartesian(-0.1, 0, 0), 0.1, Geometry.PI, Geometry.HALF_PI);

        checkSpherical(SphericalCoordinates.ofCartesian(0, 0.1, 0), 0.1, Geometry.HALF_PI, Geometry.HALF_PI);
        checkSpherical(SphericalCoordinates.ofCartesian(0, -0.1, 0), 0.1, Geometry.MINUS_HALF_PI, Geometry.HALF_PI);

        checkSpherical(SphericalCoordinates.ofCartesian(0, 0, 0.1), 0.1, 0, 0);
        checkSpherical(SphericalCoordinates.ofCartesian(0, 0, -0.1), 0.1, 0, Geometry.PI);

        checkSpherical(SphericalCoordinates.ofCartesian(1, 1, 1), sqrt3, QUARTER_PI, Math.acos(1 / sqrt3));
        checkSpherical(SphericalCoordinates.ofCartesian(-1, -1, -1), sqrt3, MINUS_THREE_QUARTER_PI, Math.acos(-1 / sqrt3));
    }

    @Test
    public void testToPoint() {
        // arrange
        double sqrt3 = Math.sqrt(3);

        // act/assert
        checkPoint(SphericalCoordinates.of(0, 0, 0).toPoint(), 0, 0, 0);

        checkPoint(SphericalCoordinates.of(1, 0, Geometry.HALF_PI).toPoint(), 1, 0, 0);
        checkPoint(SphericalCoordinates.of(1, Geometry.PI, Geometry.HALF_PI).toPoint(), -1, 0, 0);

        checkPoint(SphericalCoordinates.of(2, Geometry.HALF_PI, Geometry.HALF_PI).toPoint(), 0, 2, 0);
        checkPoint(SphericalCoordinates.of(2, Geometry.MINUS_HALF_PI, Geometry.HALF_PI).toPoint(), 0, -2, 0);

        checkPoint(SphericalCoordinates.of(3, 0, 0).toPoint(), 0, 0, 3);
        checkPoint(SphericalCoordinates.of(3, 0, Geometry.PI).toPoint(), 0, 0, -3);

        checkPoint(SphericalCoordinates.of(Math.sqrt(3), QUARTER_PI, Math.acos(1 / sqrt3)).toPoint(), 1, 1, 1);
        checkPoint(SphericalCoordinates.of(Math.sqrt(3), MINUS_THREE_QUARTER_PI, Math.acos(-1 / sqrt3)).toPoint(), -1, -1, -1);
    }

    @Test
    public void testToVector() {
        // arrange
        double sqrt3 = Math.sqrt(3);

        // act/assert
        checkVector(SphericalCoordinates.of(0, 0, 0).toVector(), 0, 0, 0);

        checkVector(SphericalCoordinates.of(1, 0, Geometry.HALF_PI).toVector(), 1, 0, 0);
        checkVector(SphericalCoordinates.of(1, Geometry.PI, Geometry.HALF_PI).toVector(), -1, 0, 0);

        checkVector(SphericalCoordinates.of(2, Geometry.HALF_PI, Geometry.HALF_PI).toVector(), 0, 2, 0);
        checkVector(SphericalCoordinates.of(2, Geometry.MINUS_HALF_PI, Geometry.HALF_PI).toVector(), 0, -2, 0);

        checkVector(SphericalCoordinates.of(3, 0, 0).toVector(), 0, 0, 3);
        checkVector(SphericalCoordinates.of(3, 0, Geometry.PI).toVector(), 0, 0, -3);

        checkVector(SphericalCoordinates.of(Math.sqrt(3), QUARTER_PI, Math.acos(1 / sqrt3)).toVector(), 1, 1, 1);
        checkVector(SphericalCoordinates.of(Math.sqrt(3), MINUS_THREE_QUARTER_PI, Math.acos(-1 / sqrt3)).toVector(), -1, -1, -1);
    }

    @Test
    public void testToCartesian_callback() {
        // arrange
        double sqrt3 = Math.sqrt(3);
        Coordinates.Factory3D<Point3D> factory = Point3D.getFactory();

        // act/assert
        checkPoint(SphericalCoordinates.of(0, 0, 0).toCartesian(factory), 0, 0, 0);

        checkPoint(SphericalCoordinates.of(1, 0, Geometry.HALF_PI).toCartesian(factory), 1, 0, 0);
        checkPoint(SphericalCoordinates.of(1, Geometry.PI, Geometry.HALF_PI).toCartesian(factory), -1, 0, 0);

        checkPoint(SphericalCoordinates.of(2, Geometry.HALF_PI, Geometry.HALF_PI).toCartesian(factory), 0, 2, 0);
        checkPoint(SphericalCoordinates.of(2, Geometry.MINUS_HALF_PI, Geometry.HALF_PI).toCartesian(factory), 0, -2, 0);

        checkPoint(SphericalCoordinates.of(3, 0, 0).toCartesian(factory), 0, 0, 3);
        checkPoint(SphericalCoordinates.of(3, 0, Geometry.PI).toCartesian(factory), 0, 0, -3);

        checkPoint(SphericalCoordinates.of(Math.sqrt(3), QUARTER_PI, Math.acos(1 / sqrt3)).toCartesian(factory), 1, 1, 1);
        checkPoint(SphericalCoordinates.of(Math.sqrt(3), MINUS_THREE_QUARTER_PI, Math.acos(-1 / sqrt3)).toCartesian(factory), -1, -1, -1);
    }

    @Test
    public void testToCartesian_static() {
        // arrange
        double sqrt3 = Math.sqrt(3);
        Coordinates.Factory3D<Point3D> factory = Point3D.getFactory();

        // act/assert
        checkPoint(SphericalCoordinates.toCartesian(0, 0, 0, factory), 0, 0, 0);

        checkPoint(SphericalCoordinates.toCartesian(1, 0, Geometry.HALF_PI, factory), 1, 0, 0);
        checkPoint(SphericalCoordinates.toCartesian(1, Geometry.PI, Geometry.HALF_PI, factory), -1, 0, 0);

        checkPoint(SphericalCoordinates.toCartesian(2, Geometry.HALF_PI, Geometry.HALF_PI, factory), 0, 2, 0);
        checkPoint(SphericalCoordinates.toCartesian(2, Geometry.MINUS_HALF_PI, Geometry.HALF_PI, factory), 0, -2, 0);

        checkPoint(SphericalCoordinates.toCartesian(3, 0, 0, factory), 0, 0, 3);
        checkPoint(SphericalCoordinates.toCartesian(3, 0, Geometry.PI, factory), 0, 0, -3);

        checkPoint(SphericalCoordinates.toCartesian(Math.sqrt(3), QUARTER_PI, Math.acos(1 / sqrt3), factory), 1, 1, 1);
        checkPoint(SphericalCoordinates.toCartesian(Math.sqrt(3), MINUS_THREE_QUARTER_PI, Math.acos(-1 / sqrt3), factory), -1, -1, -1);
    }

    @Test
    public void testGetDimension() {
        // arrange
        SphericalCoordinates s = SphericalCoordinates.of(0, 0, 0);

        // act/assert
        Assert.assertEquals(3, s.getDimension());
    }

    @Test
    public void testNaN() {
        // act/assert
        Assert.assertTrue(SphericalCoordinates.of(0, 0, Double.NaN).isNaN());
        Assert.assertTrue(SphericalCoordinates.of(0, Double.NaN, 0).isNaN());
        Assert.assertTrue(SphericalCoordinates.of(Double.NaN, 0, 0).isNaN());

        Assert.assertFalse(SphericalCoordinates.of(1, 1, 1).isNaN());
        Assert.assertFalse(SphericalCoordinates.of(1, 1, Double.NEGATIVE_INFINITY).isNaN());
        Assert.assertFalse(SphericalCoordinates.of(1, Double.POSITIVE_INFINITY, 1).isNaN());
        Assert.assertFalse(SphericalCoordinates.of(Double.NEGATIVE_INFINITY, 1, 1).isNaN());
    }

    @Test
    public void testInfinite() {
        // act/assert
        Assert.assertTrue(SphericalCoordinates.of(0, 0, Double.NEGATIVE_INFINITY).isInfinite());
        Assert.assertTrue(SphericalCoordinates.of(0, Double.NEGATIVE_INFINITY, 0).isInfinite());
        Assert.assertTrue(SphericalCoordinates.of(Double.NEGATIVE_INFINITY, 0, 0).isInfinite());
        Assert.assertTrue(SphericalCoordinates.of(0, 0, Double.POSITIVE_INFINITY).isInfinite());
        Assert.assertTrue(SphericalCoordinates.of(0, Double.POSITIVE_INFINITY, 0).isInfinite());
        Assert.assertTrue(SphericalCoordinates.of(Double.POSITIVE_INFINITY, 0, 0).isInfinite());

        Assert.assertFalse(SphericalCoordinates.of(1, 1, 1).isInfinite());
        Assert.assertFalse(SphericalCoordinates.of(0, 0, Double.NaN).isInfinite());
        Assert.assertFalse(SphericalCoordinates.of(0, Double.NEGATIVE_INFINITY, Double.NaN).isInfinite());
        Assert.assertFalse(SphericalCoordinates.of(Double.NaN, 0, Double.NEGATIVE_INFINITY).isInfinite());
        Assert.assertFalse(SphericalCoordinates.of(Double.POSITIVE_INFINITY, Double.NaN, 0).isInfinite());
        Assert.assertFalse(SphericalCoordinates.of(0, Double.NaN, Double.POSITIVE_INFINITY).isInfinite());
    }

    @Test
    public void testHashCode() {
        // arrange
        SphericalCoordinates a = SphericalCoordinates.of(1, 2, 3);
        SphericalCoordinates b = SphericalCoordinates.of(10, 2, 3);
        SphericalCoordinates c = SphericalCoordinates.of(1, 20, 3);
        SphericalCoordinates d = SphericalCoordinates.of(1, 2, 30);

        SphericalCoordinates e = SphericalCoordinates.of(1, 2, 3);

        // act/assert
        Assert.assertEquals(a.hashCode(), a.hashCode());
        Assert.assertEquals(a.hashCode(), e.hashCode());

        Assert.assertNotEquals(a.hashCode(), b.hashCode());
        Assert.assertNotEquals(a.hashCode(), c.hashCode());
        Assert.assertNotEquals(a.hashCode(), d.hashCode());
    }

    @Test
    public void testHashCode_NaNInstancesHaveSameHashCode() {
        // arrange
        SphericalCoordinates a = SphericalCoordinates.of(1, 2, Double.NaN);
        SphericalCoordinates b = SphericalCoordinates.of(1, Double.NaN, 3);
        SphericalCoordinates c = SphericalCoordinates.of(Double.NaN, 2, 3);

        // act/assert
        Assert.assertEquals(a.hashCode(), b.hashCode());
        Assert.assertEquals(b.hashCode(), c.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        SphericalCoordinates a = SphericalCoordinates.of(1, 2, 3);
        SphericalCoordinates b = SphericalCoordinates.of(10, 2, 3);
        SphericalCoordinates c = SphericalCoordinates.of(1, 20, 3);
        SphericalCoordinates d = SphericalCoordinates.of(1, 2, 30);

        SphericalCoordinates e = SphericalCoordinates.of(1, 2, 3);

        // act/assert
        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        Assert.assertTrue(a.equals(a));
        Assert.assertTrue(a.equals(e));

        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(a.equals(c));
        Assert.assertFalse(a.equals(d));
    }

    @Test
    public void testEquals_NaNInstancesEqual() {
        // arrange
        SphericalCoordinates a = SphericalCoordinates.of(1, 2, Double.NaN);
        SphericalCoordinates b = SphericalCoordinates.of(1, Double.NaN, 3);
        SphericalCoordinates c = SphericalCoordinates.of(Double.NaN, 2, 3);

        // act/assert
        Assert.assertTrue(a.equals(b));
        Assert.assertTrue(b.equals(c));
    }

    @Test
    public void testToString() {
        // arrange
        SphericalCoordinates sph = SphericalCoordinates.of(1, 2, 3);
        Pattern pattern = Pattern.compile("\\(1.{0,2}, 2.{0,2}, 3.{0,2}\\)");

        // act
        String str = sph.toString();;

        // assert
        Assert.assertTrue("Expected string " + str + " to match regex " + pattern,
                    pattern.matcher(str).matches());
    }

    @Test
    public void testParse() {
        // act/assert
        checkSpherical(SphericalCoordinates.parse("(1, 2, -3)"), 1, 2, 3);
        checkSpherical(SphericalCoordinates.parse("(  2e0 , 5 , -0.000 )"), 2, 5 - Geometry.TWO_PI, 0);
        checkSpherical(SphericalCoordinates.parse("(NaN,Infinity,-Infinity)"), Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_failure() {
        // act/assert
        SphericalCoordinates.parse("abc");
    }

    @Test
    public void testGetFactory() {
        // act
        Coordinates.Factory3D<SphericalCoordinates> factory = SphericalCoordinates.getFactory();

        // assert
        checkSpherical(factory.create(2, 0.5 + Geometry.TWO_PI, 0.1 + Geometry.PI), 2, 0.5, Geometry.PI - 0.1);
    }

    private void checkSpherical(SphericalCoordinates c, double radius, double azimuth, double polar) {
        Assert.assertEquals(radius, c.getRadius(), EPS);
        Assert.assertEquals(azimuth, c.getAzimuth(), EPS);
        Assert.assertEquals(polar, c.getPolar(), EPS);
    }

    private void checkPoint(Point3D p, double x, double y, double z) {
        Assert.assertEquals(x, p.getX(), EPS);
        Assert.assertEquals(y, p.getY(), EPS);
        Assert.assertEquals(z, p.getZ(), EPS);
    }

    private void checkVector(Vector3D v, double x, double y, double z) {
        Assert.assertEquals(x, v.getX(), EPS);
        Assert.assertEquals(y, v.getY(), EPS);
        Assert.assertEquals(z, v.getZ(), EPS);
    }
}
