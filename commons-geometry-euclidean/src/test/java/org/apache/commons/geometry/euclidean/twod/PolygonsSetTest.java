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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.partitioning.BSPTree;
import org.apache.commons.geometry.core.partitioning.BSPTreeVisitor;
import org.apache.commons.geometry.core.partitioning.BoundaryProjection;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Region;
import org.apache.commons.geometry.core.partitioning.Region.Location;
import org.apache.commons.geometry.core.partitioning.RegionFactory;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.numbers.core.Precision;
import org.junit.Assert;
import org.junit.Test;

public class PolygonsSetTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFull() {
        // act
        PolygonsSet poly = new PolygonsSet(TEST_PRECISION);

        // assert
        Assert.assertSame(TEST_PRECISION, poly.getPrecision());
        EuclideanTestUtils.assertPositiveInfinity(poly.getSize());
        Assert.assertEquals(0.0, poly.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(0, poly.getVertices().length);
        Assert.assertFalse(poly.isEmpty());
        Assert.assertTrue(poly.isFull());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.NaN, poly.getBarycenter(), TEST_EPS);

        checkPoints(Region.Location.INSIDE, poly,
                Vector2D.of(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY),
                Vector2D.ZERO,
                Vector2D.of(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));

        for (double y = -1; y < 1; y += 0.1) {
            for (double x = -1; x < 1; x += 0.1) {
                EuclideanTestUtils.assertNegativeInfinity(poly.projectToBoundary(Vector2D.of(x, y)).getOffset());
            }
        }
    }

    @Test
    public void testEmpty() {
        // act
        PolygonsSet poly = (PolygonsSet) new RegionFactory<Vector2D>().getComplement(new PolygonsSet(TEST_PRECISION));

        // assert
        Assert.assertSame(TEST_PRECISION, poly.getPrecision());
        Assert.assertEquals(0.0, poly.getSize(), TEST_EPS);
        Assert.assertEquals(0.0, poly.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(0, poly.getVertices().length);
        Assert.assertTrue(poly.isEmpty());
        Assert.assertFalse(poly.isFull());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.NaN, poly.getBarycenter(), TEST_EPS);

        checkPoints(Region.Location.OUTSIDE, poly,
                Vector2D.of(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY),
                Vector2D.ZERO,
                Vector2D.of(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));


        for (double y = -1; y < 1; y += 0.1) {
            for (double x = -1; x < 1; x += 0.1) {
                EuclideanTestUtils.assertPositiveInfinity(poly.projectToBoundary(Vector2D.of(x, y)).getOffset());
            }
        }
    }

    @Test
    public void testInfiniteLines_single() {
        // arrange
        Line line = new Line(Vector2D.of(0, 0), Vector2D.of(1, 1), TEST_PRECISION);

        List<SubHyperplane<Vector2D>> boundaries = new ArrayList<SubHyperplane<Vector2D>>();
        boundaries.add(line.wholeHyperplane());

        // act
        PolygonsSet poly = new PolygonsSet(boundaries, TEST_PRECISION);

        // assert
        Assert.assertSame(TEST_PRECISION, poly.getPrecision());
        EuclideanTestUtils.assertPositiveInfinity(poly.getSize());
        EuclideanTestUtils.assertPositiveInfinity(poly.getBoundarySize());
        Assert.assertFalse(poly.isEmpty());
        Assert.assertFalse(poly.isFull());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.NaN, poly.getBarycenter(), TEST_EPS);

        checkVertexLoopsEquivalent(new Vector2D[][] {
            {
                null,
                line.toSpace(Vector1D.of(-Float.MAX_VALUE)),
                line.toSpace(Vector1D.of(Float.MAX_VALUE))
            }
        }, poly.getVertices());

        checkPoints(Region.Location.OUTSIDE, poly,
                Vector2D.of(1, -1),
                Vector2D.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY));
        checkPoints(Region.Location.INSIDE, poly,
                Vector2D.of(-1, 1),
                Vector2D.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        checkPoints(Region.Location.BOUNDARY, poly, Vector2D.ZERO);
    }

    @Test
    public void testInfiniteLines_twoIntersecting() {
        // arrange
        Line line1 = new Line(Vector2D.of(0, 0), Vector2D.of(1, 1), TEST_PRECISION);
        Line line2 = new Line(Vector2D.of(1, -1), Vector2D.of(0, 0), TEST_PRECISION);

        List<SubHyperplane<Vector2D>> boundaries = new ArrayList<SubHyperplane<Vector2D>>();
        boundaries.add(line1.wholeHyperplane());
        boundaries.add(line2.wholeHyperplane());

        // act
        PolygonsSet poly = new PolygonsSet(boundaries, TEST_PRECISION);

        // assert
        Assert.assertSame(TEST_PRECISION, poly.getPrecision());
        EuclideanTestUtils.assertPositiveInfinity(poly.getSize());
        EuclideanTestUtils.assertPositiveInfinity(poly.getBoundarySize());
        Assert.assertFalse(poly.isEmpty());
        Assert.assertFalse(poly.isFull());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.NaN, poly.getBarycenter(), TEST_EPS);

        checkVertexLoopsEquivalent(new Vector2D[][] {
            {
                null,
                line2.toSpace(Vector1D.of(-Float.MAX_VALUE)),
                line2.toSpace(Vector1D.of(Float.MAX_VALUE))
            }
        }, poly.getVertices());

        checkPoints(Region.Location.INSIDE, poly,
                Vector2D.of(-1, 0),
                Vector2D.of(-Float.MAX_VALUE, Float.MAX_VALUE / 2.0));
        checkPoints(Region.Location.OUTSIDE, poly,
                Vector2D.of(1, 0),
                Vector2D.of(Float.MAX_VALUE, Float.MAX_VALUE / 2.0));
        checkPoints(Region.Location.BOUNDARY, poly, Vector2D.ZERO);
    }

    @Test
    public void testInfiniteLines_twoParallel_facingIn() {
        // arrange
        Line line1 = new Line(Vector2D.of(1, 1), Vector2D.of(0, 1), TEST_PRECISION);
        Line line2 = new Line(Vector2D.of(0, -1), Vector2D.of(1, -1), TEST_PRECISION);

        List<SubHyperplane<Vector2D>> boundaries = new ArrayList<SubHyperplane<Vector2D>>();
        boundaries.add(line1.wholeHyperplane());
        boundaries.add(line2.wholeHyperplane());

        // act
        PolygonsSet poly = new PolygonsSet(boundaries, TEST_PRECISION);

        // assert
        Assert.assertSame(TEST_PRECISION, poly.getPrecision());
        EuclideanTestUtils.assertPositiveInfinity(poly.getSize());
        EuclideanTestUtils.assertPositiveInfinity(poly.getBoundarySize());
        Assert.assertFalse(poly.isEmpty());
        Assert.assertFalse(poly.isFull());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.NaN, poly.getBarycenter(), TEST_EPS);

        checkVertexLoopsEquivalent(new Vector2D[][] {
            {
                null,
                line1.toSpace(Vector1D.of(-Float.MAX_VALUE)),
                line1.toSpace(Vector1D.of(Float.MAX_VALUE))
            },
            {
                null,
                line2.toSpace(Vector1D.of(-Float.MAX_VALUE)),
                line2.toSpace(Vector1D.of(Float.MAX_VALUE))
            }
        }, poly.getVertices());

        checkPoints(Region.Location.INSIDE, poly,
                Vector2D.of(0, 0),
                Vector2D.of(0, 0.9),
                Vector2D.of(0, -0.9));
        checkPoints(Region.Location.OUTSIDE, poly,
                Vector2D.of(0, 1.1),
                Vector2D.of(0, -1.1));
        checkPoints(Region.Location.BOUNDARY, poly,
                Vector2D.of(0, 1),
                Vector2D.of(0, -1));
    }

    @Test
    public void testInfiniteLines_twoParallel_facingOut() {
        // arrange
        Line line1 = new Line(Vector2D.of(0, 1), Vector2D.of(1, 1), TEST_PRECISION);
        Line line2 = new Line(Vector2D.of(1, -1), Vector2D.of(0, -1), TEST_PRECISION);

        List<SubHyperplane<Vector2D>> boundaries = new ArrayList<SubHyperplane<Vector2D>>();
        boundaries.add(line1.wholeHyperplane());
        boundaries.add(line2.wholeHyperplane());

        // act
        PolygonsSet poly = new PolygonsSet(boundaries, TEST_PRECISION);

        // assert
        Assert.assertSame(TEST_PRECISION, poly.getPrecision());
        EuclideanTestUtils.assertPositiveInfinity(poly.getSize());
        EuclideanTestUtils.assertPositiveInfinity(poly.getBoundarySize());
        Assert.assertFalse(poly.isEmpty());
        Assert.assertFalse(poly.isFull());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.NaN, poly.getBarycenter(), TEST_EPS);

        checkVertexLoopsEquivalent(new Vector2D[][] {
            {
                null,
                line1.toSpace(Vector1D.of(-Float.MAX_VALUE)),
                line1.toSpace(Vector1D.of(Float.MAX_VALUE))
            },
            {
                null,
                line2.toSpace(Vector1D.of(-Float.MAX_VALUE)),
                line2.toSpace(Vector1D.of(Float.MAX_VALUE))
            }
        }, poly.getVertices());

        checkPoints(Region.Location.OUTSIDE, poly,
                Vector2D.of(0, 0),
                Vector2D.of(0, 0.9),
                Vector2D.of(0, -0.9));
        checkPoints(Region.Location.INSIDE, poly,
                Vector2D.of(0, 1.1),
                Vector2D.of(0, -1.1));
        checkPoints(Region.Location.BOUNDARY, poly,
                Vector2D.of(0, 1),
                Vector2D.of(0, -1));
    }

    @Test
    public void testMixedFiniteAndInfiniteLines_explicitInfiniteBoundaries() {
        // arrange
        Line line1 = new Line(Vector2D.of(3, 3), Vector2D.of(0, 3), TEST_PRECISION);
        Line line2 = new Line(Vector2D.of(0, -3), Vector2D.of(3, -3), TEST_PRECISION);

        List<SubHyperplane<Vector2D>> boundaries = new ArrayList<SubHyperplane<Vector2D>>();
        boundaries.add(line1.wholeHyperplane());
        boundaries.add(line2.wholeHyperplane());
        boundaries.add(buildSegment(Vector2D.of(0, 3), Vector2D.of(0, -3)));

        // act
        PolygonsSet poly = new PolygonsSet(boundaries, TEST_PRECISION);

        // assert
        Assert.assertSame(TEST_PRECISION, poly.getPrecision());
        EuclideanTestUtils.assertPositiveInfinity(poly.getSize());
        EuclideanTestUtils.assertPositiveInfinity(poly.getBoundarySize());
        Assert.assertFalse(poly.isEmpty());
        Assert.assertFalse(poly.isFull());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.NaN, poly.getBarycenter(), TEST_EPS);

        checkVertexLoopsEquivalent(new Vector2D[][] {
            {
                null,
                Vector2D.of(1, 3), // dummy point
                Vector2D.of(0, 3),
                Vector2D.of(0, -3),
                Vector2D.of(1, -3) // dummy point
            }
        }, poly.getVertices());

        checkPoints(Region.Location.INSIDE, poly,
                Vector2D.of(0.1, 2.9),
                Vector2D.of(0.1, 0),
                Vector2D.of(0.1, -2.9));
        checkPoints(Region.Location.OUTSIDE, poly,
                Vector2D.of(0, 3.1),
                Vector2D.of(-0.5, 0),
                Vector2D.of(0, -3.1));
        checkPoints(Region.Location.BOUNDARY, poly,
                Vector2D.of(3, 3),
                Vector2D.of(0, 0),
                Vector2D.of(3, -3));
    }

    // The polygon in this test is created from finite boundaries but the generated
    // loop still begins and ends with infinite lines. This is because the boundaries
    // used as input do not form a closed region, therefore the region itself is unclosed.
    // In other words, the boundaries used as input only define the region, not the points
    // returned from the getVertices() method.
    @Test
    public void testMixedFiniteAndInfiniteLines_impliedInfiniteBoundaries() {
        // arrange
        Line line = new Line(Vector2D.of(3, 0), Vector2D.of(3, 3), TEST_PRECISION);

        List<SubHyperplane<Vector2D>> boundaries = new ArrayList<SubHyperplane<Vector2D>>();
        boundaries.add(buildSegment(Vector2D.of(0, 3), Vector2D.of(0, 0)));
        boundaries.add(buildSegment(Vector2D.of(0, 0), Vector2D.of(3, 0)));
        boundaries.add(new SubLine(line, new IntervalsSet(0, Double.POSITIVE_INFINITY, TEST_PRECISION)));

        // act
        PolygonsSet poly = new PolygonsSet(boundaries, TEST_PRECISION);

        // assert
        Assert.assertSame(TEST_PRECISION, poly.getPrecision());
        EuclideanTestUtils.assertPositiveInfinity(poly.getSize());
        EuclideanTestUtils.assertPositiveInfinity(poly.getBoundarySize());
        Assert.assertFalse(poly.isEmpty());
        Assert.assertFalse(poly.isFull());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.NaN, poly.getBarycenter(), TEST_EPS);

        checkVertexLoopsEquivalent(new Vector2D[][] {
            {
                null,
                Vector2D.of(0, 1), // dummy point
                Vector2D.of(0, 0),
                Vector2D.of(3, 0),
                Vector2D.of(3, 1) // dummy point
            }
        }, poly.getVertices());

        checkPoints(Region.Location.INSIDE, poly,
                Vector2D.of(0.1, Float.MAX_VALUE),
                Vector2D.of(0.1, 0.1),
                Vector2D.of(1.5, 0.1),
                Vector2D.of(2.9, 0.1),
                Vector2D.of(2.9, Float.MAX_VALUE));
        checkPoints(Region.Location.OUTSIDE, poly,
                Vector2D.of(-0.1, Float.MAX_VALUE),
                Vector2D.of(-0.1, 0.1),
                Vector2D.of(1.5, -0.1),
                Vector2D.of(3.1, 0.1),
                Vector2D.of(3.1, Float.MAX_VALUE));
        checkPoints(Region.Location.BOUNDARY, poly,
                Vector2D.of(0, 1),
                Vector2D.of(1, 0),
                Vector2D.of(3, 1));
    }

    @Test
    public void testBox() {
        // act
        PolygonsSet box = new PolygonsSet(0, 2, -1, 1, TEST_PRECISION);

        // assert
        Assert.assertEquals(4.0, box.getSize(), TEST_EPS);
        Assert.assertEquals(8.0, box.getBoundarySize(), TEST_EPS);
        Assert.assertFalse(box.isEmpty());
        Assert.assertFalse(box.isFull());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 0), box.getBarycenter(), TEST_EPS);

        checkVertexLoopsEquivalent(new Vector2D[][] {
            {
                Vector2D.of(2, -1),
                Vector2D.of(2, 1),
                Vector2D.of(0, 1),
                Vector2D.of(0, -1)
            }
        }, box.getVertices());

        checkPoints(Region.Location.INSIDE, box,
                Vector2D.of(0.1, 0),
                Vector2D.of(1.9, 0),
                Vector2D.of(1, 0.9),
                Vector2D.of(1, -0.9));
        checkPoints(Region.Location.OUTSIDE, box,
                Vector2D.of(-0.1, 0),
                Vector2D.of(2.1, 0),
                Vector2D.of(1, -1.1),
                Vector2D.of(1, 1.1));
        checkPoints(Region.Location.BOUNDARY, box,
                Vector2D.of(0, 0),
                Vector2D.of(2, 0),
                Vector2D.of(1, 1),
                Vector2D.of(1, -1));
    }

    @Test
    public void testInvertedBox() {
        // arrange
        List<SubHyperplane<Vector2D>> boundaries = new ArrayList<SubHyperplane<Vector2D>>();
        boundaries.add(buildSegment(Vector2D.of(0, -1), Vector2D.of(0, 1)));
        boundaries.add(buildSegment(Vector2D.of(2, 1), Vector2D.of(2, -1)));
        boundaries.add(buildSegment(Vector2D.of(0, 1), Vector2D.of(2, 1)));
        boundaries.add(buildSegment(Vector2D.of(2, -1), Vector2D.of(0, -1)));

        // act
        PolygonsSet box = new PolygonsSet(boundaries, TEST_PRECISION);

        // assert
        EuclideanTestUtils.assertPositiveInfinity(box.getSize());
        Assert.assertEquals(8.0, box.getBoundarySize(), TEST_EPS);
        Assert.assertFalse(box.isEmpty());
        Assert.assertFalse(box.isFull());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.NaN, box.getBarycenter(), TEST_EPS);

        checkVertexLoopsEquivalent(new Vector2D[][] {
            {
                Vector2D.of(0, -1),
                Vector2D.of(0, 1),
                Vector2D.of(2, 1),
                Vector2D.of(2, -1)
            }
        }, box.getVertices());

        checkPoints(Region.Location.OUTSIDE, box,
                Vector2D.of(0.1, 0),
                Vector2D.of(1.9, 0),
                Vector2D.of(1, 0.9),
                Vector2D.of(1, -0.9));
        checkPoints(Region.Location.INSIDE, box,
                Vector2D.of(-0.1, 0),
                Vector2D.of(2.1, 0),
                Vector2D.of(1, -1.1),
                Vector2D.of(1, 1.1));
        checkPoints(Region.Location.BOUNDARY, box,
                Vector2D.of(0, 0),
                Vector2D.of(2, 0),
                Vector2D.of(1, 1),
                Vector2D.of(1, -1));
    }

    @Test
    public void testSimplyConnected() {
        // arrange
        Vector2D[][] vertices = new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of(36.0, 22.0),
                Vector2D.of(39.0, 32.0),
                Vector2D.of(19.0, 32.0),
                Vector2D.of( 6.0, 16.0),
                Vector2D.of(31.0, 10.0),
                Vector2D.of(42.0, 16.0),
                Vector2D.of(34.0, 20.0),
                Vector2D.of(29.0, 19.0),
                Vector2D.of(23.0, 22.0),
                Vector2D.of(33.0, 25.0)
            }
        };

        // act
        PolygonsSet set = buildSet(vertices);

        // assert
        checkPoints(Region.Location.INSIDE, set,
            Vector2D.of(30.0, 15.0),
            Vector2D.of(15.0, 20.0),
            Vector2D.of(24.0, 25.0),
            Vector2D.of(35.0, 30.0),
            Vector2D.of(19.0, 17.0));
        checkPoints(Region.Location.OUTSIDE, set,
            Vector2D.of(50.0, 30.0),
            Vector2D.of(30.0, 35.0),
            Vector2D.of(10.0, 25.0),
            Vector2D.of(10.0, 10.0),
            Vector2D.of(40.0, 10.0),
            Vector2D.of(50.0, 15.0),
            Vector2D.of(30.0, 22.0));
        checkPoints(Region.Location.BOUNDARY, set,
            Vector2D.of(30.0, 32.0),
            Vector2D.of(34.0, 20.0));

        checkVertexLoopsEquivalent(vertices, set.getVertices());
    }

    @Test
    public void testStair() {
        // arrange
        Vector2D[][] vertices = new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of( 0.0, 0.0),
                Vector2D.of( 0.0, 2.0),
                Vector2D.of(-0.1, 2.0),
                Vector2D.of(-0.1, 1.0),
                Vector2D.of(-0.3, 1.0),
                Vector2D.of(-0.3, 1.5),
                Vector2D.of(-1.3, 1.5),
                Vector2D.of(-1.3, 2.0),
                Vector2D.of(-1.8, 2.0),
                Vector2D.of(-1.8 - 1.0 / Math.sqrt(2.0),
                            2.0 - 1.0 / Math.sqrt(2.0))
            }
        };

        // act
        PolygonsSet set = buildSet(vertices);

        // assert
        checkVertexLoopsEquivalent(vertices, set.getVertices());

        Assert.assertEquals(1.1 + 0.95 * Math.sqrt(2.0), set.getSize(), TEST_EPS);
    }

    @Test
    public void testHole() {
        // arrange
        Vector2D[][] vertices = new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of(0.0, 0.0),
                Vector2D.of(3.0, 0.0),
                Vector2D.of(3.0, 3.0),
                Vector2D.of(0.0, 3.0)
            }, new Vector2D[] {
                Vector2D.of(1.0, 2.0),
                Vector2D.of(2.0, 2.0),
                Vector2D.of(2.0, 1.0),
                Vector2D.of(1.0, 1.0)
            }
        };

        // act
        PolygonsSet set = buildSet(vertices);

        // assert
        checkPoints(Region.Location.INSIDE, set, new Vector2D[] {
            Vector2D.of(0.5, 0.5),
            Vector2D.of(1.5, 0.5),
            Vector2D.of(2.5, 0.5),
            Vector2D.of(0.5, 1.5),
            Vector2D.of(2.5, 1.5),
            Vector2D.of(0.5, 2.5),
            Vector2D.of(1.5, 2.5),
            Vector2D.of(2.5, 2.5),
            Vector2D.of(0.5, 1.0)
        });
        checkPoints(Region.Location.OUTSIDE, set, new Vector2D[] {
            Vector2D.of(1.5, 1.5),
            Vector2D.of(3.5, 1.0),
            Vector2D.of(4.0, 1.5),
            Vector2D.of(6.0, 6.0)
        });
        checkPoints(Region.Location.BOUNDARY, set, new Vector2D[] {
            Vector2D.of(1.0, 1.0),
            Vector2D.of(1.5, 0.0),
            Vector2D.of(1.5, 1.0),
            Vector2D.of(1.5, 2.0),
            Vector2D.of(1.5, 3.0),
            Vector2D.of(3.0, 3.0)
        });
        checkVertexLoopsEquivalent(vertices, set.getVertices());

        for (double x = -0.999; x < 3.999; x += 0.11) {
            Vector2D v = Vector2D.of(x, x + 0.5);
            BoundaryProjection<Vector2D> projection = set.projectToBoundary(v);
            Assert.assertTrue(projection.getOriginal() == v);
            Vector2D p = projection.getProjected();
            if (x < -0.5) {
                Assert.assertEquals(0.0,      p.getX(), TEST_EPS);
                Assert.assertEquals(0.0,      p.getY(), TEST_EPS);
                Assert.assertEquals(+v.distance(Vector2D.ZERO), projection.getOffset(), TEST_EPS);
            } else if (x < 0.5) {
                Assert.assertEquals(0.0,      p.getX(), TEST_EPS);
                Assert.assertEquals(v.getY(), p.getY(), TEST_EPS);
                Assert.assertEquals(-v.getX(), projection.getOffset(), TEST_EPS);
            } else if (x < 1.25) {
                Assert.assertEquals(1.0,      p.getX(), TEST_EPS);
                Assert.assertEquals(v.getY(), p.getY(), TEST_EPS);
                Assert.assertEquals(v.getX() - 1.0, projection.getOffset(), TEST_EPS);
            } else if (x < 2.0) {
                Assert.assertEquals(v.getX(), p.getX(), TEST_EPS);
                Assert.assertEquals(2.0,      p.getY(), TEST_EPS);
                Assert.assertEquals(2.0 - v.getY(), projection.getOffset(), TEST_EPS);
            } else if (x < 3.0) {
                Assert.assertEquals(v.getX(), p.getX(), TEST_EPS);
                Assert.assertEquals(3.0,      p.getY(), TEST_EPS);
                Assert.assertEquals(v.getY() - 3.0, projection.getOffset(), TEST_EPS);
            } else {
                Assert.assertEquals(3.0,      p.getX(), TEST_EPS);
                Assert.assertEquals(3.0,      p.getY(), TEST_EPS);
                Assert.assertEquals(+v.distance(Vector2D.of(3, 3)), projection.getOffset(), TEST_EPS);
            }
        }
    }

    @Test
    public void testDisjointPolygons() {
        // arrange
        Vector2D[][] vertices = new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of(0.0, 1.0),
                Vector2D.of(2.0, 1.0),
                Vector2D.of(1.0, 2.0)
            }, new Vector2D[] {
                Vector2D.of(4.0, 0.0),
                Vector2D.of(5.0, 1.0),
                Vector2D.of(3.0, 1.0)
            }
        };

        // act
        PolygonsSet set = buildSet(vertices);

        // assert
        Assert.assertEquals(Region.Location.INSIDE, set.checkPoint(Vector2D.of(1.0, 1.5)));
        checkPoints(Region.Location.INSIDE, set, new Vector2D[] {
            Vector2D.of(1.0, 1.5),
            Vector2D.of(4.5, 0.8)
        });
        checkPoints(Region.Location.OUTSIDE, set, new Vector2D[] {
            Vector2D.of(1.0, 0.0),
            Vector2D.of(3.5, 1.2),
            Vector2D.of(2.5, 1.0),
            Vector2D.of(3.0, 4.0)
        });
        checkPoints(Region.Location.BOUNDARY, set, new Vector2D[] {
            Vector2D.of(1.0, 1.0),
            Vector2D.of(3.5, 0.5),
            Vector2D.of(0.0, 1.0)
        });
        checkVertexLoopsEquivalent(vertices, set.getVertices());
    }

    @Test
    public void testOppositeHyperplanes() {
        // arrange
        Vector2D[][] vertices = new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of(1.0, 0.0),
                Vector2D.of(2.0, 1.0),
                Vector2D.of(3.0, 1.0),
                Vector2D.of(2.0, 2.0),
                Vector2D.of(1.0, 1.0),
                Vector2D.of(0.0, 1.0)
            }
        };

        // act
        PolygonsSet set = buildSet(vertices);

        // assert
        checkVertexLoopsEquivalent(vertices, set.getVertices());
    }

    @Test
    public void testSingularPoint() {
        // arrange
        Vector2D[][] vertices = new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of( 0.0,  0.0),
                Vector2D.of( 1.0,  0.0),
                Vector2D.of( 1.0,  1.0),
                Vector2D.of( 0.0,  1.0),
                Vector2D.of( 0.0,  0.0),
                Vector2D.of(-1.0,  0.0),
                Vector2D.of(-1.0, -1.0),
                Vector2D.of( 0.0, -1.0)
            }
        };

        // act
        PolygonsSet set = buildSet(vertices);

        // assert
        checkVertexLoopsEquivalent(new Vector2D[][] {
            {
                Vector2D.of( 0.0,  0.0),
                Vector2D.of( 1.0,  0.0),
                Vector2D.of( 1.0,  1.0),
                Vector2D.of( 0.0,  1.0)
            },
            {
                Vector2D.of( 0.0,  0.0),
                Vector2D.of(-1.0,  0.0),
                Vector2D.of(-1.0, -1.0),
                Vector2D.of( 0.0, -1.0)
            }
        }, set.getVertices());
    }

    @Test
    public void testLineIntersection() {
        // arrange
        Vector2D[][] vertices = new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of( 0.0,  0.0),
                Vector2D.of( 2.0,  0.0),
                Vector2D.of( 2.0,  1.0),
                Vector2D.of( 3.0,  1.0),
                Vector2D.of( 3.0,  3.0),
                Vector2D.of( 1.0,  3.0),
                Vector2D.of( 1.0,  2.0),
                Vector2D.of( 0.0,  2.0)
            }
        };

        // act
        PolygonsSet set = buildSet(vertices);

        // assert
        Line l1 = new Line(Vector2D.of(-1.5, 0.0), Math.PI / 4, TEST_PRECISION);
        SubLine s1 = (SubLine) set.intersection(l1.wholeHyperplane());
        List<Interval> i1 = ((IntervalsSet) s1.getRemainingRegion()).asList();
        Assert.assertEquals(2, i1.size());
        Interval v10 = i1.get(0);
        Vector2D p10Lower = l1.toSpace(Vector1D.of(v10.getInf()));
        Assert.assertEquals(0.0, p10Lower.getX(), TEST_EPS);
        Assert.assertEquals(1.5, p10Lower.getY(), TEST_EPS);
        Vector2D p10Upper = l1.toSpace(Vector1D.of(v10.getSup()));
        Assert.assertEquals(0.5, p10Upper.getX(), TEST_EPS);
        Assert.assertEquals(2.0, p10Upper.getY(), TEST_EPS);
        Interval v11 = i1.get(1);
        Vector2D p11Lower = l1.toSpace(Vector1D.of(v11.getInf()));
        Assert.assertEquals(1.0, p11Lower.getX(), TEST_EPS);
        Assert.assertEquals(2.5, p11Lower.getY(), TEST_EPS);
        Vector2D p11Upper = l1.toSpace(Vector1D.of(v11.getSup()));
        Assert.assertEquals(1.5, p11Upper.getX(), TEST_EPS);
        Assert.assertEquals(3.0, p11Upper.getY(), TEST_EPS);

        Line l2 = new Line(Vector2D.of(-1.0, 2.0), 0, TEST_PRECISION);
        SubLine s2 = (SubLine) set.intersection(l2.wholeHyperplane());
        List<Interval> i2 = ((IntervalsSet) s2.getRemainingRegion()).asList();
        Assert.assertEquals(1, i2.size());
        Interval v20 = i2.get(0);
        Vector2D p20Lower = l2.toSpace(Vector1D.of(v20.getInf()));
        Assert.assertEquals(1.0, p20Lower.getX(), TEST_EPS);
        Assert.assertEquals(2.0, p20Lower.getY(), TEST_EPS);
        Vector2D p20Upper = l2.toSpace(Vector1D.of(v20.getSup()));
        Assert.assertEquals(3.0, p20Upper.getX(), TEST_EPS);
        Assert.assertEquals(2.0, p20Upper.getY(), TEST_EPS);
    }

    @Test
    public void testUnlimitedSubHyperplane() {
        // arrange
        Vector2D[][] vertices1 = new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of(0.0, 0.0),
                Vector2D.of(4.0, 0.0),
                Vector2D.of(1.4, 1.5),
                Vector2D.of(0.0, 3.5)
            }
        };
        PolygonsSet set1 = buildSet(vertices1);
        Vector2D[][] vertices2 = new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of(1.4,  0.2),
                Vector2D.of(2.8, -1.2),
                Vector2D.of(2.5,  0.6)
            }
        };
        PolygonsSet set2 = buildSet(vertices2);

        // act
        PolygonsSet set =
            (PolygonsSet) new RegionFactory<Vector2D>().union(set1.copySelf(),
                                                                 set2.copySelf());

        // assert
        checkVertexLoopsEquivalent(vertices1, set1.getVertices());
        checkVertexLoopsEquivalent(vertices2, set2.getVertices());
        checkVertexLoopsEquivalent(new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of(0.0,  0.0),
                Vector2D.of(1.6,  0.0),
                Vector2D.of(2.8, -1.2),
                Vector2D.of(2.6,  0.0),
                Vector2D.of(4.0,  0.0),
                Vector2D.of(1.4,  1.5),
                Vector2D.of(0.0,  3.5)
            }
        }, set.getVertices());
    }

    @Test
    public void testUnion() {
        // arrange
        Vector2D[][] vertices1 = new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of( 0.0,  0.0),
                Vector2D.of( 2.0,  0.0),
                Vector2D.of( 2.0,  2.0),
                Vector2D.of( 0.0,  2.0)
            }
        };
        PolygonsSet set1 = buildSet(vertices1);
        Vector2D[][] vertices2 = new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of( 1.0,  1.0),
                Vector2D.of( 3.0,  1.0),
                Vector2D.of( 3.0,  3.0),
                Vector2D.of( 1.0,  3.0)
            }
        };
        PolygonsSet set2 = buildSet(vertices2);

        // act
        PolygonsSet set  = (PolygonsSet) new RegionFactory<Vector2D>().union(set1.copySelf(),
                                                                                set2.copySelf());

        // assert
        checkVertexLoopsEquivalent(vertices1, set1.getVertices());
        checkVertexLoopsEquivalent(vertices2, set2.getVertices());
        checkVertexLoopsEquivalent(new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of( 0.0,  0.0),
                Vector2D.of( 2.0,  0.0),
                Vector2D.of( 2.0,  1.0),
                Vector2D.of( 3.0,  1.0),
                Vector2D.of( 3.0,  3.0),
                Vector2D.of( 1.0,  3.0),
                Vector2D.of( 1.0,  2.0),
                Vector2D.of( 0.0,  2.0)
            }
        }, set.getVertices());

        checkPoints(Region.Location.INSIDE, set, new Vector2D[] {
            Vector2D.of(1.0, 1.0),
            Vector2D.of(0.5, 0.5),
            Vector2D.of(2.0, 2.0),
            Vector2D.of(2.5, 2.5),
            Vector2D.of(0.5, 1.5),
            Vector2D.of(1.5, 1.5),
            Vector2D.of(1.5, 0.5),
            Vector2D.of(1.5, 2.5),
            Vector2D.of(2.5, 1.5),
            Vector2D.of(2.5, 2.5)
        });
        checkPoints(Region.Location.OUTSIDE, set, new Vector2D[] {
            Vector2D.of(-0.5, 0.5),
            Vector2D.of( 0.5, 2.5),
            Vector2D.of( 2.5, 0.5),
            Vector2D.of( 3.5, 2.5)
        });
        checkPoints(Region.Location.BOUNDARY, set, new Vector2D[] {
            Vector2D.of(0.0, 0.0),
            Vector2D.of(0.5, 2.0),
            Vector2D.of(2.0, 0.5),
            Vector2D.of(2.5, 1.0),
            Vector2D.of(3.0, 2.5)
        });
    }

    @Test
    public void testIntersection() {
        // arrange
        Vector2D[][] vertices1 = new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of( 0.0,  0.0),
                Vector2D.of( 2.0,  0.0),
                Vector2D.of( 2.0,  2.0),
                Vector2D.of( 0.0,  2.0)
            }
        };
        PolygonsSet set1 = buildSet(vertices1);
        Vector2D[][] vertices2 = new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of( 1.0,  1.0),
                Vector2D.of( 3.0,  1.0),
                Vector2D.of( 3.0,  3.0),
                Vector2D.of( 1.0,  3.0)
            }
        };
        PolygonsSet set2 = buildSet(vertices2);

        // act
        PolygonsSet set  = (PolygonsSet) new RegionFactory<Vector2D>().intersection(set1.copySelf(),
                                                                                       set2.copySelf());

        // assert
        checkVertexLoopsEquivalent(vertices1, set1.getVertices());
        checkVertexLoopsEquivalent(vertices2, set2.getVertices());
        checkVertexLoopsEquivalent(new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of( 1.0,  1.0),
                Vector2D.of( 2.0,  1.0),
                Vector2D.of( 2.0,  2.0),
                Vector2D.of( 1.0,  2.0)
            }
        }, set.getVertices());

        checkPoints(Region.Location.INSIDE, set, new Vector2D[] {
            Vector2D.of(1.5, 1.5)
        });
        checkPoints(Region.Location.OUTSIDE, set, new Vector2D[] {
            Vector2D.of(0.5, 1.5),
            Vector2D.of(2.5, 1.5),
            Vector2D.of(1.5, 0.5),
            Vector2D.of(0.5, 0.5)
        });
        checkPoints(Region.Location.BOUNDARY, set, new Vector2D[] {
            Vector2D.of(1.0, 1.0),
            Vector2D.of(2.0, 2.0),
            Vector2D.of(1.0, 1.5),
            Vector2D.of(1.5, 2.0)
        });
    }

    @Test
    public void testXor() {
        // arrange
        Vector2D[][] vertices1 = new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of( 0.0,  0.0),
                Vector2D.of( 2.0,  0.0),
                Vector2D.of( 2.0,  2.0),
                Vector2D.of( 0.0,  2.0)
            }
        };
        PolygonsSet set1 = buildSet(vertices1);
        Vector2D[][] vertices2 = new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of( 1.0,  1.0),
                Vector2D.of( 3.0,  1.0),
                Vector2D.of( 3.0,  3.0),
                Vector2D.of( 1.0,  3.0)
            }
        };
        PolygonsSet set2 = buildSet(vertices2);

        // act
        PolygonsSet set  = (PolygonsSet) new RegionFactory<Vector2D>().xor(set1.copySelf(),
                                                                              set2.copySelf());

        // assert
        checkVertexLoopsEquivalent(vertices1, set1.getVertices());
        checkVertexLoopsEquivalent(vertices2, set2.getVertices());
        checkVertexLoopsEquivalent(new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of( 0.0,  0.0),
                Vector2D.of( 2.0,  0.0),
                Vector2D.of( 2.0,  1.0),
                Vector2D.of( 3.0,  1.0),
                Vector2D.of( 3.0,  3.0),
                Vector2D.of( 1.0,  3.0),
                Vector2D.of( 1.0,  2.0),
                Vector2D.of( 0.0,  2.0)
            },
            new Vector2D[] {
                Vector2D.of( 1.0,  1.0),
                Vector2D.of( 1.0,  2.0),
                Vector2D.of( 2.0,  2.0),
                Vector2D.of( 2.0,  1.0)
            }
        }, set.getVertices());

        checkPoints(Region.Location.INSIDE, set, new Vector2D[] {
            Vector2D.of(0.5, 0.5),
            Vector2D.of(2.5, 2.5),
            Vector2D.of(0.5, 1.5),
            Vector2D.of(1.5, 0.5),
            Vector2D.of(1.5, 2.5),
            Vector2D.of(2.5, 1.5),
            Vector2D.of(2.5, 2.5)
        });
        checkPoints(Region.Location.OUTSIDE, set, new Vector2D[] {
            Vector2D.of(-0.5, 0.5),
            Vector2D.of( 0.5, 2.5),
            Vector2D.of( 2.5, 0.5),
            Vector2D.of( 1.5, 1.5),
            Vector2D.of( 3.5, 2.5)
        });
        checkPoints(Region.Location.BOUNDARY, set, new Vector2D[] {
            Vector2D.of(1.0, 1.0),
            Vector2D.of(2.0, 2.0),
            Vector2D.of(1.5, 1.0),
            Vector2D.of(2.0, 1.5),
            Vector2D.of(0.0, 0.0),
            Vector2D.of(0.5, 2.0),
            Vector2D.of(2.0, 0.5),
            Vector2D.of(2.5, 1.0),
            Vector2D.of(3.0, 2.5)
        });
    }

    @Test
    public void testDifference() {
        // arrange
        Vector2D[][] vertices1 = new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of( 0.0,  0.0),
                Vector2D.of( 2.0,  0.0),
                Vector2D.of( 2.0,  2.0),
                Vector2D.of( 0.0,  2.0)
            }
        };
        PolygonsSet set1 = buildSet(vertices1);
        Vector2D[][] vertices2 = new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of( 1.0,  1.0),
                Vector2D.of( 3.0,  1.0),
                Vector2D.of( 3.0,  3.0),
                Vector2D.of( 1.0,  3.0)
            }
        };
        PolygonsSet set2 = buildSet(vertices2);

        // act
        PolygonsSet set  = (PolygonsSet) new RegionFactory<Vector2D>().difference(set1.copySelf(),
                                                                                     set2.copySelf());

        // assert
        checkVertexLoopsEquivalent(vertices1, set1.getVertices());
        checkVertexLoopsEquivalent(vertices2, set2.getVertices());
        checkVertexLoopsEquivalent(new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of( 0.0,  0.0),
                Vector2D.of( 2.0,  0.0),
                Vector2D.of( 2.0,  1.0),
                Vector2D.of( 1.0,  1.0),
                Vector2D.of( 1.0,  2.0),
                Vector2D.of( 0.0,  2.0)
            }
        }, set.getVertices());

        checkPoints(Region.Location.INSIDE, set, new Vector2D[] {
            Vector2D.of(0.5, 0.5),
            Vector2D.of(0.5, 1.5),
            Vector2D.of(1.5, 0.5)
        });
        checkPoints(Region.Location.OUTSIDE, set, new Vector2D[] {
            Vector2D.of( 2.5, 2.5),
            Vector2D.of(-0.5, 0.5),
            Vector2D.of( 0.5, 2.5),
            Vector2D.of( 2.5, 0.5),
            Vector2D.of( 1.5, 1.5),
            Vector2D.of( 3.5, 2.5),
            Vector2D.of( 1.5, 2.5),
            Vector2D.of( 2.5, 1.5),
            Vector2D.of( 2.0, 1.5),
            Vector2D.of( 2.0, 2.0),
            Vector2D.of( 2.5, 1.0),
            Vector2D.of( 2.5, 2.5),
            Vector2D.of( 3.0, 2.5)
        });
        checkPoints(Region.Location.BOUNDARY, set, new Vector2D[] {
            Vector2D.of(1.0, 1.0),
            Vector2D.of(1.5, 1.0),
            Vector2D.of(0.0, 0.0),
            Vector2D.of(0.5, 2.0),
            Vector2D.of(2.0, 0.5)
        });
    }

    @Test
    public void testEmptyDifference() {
        // arrange
        Vector2D[][] vertices1 = new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of( 0.5, 3.5),
                Vector2D.of( 0.5, 4.5),
                Vector2D.of(-0.5, 4.5),
                Vector2D.of(-0.5, 3.5)
            }
        };
        PolygonsSet set1 = buildSet(vertices1);
        Vector2D[][] vertices2 = new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of( 1.0, 2.0),
                Vector2D.of( 1.0, 8.0),
                Vector2D.of(-1.0, 8.0),
                Vector2D.of(-1.0, 2.0)
            }
        };
        PolygonsSet set2 = buildSet(vertices2);

        // act
        PolygonsSet diff = (PolygonsSet) new RegionFactory<Vector2D>().difference(set1.copySelf(), set2.copySelf());

        // assert
        Assert.assertEquals(0.0, diff.getSize(), TEST_EPS);
        Assert.assertTrue(diff.isEmpty());
    }

    @Test
    public void testChoppedHexagon() {
        // arrange
        double pi6   = Math.PI / 6.0;
        double sqrt3 = Math.sqrt(3.0);
        SubLine[] hyp = {
            new Line(Vector2D.of(   0.0, 1.0),  5 * pi6, TEST_PRECISION).wholeHyperplane(),
            new Line(Vector2D.of(-sqrt3, 1.0),  7 * pi6, TEST_PRECISION).wholeHyperplane(),
            new Line(Vector2D.of(-sqrt3, 1.0),  9 * pi6, TEST_PRECISION).wholeHyperplane(),
            new Line(Vector2D.of(-sqrt3, 0.0), 11 * pi6, TEST_PRECISION).wholeHyperplane(),
            new Line(Vector2D.of(   0.0, 0.0), 13 * pi6, TEST_PRECISION).wholeHyperplane(),
            new Line(Vector2D.of(   0.0, 1.0),  3 * pi6, TEST_PRECISION).wholeHyperplane(),
            new Line(Vector2D.of(-5.0 * sqrt3 / 6.0, 0.0), 9 * pi6, TEST_PRECISION).wholeHyperplane()
        };
        hyp[1] = (SubLine) hyp[1].split(hyp[0].getHyperplane()).getMinus();
        hyp[2] = (SubLine) hyp[2].split(hyp[1].getHyperplane()).getMinus();
        hyp[3] = (SubLine) hyp[3].split(hyp[2].getHyperplane()).getMinus();
        hyp[4] = (SubLine) hyp[4].split(hyp[3].getHyperplane()).getMinus().split(hyp[0].getHyperplane()).getMinus();
        hyp[5] = (SubLine) hyp[5].split(hyp[4].getHyperplane()).getMinus().split(hyp[0].getHyperplane()).getMinus();
        hyp[6] = (SubLine) hyp[6].split(hyp[3].getHyperplane()).getMinus().split(hyp[1].getHyperplane()).getMinus();
        BSPTree<Vector2D> tree = new BSPTree<>(Boolean.TRUE);
        for (int i = hyp.length - 1; i >= 0; --i) {
            tree = new BSPTree<>(hyp[i], new BSPTree<Vector2D>(Boolean.FALSE), tree, null);
        }
        PolygonsSet set = new PolygonsSet(tree, TEST_PRECISION);
        SubLine splitter =
            new Line(Vector2D.of(-2.0 * sqrt3 / 3.0, 0.0), 9 * pi6, TEST_PRECISION).wholeHyperplane();

        // act
        PolygonsSet slice =
            new PolygonsSet(new BSPTree<>(splitter,
                                                     set.getTree(false).split(splitter).getPlus(),
                                                     new BSPTree<Vector2D>(Boolean.FALSE), null),
                    TEST_PRECISION);

        // assert
        Assert.assertEquals(Region.Location.OUTSIDE,
                            slice.checkPoint(Vector2D.of(0.1, 0.5)));
        Assert.assertEquals(11.0 / 3.0, slice.getBoundarySize(), TEST_EPS);
    }

    @Test
    public void testConcentric() {
        // arrange
        double h = Math.sqrt(3.0) / 2.0;
        Vector2D[][] vertices1 = new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of( 0.00, 0.1 * h),
                Vector2D.of( 0.05, 0.1 * h),
                Vector2D.of( 0.10, 0.2 * h),
                Vector2D.of( 0.05, 0.3 * h),
                Vector2D.of(-0.05, 0.3 * h),
                Vector2D.of(-0.10, 0.2 * h),
                Vector2D.of(-0.05, 0.1 * h)
            }
        };
        PolygonsSet set1 = buildSet(vertices1);
        Vector2D[][] vertices2 = new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of( 0.00, 0.0 * h),
                Vector2D.of( 0.10, 0.0 * h),
                Vector2D.of( 0.20, 0.2 * h),
                Vector2D.of( 0.10, 0.4 * h),
                Vector2D.of(-0.10, 0.4 * h),
                Vector2D.of(-0.20, 0.2 * h),
                Vector2D.of(-0.10, 0.0 * h)
            }
        };
        PolygonsSet set2 = buildSet(vertices2);

        // act/assert
        Assert.assertTrue(set2.contains(set1));
    }

    @Test
    public void testBug20040520() {
        // arrange
        BSPTree<Vector2D> a0 =
            new BSPTree<>(buildSegment(Vector2D.of(0.85, -0.05),
                                                  Vector2D.of(0.90, -0.10)),
                                                  new BSPTree<Vector2D>(Boolean.FALSE),
                                                  new BSPTree<Vector2D>(Boolean.TRUE),
                                                  null);
        BSPTree<Vector2D> a1 =
            new BSPTree<>(buildSegment(Vector2D.of(0.85, -0.10),
                                                  Vector2D.of(0.90, -0.10)),
                                                  new BSPTree<Vector2D>(Boolean.FALSE), a0, null);
        BSPTree<Vector2D> a2 =
            new BSPTree<>(buildSegment(Vector2D.of(0.90, -0.05),
                                                  Vector2D.of(0.85, -0.05)),
                                                  new BSPTree<Vector2D>(Boolean.FALSE), a1, null);
        BSPTree<Vector2D> a3 =
            new BSPTree<>(buildSegment(Vector2D.of(0.82, -0.05),
                                                  Vector2D.of(0.82, -0.08)),
                                                  new BSPTree<Vector2D>(Boolean.FALSE),
                                                  new BSPTree<Vector2D>(Boolean.TRUE),
                                                  null);
        BSPTree<Vector2D> a4 =
            new BSPTree<>(buildHalfLine(Vector2D.of(0.85, -0.05),
                                                   Vector2D.of(0.80, -0.05),
                                                   false),
                                                   new BSPTree<Vector2D>(Boolean.FALSE), a3, null);
        BSPTree<Vector2D> a5 =
            new BSPTree<>(buildSegment(Vector2D.of(0.82, -0.08),
                                                  Vector2D.of(0.82, -0.18)),
                                                  new BSPTree<Vector2D>(Boolean.FALSE),
                                                  new BSPTree<Vector2D>(Boolean.TRUE),
                                                  null);
        BSPTree<Vector2D> a6 =
            new BSPTree<>(buildHalfLine(Vector2D.of(0.82, -0.18),
                                                   Vector2D.of(0.85, -0.15),
                                                   true),
                                                   new BSPTree<Vector2D>(Boolean.FALSE), a5, null);
        BSPTree<Vector2D> a7 =
            new BSPTree<>(buildHalfLine(Vector2D.of(0.85, -0.05),
                                                   Vector2D.of(0.82, -0.08),
                                                   false),
                                                   a4, a6, null);
        BSPTree<Vector2D> a8 =
            new BSPTree<>(buildLine(Vector2D.of(0.85, -0.25),
                                               Vector2D.of(0.85,  0.05)),
                                               a2, a7, null);
        BSPTree<Vector2D> a9 =
            new BSPTree<>(buildLine(Vector2D.of(0.90,  0.05),
                                               Vector2D.of(0.90, -0.50)),
                                               a8, new BSPTree<Vector2D>(Boolean.FALSE), null);

        BSPTree<Vector2D> b0 =
            new BSPTree<>(buildSegment(Vector2D.of(0.92, -0.12),
                                                  Vector2D.of(0.92, -0.08)),
                                                  new BSPTree<Vector2D>(Boolean.FALSE), new BSPTree<Vector2D>(Boolean.TRUE),
                                                  null);
        BSPTree<Vector2D> b1 =
            new BSPTree<>(buildHalfLine(Vector2D.of(0.92, -0.08),
                                                   Vector2D.of(0.90, -0.10),
                                                   true),
                                                   new BSPTree<Vector2D>(Boolean.FALSE), b0, null);
        BSPTree<Vector2D> b2 =
            new BSPTree<>(buildSegment(Vector2D.of(0.92, -0.18),
                                                  Vector2D.of(0.92, -0.12)),
                                                  new BSPTree<Vector2D>(Boolean.FALSE), new BSPTree<Vector2D>(Boolean.TRUE),
                                                  null);
        BSPTree<Vector2D> b3 =
            new BSPTree<>(buildSegment(Vector2D.of(0.85, -0.15),
                                                  Vector2D.of(0.90, -0.20)),
                                                  new BSPTree<Vector2D>(Boolean.FALSE), b2, null);
        BSPTree<Vector2D> b4 =
            new BSPTree<>(buildSegment(Vector2D.of(0.95, -0.15),
                                                  Vector2D.of(0.85, -0.05)),
                                                  b1, b3, null);
        BSPTree<Vector2D> b5 =
            new BSPTree<>(buildHalfLine(Vector2D.of(0.85, -0.05),
                                                   Vector2D.of(0.85, -0.25),
                                                   true),
                                                   new BSPTree<Vector2D>(Boolean.FALSE), b4, null);
        BSPTree<Vector2D> b6 =
            new BSPTree<>(buildLine(Vector2D.of(0.0, -1.10),
                                               Vector2D.of(1.0, -0.10)),
                                               new BSPTree<Vector2D>(Boolean.FALSE), b5, null);

        // act
        PolygonsSet c =
            (PolygonsSet) new RegionFactory<Vector2D>().union(new PolygonsSet(a9, TEST_PRECISION),
                                                                 new PolygonsSet(b6, TEST_PRECISION));

        // assert
        checkPoints(Region.Location.INSIDE, c, new Vector2D[] {
            Vector2D.of(0.83, -0.06),
            Vector2D.of(0.83, -0.15),
            Vector2D.of(0.88, -0.15),
            Vector2D.of(0.88, -0.09),
            Vector2D.of(0.88, -0.07),
            Vector2D.of(0.91, -0.18),
            Vector2D.of(0.91, -0.10)
        });

        checkPoints(Region.Location.OUTSIDE, c, new Vector2D[] {
            Vector2D.of(0.80, -0.10),
            Vector2D.of(0.83, -0.50),
            Vector2D.of(0.83, -0.20),
            Vector2D.of(0.83, -0.02),
            Vector2D.of(0.87, -0.50),
            Vector2D.of(0.87, -0.20),
            Vector2D.of(0.87, -0.02),
            Vector2D.of(0.91, -0.20),
            Vector2D.of(0.91, -0.08),
            Vector2D.of(0.93, -0.15)
        });

        checkVertexLoopsEquivalent(new Vector2D[][] {
            new Vector2D[] {
                Vector2D.of(0.85, -0.15),
                Vector2D.of(0.90, -0.20),
                Vector2D.of(0.92, -0.18),
                Vector2D.of(0.92, -0.08),
                Vector2D.of(0.90, -0.10),
                Vector2D.of(0.90, -0.05),
                Vector2D.of(0.82, -0.05),
                Vector2D.of(0.82, -0.18),
            }
        }, c.getVertices());
    }

    @Test
    public void testBug20041003() {
        // arrange
        Line[] l = {
            new Line(Vector2D.of(0.0, 0.625000007541172),
                     Vector2D.of(1.0, 0.625000007541172), TEST_PRECISION),
            new Line(Vector2D.of(-0.19204433621902645, 0.0),
                     Vector2D.of(-0.19204433621902645, 1.0), TEST_PRECISION),
            new Line(Vector2D.of(-0.40303524786887,  0.4248364535319128),
                     Vector2D.of(-1.12851149797877, -0.2634107480798909), TEST_PRECISION),
            new Line(Vector2D.of(0.0, 2.0),
                     Vector2D.of(1.0, 2.0), TEST_PRECISION)
        };

        BSPTree<Vector2D> node1 =
            new BSPTree<>(new SubLine(l[0],
                                                 new IntervalsSet(intersectionAbscissa(l[0], l[1]),
                                                                  intersectionAbscissa(l[0], l[2]),
                                                                  TEST_PRECISION)),
                                     new BSPTree<Vector2D>(Boolean.TRUE),
                                     new BSPTree<Vector2D>(Boolean.FALSE),
                                     null);
        BSPTree<Vector2D> node2 =
            new BSPTree<>(new SubLine(l[1],
                                                 new IntervalsSet(intersectionAbscissa(l[1], l[2]),
                                                                  intersectionAbscissa(l[1], l[3]),
                                                                  TEST_PRECISION)),
                                     node1,
                                     new BSPTree<Vector2D>(Boolean.FALSE),
                                     null);
        BSPTree<Vector2D> node3 =
            new BSPTree<>(new SubLine(l[2],
                                                 new IntervalsSet(intersectionAbscissa(l[2], l[3]),
                                                 Double.POSITIVE_INFINITY, TEST_PRECISION)),
                                     node2,
                                     new BSPTree<Vector2D>(Boolean.FALSE),
                                     null);
        BSPTree<Vector2D> node4 =
            new BSPTree<>(l[3].wholeHyperplane(),
                                     node3,
                                     new BSPTree<Vector2D>(Boolean.FALSE),
                                     null);

        // act
        PolygonsSet set = new PolygonsSet(node4, TEST_PRECISION);

        // assert
        Assert.assertEquals(0, set.getVertices().length);
    }

    @Test
    public void testSqueezedHexa() {
        // act
        PolygonsSet set = new PolygonsSet(TEST_PRECISION,
                                          Vector2D.of(-6, -4), Vector2D.of(-8, -8), Vector2D.of(  8, -8),
                                          Vector2D.of( 6, -4), Vector2D.of(10,  4), Vector2D.of(-10,  4));

        // assert
        Assert.assertEquals(Location.OUTSIDE, set.checkPoint(Vector2D.of(0, 6)));
    }

    @Test
    public void testIssue880Simplified() {
        // arrange
        Vector2D[] vertices1 = new Vector2D[] {
            Vector2D.of( 90.13595870833188,  38.33604606376991),
            Vector2D.of( 90.14047850603913,  38.34600084496253),
            Vector2D.of( 90.11045289492762,  38.36801537312368),
            Vector2D.of( 90.10871471476526,  38.36878044144294),
            Vector2D.of( 90.10424901707671,  38.374300101757),
            Vector2D.of( 90.0979455456843,   38.373578376172475),
            Vector2D.of( 90.09081227075944,  38.37526295920463),
            Vector2D.of( 90.09081378927135,  38.375193883266434)
        };

        // act
        PolygonsSet set1 = new PolygonsSet(TEST_PRECISION, vertices1);

        // assert
        Assert.assertEquals(Location.OUTSIDE, set1.checkPoint(Vector2D.of(90.12,  38.32)));
        Assert.assertEquals(Location.OUTSIDE, set1.checkPoint(Vector2D.of(90.135, 38.355)));

    }

    @Test
    public void testIssue880Complete() {
        Vector2D[] vertices1 = new Vector2D[] {
                Vector2D.of( 90.08714908223715,  38.370299337260235),
                Vector2D.of( 90.08709517675004,  38.3702895991413),
                Vector2D.of( 90.08401538704919,  38.368849330127944),
                Vector2D.of( 90.08258210430711,  38.367634558585564),
                Vector2D.of( 90.08251455106665,  38.36763409247078),
                Vector2D.of( 90.08106599752608,  38.36761621664249),
                Vector2D.of( 90.08249585300035,  38.36753627557965),
                Vector2D.of( 90.09075743352184,  38.35914647644972),
                Vector2D.of( 90.09099945896571,  38.35896264724079),
                Vector2D.of( 90.09269383800086,  38.34595756121246),
                Vector2D.of( 90.09638631543191,  38.3457988093121),
                Vector2D.of( 90.09666417351019,  38.34523360999418),
                Vector2D.of( 90.1297082145872,  38.337670454923625),
                Vector2D.of( 90.12971687748956,  38.337669827794684),
                Vector2D.of( 90.1240820219179,  38.34328502001131),
                Vector2D.of( 90.13084259656404,  38.34017811765017),
                Vector2D.of( 90.13378567942857,  38.33860579180606),
                Vector2D.of( 90.13519557833206,  38.33621054663689),
                Vector2D.of( 90.13545616732307,  38.33614965452864),
                Vector2D.of( 90.13553111202748,  38.33613962818305),
                Vector2D.of( 90.1356903436448,  38.33610227127048),
                Vector2D.of( 90.13576283227428,  38.33609255422783),
                Vector2D.of( 90.13595870833188,  38.33604606376991),
                Vector2D.of( 90.1361556630693,  38.3360024198866),
                Vector2D.of( 90.13622408795709,  38.335987048115726),
                Vector2D.of( 90.13696189099994,  38.33581914328681),
                Vector2D.of( 90.13746655304897,  38.33616706665265),
                Vector2D.of( 90.13845973716064,  38.33650776167099),
                Vector2D.of( 90.13950901827667,  38.3368469456463),
                Vector2D.of( 90.14393814424852,  38.337591835857495),
                Vector2D.of( 90.14483839716831,  38.337076122362475),
                Vector2D.of( 90.14565474433601,  38.33769000964429),
                Vector2D.of( 90.14569421179482,  38.3377117256905),
                Vector2D.of( 90.14577067124333,  38.33770883625908),
                Vector2D.of( 90.14600350631684,  38.337714326520995),
                Vector2D.of( 90.14600355139731,  38.33771435193319),
                Vector2D.of( 90.14600369112401,  38.33771443882085),
                Vector2D.of( 90.14600382486884,  38.33771453466096),
                Vector2D.of( 90.14600395205912,  38.33771463904344),
                Vector2D.of( 90.14600407214999,  38.337714751520764),
                Vector2D.of( 90.14600418462749,  38.337714871611695),
                Vector2D.of( 90.14600422249327,  38.337714915811034),
                Vector2D.of( 90.14867838361471,  38.34113888210675),
                Vector2D.of( 90.14923750157374,  38.341582537502575),
                Vector2D.of( 90.14877083250991,  38.34160685841391),
                Vector2D.of( 90.14816667319519,  38.34244232585684),
                Vector2D.of( 90.14797696744586,  38.34248455284745),
                Vector2D.of( 90.14484318014337,  38.34385573215269),
                Vector2D.of( 90.14477919958296,  38.3453797747614),
                Vector2D.of( 90.14202393306448,  38.34464324839456),
                Vector2D.of( 90.14198920640195,  38.344651155237216),
                Vector2D.of( 90.14155207025175,  38.34486424263724),
                Vector2D.of( 90.1415196143314,  38.344871730519),
                Vector2D.of( 90.14128611910814,  38.34500196593859),
                Vector2D.of( 90.14047850603913,  38.34600084496253),
                Vector2D.of( 90.14045907000337,  38.34601860032171),
                Vector2D.of( 90.14039496493928,  38.346223030432384),
                Vector2D.of( 90.14037626063737,  38.346240203360026),
                Vector2D.of( 90.14030005823724,  38.34646920000705),
                Vector2D.of( 90.13799164754806,  38.34903093011013),
                Vector2D.of( 90.11045289492762,  38.36801537312368),
                Vector2D.of( 90.10871471476526,  38.36878044144294),
                Vector2D.of( 90.10424901707671,  38.374300101757),
                Vector2D.of( 90.10263482039932,  38.37310041316073),
                Vector2D.of( 90.09834601753448,  38.373615053823414),
                Vector2D.of( 90.0979455456843,  38.373578376172475),
                Vector2D.of( 90.09086514328669,  38.37527884194668),
                Vector2D.of( 90.09084931407364,  38.37590801712463),
                Vector2D.of( 90.09081227075944,  38.37526295920463),
                Vector2D.of( 90.09081378927135,  38.375193883266434)
        };
        PolygonsSet set1 = new PolygonsSet(TEST_PRECISION, vertices1);
        Assert.assertEquals(Location.OUTSIDE, set1.checkPoint(Vector2D.of(90.0905,  38.3755)));
        Assert.assertEquals(Location.INSIDE,  set1.checkPoint(Vector2D.of(90.09084, 38.3755)));
        Assert.assertEquals(Location.OUTSIDE, set1.checkPoint(Vector2D.of(90.0913,  38.3755)));
        Assert.assertEquals(Location.INSIDE,  set1.checkPoint(Vector2D.of(90.1042,  38.3739)));
        Assert.assertEquals(Location.INSIDE,  set1.checkPoint(Vector2D.of(90.1111,  38.3673)));
        Assert.assertEquals(Location.OUTSIDE, set1.checkPoint(Vector2D.of(90.0959,  38.3457)));

        Vector2D[] vertices2 = new Vector2D[] {
                Vector2D.of( 90.13067558880044,  38.36977255037573),
                Vector2D.of( 90.12907570488,  38.36817308242706),
                Vector2D.of( 90.1342774136516,  38.356886880294724),
                Vector2D.of( 90.13090330629757,  38.34664392676211),
                Vector2D.of( 90.13078571364593,  38.344904617518466),
                Vector2D.of( 90.1315602208914,  38.3447185040846),
                Vector2D.of( 90.1316336226821,  38.34470643148342),
                Vector2D.of( 90.134020944832,  38.340936644972885),
                Vector2D.of( 90.13912536387306,  38.335497255122334),
                Vector2D.of( 90.1396178806582,  38.334878075552126),
                Vector2D.of( 90.14083049696671,  38.33316530644106),
                Vector2D.of( 90.14145252901329,  38.33152722916191),
                Vector2D.of( 90.1404779335565,  38.32863516047786),
                Vector2D.of( 90.14282712131586,  38.327504432532066),
                Vector2D.of( 90.14616669875488,  38.3237354115015),
                Vector2D.of( 90.14860976050608,  38.315714862457924),
                Vector2D.of( 90.14999277782437,  38.3164932507504),
                Vector2D.of( 90.15005207194997,  38.316534677663356),
                Vector2D.of( 90.15508513859612,  38.31878731691609),
                Vector2D.of( 90.15919938519221,  38.31852743183782),
                Vector2D.of( 90.16093758658837,  38.31880662005153),
                Vector2D.of( 90.16099420184912,  38.318825953291594),
                Vector2D.of( 90.1665411125756,  38.31859497874757),
                Vector2D.of( 90.16999653861313,  38.32505772048029),
                Vector2D.of( 90.17475243391698,  38.32594398441148),
                Vector2D.of( 90.17940844844992,  38.327427213761325),
                Vector2D.of( 90.20951909541378,  38.330616833491774),
                Vector2D.of( 90.2155400467941,  38.331746223670336),
                Vector2D.of( 90.21559881391778,  38.33175551425302),
                Vector2D.of( 90.21916646426041,  38.332584299620805),
                Vector2D.of( 90.23863749852285,  38.34778978875795),
                Vector2D.of( 90.25459855175802,  38.357790570608984),
                Vector2D.of( 90.25964298227257,  38.356918010203174),
                Vector2D.of( 90.26024593994703,  38.361692743151366),
                Vector2D.of( 90.26146187570015,  38.36311080550837),
                Vector2D.of( 90.26614159359622,  38.36510808579902),
                Vector2D.of( 90.26621342936448,  38.36507942500333),
                Vector2D.of( 90.26652190211962,  38.36494042196722),
                Vector2D.of( 90.26621240678867,  38.365113172030874),
                Vector2D.of( 90.26614057102057,  38.365141832826794),
                Vector2D.of( 90.26380080055299,  38.3660381760273),
                Vector2D.of( 90.26315345241,  38.36670658276421),
                Vector2D.of( 90.26251574942881,  38.367490323488084),
                Vector2D.of( 90.26247873448426,  38.36755266444749),
                Vector2D.of( 90.26234628016698,  38.36787989125406),
                Vector2D.of( 90.26214559424784,  38.36945909356126),
                Vector2D.of( 90.25861728442555,  38.37200753430875),
                Vector2D.of( 90.23905557537864,  38.375405314295904),
                Vector2D.of( 90.22517251874075,  38.38984691662256),
                Vector2D.of( 90.22549955153215,  38.3911564273979),
                Vector2D.of( 90.22434386063355,  38.391476432092134),
                Vector2D.of( 90.22147729457276,  38.39134652252034),
                Vector2D.of( 90.22142070120117,  38.391349167741964),
                Vector2D.of( 90.20665060751588,  38.39475580900313),
                Vector2D.of( 90.20042268367109,  38.39842558622888),
                Vector2D.of( 90.17423771242085,  38.402727751805344),
                Vector2D.of( 90.16756796257476,  38.40913898597597),
                Vector2D.of( 90.16728283954308,  38.411255399912875),
                Vector2D.of( 90.16703538220418,  38.41136059866693),
                Vector2D.of( 90.16725865657685,  38.41013618805954),
                Vector2D.of( 90.16746107640665,  38.40902614307544),
                Vector2D.of( 90.16122795307462,  38.39773101873203)
        };
        PolygonsSet set2 = new PolygonsSet(TEST_PRECISION, vertices2);
        PolygonsSet set  = (PolygonsSet) new
                RegionFactory<Vector2D>().difference(set1.copySelf(),
                                                        set2.copySelf());

        Vector2D[][] vertices = set.getVertices();
        Assert.assertTrue(vertices[0][0] != null);
        Assert.assertEquals(1, vertices.length);
    }

    @Test
    public void testTooThinBox() {
        // act/assert
        Assert.assertEquals(0.0,
                            new PolygonsSet(0.0, 0.0, 0.0, 10.3206397147574, TEST_PRECISION).getSize(),
                            TEST_EPS);
    }

    @Test
    public void testWrongUsage() {
        // the following is a wrong usage of the constructor.
        // as explained in the javadoc, the failure is NOT detected at construction
        // time but occurs later on
        PolygonsSet ps = new PolygonsSet(new BSPTree<Vector2D>(), TEST_PRECISION);
        Assert.assertNotNull(ps);
        try {
            ps.getSize();
            Assert.fail("an exception should have been thrown");
        } catch (NullPointerException npe) {
            // this is expected
        }
    }

    @Test
    public void testIssue1162() {
        // arrange
        PolygonsSet p = new PolygonsSet(TEST_PRECISION,
                                                Vector2D.of(4.267199999996532, -11.928637756014894),
                                                Vector2D.of(4.267200000026445, -14.12360595809307),
                                                Vector2D.of(9.144000000273694, -14.12360595809307),
                                                Vector2D.of(9.144000000233383, -11.928637756020067));

        PolygonsSet w = new PolygonsSet(TEST_PRECISION,
                                                Vector2D.of(2.56735636510452512E-9, -11.933116461089332),
                                                Vector2D.of(2.56735636510452512E-9, -12.393225665247766),
                                                Vector2D.of(2.56735636510452512E-9, -27.785625665247778),
                                                Vector2D.of(4.267200000030211,      -27.785625665247778),
                                                Vector2D.of(4.267200000030211,      -11.933116461089332));

        // act/assert
        Assert.assertFalse(p.contains(w));
    }

    @Test
    public void testThinRectangle_toleranceLessThanWidth_resultIsAccurate() {
        // if tolerance is smaller than rectangle width, the rectangle is computed accurately

        // arrange
        RegionFactory<Vector2D> factory = new RegionFactory<>();
        Vector2D pA = Vector2D.of(0.0,        1.0);
        Vector2D pB = Vector2D.of(0.0,        0.0);
        Vector2D pC = Vector2D.of(1.0 / 64.0, 0.0);
        Vector2D pD = Vector2D.of(1.0 / 64.0, 1.0);

        // if tolerance is smaller than rectangle width, the rectangle is computed accurately
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1.0 / 256);
        Hyperplane<Vector2D>[] h1 = new Line[] {
            new Line(pA, pB, precision),
            new Line(pB, pC, precision),
            new Line(pC, pD, precision),
            new Line(pD, pA, precision)
        };

        // act
        Region<Vector2D> accuratePolygon = factory.buildConvex(h1);

        // assert
        Assert.assertEquals(1.0 / 64.0, accuratePolygon.getSize(), TEST_EPS);
        EuclideanTestUtils.assertPositiveInfinity(new RegionFactory<Vector2D>().getComplement(accuratePolygon).getSize());
        Assert.assertEquals(2 * (1.0 + 1.0 / 64.0), accuratePolygon.getBoundarySize(), TEST_EPS);
    }

    @Test
    public void testThinRectangle_toleranceGreaterThanWidth_resultIsDegenerate() {
        // if tolerance is larger than rectangle width, the rectangle degenerates
        // as of 3.3, its two long edges cannot be distinguished anymore and this part of the test did fail
        // this has been fixed in 3.4 (issue MATH-1174)

        // arrange
        RegionFactory<Vector2D> factory = new RegionFactory<>();
        Vector2D pA = Vector2D.of(0.0,        1.0);
        Vector2D pB = Vector2D.of(0.0,        0.0);
        Vector2D pC = Vector2D.of(1.0 / 64.0, 0.0);
        Vector2D pD = Vector2D.of(1.0 / 64.0, 1.0);

        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1.0 / 16);

        Hyperplane<Vector2D>[] h2 = new Line[] {
                new Line(pA, pB, precision),
                new Line(pB, pC, precision),
                new Line(pC, pD, precision),
                new Line(pD, pA, precision)
            };

        // act
        Region<Vector2D> degeneratedPolygon = factory.buildConvex(h2);

        // assert
        Assert.assertEquals(0.0, degeneratedPolygon.getSize(), TEST_EPS);
        Assert.assertTrue(degeneratedPolygon.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInconsistentHyperplanes() {
        // act
        new RegionFactory<Vector2D>().buildConvex(new Line(Vector2D.of(0, 0), Vector2D.of(0, 1), TEST_PRECISION),
                                                     new Line(Vector2D.of(1, 1), Vector2D.of(1, 0), TEST_PRECISION));
    }

    @Test
    public void testBoundarySimplification() {
        // a simple square will result in a 4 cuts and 5 leafs tree
        PolygonsSet square = new PolygonsSet(TEST_PRECISION,
                                             Vector2D.of(0, 0),
                                             Vector2D.of(1, 0),
                                             Vector2D.of(1, 1),
                                             Vector2D.of(0, 1));
        Vector2D[][] squareBoundary = square.getVertices();
        Assert.assertEquals(1, squareBoundary.length);
        Assert.assertEquals(4, squareBoundary[0].length);
        Counter squareCount = new Counter();
        squareCount.count(square);
        Assert.assertEquals(4, squareCount.getInternalNodes());
        Assert.assertEquals(5, squareCount.getLeafNodes());

        // splitting the square in two halves increases the BSP tree
        // with 3 more cuts and 3 more leaf nodes
        SubLine cut = new Line(Vector2D.of(0.5, 0.5), 0.0, square.getPrecision()).wholeHyperplane();
        PolygonsSet splitSquare = new PolygonsSet(square.getTree(false).split(cut),
                                                  square.getPrecision());
        Counter splitSquareCount = new Counter();
        splitSquareCount.count(splitSquare);
        Assert.assertEquals(squareCount.getInternalNodes() + 3, splitSquareCount.getInternalNodes());
        Assert.assertEquals(squareCount.getLeafNodes()     + 3, splitSquareCount.getLeafNodes());

        // the number of vertices should not change, as the intermediate vertices
        // at (0.0, 0.5) and (1.0, 0.5) induced by the top level horizontal split
        // should be removed during the boundary extraction process
        Vector2D[][] splitBoundary = splitSquare.getVertices();
        Assert.assertEquals(1, splitBoundary.length);
        Assert.assertEquals(4, splitBoundary[0].length);
    }

    private static class Counter {

        private int internalNodes;
        private int leafNodes;

        public void count(PolygonsSet polygonsSet) {
            leafNodes     = 0;
            internalNodes = 0;
            polygonsSet.getTree(false).visit(new BSPTreeVisitor<Vector2D>() {
                @Override
                public Order visitOrder(BSPTree<Vector2D> node) {
                    return Order.SUB_PLUS_MINUS;
                }
                @Override
                public void visitInternalNode(BSPTree<Vector2D> node) {
                    ++internalNodes;
                }
                @Override
                public void visitLeafNode(BSPTree<Vector2D> node) {
                    ++leafNodes;
                }

            });
        }

        public int getInternalNodes() {
            return internalNodes;
        }

        public int getLeafNodes() {
            return leafNodes;
        }
    }

    private PolygonsSet buildSet(Vector2D[][] vertices) {
        ArrayList<SubHyperplane<Vector2D>> edges = new ArrayList<>();
        for (int i = 0; i < vertices.length; ++i) {
            int l = vertices[i].length;
            for (int j = 0; j < l; ++j) {
                edges.add(buildSegment(vertices[i][j], vertices[i][(j + 1) % l]));
            }
        }
        return new PolygonsSet(edges, TEST_PRECISION);
    }

    private SubHyperplane<Vector2D> buildLine(Vector2D start, Vector2D end) {
        return new Line(start, end, TEST_PRECISION).wholeHyperplane();
    }

    private double intersectionAbscissa(Line l0, Line l1) {
        Vector2D p = l0.intersection(l1);
        return (l0.toSubSpace(p)).getX();
    }

    private SubHyperplane<Vector2D> buildHalfLine(Vector2D start, Vector2D end,
                                                     boolean startIsVirtual) {
        Line   line  = new Line(start, end, TEST_PRECISION);
        double lower = startIsVirtual ? Double.NEGATIVE_INFINITY : (line.toSubSpace(start)).getX();
        double upper = startIsVirtual ? (line.toSubSpace(end)).getX() : Double.POSITIVE_INFINITY;
        return new SubLine(line, new IntervalsSet(lower, upper, TEST_PRECISION));
    }

    private SubHyperplane<Vector2D> buildSegment(Vector2D start, Vector2D end) {
        Line   line  = new Line(start, end, TEST_PRECISION);
        double lower = (line.toSubSpace(start)).getX();
        double upper = (line.toSubSpace(end)).getX();
        return new SubLine(line, new IntervalsSet(lower, upper, TEST_PRECISION));
    }

    private void checkPoints(Region.Location expected, PolygonsSet poly, Vector2D ... points) {
        for (int i = 0; i < points.length; ++i) {
            Assert.assertEquals("Incorrect location for " + points[i], expected, poly.checkPoint(points[i]));
        }
    }

    /** Asserts that the two arrays of vertex loops have equivalent content.
     * @param expectedLoops
     * @param actualLoops
     */
    private void checkVertexLoopsEquivalent(Vector2D[][] expectedLoops, Vector2D[][] actualLoops) {
        Assert.assertEquals("Expected vertices array to have length of " + expectedLoops.length + " but was " + actualLoops.length,
                expectedLoops.length, actualLoops.length);

        // go through each loop in the expected array and try to find a match in the actual array
        for (Vector2D[] expectedLoop : expectedLoops) {
            boolean foundMatch = false;
            for (Vector2D[] actualLoop : actualLoops) {
                if (vertexLoopsEquivalent(expectedLoop, actualLoop, TEST_EPS)) {
                    foundMatch = true;
                    break;
                }
            }

            if (!foundMatch) {
                StringBuilder sb = new StringBuilder();
                for (Vector2D[] actualLoop : actualLoops) {
                    sb.append(Arrays.toString(actualLoop));
                    sb.append(", ");
                }
                if (sb.length() > 0) {
                    sb.delete(sb.length() - 2, sb.length());
                }
                Assert.fail("Failed to find vertex loop " + Arrays.toString(expectedLoop) + " in loop array [" +
                        sb.toString() + "].");
            }
        }
    }

    /** Returns true if the two sets of vertices can be considered equivalent using the given
     * tolerance. For open loops, (i.e. ones that start with null) this means that the two loops
     * must have the exact same elements in the exact same order. For closed loops, equivalent
     * means that one of the loops can be rotated to match the other (e.g. [3, 1, 2] is equivalent
     * to [1, 2, 3]).
     * @param a
     * @param b
     * @param tolerance
     * @return
     */
    private boolean vertexLoopsEquivalent(Vector2D[] a, Vector2D[] b, double tolerance) {
        if (a.length == b.length) {
            if (a.length < 1) {
                // the loops are empty
                return true;
            }
            if (a[0] == null || b[0] == null) {
                // at least one of the loops is unclosed, so there is only one
                // possible sequence that could match
                return vertexLoopsEqual(a, 0, b, 0, tolerance);
            }
            else {
                // the loops are closed so they could be equivalent but
                // start at different vertices
                for (int i=0; i<a.length; ++i) {
                    if (vertexLoopsEqual(a, 0, b, i, tolerance)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /** Returns true if the two vertex loops have the same elements, starting
     * from the given indices and allowing loop-around.
     * @param a
     * @param aStartIdx
     * @param b
     * @param bStartIdx
     * @param tolerance
     * @return
     */
    private boolean vertexLoopsEqual(Vector2D[] a, int aStartIdx,
            Vector2D[] b, int bStartIdx, double tolerance) {

        int len = a.length;

        Vector2D ptA;
        Vector2D ptB;
        for (int i=0; i<len; ++i) {
            ptA = a[(i + aStartIdx) % len];
            ptB = b[(i + bStartIdx) % len];

            if (!((ptA == null && ptB == null) ||
                    (Precision.equals(ptA.getX(), ptB.getX(), tolerance) &&
                     Precision.equals(ptA.getY(), ptB.getY(), tolerance)))) {
                return false;
            }
        }

        return true;
    }
}
