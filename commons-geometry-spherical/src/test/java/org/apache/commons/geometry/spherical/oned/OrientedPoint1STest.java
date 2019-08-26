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
import org.apache.commons.geometry.spherical.oned.OrientedPoint1S.SubOrientedPoint1S;
import org.apache.commons.geometry.spherical.oned.OrientedPoint1S.SubOrientedPointBuilder1S;
import org.junit.Assert;
import org.junit.Test;

public class OrientedPoint1STest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromAzimuthAndDirection() {
        // act/assert
        checkPoint(OrientedPoint1S.fromAzimuthAndDirection(Geometry.ZERO_PI, true, TEST_PRECISION),
                Geometry.ZERO_PI, true);
        checkPoint(OrientedPoint1S.fromAzimuthAndDirection(Geometry.PI, true, TEST_PRECISION),
                Geometry.PI, true);
        checkPoint(OrientedPoint1S.fromAzimuthAndDirection(Geometry.MINUS_HALF_PI, true, TEST_PRECISION),
                Geometry.MINUS_HALF_PI, true);

        checkPoint(OrientedPoint1S.fromAzimuthAndDirection(Geometry.ZERO_PI, false, TEST_PRECISION),
                Geometry.ZERO_PI, false);
        checkPoint(OrientedPoint1S.fromAzimuthAndDirection(Geometry.PI, false, TEST_PRECISION),
                Geometry.PI, false);
        checkPoint(OrientedPoint1S.fromAzimuthAndDirection(Geometry.MINUS_HALF_PI, false, TEST_PRECISION),
                Geometry.MINUS_HALF_PI, false);
    }

    @Test
    public void testFromPointAndDirection() {
        // arrange
        Point1S pt = Point1S.of(Geometry.MINUS_HALF_PI);

        // act/assert
        checkPoint(OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, true, TEST_PRECISION),
                Geometry.ZERO_PI, true);
        checkPoint(OrientedPoint1S.fromPointAndDirection(Point1S.PI, true, TEST_PRECISION),
                Geometry.PI, true);
        checkPoint(OrientedPoint1S.fromPointAndDirection(pt, true, TEST_PRECISION),
                Geometry.MINUS_HALF_PI, true);

        checkPoint(OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, false, TEST_PRECISION),
                Geometry.ZERO_PI, false);
        checkPoint(OrientedPoint1S.fromPointAndDirection(Point1S.PI, false, TEST_PRECISION),
                Geometry.PI, false);
        checkPoint(OrientedPoint1S.fromPointAndDirection(pt, false, TEST_PRECISION),
                Geometry.MINUS_HALF_PI, false);
    }

    @Test
    public void testCreatePositiveFacing() {
        // act/assert
        checkPoint(OrientedPoint1S.createPositiveFacing(Point1S.ZERO_PI, TEST_PRECISION),
                Geometry.ZERO_PI, true);
        checkPoint(OrientedPoint1S.createPositiveFacing(Point1S.PI, TEST_PRECISION),
                Geometry.PI, true);
        checkPoint(OrientedPoint1S.createPositiveFacing(Geometry.MINUS_HALF_PI, TEST_PRECISION),
                Geometry.MINUS_HALF_PI, true);
    }

    @Test
    public void testCreateNegativeFacing() {
        // act/assert
        checkPoint(OrientedPoint1S.createNegativeFacing(Point1S.ZERO_PI, TEST_PRECISION),
                Geometry.ZERO_PI, false);
        checkPoint(OrientedPoint1S.createNegativeFacing(Point1S.PI, TEST_PRECISION),
                Geometry.PI, false);
        checkPoint(OrientedPoint1S.createNegativeFacing(Geometry.MINUS_HALF_PI, TEST_PRECISION),
                Geometry.MINUS_HALF_PI, false);
    }

    @Test
    public void testOffset() {
        // arrange
        OrientedPoint1S zeroPos = OrientedPoint1S.createPositiveFacing(Geometry.ZERO_PI, TEST_PRECISION);
        OrientedPoint1S zeroNeg = OrientedPoint1S.createNegativeFacing(Geometry.ZERO_PI, TEST_PRECISION);
        OrientedPoint1S negPiPos = OrientedPoint1S.createPositiveFacing(-Geometry.PI, TEST_PRECISION);

        OrientedPoint1S piNeg = OrientedPoint1S.createNegativeFacing(Geometry.PI, TEST_PRECISION);
        OrientedPoint1S twoAndAHalfPiPos = OrientedPoint1S.createPositiveFacing(2.5 * Geometry.PI, TEST_PRECISION);

        // act/assert
        checkOffset(zeroPos, 0, 0);
        checkOffset(zeroPos, Geometry.TWO_PI, 0);
        checkOffset(zeroPos, 2.5 * Geometry.PI, Geometry.HALF_PI);
        checkOffset(zeroPos, Geometry.PI, -Geometry.PI);
        checkOffset(zeroPos, 3.5 * Geometry.PI, Geometry.MINUS_HALF_PI);

        checkOffset(zeroNeg, 0, 0);
        checkOffset(zeroNeg, Geometry.TWO_PI, 0);
        checkOffset(zeroNeg, 2.5 * Geometry.PI, Geometry.MINUS_HALF_PI);
        checkOffset(zeroNeg, Geometry.PI, -Geometry.PI);
        checkOffset(zeroNeg, 3.5 * Geometry.PI, Geometry.HALF_PI);

        checkOffset(negPiPos, 0, -Geometry.PI);
        checkOffset(negPiPos, Geometry.TWO_PI, -Geometry.PI);
        checkOffset(negPiPos, 2.5 * Geometry.PI, Geometry.MINUS_HALF_PI);
        checkOffset(negPiPos, Geometry.PI, 0);
        checkOffset(negPiPos, 3.5 * Geometry.PI, Geometry.HALF_PI);

        checkOffset(piNeg, 0, -Geometry.PI);
        checkOffset(piNeg, Geometry.TWO_PI, -Geometry.PI);
        checkOffset(piNeg, 2.5 * Geometry.PI, Geometry.HALF_PI);
        checkOffset(piNeg, Geometry.PI, 0);
        checkOffset(piNeg, 3.5 * Geometry.PI, Geometry.MINUS_HALF_PI);

        checkOffset(twoAndAHalfPiPos, 0, Geometry.MINUS_HALF_PI);
        checkOffset(twoAndAHalfPiPos, Geometry.TWO_PI, Geometry.MINUS_HALF_PI);
        checkOffset(twoAndAHalfPiPos, 2.5 * Geometry.PI, 0);
        checkOffset(twoAndAHalfPiPos, Geometry.PI, Geometry.HALF_PI);
        checkOffset(twoAndAHalfPiPos, 3.5 * Geometry.PI, -Geometry.PI);
    }

    @Test
    public void testClassify() {
        // arrange
        OrientedPoint1S zeroPos = OrientedPoint1S.createPositiveFacing(Geometry.ZERO_PI, TEST_PRECISION);
        OrientedPoint1S zeroNeg = OrientedPoint1S.createNegativeFacing(Geometry.ZERO_PI, TEST_PRECISION);
        OrientedPoint1S negPiPos = OrientedPoint1S.createPositiveFacing(-Geometry.PI, TEST_PRECISION);

        // act/assert
        checkClassify(zeroPos, HyperplaneLocation.ON, 0, 1e-16, -1e-16);
        checkClassify(zeroPos, HyperplaneLocation.MINUS, -0.5, Geometry.MINUS_HALF_PI, Geometry.PI);
        checkClassify(zeroPos, HyperplaneLocation.PLUS, 0.5, 2.5 * Geometry.PI);

        checkClassify(zeroNeg, HyperplaneLocation.ON, 0, 1e-16, -1e-16);
        checkClassify(zeroNeg, HyperplaneLocation.PLUS, -0.5, Geometry.MINUS_HALF_PI);
        checkClassify(zeroNeg, HyperplaneLocation.MINUS, 0.5, 2.5 * Geometry.PI, Geometry.PI);

        checkClassify(negPiPos, HyperplaneLocation.ON, Geometry.PI, Geometry.PI + 1e-11);
        checkClassify(negPiPos, HyperplaneLocation.MINUS, 0.5, 0, 2.5 * Geometry.PI);
        checkClassify(negPiPos, HyperplaneLocation.PLUS, -0.5, Geometry.MINUS_HALF_PI);
    }

    @Test
    public void testPlusMinuOnPoint() {
        // arrange
        DoublePrecisionContext low = new EpsilonDoublePrecisionContext(1.1);
        DoublePrecisionContext high = new EpsilonDoublePrecisionContext(1e-10);

        OrientedPoint1S a = OrientedPoint1S.createNegativeFacing(Geometry.HALF_PI, low);
        OrientedPoint1S b = OrientedPoint1S.createNegativeFacing(Geometry.HALF_PI, high);

        // act/assert
        checkClassify(a, HyperplaneLocation.ON, a.onPoint());
        checkClassify(b, HyperplaneLocation.ON, b.onPoint());

        checkClassify(a, HyperplaneLocation.PLUS, a.plusPoint());
        checkClassify(b, HyperplaneLocation.PLUS, b.plusPoint());

        checkClassify(a, HyperplaneLocation.MINUS, a.minusPoint());
        checkClassify(b, HyperplaneLocation.MINUS, b.minusPoint());
    }

    @Test
    public void testContains() {
        // arrange
        OrientedPoint1S pt = OrientedPoint1S.createNegativeFacing(Geometry.HALF_PI, TEST_PRECISION);

        // act/assert
        Assert.assertFalse(pt.contains(Point1S.ZERO_PI));
        Assert.assertFalse(pt.contains(Point1S.of(Geometry.PI)));
        Assert.assertFalse(pt.contains(Point1S.of(Geometry.TWO_PI)));

        Assert.assertTrue(pt.contains(Point1S.of(Geometry.HALF_PI)));
        Assert.assertTrue(pt.contains(Point1S.of(Geometry.HALF_PI + 1e-11)));
        Assert.assertTrue(pt.contains(Point1S.of(2.5 * Geometry.PI)));
        Assert.assertTrue(pt.contains(Point1S.of(-3.5 * Geometry.PI)));
    }

    @Test
    public void testReverse() {
        // arrange
        OrientedPoint1S pt = OrientedPoint1S.createNegativeFacing(Geometry.HALF_PI, TEST_PRECISION);

        // act
        OrientedPoint1S result = pt.reverse();

        // assert
        checkPoint(result, Geometry.HALF_PI, true);
        Assert.assertSame(TEST_PRECISION, result.getPrecision());

        checkPoint(result.reverse(), Geometry.HALF_PI, false);
    }

    @Test
    public void testProject() {
        // arrange
        OrientedPoint1S pt = OrientedPoint1S.createNegativeFacing(Geometry.HALF_PI, TEST_PRECISION);

        // act/assert
        for (double az = -Geometry.TWO_PI; az <= Geometry.TWO_PI; az += 0.2) {
            Assert.assertEquals(Geometry.HALF_PI, pt.project(Point1S.of(az)).getAzimuth(), TEST_EPS);
        }
    }

    @Test
    public void testSimilarOrientation() {
        // arrange
        OrientedPoint1S a = OrientedPoint1S.createPositiveFacing(Geometry.ZERO_PI, TEST_PRECISION);
        OrientedPoint1S b = OrientedPoint1S.createNegativeFacing(Geometry.ZERO_PI, TEST_PRECISION);
        OrientedPoint1S c = OrientedPoint1S.createPositiveFacing(Geometry.MINUS_HALF_PI, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(a.similarOrientation(a));
        Assert.assertFalse(a.similarOrientation(b));
        Assert.assertTrue(a.similarOrientation(c));
    }

    @Test
    public void testTransform_translate() {
        // arrange
        Transform<Point1S> transform = p -> Point1S.of(p.getAzimuth() + Geometry.HALF_PI);

        // act
        checkPoint(OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, true, TEST_PRECISION).transform(transform),
                Geometry.HALF_PI, true);
        checkPoint(OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, false, TEST_PRECISION).transform(transform),
                Geometry.HALF_PI, false);

        checkPoint(OrientedPoint1S.fromPointAndDirection(Point1S.of(1.5 * Geometry.PI), true, TEST_PRECISION).transform(transform),
                Geometry.TWO_PI, true);
        checkPoint(OrientedPoint1S.fromPointAndDirection(Point1S.of(Geometry.MINUS_HALF_PI), false, TEST_PRECISION).transform(transform),
                Geometry.ZERO_PI, false);
    }

    @Test
    public void testTransform_scale() {
        // arrange
        Transform<Point1S> transform = p -> Point1S.of(p.getAzimuth() * 2);

        // act
        checkPoint(OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, true, TEST_PRECISION).transform(transform),
                Geometry.ZERO_PI, true);
        checkPoint(OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, false, TEST_PRECISION).transform(transform),
                Geometry.ZERO_PI, false);

        checkPoint(OrientedPoint1S.fromPointAndDirection(Point1S.of(1.5 * Geometry.PI), true, TEST_PRECISION).transform(transform),
                3 * Geometry.PI, true);
        checkPoint(OrientedPoint1S.fromPointAndDirection(Point1S.of(Geometry.MINUS_HALF_PI), false, TEST_PRECISION).transform(transform),
                -Geometry.PI, false);
    }

    @Test
    public void testTransform_negate() {
        // arrange
        Transform<Point1S> transform = p -> Point1S.of(-p.getAzimuth());

        // act
        checkPoint(OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, true, TEST_PRECISION).transform(transform),
                Geometry.ZERO_PI, false);
        checkPoint(OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, false, TEST_PRECISION).transform(transform),
                Geometry.ZERO_PI, true);

        checkPoint(OrientedPoint1S.fromPointAndDirection(Point1S.of(1.5 * Geometry.PI), true, TEST_PRECISION).transform(transform),
                -1.5 * Geometry.PI, false);
        checkPoint(OrientedPoint1S.fromPointAndDirection(Point1S.of(Geometry.MINUS_HALF_PI), false, TEST_PRECISION).transform(transform),
                Geometry.HALF_PI, true);
    }

    @Test
    public void testSpan() {
        // arrange
        OrientedPoint1S pt = OrientedPoint1S.fromPointAndDirection(Point1S.of(1.0), false, TEST_PRECISION);

        // act
        SubOrientedPoint1S result = pt.span();

        // assert
        Assert.assertSame(pt, result.getHyperplane());
    }

    @Test
    public void testEq() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        OrientedPoint1S a = OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, true, precision);

        OrientedPoint1S b = OrientedPoint1S.fromPointAndDirection(Point1S.PI, true, precision);
        OrientedPoint1S c = OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, false, precision);
        OrientedPoint1S d = OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, true, TEST_PRECISION);

        OrientedPoint1S e = OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, true, precision);
        OrientedPoint1S f = OrientedPoint1S.fromPointAndDirection(Point1S.of(Geometry.TWO_PI), true, precision);
        OrientedPoint1S g = OrientedPoint1S.fromPointAndDirection(Point1S.of(1e-4), true, precision);
        OrientedPoint1S h = OrientedPoint1S.fromPointAndDirection(Point1S.of(-1e-4), true, precision);

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

        OrientedPoint1S a = OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, true, TEST_PRECISION);
        OrientedPoint1S b = OrientedPoint1S.fromPointAndDirection(Point1S.PI, true, TEST_PRECISION);
        OrientedPoint1S c = OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, false, TEST_PRECISION);
        OrientedPoint1S d = OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, true, precision);
        OrientedPoint1S e = OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, true, TEST_PRECISION);

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

        OrientedPoint1S a = OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, true, TEST_PRECISION);
        OrientedPoint1S b = OrientedPoint1S.fromPointAndDirection(Point1S.PI, true, TEST_PRECISION);
        OrientedPoint1S c = OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, false, TEST_PRECISION);
        OrientedPoint1S d = OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, true, precision);
        OrientedPoint1S e = OrientedPoint1S.fromPointAndDirection(Point1S.ZERO_PI, true, TEST_PRECISION);

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
        OrientedPoint1S pt = OrientedPoint1S.createPositiveFacing(Geometry.ZERO_PI, TEST_PRECISION);

        // act
        String str = pt.toString();

        // assert
        Assert.assertTrue(str.startsWith("OrientedPoint1S["));
        Assert.assertTrue(str.contains("point= ") && str.contains("positiveFacing= "));
    }

    @Test
    public void testSubHyperplane_split() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        OrientedPoint1S pt = OrientedPoint1S.createPositiveFacing(-1.5, precision);
        SubOrientedPoint1S sub = pt.span();

        // act/assert
        checkSplit(sub, OrientedPoint1S.createPositiveFacing(1.0, precision), true, false);
        checkSplit(sub, OrientedPoint1S.createPositiveFacing(-1.5 + 1e-2, precision), true, false);

        checkSplit(sub, OrientedPoint1S.createNegativeFacing(1.0, precision), false, true);
        checkSplit(sub, OrientedPoint1S.createNegativeFacing(-1.5 + 1e-2, precision), false, true);

        checkSplit(sub, OrientedPoint1S.createNegativeFacing(-1.5, precision), false, false);
        checkSplit(sub, OrientedPoint1S.createNegativeFacing(-1.5 + 1e-4, precision), false, false);
        checkSplit(sub, OrientedPoint1S.createNegativeFacing(-1.5 - 1e-4, precision), false, false);
    }

    private void checkSplit(SubOrientedPoint1S sub, OrientedPoint1S splitter, boolean minus, boolean plus) {
        Split<SubOrientedPoint1S> split = sub.split(splitter);

        Assert.assertSame(minus ? sub : null, split.getMinus());
        Assert.assertSame(plus ? sub : null, split.getPlus());
    }

    @Test
    public void testSubHyperplane_simpleMethods() {
        // arrange
        OrientedPoint1S pt = OrientedPoint1S.createPositiveFacing(0, TEST_PRECISION);
        SubOrientedPoint1S sub = pt.span();

        // act/assert
        Assert.assertSame(pt, sub.getHyperplane());
        Assert.assertFalse(sub.isFull());
        Assert.assertFalse(sub.isEmpty());
        Assert.assertFalse(sub.isInfinite());
        Assert.assertTrue(sub.isFinite());
        Assert.assertEquals(0.0, sub.getSize(), TEST_EPS);

        List<SubOrientedPoint1S> list = sub.toConvex();
        Assert.assertEquals(1, list.size());
        Assert.assertSame(sub, list.get(0));
    }

    @Test
    public void testSubHyperplane_classify() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-1);
        OrientedPoint1S pt = OrientedPoint1S.createPositiveFacing(1, precision);
        SubOrientedPoint1S sub = pt.span();

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
        OrientedPoint1S pt = OrientedPoint1S.createPositiveFacing(1, precision);
        SubOrientedPoint1S sub = pt.span();

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
        OrientedPoint1S pt = OrientedPoint1S.createPositiveFacing(1, precision);
        SubOrientedPoint1S sub = pt.span();

        Point1S expected = Point1S.of(1);

        // act/assert
        Assert.assertEquals(expected, sub.closest(Point1S.ZERO_PI));
        Assert.assertEquals(expected, sub.closest(Point1S.of(Geometry.HALF_PI)));
        Assert.assertEquals(expected, sub.closest(Point1S.PI));
        Assert.assertEquals(expected, sub.closest(Point1S.of(Geometry.MINUS_HALF_PI)));
        Assert.assertEquals(expected, sub.closest(Point1S.of(Geometry.TWO_PI)));
    }

    @Test
    public void testSubHyperplane_transform() {
        // arrange
        OrientedPoint1S pt = OrientedPoint1S.fromPointAndDirection(Point1S.of(Geometry.HALF_PI), true, TEST_PRECISION);

        Transform<Point1S> transform = p -> Point1S.of(Geometry.PI - p.getAzimuth());

        // act
        SubOrientedPoint1S result = pt.span().transform(transform);

        // assert
        checkPoint(result.getHyperplane(), Geometry.HALF_PI, false);
    }

    @Test
    public void testSubHyperplane_reverse() {
        // arrange
        OrientedPoint1S pt = OrientedPoint1S.createPositiveFacing(2.0, TEST_PRECISION);
        SubOrientedPoint1S sub = pt.span();

        // act
        SubOrientedPoint1S result = sub.reverse();

        // assert
        Assert.assertEquals(2.0, result.getHyperplane().getAzimuth(), TEST_EPS);
        Assert.assertFalse(result.getHyperplane().isPositiveFacing());

        Assert.assertEquals(sub.getHyperplane(), result.reverse().getHyperplane());
    }

    @Test
    public void testSubHyperplane_toString() {
        // arrange
        OrientedPoint1S pt = OrientedPoint1S.createPositiveFacing(2, TEST_PRECISION);
        SubOrientedPoint1S sub = pt.span();

        // act
        String str = sub.toString();

        //assert
        Assert.assertTrue(str.contains("SubOrientedPoint1S["));
        Assert.assertTrue(str.contains("point= "));
        Assert.assertTrue(str.contains("positiveFacing= "));
    }

    @Test
    public void testBuilder() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        OrientedPoint1S pt = OrientedPoint1S.createPositiveFacing(0, precision);
        SubOrientedPoint1S sub = pt.span();

        // act
        Builder<Point1S> builder = sub.builder();

        builder.add(sub);
        builder.add(OrientedPoint1S.createPositiveFacing(1e-4, precision).span());
        builder.add((SubHyperplane<Point1S>) sub);

        SubHyperplane<Point1S> result = builder.build();

        // assert
        Assert.assertSame(sub, result);
    }

    @Test
    public void testBuilder_invalidArgs() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        OrientedPoint1S pt = OrientedPoint1S.createPositiveFacing(0, precision);
        SubOrientedPoint1S sub = pt.span();

        Builder<Point1S> builder = sub.builder();

        // act/assert
        GeometryTestUtils.assertThrows(
                () -> builder.add(OrientedPoint1S.createPositiveFacing(2e-3, precision).span()),
                IllegalArgumentException.class);
        GeometryTestUtils.assertThrows(
                () -> builder.add(OrientedPoint1S.createNegativeFacing(2e-3, precision).span()),
                IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(
                () -> builder.add((SubHyperplane<Point1S>) OrientedPoint1S.createPositiveFacing(2e-3, precision).span()),
                IllegalArgumentException.class);
    }

    @Test
    public void testBuilder_toString() {
        // arrange
        OrientedPoint1S pt = OrientedPoint1S.createPositiveFacing(2, TEST_PRECISION);
        SubOrientedPointBuilder1S builder = pt.span().builder();

        // act
        String str = builder.toString();

        //assert
        Assert.assertTrue(str.contains("SubOrientedPointBuilder1S["));
        Assert.assertTrue(str.contains("base= SubOrientedPoint1S["));
        Assert.assertTrue(str.contains("point= "));
        Assert.assertTrue(str.contains("positiveFacing= "));
    }

    private static void checkPoint(OrientedPoint1S pt, double az, boolean positiveFacing) {
        checkPoint(pt, az, positiveFacing, TEST_PRECISION);
    }

    private static void checkPoint(OrientedPoint1S pt, double az, boolean positiveFacing, DoublePrecisionContext precision) {
        Assert.assertEquals(az, pt.getAzimuth(), TEST_EPS);
        Assert.assertEquals(az, pt.getPoint().getAzimuth(), TEST_EPS);
        Assert.assertEquals(positiveFacing, pt.isPositiveFacing());

        Assert.assertSame(precision, pt.getPrecision());
    }

    private static void checkOffset(OrientedPoint1S pt, double az, double offset) {
        Assert.assertEquals(offset, pt.offset(Point1S.of(az)), TEST_EPS);
    }

    private static void checkClassify(OrientedPoint1S pt, HyperplaneLocation loc, double ... azimuths) {
        for (double az : azimuths) {
            Assert.assertEquals("Unexpected location for azimuth " + az, loc, pt.classify(Point1S.of(az)));
        }
    }

    private static void checkClassify(OrientedPoint1S orientedPt, HyperplaneLocation loc, Point1S ... pts) {
        for (Point1S pt : pts) {
            Assert.assertEquals("Unexpected location for point " + pt, loc, orientedPt.classify(pt));
        }
    }
}
