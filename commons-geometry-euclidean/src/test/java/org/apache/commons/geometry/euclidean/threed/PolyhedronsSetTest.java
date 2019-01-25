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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.partitioning.BSPTree;
import org.apache.commons.geometry.core.partitioning.BSPTreeVisitor;
import org.apache.commons.geometry.core.partitioning.BoundaryAttribute;
import org.apache.commons.geometry.core.partitioning.BoundaryProjection;
import org.apache.commons.geometry.core.partitioning.Region;
import org.apache.commons.geometry.core.partitioning.RegionFactory;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.geometry.euclidean.twod.SubLine;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.core.Precision;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.Assert;
import org.junit.Test;

public class PolyhedronsSetTest {

    private static final double TEST_TOLERANCE = 1e-10;

    @Test
    public void testWholeSpace() {
        // act
        PolyhedronsSet polySet = new PolyhedronsSet(TEST_TOLERANCE);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, polySet.getTolerance(), Precision.EPSILON);
        EuclideanTestUtils.assertPositiveInfinity(polySet.getSize());
        Assert.assertEquals(0.0, polySet.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.NaN, polySet.getBarycenter(), TEST_TOLERANCE);
        Assert.assertFalse(polySet.isEmpty());
        Assert.assertTrue(polySet.isFull());

