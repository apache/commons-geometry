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
package org.apache.commons.geometry.euclidean.twod;

import java.util.List;

import org.apache.commons.geometry.core.partitioning.RegionFactory_Old;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.junit.Assert;
import org.junit.Test;

public class SubLineTest_Old {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testEndPoints() {
        Vector2D p1 = Vector2D.of(-1, -7);
        Vector2D p2 = Vector2D.of(7, -1);
        Segment_Old segment = new Segment_Old(p1, p2, Line_Old.fromPoints(p1, p2, TEST_PRECISION));
        SubLine_Old sub = new SubLine_Old(segment);
        List<Segment_Old> segments = sub.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertEquals(0.0, Vector2D.of(-1, -7).distance(segments.get(0).getStart()), TEST_EPS);
        Assert.assertEquals(0.0, Vector2D.of( 7, -1).distance(segments.get(0).getEnd()), TEST_EPS);
    }

    @Test
    public void testNoEndPoints() {
        SubLine_Old wholeLine = Line_Old.fromPoints(Vector2D.of(-1, 7), Vector2D.of(7, 1), TEST_PRECISION).wholeHyperplane();
        List<Segment_Old> segments = wholeLine.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getX()) &&
                          segments.get(0).getStart().getX() < 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getY()) &&
                          segments.get(0).getStart().getY() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getX()) &&
                          segments.get(0).getEnd().getX() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getY()) &&
                          segments.get(0).getEnd().getY() < 0);
    }

    @Test
    public void testNoSegments() {
        SubLine_Old empty = new SubLine_Old(Line_Old.fromPoints(Vector2D.of(-1, -7), Vector2D.of(7, -1), TEST_PRECISION),
                                    new RegionFactory_Old<Vector1D>().getComplement(new IntervalsSet(TEST_PRECISION)));
        List<Segment_Old> segments = empty.getSegments();
        Assert.assertEquals(0, segments.size());
    }

    @Test
    public void testSeveralSegments() {
        SubLine_Old twoSubs = new SubLine_Old(Line_Old.fromPoints(Vector2D.of(-1, -7), Vector2D.of(7, -1), TEST_PRECISION),
                                    new RegionFactory_Old<Vector1D>().union(new IntervalsSet(1, 2, TEST_PRECISION),
                                                                           new IntervalsSet(3, 4, TEST_PRECISION)));
        List<Segment_Old> segments = twoSubs.getSegments();
        Assert.assertEquals(2, segments.size());
    }

    @Test
    public void testHalfInfiniteNeg() {
        SubLine_Old empty = new SubLine_Old(Line_Old.fromPoints(Vector2D.of(-1, -7), Vector2D.of(7, -1), TEST_PRECISION),
                                    new IntervalsSet(Double.NEGATIVE_INFINITY, 0.0, TEST_PRECISION));
        List<Segment_Old> segments = empty.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getX()) &&
                          segments.get(0).getStart().getX() < 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getY()) &&
                          segments.get(0).getStart().getY() < 0);
        Assert.assertEquals(0.0, Vector2D.of(3, -4).distance(segments.get(0).getEnd()), TEST_EPS);
    }

    @Test
    public void testHalfInfinitePos() {
        SubLine_Old empty = new SubLine_Old(Line_Old.fromPoints(Vector2D.of(-1, -7), Vector2D.of(7, -1), TEST_PRECISION),
                                    new IntervalsSet(0.0, Double.POSITIVE_INFINITY, TEST_PRECISION));
        List<Segment_Old> segments = empty.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertEquals(0.0, Vector2D.of(3, -4).distance(segments.get(0).getStart()), TEST_EPS);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getX()) &&
                          segments.get(0).getEnd().getX() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getY()) &&
                          segments.get(0).getEnd().getY() > 0);
    }

    @Test
    public void testIntersectionInsideInside() {
        SubLine_Old sub1 = new SubLine_Old(Vector2D.of(1, 1), Vector2D.of(3, 1), TEST_PRECISION);
        SubLine_Old sub2 = new SubLine_Old(Vector2D.of(2, 0), Vector2D.of(2, 2), TEST_PRECISION);
        Assert.assertEquals(0.0, Vector2D.of(2, 1).distance(sub1.intersection(sub2, true)), TEST_EPS);
        Assert.assertEquals(0.0, Vector2D.of(2, 1).distance(sub1.intersection(sub2, false)), TEST_EPS);
    }

    @Test
    public void testIntersectionInsideBoundary() {
        SubLine_Old sub1 = new SubLine_Old(Vector2D.of(1, 1), Vector2D.of(3, 1), TEST_PRECISION);
        SubLine_Old sub2 = new SubLine_Old(Vector2D.of(2, 0), Vector2D.of(2, 1), TEST_PRECISION);
        Assert.assertEquals(0.0, Vector2D.of(2, 1).distance(sub1.intersection(sub2, true)), TEST_EPS);
        Assert.assertNull(sub1.intersection(sub2, false));
    }

    @Test
    public void testIntersectionInsideOutside() {
        SubLine_Old sub1 = new SubLine_Old(Vector2D.of(1, 1), Vector2D.of(3, 1), TEST_PRECISION);
        SubLine_Old sub2 = new SubLine_Old(Vector2D.of(2, 0), Vector2D.of(2, 0.5), TEST_PRECISION);
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

    @Test
    public void testIntersectionBoundaryBoundary() {
        SubLine_Old sub1 = new SubLine_Old(Vector2D.of(1, 1), Vector2D.of(2, 1), TEST_PRECISION);
        SubLine_Old sub2 = new SubLine_Old(Vector2D.of(2, 0), Vector2D.of(2, 1), TEST_PRECISION);
        Assert.assertEquals(0.0, Vector2D.of(2, 1).distance(sub1.intersection(sub2, true)), TEST_EPS);
        Assert.assertNull(sub1.intersection(sub2, false));
    }

    @Test
    public void testIntersectionBoundaryOutside() {
        SubLine_Old sub1 = new SubLine_Old(Vector2D.of(1, 1), Vector2D.of(2, 1), TEST_PRECISION);
        SubLine_Old sub2 = new SubLine_Old(Vector2D.of(2, 0), Vector2D.of(2, 0.5), TEST_PRECISION);
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

    @Test
    public void testIntersectionOutsideOutside() {
        SubLine_Old sub1 = new SubLine_Old(Vector2D.of(1, 1), Vector2D.of(1.5, 1), TEST_PRECISION);
        SubLine_Old sub2 = new SubLine_Old(Vector2D.of(2, 0), Vector2D.of(2, 0.5), TEST_PRECISION);
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

    @Test
    public void testIntersectionParallel() {
        final SubLine_Old sub1 = new SubLine_Old(Vector2D.of(0, 1), Vector2D.of(0, 2), TEST_PRECISION);
        final SubLine_Old sub2 = new SubLine_Old(Vector2D.of(66, 3), Vector2D.of(66, 4), TEST_PRECISION);
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

}
