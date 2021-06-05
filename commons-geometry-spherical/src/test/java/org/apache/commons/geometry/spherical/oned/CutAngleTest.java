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
package org.apache.commons.geometry.spherical.oned;

import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CutAngleTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    public void testFromAzimuthAndDirection() {
        // act/assert
        checkCutAngle(CutAngles.fromAzimuthAndDirection(0.0, true, TEST_PRECISION),
                0.0, true);
        checkCutAngle(CutAngles.fromAzimuthAndDirection(Math.PI, true, TEST_PRECISION),
                Math.PI, true);
        checkCutAngle(CutAngles.fromAzimuthAndDirection(-Angle.PI_OVER_TWO, true, TEST_PRECISION),
                -Angle.PI_OVER_TWO, true);

        checkCutAngle(CutAngles.fromAzimuthAndDirection(0.0, false, TEST_PRECISION),
                0.0, false);
        checkCutAngle(CutAngles.fromAzimuthAndDirection(Math.PI, false, TEST_PRECISION),
                Math.PI, false);
        checkCutAngle(CutAngles.fromAzimuthAndDirection(-Angle.PI_OVER_TWO, false, TEST_PRECISION),
                -Angle.PI_OVER_TWO, false);
    }

    @Test
    public void testFromPointAndDirection() {
        // arrange
        final Point1S pt = Point1S.of(-Angle.PI_OVER_TWO);

        // act/assert
        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION),
                0.0, true);
        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.PI, true, TEST_PRECISION),
                Math.PI, true);
        checkCutAngle(CutAngles.fromPointAndDirection(pt, true, TEST_PRECISION),
                -Angle.PI_OVER_TWO, true);

        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.ZERO, false, TEST_PRECISION),
                0.0, false);
        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.PI, false, TEST_PRECISION),
                Math.PI, false);
        checkCutAngle(CutAngles.fromPointAndDirection(pt, false, TEST_PRECISION),
                -Angle.PI_OVER_TWO, false);
    }

    @Test
    public void testCreatePositiveFacing() {
        // act/assert
        checkCutAngle(CutAngles.createPositiveFacing(Point1S.ZERO, TEST_PRECISION),
                0.0, true);
        checkCutAngle(CutAngles.createPositiveFacing(Point1S.PI, TEST_PRECISION),
                Math.PI, true);
        checkCutAngle(CutAngles.createPositiveFacing(-Angle.PI_OVER_TWO, TEST_PRECISION),
                -Angle.PI_OVER_TWO, true);
    }

    @Test
    public void testCreateNegativeFacing() {
        // act/assert
        checkCutAngle(CutAngles.createNegativeFacing(Point1S.ZERO, TEST_PRECISION),
                0.0, false);
        checkCutAngle(CutAngles.createNegativeFacing(Point1S.PI, TEST_PRECISION),
                Math.PI, false);
        checkCutAngle(CutAngles.createNegativeFacing(-Angle.PI_OVER_TWO, TEST_PRECISION),
                -Angle.PI_OVER_TWO, false);
    }

    @Test
    public void testOffset() {
        // arrange
        final CutAngle zeroPos = CutAngles.createPositiveFacing(0.0, TEST_PRECISION);
        final CutAngle zeroNeg = CutAngles.createNegativeFacing(0.0, TEST_PRECISION);
        final CutAngle negPiPos = CutAngles.createPositiveFacing(-Math.PI, TEST_PRECISION);

        final CutAngle piNeg = CutAngles.createNegativeFacing(Math.PI, TEST_PRECISION);
        final CutAngle twoAndAHalfPiPos = CutAngles.createPositiveFacing(2.5 * Math.PI, TEST_PRECISION);

        // act/assert
        checkOffset(zeroPos, 0, 0);
        checkOffset(zeroPos, Angle.TWO_PI, 0);
        checkOffset(zeroPos, 2.5 * Math.PI, Angle.PI_OVER_TWO);
        checkOffset(zeroPos, Math.PI, Math.PI);
        checkOffset(zeroPos, 3.5 * Math.PI, 1.5 * Math.PI);

        checkOffset(zeroNeg, 0, 0);
        checkOffset(zeroNeg, Angle.TWO_PI, 0);
        checkOffset(zeroNeg, 2.5 * Math.PI, -Angle.PI_OVER_TWO);
        checkOffset(zeroNeg, Math.PI, -Math.PI);
        checkOffset(zeroNeg, 3.5 * Math.PI, -1.5 * Math.PI);

        checkOffset(negPiPos, 0, -Math.PI);
        checkOffset(negPiPos, Angle.TWO_PI, -Math.PI);
        checkOffset(negPiPos, 2.5 * Math.PI, -Angle.PI_OVER_TWO);
        checkOffset(negPiPos, Math.PI, 0);
        checkOffset(negPiPos, 3.5 * Math.PI, Angle.PI_OVER_TWO);

        checkOffset(piNeg, 0, Math.PI);
        checkOffset(piNeg, Angle.TWO_PI, Math.PI);
        checkOffset(piNeg, 2.5 * Math.PI, Angle.PI_OVER_TWO);
        checkOffset(piNeg, Math.PI, 0);
        checkOffset(piNeg, 3.5 * Math.PI, -Angle.PI_OVER_TWO);

        checkOffset(twoAndAHalfPiPos, 0, -Angle.PI_OVER_TWO);
        checkOffset(twoAndAHalfPiPos, Angle.TWO_PI, -Angle.PI_OVER_TWO);
        checkOffset(twoAndAHalfPiPos, 2.5 * Math.PI, 0);
        checkOffset(twoAndAHalfPiPos, Math.PI, Angle.PI_OVER_TWO);
        checkOffset(twoAndAHalfPiPos, 3.5 * Math.PI, Math.PI);
    }

    @Test
    public void testClassify() {
        // arrange
        final CutAngle zeroPos = CutAngles.createPositiveFacing(0.0, TEST_PRECISION);
        final CutAngle zeroNeg = CutAngles.createNegativeFacing(0.0, TEST_PRECISION);
        final CutAngle negPiPos = CutAngles.createPositiveFacing(-Math.PI, TEST_PRECISION);

        // act/assert
        checkClassify(zeroPos, HyperplaneLocation.ON,
                0, 1e-16, -1e-16,
                Angle.TWO_PI - 1e-11, Angle.TWO_PI + 1e-11);
        checkClassify(zeroPos, HyperplaneLocation.PLUS,
                0.5, 2.5 * Math.PI,
                -0.5, -Angle.PI_OVER_TWO);

        checkClassify(zeroNeg, HyperplaneLocation.ON,
                0, 1e-16, -1e-16,
                Angle.TWO_PI - 1e-11, Angle.TWO_PI + 1e-11);
        checkClassify(zeroNeg, HyperplaneLocation.MINUS,
                0.5, 2.5 * Math.PI,
                -0.5, -Angle.PI_OVER_TWO);

        checkClassify(negPiPos, HyperplaneLocation.ON, Math.PI, Math.PI + 1e-11);
        checkClassify(negPiPos, HyperplaneLocation.MINUS, 0.5, 2.5 * Math.PI,
                0, 1e-11, Angle.TWO_PI, Angle.TWO_PI - 1e-11);
        checkClassify(negPiPos, HyperplaneLocation.PLUS, -0.5, -Angle.PI_OVER_TWO);
    }

    @Test
    public void testContains() {
        // arrange
        final CutAngle pt = CutAngles.createNegativeFacing(Angle.PI_OVER_TWO, TEST_PRECISION);

        // act/assert
        Assertions.assertFalse(pt.contains(Point1S.ZERO));
        Assertions.assertFalse(pt.contains(Point1S.of(Angle.TWO_PI)));

        Assertions.assertFalse(pt.contains(Point1S.of(Math.PI)));
        Assertions.assertFalse(pt.contains(Point1S.of(0.25 * Math.PI)));
        Assertions.assertFalse(pt.contains(Point1S.of(-0.25 * Math.PI)));

        Assertions.assertTrue(pt.contains(Point1S.of(Angle.PI_OVER_TWO)));
        Assertions.assertTrue(pt.contains(Point1S.of(Angle.PI_OVER_TWO + 1e-11)));
        Assertions.assertTrue(pt.contains(Point1S.of(2.5 * Math.PI)));
        Assertions.assertTrue(pt.contains(Point1S.of(-3.5 * Math.PI)));
    }

    @Test
    public void testReverse() {
        // arrange
        final CutAngle pt = CutAngles.createNegativeFacing(Angle.PI_OVER_TWO, TEST_PRECISION);

        // act
        final CutAngle result = pt.reverse();

        // assert
        checkCutAngle(result, Angle.PI_OVER_TWO, true);
        Assertions.assertSame(TEST_PRECISION, result.getPrecision());

        checkCutAngle(result.reverse(), Angle.PI_OVER_TWO, false);
    }

    @Test
    public void testProject() {
        // arrange
        final CutAngle pt = CutAngles.createNegativeFacing(Angle.PI_OVER_TWO, TEST_PRECISION);

        // act/assert
        for (double az = -Angle.TWO_PI; az <= Angle.TWO_PI; az += 0.2) {
            Assertions.assertEquals(Angle.PI_OVER_TWO, pt.project(Point1S.of(az)).getAzimuth(), TEST_EPS);
        }
    }

    @Test
    public void testSimilarOrientation() {
        // arrange
        final CutAngle a = CutAngles.createPositiveFacing(0.0, TEST_PRECISION);
        final CutAngle b = CutAngles.createNegativeFacing(0.0, TEST_PRECISION);
        final CutAngle c = CutAngles.createPositiveFacing(-Angle.PI_OVER_TWO, TEST_PRECISION);

        // act/assert
        Assertions.assertTrue(a.similarOrientation(a));
        Assertions.assertFalse(a.similarOrientation(b));
        Assertions.assertTrue(a.similarOrientation(c));
    }

    @Test
    public void testTransform_rotate() {
        // arrange
        final Transform1S transform = Transform1S.createRotation(Angle.PI_OVER_TWO);

        // act
        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION).transform(transform),
                Angle.PI_OVER_TWO, true);
        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.ZERO, false, TEST_PRECISION).transform(transform),
                Angle.PI_OVER_TWO, false);

        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.of(1.5 * Math.PI), true, TEST_PRECISION).transform(transform),
                Angle.TWO_PI, true);
        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.of(-Angle.PI_OVER_TWO), false, TEST_PRECISION).transform(transform),
                0.0, false);
    }

    @Test
    public void testTransform_negate() {
        // arrange
        final Transform1S transform = Transform1S.createNegation();

        // act
        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION).transform(transform),
                0.0, false);
        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.ZERO, false, TEST_PRECISION).transform(transform),
                0.0, true);

        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.of(1.5 * Math.PI), true, TEST_PRECISION).transform(transform),
                -1.5 * Math.PI, false);
        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.of(-Angle.PI_OVER_TWO), false, TEST_PRECISION).transform(transform),
                Angle.PI_OVER_TWO, true);
    }

    @Test
    public void testSpan() {
        // arrange
        final CutAngle pt = CutAngles.fromPointAndDirection(Point1S.of(1.0), false, TEST_PRECISION);

        // act
        final HyperplaneConvexSubset<Point1S> result = pt.span();

        // assert
        Assertions.assertSame(pt, result.getHyperplane());
    }

    @Test
    public void testEq() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-3);

        final CutAngle a = CutAngles.fromPointAndDirection(Point1S.ZERO, true, precision);

        final CutAngle b = CutAngles.fromPointAndDirection(Point1S.PI, true, precision);
        final CutAngle c = CutAngles.fromPointAndDirection(Point1S.ZERO, false, precision);
        final CutAngle d = CutAngles.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION);

        final CutAngle e = CutAngles.fromPointAndDirection(Point1S.ZERO, true, precision);
        final CutAngle f = CutAngles.fromPointAndDirection(Point1S.of(Angle.TWO_PI), true, precision);
        final CutAngle g = CutAngles.fromPointAndDirection(Point1S.of(1e-4), true, precision);
        final CutAngle h = CutAngles.fromPointAndDirection(Point1S.of(-1e-4), true, precision);

        // act/assert
        Assertions.assertTrue(a.eq(a, precision));

        Assertions.assertFalse(a.eq(b, precision));
        Assertions.assertFalse(a.eq(c, precision));

        Assertions.assertTrue(a.eq(d, precision));
        Assertions.assertTrue(a.eq(e, precision));
        Assertions.assertTrue(a.eq(f, precision));
        Assertions.assertTrue(a.eq(g, precision));
        Assertions.assertTrue(a.eq(h, precision));
    }

    @Test
    public void testHashCode() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-3);

        final CutAngle a = CutAngles.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION);
        final CutAngle b = CutAngles.fromPointAndDirection(Point1S.PI, true, TEST_PRECISION);
        final CutAngle c = CutAngles.fromPointAndDirection(Point1S.ZERO, false, TEST_PRECISION);
        final CutAngle d = CutAngles.fromPointAndDirection(Point1S.ZERO, true, precision);
        final CutAngle e = CutAngles.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION);

        final int hash = a.hashCode();

        // act/assert
        Assertions.assertEquals(hash, a.hashCode());

        Assertions.assertNotEquals(hash, b.hashCode());
        Assertions.assertNotEquals(hash, c.hashCode());
        Assertions.assertNotEquals(hash, d.hashCode());

        Assertions.assertEquals(hash, e.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-3);

        final CutAngle a = CutAngles.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION);
        final CutAngle b = CutAngles.fromPointAndDirection(Point1S.PI, true, TEST_PRECISION);
        final CutAngle c = CutAngles.fromPointAndDirection(Point1S.ZERO, false, TEST_PRECISION);
        final CutAngle d = CutAngles.fromPointAndDirection(Point1S.ZERO, true, precision);
        final CutAngle e = CutAngles.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertSimpleEqualsCases(a);

        Assertions.assertNotEquals(a, b);
        Assertions.assertNotEquals(a, c);
        Assertions.assertNotEquals(a, d);

        Assertions.assertEquals(a, e);
    }

    @Test
    public void testToString() {
        // arrange
        final CutAngle pt = CutAngles.createPositiveFacing(0.0, TEST_PRECISION);

        // act
        final String str = pt.toString();

        // assert
        Assertions.assertTrue(str.startsWith("CutAngle["));
        Assertions.assertTrue(str.contains("point= ") && str.contains("positiveFacing= "));
    }

    @Test
    public void testSubset_split() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-3);

        final CutAngle pt = CutAngles.createPositiveFacing(-1.5, precision);
        final HyperplaneConvexSubset<Point1S> sub = pt.span();

        // act/assert
        checkSplit(sub, CutAngles.createPositiveFacing(1.0, precision), false, true);
        checkSplit(sub, CutAngles.createPositiveFacing(-1.5 + 1e-2, precision), true, false);

        checkSplit(sub, CutAngles.createNegativeFacing(1.0, precision), true, false);
        checkSplit(sub, CutAngles.createNegativeFacing(-1.5 + 1e-2, precision), false, true);

        checkSplit(sub, CutAngles.createNegativeFacing(-1.5, precision), false, false);
        checkSplit(sub, CutAngles.createNegativeFacing(-1.5 + 1e-4, precision), false, false);
        checkSplit(sub, CutAngles.createNegativeFacing(-1.5 - 1e-4, precision), false, false);
    }

    private void checkSplit(final HyperplaneConvexSubset<Point1S> sub, final CutAngle splitter, final boolean minus, final boolean plus) {
        final Split<? extends HyperplaneConvexSubset<Point1S>> split = sub.split(splitter);

        Assertions.assertSame(minus ? sub : null, split.getMinus());
        Assertions.assertSame(plus ? sub : null, split.getPlus());
    }

    @Test
    public void testSubset_simpleMethods() {
        // arrange
        final CutAngle pt = CutAngles.createPositiveFacing(1, TEST_PRECISION);
        final HyperplaneConvexSubset<Point1S> sub = pt.span();

        // act/assert
        Assertions.assertSame(pt, sub.getHyperplane());
        Assertions.assertFalse(sub.isFull());
        Assertions.assertFalse(sub.isEmpty());
        Assertions.assertFalse(sub.isInfinite());
        Assertions.assertTrue(sub.isFinite());
        Assertions.assertEquals(0.0, sub.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(1), sub.getCentroid(), TEST_EPS);

        final List<? extends HyperplaneConvexSubset<Point1S>> list = sub.toConvex();
        Assertions.assertEquals(1, list.size());
        Assertions.assertSame(sub, list.get(0));
    }

    @Test
    public void testSubset_classify() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-1);
        final CutAngle pt = CutAngles.createPositiveFacing(1, precision);
        final HyperplaneConvexSubset<Point1S> sub = pt.span();

        // act/assert
        Assertions.assertEquals(RegionLocation.BOUNDARY, sub.classify(Point1S.of(0.95)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, sub.classify(Point1S.of(1)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, sub.classify(Point1S.of(1.05)));

        Assertions.assertEquals(RegionLocation.OUTSIDE, sub.classify(Point1S.of(1.11)));
        Assertions.assertEquals(RegionLocation.OUTSIDE, sub.classify(Point1S.of(0.89)));

        Assertions.assertEquals(RegionLocation.OUTSIDE, sub.classify(Point1S.of(-3)));
        Assertions.assertEquals(RegionLocation.OUTSIDE, sub.classify(Point1S.of(10)));
    }

    @Test
    public void testSubset_contains() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-1);
        final CutAngle pt = CutAngles.createPositiveFacing(1, precision);
        final HyperplaneConvexSubset<Point1S> sub = pt.span();

        // act/assert
        Assertions.assertTrue(sub.contains(Point1S.of(0.95)));
        Assertions.assertTrue(sub.contains(Point1S.of(1)));
        Assertions.assertTrue(sub.contains(Point1S.of(1.05)));

        Assertions.assertFalse(sub.contains(Point1S.of(1.11)));
        Assertions.assertFalse(sub.contains(Point1S.of(0.89)));

        Assertions.assertFalse(sub.contains(Point1S.of(-3)));
        Assertions.assertFalse(sub.contains(Point1S.of(10)));
    }

    @Test
    public void testSubset_closestContained() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-1);
        final CutAngle pt = CutAngles.createPositiveFacing(1, precision);
        final HyperplaneConvexSubset<Point1S> sub = pt.span();

        final Point1S expected = Point1S.of(1);

        // act/assert
        Assertions.assertEquals(expected, sub.closest(Point1S.ZERO));
        Assertions.assertEquals(expected, sub.closest(Point1S.of(Angle.PI_OVER_TWO)));
        Assertions.assertEquals(expected, sub.closest(Point1S.PI));
        Assertions.assertEquals(expected, sub.closest(Point1S.of(-Angle.PI_OVER_TWO)));
        Assertions.assertEquals(expected, sub.closest(Point1S.of(Angle.TWO_PI)));
    }

    @Test
    public void testSubset_transform() {
        // arrange
        final CutAngle pt = CutAngles.fromPointAndDirection(Point1S.of(Angle.PI_OVER_TWO), true, TEST_PRECISION);

        final Transform1S transform = Transform1S.createNegation().rotate(Math.PI);

        // act
        final HyperplaneConvexSubset<Point1S> result = pt.span().transform(transform);

        // assert
        checkCutAngle((CutAngle) result.getHyperplane(), Angle.PI_OVER_TWO, false);
    }

    @Test
    public void testSubset_reverse() {
        // arrange
        final CutAngle pt = CutAngles.createPositiveFacing(2.0, TEST_PRECISION);
        final HyperplaneConvexSubset<Point1S> sub = pt.span();

        // act
        final HyperplaneConvexSubset<Point1S> result = sub.reverse();

        // assert
        Assertions.assertEquals(2.0, ((CutAngle) result.getHyperplane()).getAzimuth(), TEST_EPS);
        Assertions.assertFalse(((CutAngle) result.getHyperplane()).isPositiveFacing());

        Assertions.assertEquals(sub.getHyperplane(), result.reverse().getHyperplane());
    }

    @Test
    public void testSubset_toString() {
        // arrange
        final CutAngle pt = CutAngles.createPositiveFacing(2, TEST_PRECISION);
        final HyperplaneConvexSubset<Point1S> sub = pt.span();

        // act
        final String str = sub.toString();

        //assert
        Assertions.assertTrue(str.contains("CutAngleConvexSubset["));
        Assertions.assertTrue(str.contains("point= "));
        Assertions.assertTrue(str.contains("positiveFacing= "));
    }

    private static void checkCutAngle(final CutAngle angle, final double az, final boolean positiveFacing) {
        checkCutAngle(angle, az, positiveFacing, TEST_PRECISION);
    }

    private static void checkCutAngle(final CutAngle angle, final double az, final boolean positiveFacing, final Precision.DoubleEquivalence precision) {
        Assertions.assertEquals(az, angle.getAzimuth(), TEST_EPS);
        Assertions.assertEquals(Angle.Rad.WITHIN_0_AND_2PI.applyAsDouble(az), angle.getNormalizedAzimuth(), TEST_EPS);
        Assertions.assertEquals(az, angle.getPoint().getAzimuth(), TEST_EPS);
        Assertions.assertEquals(positiveFacing, angle.isPositiveFacing());

        Assertions.assertSame(precision, angle.getPrecision());
    }

    private static void checkOffset(final CutAngle pt, final double az, final double offset) {
        Assertions.assertEquals(offset, pt.offset(Point1S.of(az)), TEST_EPS);
    }

    private static void checkClassify(final CutAngle pt, final HyperplaneLocation loc, final double... azimuths) {
        for (final double az : azimuths) {
            Assertions.assertEquals(loc, pt.classify(Point1S.of(az)), "Unexpected location for azimuth " + az);
        }
    }
}
