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

import org.apache.commons.geometry.core.partitioning.Side_Old;
import org.apache.commons.geometry.core.partitioning.SubHyperplane_Old;
import org.apache.commons.geometry.core.partitioning.SubHyperplane_Old.SplitSubHyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.junit.Assert;
import org.junit.Test;

public class SubOrientedPointTest {

    private static final double TEST_EPS = 1e-15;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testGetSize() {
        // arrange
        OrientedPoint_Old hyperplane = OrientedPoint_Old.fromPointAndDirection(Vector1D.of(1), true, TEST_PRECISION);
        SubOrientedPoint_Old pt = hyperplane.wholeHyperplane();

        // act/assert
        Assert.assertEquals(0.0, pt.getSize(), TEST_EPS);
    }

    @Test
    public void testIsEmpty() {
        // arrange
        OrientedPoint_Old hyperplane = OrientedPoint_Old.fromPointAndDirection(Vector1D.of(1), true, TEST_PRECISION);
        SubOrientedPoint_Old pt = hyperplane.wholeHyperplane();

        // act/assert
        Assert.assertFalse(pt.isEmpty());
    }

    @Test
    public void testBuildNew() {
        // arrange
        OrientedPoint_Old originalHyperplane = OrientedPoint_Old.fromPointAndDirection(Vector1D.of(1), true, TEST_PRECISION);
        SubOrientedPoint_Old pt = originalHyperplane.wholeHyperplane();

        OrientedPoint_Old hyperplane = OrientedPoint_Old.fromPointAndDirection(Vector1D.of(2), true, TEST_PRECISION);
        IntervalsSet intervals = new IntervalsSet(2, 3, TEST_PRECISION);

        // act
        SubHyperplane_Old<Vector1D> result = pt.buildNew(hyperplane, intervals);

        // assert
        Assert.assertTrue(result instanceof SubOrientedPoint_Old);
        Assert.assertSame(hyperplane, result.getHyperplane());
        Assert.assertSame(intervals, ((SubOrientedPoint_Old) result).getRemainingRegion());
    }

    @Test
    public void testSplit_resultOnMinusSide() {
        // arrange
        OrientedPoint_Old hyperplane = OrientedPoint_Old.fromPointAndDirection(Vector1D.of(1), true, TEST_PRECISION);
        IntervalsSet interval = new IntervalsSet(TEST_PRECISION);
        SubOrientedPoint_Old pt = new SubOrientedPoint_Old(hyperplane, interval);

        OrientedPoint_Old splitter = OrientedPoint_Old.fromPointAndDirection(Vector1D.of(2), true, TEST_PRECISION);

        // act
        SplitSubHyperplane<Vector1D> split = pt.split(splitter);

        // assert
        Assert.assertEquals(Side_Old.MINUS, split.getSide());

        SubOrientedPoint_Old minusSub = ((SubOrientedPoint_Old) split.getMinus());
        Assert.assertNotNull(minusSub);

        OrientedPoint_Old minusHyper = (OrientedPoint_Old) minusSub.getHyperplane();
        Assert.assertEquals(1, minusHyper.getLocation().getX(), TEST_EPS);

        Assert.assertSame(interval, minusSub.getRemainingRegion());

        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_resultOnPlusSide() {
        // arrange
        OrientedPoint_Old hyperplane = OrientedPoint_Old.fromPointAndDirection(Vector1D.of(1), true, TEST_PRECISION);
        IntervalsSet interval = new IntervalsSet(TEST_PRECISION);
        SubOrientedPoint_Old pt = new SubOrientedPoint_Old(hyperplane, interval);

        OrientedPoint_Old splitter = OrientedPoint_Old.fromPointAndDirection(Vector1D.of(0), true, TEST_PRECISION);

        // act
        SplitSubHyperplane<Vector1D> split = pt.split(splitter);

        // assert
        Assert.assertEquals(Side_Old.PLUS, split.getSide());

        Assert.assertNull(split.getMinus());

        SubOrientedPoint_Old plusSub = ((SubOrientedPoint_Old) split.getPlus());
        Assert.assertNotNull(plusSub);

        OrientedPoint_Old plusHyper = (OrientedPoint_Old) plusSub.getHyperplane();
        Assert.assertEquals(1, plusHyper.getLocation().getX(), TEST_EPS);

        Assert.assertSame(interval, plusSub.getRemainingRegion());
    }

    @Test
    public void testSplit_equivalentHyperplanes() {
        // arrange
        OrientedPoint_Old hyperplane = OrientedPoint_Old.fromPointAndDirection(Vector1D.of(1), true, TEST_PRECISION);
        IntervalsSet interval = new IntervalsSet(TEST_PRECISION);
        SubOrientedPoint_Old pt = new SubOrientedPoint_Old(hyperplane, interval);

        OrientedPoint_Old splitter = OrientedPoint_Old.fromPointAndDirection(Vector1D.of(1), true, TEST_PRECISION);

        // act
        SplitSubHyperplane<Vector1D> split = pt.split(splitter);

        // assert
        Assert.assertEquals(Side_Old.HYPER, split.getSide());

        Assert.assertNull(split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_usesToleranceFromParentHyperplane() {
        // arrange
        DoublePrecisionContext parentPrecision = new EpsilonDoublePrecisionContext(0.1);
        DoublePrecisionContext otherPrecision = new EpsilonDoublePrecisionContext(1e-10);

        OrientedPoint_Old hyperplane = OrientedPoint_Old.fromPointAndDirection(Vector1D.of(1), true, parentPrecision);
        SubOrientedPoint_Old pt = hyperplane.wholeHyperplane();

        // act/assert
        SplitSubHyperplane<Vector1D> plusSplit = pt.split(OrientedPoint_Old.fromPointAndDirection(Vector1D.of(0.899), true, otherPrecision));
        Assert.assertNull(plusSplit.getMinus());
        Assert.assertNotNull(plusSplit.getPlus());

        SplitSubHyperplane<Vector1D> lowWithinTolerance = pt.split(OrientedPoint_Old.fromPointAndDirection(Vector1D.of(0.901), true, otherPrecision));
        Assert.assertNull(lowWithinTolerance.getMinus());
        Assert.assertNull(lowWithinTolerance.getPlus());

        SplitSubHyperplane<Vector1D> highWithinTolerance = pt.split(OrientedPoint_Old.fromPointAndDirection(Vector1D.of(1.09), true, otherPrecision));
        Assert.assertNull(highWithinTolerance.getMinus());
        Assert.assertNull(highWithinTolerance.getPlus());

        SplitSubHyperplane<Vector1D> minusSplit = pt.split(OrientedPoint_Old.fromPointAndDirection(Vector1D.of(1.101), true, otherPrecision));
        Assert.assertNotNull(minusSplit.getMinus());
        Assert.assertNull(minusSplit.getPlus());
    }
}