        checkPoints(Region.Location.INSIDE, polySet,
                Vector3D.of(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE),
                Vector3D.of(-100, -100, -100),
                Vector3D.of(0, 0, 0),
                Vector3D.of(100, 100, 100),
                Vector3D.of(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE));
    }

    @Test
    public void testEmptyRegion() {
        // act
        PolyhedronsSet polySet = new PolyhedronsSet(new BSPTree<Vector3D>(Boolean.FALSE), TEST_TOLERANCE);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, polySet.getTolerance(), Precision.EPSILON);
        Assert.assertEquals(0.0, polySet.getSize(), TEST_TOLERANCE);
        Assert.assertEquals(0.0, polySet.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.NaN, polySet.getBarycenter(), TEST_TOLERANCE);
        Assert.assertTrue(polySet.isEmpty());
        Assert.assertFalse(polySet.isFull());

        checkPoints(Region.Location.OUTSIDE, polySet,
                Vector3D.of(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE),
                Vector3D.of(-100, -100, -100),
                Vector3D.of(0, 0, 0),
                Vector3D.of(100, 100, 100),
                Vector3D.of(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE));
    }

    @Test
    public void testHalfSpace() {
        // arrange
        List<SubHyperplane<Vector3D>> boundaries = new ArrayList<>();
        boundaries.add(new SubPlane(new Plane(Vector3D.ZERO, Vector3D.PLUS_Y, TEST_TOLERANCE),
                new PolygonsSet(TEST_TOLERANCE)));

        // act
        PolyhedronsSet polySet = new PolyhedronsSet(boundaries, TEST_TOLERANCE);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, polySet.getTolerance(), Precision.EPSILON);
        EuclideanTestUtils.assertPositiveInfinity(polySet.getSize());
        EuclideanTestUtils.assertPositiveInfinity(polySet.getBoundarySize());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.NaN, polySet.getBarycenter(), TEST_TOLERANCE);
        Assert.assertFalse(polySet.isEmpty());
        Assert.assertFalse(polySet.isFull());

        checkPoints(Region.Location.INSIDE, polySet,
                Vector3D.of(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE),
                Vector3D.of(-100, -100, -100));
        checkPoints(Region.Location.BOUNDARY, polySet, Vector3D.of(0, 0, 0));
        checkPoints(Region.Location.OUTSIDE, polySet,
                Vector3D.of(100, 100, 100),
                Vector3D.of(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE));
    }

    @Test
    public void testInvertedRegion() {
        // arrange
        List<SubHyperplane<Vector3D>> boundaries = createBoxBoundaries(Vector3D.ZERO, 1.0, TEST_TOLERANCE);
        PolyhedronsSet box = new PolyhedronsSet(boundaries, TEST_TOLERANCE);;

        // act
        PolyhedronsSet polySet = (PolyhedronsSet) new RegionFactory<Vector3D>().getComplement(box);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, polySet.getTolerance(), Precision.EPSILON);
        EuclideanTestUtils.assertPositiveInfinity(polySet.getSize());
        Assert.assertEquals(6, polySet.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.NaN, polySet.getBarycenter(), TEST_TOLERANCE);
        Assert.assertFalse(polySet.isEmpty());
        Assert.assertFalse(polySet.isFull());

        checkPoints(Region.Location.INSIDE, polySet,
                Vector3D.of(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE),
                Vector3D.of(-100, -100, -100),
                Vector3D.of(100, 100, 100),
                Vector3D.of(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE));
        checkPoints(Region.Location.OUTSIDE, polySet,
                Vector3D.of(0, 0, 0));
    }

    @Test
    public void testCreateFromBoundaries_noBoundaries_treeRepresentsWholeSpace() {
        // arrange
        List<SubHyperplane<Vector3D>> boundaries = new ArrayList<>();

        // act
        PolyhedronsSet polySet = new PolyhedronsSet(boundaries, TEST_TOLERANCE);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, polySet.getTolerance(), Precision.EPSILON);
        EuclideanTestUtils.assertPositiveInfinity(polySet.getSize());
        Assert.assertEquals(0.0, polySet.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.NaN, polySet.getBarycenter(), TEST_TOLERANCE);
        Assert.assertFalse(polySet.isEmpty());
        Assert.assertTrue(polySet.isFull());
    }

    @Test
    public void testCreateFromBoundaries_unitBox() {
        // arrange
        List<SubHyperplane<Vector3D>> boundaries = createBoxBoundaries(Vector3D.ZERO, 1.0, TEST_TOLERANCE);

        // act
        PolyhedronsSet polySet = new PolyhedronsSet(boundaries, TEST_TOLERANCE);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, polySet.getTolerance(), Precision.EPSILON);
        Assert.assertEquals(1.0, polySet.getSize(), TEST_TOLERANCE);
        Assert.assertEquals(6.0, polySet.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, polySet.getBarycenter(), TEST_TOLERANCE);
        Assert.assertFalse(polySet.isEmpty());
        Assert.assertFalse(polySet.isFull());

        checkPoints(Region.Location.OUTSIDE, polySet,
                Vector3D.of(-1, 0, 0),
                Vector3D.of(1, 0, 0),
                Vector3D.of(0, -1, 0),
                Vector3D.of(0, 1, 0),
                Vector3D.of(0, 0, -1),
                Vector3D.of(0, 0, 1),

                Vector3D.of(1, 1, 1),
                Vector3D.of(1, 1, -1),
                Vector3D.of(1, -1, 1),
                Vector3D.of(1, -1, -1),
                Vector3D.of(-1, 1, 1),
                Vector3D.of(-1, 1, -1),
                Vector3D.of(-1, -1, 1),
                Vector3D.of(-1, -1, -1));

        checkPoints(Region.Location.BOUNDARY, polySet,
                Vector3D.of(0.5, 0, 0),
                Vector3D.of(-0.5, 0, 0),
                Vector3D.of(0, 0.5, 0),
                Vector3D.of(0, -0.5, 0),
                Vector3D.of(0, 0, 0.5),
                Vector3D.of(0, 0, -0.5),

                Vector3D.of(0.5, 0.5, 0.5),
                Vector3D.of(0.5, 0.5, -0.5),
                Vector3D.of(0.5, -0.5, 0.5),
                Vector3D.of(0.5, -0.5, -0.5),
                Vector3D.of(-0.5, 0.5, 0.5),
                Vector3D.of(-0.5, 0.5, -0.5),
                Vector3D.of(-0.5, -0.5, 0.5),
                Vector3D.of(-0.5, -0.5, -0.5));

        checkPoints(Region.Location.INSIDE, polySet,
                Vector3D.of(0, 0, 0),

                Vector3D.of(0.4, 0.4, 0.4),
                Vector3D.of(0.4, 0.4, -0.4),
                Vector3D.of(0.4, -0.4, 0.4),
                Vector3D.of(0.4, -0.4, -0.4),
                Vector3D.of(-0.4, 0.4, 0.4),
                Vector3D.of(-0.4, 0.4, -0.4),
                Vector3D.of(-0.4, -0.4, 0.4),
                Vector3D.of(-0.4, -0.4, -0.4));
    }

    @Test
    public void testCreateFromBoundaries_twoBoxes_disjoint() {
        // arrange
        List<SubHyperplane<Vector3D>> boundaries = new ArrayList<>();
        boundaries.addAll(createBoxBoundaries(Vector3D.ZERO, 1.0, TEST_TOLERANCE));
        boundaries.addAll(createBoxBoundaries(Vector3D.of(2, 0, 0), 1.0, TEST_TOLERANCE));

        // act
        PolyhedronsSet polySet = new PolyhedronsSet(boundaries, TEST_TOLERANCE);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, polySet.getTolerance(), Precision.EPSILON);
        Assert.assertEquals(2.0, polySet.getSize(), TEST_TOLERANCE);
        Assert.assertEquals(12.0, polySet.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, 0), polySet.getBarycenter(), TEST_TOLERANCE);
        Assert.assertFalse(polySet.isEmpty());
        Assert.assertFalse(polySet.isFull());

        checkPoints(Region.Location.OUTSIDE, polySet,
                Vector3D.of(-1, 0, 0),
                Vector3D.of(1, 0, 0),
                Vector3D.of(3, 0, 0));

        checkPoints(Region.Location.INSIDE, polySet,
                Vector3D.of(0, 0, 0),
                Vector3D.of(2, 0, 0));
    }

    @Test
    public void testCreateFromBoundaries_twoBoxes_sharedSide() {
        // arrange
        List<SubHyperplane<Vector3D>> boundaries = new ArrayList<>();
        boundaries.addAll(createBoxBoundaries(Vector3D.of(0, 0, 0), 1.0, TEST_TOLERANCE));
        boundaries.addAll(createBoxBoundaries(Vector3D.of(1, 0, 0), 1.0, TEST_TOLERANCE));

        // act
        PolyhedronsSet polySet = new PolyhedronsSet(boundaries, TEST_TOLERANCE);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, polySet.getTolerance(), Precision.EPSILON);
        Assert.assertEquals(2.0, polySet.getSize(), TEST_TOLERANCE);
        Assert.assertEquals(10.0, polySet.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0, 0), polySet.getBarycenter(), TEST_TOLERANCE);
        Assert.assertFalse(polySet.isEmpty());
        Assert.assertFalse(polySet.isFull());

        checkPoints(Region.Location.OUTSIDE, polySet,
                Vector3D.of(-1, 0, 0),
                Vector3D.of(2, 0, 0));

        checkPoints(Region.Location.INSIDE, polySet,
                Vector3D.of(0, 0, 0),
                Vector3D.of(1, 0, 0));
    }

    @Test
    public void testCreateFromBoundaries_twoBoxes_separationLessThanTolerance() {
        // arrange
        double tolerance = 1e-6;
        List<SubHyperplane<Vector3D>> boundaries = new ArrayList<>();
        boundaries.addAll(createBoxBoundaries(Vector3D.of(0, 0, 0), 1.0, tolerance));
        boundaries.addAll(createBoxBoundaries(Vector3D.of(1 + 1e-7, 0, 0), 1.0, tolerance));

        // act
        PolyhedronsSet polySet = new PolyhedronsSet(boundaries, tolerance);

        // assert
        Assert.assertEquals(tolerance, polySet.getTolerance(), Precision.EPSILON);
        Assert.assertEquals(2.0, polySet.getSize(), tolerance);
        Assert.assertEquals(10.0, polySet.getBoundarySize(), tolerance);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5 + 5e-8, 0, 0), polySet.getBarycenter(), TEST_TOLERANCE);
        Assert.assertFalse(polySet.isEmpty());
        Assert.assertFalse(polySet.isFull());

        checkPoints(Region.Location.OUTSIDE, polySet,
                Vector3D.of(-1, 0, 0),
                Vector3D.of(2, 0, 0));

        checkPoints(Region.Location.INSIDE, polySet,
                Vector3D.of(0, 0, 0),
                Vector3D.of(1, 0, 0));
    }

    @Test
    public void testCreateFromBoundaries_twoBoxes_sharedEdge() {
        // arrange
        List<SubHyperplane<Vector3D>> boundaries = new ArrayList<>();
        boundaries.addAll(createBoxBoundaries(Vector3D.of(0, 0, 0), 1.0, TEST_TOLERANCE));
        boundaries.addAll(createBoxBoundaries(Vector3D.of(1, 1, 0), 1.0, TEST_TOLERANCE));

        // act
        PolyhedronsSet polySet = new PolyhedronsSet(boundaries, TEST_TOLERANCE);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, polySet.getTolerance(), Precision.EPSILON);
        Assert.assertEquals(2.0, polySet.getSize(), TEST_TOLERANCE);
        Assert.assertEquals(12.0, polySet.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0), polySet.getBarycenter(), TEST_TOLERANCE);
        Assert.assertFalse(polySet.isEmpty());
        Assert.assertFalse(polySet.isFull());

        checkPoints(Region.Location.OUTSIDE, polySet,
                Vector3D.of(-1, 0, 0),
                Vector3D.of(1, 0, 0),
                Vector3D.of(0, 1, 0),
                Vector3D.of(2, 1, 0));

        checkPoints(Region.Location.INSIDE, polySet,
                Vector3D.of(0, 0, 0),
                Vector3D.of(1, 1, 0));
    }

    @Test
    public void testCreateFromBoundaries_twoBoxes_sharedPoint() {
        // arrange
        List<SubHyperplane<Vector3D>> boundaries = new ArrayList<>();
        boundaries.addAll(createBoxBoundaries(Vector3D.of(0, 0, 0), 1.0, TEST_TOLERANCE));
        boundaries.addAll(createBoxBoundaries(Vector3D.of(1, 1, 1), 1.0, TEST_TOLERANCE));

        // act
        PolyhedronsSet polySet = new PolyhedronsSet(boundaries, TEST_TOLERANCE);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, polySet.getTolerance(), Precision.EPSILON);
        Assert.assertEquals(2.0, polySet.getSize(), TEST_TOLERANCE);
        Assert.assertEquals(12.0, polySet.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), polySet.getBarycenter(), TEST_TOLERANCE);
        Assert.assertFalse(polySet.isEmpty());
        Assert.assertFalse(polySet.isFull());

        checkPoints(Region.Location.OUTSIDE, polySet,
                Vector3D.of(-1, 0, 0),
                Vector3D.of(1, 0, 0),
                Vector3D.of(0, 1, 1),
                Vector3D.of(2, 1, 1));

        checkPoints(Region.Location.INSIDE, polySet,
                Vector3D.of(0, 0, 0),
                Vector3D.of(1, 1, 1));
    }

    @Test
    public void testCreateBox() {
        // act
        PolyhedronsSet tree = new PolyhedronsSet(0, 1, 0, 1, 0, 1, TEST_TOLERANCE);

        // assert
        Assert.assertEquals(1.0, tree.getSize(), TEST_TOLERANCE);
        Assert.assertEquals(6.0, tree.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), tree.getBarycenter(), TEST_TOLERANCE);

        for (double x = -0.25; x < 1.25; x += 0.1) {
            boolean xOK = (x >= 0.0) && (x <= 1.0);
            for (double y = -0.25; y < 1.25; y += 0.1) {
                boolean yOK = (y >= 0.0) && (y <= 1.0);
                for (double z = -0.25; z < 1.25; z += 0.1) {
                    boolean zOK = (z >= 0.0) && (z <= 1.0);
                    Region.Location expected =
                        (xOK && yOK && zOK) ? Region.Location.INSIDE : Region.Location.OUTSIDE;
                    Assert.assertEquals(expected, tree.checkPoint(Vector3D.of(x, y, z)));
                }
            }
        }
        checkPoints(Region.Location.BOUNDARY, tree, new Vector3D[] {
            Vector3D.of(0.0, 0.5, 0.5),
            Vector3D.of(1.0, 0.5, 0.5),
            Vector3D.of(0.5, 0.0, 0.5),
            Vector3D.of(0.5, 1.0, 0.5),
            Vector3D.of(0.5, 0.5, 0.0),
            Vector3D.of(0.5, 0.5, 1.0)
        });
        checkPoints(Region.Location.OUTSIDE, tree, new Vector3D[] {
            Vector3D.of(0.0, 1.2, 1.2),
            Vector3D.of(1.0, 1.2, 1.2),
            Vector3D.of(1.2, 0.0, 1.2),
            Vector3D.of(1.2, 1.0, 1.2),
            Vector3D.of(1.2, 1.2, 0.0),
            Vector3D.of(1.2, 1.2, 1.0)
        });
    }

    @Test
    public void testInvertedBox() {
        // arrange
        PolyhedronsSet tree = new PolyhedronsSet(0, 1, 0, 1, 0, 1, 1.0e-10);

        // act
        tree = (PolyhedronsSet) new RegionFactory<Vector3D>().getComplement(tree);

        // assert
        EuclideanTestUtils.assertPositiveInfinity(tree.getSize());
        Assert.assertEquals(6.0, tree.getBoundarySize(), 1.0e-10);

        Vector3D barycenter = tree.getBarycenter();
        Assert.assertTrue(Double.isNaN(barycenter.getX()));
        Assert.assertTrue(Double.isNaN(barycenter.getY()));
        Assert.assertTrue(Double.isNaN(barycenter.getZ()));

        for (double x = -0.25; x < 1.25; x += 0.1) {
            boolean xOK = (x < 0.0) || (x > 1.0);
            for (double y = -0.25; y < 1.25; y += 0.1) {
                boolean yOK = (y < 0.0) || (y > 1.0);
                for (double z = -0.25; z < 1.25; z += 0.1) {
                    boolean zOK = (z < 0.0) || (z > 1.0);
                    Region.Location expected =
                        (xOK || yOK || zOK) ? Region.Location.INSIDE : Region.Location.OUTSIDE;
                    Assert.assertEquals(expected, tree.checkPoint(Vector3D.of(x, y, z)));
                }
            }
        }
        checkPoints(Region.Location.BOUNDARY, tree, new Vector3D[] {
            Vector3D.of(0.0, 0.5, 0.5),
            Vector3D.of(1.0, 0.5, 0.5),
            Vector3D.of(0.5, 0.0, 0.5),
            Vector3D.of(0.5, 1.0, 0.5),
            Vector3D.of(0.5, 0.5, 0.0),
            Vector3D.of(0.5, 0.5, 1.0)
        });
        checkPoints(Region.Location.INSIDE, tree, new Vector3D[] {
            Vector3D.of(0.0, 1.2, 1.2),
            Vector3D.of(1.0, 1.2, 1.2),
            Vector3D.of(1.2, 0.0, 1.2),
            Vector3D.of(1.2, 1.0, 1.2),
            Vector3D.of(1.2, 1.2, 0.0),
            Vector3D.of(1.2, 1.2, 1.0)
        });
    }

    @Test
    public void testTetrahedron() {
        // arrange
        Vector3D vertex1 = Vector3D.of(1, 2, 3);
        Vector3D vertex2 = Vector3D.of(2, 2, 4);
        Vector3D vertex3 = Vector3D.of(2, 3, 3);
        Vector3D vertex4 = Vector3D.of(1, 3, 4);

        // act
        PolyhedronsSet tree =
            (PolyhedronsSet) new RegionFactory<Vector3D>().buildConvex(
                new Plane(vertex3, vertex2, vertex1, TEST_TOLERANCE),
                new Plane(vertex2, vertex3, vertex4, TEST_TOLERANCE),
                new Plane(vertex4, vertex3, vertex1, TEST_TOLERANCE),
                new Plane(vertex1, vertex2, vertex4, TEST_TOLERANCE));

        // assert
        Assert.assertEquals(1.0 / 3.0, tree.getSize(), TEST_TOLERANCE);
        Assert.assertEquals(2.0 * Math.sqrt(3.0), tree.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1.5, 2.5, 3.5), tree.getBarycenter(), TEST_TOLERANCE);

        double third = 1.0 / 3.0;
        checkPoints(Region.Location.BOUNDARY, tree, new Vector3D[] {
            vertex1, vertex2, vertex3, vertex4,
            Vector3D.linearCombination(third, vertex1, third, vertex2, third, vertex3),
            Vector3D.linearCombination(third, vertex2, third, vertex3, third, vertex4),
            Vector3D.linearCombination(third, vertex3, third, vertex4, third, vertex1),
            Vector3D.linearCombination(third, vertex4, third, vertex1, third, vertex2)
        });
        checkPoints(Region.Location.OUTSIDE, tree, new Vector3D[] {
            Vector3D.of(1, 2, 4),
            Vector3D.of(2, 2, 3),
            Vector3D.of(2, 3, 4),
            Vector3D.of(1, 3, 3)
        });
    }

    @Test
    public void testSphere() {
        // arrange
        // (use a high tolerance value here since the sphere is only an approximation)
        double approximationTolerance = 0.2;
        double radius = 1.0;

        // act
        PolyhedronsSet polySet = createSphere(Vector3D.of(1, 2, 3), radius, 8, 16);

        // assert
        Assert.assertEquals(sphereVolume(radius), polySet.getSize(), approximationTolerance);
        Assert.assertEquals(sphereSurface(radius), polySet.getBoundarySize(), approximationTolerance);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 3), polySet.getBarycenter(), TEST_TOLERANCE);
        Assert.assertFalse(polySet.isEmpty());
        Assert.assertFalse(polySet.isFull());

        checkPoints(Region.Location.OUTSIDE, polySet,
                Vector3D.of(-0.1, 2, 3),
                Vector3D.of(2.1, 2, 3),
                Vector3D.of(1, 0.9, 3),
                Vector3D.of(1, 3.1, 3),
                Vector3D.of(1, 2, 1.9),
                Vector3D.of(1, 2, 4.1),
                Vector3D.of(1.6, 2.6, 3.6));

        checkPoints(Region.Location.INSIDE, polySet,
                Vector3D.of(1, 2, 3),
                Vector3D.of(0.1, 2, 3),
                Vector3D.of(1.9, 2, 3),
                Vector3D.of(1, 2.1, 3),
                Vector3D.of(1, 2.9, 3),
                Vector3D.of(1, 2, 2.1),
                Vector3D.of(1, 2, 3.9),
                Vector3D.of(1.5, 2.5, 3.5));
    }

    @Test
    public void testIsometry() {
        // arrange
        Vector3D vertex1 = Vector3D.of(1.1, 2.2, 3.3);
        Vector3D vertex2 = Vector3D.of(2.0, 2.4, 4.2);
        Vector3D vertex3 = Vector3D.of(2.8, 3.3, 3.7);
        Vector3D vertex4 = Vector3D.of(1.0, 3.6, 4.5);

        // act
        PolyhedronsSet tree =
            (PolyhedronsSet) new RegionFactory<Vector3D>().buildConvex(
                new Plane(vertex3, vertex2, vertex1, TEST_TOLERANCE),
                new Plane(vertex2, vertex3, vertex4, TEST_TOLERANCE),
                new Plane(vertex4, vertex3, vertex1, TEST_TOLERANCE),
                new Plane(vertex1, vertex2, vertex4, TEST_TOLERANCE));

        // assert
        Vector3D barycenter = tree.getBarycenter();
        Vector3D s = Vector3D.of(10.2, 4.3, -6.7);
        Vector3D c = Vector3D.of(-0.2, 2.1, -3.2);
        QuaternionRotation r = QuaternionRotation.fromAxisAngle(Vector3D.of(6.2, -4.4, 2.1), 0.12);

        tree = tree.rotate(c, r).translate(s);

        Vector3D newB =
                Vector3D.linearCombination(1.0, s,
                         1.0, c,
                         1.0, r.apply(barycenter.subtract(c)));
        Assert.assertEquals(0.0,
                            newB.subtract(tree.getBarycenter()).norm(),
                            TEST_TOLERANCE);

        final Vector3D[] expectedV = new Vector3D[] {
                Vector3D.linearCombination(1.0, s,
                         1.0, c,
                         1.0, r.apply(vertex1.subtract(c))),
                            Vector3D.linearCombination(1.0, s,
                                      1.0, c,
                                      1.0, r.apply(vertex2.subtract(c))),
                                        Vector3D.linearCombination(1.0, s,
                                                   1.0, c,
                                                   1.0, r.apply(vertex3.subtract(c))),
                                                    Vector3D.linearCombination(1.0, s,
                                                                1.0, c,
                                                                1.0, r.apply(vertex4.subtract(c)))
        };
        tree.getTree(true).visit(new BSPTreeVisitor<Vector3D>() {

            @Override
            public Order visitOrder(BSPTree<Vector3D> node) {
                return Order.MINUS_SUB_PLUS;
            }

            @Override
            public void visitInternalNode(BSPTree<Vector3D> node) {
                @SuppressWarnings("unchecked")
                BoundaryAttribute<Vector3D> attribute =
                    (BoundaryAttribute<Vector3D>) node.getAttribute();
                if (attribute.getPlusOutside() != null) {
                    checkFacet((SubPlane) attribute.getPlusOutside());
                }
                if (attribute.getPlusInside() != null) {
                    checkFacet((SubPlane) attribute.getPlusInside());
                }
            }

            @Override
            public void visitLeafNode(BSPTree<Vector3D> node) {
            }

            private void checkFacet(SubPlane facet) {
                Plane plane = (Plane) facet.getHyperplane();
                Vector2D[][] vertices =
                    ((PolygonsSet) facet.getRemainingRegion()).getVertices();
                Assert.assertEquals(1, vertices.length);
                for (int i = 0; i < vertices[0].length; ++i) {
                    Vector3D v = plane.toSpace(vertices[0][i]);
                    double d = Double.POSITIVE_INFINITY;
                    for (int k = 0; k < expectedV.length; ++k) {
                        d = Math.min(d, v.subtract(expectedV[k]).norm());
                    }
                    Assert.assertEquals(0, d, TEST_TOLERANCE);
                }
            }

        });

    }

    @Test
    public void testBuildBox() {
        // arrange
        double x = 1.0;
        double y = 2.0;
        double z = 3.0;
        double w = 0.1;
        double l = 1.0;

        // act
        PolyhedronsSet tree =
            new PolyhedronsSet(x - l, x + l, y - w, y + w, z - w, z + w, TEST_TOLERANCE);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(x, y, z), tree.getBarycenter(), TEST_TOLERANCE);
        Assert.assertEquals(8 * l * w * w, tree.getSize(), TEST_TOLERANCE);
        Assert.assertEquals(8 * w * (2 * l + w), tree.getBoundarySize(), TEST_TOLERANCE);
    }

    @Test
    public void testCross() {
        // arrange
        double x = 1.0;
        double y = 2.0;
        double z = 3.0;
        double w = 0.1;
        double l = 1.0;
        PolyhedronsSet xBeam =
            new PolyhedronsSet(x - l, x + l, y - w, y + w, z - w, z + w, TEST_TOLERANCE);
        PolyhedronsSet yBeam =
            new PolyhedronsSet(x - w, x + w, y - l, y + l, z - w, z + w, TEST_TOLERANCE);
        PolyhedronsSet zBeam =
            new PolyhedronsSet(x - w, x + w, y - w, y + w, z - l, z + l, TEST_TOLERANCE);
        RegionFactory<Vector3D> factory = new RegionFactory<>();

        // act
        PolyhedronsSet tree = (PolyhedronsSet) factory.union(xBeam, factory.union(yBeam, zBeam));

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(x, y, z), tree.getBarycenter(), TEST_TOLERANCE);
        Assert.assertEquals(8 * w * w * (3 * l - 2 * w), tree.getSize(), TEST_TOLERANCE);
        Assert.assertEquals(24 * w * (2 * l - w), tree.getBoundarySize(), TEST_TOLERANCE);
    }

    // Issue MATH-780
    // See https://issues.apache.org/jira/browse/MATH-780
    @Test
    public void testCreateFromBoundaries_handlesSmallBoundariesCreatedDuringConstruction() {
        // arrange
        float[] coords = {
            1.000000f, -1.000000f, -1.000000f,
            1.000000f, -1.000000f, 1.000000f,
            -1.000000f, -1.000000f, 1.000000f,
            -1.000000f, -1.000000f, -1.000000f,
            1.000000f, 1.000000f, -1f,
            0.999999f, 1.000000f, 1.000000f,   // 1.000000f, 1.000000f, 1.000000f,
            -1.000000f, 1.000000f, 1.000000f,
            -1.000000f, 1.000000f, -1.000000f};
        int[] indices = {
            0, 1, 2, 0, 2, 3,
            4, 7, 6, 4, 6, 5,
            0, 4, 5, 0, 5, 1,
            1, 5, 6, 1, 6, 2,
            2, 6, 7, 2, 7, 3,
            4, 0, 3, 4, 3, 7};
        ArrayList<SubHyperplane<Vector3D>> subHyperplaneList = new ArrayList<>();
        for (int idx = 0; idx < indices.length; idx += 3) {
            int idxA = indices[idx] * 3;
            int idxB = indices[idx + 1] * 3;
            int idxC = indices[idx + 2] * 3;
            Vector3D v_1 = Vector3D.of(coords[idxA], coords[idxA + 1], coords[idxA + 2]);
            Vector3D v_2 = Vector3D.of(coords[idxB], coords[idxB + 1], coords[idxB + 2]);
            Vector3D v_3 = Vector3D.of(coords[idxC], coords[idxC + 1], coords[idxC + 2]);
            Vector3D[] vertices = {v_1, v_2, v_3};
            Plane polyPlane = new Plane(v_1, v_2, v_3, TEST_TOLERANCE);
            ArrayList<SubHyperplane<Vector2D>> lines = new ArrayList<>();

            Vector2D[] projPts = new Vector2D[vertices.length];
            for (int ptIdx = 0; ptIdx < projPts.length; ptIdx++) {
                projPts[ptIdx] = polyPlane.toSubSpace(vertices[ptIdx]);
            }

            SubLine lineInPlane = null;
            for (int ptIdx = 0; ptIdx < projPts.length; ptIdx++) {
                lineInPlane = new SubLine(projPts[ptIdx], projPts[(ptIdx + 1) % projPts.length], TEST_TOLERANCE);
                lines.add(lineInPlane);
            }
            Region<Vector2D> polyRegion = new PolygonsSet(lines, TEST_TOLERANCE);
            SubPlane polygon = new SubPlane(polyPlane, polyRegion);
            subHyperplaneList.add(polygon);
        }

        // act
        PolyhedronsSet polyhedronsSet = new PolyhedronsSet(subHyperplaneList, TEST_TOLERANCE);

        // assert
        Assert.assertEquals(8.0, polyhedronsSet.getSize(), 3.0e-6);
        Assert.assertEquals(24.0, polyhedronsSet.getBoundarySize(), 5.0e-6);
    }

    @Test
    public void testTooThinBox() {
        // act
        PolyhedronsSet polyhedronsSet = new PolyhedronsSet(0.0, 0.0, 0.0, 1.0, 0.0, 1.0, TEST_TOLERANCE);

        // assert
        Assert.assertEquals(0.0, polyhedronsSet.getSize(), TEST_TOLERANCE);
    }

    @Test
    public void testWrongUsage() {
        // the following is a wrong usage of the constructor.
        // as explained in the javadoc, the failure is NOT detected at construction
        // time but occurs later on
        PolyhedronsSet ps = new PolyhedronsSet(new BSPTree<Vector3D>(), TEST_TOLERANCE);
        Assert.assertNotNull(ps);
        try {
            ps.checkPoint(Vector3D.ZERO);
            Assert.fail("an exception should have been thrown");
        } catch (NullPointerException npe) {
            // this is expected
        }
    }

    @Test
    public void testDumpParse() throws IOException, ParseException {
        // arrange
        double tol=1e-8;

        Vector3D[] verts=new Vector3D[8];
        double xmin=-1,xmax=1;
        double ymin=-1,ymax=1;
        double zmin=-1,zmax=1;
        verts[0]=Vector3D.of(xmin,ymin,zmin);
        verts[1]=Vector3D.of(xmax,ymin,zmin);
        verts[2]=Vector3D.of(xmax,ymax,zmin);
        verts[3]=Vector3D.of(xmin,ymax,zmin);
        verts[4]=Vector3D.of(xmin,ymin,zmax);
        verts[5]=Vector3D.of(xmax,ymin,zmax);
        verts[6]=Vector3D.of(xmax,ymax,zmax);
        verts[7]=Vector3D.of(xmin,ymax,zmax);
        //
        int[][] faces=new int[12][];
        faces[0]=new int[]{3,1,0};  // bottom (-z)
        faces[1]=new int[]{1,3,2};  // bottom (-z)
        faces[2]=new int[]{5,7,4};  // top (+z)
        faces[3]=new int[]{7,5,6};  // top (+z)
        faces[4]=new int[]{2,5,1};  // right (+x)
        faces[5]=new int[]{5,2,6};  // right (+x)
        faces[6]=new int[]{4,3,0};  // left (-x)
        faces[7]=new int[]{3,4,7};  // left (-x)
        faces[8]=new int[]{4,1,5};  // front (-y)
        faces[9]=new int[]{1,4,0};  // front (-y)
        faces[10]=new int[]{3,6,2}; // back (+y)
        faces[11]=new int[]{6,3,7}; // back (+y)

        PolyhedronsSet polyset = new PolyhedronsSet(Arrays.asList(verts), Arrays.asList(faces), tol);

        // act
        String dump = EuclideanTestUtils.dump(polyset);
        PolyhedronsSet parsed = EuclideanTestUtils.parsePolyhedronsSet(dump);

        // assert
        Assert.assertEquals(8.0, polyset.getSize(), TEST_TOLERANCE);
        Assert.assertEquals(24.0, polyset.getBoundarySize(), TEST_TOLERANCE);

        Assert.assertEquals(8.0, parsed.getSize(), TEST_TOLERANCE);
        Assert.assertEquals(24.0, parsed.getBoundarySize(), TEST_TOLERANCE);
        Assert.assertTrue(new RegionFactory<Vector3D>().difference(polyset, parsed).isEmpty());
    }

    @Test
    public void testCreateFromBRep_connectedFacets() throws IOException, ParseException {
        InputStream stream = getClass().getResourceAsStream("pentomino-N.ply");
        PLYParser   parser = new PLYParser(stream);
        stream.close();
        PolyhedronsSet polyhedron = new PolyhedronsSet(parser.getVertices(), parser.getFaces(), TEST_TOLERANCE);
        Assert.assertEquals( 5.0, polyhedron.getSize(), TEST_TOLERANCE);
        Assert.assertEquals(22.0, polyhedron.getBoundarySize(), TEST_TOLERANCE);
    }

    @Test
    public void testCreateFromBRep_verticesTooClose() throws IOException, ParseException {
        checkError("pentomino-N-too-close.ply", "Vertices are too close");
    }

    @Test
    public void testCreateFromBRep_hole() throws IOException, ParseException {
        checkError("pentomino-N-hole.ply", "connected to one facet only");
    }

    @Test
    public void testCreateFromBRep_nonPlanar() throws IOException, ParseException {
        checkError("pentomino-N-out-of-plane.ply", "out of plane");
    }

    @Test
    public void testCreateFromBRep_badOrientation() throws IOException, ParseException {
        checkError("pentomino-N-bad-orientation.ply", "Facet orientation mismatch");
    }

    @Test
    public void testCreateFromBRep_wrongNumberOfPoints() throws IOException, ParseException {
        checkError(Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), Vector3D.of(0, 0, 1)),
                   Arrays.asList(new int[] { 0, 1, 2 }, new int[] {2, 3}),
                   "");
    }

    private void checkError(final String resourceName, final String expected) {
        try (InputStream stream = getClass().getResourceAsStream(resourceName)) {
            PLYParser parser = new PLYParser(stream);
            checkError(parser.getVertices(), parser.getFaces(), expected);
        } catch (IOException ioe) {
            Assert.fail(ioe.getLocalizedMessage());
        } catch (ParseException pe) {
            Assert.fail(pe.getLocalizedMessage());
        }
    }

    private void checkError(final List<Vector3D> vertices, final List<int[]> facets,
                            final String expected) {
        try {
            new PolyhedronsSet(vertices, facets, TEST_TOLERANCE);
            Assert.fail("an exception should have been thrown");
        } catch (IllegalArgumentException e) {
            String actual = e.getMessage();
            Assert.assertTrue("Expected string to contain \"" + expected + "\" but was \"" + actual + "\"",
                    actual.contains(expected));
        }
    }

    @Test
    public void testFirstIntersection() {
        // arrange
        List<SubHyperplane<Vector3D>> boundaries = createBoxBoundaries(Vector3D.ZERO, 2.0, TEST_TOLERANCE);
        PolyhedronsSet polySet = new PolyhedronsSet(boundaries, TEST_TOLERANCE);

        Line xPlus = new Line(Vector3D.ZERO, Vector3D.of(1, 0, 0), TEST_TOLERANCE);
        Line xMinus = new Line(Vector3D.ZERO, Vector3D.of(-1, 0, 0), TEST_TOLERANCE);

        Line yPlus = new Line(Vector3D.ZERO, Vector3D.of(0, 1, 0), TEST_TOLERANCE);
        Line yMinus = new Line(Vector3D.ZERO, Vector3D.of(0, -1, 0), TEST_TOLERANCE);

        Line zPlus = new Line(Vector3D.ZERO, Vector3D.of(0, 0, 1), TEST_TOLERANCE);
        Line zMinus = new Line(Vector3D.ZERO, Vector3D.of(0, 0, -1), TEST_TOLERANCE);

        // act/assert
        assertSubPlaneNormal(Vector3D.of(-1, 0, 0), polySet.firstIntersection(Vector3D.of(-1.1, 0, 0), xPlus));
        assertSubPlaneNormal(Vector3D.of(-1, 0, 0), polySet.firstIntersection(Vector3D.of(-1, 0, 0), xPlus));
        assertSubPlaneNormal(Vector3D.of(1, 0, 0), polySet.firstIntersection(Vector3D.of(-0.9, 0, 0), xPlus));
        Assert.assertEquals(null, polySet.firstIntersection(Vector3D.of(1.1, 0, 0), xPlus));

        assertSubPlaneNormal(Vector3D.of(1, 0, 0), polySet.firstIntersection(Vector3D.of(1.1, 0, 0), xMinus));
        assertSubPlaneNormal(Vector3D.of(1, 0, 0), polySet.firstIntersection(Vector3D.of(1, 0, 0), xMinus));
        assertSubPlaneNormal(Vector3D.of(-1, 0, 0), polySet.firstIntersection(Vector3D.of(0.9, 0, 0), xMinus));
        Assert.assertEquals(null, polySet.firstIntersection(Vector3D.of(-1.1, 0, 0), xMinus));

        assertSubPlaneNormal(Vector3D.of(0, -1, 0), polySet.firstIntersection(Vector3D.of(0, -1.1, 0), yPlus));
        assertSubPlaneNormal(Vector3D.of(0, -1, 0), polySet.firstIntersection(Vector3D.of(0, -1, 0), yPlus));
        assertSubPlaneNormal(Vector3D.of(0, 1, 0), polySet.firstIntersection(Vector3D.of(0, -0.9, 0), yPlus));
        Assert.assertEquals(null, polySet.firstIntersection(Vector3D.of(0, 1.1, 0), yPlus));

        assertSubPlaneNormal(Vector3D.of(0, 1, 0), polySet.firstIntersection(Vector3D.of(0, 1.1, 0), yMinus));
        assertSubPlaneNormal(Vector3D.of(0, 1, 0), polySet.firstIntersection(Vector3D.of(0, 1, 0), yMinus));
        assertSubPlaneNormal(Vector3D.of(0, -1, 0), polySet.firstIntersection(Vector3D.of(0, 0.9, 0), yMinus));
        Assert.assertEquals(null, polySet.firstIntersection(Vector3D.of(0, -1.1, 0), yMinus));

        assertSubPlaneNormal(Vector3D.of(0, 0, -1), polySet.firstIntersection(Vector3D.of(0, 0, -1.1), zPlus));
        assertSubPlaneNormal(Vector3D.of(0, 0, -1), polySet.firstIntersection(Vector3D.of(0, 0, -1), zPlus));
        assertSubPlaneNormal(Vector3D.of(0, 0, 1), polySet.firstIntersection(Vector3D.of(0, 0, -0.9), zPlus));
        Assert.assertEquals(null, polySet.firstIntersection(Vector3D.of(0, 0, 1.1), zPlus));

        assertSubPlaneNormal(Vector3D.of(0, 0, 1), polySet.firstIntersection(Vector3D.of(0, 0, 1.1), zMinus));
        assertSubPlaneNormal(Vector3D.of(0, 0, 1), polySet.firstIntersection(Vector3D.of(0, 0, 1), zMinus));
        assertSubPlaneNormal(Vector3D.of(0, 0, -1), polySet.firstIntersection(Vector3D.of(0, 0, 0.9), zMinus));
        Assert.assertEquals(null, polySet.firstIntersection(Vector3D.of(0, 0, -1.1), zMinus));
    }

    // issue GEOMETRY-38
    @Test
    public void testFirstIntersection_linesPassThroughBoundaries() {
        // arrange
        Vector3D lowerCorner = Vector3D.ZERO;
        Vector3D upperCorner = Vector3D.of(1, 1, 1);
        Vector3D center = lowerCorner.lerp(upperCorner, 0.5);

        List<SubHyperplane<Vector3D>> boundaries = createBoxBoundaries(center, 1.0, TEST_TOLERANCE);
        PolyhedronsSet polySet = new PolyhedronsSet(boundaries, TEST_TOLERANCE);

        Line upDiagonal = new Line(lowerCorner, upperCorner, TEST_TOLERANCE);
        Line downDiagonal = upDiagonal.revert();

        // act/assert
        SubPlane upFromOutsideResult = (SubPlane) polySet.firstIntersection(Vector3D.of(-1, -1, -1), upDiagonal);
        Assert.assertNotNull(upFromOutsideResult);
        EuclideanTestUtils.assertCoordinatesEqual(lowerCorner,
                ((Plane) upFromOutsideResult.getHyperplane()).intersection(upDiagonal), TEST_TOLERANCE);

        SubPlane upFromCenterResult = (SubPlane) polySet.firstIntersection(center, upDiagonal);
        Assert.assertNotNull(upFromCenterResult);
        EuclideanTestUtils.assertCoordinatesEqual(upperCorner,
                ((Plane) upFromCenterResult.getHyperplane()).intersection(upDiagonal), TEST_TOLERANCE);

        SubPlane downFromOutsideResult = (SubPlane) polySet.firstIntersection(Vector3D.of(2, 2, 2), downDiagonal);
        Assert.assertNotNull(downFromOutsideResult);
        EuclideanTestUtils.assertCoordinatesEqual(upperCorner,
                ((Plane) downFromOutsideResult.getHyperplane()).intersection(downDiagonal), TEST_TOLERANCE);

        SubPlane downFromCenterResult = (SubPlane) polySet.firstIntersection(center, downDiagonal);
        Assert.assertNotNull(downFromCenterResult);
        EuclideanTestUtils.assertCoordinatesEqual(lowerCorner,
                ((Plane) downFromCenterResult.getHyperplane()).intersection(downDiagonal), TEST_TOLERANCE);
    }


    // Issue 1211
    // See https://issues.apache.org/jira/browse/MATH-1211
    @Test
    public void testFirstIntersection_onlyReturnsPointsInDirectionOfRay() throws IOException, ParseException {
        // arrange
        PolyhedronsSet polyset = EuclideanTestUtils.parsePolyhedronsSet(loadTestData("issue-1211.bsp"));
        UniformRandomProvider random = RandomSource.create(RandomSource.WELL_1024_A, 0xb97c9d1ade21e40al);

        // act/assert
        int nrays = 1000;
        for (int i = 0; i < nrays; i++) {
            Vector3D origin    = Vector3D.ZERO;
            Vector3D direction = Vector3D.of(2 * random.nextDouble() - 1,
                                              2 * random.nextDouble() - 1,
                                              2 * random.nextDouble() - 1).normalize();
            Line line = new Line(origin, origin.add(direction), polyset.getTolerance());
            SubHyperplane<Vector3D> plane = polyset.firstIntersection(origin, line);
            if (plane != null) {
                Vector3D intersectionPoint = ((Plane)plane.getHyperplane()).intersection(line);
                double dotProduct = direction.dot(intersectionPoint.subtract(origin));
                Assert.assertTrue(dotProduct > 0);
            }
        }
    }

    @Test
    public void testBoolean_union() throws IOException {
        // arrange
        double tolerance = 0.05;
        double size = 1.0;
        double radius = size * 0.5;
        PolyhedronsSet box = new PolyhedronsSet(0, size, 0, size, 0, size, TEST_TOLERANCE);
        PolyhedronsSet sphere = createSphere(Vector3D.of(size * 0.5, size * 0.5, size), radius, 8, 16);

        // act
        PolyhedronsSet result = (PolyhedronsSet) new RegionFactory<Vector3D>().union(box, sphere);

        // OBJWriter.write("union.obj", result);

        // assert
        Assert.assertEquals(cubeVolume(size) + (sphereVolume(radius) * 0.5),
                result.getSize(), tolerance);
        Assert.assertEquals(cubeSurface(size) - circleSurface(radius) + (0.5 * sphereSurface(radius)),
                result.getBoundarySize(), tolerance);
        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.isFull());

        checkPoints(Region.Location.OUTSIDE, result,
                Vector3D.of(-0.1, 0.5, 0.5),
                Vector3D.of(1.1, 0.5, 0.5),
                Vector3D.of(0.5, -0.1, 0.5),
                Vector3D.of(0.5, 1.1, 0.5),
                Vector3D.of(0.5, 0.5, -0.1),
                Vector3D.of(0.5, 0.5, 1.6));

        checkPoints(Region.Location.INSIDE, result,
                Vector3D.of(0.1, 0.5, 0.5),
                Vector3D.of(0.9, 0.5, 0.5),
                Vector3D.of(0.5, 0.1, 0.5),
                Vector3D.of(0.5, 0.9, 0.5),
                Vector3D.of(0.5, 0.5, 0.1),
                Vector3D.of(0.5, 0.5, 1.4));
    }

    @Test
    public void testUnion_self() {
        // arrange
        double tolerance = 0.2;
        double radius = 1.0;

        PolyhedronsSet sphere = createSphere(Vector3D.ZERO, radius, 8, 16);

        // act
        PolyhedronsSet result = (PolyhedronsSet) new RegionFactory<Vector3D>().union(sphere, sphere.copySelf());

        // assert
        Assert.assertEquals(sphereVolume(radius), result.getSize(), tolerance);
        Assert.assertEquals(sphereSurface(radius), result.getBoundarySize(), tolerance);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, result.getBarycenter(), TEST_TOLERANCE);
        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.isFull());

        checkPoints(Region.Location.OUTSIDE, result,
                Vector3D.of(-1.1, 0, 0),
                Vector3D.of(1.1, 0, 0),
                Vector3D.of(0, -1.1, 0),
                Vector3D.of(0, 1.1, 0),
                Vector3D.of(0, 0, -1.1),
                Vector3D.of(0, 0, 1.1));

        checkPoints(Region.Location.INSIDE, result,
                Vector3D.of(-0.9, 0, 0),
                Vector3D.of(0.9, 0, 0),
                Vector3D.of(0, -0.9, 0),
                Vector3D.of(0, 0.9, 0),
                Vector3D.of(0, 0, -0.9),
                Vector3D.of(0, 0, 0.9),
                Vector3D.ZERO);
    }

    @Test
    public void testBoolean_intersection() throws IOException {
        // arrange
        double tolerance = 0.05;
        double size = 1.0;
        double radius = size * 0.5;
        PolyhedronsSet box = new PolyhedronsSet(0, size, 0, size, 0, size, TEST_TOLERANCE);
        PolyhedronsSet sphere = createSphere(Vector3D.of(size * 0.5, size * 0.5, size), radius, 8, 16);

        // act
        PolyhedronsSet result = (PolyhedronsSet) new RegionFactory<Vector3D>().intersection(box, sphere);

        // OBJWriter.write("intersection.obj", result);

        // assert
        Assert.assertEquals((sphereVolume(radius) * 0.5), result.getSize(), tolerance);
        Assert.assertEquals(circleSurface(radius) + (0.5 * sphereSurface(radius)),
                result.getBoundarySize(), tolerance);
        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.isFull());

        checkPoints(Region.Location.OUTSIDE, result,
                Vector3D.of(-0.1, 0.5, 1.0),
                Vector3D.of(1.1, 0.5, 1.0),
                Vector3D.of(0.5, -0.1, 1.0),
                Vector3D.of(0.5, 1.1, 1.0),
                Vector3D.of(0.5, 0.5, 0.4),
                Vector3D.of(0.5, 0.5, 1.1));

        checkPoints(Region.Location.INSIDE, result,
                Vector3D.of(0.1, 0.5, 0.9),
                Vector3D.of(0.9, 0.5, 0.9),
                Vector3D.of(0.5, 0.1, 0.9),
                Vector3D.of(0.5, 0.9, 0.9),
                Vector3D.of(0.5, 0.5, 0.6),
                Vector3D.of(0.5, 0.5, 0.9));
    }

    @Test
    public void testIntersection_self() {
        // arrange
        double tolerance = 0.2;
        double radius = 1.0;

        PolyhedronsSet sphere = createSphere(Vector3D.ZERO, radius, 8, 16);

        // act
        PolyhedronsSet result = (PolyhedronsSet) new RegionFactory<Vector3D>().intersection(sphere, sphere.copySelf());

        // assert
        Assert.assertEquals(sphereVolume(radius), result.getSize(), tolerance);
        Assert.assertEquals(sphereSurface(radius), result.getBoundarySize(), tolerance);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, result.getBarycenter(), TEST_TOLERANCE);
        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.isFull());

        checkPoints(Region.Location.OUTSIDE, result,
                Vector3D.of(-1.1, 0, 0),
                Vector3D.of(1.1, 0, 0),
                Vector3D.of(0, -1.1, 0),
                Vector3D.of(0, 1.1, 0),
                Vector3D.of(0, 0, -1.1),
                Vector3D.of(0, 0, 1.1));

        checkPoints(Region.Location.INSIDE, result,
                Vector3D.of(-0.9, 0, 0),
                Vector3D.of(0.9, 0, 0),
                Vector3D.of(0, -0.9, 0),
                Vector3D.of(0, 0.9, 0),
                Vector3D.of(0, 0, -0.9),
                Vector3D.of(0, 0, 0.9),
                Vector3D.ZERO);
    }

    @Test
    public void testBoolean_xor_twoCubes() throws IOException {
        // arrange
        double size = 1.0;
        PolyhedronsSet box1 = new PolyhedronsSet(
                0, size,
                0, size,
                0, size, TEST_TOLERANCE);
        PolyhedronsSet box2 = new PolyhedronsSet(
                0.5, size + 0.5,
                0.5, size + 0.5,
                0.5, size + 0.5, TEST_TOLERANCE);

        // act
        PolyhedronsSet result = (PolyhedronsSet) new RegionFactory<Vector3D>().xor(box1, box2);

        // OBJWriter.write("xor_twoCubes.obj", result);

        Assert.assertEquals((2 * cubeVolume(size)) - (2 * cubeVolume(size * 0.5)), result.getSize(), TEST_TOLERANCE);

        // assert
        Assert.assertEquals(2 * cubeSurface(size), result.getBoundarySize(), TEST_TOLERANCE);
        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.isFull());

        checkPoints(Region.Location.OUTSIDE, result,
                Vector3D.of(-0.1, -0.1, -0.1),
                Vector3D.of(0.75, 0.75, 0.75),
                Vector3D.of(1.6, 1.6, 1.6));

        checkPoints(Region.Location.BOUNDARY, result,
                Vector3D.of(0, 0, 0),
                Vector3D.of(0.5, 0.5, 0.5),
                Vector3D.of(1, 1, 1),
                Vector3D.of(1.5, 1.5, 1.5));

        checkPoints(Region.Location.INSIDE, result,
                Vector3D.of(0.1, 0.1, 0.1),
                Vector3D.of(0.4, 0.4, 0.4),
                Vector3D.of(1.1, 1.1, 1.1),
                Vector3D.of(1.4, 1.4, 1.4));
    }

    @Test
    public void testBoolean_xor_cubeAndSphere() throws IOException {
        // arrange
        double tolerance = 0.05;
        double size = 1.0;
        double radius = size * 0.5;
        PolyhedronsSet box = new PolyhedronsSet(0, size, 0, size, 0, size, TEST_TOLERANCE);
        PolyhedronsSet sphere = createSphere(Vector3D.of(size * 0.5, size * 0.5, size), radius, 8, 16);

        // act
        PolyhedronsSet result = (PolyhedronsSet) new RegionFactory<Vector3D>().xor(box, sphere);

        // OBJWriter.write("xor_cubeAndSphere.obj", result);

        Assert.assertEquals(cubeVolume(size), result.getSize(), tolerance);

        // assert
        Assert.assertEquals(cubeSurface(size) + (sphereSurface(radius)),
                result.getBoundarySize(), tolerance);
        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.isFull());

        checkPoints(Region.Location.OUTSIDE, result,
                Vector3D.of(-0.1, 0.5, 0.5),
                Vector3D.of(1.1, 0.5, 0.5),
                Vector3D.of(0.5, -0.1, 0.5),
                Vector3D.of(0.5, 1.1, 0.5),
                Vector3D.of(0.5, 0.5, -0.1),
                Vector3D.of(0.5, 0.5, 1.6),
                Vector3D.of(0.5, 0.5, 0.9));

        checkPoints(Region.Location.INSIDE, result,
                Vector3D.of(0.1, 0.5, 0.5),
                Vector3D.of(0.9, 0.5, 0.5),
                Vector3D.of(0.5, 0.1, 0.5),
                Vector3D.of(0.5, 0.9, 0.5),
                Vector3D.of(0.5, 0.5, 0.1),
                Vector3D.of(0.5, 0.5, 1.4));
    }

    @Test
    public void testXor_self() {
        // arrange
        double radius = 1.0;

        PolyhedronsSet sphere = createSphere(Vector3D.ZERO, radius, 8, 16);

        // act
        PolyhedronsSet result = (PolyhedronsSet) new RegionFactory<Vector3D>().xor(sphere, sphere.copySelf());

        // assert
        Assert.assertEquals(0.0, result.getSize(), TEST_TOLERANCE);
        Assert.assertEquals(0.0, result.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.NaN, result.getBarycenter(), TEST_TOLERANCE);
        Assert.assertTrue(result.isEmpty());
        Assert.assertFalse(result.isFull());

        checkPoints(Region.Location.OUTSIDE, result,
                Vector3D.of(-1.1, 0, 0),
                Vector3D.of(1.1, 0, 0),
                Vector3D.of(0, -1.1, 0),
                Vector3D.of(0, 1.1, 0),
                Vector3D.of(0, 0, -1.1),
                Vector3D.of(0, 0, 1.1),
                Vector3D.of(-0.9, 0, 0),
                Vector3D.of(0.9, 0, 0),
                Vector3D.of(0, -0.9, 0),
                Vector3D.of(0, 0.9, 0),
                Vector3D.of(0, 0, -0.9),
                Vector3D.of(0, 0, 0.9),
                Vector3D.ZERO);
    }

    @Test
    public void testBoolean_difference() throws IOException {
        // arrange
        double tolerance = 0.05;
        double size = 1.0;
        double radius = size * 0.5;
        PolyhedronsSet box = new PolyhedronsSet(0, size, 0, size, 0, size, TEST_TOLERANCE);
        PolyhedronsSet sphere = createSphere(Vector3D.of(size * 0.5, size * 0.5, size), radius, 8, 16);

        // act
        PolyhedronsSet result = (PolyhedronsSet) new RegionFactory<Vector3D>().difference(box, sphere);

        // OBJWriter.write("difference.obj", result);

        // assert
        Assert.assertEquals(cubeVolume(size) - (sphereVolume(radius) * 0.5), result.getSize(), tolerance);
        Assert.assertEquals(cubeSurface(size) - circleSurface(radius) + (0.5 * sphereSurface(radius)),
                result.getBoundarySize(), tolerance);
        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.isFull());

        checkPoints(Region.Location.OUTSIDE, result,
                Vector3D.of(-0.1, 0.5, 1.0),
                Vector3D.of(1.1, 0.5, 1.0),
                Vector3D.of(0.5, -0.1, 1.0),
                Vector3D.of(0.5, 1.1, 1.0),
                Vector3D.of(0.5, 0.5, -0.1),
                Vector3D.of(0.5, 0.5, 0.6));

        checkPoints(Region.Location.INSIDE, result,
                Vector3D.of(0.1, 0.5, 0.4),
                Vector3D.of(0.9, 0.5, 0.4),
                Vector3D.of(0.5, 0.1, 0.4),
                Vector3D.of(0.5, 0.9, 0.4),
                Vector3D.of(0.5, 0.5, 0.1),
                Vector3D.of(0.5, 0.5, 0.4));
    }

    @Test
    public void testDifference_self() {
        // arrange
        double radius = 1.0;

        PolyhedronsSet sphere = createSphere(Vector3D.ZERO, radius, 8, 16);

        // act
        PolyhedronsSet result = (PolyhedronsSet) new RegionFactory<Vector3D>().difference(sphere, sphere.copySelf());

        // assert
        Assert.assertEquals(0.0, result.getSize(), TEST_TOLERANCE);
        Assert.assertEquals(0.0, result.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.NaN, result.getBarycenter(), TEST_TOLERANCE);
        Assert.assertTrue(result.isEmpty());
        Assert.assertFalse(result.isFull());

        checkPoints(Region.Location.OUTSIDE, result,
                Vector3D.of(-1.1, 0, 0),
                Vector3D.of(1.1, 0, 0),
                Vector3D.of(0, -1.1, 0),
                Vector3D.of(0, 1.1, 0),
                Vector3D.of(0, 0, -1.1),
                Vector3D.of(0, 0, 1.1),
                Vector3D.of(-0.9, 0, 0),
                Vector3D.of(0.9, 0, 0),
                Vector3D.of(0, -0.9, 0),
                Vector3D.of(0, 0.9, 0),
                Vector3D.of(0, 0, -0.9),
                Vector3D.of(0, 0, 0.9),
                Vector3D.ZERO);
    }

    @Test
    public void testBoolean_multiple() throws IOException {
        // arrange
        double tolerance = 0.05;
        double size = 1.0;
        double radius = size * 0.5;
        PolyhedronsSet box = new PolyhedronsSet(0, size, 0, size, 0, size, TEST_TOLERANCE);
        PolyhedronsSet sphereToAdd = createSphere(Vector3D.of(size * 0.5, size * 0.5, size), radius, 8, 16);
        PolyhedronsSet sphereToRemove1 = createSphere(Vector3D.of(size * 0.5, 0, size * 0.5), radius, 8, 16);
        PolyhedronsSet sphereToRemove2 = createSphere(Vector3D.of(size * 0.5, 1, size * 0.5), radius, 8, 16);

        RegionFactory<Vector3D> factory = new RegionFactory<Vector3D>();

        // act
        PolyhedronsSet result = (PolyhedronsSet) factory.union(box, sphereToAdd);
        result = (PolyhedronsSet) factory.difference(result, sphereToRemove1);
        result = (PolyhedronsSet) factory.difference(result, sphereToRemove2);

        // OBJWriter.write("multiple.obj", result);

        // assert
        Assert.assertEquals(cubeVolume(size) - (sphereVolume(radius) * 0.5),
                result.getSize(), tolerance);
        Assert.assertEquals(cubeSurface(size) - (3.0 * circleSurface(radius)) + (1.5 * sphereSurface(radius)),
                result.getBoundarySize(), tolerance);
        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.isFull());

        checkPoints(Region.Location.OUTSIDE, result,
                Vector3D.of(-0.1, 0.5, 0.5),
                Vector3D.of(1.1, 0.5, 0.5),
                Vector3D.of(0.5, 0.4, 0.5),
                Vector3D.of(0.5, 0.6, 0.5),
                Vector3D.of(0.5, 0.5, -0.1),
                Vector3D.of(0.5, 0.5, 1.6));

        checkPoints(Region.Location.INSIDE, result,
                Vector3D.of(0.1, 0.5, 0.1),
                Vector3D.of(0.9, 0.5, 0.1),
                Vector3D.of(0.5, 0.4, 0.1),
                Vector3D.of(0.5, 0.6, 0.1),
                Vector3D.of(0.5, 0.5, 0.1),
                Vector3D.of(0.5, 0.5, 1.4));
    }

    @Test
    public void testProjectToBoundary() {
        // arrange
        PolyhedronsSet polySet = new PolyhedronsSet(0, 1, 0, 1, 0, 1, TEST_TOLERANCE);

        // act/assert
        checkProjectToBoundary(polySet, Vector3D.of(0.4, 0.5, 0.5),
                Vector3D.of(0, 0.5, 0.5), -0.4);
        checkProjectToBoundary(polySet, Vector3D.of(1.5, 0.5, 0.5),
                Vector3D.of(1, 0.5, 0.5), 0.5);
        checkProjectToBoundary(polySet, Vector3D.of(2, 2, 2),
                Vector3D.of(1, 1, 1), Math.sqrt(3));
    }

    @Test
    public void testProjectToBoundary_invertedRegion() {
        // arrange
        PolyhedronsSet polySet = new PolyhedronsSet(0, 1, 0, 1, 0, 1, TEST_TOLERANCE);
        polySet = (PolyhedronsSet) new RegionFactory<Vector3D>().getComplement(polySet);

        // act/assert
        checkProjectToBoundary(polySet, Vector3D.of(0.4, 0.5, 0.5),
                Vector3D.of(0, 0.5, 0.5), 0.4);
        checkProjectToBoundary(polySet, Vector3D.of(1.5, 0.5, 0.5),
                Vector3D.of(1, 0.5, 0.5), -0.5);
        checkProjectToBoundary(polySet, Vector3D.of(2, 2, 2),
                Vector3D.of(1, 1, 1), -Math.sqrt(3));
    }

    private void checkProjectToBoundary(PolyhedronsSet poly, Vector3D toProject,
            Vector3D expectedPoint, double expectedOffset) {
        BoundaryProjection<Vector3D> proj = poly.projectToBoundary(toProject);

        EuclideanTestUtils.assertCoordinatesEqual(toProject, proj.getOriginal(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(expectedPoint, proj.getProjected(), TEST_TOLERANCE);
        Assert.assertEquals(expectedOffset, proj.getOffset(), TEST_TOLERANCE);
    }

    private String loadTestData(final String resourceName)
            throws IOException {
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream(resourceName), "UTF-8")) {
            StringBuilder builder = new StringBuilder();
            for (int c = reader.read(); c >= 0; c = reader.read()) {
                builder.append((char) c);
            }
            return builder.toString();
        }
    }

    private void checkPoints(Region.Location expected, PolyhedronsSet poly, Vector3D ... points) {
        for (int i = 0; i < points.length; ++i) {
            Assert.assertEquals("Incorrect location for " + points[i], expected, poly.checkPoint(points[i]));
        }
    }

    private List<SubHyperplane<Vector3D>> createBoxBoundaries(Vector3D center, double size, double tolerance) {
        List<SubHyperplane<Vector3D>> boundaries = new ArrayList<>();

        double offset = size * 0.5;

        Plane xMinus = new Plane(center.add(Vector3D.of(-offset, 0, 0)), Vector3D.MINUS_X, tolerance);
        Plane xPlus = new Plane(center.add(Vector3D.of(offset, 0, 0)), Vector3D.PLUS_X, tolerance);
        Plane yPlus = new Plane(center.add(Vector3D.of(0, offset, 0)), Vector3D.PLUS_Y, tolerance);
        Plane yMinus = new Plane(center.add(Vector3D.of(0, -offset, 0)), Vector3D.MINUS_Y, tolerance);
        Plane zPlus = new Plane(center.add(Vector3D.of(0, 0, offset)), Vector3D.PLUS_Z, tolerance);
        Plane zMinus = new Plane(center.add(Vector3D.of(0, 0, -offset)), Vector3D.MINUS_Z, tolerance);

        // +x
        boundaries.add(createSubPlane(xPlus,
                        center.add(Vector3D.of(offset, offset, offset)),
                        center.add(Vector3D.of(offset, -offset, offset)),
                        center.add(Vector3D.of(offset, -offset, -offset)),
                        center.add(Vector3D.of(offset, offset, -offset))));

        // -x
        boundaries.add(createSubPlane(xMinus,
                        center.add(Vector3D.of(-offset, -offset, offset)),
                        center.add(Vector3D.of(-offset, offset, offset)),
                        center.add(Vector3D.of(-offset, offset, -offset)),
                        center.add(Vector3D.of(-offset, -offset, -offset))));

        // +y
        boundaries.add(createSubPlane(yPlus,
                        center.add(Vector3D.of(-offset, offset, offset)),
                        center.add(Vector3D.of(offset, offset, offset)),
                        center.add(Vector3D.of(offset, offset, -offset)),
                        center.add(Vector3D.of(-offset, offset, -offset))));

        // -y
        boundaries.add(createSubPlane(yMinus,
                        center.add(Vector3D.of(-offset, -offset, offset)),
                        center.add(Vector3D.of(-offset, -offset, -offset)),
                        center.add(Vector3D.of(offset, -offset, -offset)),
                        center.add(Vector3D.of(offset, -offset, offset))));

        // +z
        boundaries.add(createSubPlane(zPlus,
                        center.add(Vector3D.of(-offset, -offset, offset)),
                        center.add(Vector3D.of(offset, -offset, offset)),
                        center.add(Vector3D.of(offset, offset, offset)),
                        center.add(Vector3D.of(-offset, offset, offset))));

        // -z
        boundaries.add(createSubPlane(zMinus,
                        center.add(Vector3D.of(-offset, -offset, -offset)),
                        center.add(Vector3D.of(-offset, offset, -offset)),
                        center.add(Vector3D.of(offset, offset, -offset)),
                        center.add(Vector3D.of(offset, -offset, -offset))));

        return boundaries;
    }

    private SubPlane createSubPlane(Plane plane, Vector3D...points) {
        Vector2D[] points2d = new Vector2D[points.length];
        for (int i=0; i<points.length; ++i) {
            points2d[i] = plane.toSubSpace(points[i]);
        }

        PolygonsSet polygon = new PolygonsSet(plane.getTolerance(), points2d);

        return new SubPlane(plane, polygon);
    }

    private PolyhedronsSet createSphere(Vector3D center, double radius, int stacks, int slices) {
        List<Plane> planes = new ArrayList<>();

        // add top and bottom planes (+/- z)
        Vector3D topZ = Vector3D.of(center.getX(), center.getY(), center.getZ() + radius);
        Vector3D bottomZ = Vector3D.of(center.getX(), center.getY(), center.getZ() - radius);

        planes.add(new Plane(topZ, Vector3D.PLUS_Z, TEST_TOLERANCE));
        planes.add(new Plane(bottomZ, Vector3D.MINUS_Z, TEST_TOLERANCE));

        // add the side planes
        double vDelta = Math.PI / stacks;
        double hDelta = Math.PI * 2 / slices;

        double adjustedRadius = (radius + (radius * Math.cos(vDelta * 0.5))) / 2.0;

        double vAngle;
        double hAngle;
        double stackRadius;
        double stackHeight;
        double x, y;
        Vector3D pt;
        Vector3D norm;

        vAngle = -0.5 * vDelta;
        for (int v=0; v<stacks; ++v) {
            vAngle += vDelta;

            stackRadius = Math.sin(vAngle) * adjustedRadius;
            stackHeight = Math.cos(vAngle) * adjustedRadius;

            hAngle = -0.5 * hDelta;
            for (int h=0; h<slices; ++h) {
                hAngle += hDelta;

                x = Math.cos(hAngle) * stackRadius;
                y = Math.sin(hAngle) * stackRadius;

                norm = Vector3D.of(x, y, stackHeight).normalize();
                pt = center.add(norm.multiply(adjustedRadius));

                planes.add(new Plane(pt, norm, TEST_TOLERANCE));
            }
        }

        return (PolyhedronsSet) new RegionFactory<Vector3D>().buildConvex(planes.toArray(new Plane[0]));
    }

    private void assertSubPlaneNormal(Vector3D expectedNormal, SubHyperplane<Vector3D> sub) {
        Vector3D norm = ((Plane) sub.getHyperplane()).getNormal();
        EuclideanTestUtils.assertCoordinatesEqual(expectedNormal, norm, TEST_TOLERANCE);
    }

    private double cubeVolume(double size) {
        return size * size * size;
    }

    private double cubeSurface(double size) {
        return 6.0 * size * size;
    }

    private double sphereVolume(double radius) {
        return 4.0 * Math.PI * radius * radius * radius / 3.0;
    }

    private double sphereSurface(double radius) {
        return 4.0 * Math.PI * radius * radius;
    }

    private double circleSurface(double radius) {
        return Math.PI * radius * radius;
    }
}
