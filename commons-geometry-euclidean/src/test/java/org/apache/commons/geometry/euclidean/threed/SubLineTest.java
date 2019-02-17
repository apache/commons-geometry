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
package org.apache.commons.geometry.euclidean.threed;

import java.util.List;

import org.apache.commons.geometry.core.partitioning.RegionFactory_Old;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.junit.Assert;
import org.junit.Test;

public class SubLineTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testEndPoints() {
        Vector3D p1 = Vector3D.of(-1, -7, 2);
        Vector3D p2 = Vector3D.of(7, -1, 0);
        Segment segment = new Segment(p1, p2, new Line(p1, p2, TEST_PRECISION));
        SubLine sub = new SubLine(segment);
        List<Segment> segments = sub.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertEquals(0.0, Vector3D.of(-1, -7, 2).distance(segments.get(0).getStart()), TEST_EPS);
        Assert.assertEquals(0.0, Vector3D.of( 7, -1, 0).distance(segments.get(0).getEnd()), TEST_EPS);
    }

    @Test
    public void testNoEndPoints() {
        SubLine wholeLine = new Line(Vector3D.of(-1, 7, 2), Vector3D.of(7, 1, 0), TEST_PRECISION).wholeLine();
        List<Segment> segments = wholeLine.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getX()) &&
                          segments.get(0).getStart().getX() < 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getY()) &&
                          segments.get(0).getStart().getY() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getZ()) &&
                          segments.get(0).getStart().getZ() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getX()) &&
                          segments.get(0).getEnd().getX() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getY()) &&
                          segments.get(0).getEnd().getY() < 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getZ()) &&
                          segments.get(0).getEnd().getZ() < 0);
    }

    @Test
    public void testNoSegments() {
        SubLine empty = new SubLine(new Line(Vector3D.of(-1, -7, 2), Vector3D.of(7, -1, 0), TEST_PRECISION),
                                    (IntervalsSet) new RegionFactory_Old<Vector1D>().getComplement(new IntervalsSet(TEST_PRECISION)));
        List<Segment> segments = empty.getSegments();
        Assert.assertEquals(0, segments.size());
    }

    @Test
    public void testSeveralSegments() {
        SubLine twoSubs = new SubLine(new Line(Vector3D.of(-1, -7, 2), Vector3D.of(7, -1, 0), TEST_PRECISION),
                                      (IntervalsSet) new RegionFactory_Old<Vector1D>().union(new IntervalsSet(1, 2, TEST_PRECISION),
                                                                                            new IntervalsSet(3, 4, TEST_PRECISION)));
        List<Segment> segments = twoSubs.getSegments();
        Assert.assertEquals(2, segments.size());
    }

    @Test
    public void testHalfInfiniteNeg() {
        SubLine empty = new SubLine(new Line(Vector3D.of(-1, -7, 2), Vector3D.of(7, -1, -2), TEST_PRECISION),
                                    new IntervalsSet(Double.NEGATIVE_INFINITY, 0.0, TEST_PRECISION));
        List<Segment> segments = empty.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getX()) &&
                          segments.get(0).getStart().getX() < 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getY()) &&
                          segments.get(0).getStart().getY() < 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getZ()) &&
                          segments.get(0).getStart().getZ() > 0);
        Assert.assertEquals(0.0, Vector3D.of(3, -4, 0).distance(segments.get(0).getEnd()), TEST_EPS);
    }

    @Test
    public void testHalfInfinitePos() {
        SubLine empty = new SubLine(new Line(Vector3D.of(-1, -7, 2), Vector3D.of(7, -1, -2), TEST_PRECISION),
                                    new IntervalsSet(0.0, Double.POSITIVE_INFINITY, TEST_PRECISION));
        List<Segment> segments = empty.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertEquals(0.0, Vector3D.of(3, -4, 0).distance(segments.get(0).getStart()), 1.0e-10);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getX()) &&
                          segments.get(0).getEnd().getX() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getY()) &&
                          segments.get(0).getEnd().getY() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getZ()) &&
                          segments.get(0).getEnd().getZ() < 0);
    }

    @Test
    public void testIntersectionInsideInside() {
        SubLine sub1 = new SubLine(Vector3D.of(1, 1, 1), Vector3D.of(3, 1, 1), TEST_PRECISION);
        SubLine sub2 = new SubLine(Vector3D.of(2, 0, 0), Vector3D.of(2, 2, 2), TEST_PRECISION);
        Assert.assertEquals(0.0, Vector3D.of(2, 1, 1).distance(sub1.intersection(sub2, true)), TEST_EPS);
        Assert.assertEquals(0.0, Vector3D.of(2, 1, 1).distance(sub1.intersection(sub2, false)), TEST_EPS);
    }

    @Test
    public void testIntersectionInsideBoundary() {
        SubLine sub1 = new SubLine(Vector3D.of(1, 1, 1), Vector3D.of(3, 1, 1), TEST_PRECISION);
        SubLine sub2 = new SubLine(Vector3D.of(2, 0, 0), Vector3D.of(2, 1, 1), TEST_PRECISION);
        Assert.assertEquals(0.0, Vector3D.of(2, 1, 1).distance(sub1.intersection(sub2, true)), TEST_EPS);
        Assert.assertNull(sub1.intersection(sub2, false));
    }

    @Test
    public void testIntersectionInsideOutside() {
        SubLine sub1 = new SubLine(Vector3D.of(1, 1, 1), Vector3D.of(3, 1, 1), TEST_PRECISION);
        SubLine sub2 = new SubLine(Vector3D.of(2, 0, 0), Vector3D.of(2, 0.5, 0.5), TEST_PRECISION);
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

    @Test
    public void testIntersectionBoundaryBoundary() {
        SubLine sub1 = new SubLine(Vector3D.of(1, 1, 1), Vector3D.of(2, 1, 1), TEST_PRECISION);
        SubLine sub2 = new SubLine(Vector3D.of(2, 0, 0), Vector3D.of(2, 1, 1), TEST_PRECISION);
        Assert.assertEquals(0.0, Vector3D.of(2, 1, 1).distance(sub1.intersection(sub2, true)),  TEST_EPS);
        Assert.assertNull(sub1.intersection(sub2, false));
    }

    @Test
    public void testIntersectionBoundaryOutside() {
        SubLine sub1 = new SubLine(Vector3D.of(1, 1, 1), Vector3D.of(2, 1, 1), TEST_PRECISION);
        SubLine sub2 = new SubLine(Vector3D.of(2, 0, 0), Vector3D.of(2, 0.5, 0.5), TEST_PRECISION);
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

    @Test
    public void testIntersectionOutsideOutside() {
        SubLine sub1 = new SubLine(Vector3D.of(1, 1, 1), Vector3D.of(1.5, 1, 1), TEST_PRECISION);
        SubLine sub2 = new SubLine(Vector3D.of(2, 0, 0), Vector3D.of(2, 0.5, 0.5), TEST_PRECISION);
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

    @Test
    public void testIntersectionNotIntersecting() {
        SubLine sub1 = new SubLine(Vector3D.of(1, 1, 1), Vector3D.of(1.5, 1, 1), TEST_PRECISION);
        SubLine sub2 = new SubLine(Vector3D.of(2, 3, 0), Vector3D.of(2, 3, 0.5), TEST_PRECISION);
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

}
