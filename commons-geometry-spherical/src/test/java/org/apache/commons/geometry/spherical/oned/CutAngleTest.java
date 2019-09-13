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

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.core.partitioning.SubHyperplane.Builder;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.spherical.oned.CutAngle.SubCutAngle;
import org.apache.commons.geometry.spherical.oned.CutAngle.SubCutAngleBuilder;
import org.junit.Assert;
import org.junit.Test;

public class CutAngleTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromAzimuthAndDirection() {
        // act/assert
        checkPoint(CutAngle.fromAzimuthAndDirection(Geometry.ZERO_PI, true, TEST_PRECISION),
                Geometry.ZERO_PI, true);
        checkPoint(CutAngle.fromAzimuthAndDirection(Geometry.PI, true, TEST_PRECISION),
                Geometry.PI, true);
        checkPoint(CutAngle.fromAzimuthAndDirection(Geometry.MINUS_HALF_PI, true, TEST_PRECISION),
                Geometry.MINUS_HALF_PI, true);

        checkPoint(CutAngle.fromAzimuthAndDirection(Geometry.ZERO_PI, false, TEST_PRECISION),
                Geometry.ZERO_PI, false);
        checkPoint(CutAngle.fromAzimuthAndDirection(Geometry.PI, false, TEST_PRECISION),
                Geometry.PI, false);
        checkPoint(CutAngle.fromAzimuthAndDirection(Geometry.MINUS_HALF_PI, false, TEST_PRECISION),
                Geometry.MINUS_HALF_PI, false);
    }

    @Test
    public void testFromPointAndDirection() {
        // arrange
        Point1S pt = Point1S.of(Geometry.MINUS_HALF_PI);

        // act/assert
        checkPoint(CutAngle.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION),
                Geometry.ZERO_PI, true);
        checkPoint(CutAngle.fromPointAndDirection(Point1S.PI, true, TEST_PRECISION),
                Geometry.PI, true);
        checkPoint(CutAngle.fromPointAndDirection(pt, true, TEST_PRECISION),
                Geometry.MINUS_HALF_PI, true);

        checkPoint(CutAngle.fromPointAndDirection(Point1S.ZERO, false, TEST_PRECISION),
                Geometry.ZERO_PI, false);
        checkPoint(CutAngle.fromPointAndDirection(Point1S.PI, false, TEST_PRECISION),
                Geometry.PI, false);
        checkPoint(CutAngle.fromPointAndDirection(pt, false, TEST_PRECISION),
                Geometry.MINUS_HALF_PI, false);
    }

    @Test
    public void testCreatePositiveFacing() {
        // act/assert
        checkPoint(CutAngle.createPositiveFacing(Point1S.ZERO, TEST_PRECISION),
                Geometry.ZERO_PI, true);
        checkPoint(CutAngle.createPositiveFacing(Point1S.PI, TEST_PRECISION),
                Geometry.PI, true);
        checkPoint(CutAngle.createPositiveFacing(Geometry.MINUS_HALF_PI, TEST_PRECISION),
                Geometry.MINUS_HALF_PI, true);
    }

    @Test
    public void testCreateNegativeFacing() {
        // act/assert
        checkPoint(CutAngle.createNegativeFacing(Point1S.ZERO, TEST_PRECISION),
                Geometry.ZERO_PI, false);
        checkPoint(CutAngle.createNegativeFacing(Point1S.PI, TEST_PRECISION),
                Geometry.PI, false);
        checkPoint(CutAngle.createNegativeFacing(Geometry.MINUS_HALF_PI, TEST_PRECISION),
                Geometry.MINUS_HALF_PI, false);
    }

    @Test
    public void testOffset() {
        // arrange
        CutAngle zeroPos = CutAngle.createPositiveFacing(Geometry.ZERO_PI, TEST_PRECISION);
        CutAngle zeroNeg = CutAngle.createNegativeFacing(Geometry.ZERO_PI, TEST_PRECISION);
        CutAngle negPiPos = CutAngle.createPositiveFacing(-Geometry.PI, TEST_PRECISION);

        CutAngle piNeg = CutAngle.createNegativeFacing(Geometry.PI, TEST_PRECISION);
        CutAngle twoAndAHalfPiPos = CutAngle.createPositiveFacing(2.5 * Geometry.PI, TEST_PRECISION);

        // act/assert
        checkOffset(zeroPos, 0, 0);
        checkOffset(zeroPos, Geometry.TWO_PI, 0);
        checkOffset(zeroPos, 2.5 * Geometry.PI, Geometry.HALF_PI);
        checkOffset(zeroPos, Geometry.PI, Geometry.PI);
        checkOffset(zeroPos, 3.5 * Geometry.PI, 1.5 * Geometry.PI);

        checkOffset(zeroNeg, 0, 0);
        checkOffset(zeroNeg, Geometry.TWO_PI, 0);
        checkOffset(zeroNeg, 2.5 * Geometry.PI, Geometry.MINUS_HALF_PI);
        checkOffset(zeroNeg, Geometry.PI, -Geometry.PI);
        checkOffset(zeroNeg, 3.5 * Geometry.PI, -1.5 * Geometry.PI);

        checkOffset(negPiPos, 0, -Geometry.PI);
        checkOffset(negPiPos, Geometry.TWO_PI, -Geometry.PI);
        checkOffset(negPiPos, 2.5 * Geometry.PI, Geometry.MINUS_HALF_PI);
        checkOffset(negPiPos, Geometry.PI, 0);
        checkOffset(negPiPos, 3.5 * Geometry.PI, Geometry.HALF_PI);

        checkOffset(piNeg, 0, Geometry.PI);
        checkOffset(piNeg, Geometry.TWO_PI, Geometry.PI);
        checkOffset(piNeg, 2.5 * Geometry.PI, Geometry.HALF_PI);
        checkOffset(piNeg, Geometry.PI, 0);
        checkOffset(piNeg, 3.5 * Geometry.PI, Geometry.MINUS_HALF_PI);

        checkOffset(twoAndAHalfPiPos, 0, Geometry.MINUS_HALF_PI);
        checkOffset(twoAndAHalfPiPos, Geometry.TWO_PI, Geometry.MINUS_HALF_PI);
        checkOffset(twoAndAHalfPiPos, 2.5 * Geometry.PI, 0);
        checkOffset(twoAndAHalfPiPos, Geometry.PI, Geometry.HALF_PI);
        checkOffset(twoAndAHalfPiPos, 3.5 * Geometry.PI, Geometry.PI);
    }

    @Test
    public void testClassify() {
        // arrange
        CutAngle zeroPos = CutAngle.createPositiveFacing(Geometry.ZERO_PI, TEST_PRECISION);
        CutAngle zeroNeg = CutAngle.createNegativeFacing(Geometry.ZERO_PI, TEST_PRECISION);
        CutAngle negPiPos = CutAngle.createPositiveFacing(-Geometry.PI, TEST_PRECISION);

        // act/assert
        checkClassify(zeroPos, HyperplaneLocation.ON,
                0, 1e-16, -1e-16,
                Geometry.TWO_PI - 1e-11, Geometry.TWO_PI + 1e-11);
        checkClassify(zeroPos, HyperplaneLocation.PLUS,
                0.5, 2.5 * Geometry.PI,
                -0.5, Geometry.MINUS_HALF_PI);

        checkClassify(zeroNeg, HyperplaneLocation.ON,
                0, 1e-16, -1e-16,
                Geometry.TWO_PI - 1e-11, Geometry.TWO_PI + 1e-11);
        checkClassify(zeroNeg, HyperplaneLocation.MINUS,
                0.5, 2.5 * Geometry.PI,
                -0.5, Geometry.MINUS_HALF_PI);

        checkClassify(negPiPos, HyperplaneLocation.ON, Geometry.PI, Geometry.PI + 1e-11);
        checkClassify(negPiPos, HyperplaneLocation.MINUS, 0.5, 2.5 * Geometry.PI,
                0, 1e-11, Geometry.TWO_PI, Geometry.TWO_PI - 1e-11);
        checkClassify(negPiPos, HyperplaneLocation.PLUS, -0.5, Geometry.MINUS_HALF_PI);
    }

    @Test
    public void testContains() {
        // arrange
        CutAngle pt = CutAngle.createNegativeFacing(Geometry.HALF_PI, TEST_PRECISION);

        // act/assert
        Assert.assertFalse(pt.contains(Point1S.ZERO));
        Assert.assertFalse(pt.contains(Point1S.of(Geometry.TWO_PI)));

        Assert.assertFalse(pt.contains(Point1S.of(Geometry.PI)));
        Assert.assertFalse(pt.contains(Point1S.of(0.25 * Geometry.PI)));
        Assert.assertFalse(pt.contains(Point1S.of(-0.25 * Geometry.PI)));

        Assert.assertTrue(pt.contains(Point1S.of(Geometry.HALF_PI)));
        Assert.assertTrue(pt.contains(Point1S.of(Geometry.HALF_PI + 1e-11)));
        Assert.assertTrue(pt.contains(Point1S.of(2.5 * Geometry.PI)));
        Assert.assertTrue(pt.contains(Point1S.of(-3.5 * Geometry.PI)));
    }

    @Test
    public void testReverse() {
        // arrange
        CutAngle pt = CutAngle.createNegativeFacing(Geometry.HALF_PI, TEST_PRECISION);

        // act
        CutAngle result = pt.reverse();

        // assert
        checkPoint(result, Geometry.HALF_PI, true);
        Assert.assertSame(TEST_PRECISION, result.getPrecision());

        checkPoint(result.reverse(), Geometry.HALF_PI, false);
    }

    @Test
    public void testProject() {
        // arrange
        CutAngle pt = CutAngle.createNegativeFacing(Geometry.HALF_PI, TEST_PRECISION);

        // act/assert
        for (double az = -Geometry.TWO_PI; az <= Geometry.TWO_PI; az += 0.2) {
            Assert.assertEquals(Geometry.HALF_PI, pt.project(Point1S.of(az)).getAzimuth(), TEST_EPS);
        }
    }

    @Test
    public void testSimilarOrientation() {
        // arrange
        CutAngle a = CutAngle.createPositiveFacing(Geometry.ZERO_PI, TEST_PRECISION);
        CutAngle b = CutAngle.createNegativeFacing(Geometry.ZERO_PI, TEST_PRECISION);
        CutAngle c = CutAngle.createPositiveFacing(Geometry.MINUS_HALF_PI, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(a.similarOrientation(a));
        Assert.assertFalse(a.similarOrientation(b));
        Assert.assertTrue(a.similarOrientation(c));
    }

    @Test
    public void testTransform_translate() {
        // arrange
        Transform<Point1S> transform = Transform1S.from(p -> Point1S.of(p.getAzimuth() + Geometry.HALF_PI));

        // act
        checkPoint(CutAngle.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION).transform(transform),
                Geometry.HALF_PI, true);
        checkPoint(CutAngle.fromPointAndDirection(Point1S.ZERO, false, TEST_PRECISION).transform(transform),
                Geometry.HALF_PI, false);

        checkPoint(CutAngle.fromPointAndDirection(Point1S.of(1.5 * Geometry.PI), true, TEST_PRECISION).transform(transform),
                Geometry.TWO_PI, true);
        checkPoint(CutAngle.fromPointAndDirection(Point1S.of(Geometry.MINUS_HALF_PI), false, TEST_PRECISION).transform(transform),
                Geometry.ZERO_PI, false);
    }

    @Test
    public void testTransform_scale() {
        // arrange
        Transform<Point1S> transform = Transform1S.from(p -> Point1S.of(p.getAzimuth() * 2));

        // act
        checkPoint(CutAngle.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION).transform(transform),
                Geometry.ZERO_PI, true);
        checkPoint(CutAngle.fromPointAndDirection(Point1S.ZERO, false, TEST_PRECISION).transform(transform),
                Geometry.ZERO_PI, false);

        checkPoint(CutAngle.fromPointAndDirection(Point1S.of(1.5 * Geometry.PI), true, TEST_PRECISION).transform(transform),
                3 * Geometry.PI, true);
        checkPoint(CutAngle.fromPointAndDirection(Point1S.of(Geometry.MINUS_HALF_PI), false, TEST_PRECISION).transform(transform),
                -Geometry.PI, false);
    }

    @Test
    public void testTransform_negate() {
        // arrange
        Transform<Point1S> transform = Transform1S.from(p -> Point1S.of(-p.getAzimuth()));

        // act
        checkPoint(CutAngle.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION).transform(transform),
                Geometry.ZERO_PI, false);
        checkPoint(CutAngle.fromPointAndDirection(Point1S.ZERO, false, TEST_PRECISION).transform(transform),
                Geometry.ZERO_PI, true);

        checkPoint(CutAngle.fromPointAndDirection(Point1S.of(1.5 * Geometry.PI), true, TEST_PRECISION).transform(transform),
                -1.5 * Geometry.PI, false);
        checkPoint(CutAngle.fromPointAndDirection(Point1S.of(Geometry.MINUS_HALF_PI), false, TEST_PRECISION).transform(transform),
                Geometry.HALF_PI, true);
    }

    @Test
    public void testSpan() {
        // arrange
        CutAngle pt = CutAngle.fromPointAndDirection(Point1S.of(1.0), false, TEST_PRECISION);

        // act
        SubCutAngle result = pt.span();

        // assert
        Assert.assertSame(pt, result.getHyperplane());
    }

    @Test
    public void testEq() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        CutAngle a = CutAngle.fromPointAndDirection(Point1S.ZERO, true, precision);

        CutAngle b = CutAngle.fromPointAndDirection(Point1S.PI, true, precision);
        CutAngle c = CutAngle.fromPointAndDirection(Point1S.ZERO, false, precision);
        CutAngle d = CutAngle.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION);

        CutAngle e = CutAngle.fromPointAndDirection(Point1S.ZERO, true, precision);
        CutAngle f = CutAngle.fromPointAndDirection(Point1S.of(Geometry.TWO_PI), true, precision);
        CutAngle g = CutAngle.fromPointAndDirection(Point1S.of(1e-4), true, precision);
        CutAngle h = CutAngle.fromPointAndDirection(Point1S.of(-1e-4), true, precision);

        // act/assert
        Assert.assertTrue(a.eq(a));

        Assert.assertFalse(a.eq(b));
        Assert.assertFalse(a.eq(c));
        Assert.assertFalse(a.eq(d));

        Assert.assertTrue(a.eq(e));
        Assert.assertTrue(a.eq(f));
        Assert.assertTrue(a.eq(g));
        Assert.assertTrue(a.eq(h));
    }

    @Test
    public void testHashCode() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        CutAngle a = CutAngle.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION);
        CutAngle b = CutAngle.fromPointAndDirection(Point1S.PI, true, TEST_PRECISION);
        CutAngle c = CutAngle.fromPointAndDirection(Point1S.ZERO, false, TEST_PRECISION);
        CutAngle d = CutAngle.fromPointAndDirection(Point1S.ZERO, true, precision);
        CutAngle e = CutAngle.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION);

        int hash = a.hashCode();

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
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        CutAngle a = CutAngle.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION);
        CutAngle b = CutAngle.fromPointAndDirection(Point1S.PI, true, TEST_PRECISION);
        CutAngle c = CutAngle.fromPointAndDirection(Point1S.ZERO, false, TEST_PRECISION);
        CutAngle d = CutAngle.fromPointAndDirection(Point1S.ZERO, true, precision);
        CutAngle e = CutAngle.fromPointAndDirection(Point1S.ZERO, true, TEST_PRECISION);

        // act/assert
        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        Assert.assertTrue(a.equals(a));

        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(a.equals(c));
        Assert.assertFalse(a.equals(d));

        Assert.assertTrue(a.equals(e));
    }

    @Test
    public void testToString() {
        // arrange
        CutAngle pt = CutAngle.createPositiveFacing(Geometry.ZERO_PI, TEST_PRECISION);

        // act
        String str = pt.toString();

        // assert
        Assert.assertTrue(str.startsWith("CutAngle["));
        Assert.assertTrue(str.contains("point= ") && str.contains("positiveFacing= "));
    }

    @Test
    public void testSubHyperplane_split() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        CutAngle pt = CutAngle.createPositiveFacing(-1.5, precision);
        SubCutAngle sub = pt.span();

        // act/assert
        checkSplit(sub, CutAngle.createPositiveFacing(1.0, precision), false, true);
        checkSplit(sub, CutAngle.createPositiveFacing(-1.5 + 1e-2, precision), true, false);

        checkSplit(sub, CutAngle.createNegativeFacing(1.0, precision), true, false);
        checkSplit(sub, CutAngle.createNegativeFacing(-1.5 + 1e-2, precision), false, true);

        checkSplit(sub, CutAngle.createNegativeFacing(-1.5, precision), false, false);
        checkSplit(sub, CutAngle.createNegativeFacing(-1.5 + 1e-4, precision), false, false);
        checkSplit(sub, CutAngle.createNegativeFacing(-1.5 - 1e-4, precision), false, false);
    }

    private void checkSplit(SubCutAngle sub, CutAngle splitter, boolean minus, boolean plus) {
        Split<SubCutAngle> split = sub.split(splitter);

        Assert.assertSame(minus ? sub : null, split.getMinus());
        Assert.assertSame(plus ? sub : null, split.getPlus());
    }

    @Test
    public void testSubHyperplane_simpleMethods() {
        // arrange
        CutAngle pt = CutAngle.createPositiveFacing(0, TEST_PRECISION);
        SubCutAngle sub = pt.span();

        // act/assert
        Assert.assertSame(pt, sub.getHyperplane());
        Assert.assertFalse(sub.isFull());
        Assert.assertFalse(sub.isEmpty());
        Assert.assertFalse(sub.isInfinite());
        Assert.assertTrue(sub.isFinite());
        Assert.assertEquals(0.0, sub.getSize(), TEST_EPS);

        List<SubCutAngle> list = sub.toConvex();
        Assert.assertEquals(1, list.size());
        Assert.assertSame(sub, list.get(0));
    }

    @Test
    public void testSubHyperplane_classify() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-1);
        CutAngle pt = CutAngle.createPositiveFacing(1, precision);
        SubCutAngle sub = pt.span();

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
    public void testSubHyperplane_contains() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-1);
        CutAngle pt = CutAngle.createPositiveFacing(1, precision);
        SubCutAngle sub = pt.span();

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
    public void testSubHyperplane_closestContained() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-1);
        CutAngle pt = CutAngle.createPositiveFacing(1, precision);
        SubCutAngle sub = pt.span();

        Point1S expected = Point1S.of(1);

        // act/assert
        Assert.assertEquals(expected, sub.closest(Point1S.ZERO));
        Assert.assertEquals(expected, sub.closest(Point1S.of(Geometry.HALF_PI)));
        Assert.assertEquals(expected, sub.closest(Point1S.PI));
        Assert.assertEquals(expected, sub.closest(Point1S.of(Geometry.MINUS_HALF_PI)));
        Assert.assertEquals(expected, sub.closest(Point1S.of(Geometry.TWO_PI)));
    }

    @Test
    public void testSubHyperplane_transform() {
        // arrange
        CutAngle pt = CutAngle.fromPointAndDirection(Point1S.of(Geometry.HALF_PI), true, TEST_PRECISION);

        Transform<Point1S> transform = Transform1S.from(p -> Point1S.of(Geometry.PI - p.getAzimuth()));

        // act
        SubCutAngle result = pt.span().transform(transform);

        // assert
        checkPoint(result.getHyperplane(), Geometry.HALF_PI, false);
    }

    @Test
    public void testSubHyperplane_reverse() {
        // arrange
        CutAngle pt = CutAngle.createPositiveFacing(2.0, TEST_PRECISION);
        SubCutAngle sub = pt.span();

        // act
        SubCutAngle result = sub.reverse();

        // assert
        Assert.assertEquals(2.0, result.getHyperplane().getAzimuth(), TEST_EPS);
        Assert.assertFalse(result.getHyperplane().isPositiveFacing());

        Assert.assertEquals(sub.getHyperplane(), result.reverse().getHyperplane());
    }

    @Test
    public void testSubHyperplane_toString() {
        // arrange
        CutAngle pt = CutAngle.createPositiveFacing(2, TEST_PRECISION);
        SubCutAngle sub = pt.span();

        // act
        String str = sub.toString();

        //assert
        Assert.assertTrue(str.contains("SubCutAngle["));
        Assert.assertTrue(str.contains("point= "));
        Assert.assertTrue(str.contains("positiveFacing= "));
    }

    @Test
    public void testBuilder() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        CutAngle pt = CutAngle.createPositiveFacing(0, precision);
        SubCutAngle sub = pt.span();

        // act
        Builder<Point1S> builder = sub.builder();

        builder.add(sub);
        builder.add(CutAngle.createPositiveFacing(1e-4, precision).span());
        builder.add((SubHyperplane<Point1S>) sub);

        SubHyperplane<Point1S> result = builder.build();

        // assert
        Assert.assertSame(sub, result);
    }

    @Test
    public void testBuilder_invalidArgs() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        CutAngle pt = CutAngle.createPositiveFacing(0, precision);
        SubCutAngle sub = pt.span();

        Builder<Point1S> builder = sub.builder();

        // act/assert
        GeometryTestUtils.assertThrows(
                () -> builder.add(CutAngle.createPositiveFacing(2e-3, precision).span()),
                IllegalArgumentException.class);
        GeometryTestUtils.assertThrows(
                () -> builder.add(CutAngle.createNegativeFacing(2e-3, precision).span()),
                IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(
                () -> builder.add((SubHyperplane<Point1S>) CutAngle.createPositiveFacing(2e-3, precision).span()),
                IllegalArgumentException.class);
    }

    @Test
    public void testBuilder_toString() {
        // arrange
        CutAngle pt = CutAngle.createPositiveFacing(2, TEST_PRECISION);
        SubCutAngleBuilder builder = pt.span().builder();

        // act
        String str = builder.toString();

        //assert
        Assert.assertTrue(str.contains("SubCutAngleBuilder["));
        Assert.assertTrue(str.contains("base= SubCutAngle["));
        Assert.assertTrue(str.contains("point= "));
        Assert.assertTrue(str.contains("positiveFacing= "));
    }

    private static void checkPoint(CutAngle pt, double az, boolean positiveFacing) {
        checkPoint(pt, az, positiveFacing, TEST_PRECISION);
    }

    private static void checkPoint(CutAngle pt, double az, boolean positiveFacing, DoublePrecisionContext precision) {
        Assert.assertEquals(az, pt.getAzimuth(), TEST_EPS);
        Assert.assertEquals(az, pt.getPoint().getAzimuth(), TEST_EPS);
        Assert.assertEquals(positiveFacing, pt.isPositiveFacing());

        Assert.assertSame(precision, pt.getPrecision());
    }

    private static void checkOffset(CutAngle pt, double az, double offset) {
        Assert.assertEquals(offset, pt.offset(Point1S.of(az)), TEST_EPS);
    }

    private static void checkClassify(CutAngle pt, HyperplaneLocation loc, double ... azimuths) {
        for (double az : azimuths) {
            Assert.assertEquals("Unexpected location for azimuth " + az, loc, pt.classify(Point1S.of(az)));
        }
    }
}
