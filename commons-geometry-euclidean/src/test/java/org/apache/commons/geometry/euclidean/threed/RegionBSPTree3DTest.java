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

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class RegionBSPTree3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testCtor_default() {
        // act
        RegionBSPTree3D tree = new RegionBSPTree3D();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());
    }

    @Test
    public void testCtor_boolean() {
        // act
        RegionBSPTree3D a = new RegionBSPTree3D(true);
        RegionBSPTree3D b = new RegionBSPTree3D(false);

        // assert
        Assert.assertTrue(a.isFull());
        Assert.assertFalse(a.isEmpty());

        Assert.assertFalse(b.isFull());
        Assert.assertTrue(b.isEmpty());
    }

    @Test
    public void testEmpty() {
        // act
        RegionBSPTree3D tree = RegionBSPTree3D.empty();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());

        Assert.assertNull(tree.getBarycenter());
        Assert.assertEquals(0.0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
    }

    @Test
    public void testFull() {
        // act
        RegionBSPTree3D tree = RegionBSPTree3D.full();

        // assert
        Assert.assertTrue(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        Assert.assertNull(tree.getBarycenter());
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
        Assert.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
    }

    @Test
    public void testRect_deltaValues_positive() {
        // act
        RegionBSPTree3D tree = RegionBSPTree3D.rect(Vector3D.ZERO, 1, 1, 1, TEST_PRECISION);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), tree.getBarycenter(), TEST_EPS);
        Assert.assertEquals(1.0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(6, tree.getBoundarySize(), TEST_EPS);

        checkClassify(tree, RegionLocation.INSIDE, Vector3D.of(0.5, 0.5, 0.5));

        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-1, 0.5, 0.5), Vector3D.of(2, 0.5, 0.5),
                Vector3D.of(0.5, -1, 0.5), Vector3D.of(0.5, 2, 0.5),
                Vector3D.of(0.5, 0.5, -1), Vector3D.of(0.5, 0.5, 2));
    }

    @Test
    public void testRect_deltaValues_negative() {
        // act
        RegionBSPTree3D tree = RegionBSPTree3D.rect(Vector3D.ZERO, -1, -1, -1, TEST_PRECISION);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-0.5, -0.5, -0.5), tree.getBarycenter(), TEST_EPS);
        Assert.assertEquals(1.0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(6, tree.getBoundarySize(), TEST_EPS);

        checkClassify(tree, RegionLocation.INSIDE, Vector3D.of(-0.5, -0.5, -0.5));

        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-2, -0.5, -0.5), Vector3D.of(1, -0.5, -0.5),
                Vector3D.of(-0.5, -2, -0.5), Vector3D.of(-0.5, 1, -0.5),
                Vector3D.of(-0.5, -0.5, -2), Vector3D.of(-0.5, -0.5, 1));
    }

    @Test
    public void testRect_givenPoints() {
        // act
        RegionBSPTree3D tree = RegionBSPTree3D.rect(Vector3D.of(1, 0, 0), Vector3D.of(2, 2, 1), TEST_PRECISION);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1.5, 1, 0.5), tree.getBarycenter(), TEST_EPS);
        Assert.assertEquals(2.0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(10.0, tree.getBoundarySize(), TEST_EPS);

        checkClassify(tree, RegionLocation.INSIDE, Vector3D.of(1.5, 1, 0.5));

        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector3D.of(0.5, 1, 0.5), Vector3D.of(2.5, 1, 0.5),
                Vector3D.of(1.5, -1, 0.5), Vector3D.of(1.5, 3, 0.5),
                Vector3D.of(1.5, 1, -0.5), Vector3D.of(1.5, 1, 1.5));
    }

    // GEOMETRY-59
    @Test
    public void testSlightlyConcavePrism() {
        // arrange
        Vector3D vertices[] = {
            Vector3D.of( 0, 0, 0 ),
            Vector3D.of( 2, 1e-7, 0 ),
            Vector3D.of( 4, 0, 0 ),
            Vector3D.of( 2, 2, 0 ),
            Vector3D.of( 0, 0, 2 ),
            Vector3D.of( 2, 1e-7, 2 ),
            Vector3D.of( 4, 0, 2 ),
            Vector3D.of( 2, 2, 2 )
        };

        int facets[][] = {
            { 4, 5, 6, 7 },
            { 3, 2, 1, 0 },
            { 0, 1, 5, 4 },
            { 1, 2, 6, 5 },
            { 2, 3, 7, 6 },
            { 3, 0, 4, 7 }
        };

        // act
        RegionBSPTree3D tree = RegionBSPTree3D.fromFacets(vertices, facets, TEST_PRECISION);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        checkClassify(tree, RegionLocation.INSIDE, Vector3D.of(2, 1, 1));
        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector3D.of(2, 1, 3), Vector3D.of(2, 1, -3),
                Vector3D.of(2, -1, 1), Vector3D.of(2, 3, 1),
                Vector3D.of(-1, 1, 1), Vector3D.of(4, 1, 1));
    }

    private static void checkClassify(Region<Vector3D> region, RegionLocation loc, Vector3D ... points) {
        for (Vector3D point : points) {
            String msg = "Unexpected location for point " + point;

            Assert.assertEquals(msg, loc, region.classify(point));
        }
    }
}
