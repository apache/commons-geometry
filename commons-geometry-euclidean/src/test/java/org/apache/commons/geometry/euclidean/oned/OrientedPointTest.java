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

import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.numbers.core.Precision;
import org.junit.Assert;
import org.junit.Test;

public class OrientedPointTest {

    @Test
    public void testConstructor() {
        // act
        OrientedPoint pt = new OrientedPoint(Point1D.of(2.0), true, 1e-5);

        // assert
        Assert.assertEquals(2.0, pt.getLocation().getX(), Precision.EPSILON);
        Assert.assertTrue(pt.isDirect());
        Assert.assertEquals(1e-5, pt.getTolerance(), Precision.EPSILON);
    }

    @Test
    public void testCopySelf() {
        // arrange
        OrientedPoint orig = new OrientedPoint(Point1D.of(2.0), true, 1e-5);

        // act
        OrientedPoint copy = orig.copySelf();

        // assert
        Assert.assertSame(orig, copy);
        Assert.assertEquals(2.0, copy.getLocation().getX(), Precision.EPSILON);
        Assert.assertTrue(copy.isDirect());
        Assert.assertEquals(1e-5, copy.getTolerance(), Precision.EPSILON);
    }

    @Test
    public void testGetOffset_direct_point() {
        // arrange
        OrientedPoint pt = new OrientedPoint(Point1D.of(-1.0), true, 1e-5);

        // act/assert
        Assert.assertEquals(-99, pt.getOffset(Point1D.of(-100)), Precision.EPSILON);
        Assert.assertEquals(-1, pt.getOffset(Point1D.of(-2)), Precision.EPSILON);
        Assert.assertEquals(-0.01, pt.getOffset(Point1D.of(-1.01)), Precision.EPSILON);
        Assert.assertEquals(0.0, pt.getOffset(Point1D.of(-1.0)), Precision.EPSILON);
        Assert.assertEquals(0.01, pt.getOffset(Point1D.of(-0.99)), Precision.EPSILON);
        Assert.assertEquals(1, pt.getOffset(Point1D.of(0)), Precision.EPSILON);
        Assert.assertEquals(101, pt.getOffset(Point1D.of(100)), Precision.EPSILON);
    }

    @Test
    public void testGetOffset_notDirect_point() {
        // arrange
        OrientedPoint pt = new OrientedPoint(Point1D.of(-1.0), false, 1e-5);

        // act/assert
        Assert.assertEquals(99, pt.getOffset(Point1D.of(-100)), Precision.EPSILON);
        Assert.assertEquals(1, pt.getOffset(Point1D.of(-2)), Precision.EPSILON);
        Assert.assertEquals(0.01, pt.getOffset(Point1D.of(-1.01)), Precision.EPSILON);
        Assert.assertEquals(0.0, pt.getOffset(Point1D.of(-1.0)), Precision.EPSILON);
        Assert.assertEquals(-0.01, pt.getOffset(Point1D.of(-0.99)), Precision.EPSILON);
        Assert.assertEquals(-1, pt.getOffset(Point1D.of(0)), Precision.EPSILON);
        Assert.assertEquals(-101, pt.getOffset(Point1D.of(100)), Precision.EPSILON);
    }

    @Test
    public void testWholeHyperplane() {
        // arrange
        OrientedPoint pt = new OrientedPoint(Point1D.of(1.0), false, 1e-5);

        // act
        SubOrientedPoint subPt = pt.wholeHyperplane();

        // assert
        Assert.assertSame(pt, subPt.getHyperplane());
        Assert.assertNull(subPt.getRemainingRegion());
    }

    @Test
    public void testWholeSpace() {
        // arrange
        OrientedPoint pt = new OrientedPoint(Point1D.of(1.0), false, 1e-5);

        // act
        IntervalsSet set = pt.wholeSpace();

        // assert
        EuclideanTestUtils.assertNegativeInfinity(set.getInf());
        EuclideanTestUtils.assertPositiveInfinity(set.getSup());
    }

    @Test
    public void testSameOrientationAs() {
        // arrange
        OrientedPoint notDirect1 = new OrientedPoint(Point1D.of(1.0), false, 1e-5);
        OrientedPoint notDirect2 = new OrientedPoint(Point1D.of(1.0), false, 1e-5);
        OrientedPoint direct1 = new OrientedPoint(Point1D.of(1.0), true, 1e-5);
        OrientedPoint direct2 = new OrientedPoint(Point1D.of(1.0), true, 1e-5);

        // act/assert
        Assert.assertTrue(notDirect1.sameOrientationAs(notDirect1));
        Assert.assertTrue(notDirect1.sameOrientationAs(notDirect2));
        Assert.assertTrue(notDirect2.sameOrientationAs(notDirect1));

        Assert.assertTrue(direct1.sameOrientationAs(direct1));
        Assert.assertTrue(direct1.sameOrientationAs(direct2));
        Assert.assertTrue(direct2.sameOrientationAs(direct1));

        Assert.assertFalse(notDirect1.sameOrientationAs(direct1));
        Assert.assertFalse(direct1.sameOrientationAs(notDirect1));
    }

    @Test
    public void testProject() {
        // arrange
        OrientedPoint pt = new OrientedPoint(Point1D.of(1.0), true, 1e-5);

        // act/assert
        Assert.assertEquals(1.0, pt.project(Point1D.of(-1.0)).getX(), Precision.EPSILON);
        Assert.assertEquals(1.0, pt.project(Point1D.of(0.0)).getX(), Precision.EPSILON);
        Assert.assertEquals(1.0, pt.project(Point1D.of(1.0)).getX(), Precision.EPSILON);
        Assert.assertEquals(1.0, pt.project(Point1D.of(100.0)).getX(), Precision.EPSILON);
    }

    @Test
    public void testRevertSelf() {
        // arrange
        OrientedPoint pt = new OrientedPoint(Point1D.of(2.0), true, 1e-5);

        // act
        pt.revertSelf();

        // assert
        Assert.assertEquals(2.0, pt.getLocation().getX(), Precision.EPSILON);
        Assert.assertFalse(pt.isDirect());
        Assert.assertEquals(1e-5, pt.getTolerance(), Precision.EPSILON);

        Assert.assertEquals(1, pt.getOffset(Point1D.of(1.0)), Precision.EPSILON);
        Assert.assertEquals(-1, pt.getOffset(Point1D.of(3.0)), Precision.EPSILON);
    }
}
