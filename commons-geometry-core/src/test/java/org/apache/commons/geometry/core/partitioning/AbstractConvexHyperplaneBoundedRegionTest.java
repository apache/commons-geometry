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
package org.apache.commons.geometry.core.partitioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.exception.GeometryException;
import org.apache.commons.geometry.core.partition.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partition.test.TestLine;
import org.apache.commons.geometry.core.partition.test.TestLineSegment;
import org.apache.commons.geometry.core.partition.test.TestPoint2D;
import org.apache.commons.geometry.core.partitioning.AbstractConvexHyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.junit.Assert;
import org.junit.Test;

public class AbstractConvexHyperplaneBoundedRegionTest {

    @Test
    public void testBoundaries_areUnmodifiable() {
        // arrange
        StubRegion region = new StubRegion(new ArrayList<>());

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            region.getBoundaries().add(TestLine.X_AXIS.span());
        }, UnsupportedOperationException.class);
    }

    @Test
    public void testFull() {
        // act
        StubRegion region = new StubRegion(Collections.emptyList());

        // assert
        Assert.assertTrue(region.isFull());
        Assert.assertFalse(region.isEmpty());
    }

    @Test
    public void testGetBoundarySize() {
        // arrange
        TestPoint2D p1 = new TestPoint2D(1, 0);
        TestPoint2D p2 = new TestPoint2D(2, 0);
        TestPoint2D p3 = new TestPoint2D(1, 1);

        // act/assert
        Assert.assertEquals(0, new StubRegion(Collections.emptyList()).getBoundarySize(), PartitionTestUtils.EPS);
        GeometryTestUtils.assertPositiveInfinity(new StubRegion(Arrays.asList(TestLine.X_AXIS.span())).getBoundarySize());
        Assert.assertEquals(2 + Math.sqrt(2), new StubRegion(Arrays.asList(
                    new TestLineSegment(p1, p2),
                    new TestLineSegment(p2, p3),
                    new TestLineSegment(p3, p1)
                )).getBoundarySize(), PartitionTestUtils.EPS);
    }

    @Test
    public void testClassify() {
        // arrange
        TestPoint2D p1 = new TestPoint2D(1, 0);
        TestPoint2D p2 = new TestPoint2D(2, 0);
        TestPoint2D p3 = new TestPoint2D(1, 1);

        StubRegion full = new StubRegion(Collections.emptyList());
        StubRegion halfSpace = new StubRegion(Arrays.asList(TestLine.X_AXIS.span()));
        StubRegion triangle = new StubRegion(Arrays.asList(
                new TestLineSegment(p1, p2),
                new TestLineSegment(p2, p3),
                new TestLineSegment(p3, p1)
            ));

        // act/assert
        checkClassify(full, RegionLocation.INSIDE, TestPoint2D.ZERO, p1, p2, p3);

        checkClassify(halfSpace, RegionLocation.INSIDE, new TestPoint2D(0, 1));
        checkClassify(halfSpace, RegionLocation.OUTSIDE, new TestPoint2D(0, -1));
        checkClassify(halfSpace, RegionLocation.BOUNDARY,
                new TestPoint2D(-1, 0), new TestPoint2D(0, 0), new TestPoint2D(1, 0));

        checkClassify(triangle, RegionLocation.INSIDE, new TestPoint2D(1.25, 0.25));
        checkClassify(triangle, RegionLocation.OUTSIDE, new TestPoint2D(-1, 0), new TestPoint2D(0, 0), new TestPoint2D(3, 0));
        checkClassify(triangle, RegionLocation.BOUNDARY, p1, p2, p3);
    }

    @Test
    public void testProject() {
        // arrange
        TestPoint2D p1 = new TestPoint2D(1, 0);
        TestPoint2D p2 = new TestPoint2D(2, 0);
        TestPoint2D p3 = new TestPoint2D(1, 1);

        StubRegion full = new StubRegion(Collections.emptyList());
        StubRegion halfSpace = new StubRegion(Arrays.asList(TestLine.X_AXIS.span()));
        StubRegion triangle = new StubRegion(Arrays.asList(
                new TestLineSegment(p1, p2),
                new TestLineSegment(p2, p3),
                new TestLineSegment(p3, p1)
            ));

        // act/assert
        Assert.assertNull(full.project(TestPoint2D.ZERO));
        Assert.assertNull(full.project(new TestPoint2D(1, 1)));

        PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, halfSpace.project(new TestPoint2D(0, 1)));
        PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, halfSpace.project(new TestPoint2D(0, 0)));
        PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, halfSpace.project(new TestPoint2D(0, -1)));

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(1.25, 0), triangle.project(new TestPoint2D(1.25, 0.1)));
        PartitionTestUtils.assertPointsEqual(p1, triangle.project(TestPoint2D.ZERO));
        PartitionTestUtils.assertPointsEqual(p3, triangle.project(new TestPoint2D(0, 10)));
    }

    @Test
    public void testTrim() {
        // arrange
        TestPoint2D p1 = new TestPoint2D(1, 0);
        TestPoint2D p2 = new TestPoint2D(2, 0);
        TestPoint2D p3 = new TestPoint2D(2, 1);
        TestPoint2D p4 = new TestPoint2D(1, 1);

        StubRegion full = new StubRegion(Collections.emptyList());
        StubRegion halfSpace = new StubRegion(Arrays.asList(TestLine.Y_AXIS.span()));
        StubRegion square = new StubRegion(Arrays.asList(
                new TestLineSegment(p1, p2),
                new TestLineSegment(p2, p3),
                new TestLineSegment(p3, p4),
                new TestLineSegment(p4, p1)
            ));

        TestLineSegment segment = new TestLineSegment(new TestPoint2D(-1, 0.5), new TestPoint2D(4, 0.5));

        // act/assert
        Assert.assertSame(segment, full.trim(segment));

        TestLineSegment trimmedA = halfSpace.trim(segment);
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(-1, 0.5), trimmedA.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 0.5), trimmedA.getEndPoint());

        TestLineSegment trimmedB = square.trim(segment);
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(1, 0.5), trimmedB.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(2, 0.5), trimmedB.getEndPoint());
    }

    @Test
    public void testSplit_full() {
        // arrange
        StubRegion region = new StubRegion(Collections.emptyList());

        TestLine splitter = TestLine.X_AXIS;

        // act
        Split<StubRegion> split = region.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        StubRegion minus = split.getMinus();
        Assert.assertEquals(1, minus.getBoundaries().size());
        checkClassify(minus, RegionLocation.INSIDE, new TestPoint2D(0, 1));
        checkClassify(minus, RegionLocation.BOUNDARY, new TestPoint2D(0, 0));
        checkClassify(minus, RegionLocation.OUTSIDE, new TestPoint2D(0, -1));

        StubRegion plus = split.getPlus();
        Assert.assertEquals(1, plus.getBoundaries().size());
        checkClassify(plus, RegionLocation.OUTSIDE, new TestPoint2D(0, 1));
        checkClassify(plus, RegionLocation.BOUNDARY, new TestPoint2D(0, 0));
        checkClassify(plus, RegionLocation.INSIDE, new TestPoint2D(0, -1));
    }

    @Test
    public void testSplit_parallel_plusOnly() {
     // arrange
        StubRegion region = new StubRegion(
                Arrays.asList(new TestLineSegment(new TestPoint2D(0, 1), new TestPoint2D(1, 1))));

        TestLine splitter = TestLine.X_AXIS.reverse();

        // act
        Split<StubRegion> split = region.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(region, split.getPlus());
    }

    @Test
    public void testSplit_parallel_minusOnly() {
     // arrange
        StubRegion region = new StubRegion(
                Arrays.asList(new TestLineSegment(new TestPoint2D(0, 1), new TestPoint2D(1, 1))));

        TestLine splitter = TestLine.X_AXIS;

        // act
        Split<StubRegion> split = region.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(region, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_coincident_sameOrientation() {
     // arrange
        StubRegion region = new StubRegion(Arrays.asList(TestLine.X_AXIS.span()));

        TestLine splitter = TestLine.X_AXIS;

        // act
        Split<StubRegion> split = region.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(region, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_coincident_oppositeOrientation() {
     // arrange
        StubRegion region = new StubRegion(Arrays.asList(TestLine.X_AXIS.span()));

        TestLine splitter = TestLine.X_AXIS.reverse();

        // act
        Split<StubRegion> split = region.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(region, split.getPlus());
    }

    @Test
    public void testSplit_finite_both() {
        // arrange
        TestPoint2D p1 = new TestPoint2D(1, -0.5);
        TestPoint2D p2 = new TestPoint2D(2, -0.5);
        TestPoint2D p3 = new TestPoint2D(2, 0.5);
        TestPoint2D p4 = new TestPoint2D(1, 0.5);

        StubRegion region = new StubRegion(Arrays.asList(
                    new TestLineSegment(p1, p2),
                    new TestLineSegment(p2, p3),
                    new TestLineSegment(p3, p4),
                    new TestLineSegment(p4, p1)
                ));

        TestLine splitter = TestLine.X_AXIS;

        // act
        Split<StubRegion> split = region.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        StubRegion minus = split.getMinus();
        Assert.assertEquals(4, minus.getBoundaries().size());
        checkClassify(minus, RegionLocation.INSIDE, new TestPoint2D(1.5, 0.25));
        checkClassify(minus, RegionLocation.BOUNDARY, new TestPoint2D(1.5, 0));
        checkClassify(minus, RegionLocation.OUTSIDE, new TestPoint2D(1.5, -0.25));

        StubRegion plus = split.getPlus();
        Assert.assertEquals(4, plus.getBoundaries().size());
        checkClassify(plus, RegionLocation.OUTSIDE, new TestPoint2D(1.5, 0.25));
        checkClassify(plus, RegionLocation.BOUNDARY, new TestPoint2D(1.5, 0));
        checkClassify(plus, RegionLocation.INSIDE, new TestPoint2D(1.5, -0.25));
    }

    @Test
    public void testTransform_full() {
        // arrange
        StubRegion region = new StubRegion(Collections.emptyList());

        Transform<TestPoint2D> transform = p -> new TestPoint2D(p.getX() + 1, p.getY() + 2);

        // act
        StubRegion transformed = region.transform(transform);

        // assert
        Assert.assertTrue(transformed.isFull());
        Assert.assertFalse(transformed.isEmpty());
    }

    @Test
    public void testTransform_infinite() {
        // arrange
        TestLine line = TestLine.Y_AXIS;

        StubRegion region = new StubRegion(Arrays.asList(
                line.span()
            ));

        Transform<TestPoint2D> transform = p -> new TestPoint2D(p.getX() + 1, p.getY() + 2);

        // act
        StubRegion transformed = region.transform(transform);

        // assert
        List<TestLineSegment> boundaries = transformed.getBoundaries();

        Assert.assertEquals(1, boundaries.size());

        TestLineSegment a = boundaries.get(0);
        TestLine aLine = a.getHyperplane();
        PartitionTestUtils.assertPointsEqual(aLine.getOrigin(), new TestPoint2D(1, 0));
        Assert.assertEquals(0.0, aLine.getDirectionX(), PartitionTestUtils.EPS);
        Assert.assertEquals(1.0, aLine.getDirectionY(), PartitionTestUtils.EPS);

        GeometryTestUtils.assertNegativeInfinity(a.getStart());
        GeometryTestUtils.assertPositiveInfinity(a.getEnd());
    }

    @Test
    public void testTransform_finite() {
        // arrange
        TestPoint2D p1 = new TestPoint2D(1, 0);
        TestPoint2D p2 = new TestPoint2D(2, 0);
        TestPoint2D p3 = new TestPoint2D(1, 1);

        StubRegion region = new StubRegion(Arrays.asList(
                new TestLineSegment(p1, p2),
                new TestLineSegment(p2, p3),
                new TestLineSegment(p3, p1)
            ));

        Transform<TestPoint2D> transform = p -> new TestPoint2D(p.getX() + 1, p.getY() + 2);

        // act
        StubRegion transformed = region.transform(transform);

        // assert
        List<TestLineSegment> boundaries = transformed.getBoundaries();

        Assert.assertEquals(3, boundaries.size());

        TestLineSegment a = boundaries.get(0);
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(2, 2), a.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(3, 2), a.getEndPoint());

        TestLineSegment b = boundaries.get(1);
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(3, 2), b.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(2, 3), b.getEndPoint());

        TestLineSegment c = boundaries.get(2);
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(2, 3), c.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(2, 2), c.getEndPoint());
    }

    @Test
    public void testTransform_reflection() {
        // arrange
        TestPoint2D p1 = new TestPoint2D(1, 0);
        TestPoint2D p2 = new TestPoint2D(2, 0);
        TestPoint2D p3 = new TestPoint2D(1, 1);

        StubRegion region = new StubRegion(Arrays.asList(
                new TestLineSegment(p1, p2),
                new TestLineSegment(p2, p3),
                new TestLineSegment(p3, p1)
            ));

        Transform<TestPoint2D> transform = p -> new TestPoint2D(-p.getX(), p.getY());

        // act
        StubRegion transformed = region.transform(transform);

        // assert
        List<TestLineSegment> boundaries = transformed.getBoundaries();

        Assert.assertEquals(3, boundaries.size());

        TestLineSegment a = boundaries.get(0);
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(-2, 0), a.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(-1, 0), a.getEndPoint());

        TestLineSegment b = boundaries.get(1);
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(-1, 1), b.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(-2, 0), b.getEndPoint());

        TestLineSegment c = boundaries.get(2);
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(-1, 0), c.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(-1, 1), c.getEndPoint());
    }

    @Test
    public void testConvexRegionBoundaryBuilder_full() {
        // act
        StubRegion region = StubRegion.fromBounds(Collections.emptyList());

        // assert
        Assert.assertSame(StubRegion.FULL, region);
    }

    @Test
    public void testConvexRegionBoundaryBuilder_singleLine() {
        // act
        StubRegion region = StubRegion.fromBounds(Arrays.asList(TestLine.Y_AXIS));

        // assert
        Assert.assertEquals(1, region.getBoundaries().size());

        checkClassify(region, RegionLocation.INSIDE, new TestPoint2D(-1, 0));
        checkClassify(region, RegionLocation.BOUNDARY, new TestPoint2D(0, 0));
        checkClassify(region, RegionLocation.OUTSIDE, new TestPoint2D(1, 0));
    }

    @Test
    public void testConvexRegionBoundaryBuilder_multipleLines() {
        // act
        StubRegion region = StubRegion.fromBounds(Arrays.asList(
                    TestLine.X_AXIS,
                    new TestLine(new TestPoint2D(1, 0), new TestPoint2D(0, 1)),
                    TestLine.Y_AXIS.reverse()
                ));

        // assert
        Assert.assertEquals(3, region.getBoundaries().size());

        checkClassify(region, RegionLocation.INSIDE, new TestPoint2D(0.25, 0.25));

        checkClassify(region, RegionLocation.BOUNDARY,
                TestPoint2D.ZERO, new TestPoint2D(1, 0), new TestPoint2D(1, 0), new TestPoint2D(0.5, 0.5));

        checkClassify(region, RegionLocation.OUTSIDE,
                new TestPoint2D(-1, 0.5), new TestPoint2D(1, 0.5),
                new TestPoint2D(0.5, 1), new TestPoint2D(0.5, -1));
    }

    @Test
    public void testConvexRegionBoundaryBuilder_duplicateLines() {
        // act
        StubRegion region = StubRegion.fromBounds(Arrays.asList(
                TestLine.Y_AXIS,
                TestLine.Y_AXIS,
                new TestLine(new TestPoint2D(0, 0), new TestPoint2D(0, 1)),
                TestLine.Y_AXIS));

        // assert
        Assert.assertEquals(1, region.getBoundaries().size());

        checkClassify(region, RegionLocation.INSIDE, new TestPoint2D(-1, 0));
        checkClassify(region, RegionLocation.BOUNDARY, new TestPoint2D(0, 0));
        checkClassify(region, RegionLocation.OUTSIDE, new TestPoint2D(1, 0));
    }

    @Test
    public void testConvexRegionBoundaryBuilder() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            StubRegion.fromBounds(Arrays.asList(TestLine.X_AXIS, TestLine.X_AXIS.reverse()));
        }, GeometryException.class);

        GeometryTestUtils.assertThrows(() -> {
            StubRegion.fromBounds(Arrays.asList(
                    TestLine.X_AXIS,
                    TestLine.Y_AXIS,
                    new TestLine(new TestPoint2D(1, 0), new TestPoint2D(0, -1))));
        }, GeometryException.class);
    }

    @Test
    public void testToString() {
        // arrange
        StubRegion region = new StubRegion(Collections.emptyList());

        // act
        String str = region.toString();

        // assert
        Assert.assertTrue(str.contains("StubRegion"));
        Assert.assertTrue(str.contains("boundaries= "));
    }

    private static void checkClassify(Region<TestPoint2D> region, RegionLocation loc, TestPoint2D ... pts) {
        for (TestPoint2D pt : pts) {
            Assert.assertEquals("Unexpected location for point " + pt, loc, region.classify(pt));
        }
    }

    private static final class StubRegion extends AbstractConvexHyperplaneBoundedRegion<TestPoint2D, TestLineSegment>{

        private static final long serialVersionUID = 1L;

        private static final StubRegion FULL = new StubRegion(Collections.emptyList());

        StubRegion(List<TestLineSegment> boundaries) {
            super(boundaries);
        }

        @Override
        public StubRegion transform(Transform<TestPoint2D> transform) {
            return transformInternal(transform, this, TestLineSegment.class, StubRegion::new);
        }

        @Override
        public Split<StubRegion> split(Hyperplane<TestPoint2D> splitter) {
            return splitInternal(splitter, this, TestLineSegment.class, StubRegion::new);
        }

        @Override
        public TestLineSegment trim(ConvexSubHyperplane<TestPoint2D> convexSubHyperplane) {
            return (TestLineSegment) super.trim(convexSubHyperplane);
        }

        @Override
        public List<StubRegion> toConvex() {
            return Arrays.asList(this);
        }

        @Override
        public double getSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public TestPoint2D getBarycenter() {
            throw new UnsupportedOperationException();
        }

        public static StubRegion fromBounds(Iterable<TestLine> boundingLines) {
            final List<TestLineSegment> segments = new ConvexRegionBoundaryBuilder<>(TestLineSegment.class).build(boundingLines);
            return segments.isEmpty() ? FULL : new StubRegion(segments);
        }
    }
}
