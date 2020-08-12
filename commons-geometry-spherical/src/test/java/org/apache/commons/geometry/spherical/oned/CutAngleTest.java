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
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class CutAngleTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromAzimuthAndDirection() {
        // act/assert
        checkCutAngle(CutAngles.fromAzimuthAndDirection(0.0, true, TEST_PRECISION),
                0.0, true);
        checkCutAngle(CutAngles.fromAzimuthAndDirection(PlaneAngleRadians.PI, true, TEST_PRECISION),
                PlaneAngleRadians.PI, true);
        checkCutAngle(CutAngles.fromAzimuthAndDirection(-PlaneAngleRadians.PI_OVER_TWO, true, TEST_PRECISION),
                -PlaneAngleRadians.PI_OVER_TWO, true);

        checkCutAngle(CutAngles.fromAzimuthAndDirection(0.0, false, TEST_PRECISION),
                0.0, false);
        checkCutAngle(CutAngles.fromAzimuthAndDirection(PlaneAngleRadians.PI, false, TEST_PRECISION),
                PlaneAngleRadians.PI, false);
        checkCutAngle(CutAngles.fromAzimuthAndDirection(-PlaneAngleRadians.PI_OVER_TWO, false, TEST_PRECISION),
                -PlaneAngleRadians.PI_OVER_TWO, false);
    }

    @Test
    public void testFromPointAndDirection() {
        // arrange
        final Point1S pt = Point1S.of(-PlaneAngleRadians.PI_OVER_TWO);

        // act/assert
        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION),
                0.0, true);
        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.PI, true, TEST_PRECISION),
                PlaneAngleRadians.PI, true);
        checkCutAngle(CutAngles.fromPointAndDirection(pt, true, TEST_PRECISION),
                -PlaneAngleRadians.PI_OVER_TWO, true);

        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.ZERO, false, TEST_PRECISION),
                0.0, false);
        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.PI, false, TEST_PRECISION),
                PlaneAngleRadians.PI, false);
        checkCutAngle(CutAngles.fromPointAndDirection(pt, false, TEST_PRECISION),
                -PlaneAngleRadians.PI_OVER_TWO, false);
    }

    @Test
    public void testCreatePositiveFacing() {
        // act/assert
        checkCutAngle(CutAngles.createPositiveFacing(Point1S.ZERO, TEST_PRECISION),
                0.0, true);
        checkCutAngle(CutAngles.createPositiveFacing(Point1S.PI, TEST_PRECISION),
                PlaneAngleRadians.PI, true);
        checkCutAngle(CutAngles.createPositiveFacing(-PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION),
                -PlaneAngleRadians.PI_OVER_TWO, true);
    }

    @Test
    public void testCreateNegativeFacing() {
        // act/assert
        checkCutAngle(CutAngles.createNegativeFacing(Point1S.ZERO, TEST_PRECISION),
                0.0, false);
        checkCutAngle(CutAngles.createNegativeFacing(Point1S.PI, TEST_PRECISION),
                PlaneAngleRadians.PI, false);
        checkCutAngle(CutAngles.createNegativeFacing(-PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION),
                -PlaneAngleRadians.PI_OVER_TWO, false);
    }

    @Test
    public void testOffset() {
        // arrange
        final CutAngle zeroPos = CutAngles.createPositiveFacing(0.0, TEST_PRECISION);
        final CutAngle zeroNeg = CutAngles.createNegativeFacing(0.0, TEST_PRECISION);
        final CutAngle negPiPos = CutAngles.createPositiveFacing(-PlaneAngleRadians.PI, TEST_PRECISION);

        final CutAngle piNeg = CutAngles.createNegativeFacing(PlaneAngleRadians.PI, TEST_PRECISION);
        final CutAngle twoAndAHalfPiPos = CutAngles.createPositiveFacing(2.5 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act/assert
        checkOffset(zeroPos, 0, 0);
        checkOffset(zeroPos, PlaneAngleRadians.TWO_PI, 0);
        checkOffset(zeroPos, 2.5 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO);
        checkOffset(zeroPos, PlaneAngleRadians.PI, PlaneAngleRadians.PI);
        checkOffset(zeroPos, 3.5 * PlaneAngleRadians.PI, 1.5 * PlaneAngleRadians.PI);

        checkOffset(zeroNeg, 0, 0);
        checkOffset(zeroNeg, PlaneAngleRadians.TWO_PI, 0);
        checkOffset(zeroNeg, 2.5 * PlaneAngleRadians.PI, -PlaneAngleRadians.PI_OVER_TWO);
        checkOffset(zeroNeg, PlaneAngleRadians.PI, -PlaneAngleRadians.PI);
        checkOffset(zeroNeg, 3.5 * PlaneAngleRadians.PI, -1.5 * PlaneAngleRadians.PI);

        checkOffset(negPiPos, 0, -PlaneAngleRadians.PI);
        checkOffset(negPiPos, PlaneAngleRadians.TWO_PI, -PlaneAngleRadians.PI);
        checkOffset(negPiPos, 2.5 * PlaneAngleRadians.PI, -PlaneAngleRadians.PI_OVER_TWO);
        checkOffset(negPiPos, PlaneAngleRadians.PI, 0);
        checkOffset(negPiPos, 3.5 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO);

        checkOffset(piNeg, 0, PlaneAngleRadians.PI);
        checkOffset(piNeg, PlaneAngleRadians.TWO_PI, PlaneAngleRadians.PI);
        checkOffset(piNeg, 2.5 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO);
        checkOffset(piNeg, PlaneAngleRadians.PI, 0);
        checkOffset(piNeg, 3.5 * PlaneAngleRadians.PI, -PlaneAngleRadians.PI_OVER_TWO);

        checkOffset(twoAndAHalfPiPos, 0, -PlaneAngleRadians.PI_OVER_TWO);
        checkOffset(twoAndAHalfPiPos, PlaneAngleRadians.TWO_PI, -PlaneAngleRadians.PI_OVER_TWO);
        checkOffset(twoAndAHalfPiPos, 2.5 * PlaneAngleRadians.PI, 0);
        checkOffset(twoAndAHalfPiPos, PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO);
        checkOffset(twoAndAHalfPiPos, 3.5 * PlaneAngleRadians.PI, PlaneAngleRadians.PI);
    }

    @Test
    public void testClassify() {
        // arrange
        final CutAngle zeroPos = CutAngles.createPositiveFacing(0.0, TEST_PRECISION);
        final CutAngle zeroNeg = CutAngles.createNegativeFacing(0.0, TEST_PRECISION);
        final CutAngle negPiPos = CutAngles.createPositiveFacing(-PlaneAngleRadians.PI, TEST_PRECISION);

        // act/assert
        checkClassify(zeroPos, HyperplaneLocation.ON,
                0, 1e-16, -1e-16,
                PlaneAngleRadians.TWO_PI - 1e-11, PlaneAngleRadians.TWO_PI + 1e-11);
        checkClassify(zeroPos, HyperplaneLocation.PLUS,
                0.5, 2.5 * PlaneAngleRadians.PI,
                -0.5, -PlaneAngleRadians.PI_OVER_TWO);

        checkClassify(zeroNeg, HyperplaneLocation.ON,
                0, 1e-16, -1e-16,
                PlaneAngleRadians.TWO_PI - 1e-11, PlaneAngleRadians.TWO_PI + 1e-11);
        checkClassify(zeroNeg, HyperplaneLocation.MINUS,
                0.5, 2.5 * PlaneAngleRadians.PI,
                -0.5, -PlaneAngleRadians.PI_OVER_TWO);

        checkClassify(negPiPos, HyperplaneLocation.ON, PlaneAngleRadians.PI, PlaneAngleRadians.PI + 1e-11);
        checkClassify(negPiPos, HyperplaneLocation.MINUS, 0.5, 2.5 * PlaneAngleRadians.PI,
                0, 1e-11, PlaneAngleRadians.TWO_PI, PlaneAngleRadians.TWO_PI - 1e-11);
        checkClassify(negPiPos, HyperplaneLocation.PLUS, -0.5, -PlaneAngleRadians.PI_OVER_TWO);
    }

    @Test
    public void testContains() {
        // arrange
        final CutAngle pt = CutAngles.createNegativeFacing(PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);

        // act/assert
        Assert.assertFalse(pt.contains(Point1S.ZERO));
        Assert.assertFalse(pt.contains(Point1S.of(PlaneAngleRadians.TWO_PI)));

        Assert.assertFalse(pt.contains(Point1S.of(PlaneAngleRadians.PI)));
        Assert.assertFalse(pt.contains(Point1S.of(0.25 * PlaneAngleRadians.PI)));
        Assert.assertFalse(pt.contains(Point1S.of(-0.25 * PlaneAngleRadians.PI)));

        Assert.assertTrue(pt.contains(Point1S.of(PlaneAngleRadians.PI_OVER_TWO)));
        Assert.assertTrue(pt.contains(Point1S.of(PlaneAngleRadians.PI_OVER_TWO + 1e-11)));
        Assert.assertTrue(pt.contains(Point1S.of(2.5 * PlaneAngleRadians.PI)));
        Assert.assertTrue(pt.contains(Point1S.of(-3.5 * PlaneAngleRadians.PI)));
    }

    @Test
    public void testReverse() {
        // arrange
        final CutAngle pt = CutAngles.createNegativeFacing(PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);

        // act
        final CutAngle result = pt.reverse();

        // assert
        checkCutAngle(result, PlaneAngleRadians.PI_OVER_TWO, true);
        Assert.assertSame(TEST_PRECISION, result.getPrecision());

        checkCutAngle(result.reverse(), PlaneAngleRadians.PI_OVER_TWO, false);
    }

    @Test
    public void testProject() {
        // arrange
        final CutAngle pt = CutAngles.createNegativeFacing(PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);

        // act/assert
        for (double az = -PlaneAngleRadians.TWO_PI; az <= PlaneAngleRadians.TWO_PI; az += 0.2) {
            Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, pt.project(Point1S.of(az)).getAzimuth(), TEST_EPS);
        }
    }

    @Test
    public void testSimilarOrientation() {
        // arrange
        final CutAngle a = CutAngles.createPositiveFacing(0.0, TEST_PRECISION);
        final CutAngle b = CutAngles.createNegativeFacing(0.0, TEST_PRECISION);
        final CutAngle c = CutAngles.createPositiveFacing(-PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(a.similarOrientation(a));
        Assert.assertFalse(a.similarOrientation(b));
        Assert.assertTrue(a.similarOrientation(c));
    }

    @Test
    public void testTransform_rotate() {
        // arrange
        final Transform1S transform = Transform1S.createRotation(PlaneAngleRadians.PI_OVER_TWO);

        // act
        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION).transform(transform),
                PlaneAngleRadians.PI_OVER_TWO, true);
        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.ZERO, false, TEST_PRECISION).transform(transform),
                PlaneAngleRadians.PI_OVER_TWO, false);

        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.of(1.5 * PlaneAngleRadians.PI), true, TEST_PRECISION).transform(transform),
                PlaneAngleRadians.TWO_PI, true);
        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.of(-PlaneAngleRadians.PI_OVER_TWO), false, TEST_PRECISION).transform(transform),
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

        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.of(1.5 * PlaneAngleRadians.PI), true, TEST_PRECISION).transform(transform),
                -1.5 * PlaneAngleRadians.PI, false);
        checkCutAngle(CutAngles.fromPointAndDirection(Point1S.of(-PlaneAngleRadians.PI_OVER_TWO), false, TEST_PRECISION).transform(transform),
                PlaneAngleRadians.PI_OVER_TWO, true);
    }

    @Test
    public void testSpan() {
        // arrange
        final CutAngle pt = CutAngles.fromPointAndDirection(Point1S.of(1.0), false, TEST_PRECISION);

        // act
        final HyperplaneConvexSubset<Point1S> result = pt.span();

        // assert
        Assert.assertSame(pt, result.getHyperplane());
    }

    @Test
    public void testEq() {
        // arrange
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        final CutAngle a = CutAngles.fromPointAndDirection(Point1S.ZERO, true, precision);

        final CutAngle b = CutAngles.fromPointAndDirection(Point1S.PI, true, precision);
        final CutAngle c = CutAngles.fromPointAndDirection(Point1S.ZERO, false, precision);
        final CutAngle d = CutAngles.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION);

        final CutAngle e = CutAngles.fromPointAndDirection(Point1S.ZERO, true, precision);
        final CutAngle f = CutAngles.fromPointAndDirection(Point1S.of(PlaneAngleRadians.TWO_PI), true, precision);
        final CutAngle g = CutAngles.fromPointAndDirection(Point1S.of(1e-4), true, precision);
        final CutAngle h = CutAngles.fromPointAndDirection(Point1S.of(-1e-4), true, precision);

        // act/assert
        Assert.assertTrue(a.eq(a, precision));

        Assert.assertFalse(a.eq(b, precision));
        Assert.assertFalse(a.eq(c, precision));

        Assert.assertTrue(a.eq(d, precision));
        Assert.assertTrue(a.eq(e, precision));
        Assert.assertTrue(a.eq(f, precision));
        Assert.assertTrue(a.eq(g, precision));
        Assert.assertTrue(a.eq(h, precision));
    }

    @Test
    public void testHashCode() {
        // arrange
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        final CutAngle a = CutAngles.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION);
        final CutAngle b = CutAngles.fromPointAndDirection(Point1S.PI, true, TEST_PRECISION);
        final CutAngle c = CutAngles.fromPointAndDirection(Point1S.ZERO, false, TEST_PRECISION);
        final CutAngle d = CutAngles.fromPointAndDirection(Point1S.ZERO, true, precision);
        final CutAngle e = CutAngles.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION);

        final int hash = a.hashCode();

        // act/assert
        Assert.assertEquals(hash, a.hashCode());

        Assert.assertNotEquals(hash, b.hashCode());
        Assert.assertNotEquals(hash, c.hashCode());
        Assert.assertNotEquals(hash, d.hashCode());

        Assert.assertEquals(hash, e.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        final CutAngle a = CutAngles.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION);
        final CutAngle b = CutAngles.fromPointAndDirection(Point1S.PI, true, TEST_PRECISION);
        final CutAngle c = CutAngles.fromPointAndDirection(Point1S.ZERO, false, TEST_PRECISION);
        final CutAngle d = CutAngles.fromPointAndDirection(Point1S.ZERO, true, precision);
        final CutAngle e = CutAngles.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertSimpleEqualsCases(a);

        Assert.assertNotEquals(a, b);
        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(a, d);

        Assert.assertEquals(a, e);
    }

    @Test
    public void testToString() {
        // arrange
        final CutAngle pt = CutAngles.createPositiveFacing(0.0, TEST_PRECISION);

        // act
        final String str = pt.toString();

        // assert
        Assert.assertTrue(str.startsWith("CutAngle["));
        Assert.assertTrue(str.contains("point= ") && str.contains("positiveFacing= "));
    }

    @Test
    public void testSubset_split() {
        // arrange
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

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

        Assert.assertSame(minus ? sub : null, split.getMinus());
        Assert.assertSame(plus ? sub : null, split.getPlus());
    }

    @Test
    public void testSubset_simpleMethods() {
        // arrange
        final CutAngle pt = CutAngles.createPositiveFacing(1, TEST_PRECISION);
        final HyperplaneConvexSubset<Point1S> sub = pt.span();

        // act/assert
        Assert.assertSame(pt, sub.getHyperplane());
        Assert.assertFalse(sub.isFull());
        Assert.assertFalse(sub.isEmpty());
        Assert.assertFalse(sub.isInfinite());
        Assert.assertTrue(sub.isFinite());
        Assert.assertEquals(0.0, sub.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(1), sub.getCentroid(), TEST_EPS);

        final List<? extends HyperplaneConvexSubset<Point1S>> list = sub.toConvex();
        Assert.assertEquals(1, list.size());
        Assert.assertSame(sub, list.get(0));
    }

    @Test
    public void testSubset_classify() {
        // arrange
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-1);
        final CutAngle pt = CutAngles.createPositiveFacing(1, precision);
        final HyperplaneConvexSubset<Point1S> sub = pt.span();

        // act/assert
        Assert.assertEquals(RegionLocation.BOUNDARY, sub.classify(Point1S.of(0.95)));
        Assert.assertEquals(RegionLocation.BOUNDARY, sub.classify(Point1S.of(1)));
        Assert.assertEquals(RegionLocation.BOUNDARY, sub.classify(Point1S.of(1.05)));

        Assert.assertEquals(RegionLocation.OUTSIDE, sub.classify(Point1S.of(1.11)));
        Assert.assertEquals(RegionLocation.OUTSIDE, sub.classify(Point1S.of(0.89)));

        Assert.assertEquals(RegionLocation.OUTSIDE, sub.classify(Point1S.of(-3)));
        Assert.assertEquals(RegionLocation.OUTSIDE, sub.classify(Point1S.of(10)));
    }

    @Test
    public void testSubset_contains() {
        // arrange
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-1);
        final CutAngle pt = CutAngles.createPositiveFacing(1, precision);
        final HyperplaneConvexSubset<Point1S> sub = pt.span();

        // act/assert
        Assert.assertTrue(sub.contains(Point1S.of(0.95)));
        Assert.assertTrue(sub.contains(Point1S.of(1)));
        Assert.assertTrue(sub.contains(Point1S.of(1.05)));

        Assert.assertFalse(sub.contains(Point1S.of(1.11)));
        Assert.assertFalse(sub.contains(Point1S.of(0.89)));

        Assert.assertFalse(sub.contains(Point1S.of(-3)));
        Assert.assertFalse(sub.contains(Point1S.of(10)));
    }

    @Test
    public void testSubset_closestContained() {
        // arrange
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-1);
        final CutAngle pt = CutAngles.createPositiveFacing(1, precision);
        final HyperplaneConvexSubset<Point1S> sub = pt.span();

        final Point1S expected = Point1S.of(1);

        // act/assert
        Assert.assertEquals(expected, sub.closest(Point1S.ZERO));
        Assert.assertEquals(expected, sub.closest(Point1S.of(PlaneAngleRadians.PI_OVER_TWO)));
        Assert.assertEquals(expected, sub.closest(Point1S.PI));
        Assert.assertEquals(expected, sub.closest(Point1S.of(-PlaneAngleRadians.PI_OVER_TWO)));
        Assert.assertEquals(expected, sub.closest(Point1S.of(PlaneAngleRadians.TWO_PI)));
    }

    @Test
    public void testSubset_transform() {
        // arrange
        final CutAngle pt = CutAngles.fromPointAndDirection(Point1S.of(PlaneAngleRadians.PI_OVER_TWO), true, TEST_PRECISION);

        final Transform1S transform = Transform1S.createNegation().rotate(PlaneAngleRadians.PI);

        // act
        final HyperplaneConvexSubset<Point1S> result = pt.span().transform(transform);

        // assert
        checkCutAngle((CutAngle) result.getHyperplane(), PlaneAngleRadians.PI_OVER_TWO, false);
    }

    @Test
    public void testSubset_reverse() {
        // arrange
        final CutAngle pt = CutAngles.createPositiveFacing(2.0, TEST_PRECISION);
        final HyperplaneConvexSubset<Point1S> sub = pt.span();

        // act
        final HyperplaneConvexSubset<Point1S> result = sub.reverse();

        // assert
        Assert.assertEquals(2.0, ((CutAngle) result.getHyperplane()).getAzimuth(), TEST_EPS);
        Assert.assertFalse(((CutAngle) result.getHyperplane()).isPositiveFacing());

        Assert.assertEquals(sub.getHyperplane(), result.reverse().getHyperplane());
    }

    @Test
    public void testSubset_toString() {
        // arrange
        final CutAngle pt = CutAngles.createPositiveFacing(2, TEST_PRECISION);
        final HyperplaneConvexSubset<Point1S> sub = pt.span();

        // act
        final String str = sub.toString();

        //assert
        Assert.assertTrue(str.contains("CutAngleConvexSubset["));
        Assert.assertTrue(str.contains("point= "));
        Assert.assertTrue(str.contains("positiveFacing= "));
    }

    private static void checkCutAngle(final CutAngle angle, final double az, final boolean positiveFacing) {
        checkCutAngle(angle, az, positiveFacing, TEST_PRECISION);
    }

    private static void checkCutAngle(final CutAngle angle, final double az, final boolean positiveFacing, final DoublePrecisionContext precision) {
        Assert.assertEquals(az, angle.getAzimuth(), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.normalizeBetweenZeroAndTwoPi(az), angle.getNormalizedAzimuth(), TEST_EPS);
        Assert.assertEquals(az, angle.getPoint().getAzimuth(), TEST_EPS);
        Assert.assertEquals(positiveFacing, angle.isPositiveFacing());

        Assert.assertSame(precision, angle.getPrecision());
    }

    private static void checkOffset(final CutAngle pt, final double az, final double offset) {
        Assert.assertEquals(offset, pt.offset(Point1S.of(az)), TEST_EPS);
    }

    private static void checkClassify(final CutAngle pt, final HyperplaneLocation loc, final double... azimuths) {
        for (final double az : azimuths) {
            Assert.assertEquals("Unexpected location for azimuth " + az, loc, pt.classify(Point1S.of(az)));
        }
    }
}
