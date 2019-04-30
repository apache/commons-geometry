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
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.exception.GeometryValueException;
import org.apache.commons.geometry.core.partition.ConvexSubHyperplane.Split;
import org.apache.commons.geometry.core.partition.HyperplaneLocation;
import org.apache.commons.geometry.core.partition.SubHyperplane;
import org.apache.commons.geometry.core.partition.SubHyperplane.Builder;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.OrientedPoint.OrientedPointSubHyperplane;
import org.apache.commons.geometry.euclidean.oned.OrientedPoint.OrientedPointSubHyperplaneBuilder;
import org.apache.commons.numbers.core.Precision;
import org.junit.Assert;
import org.junit.Test;

public class OrientedPointTest {

    private static final double TEST_EPS = 1e-15;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testGetDirection() {
        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.ONE,
                OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, TEST_PRECISION).getDirection(),
                TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.MINUS_ONE,
                OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), false, TEST_PRECISION).getDirection(),
                TEST_EPS);
    }

    @Test
    public void testReverse() {
        // act/assert
        assertOrientedPoint(OrientedPoint.fromPointAndDirection(Vector1D.of(0), true, TEST_PRECISION).reverse(),
                0.0, false, TEST_PRECISION);
        assertOrientedPoint(OrientedPoint.fromPointAndDirection(Vector1D.of(-1), false, TEST_PRECISION).reverse(),
                -1.0, true, TEST_PRECISION);
        assertOrientedPoint(OrientedPoint.fromPointAndDirection(Vector1D.of(1), true, TEST_PRECISION).reverse(),
                1.0, false, TEST_PRECISION);

        assertOrientedPoint(OrientedPoint.fromPointAndDirection(Vector1D.of(0), true, TEST_PRECISION).reverse().reverse(),
                0.0, true, TEST_PRECISION);
        assertOrientedPoint(OrientedPoint.fromPointAndDirection(Vector1D.of(-1), false, TEST_PRECISION).reverse().reverse(),
                -1.0, false, TEST_PRECISION);
        assertOrientedPoint(OrientedPoint.fromPointAndDirection(Vector1D.of(1), true, TEST_PRECISION).reverse().reverse(),
                1.0, true, TEST_PRECISION);
    }

    @Test
    public void testHyperplanePoints() {
        // arrange
        OrientedPoint orientedPoint = OrientedPoint.createNegativeFacing(2.5, TEST_PRECISION);

        // act/assert
        Assert.assertEquals(HyperplaneLocation.MINUS, orientedPoint.classify(orientedPoint.minusPoint()));
        Assert.assertEquals(HyperplaneLocation.PLUS, orientedPoint.classify(orientedPoint.plusPoint()));
        Assert.assertEquals(HyperplaneLocation.ON, orientedPoint.classify(orientedPoint.onPoint()));
    }

    @Test
    public void testHyperplanePoints_largeEpsilon() {
        // arrange
        OrientedPoint orientedPoint = OrientedPoint.createNegativeFacing(2.5, new EpsilonDoublePrecisionContext(2.0));

        // act/assert
        Assert.assertEquals(HyperplaneLocation.MINUS, orientedPoint.classify(orientedPoint.minusPoint()));
        Assert.assertEquals(HyperplaneLocation.PLUS, orientedPoint.classify(orientedPoint.plusPoint()));
        Assert.assertEquals(HyperplaneLocation.ON, orientedPoint.classify(orientedPoint.onPoint()));
    }

    @Test
    public void testTransform() {
        // arrange
        AffineTransformMatrix1D scaleAndTranslate = AffineTransformMatrix1D
                .createScale(0.5)
                .translate(-10);

        AffineTransformMatrix1D reflect = AffineTransformMatrix1D.createScale(-2);

        OrientedPoint a = OrientedPoint.createPositiveFacing(Vector1D.of(2.0), TEST_PRECISION);
        OrientedPoint b = OrientedPoint.createNegativeFacing(Vector1D.of(-3.0), TEST_PRECISION);

        // act/assert
        assertOrientedPoint(a.transform(scaleAndTranslate), -9.0, true, TEST_PRECISION);
        assertOrientedPoint(b.transform(scaleAndTranslate), -11.5, false, TEST_PRECISION);

        assertOrientedPoint(a.transform(reflect), -4.0, false, TEST_PRECISION);
        assertOrientedPoint(b.transform(reflect), 6.0, true, TEST_PRECISION);
    }

    @Test
    public void testTransform_locationAtInfinity() {
        // arrange
        OrientedPoint pos = OrientedPoint.createNegativeFacing(Double.POSITIVE_INFINITY, TEST_PRECISION);
        OrientedPoint neg = OrientedPoint.createPositiveFacing(Double.NEGATIVE_INFINITY, TEST_PRECISION);

        Transform<Vector1D> scaleAndTranslate = AffineTransformMatrix1D.identity().scale(10.0).translate(5.0);
        Transform<Vector1D> negate = Vector1D::negate;

        // act/assert
        assertOrientedPoint(pos.transform(scaleAndTranslate), Double.POSITIVE_INFINITY, false, TEST_PRECISION);
        assertOrientedPoint(neg.transform(scaleAndTranslate), Double.NEGATIVE_INFINITY, true, TEST_PRECISION);

        assertOrientedPoint(pos.transform(negate), Double.NEGATIVE_INFINITY, true, TEST_PRECISION);
        assertOrientedPoint(neg.transform(negate), Double.POSITIVE_INFINITY, false, TEST_PRECISION);
    }

    @Test
    public void testTransform_zeroScale() {
        // arrange
        AffineTransformMatrix1D zeroScale = AffineTransformMatrix1D.createScale(0.0);

        OrientedPoint pt = OrientedPoint.createPositiveFacing(Vector1D.of(2.0), TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(
                () -> pt.transform(zeroScale),
                GeometryValueException.class, "Oriented point direction cannot be zero");
    }

    @Test
    public void testOffset_positiveFacing() {
        // arrange
        OrientedPoint pt = OrientedPoint.fromPointAndDirection(Vector1D.of(-2.0), true, TEST_PRECISION);

        // act/assert
        Assert.assertEquals(-98.0, pt.offset(Vector1D.of(-100)), Precision.EPSILON);
        Assert.assertEquals(-0.1, pt.offset(Vector1D.of(-2.1)), Precision.EPSILON);
        Assert.assertEquals(0.0, pt.offset(Vector1D.of(-2)), Precision.EPSILON);
        Assert.assertEquals(0.99, pt.offset(Vector1D.of(-1.01)), Precision.EPSILON);
        Assert.assertEquals(1.0, pt.offset(Vector1D.of(-1.0)), Precision.EPSILON);
        Assert.assertEquals(1.01, pt.offset(Vector1D.of(-0.99)), Precision.EPSILON);
        Assert.assertEquals(2.0, pt.offset(Vector1D.of(0)), Precision.EPSILON);
        Assert.assertEquals(102, pt.offset(Vector1D.of(100)), Precision.EPSILON);
    }

    @Test
    public void testOffset_negativeFacing() {
        // arrange
        OrientedPoint pt = OrientedPoint.fromPointAndDirection(Vector1D.of(-2.0), false, TEST_PRECISION);

        // act/assert
        Assert.assertEquals(98.0, pt.offset(Vector1D.of(-100)), Precision.EPSILON);
        Assert.assertEquals(0.1, pt.offset(Vector1D.of(-2.1)), Precision.EPSILON);
        Assert.assertEquals(0.0, pt.offset(Vector1D.of(-2)), Precision.EPSILON);
        Assert.assertEquals(-0.99, pt.offset(Vector1D.of(-1.01)), Precision.EPSILON);
        Assert.assertEquals(-1.0, pt.offset(Vector1D.of(-1.0)), Precision.EPSILON);
        Assert.assertEquals(-1.01, pt.offset(Vector1D.of(-0.99)), Precision.EPSILON);
        Assert.assertEquals(-2, pt.offset(Vector1D.of(0)), Precision.EPSILON);
        Assert.assertEquals(-102, pt.offset(Vector1D.of(100)), Precision.EPSILON);
    }

    @Test
    public void testOffset_infinityArguments() {
        // arrange
        OrientedPoint pt = OrientedPoint.fromPointAndDirection(Vector1D.of(-2.0), true, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertPositiveInfinity(pt.offset(Vector1D.of(Double.POSITIVE_INFINITY)));
        GeometryTestUtils.assertNegativeInfinity(pt.offset(Vector1D.of(Double.NEGATIVE_INFINITY)));
    }

    @Test
    public void testOffset_infinityLocation() {
        // arrange
        OrientedPoint pt = OrientedPoint.fromPointAndDirection(Vector1D.of(Double.POSITIVE_INFINITY), true, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(Double.isNaN(pt.offset(Vector1D.of(Double.POSITIVE_INFINITY))));
        GeometryTestUtils.assertNegativeInfinity(pt.offset(Vector1D.of(Double.NEGATIVE_INFINITY)));

        GeometryTestUtils.assertNegativeInfinity(pt.offset(Vector1D.of(0)));
    }

    @Test
    public void testClassify() {
        // arrange
        DoublePrecisionContext smallPrecision = new EpsilonDoublePrecisionContext(1e-10);
        DoublePrecisionContext largePrecision = new EpsilonDoublePrecisionContext(1e-1);

        OrientedPoint smallPosFacing = OrientedPoint.fromLocationAndDirection(1.0, true, smallPrecision);
        OrientedPoint largeNegFacing = OrientedPoint.fromLocationAndDirection(1.0, false, largePrecision);

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
    public void testSpan() {
        // arrange
        OrientedPoint pt = OrientedPoint.fromPointAndDirection(Vector1D.of(1.0), false, TEST_PRECISION);

        // act
        OrientedPointSubHyperplane result = pt.span();

        // assert
        Assert.assertSame(pt, result.getHyperplane());
    }

    @Test
    public void testSimilarOrientation() {
        // arrange
        OrientedPoint negativeDir1 = OrientedPoint.fromPointAndDirection(Vector1D.of(1.0), false, TEST_PRECISION);
        OrientedPoint negativeDir2 = OrientedPoint.fromPointAndDirection(Vector1D.of(-1.0), false, TEST_PRECISION);
        OrientedPoint positiveDir1 = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, TEST_PRECISION);
        OrientedPoint positiveDir2 = OrientedPoint.fromPointAndDirection(Vector1D.of(-2.0), true, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(negativeDir1.similarOrientation(negativeDir1));
        Assert.assertTrue(negativeDir1.similarOrientation(negativeDir2));
        Assert.assertTrue(negativeDir2.similarOrientation(negativeDir1));

        Assert.assertTrue(positiveDir1.similarOrientation(positiveDir1));
        Assert.assertTrue(positiveDir1.similarOrientation(positiveDir2));
        Assert.assertTrue(positiveDir2.similarOrientation(positiveDir1));

        Assert.assertFalse(negativeDir1.similarOrientation(positiveDir1));
        Assert.assertFalse(positiveDir1.similarOrientation(negativeDir1));
    }

    @Test
    public void testProject() {
        // arrange
        OrientedPoint pt = OrientedPoint.fromPointAndDirection(Vector1D.of(1.0), true, TEST_PRECISION);

        // act/assert
        Assert.assertEquals(1.0, pt.project(Vector1D.of(-1.0)).getX(), Precision.EPSILON);
        Assert.assertEquals(1.0, pt.project(Vector1D.of(0.0)).getX(), Precision.EPSILON);
        Assert.assertEquals(1.0, pt.project(Vector1D.of(1.0)).getX(), Precision.EPSILON);
        Assert.assertEquals(1.0, pt.project(Vector1D.of(100.0)).getX(), Precision.EPSILON);
    }


    @Test
    public void testEq() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        OrientedPoint a = OrientedPoint.createPositiveFacing(0, precision);

        OrientedPoint b = OrientedPoint.createPositiveFacing(0, TEST_PRECISION);
        OrientedPoint c = OrientedPoint.createNegativeFacing(0, precision);
        OrientedPoint d = OrientedPoint.createPositiveFacing(2e-3, precision);

        OrientedPoint e = OrientedPoint.createPositiveFacing(1e-4, precision);

        // act/assert
        Assert.assertTrue(a.eq(a));

        Assert.assertFalse(a.eq(b));
        Assert.assertFalse(a.eq(c));
        Assert.assertFalse(a.eq(d));

        Assert.assertTrue(a.eq(e));
        Assert.assertTrue(e.eq(a));
    }

    @Test
    public void testHashCode() {
        // arrange
        DoublePrecisionContext precisionA = new EpsilonDoublePrecisionContext(1e-10);
        DoublePrecisionContext precisionB = new EpsilonDoublePrecisionContext(1e-15);

        OrientedPoint a = OrientedPoint.fromPointAndDirection(Vector1D.of(3.0), true, precisionA);
        OrientedPoint b = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), false, precisionA);
        OrientedPoint c = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, precisionB);

        OrientedPoint d = OrientedPoint.fromPointAndDirection(Vector1D.of(3.0), true, precisionA);
        OrientedPoint e = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), false, precisionA);
        OrientedPoint f = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, precisionB);

        // act/assert
        Assert.assertNotEquals(a.hashCode(), b.hashCode());
        Assert.assertNotEquals(b.hashCode(), c.hashCode());
        Assert.assertNotEquals(c.hashCode(), a.hashCode());

        Assert.assertEquals(a.hashCode(), d.hashCode());
        Assert.assertEquals(b.hashCode(), e.hashCode());
        Assert.assertEquals(c.hashCode(), f.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        DoublePrecisionContext precisionA = new EpsilonDoublePrecisionContext(1e-10);
        DoublePrecisionContext precisionB = new EpsilonDoublePrecisionContext(1e-15);

        OrientedPoint a = OrientedPoint.fromPointAndDirection(Vector1D.of(1.0), true, precisionA);
        OrientedPoint b = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, precisionA);

        OrientedPoint c = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, precisionA);
        OrientedPoint d = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), false, precisionA);

        OrientedPoint e = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, precisionA);
        OrientedPoint f = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, precisionB);

        OrientedPoint g = OrientedPoint.fromPointAndDirection(Vector1D.of(1.0), true, precisionA);

        // act/assert
        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(c.equals(d));
        Assert.assertFalse(e.equals(f));

        Assert.assertTrue(a.equals(a));
        Assert.assertTrue(a.equals(g));
        Assert.assertTrue(g.equals(a));
    }

    @Test
    public void testToString() {
        // arrange
        OrientedPoint pt = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, TEST_PRECISION);

        // act
        String str = pt.toString();

        // assert
        Assert.assertTrue(str.contains("OrientedPoint"));
        Assert.assertTrue(str.contains("point= (2.0)"));
        Assert.assertTrue(str.contains("direction= (1.0)"));
    }

    @Test
    public void testFromLocationAndDirection() {
        // act/assert
        assertOrientedPoint(OrientedPoint.fromLocationAndDirection(3.0, true, TEST_PRECISION),
                3.0, true, TEST_PRECISION);
        assertOrientedPoint(OrientedPoint.fromLocationAndDirection(2.0, false, TEST_PRECISION),
                2.0, false, TEST_PRECISION);
    }

    @Test
    public void testFromPointAndDirection_pointAndBooleanArgs() {
        // act/assert
        assertOrientedPoint(OrientedPoint.fromPointAndDirection(Vector1D.of(3.0), true, TEST_PRECISION),
                3.0, true, TEST_PRECISION);
        assertOrientedPoint(OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), false, TEST_PRECISION),
                2.0, false, TEST_PRECISION);
    }

    @Test
    public void testFromPointAndDirection_pointAndVectorArgs() {
        // act/assert
        assertOrientedPoint(OrientedPoint.fromPointAndDirection(Vector1D.of(-2.0), Vector1D.of(0.1), TEST_PRECISION),
                -2.0, true, TEST_PRECISION);
        assertOrientedPoint(OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), Vector1D.of(-10.1), TEST_PRECISION),
                2.0, false, TEST_PRECISION);
    }

    @Test
    public void testFromPointAndDirection_invalidDirection() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(0.1);

        // act/assert
        GeometryTestUtils.assertThrows(
                () -> OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), Vector1D.of(0.09), precision),
                GeometryValueException.class, "Oriented point direction cannot be zero");
        GeometryTestUtils.assertThrows(
                () -> OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), Vector1D.of(-0.09), precision),
                GeometryValueException.class, "Oriented point direction cannot be zero");
    }

    @Test
    public void testCreatePositiveFacing() {
        // act/assert
        assertOrientedPoint(OrientedPoint.createPositiveFacing(Vector1D.of(-2.0), TEST_PRECISION),
                -2.0, true, TEST_PRECISION);
        assertOrientedPoint(OrientedPoint.createPositiveFacing(-4.0, TEST_PRECISION),
                -4.0, true, TEST_PRECISION);
    }

    @Test
    public void testCreateNegativeFacing() {
        // act/assert
        assertOrientedPoint(OrientedPoint.createNegativeFacing(Vector1D.of(2.0), TEST_PRECISION),
                2.0, false, TEST_PRECISION);
        assertOrientedPoint(OrientedPoint.createNegativeFacing(4, TEST_PRECISION),
                4.0, false, TEST_PRECISION);
    }

    @Test
    public void testSubHyperplane_split() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        OrientedPoint pt = OrientedPoint.createPositiveFacing(-1.5, precision);
        OrientedPointSubHyperplane sub = pt.span();

        // act/assert
        checkSplit(sub, OrientedPoint.createPositiveFacing(1.0, precision), true, false);
        checkSplit(sub, OrientedPoint.createPositiveFacing(-1.5 + 1e-2, precision), true, false);

        checkSplit(sub, OrientedPoint.createNegativeFacing(1.0, precision), false, true);
        checkSplit(sub, OrientedPoint.createNegativeFacing(-1.5 + 1e-2, precision), false, true);

        checkSplit(sub, OrientedPoint.createNegativeFacing(-1.5, precision), false, false);
        checkSplit(sub, OrientedPoint.createNegativeFacing(-1.5 + 1e-4, precision), false, false);
        checkSplit(sub, OrientedPoint.createNegativeFacing(-1.5 - 1e-4, precision), false, false);
    }

    private void checkSplit(OrientedPointSubHyperplane sub, OrientedPoint splitter, boolean minus, boolean plus) {
        Split<Vector1D> split = sub.split(splitter);

        Assert.assertSame(minus ? sub : null, split.getMinus());
        Assert.assertSame(plus ? sub : null, split.getPlus());
    }

    @Test
    public void testSubHyperplane_simpleMethods() {
        // arrange
        OrientedPoint pt = OrientedPoint.createPositiveFacing(0, TEST_PRECISION);
        OrientedPointSubHyperplane sub = pt.span();

        // act/assert
        Assert.assertSame(pt, sub.getHyperplane());
        Assert.assertFalse(sub.isEmpty());
        Assert.assertFalse(sub.isInfinite());
        Assert.assertEquals(0.0, sub.getSize(), TEST_EPS);

        List<OrientedPointSubHyperplane> list = sub.toConvex();
        Assert.assertEquals(1, list.size());
        Assert.assertSame(sub, list.get(0));
    }

    @Test
    public void testSubHyperplane_transform() {
        // arrange
        AffineTransformMatrix1D scaleAndTranslate = AffineTransformMatrix1D
                .createScale(0.5)
                .translate(-10);

        AffineTransformMatrix1D reflect = AffineTransformMatrix1D.createScale(-2);

        OrientedPointSubHyperplane a = OrientedPoint.createPositiveFacing(Vector1D.of(2.0), TEST_PRECISION).span();
        OrientedPointSubHyperplane b = OrientedPoint.createNegativeFacing(Vector1D.of(-3.0), TEST_PRECISION).span();

        // act/assert
        assertOrientedPoint(a.transform(scaleAndTranslate).getHyperplane(), -9.0, true, TEST_PRECISION);
        assertOrientedPoint(b.transform(scaleAndTranslate).getHyperplane(), -11.5, false, TEST_PRECISION);

        assertOrientedPoint(a.transform(reflect).getHyperplane(), -4.0, false, TEST_PRECISION);
        assertOrientedPoint(b.transform(reflect).getHyperplane(), 6.0, true, TEST_PRECISION);
    }

    @Test
    public void testSuHyperplane_hashCode() {
        // arrange
        DoublePrecisionContext precisionA = new EpsilonDoublePrecisionContext(1e-10);
        DoublePrecisionContext precisionB = new EpsilonDoublePrecisionContext(1e-15);

        OrientedPointSubHyperplane a = OrientedPoint.fromPointAndDirection(Vector1D.of(3.0), true, precisionA).span();
        OrientedPointSubHyperplane b = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), false, precisionA).span();;
        OrientedPointSubHyperplane c = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, precisionB).span();;

        OrientedPointSubHyperplane d = OrientedPoint.fromPointAndDirection(Vector1D.of(3.0), true, precisionA).span();;
        OrientedPointSubHyperplane e = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), false, precisionA).span();;
        OrientedPointSubHyperplane f = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, precisionB).span();;

        // act/assert
        Assert.assertNotEquals(a.hashCode(), b.hashCode());
        Assert.assertNotEquals(b.hashCode(), c.hashCode());
        Assert.assertNotEquals(c.hashCode(), a.hashCode());

        Assert.assertEquals(a.hashCode(), d.hashCode());
        Assert.assertEquals(b.hashCode(), e.hashCode());
        Assert.assertEquals(c.hashCode(), f.hashCode());
    }

    @Test
    public void testSubHyperplane_equals() {
        // arrange
        DoublePrecisionContext precisionA = new EpsilonDoublePrecisionContext(1e-10);
        DoublePrecisionContext precisionB = new EpsilonDoublePrecisionContext(1e-15);

        OrientedPointSubHyperplane a = OrientedPoint.fromPointAndDirection(Vector1D.of(1.0), true, precisionA).span();
        OrientedPointSubHyperplane b = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, precisionA).span();

        OrientedPointSubHyperplane c = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, precisionA).span();
        OrientedPointSubHyperplane d = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), false, precisionA).span();

        OrientedPointSubHyperplane e = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, precisionA).span();
        OrientedPointSubHyperplane f = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, precisionB).span();

        OrientedPointSubHyperplane g = OrientedPoint.fromPointAndDirection(Vector1D.of(1.0), true, precisionA).span();

        // act/assert
        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(c.equals(d));
        Assert.assertFalse(e.equals(f));

        Assert.assertTrue(a.equals(a));
        Assert.assertTrue(a.equals(g));
        Assert.assertTrue(g.equals(a));
    }

    @Test
    public void testSubHyperplane_toString() {
        // arrange
        OrientedPoint pt = OrientedPoint.createPositiveFacing(2, TEST_PRECISION);
        OrientedPointSubHyperplane sub = pt.span();

        // act
        String str = sub.toString();

        //assert
        Assert.assertTrue(str.contains("OrientedPointSubHyperplane"));
        Assert.assertTrue(str.contains("point= (2.0)"));
        Assert.assertTrue(str.contains("direction= (1.0)"));
    }

    @Test
    public void testBuilder() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        OrientedPoint pt = OrientedPoint.createPositiveFacing(0, precision);
        OrientedPointSubHyperplane sub = pt.span();

        // act
        Builder<Vector1D> builder = sub.builder();

        builder.add(sub);
        builder.add(OrientedPoint.createPositiveFacing(1e-4, precision).span());
        builder.add((SubHyperplane<Vector1D>) sub);

        SubHyperplane<Vector1D> result = builder.build();

        // assert
        Assert.assertSame(sub, result);
    }

    @Test
    public void testBuilder_invalidArgs() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        OrientedPoint pt = OrientedPoint.createPositiveFacing(0, precision);
        OrientedPointSubHyperplane sub = pt.span();

        Builder<Vector1D> builder = sub.builder();

        // act/assert
        GeometryTestUtils.assertThrows(
                () -> builder.add(OrientedPoint.createPositiveFacing(2e-3, precision).span()),
                IllegalArgumentException.class);
        GeometryTestUtils.assertThrows(
                () -> builder.add(OrientedPoint.createNegativeFacing(2e-3, precision).span()),
                IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(
                () -> builder.add((SubHyperplane<Vector1D>) OrientedPoint.createPositiveFacing(2e-3, precision).span()),
                IllegalArgumentException.class);
    }

    @Test
    public void testBuilder_hashCode() {
        // arrange
        DoublePrecisionContext precisionA = new EpsilonDoublePrecisionContext(1e-10);
        DoublePrecisionContext precisionB = new EpsilonDoublePrecisionContext(1e-15);

        OrientedPointSubHyperplaneBuilder a = OrientedPoint.fromPointAndDirection(Vector1D.of(3.0), true, precisionA).span().builder();
        OrientedPointSubHyperplaneBuilder b = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), false, precisionA).span().builder();
        OrientedPointSubHyperplaneBuilder c = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, precisionB).span().builder();

        OrientedPointSubHyperplaneBuilder d = OrientedPoint.fromPointAndDirection(Vector1D.of(3.0), true, precisionA).span().builder();
        OrientedPointSubHyperplaneBuilder e = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), false, precisionA).span().builder();
        OrientedPointSubHyperplaneBuilder f = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, precisionB).span().builder();

        // act/assert
        Assert.assertNotEquals(a.hashCode(), b.hashCode());
        Assert.assertNotEquals(b.hashCode(), c.hashCode());
        Assert.assertNotEquals(c.hashCode(), a.hashCode());

        Assert.assertEquals(a.hashCode(), d.hashCode());
        Assert.assertEquals(b.hashCode(), e.hashCode());
        Assert.assertEquals(c.hashCode(), f.hashCode());
    }

    @Test
    public void testBuilder_equals() {
        // arrange
        DoublePrecisionContext precisionA = new EpsilonDoublePrecisionContext(1e-10);
        DoublePrecisionContext precisionB = new EpsilonDoublePrecisionContext(1e-15);

        OrientedPointSubHyperplaneBuilder a = OrientedPoint.fromPointAndDirection(Vector1D.of(1.0), true, precisionA).span().builder();
        OrientedPointSubHyperplaneBuilder b = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, precisionA).span().builder();

        OrientedPointSubHyperplaneBuilder c = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, precisionA).span().builder();
        OrientedPointSubHyperplaneBuilder d = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), false, precisionA).span().builder();

        OrientedPointSubHyperplaneBuilder e = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, precisionA).span().builder();
        OrientedPointSubHyperplaneBuilder f = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, precisionB).span().builder();

        OrientedPointSubHyperplaneBuilder g = OrientedPoint.fromPointAndDirection(Vector1D.of(1.0), true, precisionA).span().builder();

        // act/assert
        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(c.equals(d));
        Assert.assertFalse(e.equals(f));

        Assert.assertTrue(a.equals(a));
        Assert.assertTrue(a.equals(g));
        Assert.assertTrue(g.equals(a));
    }

    @Test
    public void testBuilder_toString() {
        // arrange
        OrientedPoint pt = OrientedPoint.createPositiveFacing(2, TEST_PRECISION);
        OrientedPointSubHyperplaneBuilder builder = pt.span().builder();

        // act
        String str = builder.toString();

        //assert
        Assert.assertTrue(str.contains("OrientedPointSubHyperplaneBuilder"));
        Assert.assertTrue(str.contains("base= OrientedPointSubHyperplane"));
        Assert.assertTrue(str.contains("point= (2.0)"));
        Assert.assertTrue(str.contains("direction= (1.0)"));
    }

    private static void assertOrientedPoint(OrientedPoint pt, double location, boolean positiveFacing,
            DoublePrecisionContext precision) {
        Assert.assertEquals(location, pt.getPoint().getX(), TEST_EPS);
        Assert.assertEquals(location, pt.getLocation(), TEST_EPS);
        Assert.assertEquals(positiveFacing ? 1.0 : -1.0, pt.getDirection().getX(), TEST_EPS);
        Assert.assertEquals(positiveFacing, pt.isPositiveFacing());
        Assert.assertSame(precision, pt.getPrecision());
    }

    private static void assertClassify(HyperplaneLocation expected, OrientedPoint pt, double ... locations) {
        for (double location : locations) {
            String msg = "Unexpected classification for location " + location;

            Assert.assertEquals(msg, expected, pt.classify(location));
            Assert.assertEquals(msg, expected, pt.classify(Vector1D.of(location)));
        }
    }
}
