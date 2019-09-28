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

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.exception.GeometryValueException;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
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
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.Unit.PLUS,
                OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, TEST_PRECISION).getDirection(),
                TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.Unit.MINUS,
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
    public void testCopySelf() {
        // arrange
        OrientedPoint orig = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, TEST_PRECISION);

        // act
        OrientedPoint copy = orig.copySelf();

        // assert
        Assert.assertSame(orig, copy);
        assertOrientedPoint(copy, 2.0, true, TEST_PRECISION);
    }

    @Test
    public void testGetOffset_positiveFacing() {
        // arrange
        OrientedPoint pt = OrientedPoint.fromPointAndDirection(Vector1D.of(-2.0), true, TEST_PRECISION);

        // act/assert
        Assert.assertEquals(-98.0, pt.getOffset(Vector1D.of(-100)), Precision.EPSILON);
        Assert.assertEquals(-0.1, pt.getOffset(Vector1D.of(-2.1)), Precision.EPSILON);
        Assert.assertEquals(0.0, pt.getOffset(Vector1D.of(-2)), Precision.EPSILON);
        Assert.assertEquals(0.99, pt.getOffset(Vector1D.of(-1.01)), Precision.EPSILON);
        Assert.assertEquals(1.0, pt.getOffset(Vector1D.of(-1.0)), Precision.EPSILON);
        Assert.assertEquals(1.01, pt.getOffset(Vector1D.of(-0.99)), Precision.EPSILON);
        Assert.assertEquals(2.0, pt.getOffset(Vector1D.of(0)), Precision.EPSILON);
        Assert.assertEquals(102, pt.getOffset(Vector1D.of(100)), Precision.EPSILON);
    }

    @Test
    public void testGetOffset_negativeFacing() {
        // arrange
        OrientedPoint pt = OrientedPoint.fromPointAndDirection(Vector1D.of(-2.0), false, TEST_PRECISION);

        // act/assert
        Assert.assertEquals(98.0, pt.getOffset(Vector1D.of(-100)), Precision.EPSILON);
        Assert.assertEquals(0.1, pt.getOffset(Vector1D.of(-2.1)), Precision.EPSILON);
        Assert.assertEquals(0.0, pt.getOffset(Vector1D.of(-2)), Precision.EPSILON);
        Assert.assertEquals(-0.99, pt.getOffset(Vector1D.of(-1.01)), Precision.EPSILON);
        Assert.assertEquals(-1.0, pt.getOffset(Vector1D.of(-1.0)), Precision.EPSILON);
        Assert.assertEquals(-1.01, pt.getOffset(Vector1D.of(-0.99)), Precision.EPSILON);
        Assert.assertEquals(-2, pt.getOffset(Vector1D.of(0)), Precision.EPSILON);
        Assert.assertEquals(-102, pt.getOffset(Vector1D.of(100)), Precision.EPSILON);
    }

    @Test
    public void testWholeHyperplane() {
        // arrange
        OrientedPoint pt = OrientedPoint.fromPointAndDirection(Vector1D.of(1.0), false, TEST_PRECISION);

        // act
        SubOrientedPoint subPt = pt.wholeHyperplane();

        // assert
        Assert.assertSame(pt, subPt.getHyperplane());
        Assert.assertNull(subPt.getRemainingRegion());
    }

    @Test
    public void testWholeSpace() {
        // arrange
        OrientedPoint pt = OrientedPoint.fromPointAndDirection(Vector1D.of(1.0), false, TEST_PRECISION);

        // act
        IntervalsSet set = pt.wholeSpace();

        // assert
        EuclideanTestUtils.assertNegativeInfinity(set.getInf());
        EuclideanTestUtils.assertPositiveInfinity(set.getSup());
    }

    @Test
    public void testSameOrientationAs() {
        // arrange
        OrientedPoint negativeDir1 = OrientedPoint.fromPointAndDirection(Vector1D.of(1.0), false, TEST_PRECISION);
        OrientedPoint negativeDir2 = OrientedPoint.fromPointAndDirection(Vector1D.of(-1.0), false, TEST_PRECISION);
        OrientedPoint positiveDir1 = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, TEST_PRECISION);
        OrientedPoint positiveDir2 = OrientedPoint.fromPointAndDirection(Vector1D.of(-2.0), true, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(negativeDir1.sameOrientationAs(negativeDir1));
        Assert.assertTrue(negativeDir1.sameOrientationAs(negativeDir2));
        Assert.assertTrue(negativeDir2.sameOrientationAs(negativeDir1));

        Assert.assertTrue(positiveDir1.sameOrientationAs(positiveDir1));
        Assert.assertTrue(positiveDir1.sameOrientationAs(positiveDir2));
        Assert.assertTrue(positiveDir2.sameOrientationAs(positiveDir1));

        Assert.assertFalse(negativeDir1.sameOrientationAs(positiveDir1));
        Assert.assertFalse(positiveDir1.sameOrientationAs(negativeDir1));
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
        Assert.assertTrue(str.contains("location= (2.0)"));
        Assert.assertTrue(str.contains("direction= (1.0)"));
    }

    @Test
    public void testFromPointAndDirection_trueBooleanArg() {
        // act
        OrientedPoint pt = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), true, TEST_PRECISION);

        // assert
        assertOrientedPoint(pt, 2.0, true, TEST_PRECISION);
        Assert.assertEquals(1.0, pt.getDirection().getX(), Precision.EPSILON);
    }

    @Test
    public void testFromPointAndDirection_falseBooleanArg() {
        // act
        OrientedPoint pt = OrientedPoint.fromPointAndDirection(Vector1D.of(2.0), false, TEST_PRECISION);

        // assert
        assertOrientedPoint(pt, 2.0, false, TEST_PRECISION);
        Assert.assertEquals(-1.0, pt.getDirection().getX(), Precision.EPSILON);
    }

    @Test
    public void testFromPointAndDirection_positiveVectorArg() {
        // act
        OrientedPoint pt = OrientedPoint.fromPointAndDirection(
                Vector1D.of(-2.0), Vector1D.of(0.1), TEST_PRECISION);

        // assert
        assertOrientedPoint(pt, -2.0, true, TEST_PRECISION);
        Assert.assertEquals(1.0, pt.getDirection().getX(), Precision.EPSILON);
    }

    @Test
    public void testFromPointAndDirection_negativeVectorArg() {
        // act
        OrientedPoint pt = OrientedPoint.fromPointAndDirection(
                Vector1D.of(2.0), Vector1D.of(-10.1), TEST_PRECISION);

        // assert
        assertOrientedPoint(pt, 2.0, false, TEST_PRECISION);
        Assert.assertEquals(-1.0, pt.getDirection().getX(), Precision.EPSILON);
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
        ;
    }

    @Test
    public void testCreatePositiveFacing() {
        // act
        OrientedPoint pt = OrientedPoint.createPositiveFacing(
                Vector1D.of(-2.0), TEST_PRECISION);

        // assert
        assertOrientedPoint(pt, -2.0, true, TEST_PRECISION);
        Assert.assertEquals(1.0, pt.getDirection().getX(), Precision.EPSILON);
    }

    @Test
    public void testCreateNegativeFacing() {
        // act
        OrientedPoint pt = OrientedPoint.createNegativeFacing(
                Vector1D.of(2.0), TEST_PRECISION);

        // assert
        assertOrientedPoint(pt, 2.0, false, TEST_PRECISION);
        Assert.assertEquals(-1.0, pt.getDirection().getX(), Precision.EPSILON);
    }

    private static void assertOrientedPoint(OrientedPoint pt, double location,
            boolean positiveFacing, DoublePrecisionContext precision) {
        Assert.assertEquals(location, pt.getLocation().getX(), TEST_EPS);
        Assert.assertEquals(positiveFacing, pt.isPositiveFacing());
        Assert.assertSame(precision, pt.getPrecision());
    }
}
