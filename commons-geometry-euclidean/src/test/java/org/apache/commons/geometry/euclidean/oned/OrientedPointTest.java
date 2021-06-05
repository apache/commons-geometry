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

import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OrientedPointTest {

    private static final double TEST_EPS = 1e-15;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    void testGetDirection() {
        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.Unit.PLUS,
                OrientedPoints.fromPointAndDirection(Vector1D.of(2.0), true, TEST_PRECISION).getDirection(),
                TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.Unit.MINUS,
                OrientedPoints.fromPointAndDirection(Vector1D.of(2.0), false, TEST_PRECISION).getDirection(),
                TEST_EPS);
    }

    @Test
    void testReverse() {
        // act/assert
        assertOrientedPoint(OrientedPoints.fromPointAndDirection(Vector1D.of(0), true, TEST_PRECISION).reverse(),
                0.0, false, TEST_PRECISION);
        assertOrientedPoint(OrientedPoints.fromPointAndDirection(Vector1D.of(-1), false, TEST_PRECISION).reverse(),
                -1.0, true, TEST_PRECISION);
        assertOrientedPoint(OrientedPoints.fromPointAndDirection(Vector1D.of(1), true, TEST_PRECISION).reverse(),
                1.0, false, TEST_PRECISION);

        assertOrientedPoint(OrientedPoints.fromPointAndDirection(Vector1D.of(0), true, TEST_PRECISION).reverse().reverse(),
                0.0, true, TEST_PRECISION);
        assertOrientedPoint(OrientedPoints.fromPointAndDirection(Vector1D.of(-1), false, TEST_PRECISION).reverse().reverse(),
                -1.0, false, TEST_PRECISION);
        assertOrientedPoint(OrientedPoints.fromPointAndDirection(Vector1D.of(1), true, TEST_PRECISION).reverse().reverse(),
                1.0, true, TEST_PRECISION);
    }

    @Test
    void testTransform() {
        // arrange
        final AffineTransformMatrix1D scaleAndTranslate = AffineTransformMatrix1D
                .createScale(0.5)
                .translate(-10);

        final AffineTransformMatrix1D reflect = AffineTransformMatrix1D.createScale(-2);

        final OrientedPoint a = OrientedPoints.createPositiveFacing(Vector1D.of(2.0), TEST_PRECISION);
        final OrientedPoint b = OrientedPoints.createNegativeFacing(Vector1D.of(-3.0), TEST_PRECISION);

        // act/assert
        assertOrientedPoint(a.transform(scaleAndTranslate), -9.0, true, TEST_PRECISION);
        assertOrientedPoint(b.transform(scaleAndTranslate), -11.5, false, TEST_PRECISION);

        assertOrientedPoint(a.transform(reflect), -4.0, false, TEST_PRECISION);
        assertOrientedPoint(b.transform(reflect), 6.0, true, TEST_PRECISION);
    }

    @Test
    void testTransform_locationAtInfinity() {
        // arrange
        final OrientedPoint pos = OrientedPoints.createNegativeFacing(Double.POSITIVE_INFINITY, TEST_PRECISION);
        final OrientedPoint neg = OrientedPoints.createPositiveFacing(Double.NEGATIVE_INFINITY, TEST_PRECISION);

        final Transform<Vector1D> scaleAndTranslate = AffineTransformMatrix1D.identity().scale(10.0).translate(5.0);
        final Transform<Vector1D> negate = AffineTransformMatrix1D.from(Vector1D::negate);

        // act/assert
        assertOrientedPoint(pos.transform(scaleAndTranslate), Double.POSITIVE_INFINITY, false, TEST_PRECISION);
        assertOrientedPoint(neg.transform(scaleAndTranslate), Double.NEGATIVE_INFINITY, true, TEST_PRECISION);

        assertOrientedPoint(pos.transform(negate), Double.NEGATIVE_INFINITY, true, TEST_PRECISION);
        assertOrientedPoint(neg.transform(negate), Double.POSITIVE_INFINITY, false, TEST_PRECISION);
    }

    @Test
    void testTransform_zeroScale() {
        // arrange
        final AffineTransformMatrix1D zeroScale = AffineTransformMatrix1D.createScale(0.0);

        final OrientedPoint pt = OrientedPoints.createPositiveFacing(Vector1D.of(2.0), TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
            () -> pt.transform(zeroScale),
            IllegalArgumentException.class, "Oriented point direction cannot be zero");
    }

    @Test
    void testOffset_positiveFacing() {
        // arrange
        final OrientedPoint pt = OrientedPoints.fromPointAndDirection(Vector1D.of(-2.0), true, TEST_PRECISION);

        // act/assert
        Assertions.assertEquals(-98.0, pt.offset(Vector1D.of(-100)), Precision.EPSILON);
        Assertions.assertEquals(-0.1, pt.offset(Vector1D.of(-2.1)), Precision.EPSILON);
        Assertions.assertEquals(0.0, pt.offset(Vector1D.of(-2)), Precision.EPSILON);
        Assertions.assertEquals(0.99, pt.offset(Vector1D.of(-1.01)), Precision.EPSILON);
        Assertions.assertEquals(1.0, pt.offset(Vector1D.of(-1.0)), Precision.EPSILON);
        Assertions.assertEquals(1.01, pt.offset(Vector1D.of(-0.99)), Precision.EPSILON);
        Assertions.assertEquals(2.0, pt.offset(Vector1D.of(0)), Precision.EPSILON);
        Assertions.assertEquals(102, pt.offset(Vector1D.of(100)), Precision.EPSILON);
    }

    @Test
    void testOffset_negativeFacing() {
        // arrange
        final OrientedPoint pt = OrientedPoints.fromPointAndDirection(Vector1D.of(-2.0), false, TEST_PRECISION);

        // act/assert
        Assertions.assertEquals(98.0, pt.offset(Vector1D.of(-100)), Precision.EPSILON);
        Assertions.assertEquals(0.1, pt.offset(Vector1D.of(-2.1)), Precision.EPSILON);
        Assertions.assertEquals(0.0, pt.offset(Vector1D.of(-2)), Precision.EPSILON);
        Assertions.assertEquals(-0.99, pt.offset(Vector1D.of(-1.01)), Precision.EPSILON);
        Assertions.assertEquals(-1.0, pt.offset(Vector1D.of(-1.0)), Precision.EPSILON);
        Assertions.assertEquals(-1.01, pt.offset(Vector1D.of(-0.99)), Precision.EPSILON);
        Assertions.assertEquals(-2, pt.offset(Vector1D.of(0)), Precision.EPSILON);
        Assertions.assertEquals(-102, pt.offset(Vector1D.of(100)), Precision.EPSILON);
    }

    @Test
    void testOffset_infinityArguments() {
        // arrange
        final OrientedPoint pt = OrientedPoints.fromPointAndDirection(Vector1D.of(-2.0), true, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertPositiveInfinity(pt.offset(Vector1D.of(Double.POSITIVE_INFINITY)));
        GeometryTestUtils.assertNegativeInfinity(pt.offset(Vector1D.of(Double.NEGATIVE_INFINITY)));
    }

    @Test
    void testOffset_infinityLocation() {
        // arrange
        final OrientedPoint pt = OrientedPoints.fromPointAndDirection(Vector1D.of(Double.POSITIVE_INFINITY), true, TEST_PRECISION);

        // act/assert
        Assertions.assertTrue(Double.isNaN(pt.offset(Vector1D.of(Double.POSITIVE_INFINITY))));
        GeometryTestUtils.assertNegativeInfinity(pt.offset(Vector1D.of(Double.NEGATIVE_INFINITY)));

        GeometryTestUtils.assertNegativeInfinity(pt.offset(Vector1D.of(0)));
    }

    @Test
    void testClassify() {
        // arrange
        final Precision.DoubleEquivalence smallPrecision = Precision.doubleEquivalenceOfEpsilon(1e-10);
        final Precision.DoubleEquivalence largePrecision = Precision.doubleEquivalenceOfEpsilon(1e-1);

        final OrientedPoint smallPosFacing = OrientedPoints.fromLocationAndDirection(1.0, true, smallPrecision);
        final OrientedPoint largeNegFacing = OrientedPoints.fromLocationAndDirection(1.0, false, largePrecision);

        // act/assert
        assertClassify(HyperplaneLocation.MINUS, smallPosFacing,
                Double.NEGATIVE_INFINITY, -10, 0, 0.9, 0.99999, 1 - 1e-9);
        assertClassify(HyperplaneLocation.ON, smallPosFacing,
                1 - 1e-11, 1, 1 + 1e-11);
        assertClassify(HyperplaneLocation.PLUS, smallPosFacing,
                1 + 1e-9, 2, 10, Double.POSITIVE_INFINITY);

        assertClassify(HyperplaneLocation.PLUS, largeNegFacing,
                Double.NEGATIVE_INFINITY, -10, 0, 0.89);
        assertClassify(HyperplaneLocation.ON, largeNegFacing,
                0.91, 0.9999, 1, 1.001, 1.09);
        assertClassify(HyperplaneLocation.MINUS, largeNegFacing,
                1.11, 2, 10, Double.POSITIVE_INFINITY);
    }

    @Test
    void testSpan() {
        // arrange
        final OrientedPoint pt = OrientedPoints.fromPointAndDirection(Vector1D.of(1.0), false, TEST_PRECISION);

        // act
        final HyperplaneConvexSubset<Vector1D> result = pt.span();

        // assert
        Assertions.assertSame(pt, result.getHyperplane());
    }

    @Test
    void testSimilarOrientation() {
        // arrange
        final OrientedPoint negativeDir1 = OrientedPoints.fromPointAndDirection(Vector1D.of(1.0), false, TEST_PRECISION);
        final OrientedPoint negativeDir2 = OrientedPoints.fromPointAndDirection(Vector1D.of(-1.0), false, TEST_PRECISION);
        final OrientedPoint positiveDir1 = OrientedPoints.fromPointAndDirection(Vector1D.of(2.0), true, TEST_PRECISION);
        final OrientedPoint positiveDir2 = OrientedPoints.fromPointAndDirection(Vector1D.of(-2.0), true, TEST_PRECISION);

        // act/assert
        Assertions.assertTrue(negativeDir1.similarOrientation(negativeDir1));
        Assertions.assertTrue(negativeDir1.similarOrientation(negativeDir2));
        Assertions.assertTrue(negativeDir2.similarOrientation(negativeDir1));

        Assertions.assertTrue(positiveDir1.similarOrientation(positiveDir1));
        Assertions.assertTrue(positiveDir1.similarOrientation(positiveDir2));
        Assertions.assertTrue(positiveDir2.similarOrientation(positiveDir1));

        Assertions.assertFalse(negativeDir1.similarOrientation(positiveDir1));
        Assertions.assertFalse(positiveDir1.similarOrientation(negativeDir1));
    }

    @Test
    void testProject() {
        // arrange
        final OrientedPoint pt = OrientedPoints.fromPointAndDirection(Vector1D.of(1.0), true, TEST_PRECISION);

        // act/assert
        Assertions.assertEquals(1.0, pt.project(Vector1D.of(-1.0)).getX(), Precision.EPSILON);
        Assertions.assertEquals(1.0, pt.project(Vector1D.of(0.0)).getX(), Precision.EPSILON);
        Assertions.assertEquals(1.0, pt.project(Vector1D.of(1.0)).getX(), Precision.EPSILON);
        Assertions.assertEquals(1.0, pt.project(Vector1D.of(100.0)).getX(), Precision.EPSILON);
    }


    @Test
    void testEq() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-3);

        final OrientedPoint a = OrientedPoints.createPositiveFacing(0, precision);
        final OrientedPoint b = OrientedPoints.createPositiveFacing(0, TEST_PRECISION);

        final OrientedPoint c = OrientedPoints.createPositiveFacing(2e-3, precision);
        final OrientedPoint d = OrientedPoints.createNegativeFacing(0, precision);
        final OrientedPoint e = OrientedPoints.createPositiveFacing(1e-4, precision);

        // act/assert
        Assertions.assertTrue(a.eq(a, precision));
        Assertions.assertTrue(a.eq(b, precision));

        Assertions.assertFalse(a.eq(c, precision));
        Assertions.assertFalse(a.eq(d, precision));

        Assertions.assertTrue(a.eq(e, precision));
        Assertions.assertTrue(e.eq(a, precision));
    }

    @Test
    void testHashCode() {
        // arrange
        final Precision.DoubleEquivalence precisionA = Precision.doubleEquivalenceOfEpsilon(1e-10);
        final Precision.DoubleEquivalence precisionB = Precision.doubleEquivalenceOfEpsilon(1e-15);

        final OrientedPoint a = OrientedPoints.fromPointAndDirection(Vector1D.of(3.0), true, precisionA);
        final OrientedPoint b = OrientedPoints.fromPointAndDirection(Vector1D.of(2.0), false, precisionA);
        final OrientedPoint c = OrientedPoints.fromPointAndDirection(Vector1D.of(2.0), true, precisionB);

        final OrientedPoint d = OrientedPoints.fromPointAndDirection(Vector1D.of(3.0), true, precisionA);
        final OrientedPoint e = OrientedPoints.fromPointAndDirection(Vector1D.of(2.0), false, precisionA);
        final OrientedPoint f = OrientedPoints.fromPointAndDirection(Vector1D.of(2.0), true, precisionB);

        // act/assert
        Assertions.assertNotEquals(a.hashCode(), b.hashCode());
        Assertions.assertNotEquals(b.hashCode(), c.hashCode());
        Assertions.assertNotEquals(c.hashCode(), a.hashCode());

        Assertions.assertEquals(a.hashCode(), d.hashCode());
        Assertions.assertEquals(b.hashCode(), e.hashCode());
        Assertions.assertEquals(c.hashCode(), f.hashCode());
    }

    @Test
    void testEquals() {
        // arrange
        final Precision.DoubleEquivalence precisionA = Precision.doubleEquivalenceOfEpsilon(1e-10);
        final Precision.DoubleEquivalence precisionB = Precision.doubleEquivalenceOfEpsilon(1e-15);

        final OrientedPoint a = OrientedPoints.fromPointAndDirection(Vector1D.of(1.0), true, precisionA);
        final OrientedPoint b = OrientedPoints.fromPointAndDirection(Vector1D.of(2.0), true, precisionA);

        final OrientedPoint c = OrientedPoints.fromPointAndDirection(Vector1D.of(2.0), true, precisionA);
        final OrientedPoint d = OrientedPoints.fromPointAndDirection(Vector1D.of(2.0), false, precisionA);

        final OrientedPoint e = OrientedPoints.fromPointAndDirection(Vector1D.of(2.0), true, precisionA);
        final OrientedPoint f = OrientedPoints.fromPointAndDirection(Vector1D.of(2.0), true, precisionB);

        final OrientedPoint g = OrientedPoints.fromPointAndDirection(Vector1D.of(1.0), true, precisionA);

        // act/assert
        GeometryTestUtils.assertSimpleEqualsCases(a);

        Assertions.assertNotEquals(a, b);
        Assertions.assertNotEquals(c, d);
        Assertions.assertNotEquals(e, f);

        Assertions.assertEquals(a, g);
        Assertions.assertEquals(g, a);
    }

    @Test
    void testToString() {
        // arrange
        final OrientedPoint pt = OrientedPoints.fromPointAndDirection(Vector1D.of(2.0), true, TEST_PRECISION);

        // act
        final String str = pt.toString();

        // assert
        Assertions.assertTrue(str.contains("OrientedPoint"));
        Assertions.assertTrue(str.contains("point= (2.0)"));
        Assertions.assertTrue(str.contains("direction= (1.0)"));
    }

    @Test
    void testFromLocationAndDirection() {
        // act/assert
        assertOrientedPoint(OrientedPoints.fromLocationAndDirection(3.0, true, TEST_PRECISION),
                3.0, true, TEST_PRECISION);
        assertOrientedPoint(OrientedPoints.fromLocationAndDirection(2.0, false, TEST_PRECISION),
                2.0, false, TEST_PRECISION);
    }

    @Test
    void testFromPointAndDirection_pointAndBooleanArgs() {
        // act/assert
        assertOrientedPoint(OrientedPoints.fromPointAndDirection(Vector1D.of(3.0), true, TEST_PRECISION),
                3.0, true, TEST_PRECISION);
        assertOrientedPoint(OrientedPoints.fromPointAndDirection(Vector1D.of(2.0), false, TEST_PRECISION),
                2.0, false, TEST_PRECISION);
    }

    @Test
    void testFromPointAndDirection_pointAndVectorArgs() {
        // act/assert
        assertOrientedPoint(OrientedPoints.fromPointAndDirection(Vector1D.of(-2.0), Vector1D.of(0.1), TEST_PRECISION),
                -2.0, true, TEST_PRECISION);
        assertOrientedPoint(OrientedPoints.fromPointAndDirection(Vector1D.of(2.0), Vector1D.of(-10.1), TEST_PRECISION),
                2.0, false, TEST_PRECISION);
    }

    @Test
    void testFromPointAndDirection_invalidDirection() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(0.1);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
            () -> OrientedPoints.fromPointAndDirection(Vector1D.of(2.0), Vector1D.of(0.09), precision),
            IllegalArgumentException.class, "Oriented point direction cannot be zero");
        GeometryTestUtils.assertThrowsWithMessage(
            () -> OrientedPoints.fromPointAndDirection(Vector1D.of(2.0), Vector1D.of(-0.09), precision),
            IllegalArgumentException.class, "Oriented point direction cannot be zero");
    }

    @Test
    void testCreatePositiveFacing() {
        // act/assert
        assertOrientedPoint(OrientedPoints.createPositiveFacing(Vector1D.of(-2.0), TEST_PRECISION),
                -2.0, true, TEST_PRECISION);
        assertOrientedPoint(OrientedPoints.createPositiveFacing(-4.0, TEST_PRECISION),
                -4.0, true, TEST_PRECISION);
    }

    @Test
    void testCreateNegativeFacing() {
        // act/assert
        assertOrientedPoint(OrientedPoints.createNegativeFacing(Vector1D.of(2.0), TEST_PRECISION),
                2.0, false, TEST_PRECISION);
        assertOrientedPoint(OrientedPoints.createNegativeFacing(4, TEST_PRECISION),
                4.0, false, TEST_PRECISION);
    }

    @Test
    void testSubset_split() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-3);

        final OrientedPoint pt = OrientedPoints.createPositiveFacing(-1.5, precision);
        final HyperplaneConvexSubset<Vector1D> sub = pt.span();

        // act/assert
        checkSplit(sub, OrientedPoints.createPositiveFacing(1.0, precision), true, false);
        checkSplit(sub, OrientedPoints.createPositiveFacing(-1.5 + 1e-2, precision), true, false);

        checkSplit(sub, OrientedPoints.createNegativeFacing(1.0, precision), false, true);
        checkSplit(sub, OrientedPoints.createNegativeFacing(-1.5 + 1e-2, precision), false, true);

        checkSplit(sub, OrientedPoints.createNegativeFacing(-1.5, precision), false, false);
        checkSplit(sub, OrientedPoints.createNegativeFacing(-1.5 + 1e-4, precision), false, false);
        checkSplit(sub, OrientedPoints.createNegativeFacing(-1.5 - 1e-4, precision), false, false);
    }

    private void checkSplit(final HyperplaneConvexSubset<Vector1D> sub, final OrientedPoint splitter, final boolean minus, final boolean plus) {
        final Split<? extends HyperplaneConvexSubset<Vector1D>> split = sub.split(splitter);

        Assertions.assertSame(minus ? sub : null, split.getMinus());
        Assertions.assertSame(plus ? sub : null, split.getPlus());
    }

    @Test
    void testSubset_simpleMethods() {
        // arrange
        final OrientedPoint pt = OrientedPoints.createPositiveFacing(2, TEST_PRECISION);
        final HyperplaneConvexSubset<Vector1D> sub = pt.span();

        // act/assert
        Assertions.assertSame(pt, sub.getHyperplane());
        Assertions.assertFalse(sub.isFull());
        Assertions.assertFalse(sub.isEmpty());
        Assertions.assertFalse(sub.isInfinite());
        Assertions.assertTrue(sub.isFinite());
        Assertions.assertEquals(0.0, sub.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(2), sub.getCentroid(), TEST_EPS);

        final List<? extends HyperplaneConvexSubset<Vector1D>> list = sub.toConvex();
        Assertions.assertEquals(1, list.size());
        Assertions.assertSame(sub, list.get(0));
    }

    @Test
    void testSubset_classify() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-1);
        final OrientedPoint pt = OrientedPoints.createPositiveFacing(1, precision);
        final HyperplaneConvexSubset<Vector1D> sub = pt.span();

        // act/assert
        Assertions.assertEquals(RegionLocation.BOUNDARY, sub.classify(Vector1D.of(0.95)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, sub.classify(Vector1D.of(1)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, sub.classify(Vector1D.of(1.05)));

        Assertions.assertEquals(RegionLocation.OUTSIDE, sub.classify(Vector1D.of(1.11)));
        Assertions.assertEquals(RegionLocation.OUTSIDE, sub.classify(Vector1D.of(0.89)));

        Assertions.assertEquals(RegionLocation.OUTSIDE, sub.classify(Vector1D.of(-3)));
        Assertions.assertEquals(RegionLocation.OUTSIDE, sub.classify(Vector1D.of(10)));

        Assertions.assertEquals(RegionLocation.OUTSIDE, sub.classify(Vector1D.NEGATIVE_INFINITY));
        Assertions.assertEquals(RegionLocation.OUTSIDE, sub.classify(Vector1D.POSITIVE_INFINITY));
    }

    @Test
    void testSubset_contains() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-1);
        final OrientedPoint pt = OrientedPoints.createPositiveFacing(1, precision);
        final HyperplaneConvexSubset<Vector1D> sub = pt.span();

        // act/assert
        Assertions.assertTrue(sub.contains(Vector1D.of(0.95)));
        Assertions.assertTrue(sub.contains(Vector1D.of(1)));
        Assertions.assertTrue(sub.contains(Vector1D.of(1.05)));

        Assertions.assertFalse(sub.contains(Vector1D.of(1.11)));
        Assertions.assertFalse(sub.contains(Vector1D.of(0.89)));

        Assertions.assertFalse(sub.contains(Vector1D.of(-3)));
        Assertions.assertFalse(sub.contains(Vector1D.of(10)));

        Assertions.assertFalse(sub.contains(Vector1D.NEGATIVE_INFINITY));
        Assertions.assertFalse(sub.contains(Vector1D.POSITIVE_INFINITY));
    }

    @Test
    void testSubset_closestContained() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-1);
        final OrientedPoint pt = OrientedPoints.createPositiveFacing(1, precision);
        final HyperplaneConvexSubset<Vector1D> sub = pt.span();

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(1), sub.closest(Vector1D.NEGATIVE_INFINITY), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(1), sub.closest(Vector1D.of(0)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(1), sub.closest(Vector1D.of(1)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(1), sub.closest(Vector1D.of(2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(1), sub.closest(Vector1D.POSITIVE_INFINITY), TEST_EPS);
    }

    @Test
    void testSubset_transform() {
        // arrange
        final AffineTransformMatrix1D scaleAndTranslate = AffineTransformMatrix1D
                .createScale(0.5)
                .translate(-10);

        final AffineTransformMatrix1D reflect = AffineTransformMatrix1D.createScale(-2);

        final HyperplaneConvexSubset<Vector1D> a =
                OrientedPoints.createPositiveFacing(Vector1D.of(2.0), TEST_PRECISION).span();
        final HyperplaneConvexSubset<Vector1D> b =
                OrientedPoints.createNegativeFacing(Vector1D.of(-3.0), TEST_PRECISION).span();

        // act/assert
        assertOrientedPoint((OrientedPoint) a.transform(scaleAndTranslate).getHyperplane(),
                -9.0, true, TEST_PRECISION);
        assertOrientedPoint((OrientedPoint) b.transform(scaleAndTranslate).getHyperplane(),
                -11.5, false, TEST_PRECISION);

        assertOrientedPoint((OrientedPoint) a.transform(reflect).getHyperplane(), -4.0, false, TEST_PRECISION);
        assertOrientedPoint((OrientedPoint) b.transform(reflect).getHyperplane(), 6.0, true, TEST_PRECISION);
    }

    @Test
    void testSubset_reverse() {
        // arrange
        final OrientedPoint pt = OrientedPoints.createPositiveFacing(2.0, TEST_PRECISION);
        final HyperplaneConvexSubset<Vector1D> sub = pt.span();

        // act
        final HyperplaneConvexSubset<Vector1D> result = sub.reverse();

        // assert
        Assertions.assertEquals(2.0, ((OrientedPoint) result.getHyperplane()).getLocation(), TEST_EPS);
        Assertions.assertFalse(((OrientedPoint) result.getHyperplane()).isPositiveFacing());

        Assertions.assertEquals(sub.getHyperplane(), result.reverse().getHyperplane());
    }

    @Test
    void testSubset_toString() {
        // arrange
        final OrientedPoint pt = OrientedPoints.createPositiveFacing(2, TEST_PRECISION);
        final HyperplaneConvexSubset<Vector1D> sub = pt.span();

        // act
        final String str = sub.toString();

        //assert
        Assertions.assertTrue(str.contains("OrientedPointConvexSubset"));
        Assertions.assertTrue(str.contains("point= (2.0)"));
        Assertions.assertTrue(str.contains("direction= (1.0)"));
    }

    private static void assertOrientedPoint(final OrientedPoint pt, final double location, final boolean positiveFacing,
                                            final Precision.DoubleEquivalence precision) {
        Assertions.assertEquals(location, pt.getPoint().getX(), TEST_EPS);
        Assertions.assertEquals(location, pt.getLocation(), TEST_EPS);
        Assertions.assertEquals(positiveFacing ? 1.0 : -1.0, pt.getDirection().getX(), TEST_EPS);
        Assertions.assertEquals(positiveFacing, pt.isPositiveFacing());
        Assertions.assertSame(precision, pt.getPrecision());
    }

    private static void assertClassify(final HyperplaneLocation expected, final OrientedPoint pt, final double... locations) {
        for (final double location : locations) {
            final String msg = "Unexpected classification for location " + location;

            Assertions.assertEquals(expected, pt.classify(location), msg);
            Assertions.assertEquals(expected, pt.classify(Vector1D.of(location)), msg);
        }
    }
}
